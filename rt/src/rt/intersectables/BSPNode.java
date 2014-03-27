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

	public Vector3f getSplitNormal() {
		return this.splitNormal;
	}
	
	public AxisAlignedBoundingBox[] splitNode(){
		AxisAlignedBoundingBox above, below;
		
		float 	xBelowMin = boundingBox.getXMin(), 
				xBelowMax = boundingBox.getXMax(), 
				yBelowMin = boundingBox.getYMin(), 
				yBelowMax = boundingBox.getYMax(),
				zBelowMin = boundingBox.getZMin(), 
				zBelowMax = boundingBox.getZMax(),
				
				xAboveMin = boundingBox.getXMin(), 
				xAboveMax = boundingBox.getXMax(), 
				yAboveMin = boundingBox.getYMin(), 
				yAboveMax = boundingBox.getYMax(), 
				zAboveMin = boundingBox.getZMin(), 
				zAboveMax = boundingBox.getZMax();
		
		if (splitNormal.x == 1){
			xBelowMax = splitPosition.x;
			xAboveMin = splitPosition.x;
		}
		
		if(splitNormal.y == 1){
			yBelowMax = splitPosition.y;
			yAboveMin = splitPosition.y;
		}
		
		if(splitNormal.z == 1){
			zBelowMax = splitPosition.z;
			zAboveMin = splitPosition.z;
		}
		
		above = new AxisAlignedBoundingBox(xAboveMin, xAboveMax, yAboveMin, yAboveMax, zAboveMin, zAboveMax);
		below = new AxisAlignedBoundingBox(xBelowMin, xBelowMax, yBelowMin, yBelowMax, zBelowMin, zBelowMax);
		AxisAlignedBoundingBox[] boxes = {above,below};
		return boxes;
	}
}
