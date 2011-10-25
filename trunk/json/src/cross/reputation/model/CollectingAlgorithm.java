package cross.reputation.model;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;

public class CollectingAlgorithm extends ReputationAlgorithm {
	private Resource resource;
	
	public Resource getResource() {
		return resource;
	}	
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	
	public String toString(String offset) {
		return super.toString(offset);
	}
	
	static public List<Class<? extends ReputationAlgorithm>> listSubclasses() {
		List<Class<? extends ReputationAlgorithm>> list = 
			new ArrayList<Class<? extends ReputationAlgorithm>>();
		list.add(ReputationalAction.class);
		list.add(CollectingSystem.class);
		return list;
	}
}
