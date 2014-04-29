package rt.materials;

import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Material;
import rt.Spectrum;

public class TwoSidedDiffuse extends Diffuse implements Material {

	public TwoSidedDiffuse(Spectrum kd){
		super(kd);
	}
	
	public TwoSidedDiffuse(){
		super();
	}
	
	@Override
	/**
	 * Returns diffuse BRDF value, that is, a constant.
	 * 
	 *  @param wOut outgoing direction, by convention towards camera
	 *  @param wIn incident direction, by convention towards light
	 *  @param hitRecord hit record to be used
	 */
	public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut, Vector3f wIn) {
		if (hitRecord.normal.dot(wOut) < 0){
			hitRecord.normal.negate();
		}
		return new Spectrum(kd);
	}
}
