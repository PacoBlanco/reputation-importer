package cross.reputation.model;

public abstract class NumericTransformer extends MetricTransformer {

	public NumericTransformer(Metric sourceMetric, Metric destinationMetric,
			Double correlationBetweenMetrics) throws Exception {
		super(sourceMetric, destinationMetric, correlationBetweenMetrics);
		if(!(sourceMetric.getScale() instanceof NumericScale)) {			
			throw new Exception("SourceMetric: "+sourceMetric.getIdentificator()+" has a scale:"
					+sourceMetric.getScale().getName()+" not numeric");
		}
		if(!(destinationMetric.getScale() instanceof NumericScale)) {			
			throw new Exception("SourceMetric: "+destinationMetric.getIdentificator()+" has a scale:"
					+destinationMetric.getScale().getName()+" not numeric");
		}		
	}
	
	abstract public Object tranformation(Object value, boolean adapt) throws Exception;
	public Double putInRange(Double value, boolean throwable) throws Exception {
		NumericScale sourceScale = (NumericScale) sourceMetric.getScale();
		if(value > sourceScale.getMaximum()) {
			if(throwable) {
				throw new Exception(value+" is greater than the maximum value of the scale");
			}
			value = sourceScale.getMaximum();
		}
		if(value < sourceScale.getMinimum()) {
			if(throwable) {
				throw new Exception(value+" is less than the maximum value of the scale");
			}
			value = sourceScale.getMinimum();
		}
		return value;
	}
	

}
