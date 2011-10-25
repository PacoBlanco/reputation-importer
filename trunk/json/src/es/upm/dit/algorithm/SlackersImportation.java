package es.upm.dit.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cross.reputation.model.CollectingSystemBehaviour;
import cross.reputation.model.Entity;
import cross.reputation.model.GlobalModel;
import cross.reputation.model.ImportationUnit;
import cross.reputation.model.Metric;
import cross.reputation.model.ModelException;
import cross.reputation.model.ReputationImporterBehaviour;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import es.upm.dit.ConstructModel;
import es.upm.dit.Ejecutor;
import es.upm.dit.Property;
import es.upm.dit.Scrapper;
import es.upm.dit.ScrappyExecutor;

public class SlackersImportation {
	private String urlPosts;
	private List<String> userThreads;
	private Double opalScore = 0.0;
	private String userName;
	
	public Map<Metric,List<Object>> scrappyImportation(ReputationImporterBehaviour repImp,
			ImportationUnit importationUnit, CollectingSystemBehaviour system,
			Entity entity) throws Exception {
		if(!system.getRoot().getName().contains("Scrappy")) {
			ModelException.throwException(ModelException.SYSTEM_NAME_NOT_EXPECTED, 
					"SystemName not expected:"+system.getRoot().getName()+" (expected: Scrappy)");
		}
		String url = ConstructModel.getEntityURL(system.getUriFormat(),
				importationUnit.getImportedCommunity(),entity.getIdentificatorInCommunities(
				).get(importationUnit.getImportedCommunity()));
		if(url == null) {
			return null;
		}
		ScrappyExecutor scrappy = new ScrappyExecutor();
		String scrappy_dump = scrappy.execute(url); //"1"
		try {
			JSONArray array = (JSONArray) JSONSerializer.toJSON(scrappy_dump);
			for(int j=0; j < array.size(); j++){
            	JSONObject dumpObject = array.getJSONObject(j);
            	if(system.getUriFormat().toString().contains("/$User_Profile_History_Posts")) {
	            	if(dumpObject.has("http://purl.org/dc/elements/1.1/Usuario")){
	    				JSONArray array_usuarios = dumpObject.getJSONArray(
	    						"http://purl.org/dc/elements/1.1/Usuario");
	    				JSONObject objeto_usuarios = array_usuarios.getJSONObject(0);
	    	        	//System.out.println("Informacion de usuario:");
	    		        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/Nombre")){
	    		        	JSONArray array_user = objeto_usuarios.getJSONArray(
	    		        			"http://purl.org/dc/elements/1.1/Nombre");
	    			        userName = array_user.getString(0);
	    			        ModelException.sendMessage(ModelException.INFO,"  Nombre: "+userName);
	    		        }
	    		        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/Posts")){
	    			        JSONArray array_user = objeto_usuarios.getJSONArray(
	    			        		"http://purl.org/dc/elements/1.1/Posts");
	    			        String posts = array_user.getString(0);
	    			        ModelException.sendMessage(ModelException.INFO, "  Posts: " + posts);
	    		        }
	    		        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/URLPosts")){
	    			        JSONArray array_user = objeto_usuarios.getJSONArray(
	    			        		"http://purl.org/dc/elements/1.1/URLPosts");
	    			        urlPosts = array_user.getString(0);
	    			        ModelException.sendMessage(ModelException.INFO,"  URLPosts: "+urlPosts);		        
	    		        }
	    			}
            	} else {
            		ModelException.throwException(ModelException.NOT_URI_KNOWN,
            				"Not URI procedement known:" + system.getUriFormat());
            	}
			}			
		} catch(net.sf.json.JSONException e) {
			e.printStackTrace();
			ModelException.throwException(ModelException.SCRAPPY_ERROR,
					"Invalid JSON String from url "+url+":\n"+scrappy_dump);
		}
		if(userName == null) {
			ModelException.throwException(ModelException.SCRAPPY_ERROR,
					"Slackers scrappy template is out of date or incorrect. " +
					"There is not http://purl.org/dc/elements/1.1/Nombre found");
		}
		if(urlPosts == null) {
			ModelException.throwException(ModelException.SCRAPPY_ERROR,
					"Slackers scrappy template is out of date or incorrect. " +
					"There is not http://purl.org/dc/elements/1.1/URLPosts found");
		}
		return null;
	}
	
	public Map<Metric,List<Object>> scrappyPostsAfterUserImportation(
			ReputationImporterBehaviour repImp, ImportationUnit importationUnit,
			CollectingSystemBehaviour system, Entity entity) throws Exception{
		if(!system.getRoot().getName().contains("Scrappy")) {
			throw new Exception("SystemName not expected:"+system.getRoot().getName()+
					" (expected: Scrappy)");
		}
		if(urlPosts == null) {
			ModelException.throwException(ModelException.SCRAPPY_ERROR,"scrappyImportation " +
					"method must be called before this method or it had an incorrect execution");
		}
		String scrappy_dump = Ejecutor.executeScrappy(urlPosts, "0");
		JSONArray array = (JSONArray) JSONSerializer.toJSON(scrappy_dump); 
        JSONObject objeto_dump = array.getJSONObject(0);
        int totalPages = 1;
        if(objeto_dump.has("http://purl.org/dc/elements/1.1/TotalPages")){
        	JSONArray number = objeto_dump.getJSONArray("http://purl.org/dc/elements/1.1/TotalPages");
        	totalPages = Integer.parseInt(number.getString(0));
        } else {
        	ModelException.sendMessage(ModelException.INFO,
        			"Slackers scrappy template could be out of date or incorrect. " +
					"There is not http://purl.org/dc/elements/1.1/TotalPages found. Default to 1");
        }
        int postsCount = 0;
        ModelException.sendMessage(ModelException.INFO,"  Maximum posts: " + Property.getPOSTS_NUMBER());        
        for (int j = 1; j <= totalPages; j++){        	
			if (postsCount >= Property.getPOSTS_NUMBER()) {
				break;
			}		            	
        	if (j > 1){
        		String newUrlPosts = urlPosts + ",page=" + j;
        		scrappy_dump = Ejecutor.executeScrappy(newUrlPosts, "0");
        		array = (JSONArray) JSONSerializer.toJSON(scrappy_dump);
        		objeto_dump = array.getJSONObject(0);
        	}
        	if (objeto_dump.has("http://purl.org/dc/elements/1.1/Posts")){
        		JSONArray array_objeto_post = objeto_dump.getJSONArray("http://purl.org/dc/elements/1.1/Posts");
        		ModelException.sendMessage(ModelException.INFO,
        				"  Calculate reputation over "+array_objeto_post.size()+" posts");
        		for(int i=0;i<array_objeto_post.size();i++){	
        			if (postsCount == Property.getPOSTS_NUMBER())
        				break;
	            	JSONObject objeto_array_post = array_objeto_post.getJSONObject(i);
		        	//System.out.println("Informacion de post:");
		        	if(objeto_array_post.has("http://purl.org/dc/elements/1.1/PostURL")){
		        		if(system.getUriFormat().toString().contains("/$User_Thread")) {
		        			JSONArray array_postURL = objeto_array_post.getJSONArray(
	        						"http://purl.org/dc/elements/1.1/PostURL");
		        			String postURL = array_postURL.getString(0);
		        			ModelException.sendMessage(ModelException.INFO,"    PostURL: " + postURL);
		        			if(userThreads == null) {
		        				userThreads = new ArrayList<String>();
		        			}
		        			userThreads.add(Ejecutor.executeScrappy(postURL, "0"));
		        			postsCount++;
		        		}  else {
		        			ModelException.throwException(ModelException.NOT_URI_KNOWN,
		        					"Not URI procedement known:" + system.getUriFormat());
		            	}
		        	}				        	
			        if(objeto_array_post.has("http://purl.org/dc/elements/1.1/PostName")){
			        	//JSONArray array_postName = objeto_array_post.getJSONArray("http://purl.org/dc/elements/1.1/PostName");
			        	//String postName = array_postName.getString(0);
				        //System.out.println("  PostName: " + postName);
			        }
			        if(objeto_array_post.has("http://purl.org/dc/elements/1.1/PostFecha")){
			        	//JSONArray array_fecha = objeto_array_post.getJSONArray("http://purl.org/dc/elements/1.1/PostFecha");
			        	//String postsFecha = array_fecha.getString(0);
				        //System.out.println("  Fecha: " + postsFecha);
			        }
	            }
            }
        }
        if(userThreads == null) {
        	ModelException.sendMessage(ModelException.INFO,
        			"Slackers scrappy template could be out of date or incorrect. " +
					"There is not http://purl.org/dc/elements/1.1/PostURL found.");
		}
        return null;
	}
	
	public Map<Metric,List<Object>> opalImportation(ReputationImporterBehaviour repImp,
			ImportationUnit importationUnit,CollectingSystemBehaviour system, 
			Entity entity) throws Exception {
		Map<Metric,List<Object>> reputations = null;
		//OpalExecutorService opalExec = new OpalExecutorService(userName);
		if(system.getUriFormat().toString().contains("/$User_Thread/")) {
			if(userThreads == null) {
			}
			for(int i = 0; i < userThreads.size(); i++) {
				String thread = userThreads.get(i);
				if(system.getUriFormat().toString().contains("/$Posts_After_User_Post")) {
					double opalFromPost = Scrapper.informacionPostsSlackers(
							userName,thread);					
					synchronized (this) {
						opalScore += opalFromPost;						
					}
				} else {
					throw new Exception("Not URI procedement known:" + system.getUriFormat());
				}
			}
		} else {
			throw new Exception("Not URI procedement known:" + system.getUriFormat());
		}
		//Double opalSum = opalExec.shutdown();
        if(opalScore != null) {
	        reputations = new HashMap<Metric,List<Object>>();
	        List<Object> value = new ArrayList<Object>();
	        value.add(opalScore);
        	reputations.put(GlobalModel.getMetrics().get("slackersImportedMetric"),value);
        	ModelException.sendMessage(ModelException.INFO,
        			"  Slackers reputation by OPAL: " + opalScore);
        }
        return reputations;
	}
}
