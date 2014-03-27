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
		Point3f centerDistance = this.getMiddle();
		centerDistance.sub(otherBoundingBox.getMiddle());
		centerDistance.absolute();
		
		Point3f size1 = new Point3f(xMax,yMax,zMax);
		size1.sub(new Point3f(xMin,yMin,zMin));
		size1.scale(0.5f);
		size1.absolute();
		
		Point3f size2 = new Point3f(otherBoundingBox.xMax,otherBoundingBox.yMax,otherBoundingBox.zMax);
		size2.sub(new Point3f(otherBoundingBox.xMin,otherBoundingBox.yMin,otherBoundingBox.zMin));
		size2.scale(0.5f);
		size2.absolute();
		
		Point3f sizeBetween = new Point3f();
		sizeBetween.add(size1, size2);
		
		if (sizeBetween.x < centerDistance.x || sizeBetween.y < centerDistance.y || sizeBetween.z < centerDistance.z)
			return false;
		return true;
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
