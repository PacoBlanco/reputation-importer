package cross.reputation.model;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;

public class ReputationAlgorithmImplementation extends ReputationBehaviour 
		implements ReputationAlgorithmI {
	private Resource resource = null;
	private String name = null;
	private List<AccessType> accesibilities = null;
	private CollectionType resultCollectionType = null;
	private List<EntityType> entityTypes = null;
	private List<Metric> usesMetrics = null;
	private List<ReputationValue> reputationSources = null;
	private List<ReputationValue> reputationResults = null;
	private String description = null;
	private Integer stepIdentifier = null;
	private ReputationAlgorithmImplementation definedByReputationModel = null;
	private String objectClass = null;
	private String algorithmPath = null;
	
	public Resource getResource() {
		return resource;
	}	
	public void setResource(Resource resource) {
		this.resource = resource;
	}
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
	public String getObjectClass() {
		return objectClass;
	}
	public void setObjectClass(String objectClass) {
		this.objectClass = objectClass;
	}
	public String getAlgorithmPath() {
		return algorithmPath;
	}
	public void setAlgorithmPath(String algorithmPath) {
		this.algorithmPath = algorithmPath;
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
	
	public ReputationAlgorithmImplementation getDefinedByReputationModel() {
		return definedByReputationModel;
	}
	public void setDefinedByReputationModel(
			ReputationAlgorithmImplementation definedByReputationModel) {
		this.definedByReputationModel = definedByReputationModel;
	}
	
	public String toString(String offset) {
		String result = offset+"name:"+name+"\n";
		result += offset+"description:"+description+"\n";
		result += offset+"objectClass:"+objectClass+"\n";
		result += offset+"algorithmPath:"+algorithmPath+"\n";
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
		result += "\n"+offset+"definedByReputationModel:"+
				((definedByReputationModel == null)?"null":definedByReputationModel);
		if(definedByReputationModel != null) {
			result += "\n"+definedByReputationModel.toString(offset+"     ");
		}
		/*result += "\n"+offset+"reputationSteps size:"+
				((reputationSteps == null)?"null":reputationSteps.size());
		if(reputationSteps != null) {
			for(ReputationStep reputationStep : reputationSteps) {
				result += "\n"+offset+"ReputationStep:"+reputationStep+"\n"+
						reputationStep.toString(offset+"      ");
			}
		}*/
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
		result += "\n"+offset+"stepIdentifier:"+stepIdentifier;
		String last = super.toString(offset);
		if(last.isEmpty()) {
			return result;
		}
		return result+"\n"+last;
	}
	
	@Override
	public Integer getStepIdentifier() {
		return stepIdentifier;
	}
	@Override
	public void setStepIdentifier(Integer stepIdentifier) {
		this.stepIdentifier = stepIdentifier;		
	}
	
	static public List<Class<? extends ReputationBehaviour>> listSubclasses() {
		List<Class<? extends ReputationBehaviour>> list = 
			new ArrayList<Class<? extends ReputationBehaviour>>();
		list.add(ReputationStepBehaviour.class);
		list.add(ReputationImporterBehaviour.class);
		list.add(CollectingAlgorithmBehaviour.class);
		list.add(ReputationModuleBehaviour.class);
		list.add(ReputationModelBehaviour.class);
		return list;		
	}
	
}
