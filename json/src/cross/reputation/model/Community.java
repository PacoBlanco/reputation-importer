package cross.reputation.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Community {
	private String name;
	private String domainName;
	private String categories[];
	private Set<Metric> metrics = new HashSet<Metric>();
	private Map<Entity, Set<Metric>> entities = new HashMap<Entity, Set<Metric>>();
	
	public Community(String name, String domainName, String categories[], Metric metric) {
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
			this.categories = categories;
		if(metric == null) {
			System.out.println("Error: metric cannot be null in community:"+name);
		} else
			this.metrics.add(metric);
	}
	
	public Community(String name, String domainName, String categories[], Set<Metric> metrics) {
		this.name = name;
		this.domainName = domainName;		
		this.categories = categories;
		this.metrics = metrics;
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
	public String[] getCategories() {
		return categories;
	}
	public void setCategories(String[] categories) {
		this.categories = categories;
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
}
