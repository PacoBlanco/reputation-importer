package cross.reputation.model;

public class SqrtNumericTransformer extends ExponentialNumericTransformer {
	
	public SqrtNumericTransformer(Metric sourceMetric,
			Metric destinationMetric, Double correlationBetweenMetrics)
			throws Exception {
		super(sourceMetric, destinationMetric, correlationBetweenMetrics, 0.5);		
	}
	
}
