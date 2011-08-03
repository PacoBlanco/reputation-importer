package cross.reputation.model;

public class MetricMapping {
	Metric importedMetric;
	Metric resultMetric;
	double value;
	
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
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	
}
