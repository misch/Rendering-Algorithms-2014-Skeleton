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

	public Spectrum bilinearInterpolation(float x, float y) {
		Point2f imageCoord = new Point2f(scaledX(x), scaledY(y));

		int[][] colImagePix = new int[2][2];
		try {
			colImagePix[0][0] = texture.getRGB((int) imageCoord.x,
					(int) imageCoord.y);
			colImagePix[1][0] = texture.getRGB((int) imageCoord.x + 1,
					(int) imageCoord.y);
			colImagePix[0][1] = texture.getRGB((int) imageCoord.x,
					(int) imageCoord.y + 1);
			colImagePix[1][1] = texture.getRGB((int) imageCoord.x + 1,
					(int) imageCoord.y + 1);
		} catch (ArrayIndexOutOfBoundsException a) {
			return new Spectrum();
		}

		float distanceVertical = imageCoord.x - (int) imageCoord.x;
		float distanceHorizontal = imageCoord.y - (int) imageCoord.y;

		int colTop = interpolate(colImagePix[1][1], colImagePix[0][1],
				distanceVertical);
		int colBot = interpolate(colImagePix[1][0], colImagePix[0][0],
				distanceVertical);
		int colMid = interpolate(colTop, colBot, distanceHorizontal);

		return hexToSpectrum(colMid);
	}

	private int interpolate(int rgb1, int rgb2, float distance) {
		int red1 = (rgb1 & 0xff0000) >> 16;
		int red2 = (rgb2 & 0xff0000) >> 16;
		int newRed = (int) (distance * red1 + (1 - distance) * red2);

		int green1 = (rgb1 & 0x00ff00) >> 8;
		int green2 = (rgb2 & 0x00ff00) >> 8;
		int newGreen = (int) (distance * green1 + (1 - distance) * green2);

		int blue1 = (rgb1 & 0x0000ff);
		int blue2 = (rgb2 & 0x0000ff);
		int newBlue = (int) (distance * blue1 + (1 - distance) * blue2);

		int newRGB = ((newRed << 16) ^ (newGreen << 8) ^ newBlue);

		return newRGB;
	}

	private float scaledX(float x) {
		return x * (texture.getWidth() - 1);
	}

	private float scaledY(float y) {
		return (1 - y) * (texture.getHeight() - 1);
	}

	public Spectrum getNearestNeighbour(float x, float y) {
		int hexColor = texture.getRGB(Math.round(scaledX(x)),
				Math.round(scaledY(y)));

		return hexToSpectrum(hexColor);
	}

	/** Convert hexadecimal color representation into a Spectrum 
	 * @param hexColor the int-value of the hex-representation of the color that should be converted (e.g. 0xFFFFFF)
	 * @return Spectrum
	 **/
	private Spectrum hexToSpectrum(int hexColor) {
		
		int red = hexColor >> 16 & 0xFF;
		int green = hexColor >> 8 & 0xFF;
		int blue = hexColor & 0xFF;

		return new Spectrum(red/255f, green/255f, blue/255f);
	}

	

	public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut,
			Vector3f wIn) {
		Spectrum brdf = underlying.evaluateBRDF(hitRecord, wOut, wIn);
		Spectrum texture = bilinearInterpolation(hitRecord.u,hitRecord.v);
		
		texture.mult(brdf);
		return texture;
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
