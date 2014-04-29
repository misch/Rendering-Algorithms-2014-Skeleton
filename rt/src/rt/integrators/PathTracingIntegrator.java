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
		Spectrum outgoing = new Spectrum();
		Random rand = new Random();
		while (true){
			
			// If a light is directly hit, terminate ray
			emission  = hitRecord.material.evaluateEmission(hitRecord, hitRecord.w);
			if (emission != null){
				break;
			}
			
			// Add light sample to color
			Spectrum lightSample = sampleLight(hitRecord);
		
			if (bounce > MAX_BOUNCES){
				break;
			}
			lightSample.mult(alpha);
			outgoing.add(lightSample);
			
			float pRussianRoulette = russianRouletteProbability(bounce);
			if (rand.nextFloat() < pRussianRoulette){
				break;
			}
			// Get new ray
			float[] sample = sampler.makeSamples(1, 2)[0];
			ShadingSample shadingSample = hitRecord.material.getShadingSample(hitRecord, sample);
			
			float cosTerm = shadingSample.w.dot(hitRecord.normal);

			Ray newRay = new Ray(hitRecord.position, shadingSample.w, bounce+1, true);
			hitRecord = root.intersect(newRay);
			if (hitRecord == null){
				break;
			}
			
			alpha.mult(shadingSample.brdf);
			
//			System.out.println(pRussianRoulette);
			alpha.mult(cosTerm/(shadingSample.p*(1-pRussianRoulette)));
//			alpha.mult(cosTerm/(shadingSample.p));
			bounce++;
		}
		return outgoing;
	}
	
	private float russianRouletteProbability(int bounce) {
		if (bounce <= 3){
			return 0;
		}
		return 0.5f;
	}

	private Spectrum sampleLight(HitRecord hitRecord){
		
		// Get a random light source
		LightGeometry lightSource = lightList.getRandomLightSource();
		
		float[][] sample = sampler.makeSamples(1, 2);
		HitRecord lightHit = lightSource.sample(sample[0]);
		
		// lightDir: surface hit point --> light source
		Vector3f lightDir = StaticVecmath.sub(lightHit.position, hitRecord.position);
		float d = lightDir.lengthSquared();
		lightDir.normalize();
		
		// cosTerm: account for angle from which we see the light
		float cosTerm = Math.max(lightHit.normal.dot(StaticVecmath.negate(lightDir)),0);
		
		// Create a shadow ray
		Ray shadowRay = new Ray(hitRecord.position,lightDir,0,true);
		HitRecord shadowHit = root.intersect(shadowRay);
		
		if(shadowHit != null){
			float lengthShadowHitToHitRecord = StaticVecmath.dist2(shadowHit.position, hitRecord.position);
			if (d > lengthShadowHitToHitRecord + 1e-3f){
				return new Spectrum(0);
			}
		}
		
		// Evaluate the BRDF
		Spectrum brdfValue = hitRecord.material.evaluateBRDF(hitRecord, hitRecord.w, lightDir);
		
		// Multiply together factors relevant for shading, that is, brdf * emission * ndotl * geometry term
		Spectrum s = new Spectrum(brdfValue);
		
		// Multiply with emission
		s.mult(lightHit.material.evaluateEmission(lightHit, StaticVecmath.negate(lightDir)));
		
		// Multiply with cosine of surface normal and incident direction
		float ndotl = hitRecord.normal.dot(lightDir);
		ndotl = Math.max(ndotl, 0.f);
		s.mult(ndotl);
		
		// probability to hit the light:
		// 	- p = 1/lightArea --> aready done in lightSource.sample
		//	- p = p/(number of light sources) --> lightList.size()
		// 	- convert to solid angle probability instead of area probability
		float p = lightHit.p/lightList.size();
		p *= d/cosTerm;
		
		// Geometry term
		s.mult(1/p);
		
		return s;
	}

	public float[][] makePixelSamples(Sampler sampler, int n) {
		return sampler.makeSamples(n, 2);
	}
}
