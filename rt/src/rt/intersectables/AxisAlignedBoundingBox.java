package rt.intersectables;

import javax.vecmath.Point3f;

import rt.HitRecord;
import rt.Intersectable;
import rt.Ray;

public class AxisAlignedBoundingBox{

	private float xMin,xMax,yMin,yMax,zMin,zMax;
	
	public AxisAlignedBoundingBox(float xMin, float xMax, float yMin, float yMax, float zMin, float zMax){
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
		this.zMin = zMin;
		this.zMax = zMax;
	}
	
	public AxisAlignedBoundingBox(Point3f min, Point3f max){
		this(min.x, max.x, min.y, max.y, min.z, max.z);
	}
	
	public float getXMin(){
		return xMin;
	}
	
	public float getXMax(){
		return xMax;
	}
	
	public float getYMin(){
		return yMin;
	}
	
	public float getYMax(){
		return yMax;
	}
	
	public float getZMin(){
		return zMin;
	}
	
	public float getZMax(){
		return zMax;
	}
	
	public Point3f getMiddle(){
		
		float middleX = (xMin + xMax)/2;
		float middleY = (yMin + yMax)/2;
		float middleZ = (zMin + zMax)/2;
		
		return new Point3f(middleX,middleY,middleZ);
	}

	public boolean intersect(AxisAlignedBoundingBox otherBoundingBox){
		boolean potentialIntersectionX = false, 
				potentialIntersectionY = false, 
				potentialIntersectionZ = false;
		
		if (xMax > otherBoundingBox.xMin || xMin < otherBoundingBox.xMax)
			potentialIntersectionX = true;
		else
			return false;
		
		if (yMax > otherBoundingBox.yMin || yMin < otherBoundingBox.yMax)
			potentialIntersectionY = true;
		else
			return false;
		
		if (zMax > otherBoundingBox.zMin || zMin < otherBoundingBox.zMax)
			potentialIntersectionZ = true;
		else
			return false;
		
		if(potentialIntersectionX && potentialIntersectionY && potentialIntersectionZ)
			return true;
		
		return false;
	}
	
	/*
	 * Returns tMin and tMax
	 * @return float[2] [tMin, tMax]
	 */
	public float[] intersect(Ray r){
		float t_xMin, t_xMax, t_yMin, t_yMax ,t_zMin, t_zMax;
		if(r.direction.x >= 0){
			t_xMin = (xMin - r.origin.x)/r.direction.x;
			t_xMax = (xMax - r.origin.x)/r.direction.x;
		}
		else{
			t_xMin = (xMax - r.origin.x)/r.direction.x;
			t_xMax = (xMin - r.origin.x)/r.direction.x;
		}
		
		if(r.direction.y >= 0){
			t_yMin = (yMin - r.origin.y)/r.direction.y;
			t_yMax = (yMax - r.origin.y)/r.direction.y;
		}
		else{
			t_yMin = (yMax - r.origin.y)/r.direction.y;
			t_yMax = (yMin - r.origin.y)/r.direction.y;
		}
		
		if ((t_xMin > t_yMax) || t_yMin > t_xMax){
			return null;
		}
		
		
		if(r.direction.z >= 0){
			t_zMin = (zMin - r.origin.z)/r.direction.z;
			t_zMax = (zMax - r.origin.z)/r.direction.z;
		}
		else{
			t_zMin = (zMax - r.origin.z)/r.direction.z;
			t_zMax = (zMin - r.origin.z)/r.direction.z;
		}
		
		float t_min = (float)Math.max(t_xMin,t_yMin);
		float t_max = (float)Math.min(t_xMax, t_yMax);
		if (( t_min > t_zMax) || (t_zMin > t_max)){
			return null;
		}
		
		float[] t = {(float)Math.max(t_min, t_zMin),(float)Math.min(t_max, t_zMax)};
		return t;
	}
}
