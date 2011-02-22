package es.upm.dit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import cross.reputation.model.Dimension;
import cross.reputation.model.Entity;
import cross.reputation.model.EntityIdentifier;
import cross.reputation.model.GlobalModel;
import cross.reputation.model.Community;
import cross.reputation.model.LogaritmicNumericTransformer;
import cross.reputation.model.Metric;
import cross.reputation.model.CommunityMetricToImport;
import cross.reputation.model.MetricTransformer;
import cross.reputation.model.LinealNumericTransformer;
import cross.reputation.model.Scale;
import cross.reputation.model.NumericScale;

public class ConfigureModel {
	
	static void buildCrossReputationGlobalModel() {
		//Set the dimensions of each scale, the scale of each metric, the metrics
		//of each community, the categories of each community and the communities
		Dimension SystemAdminQandA = new Dimension("SystemAdminQandA");
		GlobalModel.addScale(new NumericScale("stackOverflowScale",30000.0,0.0,1.0));
		GlobalModel.addMetric(new Metric("stackOverflowMetric", SystemAdminQandA,
				GlobalModel.getScales().get("stackOverflowScale")));
		String stackOverflowCategories[] = {GlobalModel.addCategory("QandA")};
		GlobalModel.addCommunity(new Community("stackoverflow.com","stackoverflow.com",
				stackOverflowCategories,GlobalModel.getMetrics().get("stackOverflowMetric")));		
		
		Dimension ProgramingQandA = new Dimension("ProgramingQandA");
		GlobalModel.addScale(new NumericScale("serverFaultScale",20000.0,0.0,1.0));
		GlobalModel.addMetric(new Metric("serverFaultMetric", ProgramingQandA,
				GlobalModel.getScales().get("serverFaultScale")));
		String serverFaultCategories[] = {GlobalModel.addCategory("QandA")};
		GlobalModel.addCommunity(new Community("serverfault.com","serverfault.com",
				serverFaultCategories,GlobalModel.getMetrics().get("serverFaultMetric")));
				
		Dimension WebAppsQandA = new Dimension("WebAppsQandA");
		GlobalModel.addScale(new NumericScale("webAppsStackExchangeScale",20000.0,0.0,1.0));
		GlobalModel.addMetric(new Metric("webAppsStackExchangeMetric", WebAppsQandA,
				GlobalModel.getScales().get("webAppsStackExchangeScale")));
		String webAppsStackExchangeCategories[] = {GlobalModel.addCategory("QandA")};
		GlobalModel.addCommunity(new Community("webapps.stackexchange.com","webapps.stackexchange.com",
				webAppsStackExchangeCategories,GlobalModel.getMetrics().get("webAppsStackExchangeMetric")));
				
		Dimension solveSecurityProblemsQandA = new Dimension("solveSecurityProblemsQandA");
		GlobalModel.addScale(new NumericScale("questionsSecuritytubeScale",20000.0,0.0,1.0));
		GlobalModel.addMetric(new Metric("questionsSecuritytubeMetric", solveSecurityProblemsQandA,
				GlobalModel.getScales().get("questionsSecuritytubeScale")));
		String questionsSecuritytubeCategories[] = {GlobalModel.addCategory("QandA")};
		GlobalModel.addCommunity(new Community("questions.securitytube.net","questions.securitytube.net",
				questionsSecuritytubeCategories,GlobalModel.getMetrics().get("questionsSecuritytubeMetric")));
				
		Dimension ITSecurityQandA = new Dimension("ITSecurityQandA");
		GlobalModel.addScale(new NumericScale("security.StackexchangeScale",2000.0,0.0,1.0));
		GlobalModel.addMetric(new Metric("security.StackexchangeMetric", ITSecurityQandA,
				GlobalModel.getScales().get("security.StackexchangeScale")));
		String securityStackexchangeCategories[] = {GlobalModel.addCategory("QandA")};
		GlobalModel.addCommunity(new Community("security.stackexchange.com","security.stackexchange.com",
				securityStackexchangeCategories,GlobalModel.getMetrics().get("security.StackexchangeMetric")));
		
		Dimension securityWebApps = new Dimension("SecurityWebApps");
		GlobalModel.addScale(new NumericScale("semanticWikiScale",10.0,0.0,1.0));
		GlobalModel.addMetric(new Metric("semanticWikiMetric", securityWebApps,
				GlobalModel.getScales().get("semanticWikiScale")));
		String semanticWikiCategories[] = {GlobalModel.addCategory("SecurityWebApp")};
		Community wiki = new Community("semanticWiki","lab.gsi.dit.upm.es/semanticwiki",
				semanticWikiCategories,GlobalModel.getMetrics().get("semanticWikiMetric"));
		
		Dimension ohloh = new Dimension("Ohloh");
		GlobalModel.addScale(new NumericScale("ohlohScale",10.0,0.0,1.0));
		GlobalModel.addMetric(new Metric("ohlohMetric", ohloh,
				GlobalModel.getScales().get("ohlohScale")));
		String ohlohCategories[] = {GlobalModel.addCategory("QandA")};
		GlobalModel.addCommunity(new Community("ohloh.net","ohloh.net",
				ohlohCategories,GlobalModel.getMetrics().get("ohlohMetric")));
		
		Dimension slackers = new Dimension("Slakers");
		GlobalModel.addScale(new NumericScale("slackersScale",10.0,0.0,1.0));
		GlobalModel.addMetric(new Metric("slackersMetric", slackers,
				GlobalModel.getScales().get("slackersScale")));
		String slackersCategories[] = {GlobalModel.addCategory("QandA")};
		GlobalModel.addCommunity(new Community("sla.ckers.org","sla.ckers.org",
				slackersCategories,GlobalModel.getMetrics().get("slackersMetric")));
		
		
		GlobalModel.addCommunity(wiki);
		
		//Configure all metric transformers that we need
		GlobalModel.addMetricTransformer(new LogaritmicNumericTransformer(GlobalModel.getMetrics().
				get("stackOverflowMetric"),GlobalModel.getMetrics().get("semanticWikiMetric"),1.0));
		GlobalModel.addMetricTransformer(new LogaritmicNumericTransformer(GlobalModel.getMetrics().
				get("serverFaultMetric"),GlobalModel.getMetrics().get("semanticWikiMetric"),1.0));
		GlobalModel.addMetricTransformer(new LogaritmicNumericTransformer(GlobalModel.getMetrics().
				get("webAppsStackExchangeMetric"),GlobalModel.getMetrics().get("semanticWikiMetric"),1.0));
		GlobalModel.addMetricTransformer(new LogaritmicNumericTransformer(GlobalModel.getMetrics().
				get("questionsSecuritytubeMetric"),GlobalModel.getMetrics().get("semanticWikiMetric"),1.0));
		GlobalModel.addMetricTransformer(new LogaritmicNumericTransformer(GlobalModel.getMetrics().
				get("security.StackexchangeMetric"),GlobalModel.getMetrics().get("semanticWikiMetric"),1.0));				
		GlobalModel.addMetricTransformer(new LogaritmicNumericTransformer(GlobalModel.getMetrics().
				get("ohlohMetric"),GlobalModel.getMetrics().get("semanticWikiMetric"),1.0));
		GlobalModel.addMetricTransformer(new LogaritmicNumericTransformer(GlobalModel.getMetrics().
				get("slackersMetric"),GlobalModel.getMetrics().get("semanticWikiMetric"),1.0));				

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
		GlobalModel.addFixedTrustBetweenCommunities("ohloh.net",
				"semanticWiki",1.0);
		GlobalModel.addFixedTrustBetweenCommunities("sla.ckers.org",
				"semanticWiki",1.0);
		
		//Set the category matching table
		GlobalModel.addCategoryMatching("SecurityWebApp","QandA",0.9);
		
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
			for(Metric metric : sourceCommunity.getMetrics()) {
				communityMetricsToImport.addAll(buildCommunityMetricToImportToAllMetrics(
						sourceCommunity, community, metric));
			}
		}
		//printMetricsFromCommunity(metricsFromCommunities);
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
		}
		return metricsToImport;
	}
	
	static public void printMetricsFromCommunity(List<CommunityMetricToImport> metricsFromCommunities) {
		for(CommunityMetricToImport metriCom : metricsFromCommunities) {
			if(metriCom != null) {
				System.out.println("MetriCom:"+metriCom);
				System.out.println("  Com:"+(metriCom.getCommunity() == null?
						null:metriCom.getCommunity().getName()));
				System.out.println("  Met:"+(metriCom.getMetric() == null?
						null:metriCom.getMetric().getIdentificator()));
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
					System.out.println("  Metric:"+metric.getIdentificator());
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
							System.out.println("    Metric:"+metric.getIdentificator());
						else
							System.out.println("    Metric:"+metric);
					}
				} else
					System.out.println("    Metrics:"+null);
			} else
				System.out.println("  Entity:"+entity);
		}
	}
	
	static public Collection<Entity> SetWikiUserEntitiesAndAccounts() {
		Entity administrator = GlobalModel.addEntity(new Entity("Administrator"));
		administrator.addIdentificatorInCommunities(GlobalModel.getCommunities().get("serverfault.com"),
				new EntityIdentifier("Ben Torell",null));
		Entity pblanco = GlobalModel.addEntity(new Entity("PBlanco"));
		pblanco.addIdentificatorInCommunities(GlobalModel.getCommunities().get("serverfault.com"),
				new EntityIdentifier("wayne koorts",null));
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
		Entity racker = GlobalModel.addEntity(new Entity("Racker"));
		racker.addIdentificatorInCommunities(GlobalModel.getCommunities().get("sla.ckers.org"),
				new EntityIdentifier("rsnake",null));
		GetMoreAccounts();
		return GlobalModel.getEntities().values();
	}
	
	static public void GetMoreAccounts() {
		List<String> accounts;
		try {
			for(Entity entity : GlobalModel.getEntities().values()) {
				Map<Community,EntityIdentifier> usuario = entity.getIdentificatorInCommunities();
				//In this form of iteration, we dont search accounts in the new accounts found or
				//  accounts updated that have already been iterated
				for(Object object : usuario.keySet().toArray()) {
					Community community = (Community) object;
					String userName = usuario.get(community).getName();
					//System.out.println(userName+":"+community);
					String url = usuario.get(community).getUrl();
					if(url != null) {
						accounts = Scrapper.UserAccountsByURL(url);
					} else {
						accounts = Scrapper.UserAccounts(userName,community.getDomainName());
					}
					if(accounts != null) {
						SetAccountsInEntity(entity, userName, accounts);
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Error to get more accounts:");
			e.printStackTrace();
		}
	}
	
	static private void SetAccountsInEntity(Entity entity, String userName, List<String> accounts) {
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
