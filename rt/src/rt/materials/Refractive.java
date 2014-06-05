package rt.materials;

import java.util.Random;

import javax.vecmath.Vector3f;

import rt.*;
import rt.Material.ShadingSample;

/**
 * A refractive material.
 */
public class Refractive implements Material {

	float refractionIndex;
	Spectrum color;

	/**
	 * @param refractionIndex
	 *            the index of refraction
	 */
	public Refractive(float refractionIndex, Spectrum color) {
		this.refractionIndex = refractionIndex;
		this.color = color;
	}
	
	public Refractive(float refractionIndex){
		this(refractionIndex, new Spectrum(1));
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
		return new Spectrum();
	}

	public boolean hasSpecularReflection() {
		return true;
	}

	public ShadingSample evaluateSpecularReflection(HitRecord hitRecord) {
		Vector3f r = StaticVecmath.reflect(hitRecord.normal,hitRecord.w);
		
		float rSchlick = rSchlick(hitRecord);
				
		Spectrum brdf = new Spectrum(1);
		brdf.mult(rSchlick);
		ShadingSample sample = new ShadingSample(brdf, new Spectrum(),r,true,rSchlick);
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
		
		Vector3f normal = new Vector3f(hitRecord.normal);

		float n1,n2;
		if (hitRecord.normal.dot(hitRecord.w) > 0){ // air --> material
			n1 = 1;
			n2 = refractionIndex;
		}else{ // material --> air
			n1 = refractionIndex;
			n2 = 1;
			normal.negate();
		}
		normal.normalize();
		
		float cosThetaI = -i.dot(normal);

		float sinSqrThetaT = ((n1/n2)*(n1/n2)) * (1 - cosThetaI * cosThetaI);
		if (sinSqrThetaT > 1){
			return null;
		}

		Vector3f t = new Vector3f(i);
		t.scale(n1/n2);
		
		Vector3f nScaled = new Vector3f(normal);
		nScaled.scale((n1/n2)*cosThetaI - (float)Math.sqrt(1 - sinSqrThetaT));
		t.add(nScaled);

		Spectrum brdf = new Spectrum(1,1,1);
		brdf.mult(1-rSchlick(hitRecord));
		ShadingSample result = new ShadingSample(brdf,new Spectrum(), t, true, 1-rSchlick(hitRecord));
		result.brdf.mult(color);
		return result;
	}

	public ShadingSample getShadingSample(HitRecord hitRecord, float[] sample) {
		if (sample[0] < rSchlick(hitRecord)){
			return evaluateSpecularReflection(hitRecord);
		}
		return evaluateSpecularRefraction(hitRecord);
	}

	public boolean castsShadows() {
		return false;
	}

	public Spectrum evaluateEmission(HitRecord hitRecord, Vector3f wOut) {
		return null;
	}

	public ShadingSample getEmissionSample(HitRecord hitRecord, float[] sample) {
//		return new ShadingSample();
		return null;
	}

	private float rSchlick(HitRecord hitRecord) {
		Vector3f i = new Vector3f(hitRecord.w);
		i.negate();
		i.normalize();
		Vector3f normal = new Vector3f(hitRecord.normal);
		
		float n1,n2;
		if (hitRecord.normal.dot(hitRecord.w) > 0){ // air --> material
			n1 = 1;
			n2 = refractionIndex;
		}else{ // material --> air
			n1 = refractionIndex;
			n2 = 1;
			normal.negate();
		}
		
		float cosThetaI = -i.dot(normal);

		float sinSqrThetaT = ((n1 * n1) / (n2 * n2))
				* (1 - cosThetaI * cosThetaI);
		
		if (sinSqrThetaT > 1){ // TIR
			return 1;
		}
		
		float r0 = (n1 - n2) / (n1 + n2);
		r0 *= r0;

		float rSchlick;
		
		float x;
		if (n1 <= n2){
			x = 1 - cosThetaI;
			rSchlick = r0 + (1 - r0) * x * x * x * x * x;
		}
		else {
			float cosThetaT = (float) Math.sqrt(1 - sinSqrThetaT);
			x = 1 - cosThetaT;
			rSchlick = r0 + (1 - r0) * x * x * x * x * x;
		}
		return rSchlick;
	}

	@Override
	public float getDirectionalProbability(HitRecord hitRecord, Vector3f wOut) {
		// TODO Auto-generated method stub
		return 0;
	}

}
