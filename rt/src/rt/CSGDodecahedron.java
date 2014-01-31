package rt;

import javax.vecmath.*;

/**
 * A dodecahedron implemented using planes and CSG. The dodecahedron has its center at [0,0,0]. 
 */
public class CSGDodecahedron implements Intersectable {

	CSGNode root;
	
	public CSGDodecahedron()
	{
		Vector3f normal;
		Material diffuseMaterial = new DiffuseMaterial(new Spectrum(1.f, 0.f, 0.f));
		
		// Make CSG planes
		CSGPlane planes[] = new CSGPlane[12];
		
		// Bottom half
		normal  = new Vector3f(0.f, -1.f, 0.f);
		planes[0] = new CSGPlane(normal, -1.f);				
		planes[0].material = diffuseMaterial;

		for(int i=0; i<5; i++)
		{
			float x, y, z;
			float theta;
			
			theta = (float)i * 2.f*(float)Math.PI / 5.f;
			x = (float)(Math.sin(theta) * Math.sin(Math.atan(2.f)));
			z = (float)(Math.cos(theta) * Math.sin(Math.atan(2.f)));
			y = -(float)(Math.cos(Math.atan(2.f)));
			
			normal = new Vector3f(x, y, z);
			planes[i+1] = new CSGPlane(normal, -1.f);				
			planes[i+1].material = diffuseMaterial;
		}
		
		// Top half
		normal = new Vector3f(0.f, 1.f, 0.f);
		planes[6] = new CSGPlane(normal, -1.f);				
		planes[6].material = diffuseMaterial;

		for(int i=0; i<5; i++)
		{
			float x, y, z;
			float theta;
			
			theta = ((float)i+0.5f) * 2.f*(float)Math.PI / 5.f;
			x = (float)(Math.sin(theta) * Math.sin(Math.atan(2.f)));
			z = (float)(Math.cos(theta) * Math.sin(Math.atan(2.f)));
			y = (float)(Math.cos(Math.atan(2.f)));
			
			normal = new Vector3f(x, y, z);
			planes[i+7] = new CSGPlane(normal, -1.f);				
			planes[i+7].material = diffuseMaterial;
		}
				
		// Build CSG tree
		CSGNode nodes[] = new CSGNode[6];
		for(int i=0; i<6; i++)
			nodes[i] = new CSGNode(planes[2*i], planes[2*i+1], CSGNode.OperationType.INTERSECT);
		
		CSGNode nodes2[] = new CSGNode[3];
		for(int i=0; i<3; i++)
			nodes2[i] = new CSGNode(nodes[2*i], nodes[2*i+1], CSGNode.OperationType.INTERSECT);

		CSGNode node3 = new CSGNode(nodes2[0], nodes2[1], CSGNode.OperationType.INTERSECT);

		// Return root
		root = new CSGNode(node3, nodes2[2], CSGNode.OperationType.INTERSECT);
	}

	public HitRecord intersect(Ray r) {
		return root.intersect(r);
	}

	public float surfaceArea() {
		return root.surfaceArea();
	}

	public AxisAlignedBox boundingBox() {
		return root.boundingBox();
	}
		
}
