package cross.reputation.model;

import java.util.ArrayList;
import java.util.List;

public class Evaluation {
	private Community community;
	private Entity destinationEntity;
	private Metric metric;
	private List<Object> values = new ArrayList<Object>();
	
	public Evaluation(Community community, Entity destinationEntity, Metric metric) {
		this.community = community;
		this.destinationEntity = destinationEntity;
		this.metric = metric;
	}
	
	public Evaluation(Community community, Entity destinationEntity, Metric metric, List<Object> values) {
		this.community = community;
		this.destinationEntity = destinationEntity;
		this.metric = metric;
		this.values = values;
	}
	
	public Evaluation(Community community, Entity destinationEntity, Metric metric, Object value) {
		this.community = community;
		this.destinationEntity = destinationEntity;
		this.metric = metric;
		values.add(value);
	}
	
	public Community getCommunity() {
		return community;
	}
	public void setCommunity(Community community) {
		this.community = community;
	}
	public Entity getDestinationEntity() {
		return destinationEntity;
	}
	public void setDestinationEntity(Entity destinationEntity) {
		this.destinationEntity = destinationEntity;
	}
	public Metric getMetric() {
		return metric;
	}
	public void setMetric(Metric metric) {
		this.metric = metric;
	}
	public List<Object> getValues() {
		return values;
	}
	public void setValues(List<Object> values) {
		this.values = values;
	}
	public void addValue(Object value) {
		values.add(value);
	}		
	public void addValues(List<Object> values) {
		values.addAll(values);
	}
	public void updateValue(Object value) {
		values.clear();
		values.add(value);
	}
	public void updateValues(List<Object> values) {
		values.clear();
		values.addAll(values);
	}
}
