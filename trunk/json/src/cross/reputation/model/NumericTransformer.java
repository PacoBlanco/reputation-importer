package cross.reputation.model;

import java.util.ArrayList;
import java.util.List;

public abstract class NumericTransformer extends MetricTransformer {

	public NumericTransformer(Metric sourceMetric, Metric destinationMetric,
			List<Double> correlationBetweenMetrics) throws Exception {
		super(sourceMetric, destinationMetric, correlationBetweenMetrics);
		if(!(sourceMetric.getScale() instanceof NumericScale)) {			
			throw new Exception("SourceMetric: "+sourceMetric.getIdentifier()+" has a scale:"
					+sourceMetric.getScale().getName()+" not numeric");
		}
		if(!(destinationMetric.getScale() instanceof NumericScale)) {			
			throw new Exception("SourceMetric: "+destinationMetric.getIdentifier()+" has a scale:"
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
	
	static public List<Class<? extends MetricTransformer>> listSubclasses() {
		List<Class<?  extends MetricTransformer>> list = 
			new ArrayList<Class<? extends MetricTransformer>>();
		list.add(ExponentialNumericTransformer.class);
		list.add(LinealNumericTransformer.class);
		list.add(LogaritmicNumericTransformer.class);
		return list;		
	}
}
