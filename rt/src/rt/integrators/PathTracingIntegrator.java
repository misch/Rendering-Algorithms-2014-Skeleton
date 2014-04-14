package rt.integrators;

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
public class PathTracingIntegrator implements Integrator {

	LightList lightList;
	Intersectable root;
	Sampler sampler = new RandomSampler();
	
	public PathTracingIntegrator(Scene scene)
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

		Spectrum alpha = new Spectrum(1);
		HitRecord hitRecord = root.intersect(r);
		if (hitRecord == null){
			return new Spectrum(0);
		}
		
		Spectrum emission = hitRecord.material.evaluateEmission(hitRecord, hitRecord.w); 
		if (emission != null){
			return new Spectrum(emission);
		}
		int bounce = 0;
		Spectrum color = new Spectrum();
		while (true){
			
			if (russianRoulette(bounce)){
				break;
			}
			
			// If a light is directly hit, terminate ray
			emission  = hitRecord.material.evaluateEmission(hitRecord, hitRecord.w);
			if (emission != null){
				break;
			}
			
			// Add light sample to color
			LightGeometry lightSource = lightList.getRandomLightSource();

			SpectrumWrapper lightSample = sampleLight(lightSource, hitRecord);
			
			lightSample.p *= 1f/lightList.size();
			lightSample.s.mult(lightList.size());
			
			lightSample.s.mult(alpha);
			color.add(lightSample.s);

			// Get new ray
			float[] sample = sampler.makeSamples(1, 2)[0];
			ShadingSample shadingSample = hitRecord.material.getShadingSample(hitRecord, sample);
			
			Ray newRay = new Ray(hitRecord.position, shadingSample.w, 0, true);
			Vector3f normal = new Vector3f(hitRecord.normal);
			hitRecord = root.intersect(newRay);
			
			if (hitRecord == null){
				break;
			}
			
			Spectrum brdf = shadingSample.brdf;
			alpha.mult(brdf);
			
			float cosTerm = shadingSample.w.dot(normal);
			alpha.mult(cosTerm/shadingSample.p);
			bounce++;
		}
		return color;
	}
	
	private boolean russianRoulette(int bounce) {
		if (bounce < 3){
			return false;
		}
		
		Random rand = new Random();
		if (rand.nextFloat() > 0.5){
			return true;
		}
		return false;
	}

	private SpectrumWrapper sampleLight(LightGeometry lightSource, HitRecord hitRecord){
		float[][] sample = sampler.makeSamples(1, 2);
		HitRecord lightHit = lightSource.sample(sample[0]);
		Vector3f lightDir = StaticVecmath.sub(lightHit.position, hitRecord.position);
		float d = lightDir.lengthSquared();
		lightDir.normalize();
		
		Ray shadowRay = new Ray(hitRecord.position,lightDir);
		Vector3f scaledLightDir = new Vector3f(lightDir);
		scaledLightDir.scale(1e-3f);
		shadowRay.origin.add(scaledLightDir);
		HitRecord shadowHit = root.intersect(shadowRay);
		
		if(shadowHit != null){
			float lengthShadowHitToHitRecord = StaticVecmath.dist2(shadowHit.position, hitRecord.position);
			if (d > lengthShadowHitToHitRecord + 1e-3f){
				return new SpectrumWrapper(new Spectrum(0), lightHit.p);
			}
		}
		
		// Evaluate the BRDF
		Spectrum brdfValue = hitRecord.material.evaluateBRDF(hitRecord, hitRecord.w, lightDir);
		
		// Multiply together factors relevant for shading, that is, brdf * emission * ndotl * geometry term
		Spectrum s = new Spectrum(brdfValue);
		
		// Multiply with emission
		s.mult(lightHit.material.evaluateEmission(lightHit, StaticVecmath.negate(lightDir)));
		
		float cosTerm = Math.max(lightHit.normal.dot(StaticVecmath.negate(lightDir)),0);
		s.mult(cosTerm);
		
		// Multiply with cosine of surface normal and incident direction
		float ndotl = hitRecord.normal.dot(lightDir);
		ndotl = Math.max(ndotl, 0.f);
		s.mult(ndotl);
		
		// Geometry term
		s.mult(1.f/(d*lightHit.p));
		
		return new SpectrumWrapper(s, lightHit.p);
	}

	public float[][] makePixelSamples(Sampler sampler, int n) {
		return sampler.makeSamples(n, 2);
	}
	
	private class SpectrumWrapper{
		Spectrum s;
		float p;
		
		public SpectrumWrapper(Spectrum s, float p){
			this.s = s;
			this.p = p;
		}
	}

}
