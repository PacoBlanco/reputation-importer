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
	
	public String toString(String offset) {
		String result = super.toString(offset);
		result += "\n"+offset+"mapsMetrics size:"+((mapsMetrics==null)?
        		"null":mapsMetrics.size());
        if(mapsMetrics != null) {
	        for(MetricMapping metMap : mapsMetrics) {
	        	result+="\n"+offset+"     metricMapping:" + metMap + "\n" +
	        		metMap.toString(offset+"     ");
	        }
        }
        result += "\n"+offset+"importsFrom size:"+((importsFrom==null)?
        		"null":importsFrom.size());
        if(importsFrom != null) {
	        for(ImportationUnit impUnit : importsFrom) {
	        	result+="\n"+offset+"     importsFrom:" + impUnit + "\n" +
	        		impUnit.toString(offset+"     ");
	        }
        }
        return result;
	}
	
	static public List<Class<? extends ReputationAlgorithm>> listSubclasses() {
		return null;
	}
}
