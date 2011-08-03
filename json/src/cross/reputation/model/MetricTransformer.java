package cross.reputation.model;

import java.util.ArrayList;
import java.util.List;

abstract public class MetricTransformer {
	protected Metric sourceMetric;
	protected Metric destinationMetric;
	protected List<Double> correlationBetweenMetrics;
	protected String description;
	
	public MetricTransformer(Metric sourceMetric, Metric destinationMetric,
			List<Double> correlationBetweenMetrics) {
		this.sourceMetric = sourceMetric;
		this.destinationMetric = destinationMetric;
		this.correlationBetweenMetrics = correlationBetweenMetrics;
	}
	
	abstract public Object tranformation(Object value, boolean adapt) throws Exception;
	
	public Metric getSourceMetric() {
		return sourceMetric;
	}
	public void setSourceMetric(Metric sourceMetric) {
		this.sourceMetric = sourceMetric;
	}
	public Metric getDestinationMetric() {
		return destinationMetric;
	}
	public void setDestinationMetric(Metric destinationMetric) {
		this.destinationMetric = destinationMetric;
	}
	public Double getCorrelationBetweenMetrics() {
		if(correlationBetweenMetrics == null ||
				correlationBetweenMetrics.isEmpty()) {
			return 1.0; //maximum value
		}
		//Simple multiplication
		Double trust = 1.0;
		for(Double correlation : correlationBetweenMetrics) {
			if(correlation != null) {
				trust *= correlation;
			}
		}
		return trust;
	}
	
	public String toString(String offset) {
		String result = offset+"description:"+description+"\n";
		result += offset+"sourceMetric:"+sourceMetric;
		result += sourceMetric.toString(offset+"      ");
		result += offset+"destinationMetric:"+destinationMetric;
		result += destinationMetric.toString(offset+"      ");
		result += offset+"correlationBetweenMetrics:";
		for(Double value : correlationBetweenMetrics) {
			result += value+", ";
		}
		return result+"\n";
	}
	
	public void addCorrelationBetweenMetrics(Double correlationBetweenMetric) {
		if(correlationBetweenMetrics == null) {
			correlationBetweenMetrics = new ArrayList<Double>();
		}
		this.correlationBetweenMetrics.add(correlationBetweenMetric);
	}
	public void setCorrelationBetweenMetrics(List<Double> correlationBetweenMetrics) {
		this.correlationBetweenMetrics = correlationBetweenMetrics;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}	
}
