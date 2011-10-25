package cross.reputation.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Resource;

public class Metric {
	private Resource resource;
	private String identifier;
	private Dimension dimension;
	private Scale scale;
	private String description;
	
	public Metric() {		
	}
	
	public Metric(String identificator, Dimension dimension, Scale scale) {
		this.identifier = identificator;
		this.dimension = dimension;
		this.scale = scale;
	}
	
	public Resource getResource() {
		return resource;
	}	
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identificator) {
		this.identifier = identificator;
	}		
	public Dimension getDimension() {
		return dimension;
	}
	public void setDimension(Dimension dimension) {
		this.dimension = dimension;
	}
	public Scale getScale() {
		return scale;
	}
	public void setScale(Scale scale) {
		this.scale = scale;
	}	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Object sumValues(Object value, Object valueToSum) {
		return getScale().sumValues(value, valueToSum);
	}
	public Object doAverage(Object value, int elements) {
		return getScale().doAverage(value, elements);
	}
	public Object addTrust(Object value, Double trust) {
		return getScale().addTrust(value, trust);
	}
	public Object aggregateValues(Map<CommunityMetricToImport,Object> values) {
		return getScale().aggregateValues(values);
	}
	
	public String toString(String offset) {
		String result = offset+"identifier:"+identifier+"\n";
		result += offset+"description:"+description+"\n";
		result += offset+"dimension:"+
				((dimension==null)?"null":dimension.getName())+"\n";
		result += offset+"scale:"+
				((scale==null)?"null":scale+"\n"+scale.toString(offset+"     "));
		return result;
	}
}
