package rt.intersectables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Intersectable;
import rt.Ray;

public class BSPAccelerator implements Intersectable {

	private int numberOfObjects = 0;
	private final int MAX_DEPTH;
	private final int MIN_OBJECTS = 5;
	private BSPNode rootNode;

	public BSPAccelerator(Aggregate aggregate) {

		ArrayList<Intersectable> objects = new ArrayList<Intersectable>();

		Iterator<Intersectable> it = aggregate.iterator();
		while (it.hasNext()) {
			objects.add(it.next());
		}
		this.numberOfObjects = objects.size();
		System.out.println("Number of objects: " + numberOfObjects);
		this.MAX_DEPTH = (int) (8 + 1.3 * Math.log(numberOfObjects));
//		this.MAX_DEPTH = 7;
		
		Vector3f splitNormal = new Vector3f(1, 0, 0);
		rootNode = new BSPNode(aggregate.getBoundingBox(), splitNormal, 0);
		System.out.println("Constructing BSP-tree...");
		construct(rootNode, objects);
		System.out.println("Tree constructed.");
	}

	BSPNode construct(BSPNode node, ArrayList<Intersectable> objects) {
		
		if (node.depth > MAX_DEPTH || objects.size() < MIN_OBJECTS) {
			node.objects = objects;
			return node;
		}

		Vector3f nextNormal = getNextSplitNormal(node.getSplitNormal());
		
		AxisAlignedBoundingBox[] boxes = node.splitNode();
		AxisAlignedBoundingBox above = boxes[0];
		AxisAlignedBoundingBox below = boxes[1];
		
		ArrayList<Intersectable> objsAbove = new ArrayList<>();
		ArrayList<Intersectable> objsBelow = new ArrayList<>();

		for (Intersectable obj : objects) {
			if (obj.getBoundingBox().intersect(above))
				objsAbove.add(obj);
			if (obj.getBoundingBox().intersect(below))
				objsBelow.add(obj);
		}
		node.above = construct(new BSPNode(above, nextNormal, node.depth + 1), objsAbove);
		node.below = construct(new BSPNode(below, nextNormal,node.depth + 1), objsBelow);
		return node;
	}

	@Override
	public HitRecord intersect(Ray r) {
		BSPNode node = rootNode;

		Stack<StackItem> stack = new Stack<>();
		float isect = Float.POSITIVE_INFINITY;
		float[] tValues = rootNode.getBoundingBox().intersect(r);
		HitRecord nearest = null;

		if (tValues == null) { // If bounding box of root is not intersected,
								// then there will be no intersections at all.
			return null;
		}
		float tMin = tValues[0], tMax = tValues[1];

		while (node != null) {
			if (isect < tMin)
				break;

			if (!node.isLeaf()) {
				// intersection ray and split-plane
				Vector3f splitNormal = node.getSplitNormal();
				Point3f splitPosition = node.getSplitPosition();
				
				Vector3f aSubE = new Vector3f();
				aSubE.sub(node.getSplitPosition(), r.origin);
				
				float tSplit = getCoord(aSubE,splitNormal)/getCoord(r.direction,splitNormal);

				// order children
				BSPNode first, second;
				if (getCoord(r.origin,splitNormal) < getCoord(splitPosition, splitNormal)) {
					first = node.below;
					second = node.above;
				} else {
					first = node.above;
					second = node.below;
				}

				// process children

				// -- case 1: only 1st child is hit
				if (tSplit > tMax
						|| tSplit < 0
						|| (Math.abs(tSplit) < 1e-3f && first.getBoundingBox().intersect(r) != null)) {
					node = first;
				} else if (tSplit < tMin // -- case 2: only second child is hit
						|| (Math.abs(tSplit) < 1e-3f && second.getBoundingBox().intersect(r) != null)) {
					node = second;
				} else { // -- case 3: both children are hit
					node = first;
					StackItem item = new StackItem(second, tSplit, tMax);
					stack.push(item);
					tMax = tSplit;
				}
			} else {
				// Get nearest hit of objects in leaf
				for (Intersectable obj : node.objects) {

					HitRecord objHit = obj.intersect(r);

					if (objHit != null && objHit.t < isect && objHit.t > 0) {
						isect = objHit.t;
						nearest = objHit;
					}
				}
				if (!stack.isEmpty()) {
					StackItem newItem = stack.pop();
					node = newItem.node;
					tMin = newItem.tMin;
					tMax = newItem.tMax;
				} else
					// No intersection at all
					break;
			}
		}
		return nearest;
	}

	@Override
	public AxisAlignedBoundingBox getBoundingBox() {
		return this.rootNode.getBoundingBox();
	}
	
	private float getCoord(Tuple3f tuple, Vector3f splitNormal){
		return (new Vector3f(tuple).dot(splitNormal));
	}
	
	private Vector3f getNextSplitNormal(Vector3f currentNormal){
		Vector3f nextNormal = new Vector3f();
		if(currentNormal.x == 1f){
			nextNormal = new Vector3f(0,1,0);
		}
		if(currentNormal.y == 1f){
			nextNormal = new Vector3f(0,0,1);
		}
		if(currentNormal.z == 1f){
			nextNormal = new Vector3f(1,0,0);
		}
		return nextNormal;
	}

	class StackItem {
		BSPNode node;
		float tMin, tMax;

		public StackItem(BSPNode node, float tMin, float tMax) {
			this.node = node;
			this.tMin = tMin;
			this.tMax = tMax;
		}
	}
}
