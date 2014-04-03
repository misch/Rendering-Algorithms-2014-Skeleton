package rt.materials;

import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Material;
import rt.Spectrum;

/**
 * This material should be used with {@link rt.lightsources.AreaLight}.
 */
public class AreaLightMaterial implements Material {

	private Spectrum emission;
	private float area;
	
	public AreaLightMaterial(Spectrum emission, float area)
	{
		this.emission = new Spectrum(emission);
		this.area = area;
	}
	
	public Spectrum evaluateEmission(HitRecord hitRecord, Vector3f wOut) {
		Spectrum spec = new Spectrum(emission);
//		float cosTerm = Math.max(hitRecord.normal.dot(wOut),0);
//		spec.mult(cosTerm);
		spec.mult(1f/((float)Math.PI*area));
		return spec;
	}

	/**
	 * Return a random direction over the full sphere of directions.
	 */
	public ShadingSample getEmissionSample(HitRecord hitRecord, float[] sample) {
		return null;
	}

	/** 
	 * Shouldn't be called on a point light
	 */
	public ShadingSample getShadingSample(HitRecord hitRecord, float[] sample) {
		return null;
	}

	/** 
	 * Shouldn't be called on a point light
	 */
	public boolean castsShadows() {
		return false;
	}

	/** 
	 * Shouldn't be called on a point light
	 */
	public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut,
			Vector3f wIn) {
		return new Spectrum(emission);
	}
	
	/** 
	 * Shouldn't be called on a point light
	 */
	public boolean hasSpecularReflection() {
		return false;
	}

	/** 
	 * Shouldn't be called on a point light
	 */
	public ShadingSample evaluateSpecularReflection(HitRecord hitRecord) {
		return null;
	}

	/** 
	 * Shouldn't be called on a point light
	 */
	public boolean hasSpecularRefraction() {
		return false;
	}

	/** 
	 * Shouldn't be called on a point light
	 */
	public ShadingSample evaluateSpecularRefraction(HitRecord hitRecord) {
		return null;
	}


}
