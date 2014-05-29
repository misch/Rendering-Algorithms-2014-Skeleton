package rt.materials;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.vecmath.Matrix3f;
import javax.vecmath.Point2f;
import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Material;
import rt.MathUtil;
import rt.Spectrum;

public class Textured implements Material {

	Material underlying;
	BufferedImage texture;

	public Textured(String filePath) {
		this(filePath,new Diffuse());
	}
	
	public Textured(String filePath, Material underlying){
		try {
			texture = ImageIO.read(new File(filePath));
		} catch (IOException e) {
			System.out.println("Could not load texture.");
		}
		this.underlying = underlying;
	}

	public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut,
			Vector3f wIn) {
		Spectrum brdf = underlying.evaluateBRDF(hitRecord, wOut, wIn);
		Spectrum textureColor = new Spectrum(MathUtil.bilinearInterpolation(hitRecord.u, hitRecord.v, texture));
		
		textureColor.mult(brdf);
		return textureColor;
	}

	/** Other methods of the material are delegated to the underlying material. **/
	
	public boolean hasSpecularReflection() {
		return underlying.hasSpecularReflection();
	}

	public ShadingSample evaluateSpecularReflection(HitRecord hitRecord) {
		return underlying.evaluateSpecularReflection(hitRecord);
	}

	public boolean hasSpecularRefraction() {
		return underlying.hasSpecularRefraction();
	}

	public ShadingSample evaluateSpecularRefraction(HitRecord hitRecord) {
		return underlying.evaluateSpecularRefraction(hitRecord);
	}

	public ShadingSample getShadingSample(HitRecord hitRecord, float[] sample) {
		return underlying.getShadingSample(hitRecord, sample);
	}

	public boolean castsShadows() {
		return underlying.castsShadows();
	}

	public Spectrum evaluateEmission(HitRecord hitRecord, Vector3f wOut) {
		return underlying.evaluateEmission(hitRecord, wOut);
	}

	public ShadingSample getEmissionSample(HitRecord hitRecord, float[] sample) {
		return underlying.getEmissionSample(hitRecord, sample);
	}

	public float getDirectionalProbability(HitRecord hitRecord, Vector3f wOut) {
		return underlying.getDirectionalProbability(hitRecord, wOut);
	}
}
