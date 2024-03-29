package cross.reputation.model;

import java.util.List;

public class ReputationStep extends ReputationAlgorithm{
	int stepIdentificator;

	public int getStepIdentificator() {
		return stepIdentificator;
	}

	public void setStepIdentificator(int stepIdentificator) {
		this.stepIdentificator = stepIdentificator;
	}
	
	public String toString(String offset) {
		return super.toString(offset) + "\n" +
			offset + "stepIdentificator:" + stepIdentificator;
	}
	
	static public List<Class<? extends ReputationAlgorithm>> listSubclasses() {
		return null;
	}
}
