package rt.testscenes;

import java.io.IOException;

import javax.vecmath.Vector3f;

import rt.*;
import rt.cameras.*;
import rt.films.*;
import rt.integrators.*;
import rt.intersectables.*;
import rt.lightsources.*;
import rt.samplers.*;
import rt.tonemappers.*;

/**
 * Simple scene using a Blinn material.
 */
public class AccelerationTest extends Scene {

	public AccelerationTest()
	{
		// Output file name
		outputFilename = new String("../output/testscenes/Acceleration");
		
		// Image width and height in pixels
		width = 512;
		height = 512;
		
		// Number of samples per pixel
		SPP = 1;
		
		// Specify which camera, film, and tonemapper to use
		Vector3f eye = new Vector3f(0.f, 0.f, 5.f);
		Vector3f lookAt = new Vector3f(0.f, 0.f, 0.f);
		Vector3f up = new Vector3f(0.f, 1.f, 0.f);
		float fov = 60.f;
		float aspect = 1.f;
		camera = new PinholeCamera(eye, lookAt, up, fov, aspect, width, height);
		film = new BoxFilterFilm(width, height);
		tonemapper = new ClampTonemapper();
		
		// Specify which integrator and sampler to use
		integratorFactory = new PointLightIntegratorFactory();
//		integratorFactory = new DebugIntegratorFactory();
		samplerFactory = new OneSamplerFactory();

		Mesh mesh;
		try {
			mesh = ObjReader.read("..\\obj\\teapot.obj", 1.f);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		float[] vertices = {-1,0,0,1,0,0,0,1,0};
		float[] normals = {0,0,1,0,0,1,0,0,1};
		int[] indices = {0,1,2};
		
		/* Comment out to render teapot instead, 
		 * but only after the stupid triangle is actually correctly rendered!*/
		// mesh = new Mesh(vertices, normals, indices);
		BSPAccelerator acc = new BSPAccelerator(mesh);
		
		IntersectableList intersectableList = new rt.intersectables.IntersectableList();
		intersectableList.add(acc);	
		
		root = intersectableList;
		
		// Light sources
		LightGeometry pl1 = new PointLight(new Vector3f(.5f, .5f, 2.f), new Spectrum(5.f, 5.f, 5.f));
		LightGeometry pl2 = new PointLight(new Vector3f(-.75f, .75f, 2.f), new Spectrum(5.f, 5.f, 5.f));
		lightList = new LightList();
		lightList.add(pl1);
		lightList.add(pl2);
	}
}
