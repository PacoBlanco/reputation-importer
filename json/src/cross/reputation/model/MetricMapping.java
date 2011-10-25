package cross.reputation.model;

import com.hp.hpl.jena.rdf.model.Resource;

public class MetricMapping {
	private Resource resource;
	private Metric importedMetric;
	private Metric resultMetric;
	private Double value;
	
	public Resource getResource() {
		return resource;
	}	
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	public Metric getImportedMetric() {
		return importedMetric;
	}
	public void setImportedMetric(Metric importedMetric) {
		this.importedMetric = importedMetric;
	}
	public Metric getResultMetric() {
		return resultMetric;
	}
	public void setResultMetric(Metric resultMetric) {
		this.resultMetric = resultMetric;
	}
	public Double getValue() {
		return value;
	}
	public void setValue(Double value) {
		this.value = value;
	}
	
	public String toString(String offset) {
		String result = offset+"importedMetric:" +
        		getImportedMetric()+"\n"+
        		getImportedMetric().toString(offset+"     ");
        result += "\n"+offset+"resultMetric:" +
        		getResultMetric()+"\n"+
        		getResultMetric().toString(offset+"     ");
        result += "\n"+offset+"value:" + getValue();
		return result;
	}
}
