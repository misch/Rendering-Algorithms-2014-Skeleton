package rt.intersectables;

import java.util.ArrayList;

import javax.vecmath.Vector3f;

import rt.Material;
import rt.Ray;
import rt.Spectrum;
import rt.intersectables.CSGNode.OperationType;
import rt.materials.Diffuse;

public class CSGUnitCylinder extends CSGSolid {

	private CSGNode root;
	
	public CSGUnitCylinder(Material material){
		CSGCylinder infinite = new CSGCylinder();
		infinite.material = material;
		
		CSGPlane upperPlane = new CSGPlane(new Vector3f(0,0,1),-1.f, material);
		CSGPlane lowerPlane = new CSGPlane(new Vector3f(0,0,-1),0, material);
		
		CSGNode node = new CSGNode(infinite, upperPlane, OperationType.INTERSECT);
		root = new CSGNode(node,lowerPlane,CSGNode.OperationType.INTERSECT);
	}
	
	public CSGUnitCylinder(){
		this(new Diffuse(new Spectrum(1.f, 1.f, 1.f)));
	}
	
	@Override
	ArrayList<IntervalBoundary> getIntervalBoundaries(Ray r) {
		return root.getIntervalBoundaries(r);
	}

}
