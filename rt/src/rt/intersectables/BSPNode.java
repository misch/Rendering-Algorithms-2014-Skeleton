package rt.intersectables;

import java.util.List;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import rt.Intersectable;

public class BSPNode{

	private Point3f splitPosition;
	private Vector3f splitNormal;
	public BSPNode above, below;
	public List<Intersectable> objects;
	private AxisAlignedBoundingBox boundingBox;
	public int depth;
	
	public BSPNode(Point3f planePosition, Vector3f axisNormal, List objects, AxisAlignedBoundingBox boundingBox){
		
	}
	
	public BSPNode(AxisAlignedBoundingBox boundingBox, Vector3f splitNormal, int depth) {
		this.boundingBox = boundingBox;
		this.splitNormal = splitNormal;
		this.splitPosition = boundingBox.getMiddle();
		this.depth = depth;
	}

	public AxisAlignedBoundingBox getBoundingBox(){
		return this.boundingBox;
	}
	
	public Point3f getSplitPosition(){
		return this.splitPosition;
	}
	public boolean isLeaf(){
		return this.objects != null;
	}
}
