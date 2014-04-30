package rt.materials;

import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Material;
import rt.Spectrum;
import rt.StaticVecmath;

public class XYZGrid extends Diffuse implements Material {

	float scale;
	float thickness;
	Vector3f shift;
	Spectrum lineColor;
	Spectrum tileColor;

	public XYZGrid(Spectrum lineColor, Spectrum tileColor, float thickness,
			Vector3f shift) {
		this(lineColor, tileColor, thickness, shift, 1);
	}
	
	public XYZGrid(Spectrum lineColor, Spectrum tileColor, float thickness,
			Vector3f shift, float scale) {
		super(new Spectrum(1));
		this.lineColor = lineColor;
		this.tileColor = tileColor;
		this.thickness = thickness;
		this.shift = shift;
		this.scale = scale;
	}

	@Override
	public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut,
			Vector3f wIn) {
		Spectrum diffuseBRDF = super.evaluateBRDF(hitRecord, wOut, wIn);
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
			diffuseBRDF.mult(lineColor);
		else
			diffuseBRDF.mult(tileColor);
		
		return diffuseBRDF;
	}

}
