package rt.intersectables;

import javax.vecmath.Point3f;

import rt.HitRecord;
import rt.Intersectable;
import rt.Ray;

public class AxisAlignedBoundingBox implements Intersectable {

	private float xMin,xMax,yMin,yMax,zMin,zMax;
	
	public AxisAlignedBoundingBox(float xMin, float xMax, float yMin, float yMax, float zMin, float zMax){
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
		this.zMin = zMin;
		this.zMax = zMax;
	}
	
	@Override
	public HitRecord intersect(Ray r) {
		
		// TODO Auto-generated method stub
		return null;
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

}
