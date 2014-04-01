package rt;

import java.util.ArrayList;
import java.util.Random;

/**
 * A list of light sources.
 */
public class LightList extends ArrayList<LightGeometry> {
	
	public LightGeometry getRandomLightSource(){
		int randomIndex = new Random().nextInt(this.size());
		
		return this.get(randomIndex);
	}
}
