package rt.intersectables;

import javax.vecmath.Point2f;
import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Intersectable;
import rt.Material;
import rt.MathUtil;
import rt.Ray;
import rt.Spectrum;
import rt.materials.Diffuse;

public class Sphere implements Intersectable {

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
	public Sphere(Vector3f center, float radius) {
		this(center, radius, new Diffuse(new Spectrum(1,1,1)));
	}
	
	public Sphere(Vector3f center, float radius, Material material) {
		this.center = center;
		this.radius = radius;

		this.material = material;
	}

	public Sphere() {
		this(new Vector3f(0, 0, 0), 1);
	}

	@Override
	public HitRecord intersect(Ray r) {
		float t;

		float a = r.direction.lengthSquared();

		Vector3f centerToRay = new Vector3f();
		centerToRay.sub(r.origin, center);

		float b = 2 * r.direction.dot(centerToRay);

		float c = centerToRay.dot(centerToRay) - radius * radius;

		Point2f roots = MathUtil.midnightFormula(a, b, c);
		if (roots == null)
			return null;

		t = Math.min(roots.x, roots.y);
		
		if (t<0){ // If the hit point is behind the ray
			return null;
		}

		Vector3f position = new Vector3f(r.direction);
		position.scaleAdd(t, r.origin);

		Vector3f normal = new Vector3f(position);
		normal.sub(center);
		normal.normalize();

		// wIn is incident direction; convention is that it points away from
		// surface
		Vector3f wIn = new Vector3f(r.direction);
		wIn.negate();

		return new HitRecord(t, position, normal, wIn, this, material, 0.f, 0.f);
	}
}
