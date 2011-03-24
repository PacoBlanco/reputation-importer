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
		Ejecutor.ConfigureExtractorMode(Ejecutor.SCRAPPY_EXECUTOR_SERVER,urlServer);
		//Ejecutor.ConfigureExtractorMode(Ejecutor.SCRAPPY_EXECUTOR_LINE_COMMAND,urlServer);
		Ejecutor.ConfigureOpalServer("http://localhost/opal/opal.php");
		
		//Set All Model
		ConfigureModel.buildCrossReputationGlobalModel();
		
		//Set the destination community to import the other community reputations
		Community destinationCommunity = GlobalModel.getCommunities().get("semanticWiki");
		
		//Add all entities configured to all metrics in the destination community
		for(Entity entity : ConfigureModel.SetWikiUserEntitiesAndAccounts()) {
			destinationCommunity.addEntityToAllMetrics(entity);
		}
		
		//Set that all rest communities and theirs metrics are valid to the importation
		List<CommunityMetricToImport> metricsToImport = ConfigureModel.
			buildMetricsFromAllCommunitiesToAllMetrics(destinationCommunity);
				
		//Extract the reputation information of All Entities		
		for(Entity entity : GlobalModel.getEntities().values()) {
			//TODO: solo entidades que esten en metricsFromCommunity!!!
			Map<Community,EntityIdentifier> communityEntity = entity.getIdentificatorInCommunities();
			for(Community community : communityEntity.keySet()) {
				String urlDomain = communityEntity.get(community).getUrl();
				if(urlDomain == null) {
					System.out.println("Info:"+communityEntity.get(community).getName()+"("+
							entity.getUniqueIdentificator()+") has null url in:"+community.getName());
					continue;
				}						
				Map<Metric,Object> reputationMap = Scrapper.ExtractReputation(urlDomain);				
				if(reputationMap == null || reputationMap.isEmpty()) {
					continue;
				}
				if(reputationMap.size() == 1 && community.getMetrics().size() == 1) {
					Metric metric = (Metric)community.getMetrics().toArray()[0];
					for(Object value : reputationMap.values()) {
						GlobalModel.addEvaluation(new Evaluation(community, entity,
								metric,value));
						System.out.println("Ent:"+entity.getUniqueIdentificator()+" Com: "
						+community.getName()+ " url:"+communityEntity.get(community).getUrl()
						+" met:"+metric.getIdentificator()+" rep:"+value);
					}
				} else {
					for(Metric metric : reputationMap.keySet()) {
						Metric sourceMetric = null;
						for(Metric comMetric : community.getMetrics()) {
							if(metric == null || !metric.getIdentificator().equalsIgnoreCase(
									comMetric.getIdentificator())) {
								continue;										
							}
							sourceMetric = comMetric;
							break;
						}
						if(sourceMetric == null) {							
							System.out.println("INFO: metric parsed("+(metric==null?null:metric.getIdentificator())
								+") does not correspond to any metric of the community("+community.getName()+
								"):"+community.getMetrics()+". Its score is ignored");
							continue;
						}
						GlobalModel.addEvaluation(new Evaluation(community,
								entity,sourceMetric,reputationMap.get(metric)));
						System.out.println("Ent:"+entity.getUniqueIdentificator()+" Com:"
						+community.getName()+ " url:"+communityEntity.get(community).getUrl()
						+" met:"+sourceMetric.getIdentificator()+" rep:"+reputationMap.get(metric));						
					}							
				}
				//TODO:Method to extract reputation that associated the metric with the value				
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
