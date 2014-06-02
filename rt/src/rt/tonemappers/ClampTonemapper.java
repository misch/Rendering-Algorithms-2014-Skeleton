package rt.tonemappers;

import java.awt.image.BufferedImage;

import rt.Film;
import rt.Spectrum;
import rt.Tonemapper;

/**
 * Tone maps a film by clamping all color channels to range [0,1].
 */
public class ClampTonemapper implements Tonemapper {

	/**
	 * Perform tone mapping and return a {@link java.awt.image.BufferedImage}.
	 * 
	 * @param film the film to be tonemapped
	 * @return the output image
	 */
	public BufferedImage process(Film film)
	{
		BufferedImage img = new BufferedImage(film.getWidth(), film.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		
		for(int i=0; i<film.getWidth(); i++)
		{
			for(int j=0; j<film.getHeight(); j++)
			{
				// Clamping
				Spectrum s = film.getImage()[i][j];
				if (Float.isNaN(s.r) || Float.isNaN(s.g)|| Float.isNaN(s.b)){
					s = new Spectrum(0,1,0);
				}
				s.clamp(0,1);
				img.setRGB(i, film.getHeight()-1-j, ((int)(255.f*s.r) << 16) | ((int)(255.f*s.g) << 8) | ((int)(255.f*s.b)));
			}
		}
		return img;
	}
}
