package cross.reputation.model;

import java.util.ArrayList;
import java.util.List;

public class ReputationModuleBehaviour extends ReputationBehaviour {
	List<ReputationAlgorithmImplementation> obtainsReputationsBy;

	public List<ReputationAlgorithmImplementation> getObtainsReputationsBy() {
		return obtainsReputationsBy;
	}
	
	public void addObtainsReputationsBy(
			ReputationAlgorithmImplementation reputationAlgorithm) {
		if(obtainsReputationsBy == null) {
			obtainsReputationsBy = new ArrayList<ReputationAlgorithmImplementation>();
		}
		obtainsReputationsBy.add(reputationAlgorithm);
	}
	
	public String toString(String offset) {
		String result = offset+"reputationModules size:"+((obtainsReputationsBy==null)?
				"null":obtainsReputationsBy.size());
		if(obtainsReputationsBy != null) {
			for(ReputationAlgorithmImplementation repAlg : obtainsReputationsBy) {
				result += "\n"+offset+"reputationModule:"+repAlg+
					"\n"+repAlg.toString(offset+"     ");
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
