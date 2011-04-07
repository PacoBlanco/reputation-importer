package cross.reputation.model;

public class SqrtNumericTransformer extends NumericTransformer {
	Double minimum = null;
	Double maximum = null;
	Double scale = null;
	Double linealScale = null;
	boolean linealBefore = true;
	
	public SqrtNumericTransformer(Metric sourceMetric, Metric destinationMetric,
			Double correlationBetweenMetrics) throws Exception {
		super(sourceMetric, destinationMetric, correlationBetweenMetrics);
		calculateTransformers();
	}
	
	public SqrtNumericTransformer(Metric sourceMetric, Metric destinationMetric,
			Double correlationBetweenMetrics, Double minimum, Double maximum, 
			boolean linealBefore) throws Exception {
		super(sourceMetric, destinationMetric, correlationBetweenMetrics);
		if(maximum == null) {
			throw new Exception("ERROR: maximum value cannot be null");
		}
		if(minimum == null){
			throw new Exception("ERROR: minimum value cannot be null");
		}
		if(maximum < minimum) {
			this.minimum = maximum;
			this.maximum = minimum;
		} else {
			this.minimum = minimum;
			this.maximum = maximum;
		}
		this.linealBefore = linealBefore;
		calculateTransformers();
	}
	
	private void calculateTransformers() {
		NumericScale sourceScale = (NumericScale) sourceMetric.getScale();
		NumericScale destinationScale = (NumericScale) destinationMetric.getScale();
		if(maximum == null) {
			if(sourceScale.getMaximum() != null && destinationScale.getMaximum() != null &&
					sourceScale.getMinimum() != null && destinationScale.getMinimum() != null) {
				scale = (destinationScale.getMaximum() - destinationScale.getMinimum())/
					Math.sqrt(sourceScale.getMaximum() - sourceScale.getMinimum());				
			} else if(sourceScale.getMaximum() != null && destinationScale.getMaximum() != null) {
				scale = destinationScale.getMaximum()/Math.sqrt(sourceScale.getMaximum());			
			} else if(sourceScale.getMinimum() != null && destinationScale.getMinimum() != null) {
				scale = destinationScale.getMinimum()/Math.sqrt(sourceScale.getMinimum());	
			}
		} else {
			if(linealBefore) {
				if(sourceScale.getMaximum() != null && destinationScale.getMaximum() != null &&
						sourceScale.getMinimum() != null && destinationScale.getMinimum() != null) {
					linealScale = (maximum - minimum)/
						(sourceScale.getMaximum() - sourceScale.getMinimum());
					scale = (destinationScale.getMaximum() - destinationScale.getMinimum())/
					Math.sqrt(maximum - minimum);
				} else if(sourceScale.getMaximum() != null && destinationScale.getMaximum() != null) {
					linealScale = maximum/sourceScale.getMaximum();
					scale = destinationScale.getMaximum()/Math.sqrt(maximum);										
				} else if(sourceScale.getMinimum() != null && destinationScale.getMinimum() != null) {
					linealScale = minimum/sourceScale.getMinimum();
					scale = destinationScale.getMinimum()/Math.sqrt(minimum);					
				}
			} else {
				if(sourceScale.getMaximum() != null && destinationScale.getMaximum() != null &&
						sourceScale.getMinimum() != null && destinationScale.getMinimum() != null) {
					scale = (maximum - minimum)/
							Math.sqrt(sourceScale.getMaximum() - sourceScale.getMinimum());
					linealScale = (destinationScale.getMaximum() - destinationScale.getMinimum())/
							(maximum - minimum);
				} else if(sourceScale.getMaximum() != null && destinationScale.getMaximum() != null) {
					scale = maximum/Math.sqrt(sourceScale.getMaximum());
					linealScale = destinationScale.getMaximum()/maximum;
				} else if(sourceScale.getMinimum() != null && destinationScale.getMinimum() != null) {
					scale = minimum/Math.sqrt(sourceScale.getMinimum());
					linealScale = destinationScale.getMinimum()/minimum;
				}
			}
		}
		System.out.println("NumeTrans:"+getSourceMetric().getIdentificator()+","+
		getDestinationMetric().getIdentificator()+","+getCorrelationBetweenMetrics()+": sc:"+
		scale+" linSc:"+linealScale);
	}
	
	@Override
	public Object tranformation(Object value, boolean adapt) throws Exception {
		if (!(value instanceof Double)) {
			return null;
		}
		Double valueTransformed = putInRange((Double)value, false);		
		if(scale == null) {
			valueTransformed = Math.sqrt(valueTransformed);
		} else {
			NumericScale sourceScale = (NumericScale) sourceMetric.getScale();
			NumericScale destinationScale = (NumericScale) destinationMetric.getScale();
			if(destinationScale.getMinimum() != null && destinationScale.getMaximum() != null
					&& sourceScale.getMinimum() != null && sourceScale.getMaximum() != null) {
				if(linealScale != null) {
					if(linealBefore) {
						valueTransformed = Math.sqrt((valueTransformed - sourceScale.getMinimum())
								*linealScale)*scale + destinationScale.getMinimum();
					} else {
						valueTransformed = (Math.sqrt(valueTransformed - sourceScale.getMinimum()))
								*scale * linealScale + destinationScale.getMinimum();
					}
				} else {
					valueTransformed = Math.sqrt(valueTransformed - sourceScale.getMinimum())*scale
							+ destinationScale.getMinimum();
				}
			} else {
				if(linealScale != null) {
					valueTransformed = Math.sqrt(valueTransformed) * scale * linealScale;
				} else {
					valueTransformed = Math.sqrt(valueTransformed) * scale;
				}
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
