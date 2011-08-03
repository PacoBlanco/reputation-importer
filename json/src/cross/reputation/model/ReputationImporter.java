package cross.reputation.model;

import java.util.ArrayList;
import java.util.List;

public class ReputationImporter extends ReputationAlgorithm {
	List<MetricMapping> mapsMetrics;
	//List<CommunityMetricToImport> importsFrom;
	List<ImportationUnit> importsFrom;
	
	public List<MetricMapping> getMapsMetrics() {
		return mapsMetrics;
	}
	public void addMapsMetrics(MetricMapping mapsMetric) {
		if(mapsMetrics == null) {
			mapsMetrics = new ArrayList<MetricMapping>();
		}
		mapsMetrics.add(mapsMetric);
	}
	public List<ImportationUnit> getImportsFrom() {
		return importsFrom;
	}
	public void addImportsFrom(ImportationUnit importsFrom) {
		if(this.importsFrom == null) {
			this.importsFrom = new ArrayList<ImportationUnit>();
		}
		this.importsFrom.add(importsFrom);
	}
	
}
