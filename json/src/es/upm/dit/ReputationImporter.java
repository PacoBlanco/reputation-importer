package es.upm.dit;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import cross.reputation.model.Community;
import cross.reputation.model.CrossReputation;
import cross.reputation.model.Entity;
import cross.reputation.model.EntityIdentifier;
import cross.reputation.model.Evaluation;
import cross.reputation.model.GlobalModel;
import cross.reputation.model.Metric;
import cross.reputation.model.CommunityMetricToImport;



public class ReputationImporter {
	static private String urlServer = "http://localhost:3434/ejson/";
	
	static public void main(String[] args) throws Exception {		
		//Config extraction mode
		Ejecutor.ConfigureExtractorMode(Ejecutor.SCRAPPY_EXECUTOR_LINE_COMMAND,urlServer);
		
		//Set All Model
		ConfigureModel.buildCrossReputationGlobalModel();
		
		//Set the destination community to import the other community reputations
		Community destinationCommunity = GlobalModel.getCommunities().get("semanticWiki");		
		//Set that all rest communities and theirs metrics are valid to the importation
		List<CommunityMetricToImport> metricsToImport = ConfigureModel.
			buildMetricsFromAllCommunitiesToAllMetrics(destinationCommunity);
				
		//Extract the reputation information of All Entities		
		for(Entity entity : GlobalModel.getEntities().values()) {
			//TODO: solo entidades que esten en metricsFromCommunity!!!
			Map<Community,EntityIdentifier> communityEntity = entity.getIdentificatorInCommunities();
			for(Community community : communityEntity.keySet()) {
				Double reputation = null;
				if(community.getMetrics().size() == 1) {
					String urlDomain = communityEntity.get(community).getUrl();
					if(urlDomain == null) {
						System.out.println("Info:"+communityEntity.get(community).getName()+"("+
								entity.getUniqueIdentificator()+") has null url in:"+community.getName());
						continue;
					}						
					reputation = Scrapper.ExtractReputation(urlDomain);				
					if(reputation != null) {
						Metric metric = (Metric)community.getMetrics().toArray()[0];
						//System.out.println("Me:"+metric+" Sc:"+metric.getScale()+" rep:"+reputation);
						reputation = (Double) metric.getScale().adaptToScale((Object)reputation);
						GlobalModel.addEvaluation(new Evaluation(community, entity,
								metric,reputation));						
					}					
				}
				//TODO:Method to extract reputation that associated the metric with the value
				System.out.println("Ent:"+entity.getUniqueIdentificator()+" Com: "+community.getName()
						+ " url:"+communityEntity.get(community).getUrl()+" rep:"+reputation);
			}
		}		
		
		//Calculate Cross-Reputation to import the reputation information extracted
		System.out.println("\n*********************** Calculate Reputations***********************");
		CrossReputation crossReputation = new CrossReputation(destinationCommunity,
				metricsToImport, true, true);
		crossReputation.addAllEvaluations();
		crossReputation.calculateAllReputations();
		System.out.println("\n************************ Print Evaluations *************************");
		GlobalModel.printEvaluations();					
    }
	
}
