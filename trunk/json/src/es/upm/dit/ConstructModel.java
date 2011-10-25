package es.upm.dit;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import cross.reputation.model.CollectingAlgorithmBehaviour;
import cross.reputation.model.CollectingSystemBehaviour;
import cross.reputation.model.CollectionType;
import cross.reputation.model.Community;
import cross.reputation.model.Entity;
import cross.reputation.model.EntityIdentifier;
import cross.reputation.model.GlobalModel;
import cross.reputation.model.ImportationUnit;
import cross.reputation.model.Metric;
import cross.reputation.model.MetricMapping;
import cross.reputation.model.ModelException;
import cross.reputation.model.NumericScale;
import cross.reputation.model.ReputationAlgorithmImplementation;
import cross.reputation.model.ReputationBehaviour;
import cross.reputation.model.ReputationEvaluation;
import cross.reputation.model.ReputationImporterBehaviour;
import cross.reputation.model.ReputationModelBehaviour;
import cross.reputation.model.ReputationModuleBehaviour;
import cross.reputation.model.ReputationObject;
import cross.reputation.model.ReputationValue;
import cross.reputation.model.TrustBetweenCommunities;

public class ConstructModel {
	String modelPath;
	ReputationParser reputationParser;
	static final int CONSTRUCT_ALL = 1;
	static final int CONSTRUCT_NECCESARY = 2;
	static Set<String> systemIdentifierFilter = new HashSet<String>();	
	
	static public void main(String[] args) throws Exception {
		Property.setValues();
		ConstructModel.systemIdentifierFilter = Property.getSystem_identifier_filter();
		//ConstructModel.addSystemIdentifier("Scrappy");
		//ConstructModel.addSystemIdentifier("Opal");
		//String modelPath = "dir/modelWithEntities.rdf";
		String modelPath = Property.getModel_file_path();
		ConstructModel constructor = new ConstructModel(modelPath,CONSTRUCT_NECCESARY);
		constructor.getMoreAccounts();
		Map<String, Entity> entities = constructor
				.importOverAllEntitiesToCommunity("semanticWiki");
		EntitiesToRDFFile rdfFile = new EntitiesToRDFFile();
		String importationModelPath = Property.getImportation_model_file_path();
		rdfFile.writeToRDFFile(entities, importationModelPath, constructor.reputationParser);
		System.out.println("Fin");
	}
	
	public void getMoreAccounts() throws Exception {
		for(Entity entity : GlobalModel.getEntities().values()) {
			Map<Community,EntityIdentifier> usuario = entity.getIdentificatorInCommunities();
			//In this form of iteration, we dont search accounts in the new accounts found or
			//  accounts updated that have already been iterated
			for(Object object : usuario.keySet().toArray()) {
				Community community = (Community) object;
				Set<String> accounts = new HashSet<String>();				
				String userName = usuario.get(community).getName();
				//System.out.println(userName+":"+community);
				String url = usuario.get(community).getUrl();
				try {
					if(url != null) {
						accounts.add(url);
						Set<String> userAccounts = Scrapper.MoreUserAccountsByURL(url);
						if(userAccounts != null) {
							accounts.addAll(userAccounts);
						}
					} else {
						List<String> userAccounts = Scrapper.UserAccounts(userName,
								community.getDomainName(), true);
						if(userAccounts != null) {
							accounts.addAll(userAccounts);
						}
					}					
				} catch (Exception e) {
					e.printStackTrace();
					ModelException.throwException(ModelException.GET_MORE_ACCOUNTS, 
						"Error to get more accounts for entity:"+
						entity.getUniqueIdentificator()+" with community: "+
						community.getName()+" and user:"+userName+" from"+url);					
				}
				if(accounts != null) {
					SetAccountsInEntity(entity, userName, accounts);
				}
			}			
		}		
	}
	
	static private void SetAccountsInEntity(Entity entity, 
			String userName, Collection<String> accounts) throws Exception {
		for(String accountName : accounts) {
			Community community = getCommunityByAccountName(accountName);
			if(community == null)
				continue;
			EntityIdentifier id = entity.getIdentificatorInCommunities().get(community);
			if(id == null) {
				ModelException.sendMessage(ModelException.INFO,
						"New account:"+entity.getUniqueIdentificator()+","+
						community.getName()+","+userName+","+accountName);
				entity.addIdentificatorInCommunities(community,
					new EntityIdentifier(userName, accountName));
			} else if(id.getUrl() == null) {
				ModelException.sendMessage(ModelException.INFO,"Update account:"
						+entity.getUniqueIdentificator()+","+
						community.getName()+","+userName+","+accountName);
				id.setUrl(accountName);
			}
		}
	}
	
	static public Community getCommunityByAccountName(String name) {
		name = name.replaceFirst("http://", "");
		name = name.replaceFirst("www.", "");
		for(Community community : GlobalModel.getCommunities().values()) {
			String communityName = community.getDomainName().replaceFirst("http://", "");
			communityName = communityName.replaceFirst("www.", "");
			//System.out.println("     "+name+" =? "+communityName);
			if(name.toLowerCase().startsWith(communityName.toLowerCase())) {
				return community;
			}
		}
		return null;
	}
	
	public ConstructModel(String modelPath) throws Exception {
		this.modelPath = modelPath;
		reputationParser = new ReputationParser(modelPath);
	}
	
	public ConstructModel(String modelPath, int modelConstructorType
			) throws Exception {
		this.modelPath = modelPath;
		reputationParser = new ReputationParser(modelPath);
		if(modelConstructorType == CONSTRUCT_ALL) {
			constructAllModel();
		} else if(modelConstructorType == CONSTRUCT_NECCESARY) {
			constructNeccesary();
		}
	}
	
	public ConstructModel(String modelPath, int modelConstructorType,
			Set<String> systemIdentifier)  throws Exception {
		this(modelPath, modelConstructorType);
		setSystemIdentifier(systemIdentifier);
	}		
	
	public Set<String> getSystemIdentifier() {
		return systemIdentifierFilter;
	}

	static public void setSystemIdentifier(Set<String> systemIdentifier) {
		systemIdentifierFilter = systemIdentifier;
	}
	
	static public void addSystemIdentifier(String systemIdentifier) {
		if(systemIdentifierFilter == null) {
			systemIdentifierFilter = new HashSet<String>();
		}
		systemIdentifierFilter.add(systemIdentifier);
	}

	public void constructNeccesary() throws Exception {
		GlobalModel.addCommunities(reputationParser.getCommunities(true));
		GlobalModel.addEntities(reputationParser.getEntities(true));
		GlobalModel.addMetrics(reputationParser.getMetrics(true));
		//GlobalModel.addMetricTransformers(reputationParser.getMetricTransformers(true));
	}
	
	public void constructAllModel() {
		/*
		reputationParser.getCommunities();
		reputationParser.getCategories();
		reputationParser.getTrustBetweenCommunities();
		reputationParser.getDimensions();
		reputationParser.getDimensionCorrelations();
		reputationParser.getEntities();
		reputationParser.getEvaluations();
		reputationParser.getImportationUnits();
		reputationParser.getMetrics();
		reputationParser.getMetricMappings();
		reputationParser.getMetricTransformers();
		reputationParser.getReputationEvaluations();
		reputationParser.getReputationObjects();
		reputationParser.getReputationValues();
		reputationParser.getScales();
		reputationParser.getScaleCorrelations();
		*/
	}
	
	public Map<String, Entity> importOverEntitiesToCommunity(String communityName,
			Set<String> entityNames) throws Exception {
		Community community = GlobalModel.getCommunities().get(communityName);
		if(community == null) {
			ModelException.sendMessage(ModelException.ERROR, "Community to import" +
					" is not set in the parsed semantic model");
			return null;
		}
		if(community.getReputationModel() == null) {
			ModelException.sendMessage(ModelException.ERROR, "ReputationModel from " +
					"Community(resource:"+community.getResource()+") must be set to" +
					" import reputations");
			return null;
		}
		//Find importationUnit
		Map<ReputationImporterBehaviour, Set<ImportationUnit>> importationUnits = 
				findImportationUnits(community.getReputationModel());
		//TODO: ver que existe al menos un importationUnit in todo el MAP!!
		//Set<MetricMapping> metricMappings = findMetricMappings(
		//		community.getReputationModel());
		Map<String, Entity> entities = new HashMap<String, Entity>();
		for(String entityName : entityNames) {
			Map<ReputationAlgorithmImplementation,Map<ImportationUnit,
				Map<ReputationAlgorithmImplementation,Map<Metric,List<Object>>>>>
				reputationOfRepAlg = new HashMap<ReputationAlgorithmImplementation,
				Map<ImportationUnit,Map<ReputationAlgorithmImplementation,Map<Metric,List<Object>>>>>();
			Entity entity = GlobalModel.getEntities().get(entityName);			
			Date timestamp = new Date();
			List<ReputationValue> reputationValues = new ArrayList<ReputationValue>();
			for(ReputationImporterBehaviour repImp : importationUnits.keySet()) {
				Map<ImportationUnit,Map<ReputationAlgorithmImplementation,Map<Metric,List<Object>>>>
					reputationOfImportUnits = new HashMap<ImportationUnit,Map<
					ReputationAlgorithmImplementation,Map<Metric,List<Object>>>>();
				for(ImportationUnit importationUnit : importationUnits.get(repImp)) {
					//Find collectingSystem that are inside importationUnit			
					ObjectWithIdentifier systems = findCollectingSystemsFromImportationUnit(
							importationUnit);
					if(systems == null) {
						continue;
					}
					boolean communityMatching = false;
					for(Community communityToImport : 
							entity.getIdentificatorInCommunities().keySet()) {
						//Comparison between communities is with pointers (not names)
						if(communityToImport == importationUnit.getImportedCommunity()) {
							communityMatching = true;
							break;
						}
					}
					if(communityMatching) {
						//Import and calculate reputations from all chained collectingSystems
						//inside the importationUnit
						Map<ReputationAlgorithmImplementation,Map<Metric,List<Object>>> 
							reputationOfRepAlgs = extractReputationFromEntity(repImp, systems, 
							importationUnit, entity,new ArrayList<Object>(),new HashMap<
							ReputationAlgorithmImplementation,Map<Metric,List<Object>>>());
						if(reputationOfRepAlgs != null) {
							reputationOfImportUnits.put(importationUnit, reputationOfRepAlgs);
						}		
					}
				}
				//ReputationImporter algorithm (default: 2 steps):
				//1 - join all reputation values in unique values to reduce reputationEvaluations
				//2 - calculate the final importation values with a simple add of all 
				//    reputationEvaluations of each resultMetric of the metricMappings.
				//    The value is a simple add of the importedMetrics giving the weight 
				//    according to the specific MetricMapping
				reputationValues.addAll(joinAllImportedReputationsByDefault(
						reputationOfImportUnits,repImp.getMapsMetrics(), 
						entity, community, timestamp));				
				List<ReputationValue> repValuesAux = calculateReputationsByDefaultMethod(
						repImp.getRoot(),reputationOfImportUnits,
						repImp.getMapsMetrics(), entity, community, timestamp);
				if(repValuesAux != null) {
					reputationValues.addAll(repValuesAux);
				}				
			}
			if(!reputationValues.isEmpty()) {
				continue;
			}			
			entity.addHasReputation(getReputationObjectWithValues(
					entity,community,reputationValues));		
			entities.put(entity.getUniqueIdentificator(), entity);
			System.out.println(entity.toString(""));			
		}
		return entities;
		//Process collectingSystem with process of subgroups??		
	}
	
	static public ReputationObject getReputationObjectWithValues (
			Entity entity, Community community, List<ReputationValue> reputationValues) {
		ReputationObject repObj = new ReputationObject();
		repObj.setOwner(entity);
		repObj.setFromCommunity(community);
		for(ReputationValue repVal : reputationValues) {
			repObj.addHasValue(repVal);
		}
		return repObj;
	}
	
	static public List<ReputationValue> calculateReputationsByDefaultMethod(
			ReputationAlgorithmImplementation repAlg, Map<ImportationUnit,
			Map<ReputationAlgorithmImplementation,Map<Metric,List<Object>>>> reputationOfEntities,
			List<MetricMapping> metricMappings, Entity entity, Community community,
			Date timestamp) throws Exception {
		if(repAlg.getUsesMetrics() == null || repAlg.getUsesMetrics().isEmpty()) {
			ModelException.throwException(ModelException.REPUTATIONALGORITHM_USEMETRIC,
					"reputation Algorithm(resource:"+repAlg.getResource()+") uses the default" +
					"method to obtain the final Reputation Value but without defining any" +
					"usesMetric property");
			return null;
		}
		List<ReputationValue> repVals = new ArrayList<ReputationValue>();
		for(Metric metric : repAlg.getUsesMetrics()) {
			ReputationValue repVal = new ReputationValue();
			repVal.setOwner(entity);
			repVal.setObtainedBy(repAlg);
			repVal.setTimeStamp(timestamp);
			String collectionId = repAlg.getResource()+timestamp.toString();
			repVal.setCollectionIdentifier(collectionId);
			List<Object> values = new ArrayList<Object>();
			for(ImportationUnit impUnit : reputationOfEntities.keySet()) {
				//Metric importedMetric = impUnit.getImportedMetric();
				for(ReputationAlgorithmImplementation importationRepAlg : 
						reputationOfEntities.get(impUnit).keySet()) {
					for(Metric importedMetric : reputationOfEntities.get(impUnit).get(
							importationRepAlg).keySet()) {
						if(importedMetric != metric && (impUnit.getMetricTransformation() == null ||
								impUnit.getMetricTransformation().getSourceMetric() !=
								importedMetric || impUnit.getMetricTransformation(
								).getDestinationMetric() != metric)) { //Not possible conversion
							continue;
						}
						List<Object> partialValues = reputationOfEntities.get(impUnit).get(
								importationRepAlg).get(importedMetric);
						if(partialValues == null) {
							continue;
						}
						//Find mapping and trust to add degradation
						Double mapping = 1.0;
						for(MetricMapping metricMapping : metricMappings) {
							if(metricMapping.getImportedMetric() == importedMetric &&
									metricMapping.getResultMetric() == metric) {
								mapping = metricMapping.getValue();
								break;
							}
						}
						for(Object partialValue : partialValues) {
							//Transform value from sourceMetric to destinationMetric
							if(importedMetric != metric) {
								partialValue = impUnit.getMetricTransformation(
										).tranformation(partialValue, false);
							}
							if(partialValue == null) {
								continue;
							}
							partialValue = degradeImportationValue(metric,partialValue,
									mapping,impUnit.getTrust());
							values.add(partialValue);
						}
					}
				}
			}
			if(values == null || values.isEmpty()) {				
				continue;
			}
			if(!addValuesToRepEvalInsideRepVal(repAlg, repVal, metric, entity, collectionId, values)) {
				continue;
			}			
			repVals.add(repVal);
		}
		return repVals;
	}
	
	static public boolean addValuesToRepEvalInsideRepVal(ReputationAlgorithmImplementation repAlg,
			ReputationValue repVal, Metric exportedMetric, Entity entity, String collectionId,
			List<Object> values) throws Exception {
		if(values == null || values.isEmpty()) {
			return false;
		}					
		if(repAlg.getResultCollectionType() != null && 
				repAlg.getResultCollectionType() != CollectionType.LIST) {
			Object totalReputation = null;			
			if(values.size() > 1) {
				if(!ModelException.throwException(ModelException.REPUTATIONALGORITHM_COLLECTION,
					"There is a valueReputation list when collectingSystem(resource:" +
					repAlg.getResource()+") defines a type of UNIQUE value.")) {
					return false;
				}
				ModelException.sendMessage(ModelException.WARNING,"The chosen method" +
						"to solve problem is to add all the reputationValues in one unique");
				if(exportedMetric.getScale() == null) {
					ModelException.throwException(ModelException.SCALE_SUM, "Scale of" +
							" the Metric(resource:"+exportedMetric.getResource()+") is not set so"
							+" we cannot sum more than one reputationValues(size:"+values.size()
							+") to build reputationValue of ReputationImporter(resource:"+
							repAlg.getResource()+")");
					return false;
				}
				for(Object singleReputation : values) {
					totalReputation = exportedMetric.getScale().sumValues(
							totalReputation, singleReputation);
				}
			} else {
				totalReputation = values.get(0);
			}
			if(totalReputation == null) {
				return false;
			}
			ReputationEvaluation repEval = new ReputationEvaluation();
			repEval.setHasMetric(exportedMetric);
			repEval.setTarget(entity);
			repEval.setCollectionIdentifier(collectionId);
			repEval.setValue(totalReputation);
			repVal.addHasEvaluations(repEval);
		} else {
			for(Object singleValue : values) {
				if(singleValue == null) {
					continue;
				}
				ReputationEvaluation repEval = new ReputationEvaluation();
				repEval.setHasMetric(exportedMetric);
				repEval.setTarget(entity);
				repEval.setCollectionIdentifier(collectionId);
				repEval.setValue(singleValue);
				repVal.addHasEvaluations(repEval);
			}											
		}
		return true;
	}
	
	/**
	 * Join all imported reputations from a reputationImporter. By each importationUnit and
	 * inside, by each CollectingSystem (represented by its root ReputationAlgorithm), it
	 * makes a ReputationValue object. In this object, by each metric used in the 
	 * importation, reputationEvaluations are added according to addValuesToRepEvalInsideRepVal
	 * method
	 * @param reputationOfEntities
	 * @param metricMappings
	 * @param entity
	 * @param community
	 * @param timestamp
	 * @return
	 * @throws Exception
	 */
	static public List<ReputationValue> joinAllImportedReputationsByDefault(Map<ImportationUnit,
			Map<ReputationAlgorithmImplementation,Map<Metric,List<Object>>>> reputationOfEntities,
			List<MetricMapping> metricMappings, Entity entity, Community community, 
			Date timestamp) throws Exception {		
		List<ReputationValue> reputationValues = new ArrayList<ReputationValue>();
		for(ImportationUnit impUnit : reputationOfEntities.keySet()) {
			//Metric importedMetric = impUnit.getImportedMetric();
			for(ReputationAlgorithmImplementation repAlg :
					reputationOfEntities.get(impUnit).keySet()) {
				boolean isAlreadyInside = false;
				for(ReputationValue repVa : reputationValues) {
					if(repVa.getObtainedBy() == repAlg) {
						isAlreadyInside = true;
						break;
					}
				}				
				if(isAlreadyInside) {
					try {
						ModelException.sendMessage(ModelException.INFO, "importationUnit(resource:"
								+impUnit.getResource()+") uses more than once the same reputationAlgorithm"
								+"(resource:"+repAlg.getResource()+") in importation procedement" +
								" of the comunity(resource:"+community.getResource()+")");
					} catch (Exception e) {}
					continue;
				}
				ReputationValue repVal = new ReputationValue();
				repVal.setOwner(entity);
				repVal.setObtainedBy(repAlg);
				repVal.setTimeStamp(timestamp);
				String collectionId = repAlg.getResource()+timestamp.toString();
				repVal.setCollectionIdentifier(collectionId);
				for(Metric exportedMetric : reputationOfEntities.get(
						impUnit).get(repAlg).keySet()) {
					List<Object> values = reputationOfEntities.get(
							impUnit).get(repAlg).get(exportedMetric);
					addValuesToRepEvalInsideRepVal(repAlg,repVal,exportedMetric,entity,
							collectionId,values);									
				}
				if(repVal.getHasEvaluations() == null || repVal.getHasEvaluations().isEmpty()) {
					continue;
				}
				reputationValues.add(repVal);				
			}
		}
		return reputationValues;
	}
	
	static public Object degradeImportationValue(Metric metric, Object value,Double mapping,
			TrustBetweenCommunities tbc) {
		Double trustAndMapping = mapping;
		if(tbc != null) {
			trustAndMapping *= getFinalValue(tbc);
		}
		if(trustAndMapping == 1.0) {
			return value;
		}
		if(metric.getScale() == null) {
			try {
				ModelException.throwException(ModelException.SCALE_SUM, "Scale of" +
						" the Metric(resource:is not set so we cannot add trust("
						+trustAndMapping+") to reputation value:"+value);
			} catch (Exception e) {}
			return null;
		}
		return metric.getScale().adaptToScale(metric.getScale().
				addTrust(value, trustAndMapping));
	}
	
	static public Double getFinalValue(TrustBetweenCommunities tbc) {
		Double returnValue = 1.0;
		if(tbc.getTrustProvidedBy() != null) {
			for(TrustBetweenCommunities tbcInside : tbc.getTrustProvidedBy()) {
				returnValue *= getFinalValue(tbcInside);
			}
		}
		if(tbc.getValue() != null) {
			return returnValue * tbc.getValue();
		}
		return returnValue;
	}
	
	class ReputationsAndAlgorithmObjects {
		private Map<Metric,Object> reputations;
		private Object algorithmObject;
		
		public Map<Metric, Object> getReputations() {
			return reputations;
		}
		public void setReputations(Map<Metric, Object> reputations) {
			this.reputations = reputations;
		}
		public Object getAlgorithmObject() {
			return algorithmObject;
		}
		public void setAlgorithmObject(Object algorithmObject) {
			this.algorithmObject = algorithmObject;
		}		
	}
		
	@SuppressWarnings("unchecked")
	static public Map<ReputationAlgorithmImplementation,Map<Metric,List<Object>>> 
			extractReputationFromEntity(ReputationImporterBehaviour repImp, 
			ObjectWithIdentifier systems,ImportationUnit importationUnit, 
			Entity entity,List<Object> algorithmsImpl,Map<ReputationAlgorithmImplementation,
			Map<Metric,List<Object>>> reputations) throws Exception {		
		if(systems.getObject() instanceof List) {
			List<ObjectWithIdentifier> sortedObjectList = 
				(List<ObjectWithIdentifier>)systems.getObject();
			for(int i = 0; i < sortedObjectList.size(); i++) {
				extractReputationFromEntity(repImp, sortedObjectList.get(i),importationUnit,
					entity, algorithmsImpl, reputations);				
			}
		} else if(systems.getObject() instanceof CollectingSystemBehaviour) {
			CollectingSystemBehaviour system = (CollectingSystemBehaviour)systems.getObject();
			//It was checked before
			/*for(Community community : entity.getIdentificatorInCommunities().keySet()) {
				if(community != importationUnit.getImportedCommunity()) {
					continue;
				}
			}*/
			try {
				Map<Metric,List<Object>> reputation = null;
				if(system.getRoot().getObjectClass() != null) {
					String className = system.getRoot().getObjectClass();
					Class classImpl = Class.forName(className);
					//Erase previous objects that are instances of the class of this new object
					for(int i = 0; i < algorithmsImpl.size(); i++) {
						Object algorithmObj = algorithmsImpl.get(i);
						if(!classImpl.isInstance(algorithmObj)) {
							continue;
						}
						ModelException.sendMessage(ModelException.WARNING, 
							"Previous object(class:"+algorithmObj.getClass()+" was instance" +
							" of the class of a new object(class"+classImpl+")");
						algorithmsImpl.remove(i);
						i--;												
					}
					algorithmsImpl.add(classImpl.newInstance());
				}				
				if(system.getRoot().getAlgorithmPath() != null) {
					reputation = executeAlgorithm(algorithmsImpl,repImp, importationUnit,system,
							entity, system.getRoot().getAlgorithmPath());
				} else {
					reputation = ExtractReputationByDefaultMethods(
							repImp, importationUnit, system, entity);					
				}
				//Add new imported Reputation to stored imported Reputation
				if(reputation != null && reputation.size() > 0) {
					if(reputations.containsKey(system.getRoot())) {
						ModelException.sendMessage(ModelException.ERROR, 
							"CollectingSystem(resource:"+system.getRoot().getResource()+
							") already exists in previous imported reputation from the same"
							+"ImportationUnit(res:"+importationUnit.getResource()+"in the"
							+"same ReputationImporter(res:"+repImp.getRoot().getResource()+")");
						for(Metric metric : reputation.keySet()) {
							if(reputations.get(system.getRoot()).containsKey(metric)) {
								reputations.get(system.getRoot()).get(metric).addAll(
										reputation.get(metric));
							} else {
								reputations.get(system.getRoot()).put(metric,reputation.get(metric));
							}
						}
					} else {
						reputations.put(system.getRoot(), reputation);
					}					
				}				
			} catch(MalformedURLException e) {
				ModelException.throwException(ModelException.EXTRACT_REPUTATION_URL, 
						e.getMessage()+"("+e.getClass()+")");
			}
		} else {
			ModelException.throwException(ModelException.OBJECT_WITH_IDENTIFIER_NOT_KNOWN, 
					"BugSystem: ObjectWithIdentifier is a not expected class instance:" +
					"(class:"+systems.getClass()+")");
		}
		return reputations;
	}
	
	@SuppressWarnings("unchecked")
	static public Map<Metric, List<Object>> executeAlgorithm(List<Object> algorithmsImpl, 
			ReputationImporterBehaviour repImp, ImportationUnit importatationUnit, 
			CollectingSystemBehaviour system, Entity entity, String methodName) throws Exception {
		Class<?> classMethodImpl = Class.forName(methodName.substring(
				0,methodName.lastIndexOf(".")));
		if(algorithmsImpl == null) {
			ModelException.throwException(ModelException.ALGORITHM_IMPLEMENTATION_NOT_FOUND,
					"algorithm Implementation object list cannot be null");
			return null;
		}
		for(Object algorithmImpl : algorithmsImpl) {
			if(!classMethodImpl.isInstance(algorithmImpl)) {
				continue;
			}
			String classMethodName = methodName.substring(methodName.lastIndexOf(".")+1);
			try {
				Method method = algorithmImpl.getClass().getMethod(classMethodName,
						ReputationImporterBehaviour.class,ImportationUnit.class, 
						CollectingSystemBehaviour.class, Entity.class);
				return (Map<Metric, List<Object>>) method.invoke(algorithmImpl, repImp,
						importatationUnit, system, entity);
			} catch(NoSuchMethodException e) {
				ModelException.throwException(ModelException.ALGORITHM_IMPLEMENTATION_NOT_FOUND,
						"Class "+classMethodImpl+" does not have the accesible method:"+
						classMethodName+" with the arguments type (ImportationUnit.class," +
						" CollectingSystemBehaviour.class, Entity.class");
			}
		}
		ModelException.throwException(ModelException.ALGORITHM_IMPLEMENTATION_NOT_FOUND,
				"algorithm Implementation object list does not" +
				" contain any instance of "+classMethodImpl);
		return null;
	}
	
	static public String getEntityURL(URI uri, Community community, 
			EntityIdentifier id) throws Exception {
		if(id.getUrl() != null) {
			return id.getUrl();
		} else {
			if(uri.toString().endsWith("$User_Profile_Ending") || uri.toString().endsWith(
					"$User_Profile_History_Posts")) {
				List<String> accounts = Scrapper.UserAccounts (id.getName(),
						community.getDomainName(), false);
				if(accounts != null && !accounts.isEmpty() && accounts.get(0) != null) {
					//Update the obtained url in the entity identifier
					id.setUrl(accounts.get(0));
					return accounts.get(0);
				}
			} else {
				ModelException.throwException(ModelException.NOT_URI_KNOWN,
						"Not uri known:"+uri+" in entity identifier" +
						"(resource:"+id.getResource()+")"); 
			}
		}
		ModelException.throwException(ModelException.NOT_URI_FOUND,
				"Not url found for uriFormat:"+uri+" in entity identifier" +
				"(resource:"+id.getResource()+")");
		return null;
	}
	
	static public Map<Metric,List<Object>> ExtractReputationByDefaultMethods(
			ReputationImporterBehaviour repImp, ImportationUnit importationUnit, 
			CollectingSystemBehaviour system, Entity entity) throws Exception {
		String element = null;
		if(system.getUriFormat().toString().endsWith("$User_Profile_Ending")) {
			element = "http://purl.org/dc/elements/1.1/Reputacion";
		} else if(system.getUriFormat().toString().endsWith("$User_Profile_Rank")) {
			element = "http://purl.org/dc/elements/1.1/Ranking";
		} else if(system.getUriFormat().toString().endsWith("$User_Profile_Kudo")) {
			element = "http://purl.org/dc/elements/1.1/ReputacionOhloh";
		} else {
			ModelException.throwException(ModelException.NOT_URI_KNOWN,
					"Not uri procedement known:"+system.getUriFormat()+" in CollectingSystem(" +
					"resource:"+system.getRoot().getResource()+") to import a Unit(resource:"+
					importationUnit.getResource()+")");
		}
		String url = getEntityURL(system.getUriFormat(),importationUnit.getImportedCommunity(),
				entity.getIdentificatorInCommunities().get(importationUnit.getImportedCommunity()));
		if(url == null) {
			return null;
		}			
		Map<Metric,List<Object>> reputations = new HashMap<Metric,List<Object>>();    	
		if(system.getRoot().getName().equals("Scrappy")) { //Default method for Scrappy System
			ScrappyExecutor scrappy = new ScrappyExecutor();
			String scrappy_dump = scrappy.execute(url); //"1"
			try {
				JSONArray array = (JSONArray) JSONSerializer.toJSON(scrappy_dump);
				for(int j=0; j < array.size(); j++){
	            	JSONObject dumpObject = array.getJSONObject(j);
	            	Map<Metric,List<Object>> singleReputation = ExtractionAlgorithm(repImp,
	            			importationUnit,dumpObject,system, element);
	            	if(singleReputation != null) {
	            		for(Metric metric : singleReputation.keySet()) {
	            			if(reputations.containsKey(metric)) {
	            				reputations.get(metric).addAll(singleReputation.get(metric));
	            			} else {
	            				reputations.put(metric,singleReputation.get(metric));
	            			}
	            		}
	            	}
				}
			} catch(net.sf.json.JSONException e) {
				e.printStackTrace();
				ModelException.throwException(ModelException.SCRAPPY_ERROR, "Scrappy error:"+
						"Invalid JSON String from url "+url+":\n"+scrappy_dump);				
			}			
		} else if(system.getRoot().getName().equals("Opal")) { //Default method for Opal System
			String scrappyDump = Ejecutor.executeScrappy(url, "0");
			JSONArray array = (JSONArray) JSONSerializer.toJSON(scrappyDump);
			JSONObject objeto_dump = array.getJSONObject(0);
			Double opalSum = 0.0;
			if (objeto_dump.has("http://purl.org/dc/elements/1.1/Posts")){
	    		JSONArray array_objeto_post = objeto_dump.getJSONArray(
	    				"http://purl.org/dc/elements/1.1/Posts");
	    		for(int j=0;j<array_objeto_post.size();j++){
	    			JSONObject objeto_array_post = array_objeto_post.getJSONObject(j);
	    			JSONArray array_postText = objeto_array_post.getJSONArray(
    						"http://purl.org/dc/elements/1.1/PostText");
	    			String postText = array_postText.getString(0);
	        		String opal_xml = null;
	        		try {
		        		opal_xml = Scrapper.opal_parser(postText);
		        		double opalDouble = Double.parseDouble(opal_xml);
		        		if((opalDouble > 1) || (opalDouble < -1)){
		        			opalDouble = 0;
		        		}
		        		opalSum += opalDouble;
				        ModelException.sendMessage(ModelException.INFO,
				        		"OPAL One Element Score: "+ opalDouble);
	        		} catch (NumberFormatException e) {
	        			ModelException.throwException(ModelException.OPAL_ERROR, "Opal error:"+
	        					"ERROR: OPAL doest not return a numeric" +
	        					" value for text of size:"+opal_xml);
	        		}
	    		}
			}			
	        if(opalSum != null) {
		        reputations = new HashMap<Metric,List<Object>>();
		        List<Object> reputationValue = new ArrayList<Object>();
		        reputationValue.add(opalSum);
		        Metric metric = findExportedMetric(system, importationUnit, repImp);
				if(metric == null) {
					return reputations;
				}
		        reputations.put(metric,reputationValue);
            	ModelException.sendMessage(ModelException.INFO,
            			"Reputation by OPAL: " + opalSum);
	        }
		} else {
			ModelException.throwException(ModelException.COLLECTINGSYSTEM_NOT_KNOWN,
					"Not SystemName procedement known:" + system.getRoot().getName()+
					" in CollectingSystem(resource:"+system.getRoot().getResource()+") to import" +
					" a Unit(resource:"+importationUnit.getResource()+")");
		}		
		return reputations;
    }
	
	public static Metric findExportedMetric(CollectingSystemBehaviour system, ImportationUnit
			impUnit, ReputationImporterBehaviour repImp) throws Exception {
		if(system.getRoot().getUsesMetrics() == null) {
			if(impUnit.getMetricTransformation() == null) {
				if(repImp.getRoot().getUsesMetrics() == null) {
					ModelException.throwException(ModelException.SCALECORRELATION,
						"Cannot apply default extraction algorithm if you do not define" +
						" any usesMetric in collectingSystem(resource:"+
						system.getRoot().getResource()+"), MetricTransformation"
						+"(and inside a sourceMetric) or usesMetric in reputationImporter(" +
						"resource:"+repImp.getRoot().getResource()+")");
				} else if(repImp.getRoot().getUsesMetrics().size() != 1) {
					ModelException.throwException(ModelException.SCALECORRELATION,
						"Cannot apply default extraction algorithm if you do not define" +
						" usesMetric in the collectingSystem(resource:"+system.getRoot(
						).getResource()+") and more than one usesMetric properties "+
						"in reputationImporter(resource:"+repImp.getRoot().getResource()+
						") that include the importationUnit(resource:"+
						impUnit.getResource()+")");
				} else {
					return repImp.getRoot().getUsesMetrics().get(0);					
				}
			} else if(impUnit.getMetricTransformation().getSourceMetric() != null) {
				return impUnit.getMetricTransformation().getSourceMetric();				
			}
		} else if(system.getRoot().getUsesMetrics().size() != 1) {
			ModelException.throwException(ModelException.SCALECORRELATION,
					"Cannot apply default extraction algorithm if you define more than" +
					"one usesMetric properties in CollectingSystem(resource:"+
					system.getRoot().getResource()+") inside importationUnit(resource:"+
					impUnit.getResource()+")");
		} else {
			return system.getRoot().getUsesMetrics().get(0);
		}
		return null;
	}
	
	private static Map<Metric,List<Object>> ExtractionAlgorithm(
			ReputationImporterBehaviour repImp, ImportationUnit impUnit,JSONObject objeto, 
			CollectingSystemBehaviour system, String element) throws Exception {		
		Map<Metric,List<Object>> reputation = new HashMap<Metric,List<Object>>();
		URI uri = system.getUriFormat();
    	if(uri.toString().endsWith("$User_Profile_Ending") ||
    			uri.toString().endsWith("$User_Profile_Rank") ||
    			uri.toString().endsWith("$User_Profile_Kudo")) {
    		if (!objeto.has("http://purl.org/dc/elements/1.1/Usuario")){
    			ModelException.throwException(ModelException.SCRAPPY_ERROR,
    					"Scrappy template from uri: "+uri+" and element:"+element+
    					" is out of date or incorrect. " +
						"There is not http://purl.org/dc/elements/1.1/Usuario element");
    		}
			JSONArray array_usuarios = objeto.getJSONArray(
					"http://purl.org/dc/elements/1.1/Usuario");
			JSONObject objeto_usuarios = array_usuarios.getJSONObject(0);
			String reputationString = null;
			if(uri.toString().endsWith("$User_Profile_Rank")) {
				JSONArray array_user = objeto_usuarios.getJSONArray(element);
				reputationString = array_user.getString(0).replace(
		        		"\n","").replace(" ", "").replace(".", "");		       
			} else {
				if(objeto_usuarios.has(element)){
			        JSONArray array_user = objeto_usuarios.getJSONArray(element);
			        reputationString = array_user.getString(0);	
		        } else {
		        	ModelException.throwException(ModelException.ELEMENT_FROM_URI_NOT_FOUND,
		        			"Reputation element:"+element+" not found from uri:"+uri);
		        	return reputation;
		        }
			}
			ModelException.sendMessage(ModelException.INFO," Reputation:"+ reputationString);
			//Adapt value to usesMetric/sourceMetric
			Metric metric = findExportedMetric(system, impUnit, repImp);
			if(metric == null) {
				return reputation;
			}
			Object reputationValue = defaultAddaptToMetric(metric, reputationString);
			if(reputationValue != null) {
				putReputationValueInMap(reputation, metric, reputationValue);
			}			
			ModelException.sendMessage(ModelException.INFO," Reputation adapted:"+ reputationValue);
		} else if(uri.toString().endsWith("$User_Profile_Posts")) {
			ModelException.throwException(ModelException.URI_NOT_SUPPORTED,
					"$User_Profile_Posts uriFormat is not longer supported");			
		} else {
			ModelException.throwException(ModelException.NOT_URI_KNOWN,
					"Not uri procedement known:"+uri);
		}
    	return reputation;
	}
	
	static public void putReputationValueInMap(Map<Metric,List<Object>> reputations,
			Metric metric, Object value) {
		if(reputations.containsKey(metric)) {
			reputations.get(metric).add(value);
		} else {
			List<Object> values = new ArrayList<Object>();
			values.add(value);
			reputations.put(metric,values);
		}
	}
	
	public static Object defaultAddaptToMetric(Metric metric, Object value) {
		if(metric.getScale() == null) {
			return value;
		}
		if(metric.getScale() instanceof NumericScale) {
			if(value instanceof String) {
				value = metric.getScale().adaptToScale(Double.parseDouble((String)value));
			} else if(value instanceof Double) {
				value = metric.getScale().adaptToScale(value);
			} else {
				try {
					if(!ModelException.throwException(ModelException.SCALECORRELATION, 
							"Cannot adapt value("+value+") to this Metric(resource:"+
							metric.getResource()+")")) {
						return null;
					}
				} catch (Exception e) {	}
			}
		}
		return value;
	}
	
	/*private static Map<Metric,Object> Reputation(JSONObject objeto, String cuenta) throws IOException {
    	Map<Metric,String> reputation = new HashMap<Metric,String>();
    	Map<Metric,Object> reputations = null;
    	String userName = null;
		if (objeto.has("http://purl.org/dc/elements/1.1/Usuario")){
			JSONArray array_usuarios = objeto.getJSONArray("http://purl.org/dc/elements/1.1/Usuario");
			JSONObject objeto_usuarios = array_usuarios.getJSONObject(0);
        	System.out.println("Informacion de usuario:");
	        //Vamos sacando la información relevante, que son objetos, con un array, dentro de objeto_respuestas
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/Nombre")){
		        JSONArray array_user = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/Nombre");
		        userName = array_user.getString(0);
		        System.out.println("  Nombre: " + userName);
	        }
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/Reputacion")){
		        JSONArray array_user = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/Reputacion");		        
		        reputation.put(null,array_user.getString(0));
		        System.out.println("  Reputacion: " + reputation);
	        }
	        //Ohloh-----------------------------------------------------------------------------------------------------
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/Ranking")){
		        JSONArray array_user = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/Ranking");
		        String ranking = array_user.getString(0).replace("\n","").replace(" ", "").replace(".", "");
		        System.out.println("  Ranking:" + ranking);		        
		        try {
					double posicion = Double.parseDouble(ranking.substring(0,ranking.indexOf("of")));
					double usuariosTotales = Double.parseDouble(ranking.substring(ranking.indexOf("of")+2,ranking.length()));
					//puntuation = (Math.log10(usuariosTotales/posicion))/(Math.log10(Math.pow(usuariosTotales, 0.1)));
					reputations = new HashMap<Metric,Object>();
					reputations.put(GlobalModel.getMetrics().get("ohlohRankMetric"),
							usuariosTotales - posicion);					
					//System.out.println("  Ranking Score:" + puntuation);
		        } catch (NumberFormatException e) {
					System.out.println("Error: Reputation Puntuation cannot be parsed to a double :"+
							ranking.substring(0,ranking.indexOf("of"))+" of "+
							ranking.substring(ranking.indexOf("of")+2,ranking.length()));
		        }
	        }else if (!objeto_usuarios.has("http://purl.org/dc/elements/1.1/Ranking") && 
	        		objeto_usuarios.has("http://purl.org/dc/elements/1.1/ReputacionOhloh")){
	        	double reputationWithoutRank = 200000;
				reputations = new HashMap<Metric,Object>();
				reputations.put(GlobalModel.getMetrics().get("ohlohRankMetric"), reputationWithoutRank);
	        }
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/ReputacionOhloh")){
		        JSONArray array_user = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/ReputacionOhloh");
		        System.out.println("  Ohloh reputation:"+array_user.getString(0));
		        reputation.put(GlobalModel.getMetrics().get("ohlohKudoMetric"),
		        		array_user.getString(0));
		        //reputation = (Double.parseDouble(array_user.getString(0))*puntuation) + "";
		        //System.out.println("  Reputacion: " + reputation);
	        }
	        //----------------------------------------------------------------------------------------------------------
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/MiembroDesde")){
		        JSONArray array_miembro = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/MiembroDesde");
		        String Miembro = array_miembro.getString(0);
		        System.out.println("  Miembro desde: " + Miembro);
	        }
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/VistoUltimaVez")){
		        JSONArray array_visto = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/VistoUltimaVez");
		        String Visto = array_visto.getString(0);
		        System.out.println("  Visto ultima vez: " + Visto);
	        }
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/PaginaWeb")){
		        JSONArray array_web = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/PaginaWeb");
		        String Web = array_web.getString(0);
		        System.out.println("  Pagina web: " + Web);
	        }
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/Localizacion")){
		        //JSONArray array_local = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/Localizacion");
		        //String Localizacion = array_local.getString(0);
		        //System.out.println("\nLocalización: " + Localizacion);
	        }
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/Edad")){
		        JSONArray array_edad = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/Edad");
		        String Edad = array_edad.getString(0);
		        if(Edad.contains(" ")){
		        	Edad.replaceAll(" ", "");
		        }
		        //System.out.println("\nEdad: " + Edad);
	        }
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/Descripcion")){
		        //JSONArray array_descript = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/Descripcion");
		        //String Descripcion = array_descript.getString(0);
		        //System.out.println("  Descripcion: " + Descripcion);
	        }
			if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/PreguntasUsuario")){
				JSONArray array_pregunta = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/PreguntasUsuario");
				JSONObject objeto_pregunta = array_pregunta.getJSONObject(0);
				if (objeto_pregunta.has("http://purl.org/dc/elements/1.1/NumeroPreguntas")){
					JSONArray array_numero = objeto_pregunta.getJSONArray("http://purl.org/dc/elements/1.1/NumeroPreguntas");
					int numero = array_numero.getInt(0);
					System.out.println("  Numero de preguntas: " + numero);
				}
				if (objeto_pregunta.has("http://purl.org/dc/elements/1.1/InformacionPreguntas")){
					JSONArray array_info = objeto_pregunta.getJSONArray("http://purl.org/dc/elements/1.1/InformacionPreguntas");
					for (int i=0;i<array_info.size();i++){
						JSONObject objeto_info = array_info.getJSONObject(i);
						if (objeto_info.has("http://purl.org/dc/elements/1.1/TituloPregunta")){
							//JSONArray array_titulo = objeto_info.getJSONArray("http://purl.org/dc/elements/1.1/TituloPregunta");
							//String titulo = array_titulo.getString(0);
							//System.out.println("    Titulo: " + titulo);
						}
						if (objeto_info.has("http://purl.org/dc/elements/1.1/URL")){
							JSONArray array_url = objeto_info.getJSONArray("http://purl.org/dc/elements/1.1/URL");
							String url = array_url.getString(0);
							System.out.println("    URL: " + url);
							//Launch new thread for each question with zero level!
							//new Json("0"+cuenta+url).start();
						}	
					}
				}	
			}
		}
		if(objeto.has("http://purl.org/dc/elements/1.1/RespuestasUsuario")){
			JSONArray array_respuestas = objeto.getJSONArray("http://purl.org/dc/elements/1.1/RespuestasUsuario");
			JSONObject objeto_respuestas = array_respuestas.getJSONObject(0);
			if (objeto_respuestas.has("http://purl.org/dc/elements/1.1/NumeroRespuestas")){
				JSONArray array_numero = objeto_respuestas.getJSONArray("http://purl.org/dc/elements/1.1/NumeroRespuestas");
				int numero = array_numero.getInt(0);
				System.out.println("  Numero de respuestas: " + numero);
			}
			if (objeto_respuestas.has("http://purl.org/dc/elements/1.1/InformacionRespuestas")){
				JSONArray array_info = objeto_respuestas.getJSONArray("http://purl.org/dc/elements/1.1/InformacionRespuestas");
				for (int i=0;i<array_info.size();i++){
					JSONObject objeto_info = array_info.getJSONObject(i);
					if (objeto_info.has("http://purl.org/dc/elements/1.1/TituloRespuesta")){
						//JSONArray array_titulo = objeto_info.getJSONArray("http://purl.org/dc/elements/1.1/TituloRespuesta");
						//String titulo = array_titulo.getString(0);
						//System.out.println("    Titulo ultimas resp:\n" + titulo);
					}
					if (objeto_info.has("http://purl.org/dc/elements/1.1/URL")){
						JSONArray array_url = objeto_info.getJSONArray("http://purl.org/dc/elements/1.1/URL");
						String url = array_url.getString(0);
						System.out.println("    URL: " + url);
						//Launch new thread for each last answer with zero level!						
						//new Json("0"+cuenta+url).start();
					}
				}
			}			
		}
		//sla.ckers.org && elhacker.net----------------------------------------------------------------------------
		if (cuenta.contains("sla.ckers.org") || cuenta.contains("elhacker.net")){
			if (objeto.has("http://purl.org/dc/elements/1.1/Usuario")){
				JSONArray array_usuarios = objeto.getJSONArray("http://purl.org/dc/elements/1.1/Usuario");
				JSONObject objeto_usuarios = array_usuarios.getJSONObject(0);
	        	//System.out.println("Informacion de usuario:");
		        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/Nombre")){
		        	//JSONArray array_user = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/Nombre");
		        	//String Nombre = array_user.getString(0);
			        //System.out.println("  Nombre: " + Nombre);
		        }
		        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/Posts")){
			        JSONArray array_user = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/Posts");
			        String posts = array_user.getString(0);
			        System.out.println("  Posts: " + posts);
		        }
		        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/URLPosts")){
			        JSONArray array_user = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/URLPosts");
			        String urlPosts = array_user.getString(0);
			        System.out.println("  URLPosts: " + urlPosts);
			        String scrappy_dump = Ejecutor.executeScrappy(urlPosts, "0");
					JSONArray array = (JSONArray) JSONSerializer.toJSON(scrappy_dump); 
		            JSONObject objeto_dump = array.getJSONObject(0);
			        int totalPages = 1;
			        if(objeto_dump.has("http://purl.org/dc/elements/1.1/TotalPages")){
			        	JSONArray number = objeto_dump.getJSONArray("http://purl.org/dc/elements/1.1/TotalPages");
			        	totalPages = Integer.parseInt(number.getString(0));
			        }
			        
			        OpalExecutorService opalExec = new OpalExecutorService(userName);
			        int postsCount = 0;
			        System.out.println("  Maximum posts: " + Property.getPOSTS_NUMBER());
		            
		            for (int j = 1; j <= totalPages; j++){
		            	
            			if (postsCount >= Property.getPOSTS_NUMBER()) {
            				break;
            			}		            	
		            	if (j > 1){
		            		urlPosts += ",page=" + j;
		            		scrappy_dump = Ejecutor.executeScrappy(urlPosts, "0");
		            		array = (JSONArray) JSONSerializer.toJSON(scrappy_dump);
		            		objeto_dump = array.getJSONObject(0);
		            	}
		            	if (objeto_dump.has("http://purl.org/dc/elements/1.1/Posts")){
		            		JSONArray array_objeto_post = objeto_dump.getJSONArray("http://purl.org/dc/elements/1.1/Posts");
		            		System.out.println("  Calculate reputation over "+array_objeto_post.size()+" posts");
		            		for(int i=0;i<array_objeto_post.size();i++){	
		            			if (postsCount == Property.getPOSTS_NUMBER())
		            				break;
				            	JSONObject objeto_array_post = array_objeto_post.getJSONObject(i);
					        	//System.out.println("Informacion de post:");
					        	if(objeto_array_post.has("http://purl.org/dc/elements/1.1/PostURL")){
					        		JSONArray array_postURL = objeto_array_post.getJSONArray(
					        				"http://purl.org/dc/elements/1.1/PostURL");
					        		final String postURL = array_postURL.getString(0);
							        System.out.println("    PostURL: " + postURL);
							        opalExec.execute(postURL);					
							        postsCount++; //Depurar Execute: scrappy -f ejson -g http://sla.ckers.org/forum/read.php?16,27618,27683#msg-27683 -l 0 
							        			  //Limitar caracteres a la entrada curl (en el caso de q falle curl).
					        	}				        	
						        if(objeto_array_post.has("http://purl.org/dc/elements/1.1/PostName")){
						        	//JSONArray array_postName = objeto_array_post.getJSONArray("http://purl.org/dc/elements/1.1/PostName");
						        	//String postName = array_postName.getString(0);
							        //System.out.println("  PostName: " + postName);
						        }
						        if(objeto_array_post.has("http://purl.org/dc/elements/1.1/PostFecha")){
						        	//JSONArray array_fecha = objeto_array_post.getJSONArray("http://purl.org/dc/elements/1.1/PostFecha");
						        	//String postsFecha = array_fecha.getString(0);
							        //System.out.println("  Fecha: " + postsFecha);
						        }
				            }
			            }
		            }
            		Double opalSum = opalExec.shutdown();
			        if(opalSum != null) {
				        reputations = new HashMap<Metric,Object>();
		            	reputations.put(GlobalModel.getMetrics().get("slackersMetric"),opalSum);
		            	System.out.println("  Slackers reputation by OPAL: " + opalSum);
			        }
		        }  	
			}
		}
		if(!reputation.isEmpty()) {
			if(reputations == null) {
				reputations = new HashMap<Metric,Object>();
			}
			for(Metric metric : reputation.keySet()) {
				try {
					reputations.put(metric, (Double)NumberFormat.getInstance(Locale.US).
							parse(reputation.get(metric)).doubleValue());
				} catch (ParseException e) {
					System.out.println("Error: Reputation Score cannot be parsed to a double: "+
							reputation.get(metric)+" over metric:"+metric);					
				}	
			}
		}
		return reputations;		
	}*/
	
	public void importOverAllEntities() throws Exception {
		for(String communityName : GlobalModel.getCommunities().keySet()) {
			importOverEntitiesToCommunity(communityName,
					GlobalModel.getEntities().keySet());
		}
	}
	
	public void importOverEntities(Set<String> entityNames) throws Exception {
		for(String communityName : GlobalModel.getCommunities().keySet()) {
			importOverEntitiesToCommunity(communityName,entityNames);
		}
	}
	
	public Map<String, Entity> importOverAllEntitiesToCommunity(
			String communityName) throws Exception {
		return importOverEntitiesToCommunity(communityName,
				GlobalModel.getEntities().keySet());		
	}	
	
	static public Set<MetricMapping> findMetricMappings (
			ReputationAlgorithmImplementation repAlg) {
		Set<MetricMapping> metricMappings = new HashSet<MetricMapping>();
		if(repAlg.getBehaviours() == null) {
			return metricMappings;
		}
		for(ReputationBehaviour behaviour : repAlg.getBehaviours()) {
			if(ReputationModelBehaviour.class.isInstance(behaviour)) {
				ReputationModelBehaviour model = (
						ReputationModelBehaviour) behaviour;
				if(model.getReputationModules() != null) {
					for(ReputationAlgorithmImplementation module : 
						model.getReputationModules()) {
						metricMappings.addAll(findMetricMappings(module));					
					}
				}
			}
			if(ReputationModuleBehaviour.class.isInstance(behaviour)) {
				ReputationModuleBehaviour module = (
						ReputationModuleBehaviour) behaviour;
				if(module.getObtainsReputationsBy() != null) {
					for(ReputationAlgorithmImplementation repAlgInside : 
						module.getObtainsReputationsBy()) {
						metricMappings.addAll(findMetricMappings(repAlgInside));
					}
				}
			}
			if(ReputationImporterBehaviour.class.isInstance(behaviour)) {
				ReputationImporterBehaviour importer = (
						ReputationImporterBehaviour) behaviour;
				if(importer.getImportsFrom() != null) {
					metricMappings.addAll(importer.getMapsMetrics());
				}
				//We do not have to search if there are more ReputationImporter in the
				//importationUnit method of collectsReputationBy because it does not
				//have sense (to import from a community does not need importations from
				//other communities inside)
			}			
		}
		return metricMappings;
	}			
	
	static public Map<ReputationImporterBehaviour,Set<ImportationUnit>> findImportationUnits (
			ReputationAlgorithmImplementation repAlg) {
		Map<ReputationImporterBehaviour,Set<ImportationUnit>> importationUnitsMap = 
				new HashMap<ReputationImporterBehaviour,Set<ImportationUnit>>();
		if(repAlg.getBehaviours() == null) {
			return importationUnitsMap;
		}
		for(ReputationBehaviour behaviour : repAlg.getBehaviours()) {
			if(ReputationModelBehaviour.class.isInstance(behaviour)) {
				ReputationModelBehaviour model = (
						ReputationModelBehaviour) behaviour;
				if(model.getReputationModules() != null) {
					for(ReputationAlgorithmImplementation module : 
						model.getReputationModules()) {
						importationUnitsMap.putAll(findImportationUnits(module));					
					}
				}
			}
			if(ReputationModuleBehaviour.class.isInstance(behaviour)) {
				ReputationModuleBehaviour module = (
						ReputationModuleBehaviour) behaviour;
				if(module.getObtainsReputationsBy() != null) {
					for(ReputationAlgorithmImplementation repAlgInside : 
						module.getObtainsReputationsBy()) {
						importationUnitsMap.putAll(findImportationUnits(repAlgInside));
					}
				}
			}
			if(ReputationImporterBehaviour.class.isInstance(behaviour)) {
				ReputationImporterBehaviour importer = (
						ReputationImporterBehaviour) behaviour;
				if(importer.getImportsFrom() != null) {
					Set<ImportationUnit> importationUnits = new HashSet<ImportationUnit>();
					importationUnits.addAll(importer.getImportsFrom());
					importationUnitsMap.put(importer, importationUnits);
				}
				//We do not have to search if there are more ReputationImporter in the
				//importationUnit method of collectsReputationBy because it does not
				//have sense (to import from a community does not need importations from
				//other communities inside)
			}			
		}
		return importationUnitsMap;		
	}
	
	public ObjectWithIdentifier findCollectingSystemsFromImportationUnit (
			ImportationUnit unit) throws Exception {
		ReputationAlgorithmImplementation repAlgInside =
			unit.getCollectsReputationBy();
		if(repAlgInside != null) {					
			ObjectWithIdentifier sortedCollectionSystemsInside =
					findCollectionSystemsInside(repAlgInside);
			if(sortedCollectionSystemsInside == null ||
					!ThereIsCollectingSystemInside(sortedCollectionSystemsInside) ) {
				ModelException.throwException(ModelException.IMPORTATION_UNIT_COLLSYSTEM_INSIDE,
						"Importation unit:\n"+unit.toString("  ")+"\nmust have" +
						" at least one collecting system that successes the filter " +
						" collecting system name");
			}
			return sortedCollectionSystemsInside;							
		}
		ModelException.throwException(ModelException.IMPORTATION_UNIT_COLLSYSTEM_INSIDE,
				"Importation unit:\n"+unit.toString("  ")+"\nmust correctly set the" +
				"collectsReputationBy property");
		return null;
	}
	
	@SuppressWarnings("unchecked")
	static public boolean ThereIsCollectingSystemInside(ObjectWithIdentifier sortedCollectionSystemsInside) {
		if(sortedCollectionSystemsInside.getObject() instanceof List) {
			List<ObjectWithIdentifier> sortedObjectList = 
				(List<ObjectWithIdentifier>)sortedCollectionSystemsInside.getObject();
			for(int i = 0; i < sortedObjectList.size(); i++) {
				if(ThereIsCollectingSystemInside(sortedObjectList.get(i))) {
					return true;
				}
			}
		} else if(sortedCollectionSystemsInside.getObject() instanceof CollectingSystemBehaviour) {
			return true;
		}
		return false;
	}
	
	class ObjectWithIdentifier {
		private Integer identifier;
		private Object object;
		ObjectWithIdentifier(Object object, Integer identifier) throws Exception {
			if(!(object instanceof List || object instanceof CollectingSystemBehaviour)) {
				throw new Exception("ERROR: Bug in system. Sorting something that is" +
						" not a List neither a CollectingSystemBehaviour");
			}
			this.object = object;
			this.identifier = identifier;
		}
		public Integer getIdentifier() {
			return identifier;
		}
		public Object getObject() {
			return object;
		}				
	}
	
	public void sortByStepIdentifier(List<ObjectWithIdentifier> sortedList, 
			CollectingSystemBehaviour behaviour, Integer stepIdentifier) throws Exception {
		boolean isIn = false;
		ObjectWithIdentifier behaviourWithIdentifier = new ObjectWithIdentifier(
				behaviour, stepIdentifier);
		for(int i = 0; i < sortedList.size(); i++) {
			ObjectWithIdentifier object = sortedList.get(i);
			Integer stepToCompare = object.getIdentifier();
			if(stepToCompare == null || stepToCompare >= stepIdentifier) {
				sortedList.add(i,behaviourWithIdentifier);
				isIn = true;
				break;
			}			
		}
		if(!isIn) {
			sortedList.add(behaviourWithIdentifier);
		}		
	}
	
	public void sortByStepIdentifier(List<ObjectWithIdentifier> sortedList, 
			ObjectWithIdentifier objectID) throws Exception {
		boolean isIn = false;
		for(int i = 0; i < sortedList.size(); i++) {
			ObjectWithIdentifier object = sortedList.get(i);
			Integer stepToCompare = object.getIdentifier();
			if(stepToCompare == null || stepToCompare >= objectID.getIdentifier()) {
				sortedList.add(i,objectID);
				isIn = true;
				break;
			}			
		}
		if(!isIn) {
			sortedList.add(objectID);
		}		
	}
		
	public ObjectWithIdentifier findCollectionSystemsInside(
			ReputationAlgorithmImplementation repAlg) throws Exception {
		Integer stepIdentifier = repAlg.getStepIdentifier();
		return setCollectingSystemsFromBehaviours(repAlg, stepIdentifier);		
	}
	
	public boolean filterColletingSystem(CollectingSystemBehaviour system,
			ReputationAlgorithmImplementation repAlg) {
		for(String name : systemIdentifierFilter) {
			if(repAlg.getName().contains(name)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Independence between behaviours:
	 *  - ReputationModel cannot link to obtainsReputationsBy so:
	 *    - ReputationAlgorithm has or hasModel properties (from ReputationModel behaviour) or
	 *      obtainsReputationsBy (object has not reputationModel behaviour)
	 *  - Object is inside importationUnit so there are no more ReputationImporter behaviour
	 *    inside because it does not have sense (to import from a community does not need
	 *    importations from other communities inside)
	 *  - From collectingAlgorithmBehaviour, we only select to the importation process, those
	 *    that are collectingSystemBehaviour (because they have a known URI_Format to work). From
	 *    them, we only select those that success the collecting system name filter
	 *  - In any object that has a collectingAlgorithm behaviour, we also search by its other
	 *    behaviours of model and modules to find collectingSystems inside.
	 * @param behaviours
	 * @param step
	 * @return
	 * @throws Exception
	 */
	public ObjectWithIdentifier setCollectingSystemsFromBehaviours(
			ReputationAlgorithmImplementation repAlg, Integer step) throws Exception {
		List<ReputationBehaviour> behaviours = repAlg.getBehaviours();
		List<ObjectWithIdentifier> collectingSystems = 
			new ArrayList<ObjectWithIdentifier>();
		for(ReputationBehaviour behaviour : behaviours) {
			if(ReputationModelBehaviour.class.isInstance(behaviour)) {
				ReputationModelBehaviour model = (
						ReputationModelBehaviour) behaviour;
				if(model.getReputationModules() != null) {
					for(ReputationAlgorithmImplementation module : 
							model.getReputationModules()) {
						ObjectWithIdentifier collectingSystemsInside = 
							findCollectionSystemsInside(module);
						if(collectingSystemsInside == null) {
							continue;
						}
						sortByStepIdentifier(collectingSystems,collectingSystemsInside);					
					}
				}
			}
			if(ReputationModuleBehaviour.class.isInstance(behaviour)) {
				ReputationModuleBehaviour module = (
						ReputationModuleBehaviour) behaviour;
				if(module.getObtainsReputationsBy() != null) {
					for(ReputationAlgorithmImplementation repAlgInside : 
						module.getObtainsReputationsBy()) {
						ObjectWithIdentifier collectingSystemsInside = 
							findCollectionSystemsInside(repAlgInside);
						if(collectingSystemsInside == null) {
							continue;
						}
						sortByStepIdentifier(collectingSystems,collectingSystemsInside);
					}
				}
			}
			//We do not have to search if there are no more ReputationImporter in the
			//importationUnit method of collectsReputationBy because it does not
			//have sense (to import from a community does not need importations from
			//other communities inside)
			/*if(ReputationImporterBehaviour.class.isInstance(behaviour)) {
				ReputationImporterBehaviour importer = (
						ReputationImporterBehaviour) behaviour;
				for(ImportationUnit unit : importer.getImportsFrom()) {
					ReputationAlgorithmImplementation repAlgInside =
						unit.getCollectsReputationBy();
					if(repAlgInside != null) {
						for(ReputationBehaviour behaviourInside : 
								repAlgInside.getBehaviours()) {
							if(CollectingAlgorithmBehaviour.class.isInstance(
									behaviourInside)) {
								CollectingAlgorithmBehaviour collecter = 
									(CollectingAlgorithmBehaviour) behaviourInside;
								for(ReputationBehaviour behaviourInsideInside : 
										collecter.getBehaviours()) {
									if(CollectingSystemBehaviour.class.isInstance(
											behaviourInsideInside)) {
										collectingSystems.add((
											CollectingSystemBehaviour) behaviourInsideInside);
									}
								}
							}
						}
					}
				}
			}*/
		}			
		//The last part
		for(ReputationBehaviour behaviour : behaviours) {
			if(!CollectingAlgorithmBehaviour.class.isInstance(behaviour)) {
				continue;
			}
			for(ReputationBehaviour behaviourInside : 
					behaviour.getBehaviours()) {
				if(!CollectingSystemBehaviour.class.isInstance(
						behaviourInside)) {
					continue;
				}
				CollectingSystemBehaviour collectingSystem = 
						(CollectingSystemBehaviour)behaviourInside;
				if(!filterColletingSystem(collectingSystem, repAlg)) {
					continue;
				}
				//Because there are not collectingSystems inside, we put directly the collectingSystem
				if(collectingSystems.isEmpty()) {
					return new ObjectWithIdentifier(collectingSystem, step);
				}
				//Because there are collectingSystems inside, we put the collectingSystem in the list
				sortByStepIdentifier(collectingSystems,collectingSystem, 0);
			}
		}
		//The repAlg does not have collectingSystem inside and 
		//its behaviours are not collectingSystemBehaviour
		if(collectingSystems.isEmpty()) {
			return null;
		}
		//There is/are collectingSystem inside (and maybe repAlg also has collectingSystemBehaviour)
		//so we have to put the List in a proper ObjectWithIdentifier object
		return new ObjectWithIdentifier(collectingSystems, step);
	}	
	
	public void importOverAllEntitiesToCommunity(
			URI commmunityURI) throws Exception {
		importOverEntitiesToCommunity(commmunityURI,
				GlobalModel.getEntities().keySet());
	}
	
	public void importOverEntitiesToCommunity(
			URI commmunityURI, Set<String> entityNames) throws Exception {
		Resource communityResource = ResourceFactory.createResource(
				commmunityURI.toString());
		Community community = (Community) reputationParser.getResourceFromCache(
				communityResource, Community.class);
		if(community == null) {
			community = reputationParser.getCommunity(
					reputationParser.getModel(), communityResource);
		}
		//Add community to GlobalModel
	}	
	
}
