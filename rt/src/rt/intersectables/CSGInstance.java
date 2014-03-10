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
			
		return new HitRecord(hitRecord.t,new Vector3f(position),normal,hitRecord.w,hitRecord.intersectable,hitRecord.material,0.f,0.f);
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
		
//		HitRecord hitRecord = object.intersect(rayObjCoords);
		ArrayList<IntervalBoundary> boundary = object.getIntervalBoundaries(rayObjCoords);

		
		if (boundary.isEmpty()){ // if no intersection occurred
			return boundary;
		}
		else{
			HitRecord hitRecordWorld1 = transformBack(boundary.get(0).hitRecord);
			HitRecord hitRecordWorld2 = transformBack(boundary.get(1).hitRecord);
			
			IntervalBoundary b1 = new IntervalBoundary();
			b1.hitRecord = hitRecordWorld1;
			b1.t = hitRecordWorld1.t;
			b1.type = boundary.get(0).type;
			
			IntervalBoundary b2 = new IntervalBoundary();
			b2.hitRecord = hitRecordWorld1;
			b2.t = hitRecordWorld1.t;
			b2.type = boundary.get(1).type;
			
			boundary = new ArrayList<IntervalBoundary>();
			boundary.add(b1); boundary.add(b2);
			return boundary;
		}
	}

}
