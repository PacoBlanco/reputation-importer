package cross.reputation.model;

import java.util.Arrays;
import java.util.List;

public class LinealNumericTransformer extends NumericTransformer {
	private Double scale;
	private Double difference;
	
	public LinealNumericTransformer(Metric sourceMetric, Metric destinationMetric,
			List<Double> correlationBetweenMetrics) throws Exception {
		super(sourceMetric, destinationMetric, correlationBetweenMetrics);
		calculateTransformers();
	}
	
	public LinealNumericTransformer(Metric sourceMetric, Metric destinationMetric,
			Double correlationBetweenMetrics) throws Exception {
		super(sourceMetric, destinationMetric,
				Arrays.asList(new Double[]{correlationBetweenMetrics}));
		calculateTransformers();
	}

	@Override
	public Object tranformation(Object value, boolean adapt) throws Exception {
		if (!(value instanceof Double)) {
			return null;
		}
		Double valueTransformed = putInRange((Double)value, false);;
		if(scale != null) {
			valueTransformed = valueTransformed/scale - difference;			
		} else if(difference != null) {
			valueTransformed = valueTransformed - difference;
		}
		System.out.println("  value:"+value+" valueTransf:"+valueTransformed+(adapt?" valueAdapted:"+
				destinationMetric.getScale().adaptToScale(valueTransformed):" without adaption"));
		if(adapt) {		
			return destinationMetric.getScale().adaptToScale(valueTransformed);
		} else {
			return valueTransformed;
		}
	}
	
	private void calculateTransformers() {
		NumericScale sourceScale = (NumericScale) sourceMetric.getScale();
		NumericScale destinationScale = (NumericScale) destinationMetric.getScale();
		if(sourceScale.getMaximum() != null && destinationScale.getMaximum() != null &&
				sourceScale.getMinimum() != null && destinationScale.getMinimum() != null) {
			Double sourceDifference = sourceScale.getMaximum() - sourceScale.getMinimum();
			Double destinationDifference = destinationScale.getMaximum() - destinationScale.getMinimum();
			scale = Math.abs(sourceDifference / destinationDifference);
			difference = sourceScale.getMaximum()/scale - destinationScale.getMaximum();
		} else if(sourceScale.getMaximum() != null && destinationScale.getMaximum() != null) {
			difference = sourceScale.getMaximum() - destinationScale.getMaximum();			
		} else if(sourceScale.getMinimum() != null && destinationScale.getMinimum() != null) {
			difference = sourceScale.getMinimum() - destinationScale.getMinimum();			
		}
		//System.out.println("NumeTrans:"+getSourceMetric().getIdentificator()+","+
		//getDestinationMetric().getIdentificator()+","+getCorrelationBetweenMetrics()+": sc:"+
		//scale+" dif:"+difference);
	}

	public Double getScale() {
		return scale;
	}

	public Double getDifference() {
		return difference;
	}	
	
	static public List<Class<? extends MetricTransformer>> listSubclasses() {
		return null;
	}
	
	public String toReducedString(String offset) {
		StringBuilder result = new StringBuilder(offset+"sourceMetric:" + getSourceMetric());
		result.append("\n"+offset+"destinationMetric:" +getDestinationMetric());
		result.append("\n"+offset+"correlationBetMetrics:" +getCorrelationBetweenMetrics());
		result.append("\n"+offset+"difference:"+getDifference());
		result.append("\n"+offset+"scale:" + getScale());
		return result.toString();
	}
}
