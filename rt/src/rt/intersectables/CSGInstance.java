package rt.intersectables;

import java.util.ArrayList;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Intersectable;
import rt.Ray;

public class CSGInstance extends CSGSolid {

	private CSGSolid object;
	
	/* object to world transformation */
	private Matrix4f M; 
	
	/* world to object transformation */
	private Matrix4f M_inv;
	
	/* world to object transposed - used for back transformation of normal vector */
	private Matrix4f M_inv_transp;
	
	public CSGInstance(CSGSolid object, Matrix4f M){
		this.object  = object;
		this.M = new Matrix4f(M);
		this.M_inv = new Matrix4f(M);
		M_inv.invert();
		this.M_inv_transp = new Matrix4f(M_inv);
		M_inv_transp.transpose();
	}
		
	private HitRecord transformBack(HitRecord hitRecord){
		Point3f position = new Point3f(hitRecord.position);
		M.transform(position);
		
		Vector3f normal = new Vector3f(hitRecord.normal);
		M_inv_transp.transform(normal);
		normal.normalize();
		
		Vector3f direction = new Vector3f(hitRecord.w);
		M.transform(direction);
		direction.normalize();
			
		return new HitRecord(hitRecord.t,new Vector3f(position),normal,direction,hitRecord.intersectable,hitRecord.material,0.f,0.f);
	}
	
	private Ray transformRay(Ray r){
		Point3f originObjCoords = new Point3f(r.origin);
		M_inv.transform(originObjCoords);
		
		Vector3f directionObjCoords = new Vector3f(r.direction);
		M_inv.transform(directionObjCoords);
		
		return new Ray(new Vector3f(originObjCoords),directionObjCoords);
	}

	@Override
	ArrayList<IntervalBoundary> getIntervalBoundaries(Ray r) {
		Ray rayObjCoords = transformRay(r);
		
		ArrayList<IntervalBoundary> boundary = object.getIntervalBoundaries(rayObjCoords);

		for(IntervalBoundary bound : boundary){
			bound.hitRecord = transformBack(bound.hitRecord);				
		}
	
		return boundary;
	}
}

