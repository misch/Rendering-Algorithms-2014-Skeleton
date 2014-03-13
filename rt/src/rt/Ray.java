package rt;

import javax.vecmath.*;

/**
 * A ray represented by an origin and a direction.
 */
public class Ray {

	public Vector3f origin;
	public Vector3f direction;
	public int depth;
	
	public Ray(Vector3f origin, Vector3f direction, int depth)
	{
		this.origin = new Vector3f(origin); 
		this.direction = new Vector3f(direction);
		this.depth = depth;
	}
	
	public Ray(Vector3f origin, Vector3f direction){
		this(origin,direction,0);
	}
}
