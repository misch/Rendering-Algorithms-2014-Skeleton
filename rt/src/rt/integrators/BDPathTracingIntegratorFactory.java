package rt.integrators;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import rt.Integrator;
import rt.IntegratorFactory;
import rt.Scene;
import rt.Spectrum;
import rt.Tonemapper;
import rt.films.BoxFilterFilm;
import rt.tonemappers.ClampTonemapper;

/**
 * Makes a {@link PointLightIntegrator}.
 */
public class BDPathTracingIntegratorFactory implements IntegratorFactory {
	private ArrayList<BDPathTracingIntegrator> integrators = new ArrayList<>();
	private Scene scene;
	
	public BDPathTracingIntegratorFactory(Scene scene){
		this.scene = scene;
	}
	
	public Integrator make(Scene scene) {
		BDPathTracingIntegrator integrator = new BDPathTracingIntegrator(scene);
		integrators.add(integrator);
		return integrator;
	}

	public void prepareScene(Scene scene) {
		// TODO Auto-generated method stub
	}
	
	public void writeLightImage(String path){
		BoxFilterFilm film = new BoxFilterFilm(scene.getFilm().getWidth(), scene.getFilm().getHeight());
		for (BDPathTracingIntegrator integrator : integrators){
			Spectrum[][] lightImg = integrator.getLightImg();
			int[][] nSamples = integrator.getNSamples();
			for (int x = 0; x < film.getWidth(); x++){
				for (int y = 0; y < film.getHeight(); y++){
					film.addMultSamples((double)x, (double)y, lightImg[x][y], nSamples[x][y]);
				}
			}
		}
		
		
		BufferedImage img = new ClampTonemapper().process(film);
		try
		{	
			ImageIO.write(img, "png", new File(path+".png"));
			System.out.println("Wrote light image to: \n " + path);
		} catch (IOException e) {System.out.println("Could not write image to \n"+ path);}
	}

//	public void addLightImage(Film film){
//		for (BDPathTracingIntegrator integrator : integrators){
//			Spectrum[][] lightImg = integrator.getLightImage();
//			float[][] nSamples = integrator.getNSamples();
//			for (int x = 0; x < film.getWidth(); x++){
//				for (int y = 0; y < film.getHeight(); y++){
//					film.addMultSamples(x, y, lightImg[x][y], nSamples[x][y]);
//				}
//			}
//		}
//	}

}
