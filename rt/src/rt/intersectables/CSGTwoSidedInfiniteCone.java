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

	public CSGTwoSidedInfiniteCone(Vector3f center, Material material) {
		this.center = center;
		this.material = material;
	}

	public CSGTwoSidedInfiniteCone(Vector3f center) {
		this(center, new Diffuse(new Spectrum(1.f, 1.f, 1.f)));
	}

	public CSGTwoSidedInfiniteCone(Material material) {
		this(new Vector3f(0, 0, 0), material);
	}

	@Override
	ArrayList<IntervalBoundary> getIntervalBoundaries(Ray r) {
		ArrayList<IntervalBoundary> boundaries = new ArrayList<IntervalBoundary>();

		float t_near, t_far;

		Vector3f directionFlippedZ = new Vector3f(r.direction);
		// Vector2f direction = new Vector2f(r.direction.x,r.direction.y);
		directionFlippedZ.z *= -1;
		float a = r.direction.dot(directionFlippedZ);

		Vector3f centerToRay = new Vector3f();
		centerToRay.sub(r.origin, center);
		Vector3f centerToRayFlippedZ = new Vector3f(centerToRay);
		centerToRayFlippedZ.z *= -1;

		float b = 2 * r.direction.dot(centerToRayFlippedZ);

		float c = centerToRay.dot(centerToRayFlippedZ);

		Point2f roots = MathUtil.midnightFormula(a, b, c);
		if (roots == null)
			return boundaries;

		t_near = Math.min(roots.x, roots.y);
		t_far = Math.max(roots.x, roots.y);

		IntervalBoundary b1, b2, b3, b4;
		b1 = new IntervalBoundary();
		b2 = new IntervalBoundary();
		

		b1.hitRecord = intersectCone(r, t_near);
		b1.t = t_near;
		
		b2.hitRecord = intersectCone(r, t_far);
		b2.t = t_far;
		
		if(r.direction.dot(b1.hitRecord.normal) < 0){
			b1.type = BoundaryType.START;
			b2.type = BoundaryType.END;
		}
		else{
			b1.type = BoundaryType.END;
			b2.type = BoundaryType.START;
		}
		boundaries.add(b1);
		boundaries.add(b2);
		
		if (b1.hitRecord.position.z < 0 && b2.hitRecord.position.z > 0){
			b3 = new IntervalBoundary();
			b4 = new IntervalBoundary();
			b3.t = Float.NEGATIVE_INFINITY;
			b4.t = Float.POSITIVE_INFINITY;
			if (r.direction.dot(new Vector3f(0,0,1)) > 0){
				b3.type = BoundaryType.START;
				b4.type = BoundaryType.END;
			}
			else{
				b3.type = BoundaryType.END;
				b4.type = BoundaryType.START;
			}
			
			boundaries.add(b3);
			boundaries.add(b4);
		} 
		else {
			if (b1.hitRecord.position.z > 0 && b2.hitRecord.position.z < 0){
				b3 = new IntervalBoundary();
				b4 = new IntervalBoundary();
				b3.t = Float.POSITIVE_INFINITY;
				b4.t = Float.NEGATIVE_INFINITY;
				if (r.direction.dot(new Vector3f(0,0,1)) > 0){
					 b3.type = BoundaryType.END;
					 b4.type = BoundaryType.START;
				}
				else{
					b3.type = BoundaryType.START;
					b4.type = BoundaryType.END;
				}
				
				boundaries.add(b3);
				boundaries.add(b4);
			}
		}
		return boundaries;
	}

	private HitRecord intersectCone(Ray r, float t) {
		Vector3f position = new Vector3f(r.direction);
		position.scaleAdd(t, r.origin); // position is the hit point on the surface

		Vector3f normal = new Vector3f(position);
		normal.z *= -1;
		normal.normalize();

		// wIn is incident direction; convention is that it points away from
		// surface
		Vector3f wIn = new Vector3f(r.direction);
		wIn.negate();
		wIn.normalize();

		return new HitRecord(t, position, normal, wIn, this, material, 0.f,
				0.f);
	}
}
