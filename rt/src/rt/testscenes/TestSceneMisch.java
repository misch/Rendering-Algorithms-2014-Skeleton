package rt.testscenes;

import java.io.IOException;
import java.util.Random;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import rt.LightList;
import rt.Material;
import rt.ObjReader;
import rt.Scene;
import rt.Spectrum;
import rt.cameras.PinholeCamera;
import rt.films.BoxFilterFilm;
import rt.integrators.BDPathTracingIntegratorFactory;
import rt.integrators.WhittedIntegratorFactory;
import rt.intersectables.BSPAccelerator;
import rt.intersectables.Bumpy;
import rt.intersectables.CSGDodecahedron;
import rt.intersectables.CSGInstance;
import rt.intersectables.CSGNode;
import rt.intersectables.CSGPlane;
import rt.intersectables.CSGSolid;
import rt.intersectables.CSGTwoSidedInfiniteCone;
import rt.intersectables.CSGUnitCylinder;
import rt.intersectables.Instance;
import rt.intersectables.IntersectableList;
import rt.intersectables.Mesh;
import rt.intersectables.Rectangle;
import rt.intersectables.Sphere;
import rt.lightsources.AreaLight;
import rt.materials.Diffuse;
import rt.materials.Reflective;
import rt.materials.Refractive;
import rt.materials.Textured;
import rt.samplers.RandomSamplerFactory;
import rt.tonemappers.ClampTonemapper;

public class TestSceneMisch extends Scene {
	
	public TestSceneMisch()
	{
		// Output file name
		outputFilename = new String("../output/testscenes/Misch_whitted");

		// Image width and height in pixels
		width = 640;
		height = 360;
		
		// Specify pixel sampler to be used
		samplerFactory = new RandomSamplerFactory();
//		samplerFactory = new OneSamplerFactory();
	
		// Number of samples per pixel
		SPP = 8;

		outputFilename = outputFilename + " " + String.format("%d", SPP) + "SPP";
		outputFilename = outputFilename + " " + String.format("%d", width) + "x";
		outputFilename = outputFilename + String.format("%d", height);

		// Specify which camera, film, and tonemapper to use
		Vector3f eye = new Vector3f(-20.f, 10.f, -17.f);
		Vector3f lookAt = new Vector3f(10,7,-15);
		Vector3f up = new Vector3f(0.f, 1.f, 0.f);
		float fov = 60.f;
		float aspect = 16.f/9.f;
		camera = new PinholeCamera(eye, lookAt, up, fov, aspect, width, height);
		film = new BoxFilterFilm(width, height);
		tonemapper = new ClampTonemapper();
		
		// Specify which integrator and sampler to use
		integratorFactory = new WhittedIntegratorFactory();
//		integratorFactory = new PointLightIntegratorFactory();
//		integratorFactory = new AreaLightIntegratorFactory();
//		integratorFactory = new BDPathTracingIntegratorFactory(this);
//		integratorFactory = new PathTracingIntegratorFactory();
//		integratorFactory = new DebugIntegratorFactory();
 
		// TODO
//		Material refractive = new Refractive(1.3f);
//		Material refractive = new Diffuse(new Spectrum(1.f, 1.f, 1.f));
//		Material refractive = new rt.materials.Blinn(new Spectrum(1f, 1f, 1f), new Spectrum(.4f, .4f, .4f), 50.f);
//		Material refractive = new Reflective(new Spectrum(1,1,1));
		Material refractive = new Refractive(1.1f);
		
		// Make a conical "bowl" by subtracting cross-sections of two cones
		CSGSolid outerCone = coneCrossSection(60.f, refractive);
		// Make an inner cone and subtract it
		Matrix4f trafo = new Matrix4f();
		trafo.setIdentity();
		trafo.setTranslation(new Vector3f(0.f, 0.f, 0.25f));
		CSGInstance innerCone = new CSGInstance(outerCone, trafo);		
		CSGSolid doubleCone = new CSGNode(outerCone, innerCone, CSGNode.OperationType.SUBTRACT);
		
		Mesh mesh;
		try {

			mesh = ObjReader.read("../obj/disco.obj", 1.f);
		} catch (IOException e) {
			System.out.printf("Could not read .obj file\n");
			return;
		}
		
		mesh.material = new Reflective();
		BSPAccelerator acc = new BSPAccelerator(mesh);
		
		Matrix4f t = new Matrix4f();
		t.setIdentity();
		
		// Instance one
		t.setScale(2.5f);
		t.setTranslation(new Vector3f(0, 19, 0));
		
		Instance instanceHacke = new Instance(acc, t);
		
		
		// Place it in the scene
		Matrix4f rot = new Matrix4f();
		rot.setIdentity();
		rot.rotX(-(float)Math.PI/2.f);
		Matrix4f trans = new Matrix4f();
		trans.setIdentity();
		trans.setTranslation(new Vector3f(-1.5f,-1.5f, 0f));
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
		trafo.rotZ((float)Math.toRadians(-20));
		trafo.mul(scale);
		// Rotate it "up"
		rot = new Matrix4f();
		rot.setIdentity();
		rot.rotX(-(float)Math.PI/2.f);		
		rot.mul(trafo);
		
		// Place in scene by translating
		trans = new Matrix4f();
		trans.setIdentity();
		trans.setTranslation(new Vector3f(1.5f, -1.5f, 1.f));
		trans.mul(rot);
		soap = new CSGInstance(soap, trans);
		
		// ground plane
		Rectangle groundPlane = new Rectangle(new Point3f(25,0,50),new Vector3f(0,0,-100), new Vector3f(-50,0,0));
//		groundPlane.material = new Textured("../textures/grass_big.jpg");
		groundPlane.material = new Diffuse();
		
		// left
		Rectangle leftPlane = new Rectangle(new Point3f(-25,0,50), new Vector3f(0,0,-100), new Vector3f(0,20,0));
		leftPlane.material = new Textured("../textures/wand_long.jpg");
		Bumpy bumpyLeftPlane = new Bumpy(leftPlane,"../textures/normal_wand_long.jpg");
		
		// back
		Rectangle backPlane = new Rectangle(new Point3f(-25,0,-50), new Vector3f(50,0,0), new Vector3f(0,20,0));
		backPlane.material = new Textured("../textures/wand_short.jpg");
		Bumpy bumpyBackPlane = new Bumpy(backPlane,"../textures/normal_wand_short.jpg");
		
		// right
		Rectangle rightPlane = new Rectangle(new Point3f(25,0,-50), new Vector3f(0,0,100), new Vector3f(0,20,0));
		rightPlane.material = new Textured("../textures/wand_long.jpg");
		Bumpy bumpyRightPlane = new Bumpy(rightPlane,"../textures/normal_wand_long.jpg");
		
		// top
		Rectangle topPlane = new Rectangle(new Point3f(25,20,50), new Vector3f(-50,0,0), new Vector3f(0,0,-100));
		topPlane.material = new Diffuse();
		
		
		// bar
		
		Material alu = new rt.materials.Glossy( 2.f, new Spectrum(1.3f, 1.02f, 0.64f),  new Spectrum(7.48f, 6.55f, 5.28f));
		Rectangle frontBar = new Rectangle(new Point3f(25,0,-20), new Vector3f(0,6,0), new Vector3f(-30,0,0));
		frontBar.material = alu;
		Bumpy bumpyFrontBar = new Bumpy(frontBar, "../textures/bump1.png");
		Rectangle leftBar = new Rectangle(new Point3f(-5,0,-20), new Vector3f(0,6,0), new Vector3f(0,0,-15));
		leftBar.material = alu;
		Bumpy bumpyLeftBar = new Bumpy(leftBar, "../textures/bump1.png");
		Rectangle top1 = new Rectangle(new Point3f(25,6,-20), new Vector3f(0,0,-5), new Vector3f(-30,0,0));
		top1.material = alu;
		Rectangle top2 = new Rectangle(new Point3f(-5,6,-20), new Vector3f(5,0,0), new Vector3f(0,0,-15));
		top2.material = alu;
		
		Mesh mesh2;
		try
		{
			
			mesh2 = ObjReader.read("../obj/teapot.obj", 1.f);
		} catch(IOException e) 
		{
			System.out.printf("Could not read .obj file\n");
			return;
		}
		mesh2.material = new Refractive(1.3f);
		t = new Matrix4f();
		t.setIdentity();
		BSPAccelerator acc2 = new BSPAccelerator(mesh2);
		// Instance one
		t.setScale(2);
		t.setTranslation(new Vector3f(5, 7, -22));
		Instance instanceTeapot = new Instance(acc2, t);
		
		try
		{
			
			mesh = ObjReader.read("../obj/teapot.obj", 1.f);
		} catch(IOException e) 
		{
			System.out.printf("Could not read .obj file\n");
			return;
		}
		
		Spectrum ext = new Spectrum(3.f, 2.88f, 1.846f);
		mesh.material = new rt.materials.Glossy( 8.f, new Spectrum(0.25f, 0.306f, 1.426f), ext);
		
		t = new Matrix4f();
		t.setIdentity();
		t.setScale(2);
		t.setTranslation(new Vector3f(-3, 7, -19));
		rot = new Matrix4f();
		rot.setIdentity();
		rot.rotY(-(float)Math.PI/3.f);	
		rot.rotX((float)Math.PI/3.f);
		t.mul(rot);
		acc = new BSPAccelerator(mesh);
		Instance instanceTeapot2 = new Instance(acc, t);
		
		// Collect objects in intersectable list
		IntersectableList intersectableList = new IntersectableList();
		intersectableList.add(groundPlane);
		intersectableList.add(bumpyLeftPlane);
		intersectableList.add(bumpyBackPlane);
		intersectableList.add(bumpyRightPlane);
		intersectableList.add(topPlane);
		intersectableList.add(instanceHacke);
		intersectableList.add(bumpyFrontBar);
		intersectableList.add(bumpyLeftBar);
		intersectableList.add(top1);
		intersectableList.add(top2);
		intersectableList.add(instanceTeapot);
		intersectableList.add(instanceTeapot2);
		
		lightList = new LightList();
		Random rand = new Random();
		
		for (int i = 0; i > -41; i -= 10){
			addLightCube(new Vector3f(-25,10,i), new Vector3f(-23,12,i-2), new Spectrum(800,800,600-600*rand.nextFloat()), intersectableList);
			addLightCube(new Vector3f(23,10,i), new Vector3f(25,12,i-2), new Spectrum(800,800,600-600*rand.nextFloat()), intersectableList);
		}

//		AreaLight light = new AreaLight(new Vector3f(10,13,-15), new Vector3f(0,2,-5), new Vector3f(-5,0,0), new Spectrum(5000));
		// Set the root node for the scene
		root = intersectableList;
		
//		lightList.add(light);
//		intersectableList.add(light);
		// light sources
//		addLights();
	}
	
	private void addLightCube(Vector3f botFrontLeft, Vector3f topBackRight, Spectrum emission, IntersectableList intersectableList){
		float bot = botFrontLeft.y, front = botFrontLeft.z, left = botFrontLeft.x,
			  top = topBackRight.y, back = topBackRight.z, right = topBackRight.x;
		Spectrum spec = new Spectrum(emission);
		
		AreaLight frontLight = new AreaLight(new Vector3f(botFrontLeft), new Vector3f(right-left,0,0), new Vector3f(0,top-bot,0), spec);
		AreaLight leftLight = new AreaLight(new Vector3f(botFrontLeft), new Vector3f(0,top-bot,0), new Vector3f(0,0,back-front), spec);
		AreaLight botLight = new AreaLight(new Vector3f(botFrontLeft), new Vector3f(0,0,back-front), new Vector3f(right-left,0,0), spec);
		AreaLight rightLight = new AreaLight(new Vector3f(topBackRight), new Vector3f(0,0,front-back), new Vector3f(0,bot-top,0), spec);
		AreaLight backLight = new AreaLight(new Vector3f(topBackRight), new Vector3f(0,bot-top,0), new Vector3f(left-right,0,0), spec);
		AreaLight topLight = new AreaLight(new Vector3f(topBackRight), new Vector3f(left-right,0,0), new Vector3f(0,0,front-back), spec);
		
		
		lightList.add(frontLight);
		lightList.add(rightLight);
		lightList.add(backLight);
		lightList.add(leftLight);
		lightList.add(topLight);
		lightList.add(botLight);
		
		intersectableList.add(botLight);
		intersectableList.add(topLight);
		intersectableList.add(backLight);
		intersectableList.add(leftLight);
		intersectableList.add(rightLight);
		intersectableList.add(frontLight);
		
	}
	
	/**
	 * Make a "horizontal" cross section through a cone with apex angle {@param a}.
	 * The bottom plane is at z=0, the top at z=1. The radius of the bottom circle 
	 * in the cross section is one (the top circle is bigger depending on the apex angle).
	 * @param a apex angle for the cone
	 */
	private CSGSolid coneCrossSection(float a, Material material)
	{
		// Makes a two-sided infinite cone with apex angle 90 degrees
		CSGTwoSidedInfiniteCone doubleCone = new CSGTwoSidedInfiniteCone(material);
//		CSGSolid doubleCone = new CSGCylinder(material);
		// Scaling factor along the cone axis corresponding to apex angle
		float s = (float)Math.tan((90-a/2)/180.f*(float)Math.PI);
		
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
		CSGNode out = new CSGNode(scaledCone, new CSGPlane(new Vector3f(0.f, 0.f, -1.f), 0.f, material), CSGNode.OperationType.INTERSECT);
		out = new CSGNode(out, new CSGPlane(new Vector3f(0.f, 0.f, 1.f), -1.f, material), CSGNode.OperationType.INTERSECT);
		
		return out;
	}
	
	public void finish()
	{
		if(integratorFactory instanceof BDPathTracingIntegratorFactory)
		{
			((BDPathTracingIntegratorFactory)integratorFactory).writeLightImage("../output/testscenes/lightimage");
			((BDPathTracingIntegratorFactory)integratorFactory).addLightImage(film);
		}
	}

}
