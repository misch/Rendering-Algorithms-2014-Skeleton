package rt.integrators;

import java.util.Iterator;
import java.util.Random;

import javax.vecmath.*;

import rt.HitRecord;
import rt.Integrator;
import rt.Intersectable;
import rt.LightList;
import rt.LightGeometry;
import rt.Ray;
import rt.Sampler;
import rt.Scene;
import rt.Spectrum;
import rt.StaticVecmath;

/**
 * Integrator for Whitted style ray tracing. This is a basic version that needs to be extended!
 */
public class PointLightIntegrator implements Integrator {

	LightList lightList;
	Intersectable root;
	
	public PointLightIntegrator(Scene scene)
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
		if(hitRecord != null)
		{
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
				
				if(shadowHit != null){
					float lengthShadowHitToHitRecord = StaticVecmath.dist2(shadowHit.position, hitRecord.position);
					if (d > lengthShadowHitToHitRecord){
						s.mult(0);
					}
				}
				
				Spectrum T = new Spectrum(1);
				Spectrum L = new Spectrum();
				Spectrum L_ve = new Spectrum(0.2f);
				float ds = 0.05f; // fixed step size
				Random rand = new Random();
				float sigma_t = 0.2f;
				float sigma_s = 0.2f;
				for (float currentT = 0; currentT <= hitRecord.t; currentT += ds){
					
					Vector3f currentPoint = r.getRayAt(currentT);
					shadowRay = new Ray(currentPoint,lightDir,0,true);
					shadowHit = root.intersect(shadowRay);
					
					lightDir = StaticVecmath.sub(lightHit.position, currentPoint);
					d = lightDir.lengthSquared();

					if(shadowHit != null && d > StaticVecmath.dist2(shadowHit.position, currentPoint) + 1e-3f){
						Spectrum shadowT = new Spectrum(1);
						float dt = 0.05f;
						
						for (float currentShadowT = 0; currentShadowT < shadowHit.t; currentShadowT += dt){
							shadowT.mult(1 - sigma_t*dt);
						}
						
						Spectrum L_d = new Spectrum(shadowT);
						L_d.mult(s);
						L_d.mult(sigma_s * 1/(float)Math.PI);
						L_ve.mult(L_d);
					}
					
					Spectrum tmp = new Spectrum(T);
					tmp.mult(L_ve);
					L.add(tmp);
					T.mult(1 - sigma_t * ds);
				}
				
				L.mult(ds);
				Spectrum L_s = new Spectrum(s);
				L_s.mult(T);
				L.add(L_s);
				
				// Accumulate
				outgoing.add(s);
//				outgoing.add(L);
			}
			
			return outgoing;
		} else 
			return new Spectrum(0.f,0.f,0.f);
		
	}

	public float[][] makePixelSamples(Sampler sampler, int n) {
		return sampler.makeSamples(n, 2);
	}

}
