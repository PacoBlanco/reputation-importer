package cross.reputation.model;

import com.hp.hpl.jena.rdf.model.Resource;

public class ScaleCorrelation {
	private Resource resource;
	private Scale sourceScale;
	private Scale targetScale;
	private Double correlationValue;
	
	public Resource getResource() {
		return resource;
	}	
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	public Scale getSourceScale() {
		return sourceScale;
	}
	public void setSourceScale(Scale sourceScale) {
		this.sourceScale = sourceScale;
	}
	public Scale getTargetScale() {
		return targetScale;
	}
	public void setTargetScale(Scale targetScale) {
		this.targetScale = targetScale;
	}
	public Double getCorrelationValue() {
		return correlationValue;
	}
	public void setCorrelationValue(Double correlationValue) {
		this.correlationValue = correlationValue;
	}
	public String toString(String offset) {
		StringBuilder result = new StringBuilder(
				offset+"sourceScale:"+sourceScale);
		result.append("\n"+offset+"targetScale:"+targetScale);
		result.append("\n"+offset+"correlationValue:"+correlationValue);
		return result.toString();
	}
}
