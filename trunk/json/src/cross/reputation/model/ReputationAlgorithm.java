package cross.reputation.model;

import java.util.ArrayList;
import java.util.List;

public class ReputationAlgorithm {
	String name;
	AccessType accesibility;
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
	public AccessType getAccesibility() {
		return accesibility;
	}
	public void setAccesibility(AccessType accesibility) {
		this.accesibility = accesibility;
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
	
}
