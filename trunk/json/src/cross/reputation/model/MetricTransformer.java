package cross.reputation.model;

abstract public class MetricTransformer {
	protected Metric sourceMetric;
	protected Metric destinationMetric;
	protected Double correlationBetweenMetrics;
	
	public MetricTransformer(Metric sourceMetric, Metric destinationMetric,
			Double correlationBetweenMetrics) {
		this.sourceMetric = sourceMetric;
		this.destinationMetric = destinationMetric;
		this.correlationBetweenMetrics = correlationBetweenMetrics;
	}
	
	abstract public Object tranformation(Object value, boolean adapt);
	
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
		return correlationBetweenMetrics;
	}
	public void setCorrelationBetweenMetrics(Double correlationBetweenMetrics) {
		this.correlationBetweenMetrics = correlationBetweenMetrics;
	}
}
