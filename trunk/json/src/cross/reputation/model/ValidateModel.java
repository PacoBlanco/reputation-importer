package cross.reputation.model;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Resource;


public class ValidateModel {
	static public boolean validateScale(Scale scale) throws Exception {
		if(scale.getName() == null) {
			if(!ModelException.throwException(ModelException.SCALE,
					"name property of scale(resource:"+scale.getResource()+
					") must be set")) {
				return false;
			}
		}
		if(scale instanceof NumericScale) {
			NumericScale numericScale = (NumericScale) scale;
			if(numericScale.getMaximum() != null && 
					numericScale.getMinimum() != null) {
				if(numericScale.getStep() > (numericScale.getMaximum() - 
						numericScale.getMinimum())) {
					if(!ModelException.throwException(ModelException.NUMERIC_SCALE,
							"Step is greater than differece between" +
							"maximum and minimum")) {
						return false;
					}
				}
				if((numericScale.getMaximum() - 
						numericScale.getMinimum()) % numericScale.getStep() != 0.0) {
					ModelException.sendMessage(ModelException.WARNING,
							"Step("+numericScale.getStep()+") could not be a multiple than differece" +
							" between maximum("+numericScale.getMaximum()+") and minimum("+
							numericScale.getMinimum()+") from NumericScale(resource:"+
							numericScale.getResource()+")");
				}
			}			
		} else if(scale instanceof CategoricScale) {
			CategoricScale categoricScale = (CategoricScale) scale;
			if(categoricScale.getCategories() == null) {
				if(!ModelException.throwException(ModelException.CATEGORIC_SCALE,
						"Category list does not exist")) {
					return false;
				}
			} else if(categoricScale.getCategories().size() < 2) {
				if(!ModelException.throwException(ModelException.CATEGORIC_SCALE,
					"Category list has not enough elements")) {
					return false;
				}
			}
		}
		return true;
	}
	
	static public boolean validateCategory(Category category) throws Exception {
		if(category.getName() == null) {
			if(!ModelException.throwException(ModelException.CATEGORY,
					"Category Name does not exist")) {
				return false;
			}
		}
		return true;
	}
	
	static public boolean validateTrustBetweenCommunities(
			TrustBetweenCommunities tbc) throws Exception {
		if(tbc.getTrustProvidedBy() != null) {
			for(int i = 0; i < tbc.getTrustProvidedBy().size(); i++) {
				if(!validateTrustBetweenCommunities(tbc.getTrustProvidedBy().get(i))) {
					tbc.getTrustProvidedBy().remove(i);
					i--;
				}
			}
		} else if(tbc.getValue() == null) {
			if(!ModelException.throwException(ModelException.TRUSTBETWEENCOMMUNITIES,
					"Value or TrustProvidedBy properties must be set in " +
					"TrustBetweenCommunities(resource"+tbc.getResource()+")")) {
				return false;
			}
		} 
		if(tbc.getValue() != null && tbc.getValue() <= 0) {
			if(!ModelException.throwException(ModelException.TRUSTBETWEENCOMMUNITIES,
					"Value must be greater than 0 in " +
					"TrustBetweenCommunities(resource"+tbc.getResource()+")")) {
				return false;
			}
		} else if(tbc.getValue() != null && tbc.getValue() > 1) {
			if(!ModelException.throwException(ModelException.TRUSTBETWEENCOMMUNITIES,
					"Value must be between 0 and 1 values in " +
					"TrustBetweenCommunities(resource"+tbc.getResource()+")")) {
				return false;
			}
		}
		if(tbc instanceof CategoryMatching) {
			CategoryMatching categoryMatching = (CategoryMatching) tbc;
			if(categoryMatching.getOriginatingCategory() == null) {
				if(!ModelException.throwException(ModelException.CATEGORYMATCHING,
						"Originating category must be set in " +
						"TrustBetweenCommunities(resource"+tbc.getResource()+")")) {
					return false;
				}
			} else {
				if(!validateCategory(categoryMatching.getOriginatingCategory())) {
					categoryMatching.setOriginatingCategory(null);
				}
			}
			if(categoryMatching.getReceivingCategory() == null) {
				if(!ModelException.throwException(ModelException.CATEGORYMATCHING,
						"Receiving category must be set in " +
						"TrustBetweenCommunities(resource"+tbc.getResource()+")")) {
					return false;
				}
			} else {
				if(!validateCategory(categoryMatching.getReceivingCategory())) {
					categoryMatching.setReceivingCategory(null);
				}
			}
			if(categoryMatching.getOriginatingCategory().getName() ==
					categoryMatching.getReceivingCategory().getName()) {
				if(!ModelException.throwException(ModelException.CATEGORYMATCHING,
						"Originating and Receiving categories cannot have the same name in " +
						"TrustBetweenCommunities(resource"+tbc.getResource()+")")) {
					return false;
				}
			}
		}
		if(tbc instanceof FixedCommunitiesTrust) {
			FixedCommunitiesTrust fct = (FixedCommunitiesTrust) tbc;
			if(fct.getCommunityScored() == null) {
				if(!ModelException.throwException(ModelException.FIXEDCOMMUNITYTRUST,
						"communityScored property must be set in " +
						"TrustBetweenCommunities(resource"+tbc.getResource()+")")) {
					return false;
				}
			} else if(fct.getCommunityScorer() == null) {
				if(!ModelException.throwException(ModelException.FIXEDCOMMUNITYTRUST,
						"communityScorer property must be set in " +
						"TrustBetweenCommunities(resource"+tbc.getResource()+")")) {
					return false;
				}
			} else {
				if(fct.getCommunityScored().getName() == null) {
					if(!ModelException.throwException(ModelException.FIXEDCOMMUNITYTRUST,
							"Scored(resource:"+fct.getCommunityScored().getResource()+") " +
							"community cannot have the name null in FixedCommunityTrust" +
							"(resource:"+fct.getResource()+")")) {
						return false;
					}
				}
				if(fct.getCommunityScorer().getName() == null) {
					if(!ModelException.throwException(ModelException.FIXEDCOMMUNITYTRUST,
							"Scorer(resource:"+fct.getCommunityScorer().getResource()+") " +
							"community cannot have the name null in FixedCommunityTrust" +
							"(resource:"+fct.getResource()+")")) {
						return false;
					}
				}
				if(fct.getCommunityScored().getName() == fct.getCommunityScorer()
						.getName()) {
					if(!ModelException.throwException(ModelException.FIXEDCOMMUNITYTRUST,
						"Scorer(resource:"+fct.getCommunityScored().getResource()+") " +
						"and scored(resource:"+fct.getCommunityScorer().getResource()+
						"communities cannot have the same name("+fct.getCommunityScorer()
						.getName()+") in FixedCommunityTrust(resource:"+fct.getResource()+")")) {
						return false;
					}
				}
				
			}
		}
		return true;
	}
	
	static public boolean validateCommunity(Community community) throws Exception {
		if(community.getName() == null) {
			if(!ModelException.throwException(ModelException.COMMUNITY,
					"Name must be set in community(resource:"+community.getResource()+")")) {
				return false;
			}			
		}
		if(community.getDomainName() == null) {
			if(!ModelException.throwException(ModelException.COMMUNITY,
					"DomainName must be set in community(resource:"+community.getResource()+")")) {
				return false;
			}			
		}
		if(community.getCategories() == null) {
			if(!ModelException.throwException(ModelException.COMMUNITY,
					"At least one category from community(resource:"+community.getResource()
					+")must be set")) {
				return false;
			}
		} else {
			for(int i = 0; i < community.getCategories().size(); i++) {
				if(!validateCategory(community.getCategories().get(i))) {
					community.getCategories().remove(i);
					i--;
				}				
			}
		}
		if(community.getEntities() == null) {
			ModelException.sendMessage(ModelException.INFO,"not entity property is set");
		} else {
			Entity entities[] = (Entity[])community.getEntities().keySet().toArray(new Entity[
			    community.getEntities().keySet().size()]);
			for(int i = 0; i < entities.length; i++) {				
				if(!validateEntity(entities[i])) {
					if(!ModelException.throwException(ModelException.COMMUNITY_ENTITY,
							"Entity from hasEntity has error/s")) {
						community.getEntities().remove(entities[i]);
					} 
					if(!hasOnlineAccountInCommunity(entities[i],community)) {
						ModelException.throwException(ModelException.COMMUNITY_ENTITY,
							"Entity from hasEntity has not online account defined in the community");
					}
				}
			}
		}
		if(community.getReputationModel() == null) {
			ModelException.sendMessage(ModelException.INFO,"not reputationModel property is set");
		} else {
			if(!validateReputationAlgorithmImplementation(community.getReputationModel())) {
				if(!ModelException.throwException(ModelException.COMMUNITY_MODEL,
						"reputationModel has error/s")) {
					community.setReputationModel(null);
				}
			} else {
				if(getMetricsFromReputationAlgorithm(community.getReputationModel()).isEmpty()) {
					if(!ModelException.throwException(ModelException.COMMUNITY_METRIC,
						"reputationModel(resource:"+community.getReputationModel().getResource()+
						") from community(resource:"+community.getResource()+
						")must explicitly use at least one metric (You must set " +
						"useMetric property or destinationMetric from metricTransformer of a " +
						"reputationImporter module)")) {
						return false;
					}
				} else {
					if(community.getReputationModel().getUsesMetrics() == null) {
						ModelException.sendMessage(ModelException.WARNING,
							"It is recommendable to set usesMetric in the root reputationModel");						
					}
				}
			}
		}
		return true;
	}
	
	static public Set<Metric> getMetricsFromReputationAlgorithm(
			ReputationAlgorithmImplementation repAlg) {
		Set<Metric> metrics = new HashSet<Metric>();
		if(repAlg.getUsesMetrics() != null) {
			metrics.addAll(repAlg.getUsesMetrics());
		}
		if(repAlg.getBehaviours() != null) {
			for(ReputationBehaviour behaviour : repAlg.getBehaviours()) {
				metrics.addAll(getMetricFromBehaviour(behaviour));
			}
		}
		
		return metrics;
	}
	
	static Set<Metric> getMetricFromBehaviour(ReputationBehaviour behaviour) {
		Set<Metric> metrics = new HashSet<Metric>();
		if(behaviour instanceof ReputationModelBehaviour) {
			if(((ReputationModelBehaviour) behaviour).getReputationModules() != null) {
				for(ReputationAlgorithmImplementation repImpInside : 
						((ReputationModelBehaviour) behaviour).getReputationModules()) {
					metrics.addAll(getMetricsFromReputationAlgorithm(repImpInside));
				}
			}
		}
		if(behaviour instanceof ReputationModuleBehaviour) {
			if(((ReputationModuleBehaviour) behaviour).getObtainsReputationsBy() != null) {
				for(ReputationAlgorithmImplementation repImpInside : 
					((ReputationModuleBehaviour) behaviour).getObtainsReputationsBy()) {
					metrics.addAll(getMetricsFromReputationAlgorithm(repImpInside));
				}
			}
		}
		if(behaviour instanceof ReputationImporterBehaviour) {
			if(((ReputationImporterBehaviour) behaviour).getImportsFrom() != null) {
				for(ImportationUnit impUni :
						((ReputationImporterBehaviour) behaviour).getImportsFrom()) {
					if(impUni.getMetricTransformation() != null && 
							impUni.getMetricTransformation().getDestinationMetric() != null) {
						metrics.add(impUni.getMetricTransformation().getDestinationMetric());
					}
					if(impUni.getCollectsReputationBy() != null) {
						metrics.addAll(getMetricsFromReputationAlgorithm(
								impUni.getCollectsReputationBy()));
					}					
				}
			}
		}		
		return metrics;
	}
	
	static public boolean hasOnlineAccountInCommunity(Entity entity, Community community) {
		if(entity.getIdentificatorInCommunities() != null) {
			//Not done with community name, only a resource level
			if(entity.getIdentificatorInCommunities().containsKey(community)) {
				return true;
			}
		}
		return false;
	}
	
	static public boolean validateDimension(Dimension dimension) throws Exception {
		if(dimension.getName() == null) {
			if(!ModelException.throwException(ModelException.DIMENSION,
					"Dimension Name does not exist in resource:"+dimension.getResource())) {
				return false;
			}
		}
		return true;
	}
	
	static public boolean validateEntity(Entity entity) throws Exception {
		if(entity.getUniqueIdentificator() == null) {
			if(!ModelException.throwException(ModelException.ENTITY,
					"Entity Identifier must be set in the entity(resource:"
					+entity.getResource()+")")) {
				return false;
			}
		}
		if(entity.getIdentificatorInCommunities() == null) {
			ModelException.sendMessage(ModelException.INFO, "There is not online " +
					"accounts defined in the entity(resource:"+entity.getResource()+")");
		} else {
			for(Community community : entity.getIdentificatorInCommunities().keySet()) {
				if(community == null) {
					if(!ModelException.throwException(ModelException.ONLINEACCOUNT, 
							"belongsTo property from entity (resource:"+entity
							.getResource()+") must link to a properly community")) {
						return false;
					}
				} else {
					if(community.getName() == null) {
						if(!ModelException.throwException(ModelException.ONLINEACCOUNT, 
								"belongsTo property from entity (resource:"+entity
								.getResource()+") must link to a community(resource:"
								+community.getResource()+") with name")) {
							return false;
						}
					}
				}
				EntityIdentifier identifier = entity.getIdentificatorInCommunities(
						).get(community);
				if(identifier == null) {
					if(!ModelException.throwException(ModelException.ONLINEACCOUNT, 
							"account/holdAccount "
							+"property from entity (resource:"+entity.getResource()+
							") must link to a properly online account")) {
						return false;
					}
				} else {
					if(identifier.getBelongsTo() == null) {
						if(!ModelException.throwException(ModelException.ONLINEACCOUNT, "belongsTo " +
								"property from entity (resource:"+entity.getResource()+
								"must be set in online account (resource:"+identifier.getResource()+")")) {
							return false;
						}					
					}
					if(identifier.getName() == null && identifier.getUrl() == null) {
						if(!ModelException.throwException(ModelException.ONLINEACCOUNT, "name or " +
								"url properties must be set in online account (resource:"+
								identifier.getResource()+") in entity(resource:"+entity.getResource()+")")) {
							return false;
						}
					}
				}
			}
		}
		if(entity.getHasReputation() != null) {
			for(ReputationObject repObj : entity.getHasReputation()) {
				if(repObj.getOwner()!= null && repObj.getOwner() != entity) {
					if(!ModelException.throwException(ModelException.REPUTATIONOBJECT, "Owner (resource:"+
							repObj.getOwner().getResource()+") of the reputationObject (resource"+
							repObj.getResource()+") linked by hasReputation must be the same of "
							+"the entity subject (resource:"+entity.getResource()+") of the hasReputation")) {
						return false;
					}
				}
				if(repObj.getHasValue() == null || repObj.getHasValue().isEmpty()) {
					if(!ModelException.throwException(ModelException.REPUTATIONOBJECT, "Value list of the " +
							"reputationObject (resource:"+repObj.getResource()+") linked " +
							"by hasReputation cannot be empty in entity(resource:"+entity.getResource()+")")) {
						return false;
					}
				} else {
					for(int i = 0; i < repObj.getHasValue().size(); i++) {
						if(!validateHasValue(repObj.getHasValue().get(i),entity)) {
							repObj.getHasValue().remove(i);
							i--;
						}
					}
				}
				if(repObj.getFromCommunity() == null) {
					if(!ModelException.throwException(ModelException.REPUTATIONOBJECT, "fromCommunity of the " +
							"reputationObject (resource:"+repObj.getResource()+") linked" +
							" by hasReputation must be set in entity(resource:"+entity.getResource()+")")) {
						return false;
					}
				} else {
					if(repObj.getFromCommunity().getName() != null) {
						if(!ModelException.throwException(ModelException.REPUTATIONOBJECT, 
								"fromCommunity of the reputationObject (resource:"+repObj.getOwner()+
								") linked by hasReputation must be link to a community (resource:"+
								repObj.getFromCommunity().getResource()+"with name in entity(" +
								entity.getResource()+")")) {
							return false;
						}
					}
				}				
			}
		} else {
			ModelException.sendMessage(ModelException.INFO, "hasReputation is not set" +
					" in entity(resource:"+entity.getResource()+")");
		}
		if(entity.getHasValue() != null) {
			for(int i = 0; i < entity.getHasValue().size(); i++) {
				if(!validateHasValue(entity.getHasValue().get(i), entity)){
					entity.getHasValue().remove(i);
					i--;
				}
			}
		}
		if(entity.getHasEvaluation() != null) {
			for(int i = 0; i < entity.getHasEvaluation().size(); i++) {
				if(!validateHasEvaluation(entity.getHasEvaluation().get(i), entity, null)) {
					entity.getHasEvaluation().remove(i);
					i--;
				}
			}
		}
		return true;
	}
	
	static public boolean validateHasValue(ReputationValue repVal, 
			Entity entity) throws Exception {
		if(repVal.getExpirationTime() != null && repVal.getTimeStamp() != null &&
				repVal.getExpirationTime().getTime() <= repVal.getTimeStamp().getTime()) {
			if(!ModelException.throwException(ModelException.REPUTATIONVALUE, "expirationTime(" +
					repVal.getExpirationTime()+") of the reputationValue(resource:"+repVal
					.getResource()+") of the entity must be greater than timeStamp("+
					repVal.getTimeStamp()+")")) {
				return false;
			}
		}
		if(repVal.getOwner() != entity) {
			if(ModelException.throwException(ModelException.REPUTATIONVALUE, "owner property" +
					" of the reputationValue of the entity must link to entity resource")) {
				return false;
			}
		}						
		if(repVal.getObtainedBy() == null) { //<<-- el repAlg!!
			if(repVal.getHasEvaluations() == null || repVal.getHasEvaluations().isEmpty()) {
				if(!ModelException.throwException(ModelException.REPUTATIONVALUE, "hasEvaluations property" +
						" of the reputationValue of the entity must be set or not-empty")) {
					return false;
				}
			} else {
				for(int i = 0; i < repVal.getHasEvaluations().size(); i++) {
					if(!validateHasEvaluation(repVal.getHasEvaluations().get(i), entity, null)) {
						repVal.getHasEvaluations().remove(i);
						i--;
					}
				}
			}
		} else {
			
			if(repVal.getHasEvaluations() == null || repVal.getHasEvaluations().isEmpty()) {
				if(!ModelException.throwException(ModelException.REPUTATIONVALUE, "hasEvaluations property" +
						" of the reputationValue of the entity must be set or not-empty")) {
					return false;
				}
			} else {
				for(int i = 0; i < repVal.getHasEvaluations().size(); i++) {
					if(!validateHasEvaluation(repVal.getHasEvaluations().get(i),
							entity, repVal.getObtainedBy())) {
						repVal.getHasEvaluations().remove(i);
						i--;
					}
				}				
			}
		}
		return true;
	}
	
	static public boolean validateHasEvaluation(ReputationEvaluation repEval, 
			Entity entity, ReputationAlgorithmImplementation repImp) throws Exception {
		if(repEval.getTarget() != null && repEval.getTarget() != entity) {
			if(!ModelException.throwException(ModelException.REPUTATIONEVALUATION, "Target of the " +
				"reputation evaluation linked by hasEvaluation must be the same of "
				+"the entity subject of the hasEvaluation")) {
				return false;
			}
		}
		if(repEval.getValue() != null) {
			if(!ModelException.throwException(ModelException.REPUTATIONEVALUATION, "Value of the " +
					"reputation evaluation linked by hasEvaluation must be set")) {
				return false;
			}
		}
		if(repEval.getHasMetric() == null) {
			if(!ModelException.throwException(ModelException.REPUTATIONEVALUATION, "Metric of the " +
					"reputation evaluation linked by hasEvaluation must be set")) {
				return false;
			};
		} else {
			boolean metricExists = false;
			if(repImp != null) {
				metricExists = existsMetricInCommunity(repImp, repEval.getHasMetric());
			} else {
				for(Community community : entity.getIdentificatorInCommunities().keySet()) {
					if(community == null) {
						continue;
					}
					/*if(community.getMetrics() == null) {
						continue;
					}
					for(Metric metric : community.getMetrics()) {
						if(metric == repEval.getHasMetric()) {
							metricExists = true;
							break;
						}				
					}*/
					if(community.getReputationModel() == null) {
						continue;
					}
					metricExists = existsMetricInCommunity(community.getReputationModel(),
									repEval.getHasMetric());
					if(metricExists) {
						break;
					}
				}				
			}
			if(!metricExists) {
				if(!ModelException.throwException(ModelException.REPUTATIONEVALUATION_METRIC,
						"Metric of the reputation evaluation linked by hasEvaluation must" +
						" link to a metric used by reputationModel (or its modules) of " +
						"the communities provided by the onlineAccount of the entity")) {
					return false;
				}
			}
			if(!validateDimension(repEval.getHasMetric().getDimension())) {
				repEval.getHasMetric().setDimension(null);
			}
			if(!validateScale(repEval.getHasMetric().getScale())) {
				repEval.getHasMetric().setScale(null);
			}
		}
		return true;
	}
	
	static public boolean existsMetricInCommunity(ReputationAlgorithmImplementation repImp, 
			Metric metricToCheck) {
		if(repImp.getUsesMetrics() != null) {
			for(Metric metric : repImp.getUsesMetrics()) {
				if(metric == metricToCheck) {
					return true;
				}
			}
		}
		for(ReputationBehaviour behaviour : repImp.getBehaviours()) {
			if(behaviour instanceof ReputationModelBehaviour) {
				if(((ReputationModelBehaviour) behaviour).getReputationModules() != null) {
					for(ReputationAlgorithmImplementation repImpInside : 
							((ReputationModelBehaviour) behaviour).getReputationModules()) {
						if(existsMetricInCommunity(repImpInside,metricToCheck)) {
							return true;
						}
					}
				}
			}
			if(behaviour instanceof ReputationModuleBehaviour) {
				if(((ReputationModuleBehaviour) behaviour).getObtainsReputationsBy() != null) {
					for(ReputationAlgorithmImplementation repImpInside : 
						((ReputationModuleBehaviour) behaviour).getObtainsReputationsBy()) {
						if(existsMetricInCommunity(repImpInside,metricToCheck)) {
							return true;
						}
					}
				}
			}
			if(behaviour instanceof ReputationImporterBehaviour) {
				if(((ReputationImporterBehaviour) behaviour).getImportsFrom() != null) {
					for(ImportationUnit impUni :
							((ReputationImporterBehaviour) behaviour).getImportsFrom()) {
						//impUni.getImportedMetric()
						if(impUni.getMetricTransformation() != null &&
								impUni.getMetricTransformation().getSourceMetric() == metricToCheck) {
							return true;
						}
						if(impUni.getCollectsReputationBy() == null) {
							continue;
						}
						for(ReputationAlgorithmImplementation repImpInside : 
							((ReputationModuleBehaviour) behaviour).getObtainsReputationsBy()) {
							if(existsMetricInCommunity(repImpInside,metricToCheck)) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	static public boolean validateOnlineAccount(EntityIdentifier account) throws Exception {
		if(account.getBelongsTo() == null) {
			if(!ModelException.throwException(ModelException.ONLINEACCOUNT, "belongsTo " +
					"property must be set in online account")) {
				return false;
			}				
		} else {
			if(account.getBelongsTo().getName() == null) {
				if(!ModelException.throwException(ModelException.ONLINEACCOUNT, "belongsTo " +
						"property must be link to a community with identifier")) {
					return false;
				}
			}
		}
		if(account.getName() == null && account.getUrl() == null) {
			if(!ModelException.throwException(ModelException.ONLINEACCOUNT, "name or " +
					"url properties must be set in online account")) {
				return false;
			}
		}
		return true;
	}
	
	static public boolean validateMetricTransformer(MetricTransformer transformer) throws Exception {
		if(transformer.getIdentifier() == null) {
			if(!ModelException.throwException(ModelException.METRICTRANSFORMER_IDENTIFIER,
					"identifier must be set")) {
				return false;
			}
		} 
		if(transformer.getCorrelationBetweenMetric() != null) {
			for(Double correlationBetweenMetric : transformer.getCorrelationBetweenMetric()) {
				if(correlationBetweenMetric == null) {
					throw new Exception("NULL: "+transformer.getResource()+".Size:"+
							transformer.getCorrelationBetweenMetric().size());
				}
				if(correlationBetweenMetric <= 0) {
					if(!ModelException.throwException(ModelException.METRICTRANSFORMER,
							"All Correlations must be greater than 0")) {
						return false;
					}
				} else if(correlationBetweenMetric > 1) {
					if(!ModelException.throwException(ModelException.METRICTRANSFORMER,
							"All Correlations must be between 0 and 1 values")) {
						return false;
					}
				}
			}
			if(transformer.getCorrelationBetweenMetrics() <= 0) {
				if(!ModelException.throwException(ModelException.METRICTRANSFORMER,
						"Correlation must be greater than 0")) {
					return false;
				}
			} else if(transformer.getCorrelationBetweenMetrics() > 1) {
				if(!ModelException.throwException(ModelException.METRICTRANSFORMER,
						"Correlation must be between 0 and 1 values")) {
					return false;
				}
			}
		}
		if(transformer.getSourceMetric() == null) {
			if(!ModelException.throwException(ModelException.METRICTRANSFORMER,
					"SourceMetric must be set")) {
				return false;
			}
		} else {
			if(transformer.getSourceMetric().getIdentifier() == null) {
				if(!ModelException.throwException(ModelException.METRICTRANSFORMER,
						"SourceMetric Identifier must be set")) {
					return false;
				}
			}
			if(!validateMetric(transformer.getSourceMetric())){
				transformer.setSourceMetric(null);
			}
		}
		if(transformer.getDestinationMetric() == null) {
			if(!ModelException.throwException(ModelException.METRICTRANSFORMER,
					"DestinationMetric must be set")) {
				return false;
			}
		} else {
			if(transformer.getDestinationMetric().getIdentifier() == null) {
				if(!ModelException.throwException(ModelException.METRICTRANSFORMER,
						"DestinationMetric Identifier must be set")) {
					return false;
				}
			}
			if(!validateMetric(transformer.getDestinationMetric())){
				transformer.setDestinationMetric(null);
			}
		}
		if(transformer.getSourceMetric() != null && transformer.getDestinationMetric() != null
				&& transformer.getSourceMetric().getIdentifier() != null &&
				transformer.getDestinationMetric().getIdentifier() != null) {
			if(transformer.getSourceMetric().getIdentifier().equals(
					transformer.getDestinationMetric().getIdentifier())) {
				ModelException.sendMessage(ModelException.WARNING,"source and destination" +
						"Metric of the MetricTransformer("+transformer.getResource()+
						") has the same identifier");
			}
		}
		if(transformer instanceof ExponentialNumericTransformer) {
			if(((ExponentialNumericTransformer)transformer).getBase() == null) {
				if(!ModelException.throwException(ModelException.METRICTRANSFORMER,
						"ExponentialNumericTransformer Base must be set")) {
					return false;
				}
			}
		}
		if(transformer instanceof LogaritmicNumericTransformer) {
			if(((LogaritmicNumericTransformer)transformer).getBase() == null &&
					((LogaritmicNumericTransformer)transformer).getBase() != 10 ) {
				if(!ModelException.throwException(ModelException.METRICTRANSFORMER,
						"LogaritmicNumericTransformer Base only admits a value of 10.0")) {
					return false;
				}
			}
		}
		return true;
	}
	
	static public boolean validateMetric(Metric metric) throws Exception {
		if(metric.getIdentifier() == null) {
			if(!ModelException.throwException(ModelException.METRIC,
					"Identifier from Metric(resource:"+metric.getResource()+") must be set")) {
				return false;
			}
		}
		if(metric.getDimension() == null) {
			if(!ModelException.throwException(ModelException.METRIC,
					"hasDimension from Metric(resource:"+metric.getResource()+") must be set")) {
				return false;
			}
		} else {
			if(!validateDimension(metric.getDimension())) {
				metric.setDimension(null);
			}
		}
		if(metric.getScale() == null) {
			if(!ModelException.throwException(ModelException.METRIC,
					"hasScale from Metric(resource:"+metric.getResource()+") must be set")) {
				return false;
			}
		} else {
			validateScale(metric.getScale());
		}
		return true;
	}
	
	static public boolean validateReputationAlgorithmImplementation(
			ReputationAlgorithmImplementation repImp) throws Exception {
		if(repImp.getName() == null) {
			if(!ModelException.throwException(ModelException.REPUTATIONALGORITHM,
					"name from ReputationAlgorithm(resource:"+repImp.getResource()+") must be set")) {
				return false;
			}
		}	
		if(repImp.getDefinedByReputationModel() != null) {
			if(repImp.getDefinedByReputationModel().getName() != null) {
				if(!ModelException.throwException(ModelException.REPUTATIONALGORITHM_DEFINEDBY,
						"name of ReputationModel from definedByReputationModel of the" +
						" ReputationAlgorithm(resource:"+repImp.getResource()+") " +
						"property must be set")) {
					return false;
				}
			}
			if(repImp.getDefinedByReputationModel().getBehaviours() == null) {
				if(!ModelException.throwException(ModelException.REPUTATIONALGORITHM_DEFINEDBY,
						"ReputationModel resource from definedByReputationModel of the " +
						"ReputationAlgorithm(resource:"+repImp.getResource()+") " +
						"must be ReputationModel class (or subclass)")) {
					return false;
				}
			} else {
				boolean isReputationModel = false;
				for(ReputationBehaviour behaviour : 
						repImp.getDefinedByReputationModel().getBehaviours()) {
					if(behaviour instanceof ReputationModelBehaviour) {
						isReputationModel = true;
						break;
					}
				}
				if(!isReputationModel) {
					if(!ModelException.throwException(ModelException.REPUTATIONALGORITHM_DEFINEDBY,
							"ReputationModel resource from definedByReputationModel of the " +
							"ReputationAlgorithm(resource:"+repImp.getResource()+
							") must be ReputationModel class (or subclass)")) {
						return false;
					}
				}
			}
		}
		if(repImp.getReputationResults() == null) {
			ModelException.sendMessage(ModelException.INFO, "no reputationResult " +
					"property from ReputationAlgorithm(resource:"+repImp.getResource()+") was found");
		} else {
			for(int i = 0; i < repImp.getReputationResults().size(); i++) {
				ReputationValue repVal = repImp.getReputationResults().get(i);
				if(!validateReputationValue(repVal,repImp)) {
					repImp.getReputationResults().remove(i);
					i--;
				}
			}
		}
		if(repImp.getReputationSources() == null) {
			ModelException.sendMessage(ModelException.INFO, "no reputationSources" +
					" property from ReputationAlgorithm(resource:"+repImp.getResource()+") was found");
		} else {
			for(int i = 0; i < repImp.getReputationSources().size(); i++) {
				ReputationValue repVal = repImp.getReputationSources().get(i);
				if(!validateReputationValue(repVal,repImp)) {
					repImp.getReputationResults().remove(i);
					i--;
				}
			}
		}
		if(repImp.getUsesMetrics() == null) {
			if(!ModelException.throwException(ModelException.REPUTATIONALGORITHM_METRIC,
					"usesMetric from ReputationAlgorithm(resource:"+repImp.getResource()+
					")must be set")) {
				return false;
			}
		} else {
			for(int i = 0; i < repImp.getUsesMetrics().size(); i++) {
				if(!validateMetric(repImp.getUsesMetrics().get(i))) {
					if(!ModelException.throwException(ModelException.REPUTATIONALGORITHM_METRIC_ERROR,
							"Metric from usesMetric property has error")) {
						repImp.getUsesMetrics().remove(i);
						i--;
					}
				}
			}
		}
		if(repImp.getBehaviours() != null) {
			for(int i = 0; i < repImp.getBehaviours().size(); i++) {
				if(!validateReputationBehaviour(repImp.getBehaviours().get(i))) {
					repImp.getBehaviours().remove(i);
					i--;
				}
			}
		} else {
			ModelException.sendMessage(ModelException.INFO, "resource " +
					" is only a individual of reputationAlgorithm class " +
					"(not subclass instanced)");
		}
		return true;
	}
	
	static public boolean validateReputationBehaviour(ReputationBehaviour behaviour) throws Exception {
		if(behaviour.getRoot() == null) {
			if(!ModelException.throwException(ModelException.REPUTATIONALGORITHM_BEHAVIOUR,
					"behaviour(type:"+behaviour.getClass()+") must link to ReputationAlgorithm" +
					" that represents the object")) {
				return false;
			}
		}
		if(behaviour instanceof ReputationModelBehaviour) {
			if(!validateReputationModelBehaviour((ReputationModelBehaviour)behaviour)) {
				return false;
			}
		} else if(behaviour instanceof ReputationModuleBehaviour) {
			if(!validateReputationModuleBehaviour((ReputationModuleBehaviour)behaviour)) {
				return false;
			}
		} else if(behaviour instanceof ReputationImporterBehaviour) {
			if(!validateReputationImporterBehaviour(
					(ReputationImporterBehaviour)behaviour)) {
				return false;
			}
		} else if(behaviour instanceof CollectingAlgorithmBehaviour) {
			if(!validateCollectingAlgorithmBehaviour(
					(CollectingAlgorithmBehaviour)behaviour)) {
				return false;
			}
		} else if(behaviour instanceof ReputationalActionBehaviour) {
			if(!validateReputationalActionBehaviour(
					(ReputationalActionBehaviour)behaviour)) {
				return false;
			}
		} else if(behaviour instanceof CollectingSystemBehaviour) {
			if(!validateCollectingSystemBehaviour(
					(CollectingSystemBehaviour)behaviour)) {
				return false;
			}
		}
		if(behaviour.getBehaviours() != null) {
			for(int i = 0; i < behaviour.getBehaviours().size(); i++) {
				if(!validateReputationBehaviour(behaviour.getBehaviours().get(i))) {
					behaviour.getBehaviours().remove(i);
					i--;
				}
			}
		}
		return true;
	}
	
	static public boolean validateCollectingSystemBehaviour(
			CollectingSystemBehaviour behaviour) throws Exception {
		if(behaviour.getUriFormat() == null) {
			if(!ModelException.throwException(ModelException.COLLECTINGSYSTEM,
					"uriFormat must be set")) {
				return false;
			}
		} else if(!behaviour.getUriFormat().toString().contains("$")) {
			ModelException.sendMessage(ModelException.INFO, "collectingSystem " +
					"resource does not have any special character($)");
		}
		return true;
	}
	
	static public boolean validateCollectingAlgorithmBehaviour(
			CollectingAlgorithmBehaviour behaviour) {
		return true;
	}
	
	static public boolean validateReputationalActionBehaviour(
			ReputationalActionBehaviour behaviour) {
		return true;
	}
	
	static public boolean validateReputationModelBehaviour(
			ReputationModelBehaviour behaviour) throws Exception {
		if(behaviour.getReputationModules() == null) {
			ModelException.sendMessage(ModelException.INFO, "reputationModel(" +
					"resource:"+behaviour.getRoot().getResource()+") does not have" +
					" reputationModules");
		} else {
			for(int i = 0; i < behaviour.getReputationModules().size(); i++) {
				if(!validateReputationAlgorithmImplementation(
						behaviour.getReputationModules().get(i))) {
					behaviour.getReputationModules().remove(i);
					i--;
				}
			}
			if(behaviour.getReputationModules().isEmpty()) {
				ModelException.sendMessage(ModelException.INFO, "reputationModel(" +
					"resource:"+behaviour.getRoot().getResource()+") does not have " +
					" reputationModules");
			}
		}
		return true;
	}
	
	static public boolean validateReputationModuleBehaviour(
			ReputationModuleBehaviour behaviour) throws Exception {
		if(behaviour.getObtainsReputationsBy() == null) {
			ModelException.sendMessage(ModelException.INFO, "reputationModule" +
				"(resource:"+behaviour.getRoot().getResource()+") does not have" +
				"obtainsReputationsBy properties");
		} else {
			for(int i = 0; i < behaviour.getObtainsReputationsBy().size(); i++) {
				if(!validateReputationAlgorithmImplementation(
						behaviour.getObtainsReputationsBy().get(i))) {
					behaviour.getObtainsReputationsBy().remove(i);
					i--;
				}
			}
			if(behaviour.getObtainsReputationsBy().isEmpty()) {
				ModelException.sendMessage(ModelException.INFO, "reputationModule" +
					"(resource:"+behaviour.getRoot().getResource()+") does not have " +
					"obtainsReputationsBy properties");
			}
		}
		return true;
	}
	
	static public boolean validateReputationImporterBehaviour(
			ReputationImporterBehaviour behaviour) throws Exception{
		if(behaviour.getImportsFrom() == null) {
			if(!ModelException.throwException(ModelException.REPUTATIONIMPORTER,
					"ReputationImporter must have importsFrom property")) {
				return false;
			}
		} else {
			for(int i = 0; i < behaviour.getImportsFrom().size(); i++) {
				if(!validateImportationUnit(behaviour.getImportsFrom().get(i))) {
					if(!ModelException.throwException(ModelException.REPUTATIONIMPORTER_IMPORTATION,
							"ImportationUnit from importsFrom has error/s")) {
						behaviour.getImportsFrom().remove(i);
						i--;
					}
				}
			}
			if(behaviour.getImportsFrom().isEmpty()) {
				if(!ModelException.throwException(ModelException.REPUTATIONIMPORTER,
						"ReputationImporter must have importsFrom property")) {
					return false;
				}
			}
		}
		if(behaviour.getMapsMetrics() == null) {
			ModelException.sendMessage(ModelException.INFO, "no mapsMetric " +
				"from importsFrom must be set");
		} else {
			for(int i = 0; i < behaviour.getMapsMetrics().size(); i++) {
				if(!validateMetricMapping(behaviour.getMapsMetrics().get(i))) {
					behaviour.getMapsMetrics().remove(i);
					i--;
				}
			}
			if(behaviour.getMapsMetrics().isEmpty())  {
				ModelException.sendMessage(ModelException.INFO, "no mapsMetric " +
					"from importsFrom must be set (or they were discarded)");
			}
		}
		if(behaviour.getRoot().getUsesMetrics() != null && behaviour.getImportsFrom() != null) {
			for(ImportationUnit impUni : behaviour.getImportsFrom()) {
				if(impUni.getMetricTransformation() == null ||
						impUni.getMetricTransformation().getDestinationMetric() == null) {
					continue;
				}
				//Not done by name but by resource
				if(!behaviour.getRoot().getUsesMetrics().contains(
						impUni.getMetricTransformation().getDestinationMetric())) {
					ModelException.sendMessage(ModelException.INFO, "destinationMetric" +
						" from metricTransformation from importsFrom is not declared" +
						" in usesMetric of the ReputationImporter");
				}
			}
		}
		//To check if sourceMetric and destinationMetrics have their mappings in mapsMetric
		if(behaviour.getImportsFrom() == null) {
			for(int i = 0; i < behaviour.getImportsFrom().size(); i++) {
				MetricTransformer metTra = behaviour.getImportsFrom().get(i).getMetricTransformation();
				boolean isIn = false;
				if(metTra != null && behaviour.getMapsMetrics() != null) {
					if(metTra.getSourceMetric() != null && metTra.getDestinationMetric() != null) {
						for(MetricMapping metMap : behaviour.getMapsMetrics()) {
							if(metMap.getImportedMetric() != null &&
									metMap.getImportedMetric() == metTra.getSourceMetric() &&
									metMap.getResultMetric()!= null &&
									metMap.getResultMetric() == metTra.getDestinationMetric()) {
								isIn = true;
							}
						}
					}
					
				}
				if(!isIn) {
					ModelException.sendMessage(ModelException.INFO, "not MetricMapping " +
						"found that maps sourceMetric and destinationMetric of the " +
						"metricTransformation from importsFrom");
				}
			}
		}
		boolean hasOneOrMoreCollectingSystem = false;
		if(behaviour.getImportsFrom() != null) {
			for(ImportationUnit impUni : behaviour.getImportsFrom()) {
				if(impUni.getCollectsReputationBy() != null) {
					if(existsAValidCollectingSystem(impUni.getCollectsReputationBy())) {
						hasOneOrMoreCollectingSystem = true;
						break;
					}
				}
			}
		}
		if(!hasOneOrMoreCollectingSystem) {
			if(!ModelException.throwException(ModelException.REPUTATIONIMPORTER,
					"collectingAlgorithm from importsFrom properties must have at least" +
					" one collecingSystem between all")) {
				return false;
			}
		}
		return true;
	}
	
	static public boolean existsAValidCollectingSystem(
			ReputationAlgorithmImplementation repAlg) throws Exception {
		for(ReputationBehaviour behaviour : repAlg.getBehaviours()) {
			if(!CollectingAlgorithmBehaviour.class.isInstance(behaviour)) {
				continue;
			}
			for(ReputationBehaviour behaviourInside : 
					behaviour.getBehaviours()) {
				if(!CollectingSystemBehaviour.class.isInstance(
						behaviourInside)) {
					continue;
				}
				if(validateCollectingSystemBehaviour((
						CollectingSystemBehaviour)behaviourInside)) {
					return true;
				}				
			}
		}
		for(ReputationBehaviour behaviour : repAlg.getBehaviours()) {
			if(ReputationModelBehaviour.class.isInstance(behaviour)) {
				ReputationModelBehaviour model = (
						ReputationModelBehaviour) behaviour;
				for(ReputationAlgorithmImplementation module : 
						model.getReputationModules()) {
					if(existsAValidCollectingSystem(module)) {
						return true;
					}
				}
			}
			if(ReputationModuleBehaviour.class.isInstance(behaviour)) {
				ReputationModuleBehaviour module = (
						ReputationModuleBehaviour) behaviour;
				for(ReputationAlgorithmImplementation repAlgInside : 
					module.getObtainsReputationsBy()) {
					if(existsAValidCollectingSystem(repAlgInside)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	static public boolean validateMetricMapping(MetricMapping metMap) throws Exception {		
		if(metMap.getImportedMetric() == null) {
			if(!ModelException.throwException(ModelException.METRICMAPPING,
					"importedMetric property must be set")) {
				return false;
			}
		} else {
			if(metMap.getImportedMetric().getDimension() == null) {
				if(!ModelException.throwException(ModelException.METRICMAPPING,
						"dimension from importedMetric property must be set")) {
					return false;
				}
			} else {
				if(!validateDimension(metMap.getImportedMetric().getDimension())) {
					return false;
				}
			}
		}
		if(metMap.getResultMetric() == null) {
			if(!ModelException.throwException(ModelException.METRICMAPPING,
					"importedMetric property must be set")) {
				return false;
			}
		} else {
			if(metMap.getResultMetric().getDimension() == null) {
				if(!ModelException.throwException(ModelException.METRICMAPPING,
						"dimension from importedMetric property must be set")) {
					return false;
				}
			} else {
				if(!validateDimension(metMap.getResultMetric().getDimension())) {
					return false;
				}
			}
		}
		if(metMap.getImportedMetric() != null && metMap.getResultMetric() != null
				&& metMap.getImportedMetric().getIdentifier() != null &&
				metMap.getResultMetric().getIdentifier() != null &&
				metMap.getImportedMetric().getIdentifier().equals(
				metMap.getResultMetric().getIdentifier())) {
			if(!ModelException.throwException(ModelException.METRICMAPPING,
					"name Metric(resource:"+metMap.getImportedMetric().getResource()+") in importedMetric" +
					" and name Metric(resource:"+metMap.getResultMetric().getResource()+") in " +
					"resultMetric cannot be the same in MetricMapping(resource:"+
					metMap.getResource()+")")) {
				return false;
			}
		}
		if(metMap.getImportedMetric() != null && metMap.getResultMetric() != null
				&& metMap.getImportedMetric().getDimension() != null &&
				metMap.getImportedMetric().getDimension().getName() != null &&
				metMap.getResultMetric().getDimension() != null &&
				metMap.getResultMetric().getDimension().getName() != null) {
			if(metMap.getImportedMetric().getDimension().getName() ==
					metMap.getResultMetric().getDimension().getName()) {
				ModelException.sendMessage(ModelException.WARNING,
						"name of dimension in importedMetric("+metMap.getImportedMetric(
						).getDimension().getName()+") and name of dimension in resultMetric("+
						metMap.getResultMetric().getDimension().getName()+") should not be the same" +
						" in MetricMapping(resource:"+metMap.getResource()+")");
			}
		}
		return true;
	}
	
	static public boolean validateImportationUnit(ImportationUnit impUni) throws Exception {
		if(impUni.getCollectsReputationBy() == null) {
			if(!ModelException.throwException(ModelException.IMPORTATIONUNIT,
					"ImportationUnit must have collectsReputationBy property")) {
				return false;
			}
		} else {
			if(!validateReputationAlgorithmImplementation(impUni.getCollectsReputationBy())) {
				if(!ModelException.throwException(ModelException.IMPORTATIONUNIT_COLLECTSREPUTATION,
						"ReputationImporter from collectsReputationBy has errors")) {
					return false;
				}
			}
		}
		if(impUni.getImportedCommunity() == null) {
			if(!ModelException.throwException(ModelException.IMPORTATIONUNIT,
					"ImportationUnit must have importedCommunity property")) {
				return false;
			}
		} else {
			if(!validateCommunity(impUni.getImportedCommunity())) {
				if(!ModelException.throwException(ModelException.IMPORTATIONUNIT_COMMUNITY,
						"Community from importedCommunity has errors")) {
					return false;
				}
			}
		}
		if(impUni.getMetricTransformation() == null) {
			if(!ModelException.throwException(ModelException.IMPORTATIONUNIT,
					"ImportationUnit must have metricTransformation property")) {
				return false;
			}
			
		} else {
			if(!validateMetricTransformer(impUni.getMetricTransformation())) {
				if(!ModelException.throwException(ModelException.IMPORTATIONUNIT,
						"MetricTransformer from metricTransformation has errors")) {
					return false;
				}
			}
		}
		/*if(impUni.getImportedMetric() == null) {
			if(!ModelException.throwException(ModelException.IMPORTATIONUNIT,
					"ImportationUnit must have importedMetric property")) {
				return false;
			}
		} else {
			if(!validateMetric(impUni.getImportedMetric())) {
				if(!ModelException.throwException(ModelException.IMPORTATIONUNIT,
						"Metric from importedMetric has errors")) {
					return false;
				}
			}
		}*/
		if(impUni.getTrust() != null) {
			if(!validateTrustBetweenCommunities(impUni.getTrust())) {
				if(!ModelException.throwException(ModelException.IMPORTATIONUNIT,
						"TrustBetweenCommunities from trust has errors")) {
					return false;
				}
			}
		}
		if(impUni.getMetricTransformation() != null && 
				impUni.getMetricTransformation().getSourceMetric() != null &&
				impUni.getImportedMetric() != null) {
			if(impUni.getMetricTransformation().getSourceMetric() != 
					impUni.getImportedMetric()) {
				if(!ModelException.throwException(ModelException.IMPORTATIONUNIT,
						"ImportationMetric and SourceMetric from metricTransformation" +
						" must be the same")) {
					return false;
				}
			}
		}
		if(impUni.getImportedCommunity() != null && 
				impUni.getImportedCommunity().getReputationModel() != null &&
				impUni.getMetricTransformation().getSourceMetric() != null) {
			if(!existsMetricInCommunity(impUni.getImportedCommunity().getReputationModel(),
					impUni.getMetricTransformation().getSourceMetric())) {
				if(!ModelException.throwException(ModelException.IMPORTATIONUNIT_METRIC,
						"SourceMetric(resource:"+impUni.getMetricTransformation().getSourceMetric(
						).getResource()+") from metricTransformation(resource:"+impUni.
						getMetricTransformation().getResource()+") must be also set in" +
						" the community(resource:"+impUni.getImportedCommunity().getResource()+
						") from importedCommunity in the importationUnit(resource:"+
						impUni.getResource()+")")) {
					return false;
				}
			}
		}
		if(impUni.getCollectsReputationBy() != null && impUni.getMetricTransformation() !=null
				&& impUni.getMetricTransformation().getSourceMetric() != null) {
			if(!getMetricsFromReputationAlgorithm(impUni.getCollectsReputationBy()).contains(
					impUni.getMetricTransformation().getSourceMetric())) {
				ModelException.sendMessage(ModelException.WARNING, "sourceMetric from importationUnit" +
						"(resource:"+impUni.getResource()+") does not set in any metric used by " +
						"reputationAlgorithm (resource:"+impUni.getCollectsReputationBy().getResource()
						+") linked by collectsReputationBy. Be sure that an algorithm is implemented" +
						" to have this metric in results");
			}
		}
		return true;
	}
	
	static public boolean validateReputationObject(ReputationObject repObj) throws Exception {
		if(repObj.getOwner() == null) {
			if(!ModelException.throwException(ModelException.REPUTATIONOBJECT, "owner property" +
					" of the reputationObject must be set")) {
				return false;
			}
		} else {
			if(!validateEntity(repObj.getOwner())) {
				if(!ModelException.throwException(ModelException.REPUTATIONOBJECT_ENTITY, 
						"owner property of the reputationObject has errors")) {
					return false;
				}
			}
		}
		if(repObj.getHasValue() == null) {
			if(!ModelException.throwException(ModelException.REPUTATIONVALUE, 
					"hasEvaluations property of the reputationValue of the entity must be" +
					" set or not-empty")) {
				return false;
			}
		} else {
			for(int i = 0; i < repObj.getHasValue().size(); i++) {
				if(!validateHasValue(repObj.getHasValue().get(i), repObj.getOwner())) {
					repObj.getHasValue().remove(i);
					i--;
				}
			}
			if(repObj.getHasValue().isEmpty()) {
				if(!ModelException.throwException(ModelException.REPUTATIONVALUE, "hasEvaluations property" +
						" of the reputationValue of the entity must be set or not-empty")) {
					return false;
				}
			}
		}
		return true;
	}
	
	static public boolean validateReputationValue(ReputationValue repVal,
			ReputationAlgorithmImplementation repAlg) throws Exception {
		if(repVal.getExpirationTime() != null && repVal.getTimeStamp() != null &&
				repVal.getExpirationTime().getTime() <= repVal.getTimeStamp().getTime()) {
			if(!ModelException.throwException(ModelException.REPUTATIONVALUE, "expirationTime" +
					" of the reputationValue must be greater than timeStamp")) {
				return false;
			}
		}
		if(repVal.getOwner() == null) {
			if(!ModelException.throwException(ModelException.REPUTATIONVALUE, "owner property" +
					" of the reputationValue must be set")) {
				return false;
			}
		} else {
			if(!validateEntity(repVal.getOwner())) {
				if(!ModelException.throwException(ModelException.REPUTATIONVALUE_ENTITY, 
						"owner property of the reputationValue has errors")) {
					return false;
				}
			}
		}
		if(repVal.getObtainedBy() == null) {
			if(repAlg == null) {
				if(!ModelException.throwException(ModelException.REPUTATIONVALUE, "obtainedBy property" +
						" of the reputationValue must be set")) {
					return false;
				}
			} else {
				ModelException.sendMessage(ModelException.INFO,"obtainedBy not defined but " +
						"reputationAlgorithm is get from the reputationSource or reputationResult" +
						"that links this reputationValue");
			}
			if(repVal.getHasEvaluations() == null || repVal.getHasEvaluations().isEmpty()) {
				if(!ModelException.throwException(ModelException.REPUTATIONVALUE, "hasEvaluations property" +
						" of the reputationValue must be set or not-empty")) {
					return false;
				}
			} else {
				for(int i = 0; i < repVal.getHasEvaluations().size(); i++) {
					if(!validateHasEvaluation(repVal.getHasEvaluations().get(i),
							repVal.getOwner(), repAlg)) {
						repVal.getHasEvaluations().remove(i);
						i--;
					}
				}
			}
		} else {
			if(repAlg != null) {
				if(!repVal.getObtainedBy().getName().equals(repAlg.getName())) {
					if(!ModelException.throwException(ModelException.REPUTATIONVALUE_REPUTATIONALGORITHM, 
							"ReputationAlgorithm (resource:"+repVal.getObtainedBy().getResource()
							+") from obtainedBy does not have the same identifier that " +
							"ReputationAlgorithm (resource:"+repAlg.getResource()+
							") from reputationSource or reputationResult properties")) {
						return false;
					}
				}
			}
			if(repVal.getHasEvaluations() == null) {
				if(!ModelException.throwException(ModelException.REPUTATIONVALUE, "hasEvaluations property" +
						" of the reputationValue(resource:" + repVal.getResource() +
						") of the entity must be set or not-empty")) {
					return false;
				}
			} else {
				for(ReputationEvaluation repEval : repVal.getHasEvaluations()) {
					validateHasEvaluation(repEval, repVal.getOwner(), repVal.getObtainedBy());
				}
				if(repVal.getHasEvaluations().isEmpty()) {
					if(!ModelException.throwException(ModelException.REPUTATIONVALUE, 
							"hasEvaluations property of the reputationValue(resource:"+
							repVal.getResource()+")of the entity must be set or not-empty")) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	static public boolean validateReputationEvaluation(ReputationEvaluation repEval,
			ReputationAlgorithmImplementation repAlg) throws Exception {
		if(repEval.getTarget() == null) {
			if(!ModelException.throwException(ModelException.REPUTATIONEVALUATION, 
					"Target of the reputation evaluation (resource:"+
					repEval.getResource()+"must be set")) {
				return false;
			}
		} else {
			if(!validateEntity(repEval.getTarget())) {
				if(!ModelException.throwException(ModelException.REPUTATIONEVALUATION_ENTITY, 
						"Target of the reputation evaluation (resource:"+
						repEval.getResource()+"has error/s")) {
					return false;
				}
			}
		}
		if(repEval.getHasMetric() == null) {
			if(!ModelException.throwException(ModelException.REPUTATIONEVALUATION, 
					"Metric of the reputation evaluation (resource:"+
					repEval.getResource()+" must be set")) {
				return false;
			}
		} else {
			if(!validateMetric(repEval.getHasMetric())) {
				if(!ModelException.throwException(ModelException.REPUTATIONEVALUATION_METRIC, 
						"Metric(resource:"+repEval.getHasMetric().getResource()+") of the" +
						" reputation evaluation (resource: "+repEval.getResource()+"has error/s")) {
					return false;
				}
			}
			if(repAlg != null) {
				if(repAlg.getUsesMetrics() != null) {
					boolean match = false;
					for(Metric metric : repAlg.getUsesMetrics()) {
						if(metric.getIdentifier().equals(repEval.getHasMetric().getIdentifier())){
							match = true;
							break;
						}
					}
					if(!match) {
						if(!ModelException.throwException(ModelException.REPUTATIONEVALUATION_REPUTATIONALGORITHM, 
								"Metric(resource:"+repEval.getHasMetric().getResource()+") of the" +
								" reputation evaluation (resource:"+repEval.getResource()+" not match" +
								" with any metric from usesMetric from reputationResult or " +
								"sourceResult")) {
							return false;
						}
					}
				} else {
					if(!ModelException.throwException(ModelException.REPUTATIONEVALUATION_REPUTATIONALGORITHM, 
							"Metric(resource:"+repEval.getHasMetric().getResource()+") of the" +
							" reputation evaluation (resource:"+repEval.getResource()+" not match" +
							" with metrics from usesMetric from reputationResult or sourceResult:" +
							" (maybe usesMetric is not set in ReputationAlgorithm)")) {
						return false;
					}
				}				
			}
		}
		if(repEval.getValue() == null) {
			if(!ModelException.throwException(ModelException.REPUTATIONEVALUATION, 
					"Value of the reputation evaluation (resource:"+
					repEval.getResource()+"must be set")) {
				return false;
			}
		}
		return true;
	}
	
	static public boolean validateDimensionCorrelation(DimensionCorrelation dimCor) throws Exception {
		if(dimCor.getSourceDimension() == null) {
			if(!ModelException.throwException(ModelException.DIMENSIONCORRELATION,
					"sourceDimension from DimensionCorrelation(resource:"+
					dimCor.getResource()+") must be set")) {
				return false;
			}
		} else {
			if(!validateDimension(dimCor.getSourceDimension())) {
				if(!ModelException.throwException(ModelException.DIMENSIONCORRELATION,
						"Dimension(resource:"+ dimCor.getSourceDimension().getResource()+
						") from sourceDimension of the DimensionCorrelation(resource:"+
						dimCor.getResource()+") has error/s")) {
					return false;
				}
			}
		}
		if(dimCor.getTargetDimension() == null) {
			if(!ModelException.throwException(ModelException.DIMENSIONCORRELATION,
					"targetDimension from DimensionCorrelation(resource:"+
					dimCor.getResource()+") must be set")) {
				return false;
			}
		} else {
			if(!validateDimension(dimCor.getTargetDimension())) {
				if(!ModelException.throwException(ModelException.DIMENSIONCORRELATION,
						"Dimension(resource:"+ dimCor.getTargetDimension().getResource()+
						") from sourceDimension of the DimensionCorrelation(resource:"+
						dimCor.getResource()+") has error/s")) {
					return false;
				}
			}
		}
		if(dimCor.getSourceDimension() != null && dimCor.getTargetDimension() != null
				&& dimCor.getSourceDimension().getName() != null &&
				dimCor.getTargetDimension().getName() != null) {
			if(dimCor.getSourceDimension().getName().equals(
					dimCor.getTargetDimension().getName())) {
				if(!ModelException.throwException(ModelException.DIMENSIONCORRELATION,
						"Dimensions from sourceDimension and targetDimension of the " +
						"Dimension Correlation(resource:"+ dimCor.getResource()+") must" +
						" have different names")) {
					return false;
				}
			}
		}
		if(dimCor.getCorrelationValue() == null) {
			if(!ModelException.throwException(ModelException.DIMENSIONCORRELATION,
					"correlationValue from DimensionCorrelation((resource:"+ 
					dimCor.getResource()+") must be set")) {
				return false;
			}
		} else {
			if(dimCor.getCorrelationValue() <= 0) {
				if(!ModelException.throwException(ModelException.DIMENSIONCORRELATION,
						"correlationValue from DimensionCorrelation((resource:"+ 
					dimCor.getResource()+") must be greater than 0")) {
					return false;
				}
			} else if(dimCor.getCorrelationValue() > 1) {
				if(!ModelException.throwException(ModelException.DIMENSIONCORRELATION,
						"correlationValue from DimensionCorrelation((resource:"+ 
						dimCor.getResource()+") must be between 0 and 1 values")) {
					return false;
				}
			}
		}
		return true;
	}
	
	static public boolean validateScaleCorrelation(ScaleCorrelation scaCor) throws Exception {
		if(scaCor.getSourceScale() == null) {
			if(!ModelException.throwException(ModelException.SCALECORRELATION,
					"sourceScale from ScaleCorrelation(resource:"+
					scaCor.getResource()+") must be set")) {
				return false;
			}
		} else {
			if(!validateScale(scaCor.getSourceScale())) {
				if(!ModelException.throwException(ModelException.SCALECORRELATION,
						"Scale(resource:"+scaCor.getSourceScale().getResource()+
						") from sourceScale from ScaleCorrelation(resource:"+
						scaCor.getResource()+") has error/s")) {
					return false;
				}
			}
		}
		if(scaCor.getTargetScale() == null) {
			if(!ModelException.throwException(ModelException.SCALECORRELATION,
					"sourceScale from sourceScale of the ScaleCorrelation(resource:"+
						scaCor.getResource()+") must be set")) {
				return false;
			}
		} else {
			if(!validateScale(scaCor.getTargetScale())) {
				if(!ModelException.throwException(ModelException.SCALECORRELATION,
						"Scale(resource:"+scaCor.getTargetScale().getResource()+
						"from sourceScale of the ScaleCorrelation(resource:"+
						scaCor.getResource()+") has error/s")) {
					return false;
				}
			}
		}
		if(scaCor.getSourceScale() != null && scaCor.getTargetScale() != null
				&& scaCor.getSourceScale().getName() != null &&
				scaCor.getTargetScale().getName() != null) {
			if(scaCor.getSourceScale().getName().equals(
					scaCor.getTargetScale().getName())) {
				if(!ModelException.throwException(ModelException.SCALECORRELATION,
						"Scales from sourceScale and targetScale of the ScaleCorrelation(resource:"+
						scaCor.getResource()+") must have different names")) {
					return false;
				}
			}
		}
		if(scaCor.getCorrelationValue() == null) {
			if(!ModelException.throwException(ModelException.SCALECORRELATION,
					"correlationValue of the ScaleCorrelation(resource:"+
						scaCor.getResource()+") must be set")) {
				return false;
			}
		} else {
			if(scaCor.getCorrelationValue() <= 0) {
				if(!ModelException.throwException(ModelException.SCALECORRELATION,
						"correlationValue of the ScaleCorrelation(resource:"+
						scaCor.getResource()+") must be greater than 0")) {
					return false;
				}
			} else if(scaCor.getCorrelationValue() > 1) {
				if(!ModelException.throwException(ModelException.SCALECORRELATION,
						"correlationValue must of the ScaleCorrelation(resource:"+
						scaCor.getResource()+") be between 0 and 1 values")) {
					return false;
				}
			}
		}
		return true;
	}
	
}
