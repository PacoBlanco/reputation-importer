package cross.reputation.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Resource;

public class Community {
	private Resource resource;
	private String name;
	private String domainName;
	private List<Category> categories;
	private ReputationAlgorithmImplementation reputationModel;
	private Set<Metric> metrics = new HashSet<Metric>();
	private Map<Entity, Set<Metric>> entities = new HashMap<Entity, Set<Metric>>();
	
	public Community() {		
	}
	
	public Community(String name, String domainName, Category categories[], Metric metric) {
		if(name == null && domainName == null) {
			System.out.println("Error: name and domainName cannot be null in community creation");
		} else {
			if(name == null)
				this.name = domainName;
			else
				this.name = name;
			if(domainName == null)
				this.domainName = name;
			else
				this.domainName = domainName;
		}
		if(categories == null || categories.length == 0) {
			System.out.println("Error: categories cannot be null or emptly list");
		} else
			this.categories = Arrays.asList(categories);
		if(metric == null) {
			System.out.println("Error: metric cannot be null in community:"+name);
		} else
			this.metrics.add(metric);
	}
	
	public Community(String name, String domainName, Category categories[], Set<Metric> metrics) {
		this.name = name;
		this.domainName = domainName;		
		this.categories = Arrays.asList(categories);
		this.metrics = metrics;
	}
	
	public Resource getResource() {
		return resource;
	}	
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	public String getDomainName() {
		return domainName;
	}
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Category> getCategories() {
		return categories;
	}
	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}
	public void addCategory(Category category) {
		if(categories == null) {
			categories = new ArrayList<Category>();
		}
		categories.add(category);
	}
	public Set<Metric> getMetrics() {
		return metrics;
	}
	public void setMetrics(HashSet<Metric> metrics) {
		this.metrics = metrics;		
	}
	public void addMetric(Metric metric) {
		metrics.add(metric);
	}
	public Map<Entity, Set<Metric>> getEntities() {
		return entities;
	}	
	public ReputationAlgorithmImplementation getReputationModel() {
		return reputationModel;
	}
	public void setReputationModel(ReputationAlgorithmImplementation reputationModel) {
		this.reputationModel = reputationModel;
	}

	public void setEntities(Map<Entity, Set<Metric>> entities) {
		this.entities = entities;
	}
	public void addEntity(Entity entity) {
		if(entity == null) {
			System.out.println("Error: entity to add cannot be null in community:"+name);
			return;
		}
		if(!entities.containsKey(entity)) {
			entities.put(entity, new HashSet<Metric>());
		}
	}
	public void addEntityToAllMetrics(Entity entity) {
		if(entity == null) {
			System.out.println("Error: entity to add cannot be null in community:"+name);
			return;
		}
		if(!entities.containsKey(entity)) {
			entities.put(entity, metrics);
		} else {
			entities.get(entity).addAll(metrics);
		}
	}
	public void addEntity(Entity entity, Set<Metric> metrics) {
		if(entity == null) {
			System.out.println("Error: entity to add cannot be null in community:"+name);
			return;
		}
		if(metrics == null) {
			System.out.println("Error: metrics to add cannot be null in community:"+name);
			return;
		}
		if(!entities.containsKey(entity)) {
			entities.put(entity, metrics);
		} else {
			entities.get(entity).addAll(metrics);
		}
	}	
	public boolean verifyCommunity () {
		for(Set<Metric> metrics : entities.values()) {
			for(Metric metric : metrics) {
				if(!this.metrics.contains(metric)) {
					return false;
				}
			}
		}
		return true;
	}
	public String toString(String offset) {
		String result = offset+"name:"+name+"\n";
		result += offset+"domainName:"+domainName+"\n";
		result += offset+"categories:";
		if(categories != null) {
			for(Category category : categories) {
				result += category.getName()+", ";
			}
		}
		result += "\n"+offset+"metrics:";
		if(metrics != null) {
			for(Metric metric : metrics) {
				result += "\n"+offset+"     "+metric+"\n";
				result += "\n"+metric.toString(offset+"          ");
			}
		}
		result += "\n"+offset+"hasReputationModel:"+reputationModel;
		if(reputationModel != null) {
			result += "\n"+reputationModel.toString(offset+"     ")+"\n";
		}				
		result += offset+"entities:"+entities;
		return result;
	}
}
