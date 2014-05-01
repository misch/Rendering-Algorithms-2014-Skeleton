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
	private final int MAX_EYE_BOUNCES = 1;
	
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
				Spectrum c = connect(eye, light);
				outgoing.add(c);
			}
		}
		
		return outgoing;
		
	}
	
	private ArrayList<PathNode> createLightSubPath() {
		ArrayList<PathNode> lightNodes = new ArrayList<PathNode>();
		// Sample a random light source
		LightGeometry lightSource = lightList.getRandomLightSource();		
		float[][] sample = sampler.makeSamples(1, 2);
		
		// First node (lies on the light source):
		HitRecord lightHit = lightSource.sample(sample[0]);
		// Compute alphaL_1:
		// alphaL_1 = 1/p(y0) = 1/(1/area * 1/#lightSources) = 1/(lightHit.p * 1/#lightSources) = #lightSources/lightHit.p
		Spectrum alpha = new Spectrum(lightList.size()/lightHit.p);
		
		// Geometry term
		// TODO not sure...?
		float geometryTerm = 1;
		
		// compute pL: probability for sampling the vertex from a light.
		// We have a special case here, because we are directly on the light;
		// therefore it's simply the probability according to our light sampling strategy, so
		// p = 1/(area*#lightSources)
		float pL = lightHit.p/lightList.size();
		
		// compute pE: probability for sampling the PREVIOUS vertex from the eye, via the now current vertex
		// TODO: No idea what this should be...
		// My guess is 0 because the probability that, starting from the current vertex on the light source, 
		// the eye subpath would never continue, because simply the emitted light would somehow be added
		float pE = 0;
		
		// Add node to path
		lightNodes.add(new PathNode(lightHit,0,alpha,geometryTerm, pL, pE));
		
		// Get new direction
		ShadingSample emissionSample = lightHit.material.getEmissionSample(lightHit, sampler.makeSamples(1,2)[0]);

		// Trace light path
		int lightBounce = 1;
		boolean specular = emissionSample.isSpecular;
		while (true){
			
			Ray newRay = new Ray(lightHit.position, emissionSample.w, lightBounce+1, true);
			HitRecord newHit = root.intersect(newRay);
			
			if (newHit == null){
				break;
			}
			
			if (lightBounce > MAX_LIGHT_BOUNCES){
				break;
			}
			
			// Compute alpha value
			alpha = new Spectrum(alpha);			
			alpha.mult(emissionSample.brdf);

			float Gp = 1/emissionSample.p;
			
			// TODO No idea if this makes sense.
			if (!specular){
				Gp *= lightHit.normal.dot(emissionSample.w);
			}
			alpha.mult(Gp);
			
			float d = StaticVecmath.dist2(lightHit.position, newHit.position);
			geometryTerm = (lightHit.normal.dot(emissionSample.w) * newHit.normal.dot(newHit.w))/d;
			
			// pL = geometryTerm * (emissionSample.p/newHit.normal.dot(newHit.w));
			// the cosine-term cancels out; to avoid numerical blah, just write it out easier:
			pL = (lightHit.normal.dot(emissionSample.w)/d) * (emissionSample.p);
			
			// pE seems to be complicated to calculate...
			pE = 0;		
			
			// add path node and update lightHit
			lightNodes.add(new PathNode(newHit,lightBounce,alpha,geometryTerm,pL,pE));
			lightHit = newHit;
			
			// Get new sample
			emissionSample = lightHit.material.getShadingSample(lightHit, sampler.makeSamples(1, 2)[0]);
			specular = emissionSample.isSpecular;
			
			lightBounce++;
		}
		
		return lightNodes;
	}

	private ArrayList<PathNode> createEyeSubPath(Ray r){
		ArrayList<PathNode> eyeNodes = new ArrayList<>();

		HitRecord hit = root.intersect(r);

		int eyeBounce = 1;
		Spectrum alpha = new Spectrum(1);
		
		while (true){			
			if (eyeBounce > MAX_EYE_BOUNCES){
				break;
			}
			
			if (hit == null){
				break;
			}
			
			// TODO: maybe add emission, at least if eyeBounce big enough?
			if (hit.material.evaluateEmission(hit, hit.w) != null){
				break;
			}
			ShadingSample newSample = hit.material.getShadingSample(hit, sampler.makeSamples(1, 2)[0]);
//			if(newSample == null){
//				System.out.println(hit.material);
//			}
			
			Ray newRay = new Ray(hit.position, newSample.w, eyeBounce+1, true);
			HitRecord newHit = root.intersect(newRay);
			
			if (newHit == null){
				break;
			}
		
			

			
			alpha.mult(newSample.brdf);
			
			float cosTerm = newSample.w.dot(hit.normal);
			boolean isSpecular = newSample.isSpecular;
			if (!isSpecular){
				alpha.mult(cosTerm);
			}
			alpha.mult(1/(newSample.p));;
			
			float cos1 = hit.normal.dot(newSample.w);
			float geometryTerm = (cos1 * newHit.normal.dot(StaticVecmath.negate(newSample.w)))/StaticVecmath.dist2(newHit.position,hit.position);
			
			// TODO pL
			float pL = 0;
			
			// TODO pE
			float pE = 0;
			
			eyeNodes.add(new PathNode(newHit,eyeBounce,alpha,geometryTerm,pL,pE));
			
			// Get new ray
//			ShadingSample shadingSample = hit.material.getShadingSample(hit, sampler.makeSamples(1, 2)[0]);
//			specular = shadingSample.isSpecular;
//			
//			float Gp = 1/shadingSample.p;
//
//			// TODO No idea if this makes sense
//			if (!specular){
//				Gp *= hit.normal.dot(shadingSample.w);
//			}
//			
//			
//			
//			alpha.mult(shadingSample.brdf);
//			alpha.mult(Gp);
//			
//			Ray newRay = new Ray(hit.position, shadingSample.w, eyeBounce+1, true);
//			hit = root.intersect(newRay);
//			if (hit == null){
//				break;
//			}
//			
			eyeBounce++;
		}
		
		return eyeNodes;
	}
	
	private Spectrum connect(PathNode eye, PathNode light) {
		// Direction of the connecting ray
		Vector3f connectionDir = StaticVecmath.sub(light.hitRecord.position,eye.hitRecord.position);
		float d = connectionDir.lengthSquared();
		connectionDir.normalize();
		
		// Geometry term (G in the green part on p.23)
		float cosTheta1;
		
		// TODO No idea if this makes sense
//		if (specular){
//			cosTheta1 = 1;
//		}else{
			cosTheta1 = eye.hitRecord.normal.dot(connectionDir);
			cosTheta1 = Math.max(0,cosTheta1);
//		}
		float cosTheta2 = light.hitRecord.normal.dot(StaticVecmath.negate(connectionDir));
		cosTheta2 = Math.max(0,cosTheta2);
		

		float geometryTerm = (cosTheta1 * cosTheta2)/d;
		
		// BRDF-terms (f in the green part on p.23)
		Spectrum brdfEye, brdfLight;
		brdfEye = eye.hitRecord.material.evaluateBRDF(eye.hitRecord, eye.hitRecord.w, connectionDir);
		brdfEye.mult(eye.alpha);
		
		if(light.bounce == 0){
			brdfLight = light.hitRecord.material.evaluateEmission(light.hitRecord,connectionDir);
			
			return brdfLight;
//			brdfLight.mult(lightNode.alpha);
		}else{
			brdfLight = light.hitRecord.material.evaluateBRDF(light.hitRecord, light.hitRecord.w, StaticVecmath.negate(connectionDir));
		}
		
		brdfLight.mult(light.alpha);
		
		// multiply stuff together
		Spectrum connectionContribution = new Spectrum(brdfEye);
		connectionContribution.mult(brdfLight);
		connectionContribution.mult(geometryTerm);

		// shadow ray
		Ray shadowRay = new Ray(eye.hitRecord.position,connectionDir,0,true);
		HitRecord shadowHit = root.intersect(shadowRay);
		
		if(shadowHit != null){
			float lengthShadowHitToHitRecord = StaticVecmath.dist2(shadowHit.position, eye.hitRecord.position);
			if (d > lengthShadowHitToHitRecord + 1e-3f){
				return new Spectrum();
			}
		}
		
		return connectionContribution;
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
		int bounce;
		Spectrum alpha;
		float geometryTerm;
		float probabilityLight, probabilityEye;
		
		public PathNode(HitRecord hitRecord, int bounce, Spectrum alpha, float G, float pL, float pE){
			this.hitRecord = hitRecord;
			this.bounce = bounce;
			this.alpha = alpha;
			this.geometryTerm = G;
			this.probabilityLight = pL;
			this.probabilityEye = pE;
		}
	}
}
