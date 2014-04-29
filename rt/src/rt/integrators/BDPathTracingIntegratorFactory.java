package rt.integrators;

import rt.Integrator;
import rt.IntegratorFactory;
import rt.Scene;

/**
 * Makes a {@link PointLightIntegrator}.
 */
public class BDPathTracingIntegratorFactory implements IntegratorFactory {

	public BDPathTracingIntegratorFactory(Scene scene){
		
	}
	
	public Integrator make(Scene scene) {
		return new BDPathTracingIntegrator(scene);
	}

	public void prepareScene(Scene scene) {
		// TODO Auto-generated method stub
	}

}
