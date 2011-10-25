package cross.reputation.model;

import java.util.Map;

import com.hp.hpl.jena.rdf.model.Resource;

abstract public class Scale {
	private Resource resource;
	protected String name;
	
	public Scale() {		
	}

	public Scale(String name) {
		this.name = name;
	}
	
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
	
	abstract public Object adaptToScale(Object value);		
	abstract public Object sumValues(Object value, Object valueToSum);
	abstract public Object mulValues(Object value, Object valueToSum, Double weight);
	abstract public Object doAverage(Object value, int elements);
	abstract public Object addTrust(Object value, Double trust);
	abstract public Object aggregateValues(Map<CommunityMetricToImport,Object> values);
	abstract public String toString(String offset);
}