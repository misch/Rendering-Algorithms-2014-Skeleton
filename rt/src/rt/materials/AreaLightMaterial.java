package rt.materials;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Material;
import rt.Spectrum;
import rt.Material.ShadingSample;

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
		float psi1 = sample[0], psi2 = sample[1];
		
		Vector3f direction = new Vector3f();
		direction.x = (float)(Math.cos(2*Math.PI*psi2) * Math.sqrt(psi1));
		direction.y = (float)(Math.sin(2*Math.PI*psi2) * Math.sqrt(psi1));
		direction.z = (float)Math.sqrt(1-psi1);
		assert(Math.abs(direction.length()) -1 < 1e-6f);
		// transform sampled direction to local coordinate system
		Matrix3f canonicToLocalFrame = hitRecord.getLocalFrameTransformation();
		canonicToLocalFrame.transform(direction);
		assert(Math.abs(direction.length()) -1 < 1e-6f);
		direction.normalize();
		
		float probability = (float) (direction.dot(hitRecord.normal)/Math.PI);
		
		ShadingSample shadingSample = new ShadingSample(new Spectrum(emission), new Spectrum(0,0,0),direction,false,probability);
		return shadingSample;
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
