package rt.materials;

import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Material;
import rt.Spectrum;

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
