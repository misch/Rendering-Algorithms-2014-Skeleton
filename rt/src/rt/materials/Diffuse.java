package rt.materials;

import java.util.Random;

import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Material;
import rt.Spectrum;

/**
 * A basic diffuse material.
 */
public class Diffuse implements Material {

	Spectrum kd;
	
	/**
	 * Note that the parameter value {@param kd} is the diffuse reflectance,
	 * which should be in the range [0,1], a value of 1 meaning all light
	 * is reflected (diffusely), and none is absorbed. The diffuse BRDF
	 * corresponding to {@param kd} is actually {@param kd}/pi.
	 * 
	 * @param kd the diffuse reflectance
	 */
	public Diffuse(Spectrum kd)
	{
		this.kd = new Spectrum(kd);
		// Normalize
		this.kd.mult(1/(float)Math.PI);
	}
	
	/**
	 * Default diffuse material with reflectance (1,1,1).
	 */
	public Diffuse()
	{
		this(new Spectrum(1.f, 1.f, 1.f));
	}

	/**
	 * Returns diffuse BRDF value, that is, a constant.
	 * 
	 *  @param wOut outgoing direction, by convention towards camera
	 *  @param wIn incident direction, by convention towards light
	 *  @param hitRecord hit record to be used
	 */
	public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut, Vector3f wIn) {
		return new Spectrum(kd);
	}

	public boolean hasSpecularReflection()
	{
		return false;
	}
	
	public ShadingSample evaluateSpecularReflection(HitRecord hitRecord)
	{
		return null;
	}
	public boolean hasSpecularRefraction()
	{
		return false;
	}

	public ShadingSample evaluateSpecularRefraction(HitRecord hitRecord)
	{
		return null;
	}
	
	// To be implemented for path tracer!
	public ShadingSample getShadingSample(HitRecord hitRecord, float[] sample)
	{
		float psi1 = sample[0], psi2 = sample[1];
		
		Vector3f direction = new Vector3f();
		direction.x = (float)(Math.cos(2*Math.PI*psi2) * Math.sqrt(psi1));
		direction.y = (float)(Math.sin(2*Math.PI*psi2) * Math.sqrt(psi1));
		direction.z = (float)Math.sqrt(1-psi1);
		
		float probability = (float) (direction.dot(hitRecord.normal)/Math.PI);
		
		ShadingSample shadingSample = new ShadingSample(new Spectrum(kd), new Spectrum(0,0,0),direction,false,probability);
		return shadingSample;
	}
		
	public boolean castsShadows()
	{
		return true;
	}
	
	public Spectrum evaluateEmission(HitRecord hitRecord, Vector3f wOut) {
		return null;
	}

	public ShadingSample getEmissionSample(HitRecord hitRecord, float[] sample) {
		return new ShadingSample();
	}
	
}
