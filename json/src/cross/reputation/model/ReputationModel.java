package cross.reputation.model;

import java.util.ArrayList;
import java.util.List;

public class ReputationModel extends ReputationAlgorithm {
	List<ModuleInfo> reputationModules;

	public List<ModuleInfo> getReputationModules() {
		return reputationModules;
	}

	public void addReputationModules(ModuleInfo reputationModule) {
		if(reputationModules == null) {
			reputationModules = new ArrayList<ModuleInfo>();
		}
		reputationModules.add(reputationModule);
	}
	
}
