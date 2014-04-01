package rt;

import javax.vecmath.*;

/**
 * A ray represented by an origin and a direction.
 */
public class Ray {

	public Vector3f origin;
	public Vector3f direction;
	public int depth;
	public final float EPSILON = 1e-3f;
	
	public Ray(Vector3f origin, Vector3f direction, int depth, boolean epsilonRay){
		this.origin = new Vector3f(origin);
		this.direction = new Vector3f(direction);
		this.depth = depth;
		
		if (epsilonRay){
			Vector3f scaledDir = new Vector3f(this.direction);
			scaledDir.scale(EPSILON);
			this.origin.add(scaledDir);
		}
	}
	
	public Ray(Vector3f origin, Vector3f direction, int depth)
	{
		this(origin, direction, depth, false);
	}
	
	public Ray(Vector3f origin, Vector3f direction){
		this(origin,direction,0);
	}
}
