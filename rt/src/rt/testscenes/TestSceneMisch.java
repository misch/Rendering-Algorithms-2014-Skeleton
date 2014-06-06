package rt.testscenes;

import java.io.IOException;

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
import rt.intersectables.Instance;
import rt.intersectables.IntersectableList;
import rt.intersectables.Mesh;
import rt.intersectables.Rectangle;
import rt.lightsources.AreaLight;
import rt.materials.Diffuse;
import rt.materials.Glossy;
import rt.materials.Reflective;
import rt.materials.Refractive;
import rt.materials.Textured;
import rt.samplers.RandomSamplerFactory;
import rt.tonemappers.ClampTonemapper;

public class TestSceneMisch extends Scene {

	public TestSceneMisch() {
		// Output file name
		outputFilename = new String("../output/testscenes/Misch_whitted");

		// Image width and height in pixels
		 width = 1024;
		 height = 580;

		// Specify pixel sampler to be used
		samplerFactory = new RandomSamplerFactory();
		// samplerFactory = new OneSamplerFactory();

		// Number of samples per pixel
		SPP = 1024;

		 outputFilename = outputFilename + "_" + String.format("%d", SPP) +
		 "SPP";
		 outputFilename = outputFilename + "_" + String.format("%d", width) +
		 "x";
		 outputFilename = outputFilename + String.format("%d", height);

		// Specify which camera, film, and tonemapper to use
		Vector3f eye = new Vector3f(-20.f, 10f, -17.f);
		Vector3f lookAt = new Vector3f(10, 7, -15);
		Vector3f up = new Vector3f(0.f, 1.f, 0.f);
		float fov = 60.f;
		float aspect = 16.f / 9.f;
		camera = new PinholeCamera(eye, lookAt, up, fov, aspect, width, height);
		film = new BoxFilterFilm(width, height);
		tonemapper = new ClampTonemapper();

		// Specify which integrator and sampler to use
		integratorFactory = new WhittedIntegratorFactory();
		// integratorFactory = new PointLightIntegratorFactory();
		// integratorFactory = new AreaLightIntegratorFactory();
		// integratorFactory = new BDPathTracingIntegratorFactory(this);
		// integratorFactory = new PathTracingIntegratorFactory();
		// integratorFactory = new DebugIntegratorFactory();

		Mesh discoMesh;
		try {

			discoMesh = ObjReader.read("../obj/disco.obj", 1.f);
		} catch (IOException e) {
			System.out.printf("Could not read .obj file\n");
			return;
		}

		discoMesh.material = new Reflective();
		BSPAccelerator discoAcc = new BSPAccelerator(discoMesh);

		Matrix4f t = new Matrix4f();
		t.setIdentity();

		// Instance one
		t.setScale(2.5f);
		t.setTranslation(new Vector3f(0, 17, -10));

		Instance discoInstance = new Instance(discoAcc, t);

		// Couch
		Mesh couchMesh;
		try {

			couchMesh = ObjReader.read("../obj/couch2.obj", 1.f);
		} catch (IOException e) {
			System.out.printf("Could not read .obj file\n");
			return;
		}

		couchMesh.material = new Glossy(10, new Spectrum(0.1f, 0.5f, 0.5f),
				new Spectrum(1, 0.3f, 0.3f));
		// couchMesh.material = new Diffuse(new Spectrum(0.8f,0.1f,0.1f));
		BSPAccelerator couchAcc = new BSPAccelerator(couchMesh);

		t = new Matrix4f();
		t.setIdentity();

		// Instance one
		t.setScale(8);
		t.setTranslation(new Vector3f(9, 3, 0));
		Matrix4f rot = new Matrix4f();
		rot.setIdentity();
		rot.rotY(-5 * (float) Math.PI / 6.f);
		t.mul(rot);

		Instance couchInstance = new Instance(couchAcc, t);

		// Chair
		Mesh chairMesh;
		try {

			chairMesh = ObjReader.read("../obj/chair.obj", 1.f);
		} catch (IOException e) {
			System.out.printf("Could not read .obj file\n");
			return;
		}

		chairMesh.material = new Glossy(20, new Spectrum(0.2f, 0.6f, 0.6f),
				new Spectrum(1, 0.3f, 0.3f));
		// chairMesh.material = new Diffuse(new Spectrum(0, 0, 1));
		BSPAccelerator chairAcc = new BSPAccelerator(chairMesh);

		t = new Matrix4f();
		t.setIdentity();

		// Instance one
		t.setScale(4);
		t.setTranslation(new Vector3f(-5, 3, -5));
		rot = new Matrix4f();
		rot.setIdentity();
		rot.rotY(3 * (float) Math.PI / 4.f);
		t.mul(rot);

		Instance chairInstance = new Instance(chairAcc, t);

		// Bottles
		Mesh bottleMesh;
		try {

			bottleMesh = ObjReader.read("../obj/bottles.obj", 1.f);
		} catch (IOException e) {
			System.out.printf("Could not read .obj file\n");
			return;
		}

		bottleMesh.material = new Refractive(1.3f, new Spectrum(0.3f,0.3f,1));
		BSPAccelerator bottleAcc = new BSPAccelerator(bottleMesh);

		t = new Matrix4f();
		t.setIdentity();

		// Instance one
		t.setScale(6);
		t.setTranslation(new Vector3f(19, 10, -34));
		rot = new Matrix4f();
		rot.setIdentity();
		rot.rotY((float) Math.PI / 2f);
		t.mul(rot);

		Instance bottleInstance = new Instance(bottleAcc, t);

		// ground plane
		Rectangle groundPlane = new Rectangle(new Point3f(25, 0, 50),
				new Vector3f(0, 0, -100), new Vector3f(-50, 0, 0));
		groundPlane.material = new Diffuse();
		Bumpy bumpyGroundPlane = new Bumpy(groundPlane,
				"../textures/floor_normal.jpg");

		// left
		Rectangle leftPlane = new Rectangle(new Point3f(-25, 0, 50),
				new Vector3f(0, 0, -100), new Vector3f(0, 20, 0));
		leftPlane.material = new Textured("../textures/wand_long.jpg");
		Bumpy bumpyLeftPlane = new Bumpy(leftPlane,
				"../textures/normal_wand_long.jpg");

		// back
		Rectangle backPlane = new Rectangle(new Point3f(-25, 0, -50),
				new Vector3f(50, 0, 0), new Vector3f(0, 20, 0));
		backPlane.material = new Textured("../textures/wand_short.jpg");
		Bumpy bumpyBackPlane = new Bumpy(backPlane,
				"../textures/normal_wand_short.jpg");

		// rights
		Rectangle rightPlane = new Rectangle(new Point3f(25, 0, -50),
				new Vector3f(0, 0, 100), new Vector3f(0, 20, 0));
		rightPlane.material = new Textured("../textures/wand_long.jpg");
		Bumpy bumpyRightPlane = new Bumpy(rightPlane,
				"../textures/normal_wand_long.jpg");

		// top
		Rectangle topPlane = new Rectangle(new Point3f(25, 20, 50),
				new Vector3f(-50, 0, 0), new Vector3f(0, 0, -100));
		topPlane.material = new Diffuse();

		// bar
		Material alu = new rt.materials.Glossy(20, new Spectrum(1.3f, 1.02f, 0.64f), new Spectrum(7.48f, 6.55f, 5.28f));
		Rectangle frontBar = new Rectangle(new Point3f(25, 0, -20), new Vector3f(0, 6, 0), new Vector3f(-30, 0, 0));
		frontBar.material = alu;
		Bumpy bumpyFrontBar = new Bumpy(frontBar, "../textures/barbump.png");
		Rectangle leftBar = new Rectangle(new Point3f(-5, 0, -20), new Vector3f(0, 6, 0), new Vector3f(0, 0, -15));
		leftBar.material = alu;
		Bumpy bumpyLeftBar = new Bumpy(leftBar, "../textures/barbump2.png");
		Rectangle top1 = new Rectangle(new Point3f(25, 6, -20), new Vector3f(0,0,-5), new Vector3f(-30, 0, 0));
		top1.material = alu;
		Rectangle top2 = new Rectangle(new Point3f(-5, 6, -20), new Vector3f(5,0, 0), new Vector3f(0, 0, -15));
		top2.material = alu;

		Material bottleHolderMaterial = new Refractive(1.45f);
		float holderHeight = 0.5f;
		float holderDepth = 7;
		float holderLength = 12;
		Rectangle bottleHolderTop1 = new Rectangle(new Point3f(), new Vector3f(
				0, 0, -12), new Vector3f(-holderDepth, 0, 0));
		bottleHolderTop1.material = bottleHolderMaterial;
		
		Rectangle bottleHolderTop2 = new Rectangle(new Point3f(0,-holderHeight,0), new Vector3f(
				-holderDepth, 0, 0), new Vector3f(0, 0, -holderLength));
		bottleHolderTop2.material = bottleHolderMaterial;
		
		Rectangle bottleHolderTop3 = new Rectangle(new Point3f(), new Vector3f(-holderDepth,0,0), new Vector3f(0,-holderHeight,0));
		bottleHolderTop3.material = bottleHolderMaterial;
		
		Rectangle bottleHolderTop4 = new Rectangle(new Point3f(0,0,-holderLength), new Vector3f(0,-holderHeight,0), new Vector3f(-holderDepth,0,0));
		bottleHolderTop4.material = bottleHolderMaterial;
		
		Rectangle bottleHolderTop5 = new Rectangle(new Point3f(-holderDepth,0,0), new Vector3f(0,0,-holderLength), new Vector3f(0,-holderHeight,0));
		bottleHolderTop5.material = bottleHolderMaterial;
		
		t = new Matrix4f();
		t.setIdentity();

		// Instance one
		t.setTranslation(new Vector3f(25, 10.25f, -28));
		rot = new Matrix4f();
		rot.setIdentity();

		Instance bottleHolderInstance1 = new Instance(bottleHolderTop1, t);
		Instance bottleHolderInstance2 = new Instance(bottleHolderTop2, t);
		Instance bottleHolderInstance3 = new Instance(bottleHolderTop3, t);
		Instance bottleHolderInstance4 = new Instance(bottleHolderTop4, t);
		Instance bottleHolderInstance5 = new Instance(bottleHolderTop5, t);

		
		t = new Matrix4f();
		t.setIdentity();

		// Instance one
		t.setTranslation(new Vector3f(25, 5.25f, -28));
		rot = new Matrix4f();
		rot.setIdentity();

		Instance bottleHolderInstance6 = new Instance(bottleHolderTop1, t);
		Instance bottleHolderInstance7 = new Instance(bottleHolderTop2, t);
		Instance bottleHolderInstance8 = new Instance(bottleHolderTop3, t);
		Instance bottleHolderInstance9 = new Instance(bottleHolderTop4, t);
		Instance bottleHolderInstance10 = new Instance(bottleHolderTop5, t);
		
		
		
		Mesh teapot1;
		try {

			teapot1 = ObjReader.read("../obj/wineGlass.obj", 1.f);
		} catch (IOException e) {
			System.out.printf("Could not read .obj file\n");
			return;
		}
		teapot1.material = new Refractive(1.7f);
		// teapot1.material = new Diffuse(new Spectrum(1,0,0));
		t = new Matrix4f();
		t.setIdentity();
		BSPAccelerator teapot1Acc = new BSPAccelerator(teapot1);
		// Instance one
		// t.setScale(2);
		t.setTranslation(new Vector3f(5, 7, -22));
		Instance teapot1Instance = new Instance(teapot1Acc, t);

		t = new Matrix4f();
		t.setIdentity();
		// t.setScale(2);
		t.setTranslation(new Vector3f(-3, 6.5f, -18.5f));
		rot = new Matrix4f();
		rot.setIdentity();
		rot.rotY(-(float) Math.PI / 3.f);
		rot.rotX((float) Math.PI / 3.f);
		t.mul(rot);
		// BSPAccelerator teapot2Acc = new BSPAccelerator(teapot2);
		Instance teapot2Instance = new Instance(teapot1Acc, t);

		
		// Tabourets
		Mesh tabouret;
		try {

			tabouret = ObjReader.read("../obj/tabouret.obj", 1.f);
		} catch (IOException e) {
			System.out.printf("Could not read .obj file\n");
			return;
		}
		tabouret.material = new Glossy(50, new Spectrum(0.6f,0.6f,0.6f), new Spectrum(1,1,1));
		BSPAccelerator tabouretAcc = new BSPAccelerator(tabouret);

		t = new Matrix4f();
		t.setIdentity();
		
		// Instance one
		t.setScale(3);
		t.setTranslation(new Vector3f(5, 3, -18));
		
		rot = new Matrix4f();
		rot.setIdentity();
		rot.rotX(-(float) Math.PI /2.f);
		t.mul(rot);
		Instance tabouretInstance1 = new Instance(tabouretAcc, t);
		
		// Instance two
		t.setIdentity();
		t.setScale(3);
		t.setTranslation(new Vector3f(-3,3,-16));
		Matrix4f rot2 = new Matrix4f();
		rot2.setIdentity(); rot2.rotY((float)Math.PI/6f);
		t.mul(rot2);
		t.mul(rot);
		Instance tabouretInstance2 = new Instance(tabouretAcc, t); 
		
		// Instance three
		t.setIdentity();
		t.setScale(3);
		t.setTranslation(new Vector3f(9,3,-17));
		t.mul(rot);
		Instance tabouretInstance3 = new Instance(tabouretAcc, t);
		
		// Collect objects in intersectable list
		IntersectableList intersectableList = new IntersectableList();
		intersectableList.add(bumpyGroundPlane);
		intersectableList.add(bumpyLeftPlane);
		intersectableList.add(bumpyBackPlane);
		intersectableList.add(bumpyRightPlane);
		intersectableList.add(topPlane);
		intersectableList.add(discoInstance);
		intersectableList.add(bumpyFrontBar);
		intersectableList.add(bumpyLeftBar);
		intersectableList.add(top1);
		intersectableList.add(top2);
		intersectableList.add(teapot1Instance);
		intersectableList.add(teapot2Instance);
		intersectableList.add(couchInstance);
		intersectableList.add(chairInstance);
		intersectableList.add(bottleInstance);
		intersectableList.add(bottleHolderInstance1);
		intersectableList.add(bottleHolderInstance2);
		intersectableList.add(bottleHolderInstance3);
		intersectableList.add(bottleHolderInstance4);
		intersectableList.add(bottleHolderInstance5);
		intersectableList.add(bottleHolderInstance6);
		intersectableList.add(bottleHolderInstance7);
		intersectableList.add(bottleHolderInstance8);
		intersectableList.add(bottleHolderInstance9);
		intersectableList.add(bottleHolderInstance10);
		intersectableList.add(tabouretInstance1);
		intersectableList.add(tabouretInstance2);
		intersectableList.add(tabouretInstance3);

		lightList = new LightList();

		AreaLight light = new AreaLight(new Vector3f(10,19.5f,-15), new Vector3f(0,0,-1f), new Vector3f(-1f,0,0), new Spectrum(1000));
		AreaLight light2 = new AreaLight(new Vector3f(-5, 19.5f, -20), new Vector3f(0,0,-1f), new Vector3f(-1f,0,0), new Spectrum(1000));
		AreaLight light3 = new AreaLight(new Vector3f(3, 19.5f, 5), new Vector3f(0,0,-1f), new Vector3f(-1f,0,0), new Spectrum(1000));
		AreaLight light4 = new AreaLight(new Vector3f(-5, 19.5f, -35), new Vector3f(0,0,-1f), new Vector3f(-1f,0,0), new Spectrum(1000));

		 // Set the root node for the scene
		root = intersectableList;

		// light sources
		AreaLight barLight = new AreaLight(new Vector3f(24.99f,12,-30), new Vector3f(0,2.5f,0), new Vector3f(0,0,-2.5f), new Spectrum(200));
		AreaLight barLight2 = new AreaLight(new Vector3f(24.99f,7,-35), new Vector3f(0,2.5f,0), new Vector3f(0,0,-2.5f), new Spectrum(200));

		lightList.add(barLight);
		lightList.add(barLight2);
		intersectableList.add(barLight);
		intersectableList.add(barLight2);
		 lightList.add(light);
		 lightList.add(light2);
		 lightList.add(light3);
		 lightList.add(light4);
		 intersectableList.add(light);
		 intersectableList.add(light2);
		 intersectableList.add(light3);
		 intersectableList.add(light4);
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
