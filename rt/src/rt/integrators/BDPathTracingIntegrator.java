package rt.integrators;

import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Integrator;
import rt.Intersectable;
import rt.LightGeometry;
import rt.LightList;
import rt.Material.ShadingSample;
import rt.Ray;
import rt.Sampler;
import rt.Scene;
import rt.Spectrum;
import rt.StaticVecmath;
import rt.samplers.RandomSampler;

/**
 * Integrator for Whitted style ray tracing. This is a basic version that needs to be extended!
 */
public class BDPathTracingIntegrator implements Integrator {

	LightList lightList;
	Intersectable root;
	Sampler sampler = new RandomSampler();
	private final int MAX_LIGHT_BOUNCES = 5;
	private final int MAX_EYE_BOUNCES = 2;
	private Spectrum[][] lightImg;
	private int[][] nSamples;
	private Scene scene;
	
	public BDPathTracingIntegrator(Scene scene)
	{
		this.lightList = scene.getLightList();
		this.root = scene.getIntersectable();
		this.scene = scene;
		this.lightImg = new Spectrum[scene.getFilm().getWidth()][scene.getFilm().getHeight()];
		
		for (int i = 0; i < lightImg[0].length; i++){
			for(int j = 0; j < lightImg[1].length; j++){
				this.lightImg[i][j] = new Spectrum();
			}
		}
		
		// initialize nSamples with 1's - will not be changed later because we want to have the lightimage to be unnormalized!
		this.nSamples = new int[scene.getFilm().getWidth()][scene.getFilm().getHeight()];
		for (int i = 0; i < nSamples[0].length; i++){
			for (int j = 0; j < nSamples[1].length; j++){
				this.nSamples[i][j] = 1;
			}
		}
	}

	/**
	 * Basic integrator that simply iterates over the light sources and accumulates
	 * their contributions. No shadow testing, reflection, refraction, or 
	 * area light sources, etc. supported.
	 */
	public Spectrum integrate(Ray r) {
		
		Spectrum outgoing = new Spectrum();
		
		ArrayList<PathNode> lightSubPath = createLightSubPath();
		ArrayList<PathNode> eyeSubPath = createEyeSubPath(r);
		
		for(PathNode light : lightSubPath){
			if (light.bounce > 1){
				connectToCamera(light,r.origin);
			}

			for(PathNode eye : eyeSubPath){
				Spectrum c = connect(eye, light, new Vector3f(r.origin));
				outgoing.add(c);
			}
		}
		return outgoing;
		
	}
	
	private ArrayList<PathNode> createLightSubPath() {
		ArrayList<PathNode> lightNodes = new ArrayList<PathNode>();
		
		// Sample a random light source
		LightGeometry lightSource = lightList.getRandomLightSource();		
		HitRecord lightHit = lightSource.sample(sampler.makeSamples(1, 2)[0]);
		
		// Compute alpha:
		// alpha = 1/p(y0) = 1/(1/area * 1/#lightSources) = 1/(lightHit.p * 1/#lightSources) = #lightSources/lightHit.p
		Spectrum alpha = new Spectrum(lightList.size()/lightHit.p);
		
		// Geometry term
		float geometryTerm = 1;
		
		// compute pL: probability for sampling the vertex from a light.
		// We have a special case here, because we are directly on the light;
		// therefore it's simply the probability according to our light sampling strategy, so
		// p = 1/(area*#lightSources)
		float pL = lightHit.p/lightList.size();
		
		// compute pE: probability for sampling the PREVIOUS vertex from the eye, via the now current vertex
		// My guess is 0 because the probability that, starting from the current vertex on the light source, 
		// the eye subpath would never continue, because simply the emitted light would somehow be added
		float pE = 0;
		
		// Add node to path
		if (MAX_LIGHT_BOUNCES >= 1){
			lightNodes.add(new PathNode(lightHit,1,alpha,geometryTerm, pL, pE,false));
		} else{
			return lightNodes;
		}
		
		// Get new direction
		ShadingSample emissionSample = lightHit.material.getEmissionSample(lightHit, sampler.makeSamples(1,2)[0]);
		Ray newRay = new Ray(lightHit.position, emissionSample.w, 1, true);
		HitRecord hit = root.intersect(newRay); // y1
		
		if (hit == null){
			return lightNodes;
		}
		
		if (MAX_LIGHT_BOUNCES >= 2){
			alpha = new Spectrum(alpha);
			alpha.mult(lightHit.material.evaluateEmission(lightHit, emissionSample.w));
			alpha.mult(lightHit.normal.dot(emissionSample.w)/emissionSample.p);
			geometryTerm = lightHit.normal.dot(emissionSample.w) * hit.normal.dot(hit.w);
			geometryTerm /= StaticVecmath.dist2(lightHit.position,hit.position);

			// TODO Simpler calculation? Cosine factor in geometry term cancels out...!
			pL = emissionSample.p;
			pL /= hit.normal.dot(hit.w);
			pL *= geometryTerm;
			
			// TODO Simpler calculation? Cosine factor in geometry term cancels out...!
			pE = hit.material.getDirectionalProbability(hit, hit.w);
			pE /= lightHit.normal.dot(emissionSample.w);
			pE *= geometryTerm;
			
			lightNodes.add(new PathNode(hit,2,alpha,geometryTerm,pL,pE,false));
		} else{
			return lightNodes;
		}
		
		// Trace light path
		int lightBounce = 3;
		boolean specular = false;
		while (true){
			if (lightBounce > MAX_LIGHT_BOUNCES){
				break;
			}
			
			ShadingSample newSample = hit.material.getShadingSample(hit, sampler.makeSamples(1, 2)[0]);

			if(newSample == null){
				break;
			}
			
			specular = newSample.isSpecular;
			Ray r = new Ray(hit.position,newSample.w,0,true);
			HitRecord newHit = root.intersect(r);
			
			if (newHit == null || newHit.material.evaluateEmission(newHit, newSample.w) != null){
				break;
			}
			
			// Compute alpha value
			alpha = new Spectrum(alpha);			
			alpha.mult(newSample.brdf); // f(y_i-3 --> y_i-2 --> y_i-1)
			if (!specular){
				alpha.mult(Math.max(hit.normal.dot(newSample.w),0)); // cos(theta_i-2 --> theta_i-2)
			}
			alpha.mult(1/newSample.p);
			geometryTerm = hit.normal.dot(newSample.w);
			geometryTerm *= newHit.normal.dot(newHit.w);
			geometryTerm /= StaticVecmath.dist2(hit.position, newHit.position);
//			float Gp = 1/emissionSample.p;
//			
//			if (!specular){
//				Gp *= lightHit.normal.dot(emissionSample.w);
//			}
//			alpha.mult(Gp);
//			

			// TODO Simpler calculation? Cosine factor in geometry term cancels out...!
			pL = newSample.p;
			pL /= newHit.normal.dot(newHit.w);
			pL *= geometryTerm;

			// TODO Simpler calculation? Cosine factor in geometry term cancels out...!
			pE = newHit.material.getDirectionalProbability(newHit, newHit.w);
			pE /= hit.normal.dot(newSample.w);
			pE *= geometryTerm;
			
			// add path node and update hit
			lightNodes.add(new PathNode(newHit,lightBounce,alpha,geometryTerm,pL,pE,specular));
			hit = newHit;
			
			lightBounce++;
		}
		
		return lightNodes;
	}

	private ArrayList<PathNode> createEyeSubPath(Ray r){
		ArrayList<PathNode> eyeNodes = new ArrayList<>();
		
		HitRecord hit = root.intersect(r);

		if (hit == null){
			return eyeNodes;
		}
		int eyeBounce = 2;
		Spectrum alpha = new Spectrum(1);
		boolean specular = false;
		float geometryTerm = Math.max(hit.normal.dot(hit.w),0)/StaticVecmath.dist2(hit.position, r.origin);
		// TODO: ??? multiply geometryTerm by cosine between look-at and r?
		assert(geometryTerm >= 0);
		
		float pL = 0; 	// TODO: not sure... 
						// But the probability to hit the camera position from the first hit is 0 because cam is infinitely small...?
						// Could also be simply the probability to sample this direction... O.o
		float pE = hit.normal.dot(hit.w)/StaticVecmath.dist2(hit.position, r.origin);
		while (true){			
			if (eyeBounce > MAX_EYE_BOUNCES){
				break;
			}
			
			if (hit.material.evaluateEmission(hit, hit.w) != null){ // will be handled by connect()-method (corresponds to case of s = 0
				eyeNodes.add(new PathNode(hit,eyeBounce,alpha,0,pE,0,specular));
				break; // last node 
			}
			
			eyeNodes.add(new PathNode(hit,eyeBounce,alpha,geometryTerm,pL,pE,specular));
			
			// get to next node
			ShadingSample newSample = hit.material.getShadingSample(hit, sampler.makeSamples(1, 2)[0]);
			float cosTerm = newSample.w.dot(hit.normal);
			
			specular = newSample.isSpecular;
			Ray newRay = new Ray(hit.position, newSample.w, eyeBounce+1, true);
			hit = root.intersect(newRay);
			
			if (hit == null){
				break;
			}
			
			geometryTerm = cosTerm * hit.normal.dot(hit.w);
			geometryTerm /= StaticVecmath.dist2(newRay.origin, hit.position);
			
			alpha = new Spectrum(alpha);
			alpha.mult(newSample.brdf);

			if (!specular){
				alpha.mult(cosTerm);
			}
			alpha.mult(1/(newSample.p));
			
			// TODO Maybe do a simpler calculation since [hit.normal.dot(hit.w)] could be cancelled out?!
			pE = newSample.p/(hit.normal.dot(hit.w));
			pE *= geometryTerm;
			
			// TODO Maybe do a simpler calculation since [cosTerm] could be cancelled out!
			pL = hit.material.getDirectionalProbability(hit, hit.w);
			pL /= cosTerm;
			pL *= geometryTerm;
			
			eyeBounce++;
		}
		
		return eyeNodes;
	}
	
	private Spectrum connect(PathNode eye, PathNode light, Vector3f cameraPosition) {
		Spectrum c_st;
		// Direction of the connecting ray: eyeNode --> lightNode
		Vector3f connectionDir = StaticVecmath.sub(light.hitRecord.position,eye.hitRecord.position);
		float d = connectionDir.lengthSquared();
		connectionDir.normalize();
		
		// shadow ray
		Ray shadowRay = new Ray(eye.hitRecord.position,connectionDir,0,true);
		HitRecord shadowHit = root.intersect(shadowRay);
		
		if(shadowHit != null){
			float lengthShadowHitToHitRecord = StaticVecmath.dist2(shadowHit.position, eye.hitRecord.position);
			if (d > lengthShadowHitToHitRecord + 1e-3f){
				return new Spectrum();
			}
		}
				
		Spectrum emission = eye.hitRecord.material.evaluateEmission(eye.hitRecord, eye.hitRecord.w);
		if (emission != null && (eye.bounce == 2 || eye.specular)){ // that's the case of s = 0!

			return new Spectrum(emission);
		}
				
		assert(eye.bounce > 0);
		
		
		// Geometry term (G in the green part on p.23)
		float cosTheta1;
		
		cosTheta1 = Math.max(eye.hitRecord.normal.dot(connectionDir),0);
		
		float cosTheta2 = Math.max(light.hitRecord.normal.dot(StaticVecmath.negate(connectionDir)),0);
		
		float geometryTerm = (cosTheta1 * cosTheta2)/d;
		
		// BRDF-terms (f in the green part on p.23)
		Spectrum brdfEye, brdfLight;

		brdfEye = eye.hitRecord.material.evaluateBRDF(eye.hitRecord, eye.hitRecord.w, connectionDir);
		
		if (light.bounce == 1){
			brdfLight = new Spectrum(light.hitRecord.material.evaluateEmission(light.hitRecord, StaticVecmath.negate(connectionDir)));
		}else{
			brdfLight = light.hitRecord.material.evaluateBRDF(light.hitRecord, light.hitRecord.w, StaticVecmath.negate(connectionDir));
		}
		
		// compute c_st
		c_st = new Spectrum(brdfEye);
		c_st.mult(brdfLight);
		c_st.mult(geometryTerm);
		
		// multiply stuff together		
		c_st.mult(eye.alpha);
		c_st.mult(light.alpha);
			
		return c_st; // This corresponds to C*_st in Veach, p.302 (or p. 305, eq. (10.8))
		
	}

	private void connectToCamera(PathNode light, Vector3f cameraPosition) {
		Vector3f lightDir = StaticVecmath.sub(light.hitRecord.position,cameraPosition);
		float distanceToCamera = lightDir.lengthSquared();
		lightDir.normalize();
		
		Ray lightRay = new Ray(light.hitRecord.position,StaticVecmath.negate(lightDir),0,true);
		HitRecord lightToCam = root.intersect(lightRay);
		
		if (lightToCam == null || distanceToCamera <= StaticVecmath.dist2(light.hitRecord.position, lightToCam.position) + 1e-3f){
			int[] pixels = this.scene.getCamera().getPixel(lightRay);
		
			if(pixels[0] >= 0 && pixels[0] < scene.getFilm().getWidth() && pixels[1] >= 0 && pixels[1] < scene.getFilm().getHeight()){
				Spectrum em = light.hitRecord.material.evaluateEmission(light.hitRecord, StaticVecmath.negate(lightDir));
				if (em == null){
					em = new Spectrum(light.hitRecord.material.evaluateBRDF(light.hitRecord, StaticVecmath.negate(lightDir), light.hitRecord.w));
				}
				
				float cosTerm = light.hitRecord.normal.dot(StaticVecmath.negate(lightDir));
				cosTerm /= distanceToCamera;
				em.mult(cosTerm);
				// TODO: ??? cosine between look-at direction and StaticVecmath.negate(lightDir) ???
				lightImg[pixels[0]][pixels[1]].add(em);
			}
		}
		
	}

	public Spectrum[][] getLightImg(){
		return this.lightImg;
	}
	
	public int[][] getNSamples(){
		return this.nSamples;
	}
	
	private float russianRouletteProbability(int bounce) {
		if (bounce <= 3){
			return 0;
		}
		return 0.5f;
	}

	public float[][] makePixelSamples(Sampler sampler, int n) {
		return sampler.makeSamples(n, 2);
	}
	
	private class PathNode{
		HitRecord hitRecord;
		boolean specular;
		
		/* Corresponds to s in light subpath and t in eye subpath.
		 * - Light subpath: 
		 * 			- bounce = 0 	==> 	no connection to light source is made - all segments are sampled from eye
		 * 			- bounce = 1 	==> 	each vertex of the eye subpath will be connected with the (same) point on the light source
		 * - Eye subpath:
		 * 			- bounce = 0 	==> 	ignore since our camera is infinitely small and will never be hit by accident
		 * 			- bounce = 1 	==> 	each light hit will be connected to the eye, contribution added to light image
		 */
		int bounce;
		Spectrum alpha;
		float geometryTerm;
		float probabilityLight, probabilityEye;
		
		public PathNode(HitRecord hitRecord, int bounce, Spectrum alpha, float G, float pL, float pE, boolean specular){
			this.hitRecord = hitRecord;
			this.bounce = bounce;
			this.alpha = alpha;
			this.geometryTerm = G;
			this.probabilityLight = pL;
			this.probabilityEye = pE;
			this.specular = specular;
		}
	}
}
