package cross.reputation.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Metric {
	private String identificator;
	private Dimension dimension;
	private Scale scale;
	
	public Metric(String identificator, Dimension dimension, Scale scale) {
		this.identificator = identificator;
		this.dimension = dimension;
		this.scale = scale;
	}
	
	public String getIdentificator() {
		return identificator;
	}
	public void setIdentificator(String identificator) {
		this.identificator = identificator;
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
}
