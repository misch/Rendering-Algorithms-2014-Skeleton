package rt.intersectables;

import java.util.ArrayList;

import javax.vecmath.Vector3f;

import rt.Material;
import rt.Ray;
import rt.Spectrum;
import rt.materials.Diffuse;

public class CSGSphere extends CSGSolid {

	Vector3f center;
	float radius;
	public Material material;
	
	/**
	 * Makes a CSG sphere.
	 * 
	 * @param center the sphere center
	 * @param radius radius of the sphere
	 */
	public CSGSphere(Vector3f center, float radius){
		this.center = center;
		this.radius = radius;
		
		material = new Diffuse(new Spectrum(1.f, 1.f, 1.f));
	}
	
	@Override
	ArrayList<IntervalBoundary> getIntervalBoundaries(Ray r) {
		// TODO Auto-generated method stub
		return null;
	}

}
