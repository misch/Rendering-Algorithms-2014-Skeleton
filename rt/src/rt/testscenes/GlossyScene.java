package rt.testscenes;

import java.io.IOException;

import javax.vecmath.Matrix4f;
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
public class GlossyScene extends Scene {

	public GlossyScene()
	{
		// Output file name
		outputFilename = new String("..//output//testscenes/Glossy");
		
		// Image width and height in pixels
		width = 512;
		height = 512;
		
		// Number of samples per pixel
		SPP = 1;
		
		// Specify which camera, film, and tonemapper to use
		Vector3f eye = new Vector3f(0.f, 0f, 3.f);
		Vector3f lookAt = new Vector3f(0.f, 0.3f, 0.f);
		Vector3f up = new Vector3f(0.f, 1.f, 0.f);
		float fov = 60.f;
		float aspect = 1.f;
		camera = new PinholeCamera(eye, lookAt, up, fov, aspect, width, height);
		film = new BoxFilterFilm(width, height);
		tonemapper = new ClampTonemapper();
		
		// Specify which integrator and sampler to use
		integratorFactory = new WhittedIntegratorFactory();
		samplerFactory = new OneSamplerFactory();

		// Ground plane
		CSGPlane groundPlane = new CSGPlane(new Vector3f(0.f, 1.f, 0.f), 1.f);
		
		// Add objects
		Mesh mesh;
		try
		{		
			mesh = ObjReader.read("..\\obj\\teapot.obj", 1.f);
		} catch(IOException e) 
		{
			System.out.printf("Could not read .obj file\n");
			return;
		}
		Matrix4f t = new Matrix4f();
		t.setIdentity();
		
		// Instance one
		t.setScale(1.5f);
		Matrix4f r = new Matrix4f();
		r.rotX(0.607f);
		t.mul(r);
		r.rotY(-3*(float)Math.PI/4);
		t.mul(r);
		
		//gold
		Spectrum ext = new Spectrum(3.f, 2.88f, 1.846f);
	//	mesh.material = new rt.materials.Glossy( 8.f, new Spectrum(0.25f, 0.306f, 1.426f), ext);
		//copper
		ext = new Spectrum(3.23f, 2.6f, 2.5f);
		mesh.material = new rt.materials.Glossy( 8.f, new Spectrum(0.27f, 0.82f, 1.16f), ext);
		//aluminium
		ext = new Spectrum(7.48f, 6.55f, 5.28f);
	//	mesh.material = new rt.materials.Glossy( 8.f, new Spectrum(1.3f, 1.02f, 0.64f), ext);
		//silver
		ext = new Spectrum(3.88f, 3.45f, 2.56f);
	//	mesh.material = new rt.materials.Glossy( 8.f, new Spectrum(0.131f, 0.12f, 0.144f), ext);
		
		Instance instance = new Instance(mesh, t);
		
		IntersectableList intersectableList = new IntersectableList();
		intersectableList.add(groundPlane);
		intersectableList.add(instance);
		
		root = intersectableList;
		
		// Light sources
		LightGeometry pl1 = new PointLight(new Vector3f(.5f, 1.5f, 2.f), new Spectrum(10.f, 10.f, 10.f));
		LightGeometry pl2 = new PointLight(new Vector3f(-.75f, .25f, 2.f), new Spectrum(7.f, 7.f, 7.f));
		lightList = new LightList();
		lightList.add(pl1);
		lightList.add(pl2);
	}
}
