package rt.testscenes;

import javax.vecmath.Vector3f;

import rt.LightGeometry;
import rt.LightList;
import rt.Scene;
import rt.Spectrum;
import rt.cameras.PinholeCamera;
import rt.films.BoxFilterFilm;
import rt.integrators.DebugIntegratorFactory;
import rt.integrators.PointLightIntegratorFactory;
import rt.intersectables.CSGCylinder;
import rt.intersectables.CSGPlane;
import rt.intersectables.CSGTwoSidedInfiniteCone;
import rt.intersectables.CSGUnitCylinder;
import rt.intersectables.IntersectableList;
import rt.lightsources.PointLight;
import rt.samplers.OneSamplerFactory;
import rt.tonemappers.ClampTonemapper;

/**
 * Simple scene using a Blinn material.
 */
public class CSGPrimitives extends Scene {

	public CSGPrimitives()
	{
		// Output file name
		outputFilename = new String("../output/testscenes/CSGPrimitive");
		
		// Image width and height in pixels
		width = 512;
		height = 512;
		
		// Number of samples per pixel
		SPP = 1;
		
		// Specify which camera, film, and tonemapper to use
		Vector3f eye = new Vector3f(0.f, 3.f, 3.5f);
		Vector3f lookAt = new Vector3f(0.f, 0.f, 0.5f);
		Vector3f up = new Vector3f(0.f, 0.f, 1.f);
		float fov = 60.f;
		float aspect = 1.f;
		camera = new PinholeCamera(eye, lookAt, up, fov, aspect, width, height);
		film = new BoxFilterFilm(width, height);
		tonemapper = new ClampTonemapper();
		
		// Specify which integrator and sampler to use
		integratorFactory = new PointLightIntegratorFactory();
//		integratorFactory = new DebugIntegratorFactory();
		samplerFactory = new OneSamplerFactory();

		// Ground plane
		CSGPlane groundPlane = new CSGPlane(new Vector3f(0.f, 1.f, 0.f), 1.f);
		
		// CSG Primitive
		CSGCylinder cylinder = new CSGCylinder(new Vector3f(0,0,0), 0.5f);
		CSGTwoSidedInfiniteCone cone = new CSGTwoSidedInfiniteCone(new Vector3f(0,0,0));
		CSGUnitCylinder unitCylinder = new CSGUnitCylinder();
		
		IntersectableList intersectableList = new IntersectableList();
//		intersectableList.add(groundPlane);
		intersectableList.add(unitCylinder);
		
		root = intersectableList;
		
		// Light sources
		LightGeometry pl1 = new PointLight(new Vector3f(0.f, 3.f, 3.5f), new Spectrum(10.f, 10.f, 10.f));
//		LightGeometry pl1 = new PointLight(new Vector3f(0.f, 0.f, 1.f), new Spectrum(1.f, 1.f, 1.f));
		LightGeometry pl2 = new PointLight(new Vector3f(-.75f, 2.f, .75f), new Spectrum(1.f, 1.f, 1.f));
		lightList = new LightList();
		lightList.add(pl1);
		lightList.add(pl2);
	}
}
