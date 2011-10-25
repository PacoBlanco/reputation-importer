package cross.reputation.model;

import java.util.ArrayList;
import java.util.List;

public interface ReputationAlgorithmI {
	
	public String getName();
	public void setName(String name);
	public List<AccessType> getAccesibility();
	public void setAccesibility(List<AccessType> accesibilities);
	public void addAccesibility(AccessType accesibility);
	public CollectionType getResultCollectionType();
	public void setResultCollectionType(CollectionType resultCollectionType);
	public List<EntityType> getEntityType();
	public void addEntityType(EntityType entityType);
	public List<Metric> getUsesMetrics();
	public void addUsesMetrics(Metric usesMetric);
	public List<ReputationValue> getReputationSources();
	public void addReputationSources(ReputationValue reputationSource);
	public List<ReputationValue> getReputationResults();
	public void addReputationResults(ReputationValue reputationResult);
	public void setDefinedByReputationModel(ReputationAlgorithmImplementation repAlg);
	public ReputationAlgorithmImplementation getDefinedByReputationModel();
	public String getDescription();
	public void setDescription(String description);
	public Integer getStepIdentifier();
	public void setStepIdentifier(Integer stepIdentifier);
	public String toString(String offset);
	
}
