package cross.reputation.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import es.upm.dit.ConfigureModel;

public class CrossReputation {
	private Community community;
	private Set<Entity> entities;
	private List<Evaluation> evaluations = new ArrayList<Evaluation>();
	private List<CommunityMetricToImport> metricsToImport;
	
	public CrossReputation(Community community, List<CommunityMetricToImport> 
			metricsFromCommunities, boolean allEntities, boolean verifyMetrics) {
		this.community = community;
		if(allEntities) {
			entities = community.getEntities().keySet();
		} else {
			entities = new HashSet<Entity>();
		}
		if(verifyMetrics) {
			metricsToImport = new ArrayList<CommunityMetricToImport>();
			for(CommunityMetricToImport metric : metricsFromCommunities) {
				if(metric.getDestinationCommunity() == community)
					metricsToImport.add(metric);
			}
		} else {
			this.metricsToImport = metricsFromCommunities;
		}
	}	
	
	public void addEntity(Entity entity) {
		entities.add(entity);
	}
	
	public void addEntities(List<Entity> entities) {
		entities.addAll(entities);
	}
	
	public void addAllEvaluations() {
		evaluations = GlobalModel.getEvaluations();
	}
	
	public Object calculateSingleReputation(CommunityMetricToImport communityMetricToImport,
			Metric destinationMetric, Entity entity) {
		if(communityMetricToImport.getDestinationMetric() != destinationMetric)
			return null;
		Double value = null;
		MetricTransformer exTransformer = null;
		for(MetricTransformer transformer : GlobalModel.getMetricTransformers()){
			if(transformer.getSourceMetric() == communityMetricToImport.getMetric() &&
					transformer.getDestinationMetric() == destinationMetric) {
				exTransformer = transformer;
				value = transformer.getCorrelationBetweenMetrics();
				break;
			}				
		}
		if(value == null || communityMetricToImport.getTrust() == null)
			return null;
		value *= communityMetricToImport.getTrust();
		Object values = null;
		for(Evaluation evaluation : evaluations) {
			if(evaluation.getCommunity() == communityMetricToImport.getCommunity() &&
					evaluation.getDestinationEntity() == entity &&
					evaluation.getMetric() == communityMetricToImport.getMetric()) {
				//TODO: que devuelva List<Object> y no sumValues sino 
				//manageValuesToCrossReputation y que devuelva List<Object>
				
				for(Object sourceValue : evaluation.getValues()) {
					values = communityMetricToImport.getMetric().sumValues(values,sourceValue);
				}
				if(values != null) {
					values = communityMetricToImport.getMetric().doAverage(values,
						evaluation.getValues().size());
					System.out.println("  -SR:before trans:"+values+" elems:"+
							evaluation.getValues().size()+" c:"+communityMetricToImport.getCommunity().getDomainName()+" m:"+
							communityMetricToImport.getMetric().getIdentificator());
					values = exTransformer.tranformation(values, false);
				}
				break;
			}
		}
		if(values == null)
			return null;
		System.out.println("   SR:after trans:"+values+" trust:"+value+" ("+
				exTransformer.getCorrelationBetweenMetrics()+"*"+communityMetricToImport.getTrust()
				+") SR:"+communityMetricToImport.getMetric().addTrust(values,value)+" c"+
				communityMetricToImport.getDestinationCommunity().getDomainName()+" m:"+
				communityMetricToImport.getDestinationMetric().getIdentificator());
		return communityMetricToImport.getMetric().addTrust(values,value);		
	}
	
	/**
	 * Without doing case to metricsToImport setted
	 */
	public void calculateCrossReputationFromEntities() {
		calculateCrossReputationFromEntities(entities);
	}
	
	/**
	 * Without doing case to metricsToImport and entities setted
	 */
	public void calculateCrossReputationFromEntities(Set<Entity> entities) {
		List<CommunityMetricToImport> commMetricsToImport = 
			new ArrayList<CommunityMetricToImport>();
		for(Entity entity : entities) {			
			for(Community community : entity.getIdentificatorInCommunities().keySet()) {
				if(community == this.community)
					continue;
				for(Metric metric : community.getEntities().get(entity)) {
					List<CommunityMetricToImport> comMetrToImp = 
						ConfigureModel.buildCommunityMetricToImportToAllMetrics(community,
						this.community, metric);
					if(comMetrToImp == null)
						continue;
					for(int i = 0; i < comMetrToImp.size(); i++) {
						if(comMetrToImp.get(i).isContained(commMetricsToImport)) {
							comMetrToImp.remove(i);
							i--;
						}
					}
					commMetricsToImport.addAll(comMetrToImp);
				}
			}
		}
		calculateAllReputationByMetricsMatchingAndEntities(commMetricsToImport,entities);
	}
	
	/**
	 * Without doing case to entities setted
	 */
	public void calculateCrossReputationFromMetricsMatching() {
		calculateCrossReputationFromMetricsMatching(metricsToImport);
	}
	
	/**
	 * Without doing case to entities and metricsToImport setted
	 * @param commMetricsToImport
	 */
	public void calculateCrossReputationFromEvaluation() {
		List<CommunityMetricToImport> commMetricsToImport = 
			new ArrayList<CommunityMetricToImport>();
		Set<Entity> entities = new HashSet<Entity>();
		for(Evaluation evaluation : evaluations) {
			if(evaluation.getCommunity() == community)
				continue;
			List<CommunityMetricToImport> comMetrToImp = 
				ConfigureModel.buildCommunityMetricToImportToAllMetrics(
				evaluation.getCommunity(),this.community,evaluation.getMetric());
			if(comMetrToImp == null || comMetrToImp.size() == 0)
				continue;
			for(int i = 0; i < comMetrToImp.size(); i++) {
				if(comMetrToImp.get(i).isContained(commMetricsToImport)) {
					comMetrToImp.remove(i);
					i--;
				}
			}
			commMetricsToImport.addAll(comMetrToImp);
			entities.add(evaluation.getDestinationEntity());		
		}
		calculateAllReputationByMetricsMatchingAndEntities(
				commMetricsToImport, entities);
	}
	
	/**
	 * Without doing case to entities and metricsToImport setted
	 * @param commMetricsToImport
	 */
	public void calculateCrossReputationFromMetricsMatching(
			List<CommunityMetricToImport> commMetricsToImport) {
		Set<Entity> entitiesReputed = new HashSet<Entity>();
		for(CommunityMetricToImport commMeticToImp : commMetricsToImport) {
			for(Evaluation evaluation : evaluations) {
				if(entitiesReputed.contains(evaluation.getDestinationEntity()))
					continue;
				if(evaluation.getCommunity() == commMeticToImp.getCommunity() &&
						evaluation.getMetric() == commMeticToImp.getMetric()) {
					calculateCrossReputationByMetricsMatchingAndEntity(
							commMetricsToImport, evaluation.getDestinationEntity());
					entitiesReputed.add(evaluation.getDestinationEntity());					
				}
			}
		}
	}
	
	public void calculateCrossReputationByMetricsMatchingAndEntity(List<CommunityMetricToImport> 
			metricsToImport, Entity entity) {
		Map<Metric,List<CommunityMetricToImport>> metricsByDestination = 
			sortMetricsByDestinationMetric(metricsToImport);
		for(Metric destinationMetric : metricsByDestination.keySet()) {
			calculateCrossReputationByMetricsAndEntity(metricsByDestination.
					get(destinationMetric),destinationMetric,entity);							
		}
	}
	
	public Object calculateCrossReputationByMetricsAndEntity(List<CommunityMetricToImport> 
			metricsToImport, Metric destinationMetric, Entity entity) {
		Object values = null;		
		for(CommunityMetricToImport metricToImport : metricsToImport) {
			values = destinationMetric.sumValues(values,calculateSingleReputation(
					metricToImport,destinationMetric,entity));
		}
		System.out.println("-Eva:"+entity.getUniqueIdentificator()+","+destinationMetric.
				getIdentificator()+","+values);
		updateEvaluation(entity,destinationMetric,
				destinationMetric.getScale().adaptToScale(values));
		return values;
	}	
	
	public Map<Metric,List<CommunityMetricToImport>> sortMetricsByDestinationMetric(
			List<CommunityMetricToImport> metricsToImport) {
		Map<Metric,List<CommunityMetricToImport>> metricsSortByDestination = 
			new HashMap<Metric,List<CommunityMetricToImport>>();
		for(CommunityMetricToImport metricToImport : metricsToImport) {
			List<CommunityMetricToImport> metricsByDestination = 
				metricsSortByDestination.get(metricToImport.getDestinationMetric());
			if(metricsByDestination == null) {
				metricsByDestination = new ArrayList<CommunityMetricToImport>();
				metricsByDestination.add(metricToImport);
				metricsSortByDestination.put(metricToImport.getDestinationMetric(),
						metricsByDestination);
			} else if (!metricToImport.isContained(metricsByDestination)){
				metricsByDestination.add(metricToImport);
			}
		}
		return metricsSortByDestination;
	}
	
	public void calculateAllReputationByDestinationMetricsAndEntity(
			Metric destinationMetric,Entity entity) {
		calculateCrossReputationByMetricsAndEntity(metricsToImport,
				destinationMetric, entity);				
	}	
	
	public void calculateAllReputationByEntity(Entity entity) {
		calculateCrossReputationByMetricsMatchingAndEntity(metricsToImport,entity);				
	}
	
	/*public void calculateAllReputationByMetricsMatchingAndEntity(List<CommunityMetricToImport>
			metricsToImport, Entity entity) {
		calculateCrossReputationByMetricsMatchingAndEntity(metricsToImport,entity);		
	}*/
	
	public void calculateAllReputationByMetricsMatchingAndEntities(List<CommunityMetricToImport>
			metricsFromCommunities, Set<Entity> entities) {
		for(Entity entity : entities) {
			calculateCrossReputationByMetricsMatchingAndEntity(metricsFromCommunities,
					entity);
		}
	}
	
	public void calculateAllReputationByMetricsMatching(
			List<CommunityMetricToImport> metricsFromCommunities) {
		for(Entity entity : entities) {
			calculateCrossReputationByMetricsMatchingAndEntity(metricsFromCommunities, entity);
		}
	}
	
	public void calculateCrossReputationByDestinationMetric(Metric destinationMetric) {
		for(Entity entity : entities) {
			calculateCrossReputationByMetricsAndEntity(metricsToImport,
					destinationMetric, entity);
		}
	}
	
	public void calculateAllReputations() {
		calculateAllReputationByMetricsMatching(metricsToImport);		
	}
	
	public void addValueEvaluation(Entity entity, Metric metric, Object value) {
		for(Evaluation evaluation : evaluations) {
			if(evaluation.getDestinationEntity() == entity &&
					evaluation.getMetric() == metric) {
				evaluation.addValue(value);
				break;
			}
		}
		evaluations.add(new Evaluation(community,entity,metric,value));		
	}
	
	public void updateEvaluation(Entity entity, Metric metric, Object value) {
		for(Evaluation evaluation : evaluations) {
			if(evaluation.getDestinationEntity() == entity &&
					evaluation.getMetric() == metric) {
				evaluation.updateValue(value);
				break;
			}
		}
		evaluations.add(new Evaluation(community,entity,metric,value));	
	}			
	
	public boolean verifyModel() {
		//Comprobar que las metricas encajan
		//Comprobar que las entidades encajan!
		return true;
	}
	
	public Community getCommunity() {
		return community;
	}
	public void setCommunity(Community community) {
		this.community = community;
	}	
	public Set<Entity> getEntities() {
		return entities;
	}
	public void setEntities(Set<Entity> entities) {
		this.entities = entities;
	}
	public List<Evaluation> getEvaluations() {
		return evaluations;
	}
	public void setEvaluations(List<Evaluation> evaluations) {
		this.evaluations = evaluations;
	}
	public List<CommunityMetricToImport> getMetricsToImport() {
		return metricsToImport;
	}
	public void setMetricsToImport(List<CommunityMetricToImport> metricsToImport) {
		this.metricsToImport = metricsToImport;
	}	
	
}
