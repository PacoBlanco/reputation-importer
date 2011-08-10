package cross.reputation.model;

import java.util.List;

public class SqrtNumericTransformer extends ExponentialNumericTransformer {
	
	public SqrtNumericTransformer(Metric sourceMetric,
			Metric destinationMetric, Double correlationBetweenMetrics)
			throws Exception {
		super(sourceMetric, destinationMetric, correlationBetweenMetrics, 0.5);		
	}
	
	public SqrtNumericTransformer(Metric sourceMetric,
			Metric destinationMetric, List<Double> correlationBetweenMetrics)
			throws Exception {
		super(sourceMetric, destinationMetric, correlationBetweenMetrics, 0.5);		
	}
	
	static public List<Class<? extends MetricTransformer>> listSubclasses() {
		return null;
	}
}
