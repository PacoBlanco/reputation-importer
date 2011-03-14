package cross.reputation.model;

import java.util.List;
import java.util.Map;

abstract public class Scale {
	String name;

	public Scale(String name) {
		this.name = name;
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
	
}