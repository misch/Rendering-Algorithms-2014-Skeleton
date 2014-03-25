package rt.intersectables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Intersectable;
import rt.Ray;

public class BSPAccelerator implements Intersectable {

	private int numberOfObjects = 0;
	private final int MAX_DEPTH;
	private BSPNode rootNode;
	
	public BSPAccelerator(Aggregate aggregate){
		
		ArrayList<Intersectable> objects = new ArrayList<Intersectable>();
		
		Iterator<Intersectable> it = aggregate.iterator();
		while(it.hasNext()){
			objects.add(it.next());
		}
		this.numberOfObjects = objects.size();
//		this.MAX_DEPTH = (int)(8+1.3 * Math.log(numberOfObjects));
		this.MAX_DEPTH = 1;
		
		Vector3f splitNormal = new Vector3f(1,0,0);
		rootNode = new BSPNode(aggregate.getBoundingBox(), splitNormal,0);

		construct(rootNode, objects);
	}
	
	BSPNode construct(BSPNode node, ArrayList<Intersectable> objects){
		if (node.depth > MAX_DEPTH || objects.size() < 3){
			node.objects = objects;
			return node;
		}
		
		
		AxisAlignedBoundingBox bb = node.getBoundingBox();
		AxisAlignedBoundingBox above, below;
		
		above = new AxisAlignedBoundingBox(node.getSplitPosition().x, bb.getXMax(), bb.getYMin(), bb.getYMax(), bb.getZMin(), bb.getZMax());
		below = new AxisAlignedBoundingBox(bb.getXMin(), node.getSplitPosition().x, bb.getYMin(), bb.getYMax(), bb.getZMin(), bb.getZMax());
		
		ArrayList<Intersectable> objsAbove = new ArrayList<Intersectable>();
		ArrayList<Intersectable> objsBelow = new ArrayList<Intersectable>();
		
		for (Intersectable obj: objects){
			if(obj.getBoundingBox().intersect(above))
				objsAbove.add(obj);
			if(obj.getBoundingBox().intersect(below))
				objsBelow.add(obj);
		}
		

		node.above = construct(new BSPNode(above, new Vector3f(1,0,0),node.depth+1), objsAbove);
		node.below = construct(new BSPNode(below, new Vector3f(1,0,0),node.depth+1), objsBelow);

		return node;
	}
	
	@Override
	public HitRecord intersect(Ray r) {
		Stack<BSPNode> maiföckinstäck = new Stack<>();
		maiföckinstäck.push(rootNode);
		float nearestT = Float.POSITIVE_INFINITY;
		HitRecord nearest = null;
		while(!maiföckinstäck.isEmpty()) {
			BSPNode node = maiföckinstäck.pop();
			if (node.isLeaf()){
				for (Intersectable i: node.objects){
					HitRecord tmp = i.intersect(r);
					if (tmp != null && tmp.t < nearestT && tmp.t > 0) {
						nearest = tmp;
						nearestT = tmp.t;
					}
				}
			} else {
				if (node.above.getBoundingBox().intersect(r) != null)
					maiföckinstäck.push(node.above);
				if (node.below.getBoundingBox().intersect(r) != null)
					maiföckinstäck.push(node.below);
			}
		}
		return nearest;
	}
	
	@Override
	public AxisAlignedBoundingBox getBoundingBox() {
		return this.rootNode.getBoundingBox();
	}
	
	class StackItem{
		private BSPNode node;
		private float tMin, tMax;
		
		public StackItem(BSPNode node, float tMin, float tMax){
			this.node = node;
			this.tMin = tMin;
			this.tMax = tMax;
		}
	}
}
