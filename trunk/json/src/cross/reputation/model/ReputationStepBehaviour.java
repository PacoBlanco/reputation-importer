package cross.reputation.model;

import java.util.List;

public class ReputationStepBehaviour extends ReputationBehaviour {
	int stepIdentificator;

	public int getStepIdentificator() {
		return stepIdentificator;
	}

	public void setStepIdentificator(int stepIdentificator) {
		this.stepIdentificator = stepIdentificator;
	}
	
	public String toString(String offset) {
		String result = offset + "stepIdentificator:" + stepIdentificator;
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
