package rt.intersectables;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.vecmath.Matrix3f;
import javax.vecmath.Point2f;
import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Intersectable;
import rt.Material;
import rt.Ray;

public class Bumpy implements Intersectable {

	Intersectable intersectable;
	BufferedImage bumpMap;
	public Material material;
	
	public Bumpy(Intersectable intersectable, String bumpMapFilePath){
		this.intersectable = intersectable;

		try {
			bumpMap = ImageIO.read(new File(bumpMapFilePath));
		} catch (IOException e) {
			System.out.println("Could not load bump map.");
		}
	}
	@Override
	public HitRecord intersect(Ray r) {
		HitRecord actualHit = this.intersectable.intersect(r);
		if (actualHit == null){
			return null;
		}
		
		Vector3f bumpNormal = bilinearInterpolation(actualHit.u, actualHit.v);
		Matrix3f toLocalFrame = actualHit.getLocalFrameTransformation();
		
		toLocalFrame.transform(bumpNormal);
//		bumpNormal.normalize();
		
		actualHit.normal = bumpNormal;
		
		return actualHit;
	}

	@Override
	public AxisAlignedBoundingBox getBoundingBox() {
		return intersectable.getBoundingBox();
	}
	
	public Vector3f bilinearInterpolation(float x, float y) {
		Point2f imageCoord = new Point2f(scaledX(x), scaledY(y));

		int[][] colImagePix = new int[2][2];
		try {
			colImagePix[0][0] = bumpMap.getRGB((int) imageCoord.x,
					(int) imageCoord.y);
			colImagePix[1][0] = bumpMap.getRGB((int) imageCoord.x + 1,
					(int) imageCoord.y);
			colImagePix[0][1] = bumpMap.getRGB((int) imageCoord.x,
					(int) imageCoord.y + 1);
			colImagePix[1][1] = bumpMap.getRGB((int) imageCoord.x + 1,
					(int) imageCoord.y + 1);
		} catch (ArrayIndexOutOfBoundsException a) {
			return new Vector3f();
		}

		float distanceVertical = imageCoord.x - (int) imageCoord.x;
		float distanceHorizontal = imageCoord.y - (int) imageCoord.y;

		int colTop = interpolate(colImagePix[1][1], colImagePix[0][1],
				distanceVertical);
		int colBot = interpolate(colImagePix[1][0], colImagePix[0][0],
				distanceVertical);
		int colMid = interpolate(colTop, colBot, distanceHorizontal);

		return hexToVec(colMid);
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
		return x * (bumpMap.getWidth() - 1);
	}

	private float scaledY(float y) {
		return (1 - y) * (bumpMap.getHeight() - 1);
	}

	public Vector3f getNearestNeighbour(float x, float y) {
		int hexColor = bumpMap.getRGB(Math.round(scaledX(x)),
				Math.round(scaledY(y)));

		return hexToVec(hexColor);
	}

	/** Convert hexadecimal color representation into a Vector3f 
	 * @param hexColor the int-value of the hex-representation of the color that should be converted (e.g. 0xFFFFFF)
	 * @return Vector3f
	 **/
	private Vector3f hexToVec(int hexColor) {
		
		int x = hexColor >> 16 & 0xFF;
		int y = hexColor >> 8 & 0xFF;
		int z = hexColor & 0xFF;

		Vector3f vec = new Vector3f(x,y,z);
		vec.normalize();
		return vec;
	}

}
