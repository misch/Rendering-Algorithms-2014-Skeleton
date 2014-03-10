package rt.intersectables;

import java.util.ArrayList;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point2f;
import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Material;
import rt.MathUtil;
import rt.Ray;
import rt.Spectrum;
import rt.materials.Diffuse;

public class CSGTwoSidedInfiniteCone extends CSGSolid {

	Vector3f center;
	public Material material;

	/**
	 * Makes a CSG cone.
	 * 
	 * @param center
	 *            the point where the cone has radius 0            
	**/

	public CSGTwoSidedInfiniteCone(Vector3f center) {
		this.center = center;

		material = new Diffuse(new Spectrum(1.f, 1.f, 1.f));
	}

	@Override
	ArrayList<IntervalBoundary> getIntervalBoundaries(Ray r) {
		ArrayList<IntervalBoundary> boundaries = new ArrayList<IntervalBoundary>();

		float t_near, t_far;

		Vector3f directionFlippedY = new Vector3f(r.direction);
//		Vector2f direction = new Vector2f(r.direction.x,r.direction.y);
		directionFlippedY.y *= -1;
		float a = r.direction.dot(directionFlippedY);

		Vector3f centerToRay = new Vector3f();
		centerToRay.sub(r.origin, center);
		Vector3f centerToRayFlippedY = new Vector3f(centerToRay);
		centerToRayFlippedY.y *= -1;

		float b = 2 * r.direction.dot(centerToRayFlippedY);

		float c = centerToRay.dot(centerToRayFlippedY);
			

		float discriminant = b * b - 4 * a * c;
		if (discriminant < 0) {
			return boundaries;
		} else {
			Point2f roots = MathUtil.midnightFormula(a, b, discriminant);
			t_near = Math.min(roots.x, roots.y);
			t_far = Math.max(roots.x, roots.y);

			IntervalBoundary b1, b2;
			b1 = new IntervalBoundary();
			b2 = new IntervalBoundary();

			b1.hitRecord = intersectCone(r, t_near);
			b1.t = t_near;
			b1.type = BoundaryType.START;

			b2.hitRecord = intersectCone(r, t_far);
			b2.t = t_near;
			b2.type = BoundaryType.END;

			boundaries.add(b1);
			boundaries.add(b2);
		}

		return boundaries;
	}

	private HitRecord intersectCone(Ray r, float t) {
		Vector3f position = new Vector3f(r.direction);
		position.scaleAdd(t, r.origin);

		Vector3f normalCyl = new Vector3f(position); // b!
		normalCyl.sub(center);
		normalCyl.y = 0; // TODO: Correct normals!
//		normalCyl.normalize();
		
		Vector3f a = new Vector3f(0,0,1);
		
		Vector3f tangent = new Vector3f();
		tangent.cross(a,normalCyl);
		
		Vector3f normal = new Vector3f();
		normal.cross(r.direction,tangent);
		normal.normalize();	

		// wIn is incident direction; convention is that it points away from
		// surface
		Vector3f wIn = new Vector3f(r.direction);
		wIn.negate();

		return new HitRecord(t, position, normalCyl, wIn, this, material, 0.f, 0.f);
	}
}
