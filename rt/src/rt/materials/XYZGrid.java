package rt.materials;

import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Material;
import rt.Spectrum;
import rt.StaticVecmath;

public class XYZGrid implements Material {

	float scale;
	float thickness;
	Vector3f shift;
	Spectrum lineColor;
	Spectrum tileColor;

	public XYZGrid(Spectrum lineColor, Spectrum tileColor, float thickness,
			Vector3f shift) {
		this.lineColor = lineColor;
		this.tileColor = tileColor;
		this.thickness = thickness;
		this.shift = shift;
		this.scale = 1.0f;
	}

	@Override
	public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut,
			Vector3f wIn) {
		
		Vector3f hitPoint = new Vector3f(hitRecord.position);
		
		// Shift
		Vector3f shifted = new Vector3f(hitRecord.position);
		shifted.add(shift);
		
		// Scaling
		hitPoint.scale(1/scale);
		shifted.scale(1/scale);
		float thickness = this.thickness/this.scale;
		
		Vector3f roundedPosition = new Vector3f(Math.round(hitPoint.x), Math.round(hitPoint.y),Math.round(hitPoint.z));
		Vector3f diff = StaticVecmath.sub(shifted, roundedPosition);
		diff.absolute();
		
		if (diff.x < thickness || diff.y < thickness || diff.z < thickness)
			return lineColor;
		else
			return tileColor;
	}

	@Override
	public Spectrum evaluateEmission(HitRecord hitRecord, Vector3f wOut) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasSpecularReflection() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ShadingSample evaluateSpecularReflection(HitRecord hitRecord) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasSpecularRefraction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ShadingSample evaluateSpecularRefraction(HitRecord hitRecord) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ShadingSample getShadingSample(HitRecord hitRecord, float[] sample) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ShadingSample getEmissionSample(HitRecord hitRecord, float[] sample) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean castsShadows() {
		// TODO Auto-generated method stub
		return false;
	}

}
