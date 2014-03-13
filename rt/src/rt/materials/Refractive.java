package rt.materials;

import javax.vecmath.Vector3f;

import rt.*;
import rt.Material.ShadingSample;

/**
 * A refractive material.
 */
public class Refractive implements Material {

	float refractionIndex;

	/**
	 * @param refractionIndex
	 *            the index of refraction
	 */
	public Refractive(float refractionIndex) {
		this.refractionIndex = refractionIndex;
	}

	/**
	 * Returns diffuse BRDF value, that is, a constant.
	 * 
	 * @param wOut
	 *            outgoing direction, by convention towards camera
	 * @param wIn
	 *            incident direction, by convention towards light
	 * @param hitRecord
	 *            hit record to be used
	 */
	public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut,
			Vector3f wIn) {
		return new Spectrum(1, 1, 1);
	}

	public boolean hasSpecularReflection() {
		return true;
	}

	public ShadingSample evaluateSpecularReflection(HitRecord hitRecord) {
		Vector3f i = new Vector3f(hitRecord.w);
		i.negate();
		i.normalize();
		
		float cosThetaI = -i.dot(hitRecord.normal); 
				
		Vector3f r = new Vector3f();
		r.scaleAdd(2*cosThetaI,hitRecord.normal,i);
		
		// air --> material OR material --> air
		float n1 = (hitRecord.normal.dot(hitRecord.w) < 0) ? refractionIndex
				: 1;
		float n2 = (hitRecord.normal.dot(hitRecord.w) < 0) ? 1
				: refractionIndex;
		
		float rSchlick = rSchlick(hitRecord,n1,n2);
		
		if (Float.isNaN(rSchlick)){
			new ShadingSample(new Spectrum(1, 1, 1),new Spectrum(0, 0, 0), null, true, 1);
		}
		
		Spectrum brdf = new Spectrum(1,1,1);
		brdf.mult(rSchlick);
		ShadingSample sample = new ShadingSample(brdf, new Spectrum(0,0,0),r,true,1);
		return sample;
	}

	public boolean hasSpecularRefraction() {
		return true;
	}

	/*
	 * Evaluate specular refraction according to Bram de Greve, 2006
	 * "Reflections and Refractions in Ray Tracing"
	 * 
	 * @see rt.Material#evaluateSpecularRefraction(rt.HitRecord)
	 */
	public ShadingSample evaluateSpecularRefraction(HitRecord hitRecord) {
		Vector3f i = new Vector3f(hitRecord.w);
		i.negate();
		i.normalize();

		// air --> material OR material --> air
		float n1 = (hitRecord.normal.dot(hitRecord.w) < 0) ? refractionIndex
				: 1;
		float n2 = (hitRecord.normal.dot(hitRecord.w) < 0) ? 1
				: refractionIndex;

		i.scale(n1 / n2);

		float cosThetaI = -i.dot(hitRecord.normal);
		float sinSqrThetaT = ((n1 * n1) / (n2 * n2))
				* (1 - cosThetaI * cosThetaI);

		if (sinSqrThetaT > 1) {
			new ShadingSample(new Spectrum(1, 1, 1),new Spectrum(0, 0, 0), null, true, 1);
		}

		Vector3f t = new Vector3f();
		t.scaleAdd((n1 / n2) * cosThetaI - (float) Math.sqrt(1 - sinSqrThetaT),
				hitRecord.normal, i);

		Spectrum brdf = new Spectrum(1,1,1);
		brdf.mult(1-rSchlick(hitRecord,n1,n2));
		ShadingSample sample = new ShadingSample(new Spectrum(1, 1, 1),new Spectrum(0, 0, 0), t, true, 1);
		return sample;
	}

	// To be implemented for path tracer!
	public ShadingSample getShadingSample(HitRecord hitRecord, float[] sample) {
		return null;
	}

	public boolean castsShadows() {
		return false;
	}

	public Spectrum evaluateEmission(HitRecord hitRecord, Vector3f wOut) {
		return new Spectrum(0.f, 0.f, 0.f);
	}

	public ShadingSample getEmissionSample(HitRecord hitRecord, float[] sample) {
		return new ShadingSample();
	}

	private float rSchlick(HitRecord hitRecord, float n1, float n2) {
		Vector3f i = new Vector3f(hitRecord.w);
		i.negate();
		i.normalize();
		float cosThetaI = -i.dot(hitRecord.normal);

		float sinSqrThetaT = ((n1 * n1) / (n2 * n2))
				* (1 - cosThetaI * cosThetaI);
		
		if (sinSqrThetaT > 1){ // In case of reflection, this is not yet captured in evaluateSpecularReflection()
			return Float.NaN;
		}
		
		float r0 = (n1 - n1) / (n1 + n2);
		r0 *= r0;

		float rSchlick;
		
		float x = 1 - cosThetaI;
		if (n1 <= n2)
			rSchlick = r0 + (1 - r0) * x * x * x * x * x;
		else {
			float cosTheta_t = (float) Math.sqrt(1 - sinSqrThetaT);
			rSchlick = r0 + (1 - r0) * x * x * x * x * x;
		}
		return rSchlick;
	}

}
