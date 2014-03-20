package rt.intersectables;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Intersectable;
import rt.Ray;

public class BSPAccelerator implements Intersectable {

	/*
	 * Method to build BSPTree.
	 */
	BSPNode construct(Aggregate aggregate){
		Vector3f splitPlaneNormal = new Vector3f(1,0,0); // yz-plane
		Point3f splitPos = getSplitPostition(aggregate.getBoundingBox());
		
		
		return null;
	}
	
	@Override
	public HitRecord intersect(Ray r) {
		
		return null;
	}
	
	private Point3f getSplitPostition(AxisAlignedBoundingBox bb) {
		return bb.getMiddle();
	}
}
