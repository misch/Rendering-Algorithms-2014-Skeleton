package rt;

import javax.vecmath.Point2f;

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
}
