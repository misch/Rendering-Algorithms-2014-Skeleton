package rt;

import java.awt.image.BufferedImage;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;

public class MathUtil {

	public static Point2f midnightFormula(float a, float b, float c) {
		float discriminant = b*b - 4*a*c;
		if (discriminant < 0){
			return null;
		}
		
		Point2f point = new Point2f();
		point.x = (float) ((-b + Math.sqrt(discriminant)) / (2 * a));
		point.y = (float) ((-b - Math.sqrt(discriminant)) / (2 * a));

		return point;
	}
	
	public static Point3f bilinearInterpolation(float x, float y, BufferedImage tex) {
		Point2f imageCoord = new Point2f(scaledX(x, tex.getWidth()), scaledY(y, tex.getHeight()));

		int[][] colImagePix = new int[2][2];
		try {
			colImagePix[0][0] = tex.getRGB((int) imageCoord.x,
					(int) imageCoord.y);
			colImagePix[1][0] = tex.getRGB((int) imageCoord.x + 1,
					(int) imageCoord.y);
			colImagePix[0][1] = tex.getRGB((int) imageCoord.x,
					(int) imageCoord.y + 1);
			colImagePix[1][1] = tex.getRGB((int) imageCoord.x + 1,
					(int) imageCoord.y + 1);
		} catch (ArrayIndexOutOfBoundsException a) {
			return new Point3f();
		}

		float distanceVertical = imageCoord.x - (int) imageCoord.x;
		float distanceHorizontal = imageCoord.y - (int) imageCoord.y;

		int colTop = interpolate(colImagePix[1][1], colImagePix[0][1],
				distanceVertical);
		int colBot = interpolate(colImagePix[1][0], colImagePix[0][0],
				distanceVertical);
		int colMid = interpolate(colTop, colBot, distanceHorizontal);

		return hexToTuple(colMid);
	}
	
	public static Point3f nearestNeighbour(float x, float y, BufferedImage tex) {
		int hexColor = tex.getRGB(Math.round(scaledX(x, tex.getWidth())),
				Math.round(scaledY(y, tex.getHeight())));
		return hexToTuple(hexColor);
	}
	
	private static Point3f hexToTuple(int hexVal){
		
			int x = hexVal >> 16 & 0xFF;
			int y = hexVal >> 8 & 0xFF;
			int z = hexVal & 0xFF;

			return new Point3f(x/255f, y/255f, z/255f);
	}
	
	private static int interpolate(int rgb1, int rgb2, float distance) {
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
	
	private static float scaledX(float x, int width) {
		return x * (width - 1);
	}

	private static float scaledY(float y, int height) {
		return (1 - y) * (height - 1);
	}
	

}
