package rt;

import javax.imageio.ImageIO;

import rt.basicscenes.*;
import java.util.*;
import java.awt.image.*;
import java.io.*;

/**
 * The main rendering loop. Provides multi-threading support. The {@link Main#scene} to be rendered
 * is hard-coded here, so you can easily change it. The {@link Main#scene} contains 
 * all configuration information for the renderer.
 */
public class Main {
	public static int[] debugPixel;// = {69,128-13}; // if defined, then only a certain number of pixels will be rendered
	public static int debugWindowSize = 0; // size 0 will render only one pixel
	/** 
	 * The scene to be rendered.
	 */
//	public static Scene scene = new rt.testscenes.Camera();
//	public static Scene scene = new rt.testscenes.Blinn();
//	public static Scene scene = new rt.testscenes.MeshTest();
//	public static Scene scene = new rt.testscenes.CSGPrimitives();
//	public static Scene scene = new rt.testscenes.InstancingTest();
//	public static Scene scene = new rt.testscenes.InstancingTeapots();
//	public static Scene scene = new rt.testscenes.Blinn();
//	public static Scene scene = new rt.testscenes.CSGScene();
//	public static Scene scene = new rt.testscenes.RefractiveSphere();
//	public static Scene scene = new rt.testscenes.AccelerationTest();
//	public static Scene scene = new rt.testscenes.AreaLightTest();
//	public static Scene scene = new rt.testscenes.AreaLightSceneMarco();
//	public static Scene scene = new rt.testscenes.GlossyScene();
//	public static Scene scene = new rt.testscenes.ImportanceSampling();
//	public static Scene scene = new rt.testscenes.Textures();
	public static Scene scene = new rt.testscenes.TestSceneMisch();
//	public static Scene scene = new rt.testscenes.BDPathtracingBoxSphereGlass();
	
	static LinkedList<RenderTask> queue;
	static Counter tasksLeft;
		
	static public class Counter
	{
		public Counter(int n)
		{
			this.n = n;
		}
		
		public int n;
	}
	
	/**
	 * A render task represents a rectangular image region that is rendered
	 * by a thread in one chunk.
	 */
	static public class RenderTask
	{
		public int left, right, bottom, top;
		public Integrator integrator;
		public Scene scene;
		public Sampler sampler;
		
		public RenderTask(Scene scene, int left, int right, int bottom, int top)
		{			
			this.scene = scene;
			this.left = left;
			this.right = right;
			this.bottom = bottom;
			this.top = top;

			// The render task has its own sampler and integrator. This way threads don't 
			// compete for access to a shared sampler/integrator, and thread contention
			// can be reduced. 
			integrator = scene.getIntegratorFactory().make(scene);
			sampler = scene.getSamplerFactory().make();
		}
	}
	
	static public class RenderThread implements Runnable
	{			
		public void run()
		{
			while(true)
			{
				RenderTask task;
				synchronized(queue)
				{
					if(queue.size() == 0) break;
					task = queue.poll();
				}
													
				// Render the image block represented by the task
				
				// For all pixels
				for(int j=task.bottom; j<task.top; j++)
				{
					for(int i=task.left; i<task.right; i++)
					{								
						float samples[][] = task.integrator.makePixelSamples(task.sampler, task.scene.getSPP());

						// For all samples of the pixel
						for(int k=0; k<samples.length; k++)
						{	
							// Make ray
							Ray r = task.scene.getCamera().makeWorldSpaceRay(i, j, samples[k]);
							
							Spectrum s;

							s = task.integrator.integrate(r);
							
							// Write to film
							task.scene.getFilm().addSample((double)i+(double)samples[k][0], (double)j+(double)samples[k][1], s);
						}
					}
				}
				
				synchronized(tasksLeft)
				{
					tasksLeft.n--;
					if(tasksLeft.n == 0) tasksLeft.notifyAll();
				}
			}
		}
	}
	
	public static void main(String[] args)
	{			
		int taskSize = 4;	// Each task renders a square image block of this size
		int nThreads;
		if (debugPixel == null){
			nThreads = 4;	// Number of threads to be used for rendering
		}else{
			nThreads = 1;
		}
		int width = scene.getFilm().getWidth();
		int height = scene.getFilm().getHeight();

		scene.prepare();
		
		// Make render tasks, split image into blocks to be rendered by the tasks
		queue = new LinkedList<RenderTask>();
		int nTasks;
		if (debugPixel == null){
			nTasks = (int)Math.ceil((double)width/(double)taskSize) * (int)Math.ceil((double)height/(double)taskSize);
			for(int j=0; j<(int)Math.ceil((double)height/(double)taskSize); j++)
			{
				for(int i=0; i<(int)Math.ceil((double)width/(double)taskSize); i++)
				{
					RenderTask task = new RenderTask(scene, i*taskSize, Math.min((i+1)*taskSize,width), j*taskSize, Math.min((j+1)*taskSize,height));
					queue.add(task);
				}
			}
		}
		else{
			nTasks = 1;
			int i = debugPixel[0]; int j = debugPixel[1];
			RenderTask debugTask = new RenderTask(scene, Math.max(0,i-debugWindowSize), Math.min(i+1+debugWindowSize,width), Math.max(0,j-debugWindowSize), Math.min(j+1+debugWindowSize,height));
			queue.add(debugTask);
		}
		
		tasksLeft = new Counter(nTasks);
		
		
		
		Timer timer = new Timer();
		timer.reset();
		
		// Start render threads
		for(int i=0; i<nThreads; i++)
		{
			new Thread(new RenderThread()).start();
		}
		
		// Wait for threads to end
		int printed = 0;
		System.out.printf("Rendering image: " + scene.getOutputFilename()+ "\n");
		System.out.printf("0%%                                                50%%                                           100%%\n");
		System.out.printf("|---------|---------|---------|---------|---------|---------|---------|---------|---------|---------\n");
		synchronized(tasksLeft)
		{
			while(tasksLeft.n>0)
			{
				try
				{
					tasksLeft.wait(500);
				} catch (InterruptedException e) {}
				
				int toPrint = (int)( ((float)nTasks-(float)tasksLeft.n)/(float)nTasks*100-printed );
				for(int i=0; i<toPrint; i++)
					System.out.printf("*");
				printed += toPrint;
			}
		}
		
		System.out.printf("\n");
		System.out.printf("Image computed in %d ms.\n", timer.timeElapsed());
		scene.finish();
		// Tone map output image and writ to file
		BufferedImage image = scene.getTonemapper().process(scene.getFilm());
		try
		{
			ImageIO.write(image, "png", new File(scene.getOutputFilename()+".png"));
		} catch (IOException e) {}
	}
	
	
}
