package rt.lightsources;

import java.util.Random;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.LightGeometry;
import rt.Ray;
import rt.Spectrum;
import rt.intersectables.AxisAlignedBoundingBox;
import rt.intersectables.Rectangle;
import rt.materials.AreaLightMaterial;

/**
 * Implements an area light using a {@link rt.materials.AreaLightMaterial}.
 */
public class AreaLight implements LightGeometry {

	Vector3f position;
	float area;
	AreaLightMaterial areaLightMaterial;
	Random rand;
	Vector3f vec1, vec2, normal;
	Rectangle rectangle;
	
	/*
	 * Assuming that the orientation of the area light is given by
	 * normal = vec1 x vec2
	 */
	public AreaLight(Vector3f position, Vector3f vec1, Vector3f vec2, Spectrum emission)
	{
		this.position = new Vector3f(position);
		this.rand = new Random();
		this.vec1 = new Vector3f(vec1);
		this.vec2 = new Vector3f(vec2);
		Vector3f normal = new Vector3f();
		normal.cross(vec1, vec2);
		this.area = normal.length();
		normal.normalize();
		this.normal = new Vector3f(normal);
		this.rectangle = new Rectangle(new Point3f(position), new Vector3f(vec1), new Vector3f(vec2));
		this.areaLightMaterial = new AreaLightMaterial(emission, area);
	}
	
	/**
	 * A ray may hit an area light source.
	 */
	public HitRecord intersect(Ray r) {
		HitRecord h = rectangle.intersect(r);
		if (h == null)
		return null;
		h.intersectable = this;
		h.material = this.areaLightMaterial;
		return h;
	}

	/**
	 * Sample a point on the light geometry.
	 */
	public HitRecord sample(float[] s) {
		HitRecord hitRecord = new HitRecord();
		
		Vector3f randomPosition = new Vector3f(position);
		
		Vector3f dir1 = new Vector3f(vec1);
		dir1.scale(s[0]);
		
		Vector3f dir2 = new Vector3f(vec2);
		dir2.scale(s[1]);
		
		randomPosition.add(dir1);
		randomPosition.add(dir2);
		
		hitRecord.position = randomPosition;
		
		hitRecord.material = areaLightMaterial;
		hitRecord.intersectable = this;

		Vector3f normal = new Vector3f();
		normal.cross(vec1, vec2);
		normal.normalize();
		hitRecord.normal = normal;
		
		hitRecord.p = 1f/area;
		return hitRecord;
	}

	@Override
	public AxisAlignedBoundingBox getBoundingBox() {
		return null;
	}
	
	public Vector3f getNormal(){
		return new Vector3f(this.normal);
	}

}
