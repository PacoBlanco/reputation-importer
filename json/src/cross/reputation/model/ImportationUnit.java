package cross.reputation.model;

import com.hp.hpl.jena.rdf.model.Resource;

public class ImportationUnit {
	private Resource resource;
	private Community importedCommunity;
	private Metric importedMetric;
	private MetricTransformer metricTransformation;
	private ReputationAlgorithmImplementation collectsReputationBy;
	private TrustBetweenCommunities trust;
	
	public Resource getResource() {
		return resource;
	}	
	public void setResource(Resource resource) {
		this.resource = resource;
	}
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
	public ReputationAlgorithmImplementation getCollectsReputationBy() {
		return collectsReputationBy;
	}
	public void setCollectsReputationBy(
			ReputationAlgorithmImplementation collectsReputationBy) {
		this.collectsReputationBy = collectsReputationBy;
	}
	public TrustBetweenCommunities getTrust() {
		return trust;
	}
	public void setTrust(TrustBetweenCommunities trust) {
		this.trust = trust;
	}
	public String toString(String offset) {
		String result = offset+"importedMetric:" +
			importedMetric;
        if(importedMetric != null) {
        	result += "\n"+importedMetric.toString(offset+"     ");
        }
        result += "\n"+offset+"importedCommunity:" +
        		importedCommunity;
        if(importedCommunity != null) {
        	result += "\n"+importedCommunity.toString(offset+"     ");
        }
        result += "\n"+offset+"metricTransformation:" +
        		metricTransformation;
        if(metricTransformation != null) {
        	result += "\n"+metricTransformation.toString(offset+"     ");
        }
        result += "\n"+offset + "collectsReputationBy:" +
        		collectsReputationBy;
        if(collectsReputationBy != null) {
        	result += "\n"+collectsReputationBy.toString(offset+"     ");
        }
        result += "\n"+offset+"trust:" +trust;
        if(trust != null) {
        	result += "\n"+trust.toString(offset+"     ");
        }
		return result;
	}
}
