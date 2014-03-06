package rt.materials;

import javax.vecmath.Vector3f;

import rt.*;

/**
 * A basic diffuse material.
 */
public class Blinn implements Material {

	private Spectrum kd;
	private Spectrum ks;
	private float s;
	
	/**
	 * Note that the parameter value {@param kd} is the diffuse reflectance,
	 * which should be in the range [0,1], a value of 1 meaning all light
	 * is reflected (diffusely), and none is absorbed. The diffuse BRDF
	 * corresponding to {@param kd} is actually {@param kd}/pi.
	 * 
	 * @param kd the diffuse reflectance
	 */
	public Blinn(Spectrum kd, Spectrum ks, float s)
	{
		this.kd = new Spectrum(kd);
		this.ks = new Spectrum(ks);
		this.s = s;
		
		// Normalize
		this.kd.mult(1/(float)Math.PI);
	}
	

	/**
	 * Returns diffuse BRDF value, that is, a constant.
	 * 
	 *  @param wOut outgoing direction, by convention towards camera
	 *  @param wIn incident direction, by convention towards light
	 *  @param hitRecord hit record to be used
	 */
	public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut, Vector3f wIn) {
		Spectrum kd = new Spectrum(this.kd);
		kd.mult(wIn.dot(hitRecord.normal));
		
		Spectrum ks = new Spectrum(this.ks);
		
		Vector3f h = new Vector3f();
		h.add(wIn, wOut);
		h.normalize();
		ks.mult((float)Math.pow(h.dot(hitRecord.normal),this.s));
		
		Spectrum ka = new Spectrum(this.kd);
		
		Spectrum total = new Spectrum(kd);
		total.add(ks);
		total.add(ka);
		
		return total;
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
		return null;	
	}
		
	public boolean castsShadows()
	{
		return true;
	}
	
	public Spectrum evaluateEmission(HitRecord hitRecord, Vector3f wOut) {
		return new Spectrum(0.f, 0.f, 0.f);
	}

	public ShadingSample getEmissionSample(HitRecord hitRecord, float[] sample) {
		return new ShadingSample();
	}
	
}
