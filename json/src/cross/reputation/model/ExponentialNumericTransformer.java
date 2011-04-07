package cross.reputation.model;

public class ExponentialNumericTransformer extends NumericTransformer {
	Double scale = null;
	Double base = 2.0;
	
	public ExponentialNumericTransformer(Metric sourceMetric, Metric destinationMetric,
			Double correlationBetweenMetrics) throws Exception {
		super(sourceMetric, destinationMetric, correlationBetweenMetrics);
		calculateTransformers();
	}
	
	public ExponentialNumericTransformer(Metric sourceMetric, Metric destinationMetric,
			Double correlationBetweenMetrics, Double base) throws Exception {
		super(sourceMetric, destinationMetric, correlationBetweenMetrics);		
		this.base = base;
		calculateTransformers();
	}
	
	private void calculateTransformers() {
		NumericScale sourceScale = (NumericScale) sourceMetric.getScale();
		NumericScale destinationScale = (NumericScale) destinationMetric.getScale();
		if(sourceScale.getMaximum() != null && destinationScale.getMaximum() != null &&
				sourceScale.getMinimum() != null && destinationScale.getMinimum() != null) {
			scale = (destinationScale.getMaximum() - destinationScale.getMinimum())/
				Math.pow(sourceScale.getMaximum() - sourceScale.getMinimum(),base);				
		} else if(sourceScale.getMaximum() != null && destinationScale.getMaximum() != null) {
			scale = destinationScale.getMaximum()/Math.pow(sourceScale.getMaximum(),base);			
		} else if(sourceScale.getMinimum() != null && destinationScale.getMinimum() != null) {
			scale = destinationScale.getMinimum()/Math.pow(sourceScale.getMinimum(),base);	
		}
		System.out.println("NumeTrans:"+getSourceMetric().getIdentificator()+
				","+getDestinationMetric().getIdentificator()+","+
				getCorrelationBetweenMetrics()+": sc:"+scale);
	}
	
	@Override
	public Object tranformation(Object value, boolean adapt) throws Exception {
		if (!(value instanceof Double)) {
			return null;
		}
		Double valueTransformed = putInRange((Double)value, false);		
		if(scale == null) {
			valueTransformed = Math.pow(valueTransformed,base);
		} else {
			NumericScale sourceScale = (NumericScale) sourceMetric.getScale();
			NumericScale destinationScale = (NumericScale) destinationMetric.getScale();
			if(destinationScale.getMinimum() != null && destinationScale.getMaximum() != null
					&& sourceScale.getMinimum() != null && sourceScale.getMaximum() != null) {
				valueTransformed = Math.pow(valueTransformed - sourceScale.getMinimum(),
						base) * scale + destinationScale.getMinimum();				
			} else {
				valueTransformed = Math.pow(valueTransformed,base) * scale;				
			}			
		}
		System.out.println("   value:"+value+" valueTransf:"+valueTransformed+(adapt?" valueAdapted:"+
				destinationMetric.getScale().adaptToScale(valueTransformed):" without adaption"));
		if(adapt) {		
			return destinationMetric.getScale().adaptToScale(valueTransformed);
		} else {
			return valueTransformed;
		}
	}	

}

