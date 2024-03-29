package es.upm.dit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import cross.reputation.model.Category;
import cross.reputation.model.Dimension;
import cross.reputation.model.Entity;
import cross.reputation.model.EntityIdentifier;
import cross.reputation.model.ExponentialNumericTransformer;
import cross.reputation.model.GlobalModel;
import cross.reputation.model.Community;
import cross.reputation.model.LogaritmicNumericTransformer;
import cross.reputation.model.Metric;
import cross.reputation.model.CommunityMetricToImport;
import cross.reputation.model.MetricTransformer;
import cross.reputation.model.LinealNumericTransformer;
import cross.reputation.model.Scale;
import cross.reputation.model.NumericScale;
import cross.reputation.model.SqrtNumericTransformer;

public class ConfigureModel {
	
	static public void buildCrossReputationGlobalModel() throws Exception {
		//Set the dimensions of each scale, the scale of each metric, the metrics
		//of each community, the categories of each community and the communities
		Dimension reputationInQandA = new Dimension("ReputationInQandA");
		Dimension projectsReputation = new Dimension("ReputationInProject");
		Dimension rankReputation = new Dimension("rankReputation");		
		
		
		Category qandACategory = GlobalModel.addCategory("QandA");
		Category securityWebAppCategory = GlobalModel.addCategory("SecurityWebApp");
		Category projectConnCategory = GlobalModel.addCategory("ProjectConnection");
		
		
		GlobalModel.addScale(new NumericScale("stackOverflowScale",200000.0,0.0,1.0));
		GlobalModel.addMetric(new Metric("stackOverflowMetric", reputationInQandA,
				GlobalModel.getScales().get("stackOverflowScale")));
		Category stackOverflowCategories[] = {qandACategory};
		GlobalModel.addCommunity(new Community("stackoverflow.com","stackoverflow.com",
				stackOverflowCategories,GlobalModel.getMetrics().get("stackOverflowMetric")));		
		
		GlobalModel.addScale(new NumericScale("serverFaultScale",20000.0,0.0,1.0));
		GlobalModel.addMetric(new Metric("serverFaultMetric", reputationInQandA,
				GlobalModel.getScales().get("serverFaultScale")));
		Category serverFaultCategories[] = {qandACategory};
		GlobalModel.addCommunity(new Community("serverfault.com","serverfault.com",
				serverFaultCategories,GlobalModel.getMetrics().get("serverFaultMetric")));
				
		GlobalModel.addScale(new NumericScale("webAppsStackExchangeScale",20000.0,0.0,1.0));
		GlobalModel.addMetric(new Metric("webAppsStackExchangeMetric", reputationInQandA,
				GlobalModel.getScales().get("webAppsStackExchangeScale")));
		Category webAppsStackExchangeCategories[] = {qandACategory};
		GlobalModel.addCommunity(new Community("webapps.stackexchange.com","webapps.stackexchange.com",
				webAppsStackExchangeCategories,GlobalModel.getMetrics().get("webAppsStackExchangeMetric")));
				
		GlobalModel.addScale(new NumericScale("questionsSecuritytubeScale",20000.0,0.0,1.0));
		GlobalModel.addMetric(new Metric("questionsSecuritytubeMetric", reputationInQandA,
				GlobalModel.getScales().get("questionsSecuritytubeScale")));
		Category questionsSecuritytubeCategories[] = {qandACategory};
		GlobalModel.addCommunity(new Community("questions.securitytube.net","questions.securitytube.net",
				questionsSecuritytubeCategories,GlobalModel.getMetrics().get("questionsSecuritytubeMetric")));
				
		GlobalModel.addScale(new NumericScale("security.StackexchangeScale",2000.0,0.0,1.0));
		GlobalModel.addMetric(new Metric("security.StackexchangeMetric", reputationInQandA,
				GlobalModel.getScales().get("security.StackexchangeScale")));
		Category securityStackexchangeCategories[] = {qandACategory};
		GlobalModel.addCommunity(new Community("security.stackexchange.com","security.stackexchange.com",
				securityStackexchangeCategories,GlobalModel.getMetrics().get("security.StackexchangeMetric")));
		
		GlobalModel.addScale(new NumericScale("semanticWikiScale",10.0,0.0,1.0));
		GlobalModel.addMetric(new Metric("semanticWikiMetric", reputationInQandA,
				GlobalModel.getScales().get("semanticWikiScale")){
			public Object aggregateValues(Map<CommunityMetricToImport,Object> values) {
				Object total = null;
				int sum = 0;
				for(CommunityMetricToImport comMetToImp: values.keySet()) {
					if(comMetToImp.getCommunity() ==
							GlobalModel.getCommunities().get("ohloh.net")) {
						if(total != null) {
							total = getScale().mulValues(total,values.get(comMetToImp),0.1/0.9);
						} else {
							total = values.get(comMetToImp);
						}
						if(sum == 0) {
							sum = 1;
						}
						//System.out.println("new total in mul:"+total);
					}										
				}
				for(CommunityMetricToImport comMetToImp: values.keySet()) {
					if(comMetToImp.getCommunity() ==
						GlobalModel.getCommunities().get("ohloh.net")) {
						continue;
					}
					//System.out.println("total:"+total+" next value to sum:"+values.get(comMetToImp));					
					total = getScale().sumValues(total, values.get(comMetToImp));
					sum++;
				}				
				//return doAverage(total,sum);
				return total;
			}
		});		
		Category semanticWikiCategories[] = {securityWebAppCategory};
		Community wiki = new Community("semanticWiki","lab.gsi.dit.upm.es/semanticwiki",
				semanticWikiCategories,GlobalModel.getMetrics().get("semanticWikiMetric"));
		
		Category ohlohCategories[] = {projectConnCategory};		
		Set<Metric> ohlohMetrics = new HashSet<Metric>();
		GlobalModel.addScale(new NumericScale("ohlohKudoScale",10.0,0.0,1.0));
		Metric ohlohKudoMetric = GlobalModel.addMetric(new Metric("ohlohKudoMetric",
				projectsReputation,GlobalModel.getScales().get("ohlohKudoScale")));
		ohlohMetrics.add(ohlohKudoMetric);	
		GlobalModel.addScale(new NumericScale("ohlohRankScale",450000.0,0.0,1.0));
		Metric ohlohRankMetric = GlobalModel.addMetric(new Metric("ohlohRankMetric", rankReputation,
				GlobalModel.getScales().get("ohlohRankScale")));
		ohlohMetrics.add(ohlohRankMetric);		
		GlobalModel.addCommunity(new Community("ohloh.net","ohloh.net",
				ohlohCategories,ohlohMetrics));
		
		GlobalModel.addScale(new NumericScale("slackersScale",10.0,0.0,1.0));
		GlobalModel.addMetric(new Metric("slackersMetric", reputationInQandA,
				GlobalModel.getScales().get("slackersScale")));
		Category slackersCategories[] = {GlobalModel.addCategory("QandA")};
		GlobalModel.addCommunity(new Community("sla.ckers.org","sla.ckers.org",
				slackersCategories,GlobalModel.getMetrics().get("slackersMetric")));
		
		
		GlobalModel.addCommunity(wiki);
		
		//Configure all metric transformers that we need
		//GlobalModel.addMetricTransformer(new LogaritmicNumericTransformer(GlobalModel.getMetrics().
		//		get("stackOverflowMetric"),GlobalModel.getMetrics().get("semanticWikiMetric"),1.0));
		//GlobalModel.addMetricTransformer(new LogaritmicNumericTransformer(GlobalModel.getMetrics().
		//		get("serverFaultMetric"),GlobalModel.getMetrics().get("semanticWikiMetric"),1.0));
		GlobalModel.addMetricTransformer(new SqrtNumericTransformer(GlobalModel.getMetrics().
				get("stackOverflowMetric"),GlobalModel.getMetrics().get("semanticWikiMetric"),1.0));
		//Cube root = exponent of 0.333:
		GlobalModel.addMetricTransformer(new ExponentialNumericTransformer(GlobalModel.getMetrics().
				get("serverFaultMetric"),GlobalModel.getMetrics().get("semanticWikiMetric"),1.0,0.333));
		GlobalModel.addMetricTransformer(new LogaritmicNumericTransformer(GlobalModel.getMetrics().
				get("webAppsStackExchangeMetric"),GlobalModel.getMetrics().get("semanticWikiMetric"),1.0));
		GlobalModel.addMetricTransformer(new LogaritmicNumericTransformer(GlobalModel.getMetrics().
				get("questionsSecuritytubeMetric"),GlobalModel.getMetrics().get("semanticWikiMetric"),1.0));
		GlobalModel.addMetricTransformer(new LogaritmicNumericTransformer(GlobalModel.getMetrics().
				get("security.StackexchangeMetric"),GlobalModel.getMetrics().get("semanticWikiMetric"),1.0));				
		GlobalModel.addMetricTransformer(new LogaritmicNumericTransformer(GlobalModel.getMetrics().
				get("slackersMetric"),GlobalModel.getMetrics().get("semanticWikiMetric"),1.0));
		GlobalModel.addMetricTransformer(new LinealNumericTransformer(GlobalModel.getMetrics().
				get("ohlohKudoMetric"),GlobalModel.getMetrics().get("semanticWikiMetric"),0.9));
		GlobalModel.addMetricTransformer(new LogaritmicNumericTransformer(GlobalModel.getMetrics().
				get("ohlohRankMetric"),GlobalModel.getMetrics().get("semanticWikiMetric"),1.0));		
		//for(Community community : GlobalModel.getCommunities().values())
		//	printCommunity(community);
		
		//Set the fixed trust between communities table
		GlobalModel.addFixedTrustBetweenCommunities("stackoverflow.com",
				"semanticWiki",0.7);
		GlobalModel.addFixedTrustBetweenCommunities("serverfault.com",
				"semanticWiki",0.4);
		GlobalModel.addFixedTrustBetweenCommunities("webapps.stackexchange.com",
				"semanticWiki",0.5);
		GlobalModel.addFixedTrustBetweenCommunities("questions.securitytube.net",
				"semanticWiki",0.8);
		GlobalModel.addFixedTrustBetweenCommunities("security.stackexchange.com",
				"semanticWiki",1.0);
		//GlobalModel.addFixedTrustBetweenCommunities("ohloh.net",
		//		"semanticWiki",1.0);
		GlobalModel.addFixedTrustBetweenCommunities("sla.ckers.org",
				"semanticWiki",1.0);
		
		//Set the category matching table
		GlobalModel.addCategoryMatching("SecurityWebApp","QandA",0.9);
		GlobalModel.addCategoryMatching("SecurityWebApp","ProjectConnection",0.9);
	}
	
	static public List<CommunityMetricToImport> 
			buildMetricsFromAllCommunitiesToAllMetrics(Community community) {
		List<CommunityMetricToImport> communityMetricsToImport = 
				new ArrayList<CommunityMetricToImport>();		
		for(String communityId : GlobalModel.getCommunities().keySet()) {
			if(communityId == community.getName()) {
				continue;
			}
			Community sourceCommunity = GlobalModel.getCommunities().get(communityId);
			for(Metric sourceMetric : sourceCommunity.getMetrics()) {
				communityMetricsToImport.addAll(buildCommunityMetricToImportToAllMetrics(
						sourceCommunity, community, sourceMetric));
			}
		}
		//printMetricsFromCommunity(communityMetricsToImport);
		return communityMetricsToImport;
	}
	
	static public List<CommunityMetricToImport> buildCommunityMetricToImportToAllMetrics(
			Community sourceCommunity,Community destinationCommunity,Metric sourceMetric){
		List<CommunityMetricToImport> metricsToImport = 
				new ArrayList<CommunityMetricToImport>();
		Double categMatching = GlobalModel.getCategoryLinealAverageMatching(
				sourceCommunity, destinationCommunity);
		Double fixedValue = GlobalModel.getFixedTrust(sourceCommunity, 
				destinationCommunity);
		//System.out.println("-CalcTrust: categ:"+categMatching+" fix:"+fixedValue);		
		if(categMatching != null || fixedValue != null) {
			for(Metric destinationMetric : destinationCommunity.getMetrics()) {
				metricsToImport.add(new CommunityMetricToImport(sourceCommunity,destinationCommunity,
						sourceMetric, destinationMetric, categMatching, fixedValue));
			}
		} else {
			System.out.println("INFO: no category matching neither fixed value between:"+
					sourceCommunity.getName()+" and "+destinationCommunity.getName());
		}
		return metricsToImport;
	}
	
	static public void printMetricsFromCommunity(List<CommunityMetricToImport> metricsFromCommunities) {
		for(CommunityMetricToImport metriCom : metricsFromCommunities) {
			if(metriCom != null) {
				System.out.println("MetriCom:"+metriCom);
				System.out.println("  SourCom:"+(metriCom.getCommunity() == null?
						null:metriCom.getCommunity().getName()));
				System.out.println("  SourMet:"+(metriCom.getMetric() == null?
						null:metriCom.getMetric().getIdentifier()));
				System.out.println("  DesCom:"+(metriCom.getDestinationCommunity() == null?
						null:metriCom.getDestinationCommunity().getName()));
				System.out.println("  DesMet:"+(metriCom.getDestinationMetric() == null?
						null:metriCom.getDestinationMetric().getIdentifier()));
				System.out.println("  Tru:"+metriCom.getTrust());
			} else
				System.out.println("MetriCom:"+metriCom);
		}
	}
	
	static public void printCommunity(Community community) {
		System.out.println(community.getName()+":");
		Set<Metric> metrics = community.getMetrics();
		if(metrics != null) {
			for(Metric metric : metrics) {
			if(metric != null) {
					System.out.println("  Metric:"+metric.getIdentifier());
					System.out.println("    Dim:"+(metric.getDimension() == null?
							null:metric.getDimension().getName()) );
					System.out.println("    Sca:"+(metric.getScale() == null?
							null:metric.getScale().getName()) );
				} else
					System.out.println("  Metric:"+metric);			
			}
		} else
			System.out.println("  Metrics:"+metrics);
		for(Entity entity : community.getEntities().keySet()) {
			if(entity != null) {
				System.out.println("  Entity:"+entity.getUniqueIdentificator());
				metrics = community.getEntities().get(entity);
				if(metrics != null) {
					for(Metric metric : metrics) {
						if(metric != null)
							System.out.println("    Metric:"+metric.getIdentifier());
						else
							System.out.println("    Metric:"+metric);
					}
				} else
					System.out.println("    Metrics:"+null);
			} else
				System.out.println("  Entity:"+entity);
		}
	}
	
	static public Collection<Entity> SetWikiUserEntitiesAndAccounts() throws Exception {
		/*Entity administrator = GlobalModel.addEntity(new Entity("Administrator"));
		administrator.addIdentificatorInCommunities(GlobalModel.getCommunities().get("serverfault.com"),
				new EntityIdentifier("Ben Torell",null));
		Entity pblanco = GlobalModel.addEntity(new Entity("PBlanco"));
		pblanco.addIdentificatorInCommunities(GlobalModel.getCommunities().get("serverfault.com"),
				new EntityIdentifier("wayne koorts",null));
		pblanco.addIdentificatorInCommunities(GlobalModel.getCommunities().get("security.stackexchange.com"),
				new EntityIdentifier("kk-sjp-kk","http://security.stackexchange.com/users/1337/sjp"));
		pblanco.addIdentificatorInCommunities(GlobalModel.getCommunities().get("ohloh.net"),
				new EntityIdentifier("Arjan van de Ven",null));
		Entity jAMaldonado = GlobalModel.addEntity(new Entity("Jamaldonado"));
		jAMaldonado.addIdentificatorInCommunities(GlobalModel.getCommunities().get("questions.securitytube.net"),
				new EntityIdentifier("Andre G",null));
		jAMaldonado.addIdentificatorInCommunities(GlobalModel.getCommunities().get("security.stackexchange.com"), 
				new EntityIdentifier("Karrax",null));
		jAMaldonado.addIdentificatorInCommunities(GlobalModel.getCommunities().get("stackoverflow.com"), 
				new EntityIdentifier("Karrax",null));
		Entity dPozog = GlobalModel.addEntity(new Entity("DPozog"));
		dPozog.addIdentificatorInCommunities(GlobalModel.getCommunities().get("stackoverflow.com"),
				new EntityIdentifier("347915/Jose",null));
		dPozog.addIdentificatorInCommunities(GlobalModel.getCommunities().get("security.stackexchange.com"), 
				new EntityIdentifier("sdanelson",null));
		Entity ebarear = GlobalModel.addEntity(new Entity("Ebarear"));
		ebarear.addIdentificatorInCommunities(GlobalModel.getCommunities().get("stackoverflow.com"), 
				new EntityIdentifier("Jon Skeet",null));
		Entity edukun = GlobalModel.addEntity(new Entity("Edukun"));
		edukun.addIdentificatorInCommunities(GlobalModel.getCommunities().get("ohloh.net"),
				new EntityIdentifier("Gavin Sharp",null));
		edukun.addIdentificatorInCommunities(GlobalModel.getCommunities().get("security.stackexchange.com"),
				new EntityIdentifier("Sairam Kunala",null));*/
		Entity racker = GlobalModel.addEntity(new Entity("Racker"));
		//racker.addIdentificatorInCommunities(GlobalModel.getCommunities().get("ohloh.net"),
		//		new EntityIdentifier("janosch","http://www.ohloh.net/p/linux-omap/contributors/34615288934090"));
		racker.addIdentificatorInCommunities(GlobalModel.getCommunities().get("sla.ckers.org"),
				new EntityIdentifier("rsnake",null));
		GetMoreAccounts();
		return GlobalModel.getEntities().values();
	}
	
	static public void GetMoreAccounts() {
		for(Entity entity : GlobalModel.getEntities().values()) {
			Map<Community,EntityIdentifier> usuario = entity.getIdentificatorInCommunities();
			//In this form of iteration, we dont search accounts in the new accounts found or
			//  accounts updated that have already been iterated
			for(Object object : usuario.keySet().toArray()) {
				Community community = (Community) object;
				Set<String> accounts = new HashSet<String>();				
				String userName = usuario.get(community).getName();
				//System.out.println(userName+":"+community);
				String url = usuario.get(community).getUrl();
				try {
					if(url != null) {
						accounts.add(url);
						accounts.addAll(Scrapper.MoreUserAccountsByURL(url));
					} else {
						accounts.addAll(Scrapper.UserAccounts(userName,
								community.getDomainName(),true));
					}
					
				} catch (Exception e) {
					System.out.println("Error to get more accounts for entity:"+
						entity.getUniqueIdentificator()+" with comunnity: "+community.getName()
						+(url==null?" and user:"+userName:" and url:"+url));
					e.printStackTrace();
				}
				if(accounts != null) {
					SetAccountsInEntity(entity, userName, accounts);
				}
			}			
		}		
	}
	
	static private void SetAccountsInEntity(Entity entity, String userName, Collection<String> accounts) {
		for(String accountName : accounts) {
			Community community = getCommunityByAccountName(accountName);
			if(community == null)
				continue;
			EntityIdentifier id = entity.getIdentificatorInCommunities().get(community);
			if(id == null) {
				System.out.println("New account:"+entity.getUniqueIdentificator()+","+
						community.getName()+","+userName+","+accountName);
				entity.addIdentificatorInCommunities(community,
					new EntityIdentifier(userName, accountName));
			} else if(id.getUrl() == null) {
				System.out.println("Update account:"+entity.getUniqueIdentificator()+","+
						community.getName()+","+userName+","+accountName);
				id.setUrl(accountName);
			}
		}
	}
	
	static public Community getCommunityByAccountName(String name) {
		name = name.replaceFirst("http://", "");
		name = name.replaceFirst("www.", "");
		for(Community community : GlobalModel.getCommunities().values()) {
			String communityName = community.getDomainName().replaceFirst("http://", "");
			communityName = communityName.replaceFirst("www.", "");
			//System.out.println("     "+name+" =? "+communityName);
			if(name.toLowerCase().startsWith(communityName.toLowerCase())) {
				return community;
			}
		}
		return null;
	}
	
}
