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

	public BSPAccelerator(Aggregate aggregate) {

		ArrayList<Intersectable> objects = new ArrayList<Intersectable>();

		Iterator<Intersectable> it = aggregate.iterator();
		while (it.hasNext()) {
			objects.add(it.next());
		}
		this.numberOfObjects = objects.size();
//		 this.MAX_DEPTH = (int) (8 + 1.3 * Math.log(numberOfObjects));
		this.MAX_DEPTH = 2;

		Vector3f splitNormal = new Vector3f(1, 0, 0);
		rootNode = new BSPNode(aggregate.getBoundingBox(), splitNormal, 0);
		System.out.println("Constructing BSP-tree...");
		construct(rootNode, objects);
		System.out.println("Tree constructed.");
	}

	BSPNode construct(BSPNode node, ArrayList<Intersectable> objects) {
		
		if (node.depth > MAX_DEPTH || objects.size() < 3) {
			node.objects = objects;
			return node;
		}

		AxisAlignedBoundingBox bb = node.getBoundingBox();
		AxisAlignedBoundingBox above, below;

		above = new AxisAlignedBoundingBox(node.getSplitPosition().x,
				bb.getXMax(), bb.getYMin(), bb.getYMax(), bb.getZMin(),
				bb.getZMax());
		below = new AxisAlignedBoundingBox(bb.getXMin(),
				node.getSplitPosition().x, bb.getYMin(), bb.getYMax(),
				bb.getZMin(), bb.getZMax());

		ArrayList<Intersectable> objsAbove = new ArrayList<Intersectable>();
		ArrayList<Intersectable> objsBelow = new ArrayList<Intersectable>();

		for (Intersectable obj : objects) {
			if (obj.getBoundingBox().intersect(above))
				objsAbove.add(obj);
			if (obj.getBoundingBox().intersect(below))
				objsBelow.add(obj);
		}

		node.above = construct(new BSPNode(above, new Vector3f(1, 0, 0),
				node.depth + 1), objsAbove);
		node.below = construct(new BSPNode(below, new Vector3f(1, 0, 0),
				node.depth + 1), objsBelow);

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
		// System.out.println(tValues[0] + "," + tValues[1]);
		float tMin = tValues[0], tMax = tValues[1];
//		int iteration = 0;
		while (node != null) {
//			iteration++;
			// System.out.println(iteration);
			if (isect < tMin)
				break;

			if (!node.isLeaf()) {
				// intersection ray and split-plane
				float tSplit = (node.getSplitPosition().x - r.origin.x)
						/ r.direction.x;

				// order children
				BSPNode first, second;
				if (r.origin.x < node.getSplitPosition().x) {
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
						|| (Math.abs(tSplit) < 1e-3f && first.getBoundingBox()
								.intersect(r) != null)) {
					node = first;
				} else if (tSplit < tMin // -- case 2: only second child is hit
						|| (Math.abs(tSplit) < 1e-3f && second.getBoundingBox()
								.intersect(r) != null)) {
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
					tValues[0] = newItem.tMin;
					tValues[1] = newItem.tMax;
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
