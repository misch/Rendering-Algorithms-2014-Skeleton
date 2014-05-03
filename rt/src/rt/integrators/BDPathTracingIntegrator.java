package rt.integrators;

import java.util.ArrayList;
import java.util.Random;

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
	private final int MAX_LIGHT_BOUNCES = 0;
	private final int MAX_EYE_BOUNCES = 5;
	
	public BDPathTracingIntegrator(Scene scene)
	{
		this.lightList = scene.getLightList();
		this.root = scene.getIntersectable();
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
			for(PathNode eye : eyeSubPath){
				Spectrum c = connect(eye, light, lightSubPath.get(0).hitRecord);
//				c.mult(1e-2f);
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
		lightNodes.add(new PathNode(lightHit,1,alpha,geometryTerm, pL, pE,false));
		
		// Get new direction
		ShadingSample emissionSample = lightHit.material.getEmissionSample(lightHit, sampler.makeSamples(1,2)[0]);
		Ray newRay = new Ray(lightHit.position, emissionSample.w, 1, true);
		HitRecord hit = root.intersect(newRay); // y1
		
		if (hit == null){
			return lightNodes;
		}
		
		alpha = new Spectrum(alpha);
		alpha.mult(lightHit.material.evaluateEmission(lightHit, emissionSample.w));
		alpha.mult(lightHit.normal.dot(emissionSample.w)/emissionSample.p);
		
		lightNodes.add(new PathNode(hit,2,alpha,1,0,0,false));
		
		// Trace light path
		int lightBounce = 3;
		boolean specular = false;
		while (true){
			
			ShadingSample newSample = hit.material.getShadingSample(hit, sampler.makeSamples(1, 2)[0]);
			Ray r = new Ray(hit.position,newSample.w,0,true);
			HitRecord newHit = root.intersect(r);
			
			if (newHit == null){
				break;
			}
			
			if (lightBounce > MAX_LIGHT_BOUNCES){
				break;
			}

			// Compute alpha value
			alpha = new Spectrum(alpha);			
			alpha.mult(newSample.brdf); // f(y_i-3 --> y_i-2 --> y_i-1)
			alpha.mult(hit.normal.dot(newSample.w)); // cos(theta_i-2 --> theta_i-2)

//			float Gp = 1/emissionSample.p;
//			
//			if (!specular){
//				Gp *= lightHit.normal.dot(emissionSample.w);
//			}
//			alpha.mult(Gp);
//			
//			float d = StaticVecmath.dist2(lightHit.position, hit.position);
//			geometryTerm = (lightHit.normal.dot(emissionSample.w) * hit.normal.dot(hit.w))/d;
//			
//			// pL = geometryTerm * (emissionSample.p/newHit.normal.dot(newHit.w));
//			// the cosine-term cancels out; to avoid numerical blah, just write it out easier:
//			pL = (lightHit.normal.dot(emissionSample.w)/d) * (emissionSample.p);
//			
//			// pE seems to be complicated to calculate...
//			pE = 0;		
			
			// add path node and update hit
			lightNodes.add(new PathNode(newHit,lightBounce,alpha,1,0,0,false));
			hit = newHit;
			
			lightBounce++;
		}
		
		return lightNodes;
	}

	private ArrayList<PathNode> createEyeSubPath(Ray r){
		ArrayList<PathNode> eyeNodes = new ArrayList<>();

		// Add t=1-node: point on the camera
		HitRecord camHit = new HitRecord();
		camHit.position = r.origin;
		eyeNodes.add(new PathNode(camHit,1,new Spectrum(1), 1,0,0,false));
		
		HitRecord hit = root.intersect(r);

		int eyeBounce = 2;
		Spectrum alpha = new Spectrum(1);
		boolean specular = false;
		while (true){			
			if (eyeBounce > MAX_EYE_BOUNCES){
				break;
			}
			
			if (hit == null){
				break;
			}
			
			if (hit.material.evaluateEmission(hit, hit.w) != null){ // will be handled by connect()-method (corresponds to case of s = 0
				eyeNodes.add(new PathNode(hit,0,null,0,0,0,false)); // only hit record is needed
				break; // last node 
			}
			
			// TODO: compute those terms
			float geometryTerm = 1;
			float pL = 0;
			float pE = 0;
			
			eyeNodes.add(new PathNode(hit,eyeBounce,alpha,geometryTerm,pL,pE,specular));
			
			// get to next node
			ShadingSample newSample = hit.material.getShadingSample(hit, sampler.makeSamples(1, 2)[0]);
			float cosTerm = newSample.w.dot(hit.normal);
			
			specular = newSample.isSpecular;
			Ray newRay = new Ray(hit.position, newSample.w, eyeBounce+1, true);
			hit = root.intersect(newRay);
			
			eyeBounce++;
		
			alpha.mult(newSample.brdf);

			if (!specular){
				alpha.mult(cosTerm);
			}
			alpha.mult(1/(newSample.p));;
		}
		
		return eyeNodes;
	}
	
	private Spectrum connect(PathNode eye, PathNode light, HitRecord sampledLightPoint) {
		Spectrum c_st;
		
		if (light.bounce == 1 && eye.bounce == 1){
			// Accounts for directly visible lights - ignore that one
			return new Spectrum();
		}
		
		Spectrum emission = eye.hitRecord.material.evaluateEmission(eye.hitRecord, eye.hitRecord.w);
		if (emission != null){ // that's the case of s = 0!
			// c_0t = emitted light 
			return new Spectrum(emission);
		}
		
		// Direction of the connecting ray: eyeNode --> lightNode
		Vector3f connectionDir = StaticVecmath.sub(light.hitRecord.position,eye.hitRecord.position);
		float d = connectionDir.lengthSquared();
		connectionDir.normalize();
		
		if (light.bounce == 1){
			c_st = light.hitRecord.material.evaluateEmission(light.hitRecord, StaticVecmath.negate(connectionDir));
			float geometryTerm = light.hitRecord.normal.dot(StaticVecmath.negate(connectionDir)) * eye.hitRecord.normal.dot(connectionDir);
			geometryTerm /= StaticVecmath.dist2(eye.hitRecord.position, light.hitRecord.position);
			Spectrum brdf = eye.hitRecord.material.evaluateBRDF(eye.hitRecord, connectionDir, eye.hitRecord.w);
			c_st.mult(geometryTerm);
			c_st.mult(brdf);
			return c_st;
		}
		
		assert(eye.bounce > 0);
		
		if (eye.bounce == 1){
			// Write contribution to separate buffer
		}
		
		// Geometry term (G in the green part on p.23)
		float cosTheta1;
		
		cosTheta1 = eye.hitRecord.normal.dot(connectionDir);
//		cosTheta1 = Math.max(0,cosTheta1);
			
		float cosTheta2 = light.hitRecord.normal.dot(StaticVecmath.negate(connectionDir));
//		cosTheta2 = Math.max(0,cosTheta2);
		
		float geometryTerm = (cosTheta1 * cosTheta2)/d;
		
		// BRDF-terms (f in the green part on p.23)
		Spectrum brdfEye, brdfLight;
		brdfEye = eye.hitRecord.material.evaluateBRDF(eye.hitRecord, eye.hitRecord.w, connectionDir);
		
		brdfLight = light.hitRecord.material.evaluateBRDF(light.hitRecord, light.hitRecord.w, StaticVecmath.negate(connectionDir));
		
		// compute c_st
		c_st = new Spectrum(brdfEye);
		c_st.mult(brdfLight);
		c_st.mult(geometryTerm);
		
		// multiply stuff together		
		c_st.mult(eye.alpha);
		c_st.mult(light.alpha);
		
		// shadow ray
		Ray shadowRay = new Ray(eye.hitRecord.position,connectionDir,0,true);
		HitRecord shadowHit = root.intersect(shadowRay);
		
		if(shadowHit != null){
			float lengthShadowHitToHitRecord = StaticVecmath.dist2(shadowHit.position, eye.hitRecord.position);
			if (d > lengthShadowHitToHitRecord + 1e-3f){
				return new Spectrum(1,1,0);
			}
		}
		
		return c_st;
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
		 * 			- bounce = 0 	==> 	no connection to light source is made - all segments are sampled from light
		 * 			- bounce = 1 	==> 	each vertex of the eye subpath will be connected with the (same) point on the light source
		 * - Eye subpath:
		 * 			- bounce = 0 	==> 	...? ignore for now (light image?)
		 * 			- bounce = 1 	==> 	each light hit will be connected to the eye
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
