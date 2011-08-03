package cross.reputation.model;

import java.util.ArrayList;
import java.util.List;

public class ModuleInfo {
	List<CollectingAlgorithm> collectsReputationsBy;

	public List<CollectingAlgorithm> getCollectsReputationsBy() {
		return collectsReputationsBy;
	}
	
	public void addCollectsReputationsBy(CollectingAlgorithm collectingAlgorithm) {
		if(collectsReputationsBy == null) {
			collectsReputationsBy = new ArrayList<CollectingAlgorithm>();
		}
		collectsReputationsBy.add(collectingAlgorithm);
	}
}
