package cross.reputation.model;

import java.util.List;
import java.util.Map;

public class NumericScale extends Scale {
	private Double minimum;
	private Double maximum;
	private Double step;
	
	public NumericScale(String name, Double minimum, Double maximum, Double step) {
		super(name);
		this.minimum = minimum;
		this.maximum = maximum;
		if(maximum != null && minimum != null && maximum < minimum) {
			Double aux = this.minimum;
			this.minimum = this.maximum;
			this.maximum = aux;
		}
		this.step = step;
	}		

	public Double adaptToScale(Double value) {
		//System.out.println("    Value to adapt:"+maximum+","+minimum+","+step+"  <-  "+value);
		if(value == null)
			return null;
		if(minimum != null) {
			if(maximum != null) {
				return adaptToStep(Math.max(minimum, Math.min(maximum, value)));
			}
			return adaptToStep(Math.max(minimum, value));
		} 
		if(maximum != null) {
			return adaptToStep(Math.min(maximum, value));
		}
		return adaptToStep(value);
	}
	
	public Double adaptToStep (Double value) {
		if(step == null) {
			return value;
		}
		double rest = value % step;
		if(rest != 0) {
			return value - rest;
		}
		return value;
	}
	//No necessary to adapt to the maximum, minimum and step	
	public Object sumValues(Object value, Object valueToSum) {
		if(value == null)
			return valueToSum;
		if(valueToSum == null)
			return value;
		if(value instanceof Double && valueToSum instanceof Double)
			return (Object)((Double)value + (Double)valueToSum);
		return null;
	}
	//No necessary to adapt to the maximum, minimum and step	
	public Object mulValues(Object value, Object valueToSum, Double weight) {
		if(value == null)
			return valueToSum;
		if(valueToSum == null)
			return value;
		if(value instanceof Double && valueToSum instanceof Double)
			return (Object)((Double)value * (Double)valueToSum * weight);
		return null;
	}
	//No necessary to adapt to the maximum, minimum and step	
	public Object aggregateValues(Map<CommunityMetricToImport,Object> values) {
		Object totalValue = null;
		for(Object value : values.values()) {
			totalValue = sumValues(totalValue, value);
		}
		return doAverage(totalValue,values.size());		
	}
	//No necessary to adapt to the maximum, minimum and step
	public Object doAverage(Object value, int elements) {
		if(value == null)
			return null;
		if(elements <= 1)
			return value;
		if(value instanceof Double)
			return (Object)((Double)value/elements);
		return null;
	}
	//No necessary to adapt to the maximum, minimum and step	
	public Object addTrust(Object value, Double trust) {
		if(value == null || !(value instanceof Double))
			return value;
		if(trust == null)
			return (Object)0.0;
		return (Object)((Double)value * trust);			
	}
	
	@Override
	public Object adaptToScale(Object value) {
		if(value instanceof Double)
			return adaptToScale((Double)value);
		return null;
	}

	public Double getMinimum() {
		return minimum;
	}

	public void setMinimum(Double minimum) {
		this.minimum = minimum;
	}

	public Double getMaximum() {
		return maximum;
	}

	public void setMaximum(Double maximum) {
		this.maximum = maximum;
	}

	public Double getStep() {
		return step;
	}

	public void setStep(Double step) {
		this.step = step;
	}
	
}
