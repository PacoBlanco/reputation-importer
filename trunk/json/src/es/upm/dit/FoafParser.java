package es.upm.dit;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Container;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceF;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.rulesys.RDFSRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.RDFSRuleReasonerFactory;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

import cross.reputation.model.Entity;
import cross.reputation.model.EntityIdentifier;
import cross.reputation.model.GlobalModel;
import es.upm.dit.vulnerapedia.Ent_Eva;
import es.upm.dit.vulnerapedia.ReputationWiki;

public class FoafParser {
	private List<Property> propertyAccount = new ArrayList<Property>();
	private List<Property> propertyAccountName = new ArrayList<Property>();
	private List<Property> propertyAccountProfile = new ArrayList<Property>();
	private List<Resource> agentResource = new ArrayList<Resource>();
	
	private List<Resource> resources = new ArrayList<Resource>();
	
	String riNamespace = "http://purl.org/reputationImport/0.1/";
	
	private List<Property> communitiesProperty = new ArrayList<Property>();
	private List<Property> communitiesReputationModel = new ArrayList<Property>();
	private List<Property> communitiesReputationModelProperties = new ArrayList<Property>();
	private List<Property> collectingSystemProperty = new ArrayList<Property>();
	private List<Property> metricDefinitionProperty = new ArrayList<Property>();
	private List<Property> metricTransformerProperty = new ArrayList<Property>();
	private List<Property> correlationProperty = new ArrayList<Property>();
	private List<Property> trustCommunitiesProperty = new ArrayList<Property>();
	private List<Property> fixedTrustProperty = new ArrayList<Property>();
	private List<Property> categoryMatchingProperty = new ArrayList<Property>();
	private List<Property> dimensionsProperty = new ArrayList<Property>();
	private String userName = "";
	
	static public void main(String[] args) {
		FoafParser foaf = new FoafParser();
		//foaf.foafAgent("http://localhost/foafSample2.rdf");
		//foaf.foafAgent("dir/foafAndrea.rdf");
		foaf.getCrossReputationGlobalModelFromRDF("dir/model2.rdf");
	}
	
	public void foafAgent(String inputFileName) {
		String foafNamespace = "http://xmlns.com/foaf/0.1/";
		// create an empty model
		Model model = ModelFactory.createOntologyModel(); // createDefaultModel();
	
		// use the FileManager to find the input file
		InputStream in = FileManager.get().open( inputFileName );
		if (in == null) {
		    throw new IllegalArgumentException(
		           "File: " + inputFileName + " not found");
		}
	
		// read the RDF/XML file
		model.read(in, "", null);
		
		System.out.println("Base Namespace:"+model.getNsPrefixURI(""));
		if(model.getNsPrefixURI("foaf") != null) {
			foafNamespace = model.getNsPrefixURI("foaf");
			System.out.println("foaf namespace:"+foafNamespace);
		}
		// write it to standard out
		//model.write(System.out);
		
		// create the reasoner factory and the reasoner
		Resource conf = model.createResource();
		//conf.addProperty( ReasonerVocabulary.PROPtraceOn, "true" );
		RDFSRuleReasoner reasoner = (RDFSRuleReasoner) RDFSRuleReasonerFactory.theInstance().create(conf);
		// Create inference model
		InfModel infModel = ModelFactory.createInfModel(reasoner, model);
		
		//model = infModel;
		
		
		propertyAccountName.add(ResourceFactory.createProperty(foafNamespace, "accountName"));
		propertyAccountProfile.add(ResourceFactory.createProperty(foafNamespace, "accountProfilePage"));
		
		propertyAccount.add(ResourceFactory.createProperty(foafNamespace, "account"));
		propertyAccount.add(ResourceFactory.createProperty(foafNamespace, "holdsAccount"));
				
		agentResource.add(ResourceFactory.createResource(foafNamespace + "Agent"));
		agentResource.add(ResourceFactory.createResource(foafNamespace + "Person"));
		//Resource person = ResourceFactory.createResource(foafNamespace + "Person" );
		
		for(Resource agent : agentResource) {				
			ResIterator iters = model.listResourcesWithProperty(RDF.type,agent);
			
			if (iters.hasNext()) {
			    System.out.println("The database contains resource Person for:");
			    while (iters.hasNext()) {
			    	Resource resource = iters.nextResource();
			    	String resourceName = null;
			    	if(resource.getLocalName() != null) {
			    		resourceName = resource.getLocalName();
			    	} else if(resource.getId() != null) {
			    		if(resource.getId().getLabelString() != null) {
			    			resourceName = resource.getId().getLabelString();
			    		} else {
			    			resourceName = resource.getId().toString();
			    		}
			    	} else if(resource.getURI() != null) {
			    		resourceName = resource.getURI();
			    	}
			    	System.out.println("  " + resourceName+" class:"+resource.getClass());
			    	NodeIterator nodes = model.listObjectsOfProperty(resource, RDF.type);
			    	while(nodes.hasNext()) {
			    		RDFNode node = nodes.nextNode();
			    		if(node.isResource()) {
			    			System.out.println("   type " + node.asResource().getURI());
			    		}
			    	}
			    	StmtIterator stmtI = model.listStatements(resource, null, (RDFNode)null);
			    	while(stmtI.hasNext()) {
			    		Statement statement = stmtI.nextStatement();
			    		if (statement.getPredicate().toString().equals("http://xmlns.com/foaf/0.1/name")){
			    			userName = statement.getObject().toString();
			    		}
			    		System.out.println("   triple "+statement.getPredicate()+" - "+statement.getObject());
			    	}
			    	for(Property property : propertyAccount) {
				    	StmtIterator stmtI1 = model.listStatements(resource, property, (RDFNode)null);
						Entity entity = new Entity(userName);
				    	while(stmtI1.hasNext()) {
				    		Statement statement = stmtI1.nextStatement();			    		
				    		System.out.println("   OnlineAccount "+statement.getObject());
				    		String accountURL = "";
				    		String accountUserName = "";
				    		if(statement.getObject().isResource()) {
				    			Resource onlineAccount = statement.getObject().asResource();				    			
				    			NodeIterator nodess = model.listObjectsOfProperty(onlineAccount, RDF.type);
						    	while(nodess.hasNext()) {
						    		RDFNode node = nodess.nextNode();
						    		if(node.isResource()) {
						    			System.out.println("      type " + node.asResource().getURI());
						    		}
						    	}
				    			for(Property property2 : propertyAccountName) {
				    				StmtIterator stmtI2 = model.listStatements(onlineAccount, 
				    						property2, (RDFNode)null);
				    				Statement statement2 = stmtI2.nextStatement();
				    				System.out.println("      AccountName "+statement2.getObject());
				    				accountUserName = statement2.getObject().toString();
				    			}
				    			for(Property property2 : propertyAccountProfile) {
				    				StmtIterator stmtI2 = model.listStatements(onlineAccount, 
				    						property2, (RDFNode)null);
				    				if(stmtI2.hasNext()){
					    				Statement statement2 = stmtI2.nextStatement();
					    				System.out.println("      AccountProfile "+statement2.getObject());
					    				accountURL = statement2.getObject().toString();
				    				}
				    			}
				    			if(accountUserName.equalsIgnoreCase("not_exist")) {
				    				ReputationWiki.notExistantUser.add(userName);
				    				continue;
				    			}
				    			try{
				    				Double value = Double.parseDouble(accountUserName);
				    				ReputationWiki.userPredefined.add(new Ent_Eva(new Entity(userName),value));
				    				continue;
				    				
				    			}catch (Exception e) {}
				    			
				    			String domain = ReputationWiki.findDomain(accountURL);
								if(domain == null) {
									System.out.println("Error: domain is not known from user:"+
									entity.getUniqueIdentificator()+" and it is discarted: "+ accountURL);
									continue;
								}
								if(!accountUserName.equals("")){
									entity.addIdentificatorInCommunities(GlobalModel.getCommunities().get(domain),
										new EntityIdentifier(accountUserName,null));
								}
								else{
									entity.addIdentificatorInCommunities(GlobalModel.getCommunities().get(domain),
										new EntityIdentifier(entity.getUniqueIdentificator(),accountURL));
								}
					    		
								if(!entity.getIdentificatorInCommunities().isEmpty()) {
									GlobalModel.addEntity(entity);
								}
				    		}
				    	}
			    	}
			    }
			}
		}		
		
		ResIterator iters = model.listSubjectsWithProperty(RDF.type,foafNamespace+"Person");
		if (iters.hasNext()) {
		    System.out.println("The database contains literal person for:");
		    while (iters.hasNext()) {
		    	Resource resource = iters.nextResource();
		    	System.out.println("  " + resource.getLocalName());
		       // node.
		        
		    }
		} else {
			System.out.println("No simple String foafNamespace+Person were found in the database");
		}
		
		Property propertyOnlineAccount = ResourceFactory.createProperty(
				foafNamespace, "OnlineAccount");
		iters = model.listSubjectsWithProperty(propertyOnlineAccount);
		if (iters.hasNext()) {
		    System.out.println("The database contains OnlineAccount for:");
		    while (iters.hasNext()) {
		    	Resource resource = iters.nextResource();
		    	System.out.println("  " + resource.getLocalName());
		    }
		} else {
		    System.out.println("No PROPERTY OnlineAccount were found in the database");
		}		
		
		iters = model.listSubjectsWithProperty(RDF.type);
		if (iters.hasNext()) {
		    System.out.println("The database contains RDF.type for:");
		    while (iters.hasNext()) {
		    	Resource resource = iters.nextResource();
		    	NodeIterator nodes = model.listObjectsOfProperty(resource, RDF.type);
		    	String resourceName = null;
		    	if(resource.getLocalName() != null) {
		    		resourceName = resource.getLocalName();
		    	} else if(resource.getId() != null) {
		    		if(resource.getId().getLabelString() != null) {
		    			resourceName = resource.getId().getLabelString();
		    		} else {
		    			resourceName = resource.getId().toString();
		    		}
		    	} else if(resource.getURI() != null) {
		    		resourceName = resource.getURI();
		    	}
		    	System.out.println("  " + resourceName);
		    	while(nodes.hasNext()) {
		    		RDFNode node = nodes.nextNode();
		    		if(node.isLiteral()) {
		    			System.out.println("   l " + node.asLiteral().getString());
		    		} else if(node.isResource()) {
		    			System.out.println("   r " + node.asResource().getURI());
		    		}
		    	}		    	
		    }
		} else {
		    System.out.println("No subject with RDF.type were found in the database");
		}
		
		Resource resourceOnlineAccount = ResourceFactory.createResource(foafNamespace+ "OnlineAccount" );	
		StmtIterator stmtI = model.listStatements(null, RDF.type, resourceOnlineAccount);
		if (stmtI.hasNext()) {
			System.out.println("The database contains RDF.type OnlineAccount for:");
			while(stmtI.hasNext()) {
	    		Statement statement = stmtI.nextStatement();
	    		System.out.println("  "+statement.getSubject()+" - "+statement.getPredicate()+
	    				" - "+statement.getObject());
	    	}
		} else {
			System.out.println("No subject with RDF.type OnlineAccount were found in the database");
		}
		ConfigureModel.GetMoreAccounts();
	}
	
	public void getContainerResources (Resource resource, 
			Model model, String resourceName) {
		try {
	    	//No Container
    		Container resourceBag = (Container) resource;
    		NodeIterator nodeI = resourceBag.iterator();
	    	//OntResource resourceBag = (OntResource) resource;
	    	//NodeIterator nodeI = resourceBag.listPropertyValues(null); 
	    	while(nodeI.hasNext()) {
	    		RDFNode node = nodeI.nextNode();
	    		System.out.println("   node to check:" + node);				    		
	    		if(node.isResource()) {
	    			NodeIterator nodess = model.listObjectsOfProperty((Resource)node,null);
	    			while(nodess.hasNext()) {
			    		RDFNode profile = nodess.nextNode();
			    		System.out.println("     " + profile);				    		
			    	}
	    		}		    		
	    		//System.out.println("   triple "+statement.getPredicate()+" - "+statement.getObject());
	    	}
    	} catch (java.lang.ClassCastException e) {
    		System.out.println("   "+resourceName+" does not have resources inside");
    	}
	}
	
	public void getCrossReputationGlobalModelFromRDF(String inputFileName){        
        // create an empty model
        Model model = ModelFactory.createOntologyModel(); // createDefaultModel();

        // use the FileManager to find the input file
        InputStream in = FileManager.get().open( inputFileName );
        if (in == null) {
            throw new IllegalArgumentException(
                   "File: " + inputFileName + " not found");
        }

        // read the RDF/XML file
        model.read(in, "", null);
        
        // create the reasoner factory and the reasoner
		/*Resource conf = model.createResource();
		//conf.addProperty( ReasonerVocabulary.PROPtraceOn, "true" );
		RDFSRuleReasoner reasoner = (RDFSRuleReasoner) RDFSRuleReasonerFactory.theInstance().create(conf);
		// Create inference model
		InfModel infModel = ModelFactory.createInfModel(reasoner, model);
		
		model = infModel;*/
        
        System.out.println("Base Namespace:"+model.getNsPrefixURI(""));
        
        if(model.getNsPrefixURI("ri") != null) {
        	riNamespace = model.getNsPrefixURI("ri");
            System.out.println("ri namespace:"+riNamespace);
        }
        
        addPropertiesAndResources();
     
        for(Resource res : resources){
        	
            ResIterator iters = model.listSubjectsWithProperty(RDF.type,res);
            if (iters.hasNext()) {
                System.out.println("The database contains subjects of type " + res.getLocalName());
                while (iters.hasNext()) {
                    Resource resource = iters.nextResource();
                    System.out.println("  " + resource.getLocalName());  
                    
        	    	StmtIterator stmtI = model.listStatements(resource, null, (RDFNode)null);
        	    	while(stmtI.hasNext()) {
        	    		Statement statement = stmtI.nextStatement();
        	    		System.out.println("   triple "+statement.getPredicate()+" - "+statement.getObject());
        	    	}
			    	for(Property property : communitiesProperty) {
				    	StmtIterator stmtI1 = model.listStatements(resource, property, (RDFNode)null);
				    	while(stmtI1.hasNext()) {
				    		Statement statement = stmtI1.nextStatement();			    		
				    		System.out.println("   OnlineAccount "+statement.getObject());
				    		if(statement.getObject().isResource()) {
				    			Resource onlineAccount = statement.getObject().asResource();				    			
				    			NodeIterator nodess = model.listObjectsOfProperty(onlineAccount, RDF.type);
						    	while(nodess.hasNext()) {
						    		RDFNode node = nodess.nextNode();
						    		if(node.isResource()) {
						    			System.out.println("      type " + node.asResource().getURI());
						    		}
						    	}
				    			for(Property property2 : communitiesReputationModel) {
				    				StmtIterator stmtI2 = model.listStatements(onlineAccount, 
				    						property2, (RDFNode)null);
				    				Statement statement2 = stmtI2.nextStatement();
				    				System.out.println("      Properties "+statement2.getObject());
				    			}
				    		}
				    	}
			    	}
                }
            } else {
                System.out.println("No simple String " + riNamespace + res.getLocalName() +  " were found in the database");
            }  
            
            /*if (res.getLocalName().toString().equals("Community")){
            	
            }*/
            
        }
                     
        
	}
	
	private void addPropertiesAndResources(){
		
		resources.add(ResourceFactory.createResource(riNamespace + "Community"));
        resources.add(ResourceFactory.createResource(riNamespace + "CollectingSystem"));
        resources.add(ResourceFactory.createResource(riNamespace + "Metric"));
        resources.add(ResourceFactory.createResource(riNamespace + "SqrtNumericTransformer"));
        resources.add(ResourceFactory.createResource(riNamespace + "ExponentialNumericTransformer"));
        resources.add(ResourceFactory.createResource(riNamespace + "LogaritmicNumericTransformer"));
        resources.add(ResourceFactory.createResource(riNamespace + "LinealNumericTransformer"));
        resources.add(ResourceFactory.createResource(riNamespace + "CorrelationBetweenDimension"));
        resources.add(ResourceFactory.createResource(riNamespace + "TrustBetweenCommunities"));
        resources.add(ResourceFactory.createResource(riNamespace + "FixedCommunitiesTrust"));
        resources.add(ResourceFactory.createResource(riNamespace + "CategoryMatching"));
        resources.add(ResourceFactory.createResource(riNamespace + "Dimension"));
        
        communitiesProperty.add(ResourceFactory.createProperty(riNamespace, "hasReputationModel"));
        communitiesReputationModel.add(ResourceFactory.createProperty(riNamespace, "ReputationModel"));
        communitiesReputationModelProperties.add(ResourceFactory.createProperty(riNamespace, "name"));
        communitiesReputationModelProperties.add(ResourceFactory.createProperty(riNamespace, "description"));
        communitiesReputationModelProperties.add(ResourceFactory.createProperty(riNamespace, "resultCollectionType"));
        communitiesReputationModelProperties.add(ResourceFactory.createProperty(riNamespace, "Accesibility"));

	}
	
}
