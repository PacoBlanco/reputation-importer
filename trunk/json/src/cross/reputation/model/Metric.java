package cross.reputation.model;

import java.util.ArrayList;
import java.util.List;

public class Metric {
	private String identificator;
	private List<MetricTransformer> transformers = new ArrayList<MetricTransformer>();
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
	public List<MetricTransformer> getMetricTransformers() {
		return transformers;
	}
	public void setTransformers(List<MetricTransformer> transformers) {
		this.transformers = transformers;
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
	public void addMetricTransformer(MetricTransformer metricTransformer)  {
		transformers.add(metricTransformer);
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
}
