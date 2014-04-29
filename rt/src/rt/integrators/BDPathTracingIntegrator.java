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
	private final int MAX_BOUNCES = 8;
	
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
		
		// ///////////////////////////////////////////
		// 1) make light path, store light vertices
		// ///////////////////////////////////////////
		ArrayList<LightNode> lightNodes = new ArrayList<LightNode>();
		// Sample a random light source
		LightGeometry lightSource = lightList.getRandomLightSource();		
		float[][] sample = sampler.makeSamples(1, 2);
		HitRecord lightHit = lightSource.sample(sample[0]);
		
		lightNodes.add(new LightNode(lightHit,0,new Spectrum(lightList.size()/lightHit.p)));
		
		ShadingSample emissionSample = lightHit.material.getEmissionSample(lightHit, sampler.makeSamples(1,2)[0]);
		// Trace light path
		int lightBounce = 1;
		while (true){
			
			if (lightHit == null){
				break;
			}
			
			if (lightBounce > 3){
				break;
			}
			
			// Compute alpha value based on previous lightHit
			Spectrum alpha;			
			alpha = new Spectrum(1);
			float Gp = lightHit.normal.dot(emissionSample.w)/emissionSample.p;
			alpha.mult(emissionSample.brdf);
			alpha.mult(Gp);
			
			// Now, after alpha is computed, update lightHit
			Ray newRay = new Ray(lightHit.position, emissionSample.w, lightBounce+1, true);
			lightHit = root.intersect(newRay);
			
			if (lightHit == null || lightHit.material.evaluateEmission(lightHit, lightHit.w) != null){
				break;
			}
			
			lightNodes.add(new LightNode(lightHit,lightBounce,alpha));
			
			// Get new sample
			emissionSample = lightHit.material.getShadingSample(lightHit, sampler.makeSamples(1, 2)[0]);
			
			lightBounce++;
		}

		// //////////////////////////////////////////////////////////////
		// 2) make eye path; for each hit, connect with each light node
		// //////////////////////////////////////////////////////////////
		int eyeBounce = 1;
		HitRecord hit = root.intersect(r);
		if(hit == null){
			return new Spectrum();
		}
		
		Spectrum alpha = new Spectrum(1);
		while (true){			
			if (eyeBounce > 1){
				break;
			}
			for(LightNode lightNode : lightNodes){
				Spectrum connectionContribution = connect(hit,lightNode);
				connectionContribution.mult(alpha);
				outgoing.add(connectionContribution);
			}
			
			if (hit.material.evaluateEmission(hit, hit.w) != null){
				break;
			}
			
			// Get new ray
			ShadingSample shadingSample = hit.material.getShadingSample(hit, sampler.makeSamples(1, 2)[0]);
			
			float Gp = hit.normal.dot(shadingSample.w)/shadingSample.p;
			alpha.mult(shadingSample.brdf);
			alpha.mult(Gp);
			
			Ray newRay = new Ray(hit.position, shadingSample.w, eyeBounce+1, true);
			hit = root.intersect(newRay);
			if (hit == null){
				break;
			}
			
			eyeBounce++;
		}
		
		return outgoing;
	}
	
	private Spectrum connect(HitRecord eyeHit, LightNode lightNode) {
		// Direction of the connecting ray
		Vector3f connectionDir = StaticVecmath.sub(lightNode.hitRecord.position,eyeHit.position);
		float d = connectionDir.lengthSquared();
		connectionDir.normalize();
		
		// Geometry term (G in the green part on p.23)
		float cosTheta1 = eyeHit.normal.dot(connectionDir);
		float cosTheta2 = lightNode.hitRecord.normal.dot(StaticVecmath.negate(connectionDir));
		float geometryTerm = (cosTheta1 * cosTheta2)/d;
		
		// BRDF-terms (f in the green part on p.23)
		Spectrum brdfEye, brdfLight;
		brdfEye = eyeHit.material.evaluateBRDF(eyeHit, eyeHit.w, connectionDir);
		
		if(lightNode.bounce == 0){
			brdfLight = lightNode.hitRecord.material.evaluateEmission(lightNode.hitRecord,connectionDir);
		}else{
			brdfLight = lightNode.hitRecord.material.evaluateBRDF(lightNode.hitRecord, lightNode.hitRecord.w, StaticVecmath.negate(connectionDir));
		}
		
		brdfLight.mult(lightNode.alpha);
		
		// multiply stuff together
		Spectrum connectionContribution = new Spectrum(brdfEye);
		connectionContribution.mult(brdfLight);
		connectionContribution.mult(geometryTerm);

		// shadow ray
		Ray shadowRay = new Ray(eyeHit.position,connectionDir,0,true);
		HitRecord shadowHit = root.intersect(shadowRay);
		
		if(shadowHit != null){
			float lengthShadowHitToHitRecord = StaticVecmath.dist2(shadowHit.position, eyeHit.position);
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
	
	private class LightNode{
		HitRecord hitRecord;
		int bounce;
		Spectrum alpha;
		
		public LightNode(HitRecord hitRecord, int bounce, Spectrum alpha){
			this.hitRecord = hitRecord;
			this.bounce = bounce;
			this.alpha = alpha;
		}
	}
}
