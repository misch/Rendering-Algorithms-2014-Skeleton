package rt.materials;

import javax.vecmath.Vector3f;

import rt.*;

/**
 * A basic diffuse material.
 */
public class Reflective implements Material {

	Spectrum ks;
	
	/**
	 * Note that the parameter value {@param kd} is the diffuse reflectance,
	 * which should be in the range [0,1], a value of 1 meaning all light
	 * is reflected (diffusely), and none is absorbed. The diffuse BRDF
	 * corresponding to {@param kd} is actually {@param kd}/pi.
	 * 
	 * @param ks the specular reflection coefficient
	 */
	public Reflective(Spectrum ks)
	{
		this.ks = new Spectrum(ks);
	}
	
	/**
	 * Default diffuse material with reflectance (1,1,1).
	 */
	public Reflective()
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
		return new Spectrum(1,1,1);
	}

	public boolean hasSpecularReflection()
	{
		return true;
	}
	
	public ShadingSample evaluateSpecularReflection(HitRecord hitRecord)
	{	
		Vector3f i = new Vector3f(hitRecord.w);
		i.negate();
		i.normalize();
		
		float cosThetaI = -i.dot(hitRecord.normal); 
				
		Vector3f r = new Vector3f();
		r.scaleAdd(2*cosThetaI,hitRecord.normal,i);
		
		ShadingSample sample = new ShadingSample(new Spectrum(1,1,1), new Spectrum(0,0,0),r,true,1);
		return sample;
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
		return null;	
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
