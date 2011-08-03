package cross.reputation.model;

public class DimensionCorrelation {
	Dimension sourceDimension;
	Dimension targetDimension;
	Double correlationValue;	
	
	public Dimension getSourceDimension() {
		return sourceDimension;
	}
	public void setSourceDimension(Dimension sourceDimension) {
		this.sourceDimension = sourceDimension;
	}
	public Dimension getTargetDimension() {
		return targetDimension;
	}
	public void setTargetDimension(Dimension targetDimension) {
		this.targetDimension = targetDimension;
	}
	public Double getCorrelationValue() {
		return correlationValue;
	}
	public void setCorrelationValue(Double correlationValue) {
		this.correlationValue = correlationValue;
	}	
}
