package rt.intersectables;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Intersectable;
import rt.Ray;

public class Instance implements Intersectable {

	private Intersectable object;
	private Matrix4f objToWorld; // M on slides
	private Matrix4f worldToObj; // M_inv on slides
	
	public Instance(Intersectable object, Matrix4f objectToWorldTransform){
		this.object  = object;
		this.objToWorld = objectToWorldTransform;
		this.worldToObj = new Matrix4f(objectToWorldTransform);
		worldToObj.invert();
	}
	
	@Override
	public HitRecord intersect(Ray r) {
		Ray rayObjCoords = transformRay(r);
		
		HitRecord hitRecord = object.intersect(rayObjCoords);

		if (hitRecord == null){ // if no intersection occurred
			return null;
		}
		
		// TODO 
		// back transformation of hit record:
		//  - position
		//  - normal
		//  - w (direction towards origin of ray)
		//  - --> return new HitRecord
		
		return null;
	}
	
	private Ray transformRay(Ray r){
		Vector3f originObjCoords = new Vector3f(r.origin);
		worldToObj.transform(originObjCoords);
		
		Vector3f directionObjCoords = new Vector3f(r.direction);
		worldToObj.transform(directionObjCoords);
		
		return new Ray(originObjCoords,directionObjCoords);
	}

}
