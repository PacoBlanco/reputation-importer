package es.upm.dit.vulnerapedia;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import cross.reputation.model.Community;
import cross.reputation.model.CommunityMetricToImport;
import cross.reputation.model.CrossReputation;
import cross.reputation.model.Entity;
import cross.reputation.model.EntityIdentifier;
import cross.reputation.model.Evaluation;
import cross.reputation.model.GlobalModel;
import cross.reputation.model.Metric;
import es.upm.dit.ConfigureModel;
import es.upm.dit.Ejecutor;
import es.upm.dit.FoafParser;
import es.upm.dit.Property;
import es.upm.dit.Scrapper;

public class ReputationWiki {
	static public String directory = "./";	
	static public List<String> notExistantUser = new ArrayList<String>();
	static public List<String> notAccountUser = new ArrayList<String>();
	static public List<Ent_Eva> userPredefined = new ArrayList<Ent_Eva>();
	static public String tableFile = "reputation-";
	
	static public void main(String[] args) throws Exception {
		if(args.length > 0) {
			directory = args[0];
			System.out.println("Directory:"+directory);
		} 
		String lastFileName = takesLastFile();		
		if(lastFileName == null) {
			System.out.println("There is not users file");
			return;
		} else {
			System.out.println("The file to process is:"+lastFileName);
		}
		//Config properties
		Property.setValues();
		
		//Set All Model
		ConfigureModel.buildCrossReputationGlobalModel();
		
		//Set the destination community to import the other community reputations
		Community destinationCommunity = GlobalModel.getCommunities().get("semanticWiki");
		
		//Set user accounts from file generated by the mediawiki python bot
		/*try {
			setUserAccountsFromJSON(lastFileName);
			//setUserAccountsFromFile(lastFileName);
		} catch (IOException e) {
			System.out.println("Error: ioexception at reading file:"+lastFileName
					+"\n "+e.getMessage());
			return;
		} catch (JSONException e) {
        	System.out.println("The file could have a not JSON format.");
        	e.printStackTrace();
        	return;
		}*/
		
		//Set user accounts from Foaf RDF file
		FoafParser foaf = new FoafParser();
		//foaf.foafAgent("dir/foafSample.rdf");
		foaf.foafAgent("http://localhost/foafSample2.rdf");
		
		//Add all entities configured to all metrics in the destination community
		for(Entity entity : GlobalModel.getEntities().values()) {
			destinationCommunity.addEntityToAllMetrics(entity);
		}		
		
		//Set that all rest communities and theirs metrics are valid to the importation
		List<CommunityMetricToImport> metricsToImport = ConfigureModel.
			buildMetricsFromAllCommunitiesToAllMetrics(destinationCommunity);
		
		extractReputation();
		
		//Calculate Cross-Reputation to import the reputation information extracted
		//System.out.println("\n*********************** Calculate Reputations***********************");
		CrossReputation crossReputation = new CrossReputation(destinationCommunity,
				metricsToImport, true, true);
		crossReputation.addAllEvaluations();
		crossReputation.calculateAllReputations();
		//System.out.println("\n************************ Print Evaluations *************************");
		//GlobalModel.printEvaluations();
		
		String table = formTable(destinationCommunity);
		//System.out.println("Table:\n"+table);		
		tableToFile(table);		
	}
	
	static public void tableToFile(String table) {
		Date now = new Date();
		File userFile = new File(directory + tableFile+now.getTime());		
		try {
			//System.out.println("File:"+directory + tableFile+now.getTime());
			FileWriter fileReader;		
			fileReader = new FileWriter(userFile);
			BufferedWriter writer = new BufferedWriter(fileReader);
			table = "'''''Last Update: '''''"+now+"\n\n"+table;
			writer.write(table);
			writer.close();
		} catch (IOException e) {
			System.out.println("Error: IOException putting content in file: "+
				userFile.getAbsolutePath()+"\n "+e.getMessage());
		}		
	}
	
	static public void setUserAccountsFromJSON(String lastFileName) throws IOException {
		File userFile = new File(directory + lastFileName);
		System.out.println("File to process:"+userFile.getAbsolutePath());
		FileReader fileReader = new FileReader(userFile);
		BufferedReader reader = new BufferedReader(fileReader);		
		String temp = "";
        int current = 0;
        while((current = reader.read()) != -1){
            temp += (char)current;
        }
        reader.close();
        
        String json = new String(temp);
        JSONArray array = (JSONArray) JSONSerializer.toJSON(json);
        int size = array.size();
        
        for(int i = 0; i < size; i++){
        	JSONObject object = array.getJSONObject(i);
        	String name = object.names().getString(0);
        	JSONArray array_object = object.getJSONArray(name);
        	int arraySize = array_object.size();
        	Entity entity = new Entity(name);
        	
    		for(int j = 0; j < arraySize; j++){
            	String user = array_object.getJSONObject(j).names().getString(0);

    			if(user.equalsIgnoreCase("not_exist")) {
    				notExistantUser.add(name);
    				continue;
    			}
    			try{
    				Double value = Double.parseDouble(user);
    				userPredefined.add(new Ent_Eva(new Entity(name),value));
    				continue;
    				
    			}catch (Exception e) {}
    			
    			JSONArray userArray = array_object.getJSONObject(j).getJSONArray(user);
    			String domain = findDomain(userArray.getString(0));
				if(domain == null) {
					System.out.println("Error: domain is not known from user "+
					entity.getUniqueIdentificator()+" and it is discarted:"+ userArray.getString(0));
					continue;
				}
				if(!name.equals("")){
					entity.addIdentificatorInCommunities(GlobalModel.getCommunities().get(domain),
						new EntityIdentifier(user,null));
				}
				else{
					entity.addIdentificatorInCommunities(GlobalModel.getCommunities().get(domain),
						new EntityIdentifier(entity.getUniqueIdentificator(),userArray.getString(0)));
				}
	    		
				if(!entity.getIdentificatorInCommunities().isEmpty()) {
					GlobalModel.addEntity(entity);
				}
    			
    		}

        }
		ConfigureModel.GetMoreAccounts();
			
	}
	
	static public void setUserAccountsFromFile(String lastFileName) throws IOException {
		File userFile = new File(directory + lastFileName);
		System.out.println("File to process:"+userFile.getAbsolutePath());
		FileReader fileReader = new FileReader(userFile);
		BufferedReader reader = new BufferedReader(fileReader);		
		while (true) {
			String line = reader.readLine();
			if(line == null) {
				break;
			}
			String contents[] = line.split(":");
			if(contents.length < 2) {
				notAccountUser.add(contents[0]);
				continue;
			}
			if(contents[1].equalsIgnoreCase("not_exist")) {
				notExistantUser.add(contents[0]);
				continue;
			}
			if(contents.length > 2) {
				contents[1] = line.substring(line.indexOf(":")+1);
				//System.out.println("line:"+contents[1]);
			}
			try {
				Double value = Double.parseDouble(contents[1]);
				userPredefined.add(new Ent_Eva(new Entity(contents[0]),value));
				continue;
			} catch (Exception e) {}
			Entity entity = new Entity(contents[0]);
			contents = contents[1].split(",");
			for(int i = 0; i < contents.length; i++) {
				int coincidence = contents[i].indexOf("^#^");
				if(coincidence == -1) {
					String domain = findDomain(contents[i]);
					if(domain == null) {
						System.out.println("Error: domain is not known from user:"+
						entity.getUniqueIdentificator()+" and it is discarted: "+contents[i]);
						continue;
					}
					entity.addIdentificatorInCommunities(GlobalModel.getCommunities().get(domain),
							new EntityIdentifier(entity.getUniqueIdentificator(),contents[i]));
				} else {
					String userName = contents[i].substring(0,coincidence);
					//System.out.println("iu:"+entity.getUniqueIdentificator()+",u:"+userName);
					String domain = findDomain(contents[i].substring(coincidence+3));
					//System.out.println("d:"+domain);					
					if(domain == null) {
						System.out.println("Error: domain is not known from user:"+
								entity.getUniqueIdentificator()+"-nickname:"+userName+
								" and it is discarted: "+contents[i].substring(coincidence+3));
						continue;
					}
					entity.addIdentificatorInCommunities(GlobalModel.getCommunities().get(domain),
							new EntityIdentifier(userName,null));
				}					
			}
			if(!entity.getIdentificatorInCommunities().isEmpty()) {
				GlobalModel.addEntity(entity);
			}
		}
		reader.close();
		ConfigureModel.GetMoreAccounts();
	}
	
	static public String formTable(Community destinationCommunity) {
		String table = "{| border=\"2\" size=70% cellspadding=\"10\"\n";
		table += WikiFormat.AddNewRow();
		table += WikiFormat.AddHeaderTable("'''User'''");
		table += WikiFormat.AddHeaderTable("'''Description'''");
		table += WikiFormat.AddHeaderTable("'''Reputation'''");
		List<Ent_Eva> entitiesSorted = new ArrayList<Ent_Eva>();
		for(Entity entity : GlobalModel.getEntities().values()) {
			Ent_Eva entityValue = new Ent_Eva(GlobalModel.getEvaluations(),
					entity, destinationCommunity);
			boolean isIn = false;
			if(entityValue.getValue() == null) {
				entitiesSorted.add(entityValue);
				continue;
			}
			for(int i = 0; i < entitiesSorted.size(); i++) {
				Ent_Eva entitySorted = entitiesSorted.get(i);
				if(entitySorted.getValue() == null ||
						entitySorted.getValue() < entityValue.getValue()) {
					entitiesSorted.add(i,entityValue);
					isIn = true;
					break;
				}
			}
			if(!isIn) {
				entitiesSorted.add(entityValue);
			}
		}
		for(Ent_Eva entityValue : userPredefined) {
			boolean isIn = false;
			for(int i = 0; i < entitiesSorted.size(); i++) {
				Ent_Eva entitySorted = entitiesSorted.get(i);
				if(entitySorted.getValue() == null ||entitySorted.getValue() < entityValue.getValue()) {
					entitiesSorted.add(i,entityValue);
					isIn = true;
					break;
				}
			}
			if(!isIn) {
				entitiesSorted.add(entityValue);
			} 
		}
		for(int i = 0; i < entitiesSorted.size(); i++) {
			Ent_Eva entValue = entitiesSorted.get(i);
			String name = entValue.getEntity().getUniqueIdentificator();
			Double value = entValue.getValue();
			table += WikiFormat.AddNewRow();
			table += WikiFormat.AddColumnTable("align=\"center\" | [[User:"+
					name+"]]");
			if(entValue.getType() == Ent_Eva.USER_PREDEFINED) {
				table += WikiFormat.AddColumnTable("align=\"center\" | Represents a community"
						+" whose information has been extracted. It reputation value is predefined");
				table += WikiFormat.AddColumnTable("align=\"center\" | [[Image:sm_laurel_"+
					value.intValue()+".png|frameless|alt="+value.intValue()+"]][["+
					name+"::Web Reputation:="+value.intValue()+"| ]]");
				continue;
			}				
			Set<Community> communities = new HashSet<Community>();
			for(Evaluation evaluation : GlobalModel.getEvaluations()) {
				//System.out.println("comun in eval:"+evaluation.getCommunity()+"=?="+
				//		destinationCommunity+", name in eval:"+evaluation.getCommunity().getName()+
				//		"=?="+destinationCommunity.getName()+": in destina");
				if(evaluation.getDestinationEntity() != entValue.getEntity() ||
						evaluation.getCommunity() == destinationCommunity) {
					continue;
				}
				//Without taking into account the metric names!!
				communities.add(evaluation.getCommunity());
			}
			String accounts = "";
			for(Community community : communities) {
				if(!accounts.isEmpty()) {
					accounts += ", ";
				}
				accounts += community.getName();
			}
			if(value != null) {				
				table += WikiFormat.AddColumnTable("align=\"center\" | "+accounts);
				table += WikiFormat.AddColumnTable("align=\"center\" | [[Image:sm_laurel_"+
					value.intValue()+".png|frameless|alt="+value.intValue()+"]][["+
					name+"::Web Reputation:="+value.intValue()+"| ]]");
			} else {
				if(communities.isEmpty()) {
					table += WikiFormat.AddColumnTable("align=\"center\" | "+
						"User profile with all incorrect url user profile or" +
						" usernames in accounts");
				} else {
					table += WikiFormat.AddColumnTable("align=\"center\" | "+
						"User profile with all incomprehensible reputation " +
						"information in accounts:"+accounts);
				}
				table += WikiFormat.AddColumnTable("align=\"center\" | [[Image:sm_laurel_0.png" +
						"|frameless|alt=0]][["+name+"::Web Reputation:=0| ]]");
			}				
		}
		for(int i = 0; i < notExistantUser.size(); i++) {
			String name = notExistantUser.get(i);
			table += WikiFormat.AddNewRow();			
			table += WikiFormat.AddColumnTable("align=\"center\" | [[User:"+
					name+"]]");
			table += WikiFormat.AddColumnTable("align=\"center\" | "+
					"User Profile not existant");
			table += WikiFormat.AddColumnTable("align=\"center\" | [[Image:sm_laurel_0.png" +
					"|frameless|alt=0]][["+name+"::Web Reputation:=0| ]]");
		}
		for(int i = 0; i < notAccountUser.size(); i++) {
			String name = notAccountUser.get(i);			
			table += WikiFormat.AddNewRow();			
			table += WikiFormat.AddColumnTable("align=\"center\" | [[User:"+
					name+"]]");
			table += WikiFormat.AddColumnTable("align=\"center\" | "+
					"Without userNames or user profile urls with known accounts");
			table += WikiFormat.AddColumnTable("align=\"center\" | [[Image:sm_laurel_0.png" +
					"|frameless|alt=0]][["+name+"::Web Reputation:=0| ]]");
		}		
		table += WikiFormat.AddClosingTable();
		return table;
	}
	
	static public String takesLastFile() {
		File dir = new File(directory);
		//System.out.println("Directoty:"+dir.getAbsolutePath());
		if(!dir.isDirectory()) {
			System.out.println("Directory path is not a directoty");
			return null;
		}
		FilenameFilter filter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		    	return name.startsWith("users-");
		    }
		};
		String[] children = dir.list(filter);
		String lastFileName = null;
		Double lastNumber = 0.0;
		for(String fileName : children) {
			try {
				Double number = Double.parseDouble(fileName.substring(6));
				if(number > lastNumber) {
					lastNumber = number;
					lastFileName = fileName;
				}
			} catch(Exception e) {}
		}		
		return lastFileName;
	}
	
	static public void extractReputation() {
		for(Entity entity : GlobalModel.getEntities().values()) {
			//TODO: solo entidades que esten en metricsFromCommunity!!!
			Map<Community,EntityIdentifier> communityEntity = entity.getIdentificatorInCommunities();
			for(Community community : communityEntity.keySet()) {
				String urlDomain = communityEntity.get(community).getUrl();
				if(urlDomain == null) {
					System.out.println("Info:"+entity.getUniqueIdentificator()+
							":"+communityEntity.get(community).getName()+
							" has null url in:"+community.getName());
					continue;
				}
				try {
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
								System.out.println("ERROR: metric parsed("+(metric==null?null:metric.getIdentificator())
									+") does not correspond to any metric of the community("+community.getName()+
									"):"+community.getMetrics()+". Its score is ignored");
								continue;
							}	
							GlobalModel.addEvaluation(new Evaluation(community,
									entity,sourceMetric,reputationMap.get(metric)));
							System.out.println("Ent:"+entity.getUniqueIdentificator()+" Com: "
							+community.getName()+ " url:"+communityEntity.get(community).getUrl()
							+" met:"+sourceMetric.getIdentificator()+" rep:"+reputationMap.get(metric));
							
						}							
					}
				} catch (IOException e) {
					System.out.println("INFO: extract reputation on user:" +
						entity.getUniqueIdentificator()+" over("+community.getName()+","+
						communityEntity.get(community).getName()+") and url:"+
						communityEntity.get(community).getUrl()+" gives a exception." +
						" His evaluation on this community is discarted.");
				}
				//TODO:Method to extract reputation that associated the metric with the value				
			}
		}
	}
	
	static public String findDomain(String url) {
		url = url.toLowerCase();
		if(url.contains("serverfault")) {
			return "serverfault.com";
		} else if(url.contains("questions.securitytube")) {
			return "questions.securitytube.net";
		} else if(url.contains("security.stackexchange")) {
			return "security.stackexchange.com";
		} else if(url.contains("stackoverflow")) {
			return "stackoverflow.com";
		} else if(url.contains("ohloh")) {
			return "ohloh.net";
		} else if(url.contains("webapps.stackexchange")) {
			return "webapps.stackexchange.com";
		} else if(url.contains("sla.ckers")) {
			return "sla.ckers.org";
		} else if(url.contains("elhacker")) {
			return "foro.elhacker.net";
		}
		return null;
	}
}
