package rt.intersectables;

import javax.vecmath.*;

import rt.HitRecord;
import rt.Intersectable;
import rt.Ray;

/**
 * Defines a triangle by referring back to a {@link Mesh}
 * and its vertex and index arrays. 
 */
public class MeshTriangle implements Intersectable {

	private Mesh mesh;
	private int index;
	
	/**
	 * Make a triangle.
	 * 
	 * @param mesh the mesh storing the vertex and index arrays
	 * @param index the index of the triangle in the mesh
	 */
	public MeshTriangle(Mesh mesh, int index)
	{
		this.mesh = mesh;
		this.index = index;		
	}
	
	public HitRecord intersect(Ray r)
	{
		float vertices[] = mesh.vertices;
		
		// Get three vertex indices for triangle
		int v0 = mesh.indices[index*3];
		int v1 = mesh.indices[index*3+1];
		int v2 = mesh.indices[index*3+2];
		
		// Access x,y,z coordinates for each vertex
		Vector3f a = new Vector3f(vertices[v0*3], vertices[v0*3+1], vertices[v0*3+2]);
		Vector3f b = new Vector3f(vertices[v1*3],vertices[v1*3+1],vertices[v1*3+2]);
		Vector3f c = new Vector3f(vertices[v2*3], vertices[v2*3+1], vertices[v2*3+2]);
		
		// Acces normals
		float normals[] = mesh.normals;
		Vector3f n0 = new Vector3f(normals[v0*3], normals[v0*3+1], normals[v0*3+2]);
		Vector3f n1 = new Vector3f(normals[v1*3], normals[v1*3+1], normals[v1*3+2]);
		Vector3f n2 = new Vector3f(normals[v2*3], normals[v2*3+1], normals[v2*3+2]);
		
		Vector3f bToa = new Vector3f(a);
		bToa.sub(b);
		
		Vector3f cToa = new Vector3f(a);
		cToa.sub(c);
		
		Matrix3f matrix = new Matrix3f();
		matrix.setColumn(0, bToa);
		matrix.setColumn(1, cToa);
		matrix.setColumn(2, r.direction);
		
		Vector3f rightHand = new Vector3f(a);
		rightHand.sub(r.origin);

		float[] cramer = applyCramersRule(matrix, rightHand);
		float beta = cramer[0];
		float gamma = cramer[1];
		float t = cramer[2];
		
		if (beta+gamma > 0 && beta+gamma < 1  && beta > 0 && gamma > 0 && t > 0){
			Vector3f position = new Vector3f(r.direction);
			position.scaleAdd(t, r.origin);
			
			Vector3f interpolatedNormal = new Vector3f();
			Vector3f weighted_n0 = new Vector3f(n0);
			weighted_n0.normalize();
			weighted_n0.scale(1-beta-gamma);
			
			Vector3f weighted_n1 = new Vector3f(n1);
			weighted_n1.normalize();
			weighted_n1.scale(beta);
			
			Vector3f weighted_n2 = new Vector3f(n2);
			weighted_n2.normalize();
			weighted_n2.scale(gamma);
			
			interpolatedNormal.add(weighted_n0, weighted_n1);
			interpolatedNormal.add(weighted_n2);
			interpolatedNormal.normalize();
			
			// wIn is incident direction; convention is that it points away from surface
			Vector3f wIn = new Vector3f(r.direction);
			wIn.negate();
			wIn.normalize();
			
			return new HitRecord(t,position,interpolatedNormal,wIn,this,mesh.material,0.f,0.f);
		}
		else{
			return null;
		}
		
	}

	/*
	 * Apply Cramer's rule
	 */
	private float[] applyCramersRule(Matrix3f matrix, Vector3f rightHand) {
		float detA = matrix.determinant();
		
		Matrix3f matrix0 = new Matrix3f(matrix);
		matrix0.setColumn(0, rightHand);
		float detA0 = matrix0.determinant();
		
		Matrix3f matrix1 = new Matrix3f(matrix);
		matrix1.setColumn(1, rightHand);
		float detA1 = matrix1.determinant();
		
		Matrix3f matrix2 = new Matrix3f(matrix);
		matrix2.setColumn(2, rightHand);
		float detA2 = matrix2.determinant();
		
		float beta = detA0/detA;
		float gamma = detA1/detA;
		float t = detA2/detA;
		
		float[] cramer = {beta,gamma,t};
		return cramer;
	}

	@Override
	public AxisAlignedBoundingBox getBoundingBox() {
		float vertices[] = mesh.vertices;
		
		// Access the triangle vertices as follows (same for the normals):		
		// 1. Get three vertex indices for triangle
		int v0 = mesh.indices[index*3];
		int v1 = mesh.indices[index*3+1];
		int v2 = mesh.indices[index*3+2];
		
		// 2. Access x,y,z coordinates for each vertex
		float x0 = vertices[v0*3];
		float x1 = vertices[v1*3];
		float x2 = vertices[v2*3];
		float y0 = vertices[v0*3+1];
		float y1 = vertices[v1*3+1];
		float y2 = vertices[v2*3+1];
		float z0 = vertices[v0*3+2];
		float z1 = vertices[v1*3+2];
		float z2 = vertices[v2*3+2];
		
		float xMin = (float)Math.min(Math.min(x0, x1),x2);
		float yMin = (float)Math.min(Math.min(y0, y1),y2);
		float zMin = (float)Math.min(Math.min(z0, z1),z2);
		float xMax = (float)Math.max(Math.max(x0, x1),x2);
		float yMax = (float)Math.max(Math.max(y0, y1),y2);
		float zMax = (float)Math.max(Math.max(z0, z1),z2);
		
		return new AxisAlignedBoundingBox(xMin,xMax,yMin,yMax,zMin,zMax);
		
	}
	
	
	
}