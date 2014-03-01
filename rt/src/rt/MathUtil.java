package rt;

import javax.vecmath.Point2f;

public class MathUtil {

	public static Point2f midnightFormula(float a, float b, float discriminant) {
		Point2f point = new Point2f();
		point.x = (float) ((-b + Math.sqrt(discriminant)) / (2 * a));
		point.y = (float) ((-b - Math.sqrt(discriminant)) / (2 * a));

		return point;
	}
}
