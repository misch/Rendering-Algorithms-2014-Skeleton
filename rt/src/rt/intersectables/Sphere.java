package rt.intersectables;

import javax.vecmath.Point2f;
import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Intersectable;
import rt.Material;
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
	 * @param center the sphere center
	 * @param radius radius of the sphere
	 */
	public Sphere(Vector3f center, float radius){
		this.center = center;
		this.radius = radius;
		
		material = new Diffuse(new Spectrum(1.f, 1.f, 1.f));
	}
	
	@Override
	public HitRecord intersect(Ray r) {
		float t;
		
		float a = r.direction.lengthSquared();
		
		Vector3f centerToRay = new Vector3f();
		centerToRay.sub(r.origin,center);

		float b = 2*r.direction.dot(centerToRay);
		
		float c = centerToRay.dot(centerToRay) - radius*radius;
		
		float discriminant = b*b - 4*a*c;
		if (discriminant < 0){
			return null;
		}
		else{
				Point2f roots = midnightFormula(a,b,c);
				t = Math.min(roots.x, roots.y);
				
				Vector3f position = new Vector3f(r.direction);
				position.scaleAdd(t, r.origin);
				
				Vector3f normal = new Vector3f(position);
				normal.sub(center);
				normal.normalize();
				
				// wIn is incident direction; convention is that it points away from surface
				Vector3f wIn = new Vector3f(r.direction);
				wIn.negate();
				
				return new HitRecord(t,position,normal,wIn,this,material,0.f,0.f);
		}
	}
	
	private Point2f midnightFormula(float a, float b, float discriminant){
		Point2f point = new Point2f();
		point.x = (float) ((-b + Math.sqrt(discriminant))/(2*a));
		point.y = (float) ((-b - Math.sqrt(discriminant))/(2*a));
		
		return point;
	}

}
