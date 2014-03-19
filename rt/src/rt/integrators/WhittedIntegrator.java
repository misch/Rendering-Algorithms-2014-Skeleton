package rt.integrators;

import java.util.Iterator;

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

/**
 * Integrator for Whitted style ray tracing. This is a basic version that needs to be extended!
 */
public class WhittedIntegrator implements Integrator {

	LightList lightList;
	Intersectable root;
	static final int MAX_DEPTH = 10;
	
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
			return new Spectrum(0,0,0);
		}
		
		Spectrum reflection = new Spectrum(0,0,0);
		Spectrum refraction = new Spectrum(0,0,0);
		if(hitRecord.material.hasSpecularReflection() && r.depth < MAX_DEPTH){
			ShadingSample sample = hitRecord.material.evaluateSpecularReflection(hitRecord);

			reflection = new Spectrum(sample.brdf);
			
			Vector3f posPlusEps = new Vector3f();
			posPlusEps.scaleAdd(1e-3f, sample.w,hitRecord.position);
			Ray reflectedRay = new Ray(posPlusEps, sample.w, r.depth+1);
			reflection.mult(integrate(reflectedRay));
		}
		
		if(hitRecord.material.hasSpecularRefraction() && r.depth < MAX_DEPTH){
			ShadingSample sample = hitRecord.material.evaluateSpecularRefraction(hitRecord);
			
			if (sample.w == null){
				return new Spectrum(0,0,0);
			}
			
			refraction = new Spectrum(sample.brdf);
			
			Vector3f posPlusEps = new Vector3f();
			posPlusEps.scaleAdd(1e-3f, sample.w,hitRecord.position);
			Ray refractedRay = new Ray(posPlusEps, sample.w, r.depth+1);
			refraction.mult(integrate(refractedRay));
		}
		
		if (hitRecord.material.hasSpecularReflection() || hitRecord.material.hasSpecularRefraction()){
			Spectrum tmp = new Spectrum();
			tmp.add(reflection);
			tmp.add(refraction);
			return refraction;
		}
		
			Spectrum outgoing = new Spectrum(0.f, 0.f, 0.f);
			Spectrum brdfValue;
			
			// Iterate over all light sources
			Iterator<LightGeometry> it = lightList.iterator();
			while(it.hasNext())
			{
				LightGeometry lightSource = it.next();
			
				
				// Make direction from hit point to light source position; this is only supposed to work with point lights
				float dummySample[] = new float[2];
				HitRecord lightHit = lightSource.sample(dummySample);
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
					if (d > lengthShadowHitToHitRecord){
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
				s.mult(1.f/(d));
				
				// Accumulate
				outgoing.add(s);
			}
			
			return outgoing;
	}

	public float[][] makePixelSamples(Sampler sampler, int n) {
		return sampler.makeSamples(n, 2);
	}

}
