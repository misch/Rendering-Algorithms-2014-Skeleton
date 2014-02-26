package rt;

import java.util.ArrayList;
import java.util.Iterator;

import rt.intersectables.Aggregate;

public class IntersectableList extends Aggregate{
	
	private ArrayList<Intersectable> list = new ArrayList<Intersectable>();
	
	public void add(Intersectable intersectable){
		list.add(intersectable);
	}
	@Override
	public Iterator<Intersectable> iterator() {
		return list.iterator();
	}
	
}
