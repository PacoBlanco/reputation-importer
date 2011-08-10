package cross.reputation.model;

import java.util.ArrayList;
import java.util.List;

public class ReputationBehaviour {
	List<ReputationBehaviour> behaviours;
	
	public void addBehaviourInstance(ReputationBehaviour behaviour) {
		if(behaviours == null) {
			behaviours = new ArrayList<ReputationBehaviour>();
		}
		behaviours.add(behaviour);
	}

	public List<ReputationBehaviour> getBehaviours() {
		return behaviours;
	}

	public void setBehaviours(List<ReputationBehaviour> behaviours) {
		this.behaviours = behaviours;
	}
	
	public boolean addBehaviour(ReputationBehaviour behaviour) throws Exception {
		if(behaviours == null) {
			behaviours = new ArrayList<ReputationBehaviour>();
		}
		return verifyAndAddSubclass(behaviour);
	}
	
	public boolean addBehaviour(Class<? extends ReputationBehaviour> behaviour) 
			throws Exception {
		if(behaviours == null) {
			behaviours = new ArrayList<ReputationBehaviour>();
		}
		return verifyAndAddSubclass(behaviour.newInstance());
	}
	
	public boolean verifyAndAddSubclass(ReputationBehaviour instance) throws Exception {
		Class<? extends	ReputationBehaviour> newBehaviour = instance.getClass();
		for(ReputationBehaviour behaviour : behaviours) {
			if(behaviour.getClass() == newBehaviour) {
				return false;
			}
			if(behaviour.getClass() == ReputationModelBehaviour.class) {
				if(newBehaviour == ReputationModuleBehaviour.class) {
					throw new Exception("ReputationModule class cannot be" +
							" instanced over ReputationModel object");
				}
			}
			if(behaviour.getClass() == ReputationImporterBehaviour.class) {
				if(newBehaviour == CollectingAlgorithmBehaviour.class) {
					throw new Exception("CollectingAlgorithm class cannot be" +
							" instanced over ReputationImporter object");
				}
			}
			if(behaviour.getClass() == CollectingAlgorithmBehaviour.class) {
				if(newBehaviour == CollectingSystemBehaviour.class) {
					for(ReputationBehaviour subBehaviour : behaviour.getBehaviours()) {
						if(subBehaviour.getClass() == 
							CollectingSystemBehaviour.class) {
							return false;
						}
						if(subBehaviour.getClass() == 
							ReputationalActionBehaviour.class) {
							throw new Exception("CollectingSystem class cannot be" +
									" instanced over ReputationalAction object");
						}
					}
					behaviour.addBehaviourInstance(newBehaviour.newInstance());
					return true;
				}
				if(newBehaviour == ReputationalActionBehaviour.class) {
					for(ReputationBehaviour subBehaviour : behaviour.getBehaviours()) {
						if(subBehaviour.getClass() == 
							CollectingSystemBehaviour.class) {
							throw new Exception("ReputationalAction class cannot be" +
									" instanced over CollectingSystem object");							
						}
						if(subBehaviour.getClass() == 
							ReputationalActionBehaviour.class) {
							return false;
						}
					}
					behaviour.addBehaviourInstance(instance);
					return true;
				}
			}
		}
		if(newBehaviour == ReputationalActionBehaviour.class ||
				newBehaviour == CollectingSystemBehaviour.class) {
			ReputationBehaviour behaviour = CollectingAlgorithmBehaviour.class.newInstance();
			behaviour.addBehaviourInstance(instance);
			addBehaviourInstance(behaviour);
			return true;
		}
		addBehaviourInstance(instance);
		return true;
	}
	
	public String toString(String offset) {
		StringBuilder result = new StringBuilder();
		if(behaviours != null) {
			for(ReputationBehaviour behaviour : behaviours) {
				if(result.length() > 0) {
					result.append("\n");
				}
				result.append(offset+behaviour);
				String last = behaviour.toString(offset+"     ");
				if(!last.isEmpty()) {
					result.append("\n"+last);
				}
			}
		}
		return result.toString();	
	}
}
