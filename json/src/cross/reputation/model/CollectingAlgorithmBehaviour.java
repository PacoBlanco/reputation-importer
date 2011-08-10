package cross.reputation.model;

import java.util.ArrayList;
import java.util.List;

public class CollectingAlgorithmBehaviour extends ReputationBehaviour {
	List<Object> behaviours;
	
	public String toString(String offset) {
		return super.toString(offset);
	}
	
	static public List<Class<? extends 
			ReputationBehaviour>> listSubclasses() {
		List<Class<? extends ReputationBehaviour>> list = 
			new ArrayList<Class<? extends ReputationBehaviour>>();
		list.add(ReputationalActionBehaviour.class);
		list.add(CollectingSystemBehaviour.class);
		return list;
	}
}
