package rt.integrators;

import java.util.Iterator;

import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Integrator;
import rt.Intersectable;
import rt.LightGeometry;
import rt.LightList;
import rt.Material.ShadingSample;
import rt.samplers.RandomSampler;
import rt.Ray;
import rt.Sampler;
import rt.Scene;
import rt.Spectrum;
import rt.StaticVecmath;

/**
 * Integrator for Whitted style ray tracing. This is a basic version that needs to be extended!
 */
public class WhittedIntegrator implements Integrator {

	LightList lightList;
	Intersectable root;
	static final int MAX_DEPTH = 5;
	Sampler sampler = new RandomSampler();
	
	public WhittedIntegrator(Scene scene)
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

		HitRecord hitRecord = root.intersect(r);
		
		if (hitRecord == null){
			return new Spectrum();
		}
		
		Spectrum emission = hitRecord.material.evaluateEmission(hitRecord, hitRecord.w);
		if (emission != null){
			return new Spectrum(emission);
		}
		
		Spectrum reflection = new Spectrum();
		Spectrum refraction = new Spectrum();
		if(hitRecord.material.hasSpecularReflection() && r.depth < MAX_DEPTH){
			ShadingSample sample = hitRecord.material.evaluateSpecularReflection(hitRecord);

			reflection = new Spectrum(sample.brdf);
			
			Ray reflectedRay = new Ray(hitRecord.position, sample.w, r.depth+1,true);
			reflection.mult(integrate(reflectedRay));
		}
		
		if(hitRecord.material.hasSpecularRefraction() && r.depth < MAX_DEPTH){
			ShadingSample sample = hitRecord.material.evaluateSpecularRefraction(hitRecord);
			
			if (sample.w == null){
				return new Spectrum();
			}
			
			refraction = new Spectrum(sample.brdf);
			
			Ray refractedRay = new Ray(hitRecord.position, sample.w, r.depth+1, true);
			refraction.mult(integrate(refractedRay));
		}
		
		if (hitRecord.material.hasSpecularReflection() || hitRecord.material.hasSpecularRefraction()){
			Spectrum tmp = new Spectrum();
			tmp.add(reflection);
			tmp.add(refraction);
			return tmp;
		}
		
			Spectrum outgoing = new Spectrum();
			Spectrum brdfValue;
			
			// Iterate over all light sources
			Iterator<LightGeometry> it = lightList.iterator();
			while(it.hasNext())
			{
				LightGeometry lightSource = it.next();
				
				// Make direction from hit point to light source position; this is only supposed to work with point lights
				float[][] sample = sampler.makeSamples(1, 2);
				HitRecord lightHit = lightSource.sample(sample[0]);
				Vector3f lightDir = StaticVecmath.sub(lightHit.position, hitRecord.position);
				float d = lightDir.lengthSquared();
				lightDir.normalize();
				
				Ray shadowRay = new Ray(hitRecord.position,lightDir,0,true);
				HitRecord shadowHit = root.intersect(shadowRay);

				if(shadowHit != null){
					float lengthShadowHitToHitRecord = StaticVecmath.dist2(shadowHit.position, hitRecord.position);
					if (d > lengthShadowHitToHitRecord + 1e-3f){
						continue;
					}
				}
				
				// Evaluate the BRDF
				brdfValue = hitRecord.material.evaluateBRDF(hitRecord, hitRecord.w, lightDir);
				
				// Multiply together factors relevant for shading, that is, brdf * emission * ndotl * geometry term
				Spectrum s = new Spectrum(brdfValue);
				
				// Multiply with emission
				s.mult(lightHit.material.evaluateEmission(lightHit, StaticVecmath.negate(lightDir)));
				
				// Multiply with cosine of surface normal and incident direction
				float ndotl = hitRecord.normal.dot(lightDir);
				ndotl = Math.max(ndotl, 0.f);
				s.mult(ndotl);
				
				// Geometry term: multiply with 1/(squared distance), only correct like this 
				// for point lights (not area lights)!
				s.mult(1.f/(d*lightHit.p));
				
				// Accumulate
				outgoing.add(s);
			}
			
			return outgoing;
	}

	public float[][] makePixelSamples(Sampler sampler, int n) {
		return sampler.makeSamples(n, 2);
	}

}
