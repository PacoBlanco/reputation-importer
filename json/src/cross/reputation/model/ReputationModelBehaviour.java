package cross.reputation.model;

import java.util.ArrayList;
import java.util.List;

public class ReputationModelBehaviour extends ReputationBehaviour{
	List<ReputationAlgorithmImplementation> reputationModules;

	public List<ReputationAlgorithmImplementation> getReputationModules() {
		return reputationModules;
	}

	public void addReputationModules(
			ReputationAlgorithmImplementation reputationModule) {
		if(reputationModules == null) {
			reputationModules = new ArrayList<ReputationAlgorithmImplementation>();
		}
		reputationModules.add(reputationModule);
	}
	
	public String toString(String offset) {
		String result = offset+"reputationModules size:"+
			((reputationModules==null)?"null":reputationModules.size());
		if(reputationModules != null) {
			for(ReputationAlgorithmImplementation reputationModule : reputationModules) {
				result += "\n"+offset+"reputationModule:"+reputationModule+
					"\n"+reputationModule.toString(offset+"     ");
			}
		}
		String last = super.toString(offset);
		if(last.isEmpty()) {
			return result;
		}
		return result+"\n"+last;
	}
	
	static public List<Class<? extends ReputationAlgorithm>> listSubclasses() {
		return null;
	}
}
