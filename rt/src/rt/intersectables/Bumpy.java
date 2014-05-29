package rt.intersectables;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.vecmath.Matrix3f;
import javax.vecmath.Point2f;
import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Intersectable;
import rt.Material;
import rt.MathUtil;
import rt.Ray;

public class Bumpy implements Intersectable {

	Intersectable intersectable;
	BufferedImage bumpMap;
	public Material material;
	
	public Bumpy(Intersectable intersectable, String bumpMapFilePath){
		this.intersectable = intersectable;

		try {
			bumpMap = ImageIO.read(new File(bumpMapFilePath));
		} catch (IOException e) {
			System.out.println("Could not load bump map.");
		}
	}
	@Override
	public HitRecord intersect(Ray r) {
		HitRecord actualHit = this.intersectable.intersect(r);
		if (actualHit == null){
			return null;
		}
		
		Vector3f bumpNormal = new Vector3f(MathUtil.bilinearInterpolation(actualHit.u, actualHit.v, bumpMap));
		Matrix3f toLocalFrame = actualHit.getLocalFrameTransformation();
		
		toLocalFrame.transform(bumpNormal);
		bumpNormal.normalize();
		
		actualHit.normal = bumpNormal;
		
		return actualHit;
	}

	@Override
	public AxisAlignedBoundingBox getBoundingBox() {
		return intersectable.getBoundingBox();
	}
}