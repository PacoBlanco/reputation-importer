package cross.reputation.model;

import java.util.ArrayList;
import java.util.List;

public class ReputationAlgorithm {
	String name;
	List<AccessType> accesibilities;
	CollectionType resultCollectionType;
	List<EntityType> entityTypes;
	List<Metric> usesMetrics;
	List<ReputationStep> reputationSteps;
	List<ReputationValue> reputationSources;
	List<ReputationValue> reputationResults;
	String description;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<AccessType> getAccesibility() {
		return accesibilities;
	}
	public void setAccesibility(List<AccessType> accesibilities) {
		this.accesibilities = accesibilities;
	}
	public void addAccesibility(AccessType accesibility) {
		if(accesibilities == null) {
			accesibilities = new ArrayList<AccessType>();
		}
		accesibilities.add(accesibility);
	}
	public CollectionType getResultCollectionType() {
		return resultCollectionType;
	}
	public void setResultCollectionType(CollectionType resultCollectionType) {
		this.resultCollectionType = resultCollectionType;
	}
	public List<EntityType> getEntityType() {
		return entityTypes;
	}
	public void addEntityType(EntityType entityType) {
		if(entityTypes == null) {
			entityTypes = new ArrayList<EntityType>();
		}
		entityTypes.add(entityType);
	}
	public List<Metric> getUsesMetrics() {
		return usesMetrics;
	}
	public void addUsesMetrics(Metric usesMetric) {
		if(usesMetrics == null) {
			usesMetrics = new ArrayList<Metric>();
		}
		usesMetrics.add(usesMetric);
	}
	public List<ReputationStep> getReputationSteps() {
		return reputationSteps;
	}
	public void addReputationSteps(ReputationStep reputationStep) {
		if(reputationSteps == null) {
			reputationSteps = new ArrayList<ReputationStep>();
		}
		reputationSteps.add(reputationStep);
	}
	public List<ReputationValue> getReputationSources() {
		return reputationSources;
	}
	public void addReputationSources(ReputationValue reputationSource) {
		if(reputationSources == null) {
			reputationSources = new ArrayList<ReputationValue>();
		}
		reputationSources.add(reputationSource);
	}
	public List<ReputationValue> getReputationResults() {
		return reputationResults;
	}
	public void addReputationResults(ReputationValue reputationResult) {
		if(reputationResults == null) {
			reputationResults = new ArrayList<ReputationValue>();
		}
		reputationResults.add(reputationResult);
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String toString(String offset) {
		String result = offset+"name:"+name+"\n";
		result += offset+"description:"+description+"\n";
		result += offset+"accesibilities:"+
				((accesibilities == null)?"null":"");
		if(accesibilities != null) {
			for(AccessType accessType : accesibilities) {
				result += accessType+",";
			}
		}
		result += "\n"+offset+"resultCollectionType:"+resultCollectionType+"\n";
		result += offset+"entityTypes size:"+
				((entityTypes == null)?"null":entityTypes.size());
		if(entityTypes != null) {
			for(EntityType entityType : entityTypes) {
				result += "\n"+offset+"entityType:"+entityType+"\n"+
						entityType.toString(offset+"      ");
			}
		}
		result += "\n"+offset+"usesMetrics size:"+
				((usesMetrics == null)?"null":usesMetrics.size());
		if(usesMetrics != null) {
			for(Metric metric : usesMetrics) {
				result += "\n"+offset+"Metric:"+metric+"\n"+
						metric.toString(offset+"      ");
			}
		}
		result += "\n"+offset+"reputationSteps size:"+
				((reputationSteps == null)?"null":reputationSteps.size());
		if(reputationSteps != null) {
			for(ReputationStep reputationStep : reputationSteps) {
				result += "\n"+offset+"ReputationStep:"+reputationStep+"\n"+
						reputationStep.toString(offset+"      ");
			}
		}
		result += "\n"+offset+"reputationSources size:"+
				((reputationSources == null)?"null":reputationSources.size());
		if(reputationSources != null) {
			for(ReputationValue reputationValue : reputationSources) {
				result += "\n"+offset+"reputationValue:"+reputationValue+"\n"+
						reputationValue.toString(offset+"      ");
			}
		}
		result += "\n"+offset+"reputationResults size:"+
				((reputationResults == null)?"null":reputationResults.size());
		if(reputationResults != null) {
			for(ReputationValue reputationValue : reputationResults) {
				result += "\n"+offset+"reputationValue:"+reputationValue+"\n"+
						reputationValue.toString(offset+"      ");						
			}
		}
		return result;
	}
	
	static public List<Class<? extends ReputationAlgorithm>> listSubclasses() {
		List<Class<? extends ReputationAlgorithm>> list = 
			new ArrayList<Class<? extends ReputationAlgorithm>>();
		list.add(ReputationStep.class);
		list.add(ReputationImporter.class);
		list.add(CollectingAlgorithm.class);
		list.add(ReputationModule.class);
		list.add(ReputationModel.class);
		return list;		
	}
}
