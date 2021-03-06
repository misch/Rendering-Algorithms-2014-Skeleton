package rt.cameras;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import rt.Camera;
import rt.Ray;
import rt.StaticVecmath;

public class PinholeCamera implements Camera {

	private Matrix4f m;
	private Vector3f eye;
	private Vector3f lookAt;
	
	public PinholeCamera(Vector3f eye, Vector3f lookAt, Vector3f up, float fov, float aspect, float width, float height){
		this.eye = eye;
		this.lookAt = new Vector3f(lookAt);
		// Camera to world matrix c
		Vector3f wAxis = new Vector3f(eye);
		wAxis.sub(lookAt);
		wAxis.normalize();
		
		Vector3f uAxis = new Vector3f();
		uAxis.cross(up,wAxis);
		uAxis.normalize();
		
		Vector3f vAxis = new Vector3f();
		vAxis.cross(wAxis,uAxis);

		Matrix4f c = new Matrix4f();
		c.setColumn(0, new Vector4f(uAxis));
		c.setColumn(1, new Vector4f(vAxis));
		c.setColumn(2, new Vector4f(wAxis));
		c.setColumn(3, new Vector4f(eye));
		c.m33 = 1;
		
		Matrix4f p = new Matrix4f();
		float t = (float)Math.tan(Math.toRadians(fov)/2);
		float r = aspect*t;
		p.m00 = 2*r/width;
		p.m02 = r;
		p.m11 = 2*t/height;
		p.m12 = t;
		p.m22 = 1;
		p.m33 = 1;
		
		c.mul(p);
		m = c;
	}
	
	@Override
	public Ray makeWorldSpaceRay(int i, int j, float[] sample) {
		// Make point on image plane in viewport coordinates, that is range [0,width-1] x [0,height-1]
		// The assumption is that pixel [i,j] is the square [i,i+1] x [j,j+1] in viewport coordinates
		Vector4f d = new Vector4f((float)i+sample[0],(float)j+sample[1],-1.f,1.f);
		
		// Transform it back to world coordinates
		m.transform(d);
		
		// Make ray consisting of origin and direction in world coordinates
		Vector3f dir = new Vector3f();
		dir.sub(new Vector3f(d.x, d.y, d.z), eye);
		Ray r = new Ray(new Vector3f(eye), dir);
		return r;
	}
	
	public int[] getPixel(Ray worldRayTowardsCam){
		Vector3f dir = new Vector3f(worldRayTowardsCam.direction);
		Vector4f origin = new Vector4f(worldRayTowardsCam.origin.x,worldRayTowardsCam.origin.y,worldRayTowardsCam.origin.z,1);
		
		Matrix4f m_inv = new Matrix4f(m);
		m_inv.invert();
		
		m_inv.transform(dir);
		m_inv.transform(origin);
		
		Vector3f combined = new Vector3f(dir);
		combined.add(new Vector3f(origin.x,origin.y,origin.z));
		
		combined.scale(-(1f/combined.z));
		int[] pixels = {(int)combined.x,(int)combined.y};
		return pixels;
		
	}

	@Override
	public Vector3f getLookAtDirection() {
		return new Vector3f(this.lookAt);
	}

}
