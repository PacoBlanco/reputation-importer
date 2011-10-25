package cross.reputation.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cross.reputation.model.Entity;
import cross.reputation.model.Evaluation;
import cross.reputation.model.Metric;
import cross.reputation.model.MetricTransformer;
import cross.reputation.model.ReputationTransformer;
import cross.reputation.model.Scale;

public class GlobalModel {
	static private Map<String,Community> communities = new HashMap<String,Community>();
	static private Set<Category> categories = new HashSet<Category>();
	static private Map<String,Map<String,Double>> categoryMatching = 
		new HashMap<String,Map<String,Double>>();
	static private Map<Community,Map<Community,Double>> fixedTrustBetweenCommunities =
		new HashMap<Community,Map<Community,Double>>();
	static private Map<String,Metric> metrics = new HashMap<String,Metric>();
	static private Map<String,Entity> entities = new HashMap<String,Entity>();
	static private Map<String,MetricTransformer> metricTransformers = 
		new HashMap<String,MetricTransformer>();
	static private Map<String,Scale> scales = new HashMap<String,Scale>();
	//static private Map<Community,ReputationTransformer> reputationTransformers =
	//	 new HashMap<Community,ReputationTransformer>();
	static private List<Evaluation> evaluations = new ArrayList<Evaluation>();
	
	static public Map<String, Community> getCommunities() {
		return communities;
	}
	static public Set<Category> getCategories() {
		return categories;
	}
	static public Map<String,Map<String,Double>> getCategoryMatching() {
		return categoryMatching;
	}
	static public Double getCategoryLinealAverageMatching(Community sourceCommunity, 
			Community destinationCommunity) {
		Double total = null;
		for (Category category : destinationCommunity.getCategories()) {
			Double value = null;
			Map<String,Double> destinCategoryMatching = categoryMatching.get(category);
			for(Category sourceCategory : sourceCommunity.getCategories()) {
				if(destinCategoryMatching.containsKey(sourceCategory)) {
					if(value == null)
						value = destinCategoryMatching.get(sourceCategory);
					else
						value += destinCategoryMatching.get(sourceCategory);
				}
			}
			if(total != null)
				total += value;
			else
				total = value;
		}
		//System.out.println("value catMatc for:"+sourceCommunity.getName()+","+
		//		destinationCommunity.getName()+":"+(total == null ? 
		//		null : total/destinationCommunity.getCategories().length));
		return (total == null ? null : total/destinationCommunity.getCategories().size());
		
	}
	static public Map<Community,Map<Community,Double>> getFixedTrustBetweenCommunities() {
		return fixedTrustBetweenCommunities;
	}
	static public Double getFixedTrust(Community sourceCommunity, 
			Community destinationCommunity) {
		Map<Community,Double> sourceFixedTrusts = 
			fixedTrustBetweenCommunities.get(sourceCommunity);
		if(sourceFixedTrusts != null) {
			return sourceFixedTrusts.get(destinationCommunity);
		}
		return null;
	}
	static public Map<String, Metric> getMetrics() {
		return metrics;
	}
	static public Map<String, Entity> getEntities() {
		return entities;
	}
	static public Map<String,MetricTransformer> getMetricTransformers() {
		return metricTransformers;
	}
	static public Map<String, Scale> getScales() {
		return scales;
	}
	//static public Map<Community, ReputationTransformer> getReputationTransformers() {
	//	return reputationTransformers;
	//}
	static public List<Evaluation> getEvaluations() {
		return evaluations;
	}
	
	static public Community getCommunity(String communityName) {
		for(String community : communities.keySet()) {
			if(community.equalsIgnoreCase(communityName)) {
				return communities.get(community);
			}
		}
		return null;
	}
	
	static public void addFixedTrustBetweenCommunities(String sourceComName,
			String destinationComName, Double fixedValue) {
		if(sourceComName == null) {
			System.out.println("Error: source community Name is null");
			return;
		}
		if(destinationComName == null) {
			System.out.println("Error: destination community Name is null");
			return;
		}
		//System.out.println("Add fixed trust:"+communities.get(sourceComName)+","+
		//		communities.get(destinationComName)+","+fixedValue);
		addFixedTrustBetweenCommunities(communities.get(sourceComName),
				communities.get(destinationComName),fixedValue);
	}
	static public void addFixedTrustBetweenCommunities(Community sourceCoummunity,
			Community destinationCommunity,	Double fixedValue) {
		if(sourceCoummunity == null) {
			System.out.println("Error: source community is null");
			return;
		}
		if(destinationCommunity == null) {
			System.out.println("Error: destination community is null");
			return;
		}
		Map<Community, Double> sourceMap = fixedTrustBetweenCommunities.get(sourceCoummunity);
		if(sourceMap == null) {
			Map<Community,Double> destMap = new HashMap<Community,Double>();
			destMap.put(destinationCommunity,fixedValue);
			fixedTrustBetweenCommunities.put(sourceCoummunity,destMap);
		} else {
			sourceMap.put(destinationCommunity,fixedValue);
		}
	}	
	static public void addCategoryMatching(String sourceCategory, String destinationCategory,
			Double matching) {
		if(sourceCategory == null) {
			System.out.println("Error: source category is null");
			return;
		}
		if(destinationCategory == null) {
			System.out.println("Error: destination category is null");
			return;
		}
		Map<String, Double> sourceMap = categoryMatching.get(sourceCategory);
		if(sourceMap == null) {
			Map<String,Double> destMap = new HashMap<String,Double>();
			destMap.put(destinationCategory,matching);
			categoryMatching.put(sourceCategory,destMap);
		} else {
			sourceMap.put(destinationCategory,matching);
		}
	}
	static public void addCommunity(Community community) throws Exception {
		if(community == null) {
			ModelException.sendMessage(ModelException.ERROR, "Community to add"
					+" in GlobalModel is null");
			return;
		}
		if(community.getName() == null) {
			ModelException.sendMessage(ModelException.ERROR, "Community(resource:"
					+community.getResource()+") with null"
					+"identifier cannot be added to GlobalModel");
			return;
		}
		if(communities.containsKey(community.getName())) {
			ModelException.throwException(ModelException.COMMUNITY, "There is a " +
					"Community(resource:"+entities.get(community.getName()).
					getResource()+") already added in GlobalModel with the same"+
					"identifier(new resource:"+community.getResource()+")");
		}
		communities.put(community.getName(), community);
	}
	static public void addCommunities(List<Community> communitiess) throws Exception {
		if(communitiess == null || communitiess.isEmpty()) {
			ModelException.sendMessage(ModelException.ERROR,"There is no" +
					" Community in the model");
			return;
		}
		for(Community community : communitiess) {
			addCommunity(community);
		}
	}
	static public Category addCategory(String categoryName) {
		if(categoryName == null) {
			System.out.println("Error: category is null");
			return null;
		}
		Category category = new Category(categoryName);
		categories.add(category);
		return category;
	}
	static public Metric addMetric(Metric metric) throws Exception {
		if(metric == null) {
			ModelException.sendMessage(ModelException.ERROR, "Metric is null");
			return null;
		}
		if(metric.getIdentifier() == null) {
			ModelException.sendMessage(ModelException.ERROR, "Metric(resource:"
					+metric.getResource()+") with null identifier cannot be" +
					" added to GlobalModel");
			return null;
		}
		if(metrics.containsKey(metric.getIdentifier())) {
			ModelException.throwException(ModelException.COMMUNITY, "There is a " +
					"Metric(resource:"+entities.get(metric.getIdentifier()).getResource()
					+") already added in GlobalModel with the same identifier(new resource:"+
					metric.getResource()+")");
		}
		metrics.put(metric.getIdentifier(),metric);
		return metric;
	}
	static public void addMetrics(List<Metric> metricss) throws Exception {
		if(metricss == null || metricss.isEmpty()) {
			ModelException.sendMessage(ModelException.ERROR,"There is no Metric in the model");
			return;
		}
		for(Metric metric : metricss) {
			addMetric(metric);
		}		
	}
	static public Entity addEntity(Entity entity) throws Exception {
		if(entity == null) {
			ModelException.sendMessage(ModelException.ERROR,"Entity is null");
			return null;
		}
		if(entity.getUniqueIdentificator() == null) {
			ModelException.sendMessage(ModelException.ERROR, "Entity(resource:"
					+entity.getResource()+") with null identifier cannot be" +
					" added to GlobalModel");
			return null;
		}
		if(entities.containsKey(entity.getUniqueIdentificator())) {
			ModelException.throwException(ModelException.COMMUNITY, "There is a" +
					" Entity(resource:"+entities.get(entity.getUniqueIdentificator()).
					getResource()+") already" +	" added in GlobalModel with the same" +
					" identifier(new resource:"+entity.getResource()+")");
		}
		entities.put(entity.getUniqueIdentificator(),entity);
		return entity;
	}
	static public void addEntities(List<Entity> entitiess) throws Exception {
		if(entitiess == null || entitiess.isEmpty()) {
			ModelException.sendMessage(ModelException.ERROR, "There is no entity in the model");
			return;
		}
		for(Entity entity : entitiess) {
			addEntity(entity);
		}		
	}
	static public void addMetricTransformers(List<MetricTransformer> tranformerss) {
		if(tranformerss == null || tranformerss.isEmpty()) {
			System.out.println("Error: there is no MetricTransformer in the model");
			return;
		}
		for(MetricTransformer transformer : tranformerss) {
			metricTransformers.put(transformer.getIdentifier(), transformer);
		}		
	}
	static public void addMetricTransformer(MetricTransformer metricTransformer) {
		if(metricTransformer == null) {
			System.out.println("Error: metricTransformer is null");
			return;
		}
		/*for(int i = 0; i < metricTransformers.size(); i++) {
			if(metricTransformers.get(i).getDestinationMetric() == 
					metricTransformer.getDestinationMetric()
					&& metricTransformers.get(i).getSourceMetric() ==
					metricTransformer.getSourceMetric()) {
				metricTransformers.remove(i);
				break;
			}
		}*/
		metricTransformers.put(metricTransformer.getIdentifier(),metricTransformer);
	}
	static public void addScale(Scale scale) {
		if(scale == null) {
			System.out.println("Error: scale is null");
			return;
		}
		scales.put(scale.getName(), scale);
	}
	//static public void addReputationTransformers(ReputationTransformer reputationTransformer) {			
	//	reputationTransformers.put(reputationTransformer.getDestineCommunity(),reputationTransformer);
	//}
	static public void addEvaluation(Evaluation evaluation) {
		if(evaluation == null) {
			System.out.println("Error: evaluation is null");
			return;
		}
		for(int i = 0; i < evaluations.size(); i++) {
			if(evaluations.get(i).getCommunity() == evaluation.getCommunity() &&
			evaluations.get(i).getDestinationEntity() == evaluation.getDestinationEntity()
			&& evaluations.get(i).getMetric() == evaluation.getMetric()) {
				evaluations.remove(i);
				break;
			}
		}
		evaluations.add(evaluation);
	}
	
	static public void printEvaluations() {
		for(Evaluation evaluation : evaluations) {
			if(evaluation != null) {
				System.out.println("Evaluation:"+evaluation);
				System.out.println("  sourMetr:"+(evaluation.getMetric()==null?
						null:evaluation.getMetric().getIdentifier()));
				System.out.println("  Ent:"+(evaluation.getDestinationEntity()==null?
						null:evaluation.getDestinationEntity().getUniqueIdentificator()));
				System.out.println("  DesCom:"+(evaluation.getCommunity()==null?
						null:evaluation.getCommunity().getName()));
				List<Object> values = evaluation.getValues();
				if(values == null)
					System.out.println("  Val:"+values);
				else if (values.size() == 0)
					System.out.println("  Val:empty,"+values);
				else {
					for(Object value : values)
						System.out.println("  Val:"+value);
				}				
			} else
				System.out.println("Evaluation:"+evaluation);
		}
	}
}
