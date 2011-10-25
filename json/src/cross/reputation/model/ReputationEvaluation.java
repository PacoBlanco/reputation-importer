package cross.reputation.model;

import com.hp.hpl.jena.rdf.model.Resource;

public class ReputationEvaluation {
	private Resource resource;
	private String collectionIdentifier;
	private Entity target;
	private Metric hasMetric;
	private Object value;
	
	public Resource getResource() {
		return resource;
	}	
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	public String getCollectionIdentifier() {
		return collectionIdentifier;
	}
	public void setCollectionIdentifier(String collectionIdentifier) {
		this.collectionIdentifier = collectionIdentifier;
	}
	public Entity getTarget() {
		return target;
	}
	public void setTarget(Entity target) {
		this.target = target;
	}
	public Metric getHasMetric() {
		return hasMetric;
	}
	public void setHasMetric(Metric hasMetric) {
		this.hasMetric = hasMetric;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public String toString(String offset) {
		StringBuilder result = new StringBuilder(offset+"target:"+target+"\n");
		result.append(offset+"collectionIdentifier:"+collectionIdentifier+"\n");
		result.append(offset+"hasMetric:"+hasMetric+"\n");
		result.append(offset+"value:"+value);
		return result.toString();
	}
	public String toLimitedString(String offset) {
		StringBuilder result = new StringBuilder(offset+"target:"+
				(target==null?"null":target.getUniqueIdentificator())+"\n");
		result.append(offset+"collectionIdentifier:"+collectionIdentifier+"\n");
		result.append(offset+"hasMetric:"+
				(hasMetric==null?"null":hasMetric.getIdentifier())+"\n");
		result.append(offset+"value:"+value);
		return result.toString();
	}
		
}
