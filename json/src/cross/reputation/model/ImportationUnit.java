package cross.reputation.model;

public class ImportationUnit {
	Community importedCommunity;
	Metric importedMetric;
	MetricTransformer metricTransformation;
	CollectingAlgorithm collectsReputationBy;
	TrustBetweenCommunities trust;
	
	public Community getImportedCommunity() {
		return importedCommunity;
	}
	public void setImportedCommunity(Community importedCommunity) {
		this.importedCommunity = importedCommunity;
	}
	public Metric getImportedMetric() {
		return importedMetric;
	}
	public void setImportedMetric(Metric importedMetric) {
		this.importedMetric = importedMetric;
	}
	public MetricTransformer getMetricTransformation() {
		return metricTransformation;
	}
	public void setMetricTransformation(MetricTransformer metricTransformation) {
		this.metricTransformation = metricTransformation;
	}
	public CollectingAlgorithm getCollectsReputationBy() {
		return collectsReputationBy;
	}
	public void setCollectsReputationBy(CollectingAlgorithm collectsReputationBy) {
		this.collectsReputationBy = collectsReputationBy;
	}
	public TrustBetweenCommunities getTrust() {
		return trust;
	}
	public void setTrust(TrustBetweenCommunities trust) {
		this.trust = trust;
	}
	
}
