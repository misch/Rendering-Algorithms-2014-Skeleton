package rt.testscenes;

import java.io.IOException;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import rt.LightGeometry;
import rt.LightList;
import rt.Material;
import rt.ObjReader;
import rt.Scene;
import rt.Spectrum;
import rt.cameras.PinholeCamera;
import rt.films.BoxFilterFilm;
import rt.integrators.*;
import rt.intersectables.*;
import rt.lightsources.PointLight;
import rt.materials.Diffuse;
import rt.materials.Reflective;
import rt.materials.Refractive;
import rt.materials.Textured;
import rt.materials.XYZGrid;
import rt.samplers.OneSamplerFactory;
//import rt.samplers.OneSamplerFactory;
import rt.samplers.RandomSamplerFactory;
import rt.tonemappers.ClampTonemapper;

public class Textures extends Scene {

	public Textures() {
		// Output file name
		outputFilename = new String("../output/testscenes/Textures");

		// Image width and height in pixels
		width = 640;
		height = 360;

		// Specify pixel sampler to be used
		samplerFactory = new RandomSamplerFactory();
		// samplerFactory = new OneSamplerFactory();

		// Number of samples per pixel
		SPP = 2;

		outputFilename = outputFilename + " " + String.format("%d", SPP)
				+ "SPP";
		outputFilename = outputFilename + " " + String.format("%d", width)
				+ "x";
		outputFilename = outputFilename + String.format("%d", height);

		// Specify which camera, film, and tonemapper to use
		Vector3f eye = new Vector3f(0.f, 3.f, 3.f);
		Vector3f lookAt = new Vector3f(0.f, -.5f, 0.f);
		Vector3f up = new Vector3f(0.f, 1.f, 0.f);
		float fov = 60.f;
		float aspect = 16.f / 9.f;
		camera = new PinholeCamera(eye, lookAt, up, fov, aspect, width, height);
		film = new BoxFilterFilm(width, height);
		tonemapper = new ClampTonemapper();

		// Specify which integrator and sampler to use
//		integratorFactory = new WhittedIntegratorFactory();
		 integratorFactory = new PointLightIntegratorFactory();
//		 integratorFactory = new BDPathTracingIntegratorFactory(this);
		// integratorFactory = new PathTracingIntegratorFactory();

		Material refractive = new Refractive(1.1f);

		// Make a conical "bowl" by subtracting cross-sections of two cones
		CSGSolid outerCone = coneCrossSection(60.f, refractive);
		// Make an inner cone and subtract it
		Matrix4f trafo = new Matrix4f();
		trafo.setIdentity();
		trafo.setTranslation(new Vector3f(0.f, 0.f, 0.25f));
		CSGInstance innerCone = new CSGInstance(outerCone, trafo);
		CSGSolid doubleCone = new CSGNode(outerCone, innerCone,
				CSGNode.OperationType.SUBTRACT);

		// Add objects
		Mesh mesh;
		try {

			mesh = ObjReader.read("../obj/disco.obj", 1.f);
		} catch (IOException e) {
			System.out.printf("Could not read .obj file\n");
			return;
		}
		
//		mesh.material = new Textured("../textures/test.jpg", new Diffuse(new Spectrum(1)));
		mesh.material = new Diffuse();
		BSPAccelerator acc = new BSPAccelerator(mesh);
		Sphere thing = new Sphere();
		thing.material = new Diffuse(new Spectrum(1,0.8f,0.2f));
		Bumpy sphere = new Bumpy(thing, "../textures/bump1.png");

		// Place it in the scene
		Matrix4f rot = new Matrix4f();
		rot.setIdentity();
		rot.rotX(-(float) Math.PI / 2.f);
		Matrix4f trans = new Matrix4f();
		trans.setIdentity();
		trans.setTranslation(new Vector3f(-1.5f, -1.5f, 0f));
		trans.mul(rot);
		doubleCone = new CSGInstance(doubleCone, trans);

		// Something like a"soap bar"
		Material yellow = new Diffuse(new Spectrum(1.f, 0.8f, 0.2f));
		CSGSolid soap = new CSGUnitCylinder(yellow);
		CSGSolid cap = new CSGTwoSidedInfiniteCone(yellow);
		// Smoothen the edges
		trans.setIdentity();
		trans.m23 = -0.8f;
		CSGSolid cap1 = new CSGInstance(cap, trans);
		soap = new CSGNode(soap, cap1, CSGNode.OperationType.INTERSECT);
		trans.m23 = 1.8f;
		CSGSolid cap2 = new CSGInstance(cap, trans);
		soap = new CSGNode(soap, cap2, CSGNode.OperationType.INTERSECT);

		// Transform it and place it in the scene
		Matrix4f scale = new Matrix4f();
		// Make it elliptical and rotate a bit around the cylinder axis
		scale.setIdentity();
		scale.m11 = 0.5f;
		scale.m22 = 0.5f;
		trafo = new Matrix4f();
		trafo.rotZ((float) Math.toRadians(-20));
		trafo.mul(scale);
		// Rotate it "up"
		rot = new Matrix4f();
		rot.setIdentity();
		rot.rotX(-(float) Math.PI / 2.f);
		rot.mul(trafo);

		// Place in scene by translating
		trans = new Matrix4f();
		trans.setIdentity();
		trans.setTranslation(new Vector3f(1.5f, -1.5f, 1.f));
		trans.mul(rot);
		soap = new CSGInstance(soap, trans);

		// Ground and back plane
		XYZGrid grid = new XYZGrid(new Spectrum(0.2f, 0.f, 0.f), new Spectrum(
				1.f, 1.f, 1.f), 0.1f, new Vector3f(0.f, 0.3f, 0.f));
		Rectangle groundPlane = new Rectangle(new Point3f(-10, -1.5f, -10),
				new Vector3f(0, 0, 20), new Vector3f(20, 0, 0));
		groundPlane.material = new Textured("../textures/grass.jpg");
		Bumpy bumpyGroundPlane = new Bumpy(groundPlane, "../textures/bump4.jpg");
		Plane backPlane = new Plane(new Vector3f(0.f, 0.f, 1.f), 3.15f);
		backPlane.material = grid;

		// Collect objects in intersectable list
		IntersectableList intersectableList = new IntersectableList();
		intersectableList.add(acc);
//		intersectableList.add(soap);
		intersectableList.add(bumpyGroundPlane);
		intersectableList.add(backPlane);

		// Set the root node for the scene
		root = intersectableList;

		// Light sources
		Vector3f lightPos = new Vector3f(eye);
		lightPos.add(new Vector3f(-1.f, 0.f, 0.f));
		LightGeometry pointLight1 = new PointLight(lightPos, new Spectrum(14.f,
				14.f, 14.f));
		lightPos.add(new Vector3f(2.f, 0.f, 0.f));
		LightGeometry pointLight2 = new PointLight(lightPos, new Spectrum(14.f,
				14.f, 14.f));
		LightGeometry pointLight3 = new PointLight(new Vector3f(0.f, 3.f, 1.f),
				new Spectrum(44.f, 44.f, 44.f));
		
		LightGeometry pointLightAbove = new PointLight(new Vector3f(0,3,0), new Spectrum(50,50,50));
		lightList = new LightList();
//		lightList.add(pointLight1);
//		lightList.add(pointLight2);
//		lightList.add(pointLight3);
		lightList.add(pointLightAbove);
	}

	/**
	 * Make a "horizontal" cross section through a cone with apex angle
	 * 
	 * @param a
	 *            . The bottom plane is at z=0, the top at z=1. The radius of
	 *            the bottom circle in the cross section is one (the top circle
	 *            is bigger depending on the apex angle).
	 * @param a
	 *            apex angle for the cone
	 */
	private CSGSolid coneCrossSection(float a, Material material) {
		// Makes a two-sided infinite cone with apex angle 90 degrees
		CSGTwoSidedInfiniteCone doubleCone = new CSGTwoSidedInfiniteCone(
				material);
		// CSGSolid doubleCone = new CSGCylinder(material);
		// Scaling factor along the cone axis corresponding to apex angle
		float s = (float) Math.tan((90 - a / 2) / 180.f * (float) Math.PI);

		// Scale and translate cone
		Matrix4f scale = new Matrix4f();
		scale.setIdentity();
		scale.m22 = s;
		Matrix4f trans = new Matrix4f();
		trans.setIdentity();
		trans.setTranslation(new Vector3f(0.f, 0.f, -s));
		trans.mul(scale);
		CSGInstance scaledCone = new CSGInstance(doubleCone, trans);

		// Cut off at z=0 and z=1
		CSGNode out = new CSGNode(scaledCone, new CSGPlane(new Vector3f(0.f,
				0.f, -1.f), 0.f, material), CSGNode.OperationType.INTERSECT);
		out = new CSGNode(out, new CSGPlane(new Vector3f(0.f, 0.f, 1.f), -1.f,
				material), CSGNode.OperationType.INTERSECT);

		return out;
	}

	public void finish() {
		if (integratorFactory instanceof BDPathTracingIntegratorFactory) {
			((BDPathTracingIntegratorFactory) integratorFactory)
					.writeLightImage("../output/testscenes/lightimage");
			((BDPathTracingIntegratorFactory) integratorFactory)
					.addLightImage(film);
		}
	}

}
