package rt.intersectables;

import javax.vecmath.Matrix4f;

import rt.HitRecord;
import rt.Intersectable;
import rt.Ray;

public class Instance implements Intersectable {

	private Intersectable object;
	private Matrix4f objToWorld;
	
	public Instance(Intersectable object, Matrix4f objectToWorldTransform){
		this.object  = object;
		this.objToWorld = objectToWorldTransform;
	}
	
	@Override
	public HitRecord intersect(Ray r) {
		// TODO Auto-generated method stub
		return null;
	}

}
