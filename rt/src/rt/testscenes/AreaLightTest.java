package rt.testscenes;

import javax.vecmath.Vector3f;

import rt.Intersectable;
import rt.LightGeometry;
import rt.LightList;
import rt.Scene;
import rt.Spectrum;
import rt.cameras.PinholeCamera;
import rt.films.BoxFilterFilm;
import rt.integrators.AreaLightIntegratorFactory;
import rt.intersectables.CSGSphere;
import rt.intersectables.IntersectableList;
import rt.lightsources.AreaLight;
import rt.samplers.OneSamplerFactory;
import rt.tonemappers.ClampTonemapper;

/**
 * Simple scene using a Blinn material.
 */
public class AreaLightTest extends Scene {

	public AreaLightTest()
	{
		// Output file name
		outputFilename = new String("../output/testscenes/AreaLightTest");
		
		// Image width and height in pixels
		width = 512;
		height = 512;
		
		// Number of samples per pixel
		SPP = 1;
		
		// Specify which camera, film, and tonemapper to use
		Vector3f eye = new Vector3f(0.f, -2f, 1.f);
		Vector3f lookAt = new Vector3f(0.f, 0.f, 0.f);
		Vector3f up = new Vector3f(0.f, 1.f, 0.f);
		float fov = 60.f;
		float aspect = 1.f;
		camera = new PinholeCamera(eye, lookAt, up, fov, aspect, width, height);
		film = new BoxFilterFilm(width, height);
		tonemapper = new ClampTonemapper();
		
		// Specify which integrator and sampler to use
		integratorFactory = new AreaLightIntegratorFactory();
//		integratorFactory = new DebugIntegratorFactory();
		samplerFactory = new OneSamplerFactory();


		float[] vertices = {-1,0,0,1,0,0,0,1,0};
		float[] normals = {0,0,1,0,0,1,0,0,1};
		int[] indices = {0,1,2};
		
		/* Comment out to render teapot instead, 
		 * but only after the stupid triangle is actually correctly rendered!*/
		// mesh = new Mesh(vertices, normals, indices);
		Intersectable sphere = new CSGSphere(new Vector3f(0,0,0), 1);
		
		// Instance
		IntersectableList intersectableList = new rt.intersectables.IntersectableList();
		intersectableList.add(sphere);	
		
		root = intersectableList;
		
		// Light sources
		LightGeometry pl1 = new AreaLight(new Vector3f(-2f, 2f, 2f), new Vector3f(4,0,0), new Vector3f(0,0,-4), new Spectrum(5.f, 5.f, 5.f));
//		LightGeometry pl2 = new AreaLight(new Vector3f(-.75f, .75f, 2.f), new Spectrum(5.f, 5.f, 5.f));
		lightList = new LightList();
		lightList.add(pl1);
//		lightList.add(pl2);
	}
}
