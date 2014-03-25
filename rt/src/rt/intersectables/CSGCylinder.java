package rt.intersectables;

import java.util.ArrayList;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Material;
import rt.MathUtil;
import rt.Ray;
import rt.Spectrum;
import rt.intersectables.CSGSolid.BoundaryType;
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
	public CSGCylinder(Vector3f center, float radius, Material material) {
		this.center = center;
		this.radius = radius;
		this.material = material;
	}
	
	public CSGCylinder(){
		this(new Vector3f(0,0,0), 1, new Diffuse(new Spectrum(1,1,1)));
	}
	
	public CSGCylinder(Vector3f center, float radius){
		this(center, radius, new Diffuse(new Spectrum(1,1,1)));
	}
	
	public CSGCylinder(Material material){
		this(new Vector3f(0,0,0),1,material);
	}

	@Override
	ArrayList<IntervalBoundary> getIntervalBoundaries(Ray r) {
		ArrayList<IntervalBoundary> boundaries = new ArrayList<IntervalBoundary>();

		float t_near, t_far;

		Vector2f direction2D = new Vector2f(r.direction.x, r.direction.y);
		float a = direction2D.lengthSquared();

		Vector2f rayOrigin2D = new Vector2f(r.origin.x, r.origin.y);
		Vector2f center2D = new Vector2f(center.x, center.y);
		Vector2f centerToRay = new Vector2f();
		centerToRay.sub(rayOrigin2D, center2D);

		float b = 2 * direction2D.dot(centerToRay);

		float c = centerToRay.dot(centerToRay) - radius * radius;

		Point2f roots = MathUtil.midnightFormula(a, b, c);
		if (roots == null)
			return boundaries;
		
		t_near = Math.min(roots.x, roots.y);
		t_far = Math.max(roots.x, roots.y);

		IntervalBoundary b1, b2;
		b1 = new IntervalBoundary();
		b2 = new IntervalBoundary();

		b1.hitRecord = intersectCylinder(r, t_near);
		b1.t = t_near;
		
		if(r.direction.dot(b1.hitRecord.normal) < 0){
			b1.type = BoundaryType.START;
		}else{
			b1.type = BoundaryType.END;
		}

		b2.hitRecord = intersectCylinder(r, t_far);
		b2.t = t_far;
		
		if(r.direction.dot(b2.hitRecord.normal) < 0){
			b2.type = BoundaryType.START;
		}else{
			b2.type = BoundaryType.END;
		}

		boundaries.add(b1);
		boundaries.add(b2);

		return boundaries;
	}

	private HitRecord intersectCylinder(Ray r, float t) {
		Vector3f position = new Vector3f(r.direction);
		position.scaleAdd(t, r.origin);

		Vector3f normal = new Vector3f(position);
		normal.sub(center);
		normal.z = 0;
		normal.normalize();

		// wIn is incident direction; convention is that it points away from
		// surface
		Vector3f wIn = new Vector3f(r.direction);
		wIn.negate();

		return new HitRecord(t, position, normal, wIn, this, material, 0.f, 0.f);
	}

	@Override
	public AxisAlignedBoundingBox getBoundingBox() {
		// TODO Auto-generated method stub
		return null;
	}
}
