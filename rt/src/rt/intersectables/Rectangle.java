package rt.intersectables;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Intersectable;
import rt.Ray;

public class Rectangle extends Plane implements Intersectable {

	private Point3f position;
	private Vector3f vec1, vec2, normal;

	public Rectangle(Point3f position, Vector3f vec1, Vector3f vec2) {
		super(getNormal(vec1, vec2), getPlaneDistance(position, vec1, vec2));
		this.position = new Point3f(position);
		this.vec1 = new Vector3f(vec1);
		this.vec2 = new Vector3f(vec2);
	}

	public Rectangle(Vector3f position, Vector3f vec1, Vector3f vec2) {
		this(new Point3f(position), vec1, vec2);
	}

	private static Vector3f getNormal(Vector3f vec1, Vector3f vec2) {
		Vector3f normal = new Vector3f();
		normal.cross(vec1, vec2);
		normal.normalize();
		return normal;
	}
	
	private static float getPlaneDistance(Point3f position, Vector3f vec1, Vector3f vec2) {
		Vector3f p = new Vector3f(position);
		Vector3f n = getNormal(vec1,vec2);
		float distance = (n.dot(p));
		return -distance;
	}
	
	@Override
	public HitRecord intersect(Ray r) {
		HitRecord h = super.intersect(r);
		if (h == null)
			return null;
		
		Vector3f posToHit = new Vector3f();
		posToHit.sub(h.position, this.position);
		
		// Project onto (normalized) edges: If projected length is within edge length,
		// then the hitpoint is inside the rectangle light.
		Vector3f edge1 = new Vector3f(vec1);
		edge1.normalize();
		Vector3f edge2 = new Vector3f(vec2);
		edge2.normalize();
		float length1 = posToHit.dot(edge1);
		float length2 = posToHit.dot(edge2);
		
		boolean isInside = 	   length1 >= 0 && length1 <= vec1.length()
							&& length2 >= 0 && length2 <= vec2.length();
							
		return isInside ? h : null;
	}

}
