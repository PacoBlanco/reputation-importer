package cross.reputation.model;

import com.hp.hpl.jena.rdf.model.Resource;

public class DimensionCorrelation {
	private Resource resource;
	private Dimension sourceDimension;
	private Dimension targetDimension;
	private Double correlationValue;	
	
	public Resource getResource() {
		return resource;
	}	
	public void setResource(Resource resource) {
		this.resource = resource;
	}
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
	public String toString(String offset) {
		StringBuilder result = new StringBuilder(
				offset+"sourceDimension:"+sourceDimension);
		result.append("\n"+offset+"targetDimension:"+targetDimension);
		result.append("\n"+offset+"correlationValue:"+correlationValue);
		return result.toString();
	}
}
