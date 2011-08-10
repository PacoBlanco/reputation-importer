package cross.reputation.model;

import java.util.ArrayList;
import java.util.List;

public class ReputationModule extends ReputationAlgorithm {
	List<ReputationAlgorithm> obtainsReputationsBy;

	public List<ReputationAlgorithm> getObtainsReputationsBy() {
		return obtainsReputationsBy;
	}
	
	public void addObtainsReputationsBy(ReputationAlgorithm collectingAlgorithm) {
		if(obtainsReputationsBy == null) {
			obtainsReputationsBy = new ArrayList<ReputationAlgorithm>();
		}
		obtainsReputationsBy.add(collectingAlgorithm);
	}
	
	public String toString(String offset) {
		String result = super.toString(offset);
		result += "\n"+offset+"reputationModules size:"+((obtainsReputationsBy==null)?
				"null":obtainsReputationsBy.size());
		if(obtainsReputationsBy != null) {
			for(ReputationAlgorithm collectingAlg : obtainsReputationsBy) {
				result += "\n"+offset+"reputationModule:"+collectingAlg+
					"\n"+collectingAlg.toString(offset+"     ");
			}
		}
		return result;
	}
	
	static public List<Class<? extends ReputationAlgorithm>> listSubclasses() {
		return null;
	}
}
