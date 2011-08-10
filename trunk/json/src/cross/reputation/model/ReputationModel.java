package cross.reputation.model;

import java.util.ArrayList;
import java.util.List;

public class ReputationModel extends ReputationAlgorithm {
	List<ReputationModule> reputationModules;

	public List<ReputationModule> getReputationModules() {
		return reputationModules;
	}

	public void addReputationModules(ReputationModule reputationModule) {
		if(reputationModules == null) {
			reputationModules = new ArrayList<ReputationModule>();
		}
		reputationModules.add(reputationModule);
	}
	
	public String toString(String offset) {
		String result = super.toString(offset);
		result += "\n"+offset+"reputationModules size:"+((reputationModules==null)?
				"null":reputationModules.size());
		if(reputationModules != null) {
			for(ReputationModule reputationModule : reputationModules) {
				result += "\n"+offset+"reputationModule:"+reputationModule+
					"\n"+reputationModule.toString(offset+"     ");
			}
		}
		return result;
	}
	
	static public List<Class<? extends ReputationAlgorithm>> listSubclasses() {
		return null;
	}
}
