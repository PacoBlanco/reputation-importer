package cross.reputation.model;

import java.util.Arrays;
import java.util.List;

public class LogaritmicNumericTransformer extends NumericTransformer {
	Double base = 10.0;
	Double difference;
	Double scale;
	
	public LogaritmicNumericTransformer(Metric sourceMetric, Metric destinationMetric,
			List<Double> correlationBetweenMetrics) throws Exception  {
		super(sourceMetric, destinationMetric, correlationBetweenMetrics);
		calculateTransformers();
	}
	
	public LogaritmicNumericTransformer(Metric sourceMetric, Metric destinationMetric,
			Double correlationBetweenMetrics) throws Exception  {
		super(sourceMetric, destinationMetric,
				Arrays.asList(new Double[]{correlationBetweenMetrics}));
		calculateTransformers();
	}
	
	public LogaritmicNumericTransformer(Metric sourceMetric, Metric destinationMetric,
			List<Double> correlationBetweenMetrics, Double base) throws Exception  {
		super(sourceMetric, destinationMetric, correlationBetweenMetrics);
		this.base = base;
		calculateTransformers();
	}
	
	public LogaritmicNumericTransformer(Metric sourceMetric, Metric destinationMetric,
			Double correlationBetweenMetrics, Double base) throws Exception  {
		super(sourceMetric, destinationMetric, 
				Arrays.asList(new Double[]{correlationBetweenMetrics}));
		this.base = base;
		calculateTransformers();
	}
	
	private void calculateTransformers() {
		NumericScale sourceScale = (NumericScale) sourceMetric.getScale();
		NumericScale destinationScale = (NumericScale) destinationMetric.getScale();		
		if(sourceScale.getMaximum() != null && destinationScale.getMaximum() != null &&
				sourceScale.getMinimum() != null && destinationScale.getMinimum() != null) {
			difference = 1 - sourceScale.getMinimum();
			Double newSourceMaximum =  sourceScale.getMaximum() + difference;
			Double destinationDifference = destinationScale.getMaximum() - 
				destinationScale.getMinimum();
			scale = destinationDifference / Math.log(newSourceMaximum);
		} else if(sourceScale.getMaximum() != null && destinationScale.getMaximum() != null) {
			difference = sourceScale.getMaximum() - destinationScale.getMaximum();			
		} else if(sourceScale.getMinimum() != null && destinationScale.getMinimum() != null) {
			difference = sourceScale.getMinimum() - destinationScale.getMinimum();			
		}
		System.out.println("NumeTrans:"+getSourceMetric().getIdentifier()+","+
		getDestinationMetric().getIdentifier()+","+getCorrelationBetweenMetrics()+": sc:"+
		scale+" dif:"+difference);
	}
	
	@Override
	public Object tranformation(Object value, boolean adapt) throws Exception {
		if (!(value instanceof Double)) {
			return null;
		}
		Double valueTransformed = putInRange((Double)value, false);
		if(scale != null) {
			valueTransformed = scale * Math.log(valueTransformed + difference);			
		} else if(difference != null) {
			valueTransformed = valueTransformed - difference;
		}
		System.out.println("   value:"+value+" valueTransf:"+valueTransformed+(adapt?" valueAdapted:"+
				destinationMetric.getScale().adaptToScale(valueTransformed):" without adaption"));
		if(adapt) {		
			return destinationMetric.getScale().adaptToScale(valueTransformed);
		} else {
			return valueTransformed;
		}
	}

	public Double getBase() {
		return base;
	}

	public Double getDifference() {
		return difference;
	}

	public Double getScale() {
		return scale;
	}	

}
