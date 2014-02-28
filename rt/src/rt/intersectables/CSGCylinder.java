package rt.intersectables;

import java.util.ArrayList;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Material;
import rt.Ray;
import rt.Spectrum;
import rt.materials.Diffuse;

public class CSGCylinder extends CSGSolid {

	Vector3f center;
	float radius;
	public Material material;

	/**
	 * Makes a CSG sphere.
	 * 
	 * @param center
	 *            the sphere center
	 * @param radius
	 *            radius of the sphere
	 */
	public CSGCylinder(Vector3f center, float radius) {
		this.center = center;
		this.radius = radius;

		material = new Diffuse(new Spectrum(1.f, 1.f, 1.f));
	}

	@Override
	ArrayList<IntervalBoundary> getIntervalBoundaries(Ray r) {
		ArrayList<IntervalBoundary> boundaries = new ArrayList<IntervalBoundary>();

		float t_near, t_far;

		Vector2f direction2D = new Vector2f(r.direction.x,r.direction.z);
		float a = direction2D.lengthSquared();

		Vector2f rayOrigin2D = new Vector2f(r.origin.x,r.origin.z);
		Vector2f center2D = new Vector2f(center.x,center.z);
		Vector2f centerToRay = new Vector2f();
		centerToRay.sub(rayOrigin2D, center2D);

		float b = 2 * direction2D.dot(centerToRay);

		float c = centerToRay.dot(centerToRay) - radius * radius;

		float discriminant = b * b - 4 * a * c;
		if (discriminant < 0) {
			return boundaries;
		} else {
			Point2f roots = midnightFormula(a, b, c);
			t_near = Math.min(roots.x, roots.y);
			t_far = Math.max(roots.x, roots.y);

			IntervalBoundary b1, b2;
			b1 = new IntervalBoundary();
			b2 = new IntervalBoundary();

			b1.hitRecord = intersectCylinder(r, t_near);
			b1.t = t_near;
			b1.type = BoundaryType.START;

			b2.hitRecord = intersectCylinder(r, t_far);
			b2.t = t_near;
			b2.type = BoundaryType.END;

			boundaries.add(b1);
			boundaries.add(b2);
		}

		return boundaries;
	}

	private HitRecord intersectCylinder(Ray r, float t) {
		Vector3f position = new Vector3f(r.direction);
		position.scaleAdd(t, r.origin);

		Vector3f normal = new Vector3f(position);
		normal.sub(center);
		normal.y = 0;
		normal.normalize();

		// wIn is incident direction; convention is that it points away from
		// surface
		Vector3f wIn = new Vector3f(r.direction);
		wIn.negate();

		return new HitRecord(t, position, normal, wIn, this, material, 0.f, 0.f);
	}

	private Point2f midnightFormula(float a, float b, float discriminant) {
		Point2f point = new Point2f();
		point.x = (float) ((-b + Math.sqrt(discriminant)) / (2 * a));
		point.y = (float) ((-b - Math.sqrt(discriminant)) / (2 * a));

		return point;
	}

}
