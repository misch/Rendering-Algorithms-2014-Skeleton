package rt.materials;

import java.util.Random;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Material;
import rt.Spectrum;
import rt.Material.ShadingSample;

public class Glossy implements Material {

	private float shininess;
	private Spectrum n;
	private Spectrum k;
	private Spectrum n2Plusk2;

	public Glossy(float shininess, Spectrum n, Spectrum k){
		this.shininess = shininess;
		this.n = n;
		this.k = k;
		Spectrum n2 = new Spectrum(n); n2.sqr(); // n^2
		Spectrum k2 = new Spectrum(k); k2.sqr(); // k^2
		this.n2Plusk2 = new Spectrum(n2); n2Plusk2.add(k2); // (n^2 + k^2)
	}
	
	@Override
	public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut, Vector3f wIn) {
//		if (hitRecord.normal.dot(wIn) < 0 && hitRecord.normal.dot(wOut) < 0)
//			return new Spectrum(1,0,1);
//		if (hitRecord.normal.dot(wIn) < 0){
//			return new Spectrum(1,0,0);
//		}
//		if (hitRecord.normal.dot(wOut) <0){
//			return new Spectrum(0,0,1);
//		}
		
		Vector3f w_h = new Vector3f();
		w_h.add(wIn,wOut);
		w_h.normalize();

		assert(Math.abs(wIn.length()-1) < 1e-5f);
		assert(Math.abs(wOut.length()-1) < 1e-5f);
		assert(Math.abs(w_h.length()-1) < 1e-5f);
		assert(Math.abs(hitRecord.normal.length()-1) < 1e-5f);
		
		Spectrum F = computeFresnel(wIn, hitRecord.normal);
		float G = computeGeometryTerm(wIn, wOut, w_h, hitRecord.normal);
		float D = computeMicrofacetDistribution(shininess, hitRecord.normal, w_h);
		
		F.mult(G);
		F.mult(D);
		F.divide(4 * wIn.dot(hitRecord.normal) * wOut.dot(hitRecord.normal));
		return F;
	}

	private float computeMicrofacetDistribution(float shininess,Vector3f normal, Vector3f w_h) {
		return (shininess + 2)/((float)(2*Math.PI)) * (float)Math.pow(normal.dot(w_h),shininess);
	}

	private float computeGeometryTerm(Vector3f wIn, Vector3f wOut, Vector3f w_h, Vector3f normal) {
		
		assert(Math.abs(wIn.length()-1) < 1e-5f);
		assert(Math.abs(wOut.length()-1) < 1e-5f);
		assert(Math.abs(w_h.length()-1) < 1e-5f);
		assert(Math.abs(normal.length()-1) < 1e-5f);
		
		float nWh = normal.dot(w_h);
		float nWi = normal.dot(wIn);
		float nWo = normal.dot(wOut);
		float WoWh = wOut.dot(w_h);
		
		float stuff1 = (2 * nWh * nWo)/WoWh;
		float stuff2 =  (2 * nWh * nWi)/WoWh;
		
		float stuff = Math.min(stuff1,stuff2);
		
		float result = Math.min(1,stuff);
		return result;
	}

	private Spectrum computeFresnel(Vector3f wIn, Vector3f normal){
		float cosThetaI = wIn.dot(normal); // cos(theta_i)
		Spectrum twoNCosThetaI = new Spectrum(n); twoNCosThetaI.mult(2 * cosThetaI); // 2n*cos(theta_i) 
		
		Spectrum r1 = new Spectrum(n2Plusk2); 
		r1.mult(cosThetaI*cosThetaI); 
		r1.sub(twoNCosThetaI);
		r1.add(1);
		
		Spectrum r1_denom = new Spectrum(n2Plusk2);
		r1_denom.mult(cosThetaI*cosThetaI);
		r1_denom.add(twoNCosThetaI);
		r1_denom.add(1);

		r1.divide(r1_denom);
		
		Spectrum r2 = new Spectrum(n2Plusk2);
		r2.sub(twoNCosThetaI);
		r2.add(cosThetaI*cosThetaI);
		
		Spectrum r2_denom = new Spectrum(n2Plusk2);
		r2_denom.add(twoNCosThetaI);
		r2_denom.add(cosThetaI*cosThetaI);
		
		r2.divide(r2_denom);
		
		Spectrum avg = new Spectrum(r1);
		avg.add(r2);
		avg.mult(0.5f);

		return avg;
	}
	@Override
	public Spectrum evaluateEmission(HitRecord hitRecord, Vector3f wOut) {
		return null;
	}

	@Override
	public boolean hasSpecularReflection() {
		return false;
	}

	@Override
	public ShadingSample evaluateSpecularReflection(HitRecord hitRecord) {
		return null;
	}

	@Override
	public boolean hasSpecularRefraction() {
		return false;
	}

	@Override
	public ShadingSample evaluateSpecularRefraction(HitRecord hitRecord) {
		return null;
	}

	@Override
	public ShadingSample getShadingSample(HitRecord hitRecord, float[] sample) {
		float psi1 = sample[0], psi2 = sample[1];

		// sample half-vector w_h
		float cosTheta = (float) Math.pow(psi1, 1f/(shininess+1));
		float sinTheta = (float) Math.sqrt(1-cosTheta*cosTheta);
		float phi = (float) (2 * Math.PI * psi2);

		Vector3f w_h = new Vector3f();
		w_h.x = (float) (sinTheta * Math.cos(phi));
		w_h.y = (float) (sinTheta * Math.sin(phi));
		w_h.z = cosTheta;
		w_h.normalize();
		
		// transform sampled direction to local coordinate system
		Matrix3f canonicToLocalFrame = hitRecord.getLocalFrameTransformation();
		canonicToLocalFrame.transform(w_h);
		assert(Math.abs(w_h.length())-1 < 1e-5f);
		assert(Math.abs(hitRecord.normal.length() - 1) < 1e-5f);
		assert(Math.abs(w_h.dot(hitRecord.normal) - cosTheta) < 1e-5f);
		
		// get w_i by reflecting w_o around w_h
		Vector3f w_o = new Vector3f(hitRecord.w);
		w_o.negate();
		w_o.normalize();
		
		float cosThetaI = -w_o.dot(hitRecord.normal); 
				
		Vector3f w_i = new Vector3f();
		w_i.scaleAdd(2*cosThetaI,hitRecord.normal,w_o);
		
		
		// compute probability
		float probability = (float) ((shininess+1)/(8*w_o.dot(w_h)*Math.PI) * Math.pow(cosTheta, shininess));
		
		ShadingSample shadingSample = new ShadingSample(evaluateBRDF(hitRecord,w_o,w_i), new Spectrum(0,0,0),w_i,false,probability);
		// TODO: Check if directions w_o and w_i are the right way around.
		// TODO: If w_i is below horizon, then return brdf = 0 (see slides 05, p.77)
		return shadingSample;
	}

	@Override
	public ShadingSample getEmissionSample(HitRecord hitRecord, float[] sample) {
		return null;
	}

	@Override
	public boolean castsShadows() {
		return false;
	}

}
