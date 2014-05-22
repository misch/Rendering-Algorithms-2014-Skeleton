package rt.intersectables;

import java.util.ArrayList;

import javax.vecmath.Point2f;
import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Material;
import rt.MathUtil;
import rt.Ray;
import rt.Spectrum;
import rt.intersectables.CSGSolid.BoundaryType;
import rt.materials.Diffuse;

public class CSGSphere extends CSGSolid {

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
	public CSGSphere(Vector3f center, float radius, Material material) {
		this.center = center;
		this.radius = radius;
		
		this.material = material;
	}
	
	public CSGSphere(Vector3f center, float radius) {
		this(center,radius,new Diffuse(new Spectrum(1,1,1)));
	}

	@Override
	ArrayList<IntervalBoundary> getIntervalBoundaries(Ray r) {
		ArrayList<IntervalBoundary> boundaries = new ArrayList<IntervalBoundary>();

		float t_near, t_far;

		float a = r.direction.lengthSquared();

		Vector3f centerToRay = new Vector3f();
		centerToRay.sub(r.origin, center);

		float b = 2 * r.direction.dot(centerToRay);

		float c = centerToRay.dot(centerToRay) - radius * radius;

		Point2f roots = MathUtil.midnightFormula(a, b, c);
		if (roots == null)
			return boundaries;

		t_near = Math.min(roots.x, roots.y);
		t_far = Math.max(roots.x, roots.y);

		IntervalBoundary b1, b2;
		b1 = new IntervalBoundary();
		b2 = new IntervalBoundary();

		b1.hitRecord = intersectSphere(r, t_near);
		b1.t = t_near;

		b2.hitRecord = intersectSphere(r, t_far);
		b2.t = t_far;

		if(r.direction.dot(b1.hitRecord.normal) < 0){
			b1.type = BoundaryType.START;
			b2.type = BoundaryType.END;
		}else{
			b1.type = BoundaryType.END;
			b2.type = BoundaryType.START;
		}
		
		boundaries.add(b1);
		boundaries.add(b2);
		return boundaries;
	}

	private HitRecord intersectSphere(Ray r, float t) {
		Vector3f position = new Vector3f(r.direction);
		position.scaleAdd(t, r.origin);

		Vector3f normal = new Vector3f(position);
		normal.sub(center);
		normal.normalize();

		// wIn is incident direction; convention is that it points away from
		// surface
		Vector3f wIn = new Vector3f(r.direction);
		wIn.negate();

		float u = 0.5f - (float) (Math.asin(position.y)/Math.PI);
		float v = 0.5f + (float) (Math.atan(position.x/position.z)/(2*Math.PI));
		
		return new HitRecord(t, position, normal, wIn, this, material, u, v);
	}

	@Override
	public AxisAlignedBoundingBox getBoundingBox() {
		// TODO Auto-generated method stub
		return null;
	}
}
