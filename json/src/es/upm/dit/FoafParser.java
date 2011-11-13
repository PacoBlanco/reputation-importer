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
	private List<Property> collectingSystemProperty = new ArrayList<Property>();
	private List<Property> metricDefinitionProperty = new ArrayList<Property>();
	private List<Property> metricTransformerProperty = new ArrayList<Property>();
	private List<Property> correlationProperty = new ArrayList<Property>();
	private List<Property> trustCommunitiesProperty = new ArrayList<Property>();
	private List<Property> fixedTrustProperty = new ArrayList<Property>();
	private List<Property> categoryMatchingProperty = new ArrayList<Property>();
	private List<Property> dimensionsProperty = new ArrayList<Property>();
	private String userName = "";
	
	public FoafParser() {		
	}
	
	public FoafParser(String foafNamespace) {
		propertyAccountName.add(ResourceFactory.createProperty(foafNamespace, "accountName"));
		propertyAccountProfile.add(ResourceFactory.createProperty(foafNamespace, "accountProfilePage"));
		
		propertyAccount.add(ResourceFactory.createProperty(foafNamespace, "account"));
		propertyAccount.add(ResourceFactory.createProperty(foafNamespace, "holdsAccount"));
				
		agentResource.add(ResourceFactory.createResource(foafNamespace + "Agent"));
		agentResource.add(ResourceFactory.createResource(foafNamespace + "Person"));
	}
	
	static public void main(String[] args) {
		FoafParser foaf = new FoafParser();
		//foaf.foafAgent("http://localhost/foafSample2.rdf");
		foaf.getCrossReputationGlobalModelFromRDF("dir/model0.rdf");
	}
	
	public void foafAgent(String inputFileName) throws Exception {
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
		Resource conf = model.createResource();
		//conf.addProperty( ReasonerVocabulary.PROPtraceOn, "true" );
		RDFSRuleReasoner reasoner = (RDFSRuleReasoner) RDFSRuleReasonerFactory.theInstance().create(conf);
		// Create inference model
		InfModel infModel = ModelFactory.createInfModel(reasoner, model);
		
		model = infModel;
        
        System.out.println("Base Namespace:"+model.getNsPrefixURI(""));
        
        if(model.getNsPrefixURI("ri") != null) {
        	riNamespace = model.getNsPrefixURI("ri");
            System.out.println("ri namespace:"+riNamespace);
        }
        Resource dimension = ResourceFactory.createResource(riNamespace + "Dimension");
        Resource categoryMatching = ResourceFactory.createResource(riNamespace + "CategoryMatching");
        Resource fixedCommunitiesTrust = ResourceFactory.createResource(
        		riNamespace + "FixedCommunitiesTrust");
        Resource trustBetweenCommunities = ResourceFactory.
        		createResource(riNamespace + "TrustBetweenCommunities");
        Resource correlationBetweenDimension = ResourceFactory.
        		createResource(riNamespace + "CorrelationBetweenDimension");
        Resource logaritmicNumericTransformer = ResourceFactory.
        		createResource(riNamespace + "LogaritmicNumericTransformer");
        Resource linealNumericTransformer = ResourceFactory.
				createResource(riNamespace + "LinealNumericTransformer");
        Resource sqrtNumericTransformer = ResourceFactory.
				createResource(riNamespace + "SqrtNumericTransformer");
        Resource metric = ResourceFactory.createResource(
        		riNamespace + "Metric");
        Resource collectingSystem = ResourceFactory.createResource(
        		riNamespace + "CollectingSystem");
        Resource community = ResourceFactory.createResource(
        		riNamespace + "Community");
        
        Property identifier = ResourceFactory.createProperty(
        		riNamespace + "identifier");
        Property hasDimension = ResourceFactory.createProperty(
        		riNamespace + "hasDimension");
        Property hasScale = ResourceFactory.createProperty(
        		riNamespace + "hasScale");
        Property name = ResourceFactory.createProperty(
        		riNamespace + "name");
        Property uriFormat = ResourceFactory.createProperty(
        		riNamespace + "uriFormat");
        Property homePage = ResourceFactory.createProperty(
        		riNamespace + "homePage");
        Property hasCategory = ResourceFactory.createProperty(
        		riNamespace + "hasCategory");
        Property hasReputationModel = ResourceFactory.createProperty(
        		riNamespace + "hasReputationModel");
        Property reputationModule = ResourceFactory.createProperty(
        		riNamespace + "reputationModule");
        Property mapsMetric = ResourceFactory.createProperty(
        		riNamespace + "mapsMetric");
        Property importedCommunity = ResourceFactory.createProperty(
        		riNamespace + "importedCommunity");
        Property importedMetric = ResourceFactory.createProperty(
        		riNamespace + "importedMetric");
        Property collectsReputationBy = ResourceFactory.createProperty(
        		riNamespace + "collectsReputationBy");
        Property metricTransformation = ResourceFactory.createProperty(
        		riNamespace + "metricTransformation");
        Property trust = ResourceFactory.createProperty(
        		riNamespace + "trust");
        Property importsFrom = ResourceFactory.createProperty(
        		riNamespace + "importsFrom");
        
        addPropertiesAndResources();
        
        for(Resource agent : resources) {                           
            ResIterator iters = model.listResourcesWithProperty(RDF.type,agent);
            if (iters.hasNext()) {
                System.out.println("The database contains resource for:");
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
                            System.out.println("   triple "+statement.getPredicate()+" - "+statement.getObject());
                    }
                    for(Property property : propertyAccount) {
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
                                for(Property property2 : propertyAccountName) {
                                    StmtIterator stmtI2 = model.listStatements(onlineAccount, property2, (RDFNode)null);
                                    Statement statement2 = stmtI2.nextStatement();
                                    System.out.println("      AccountName "+statement2.getObject());
                                }
                                for(Property property2 : propertyAccountProfile) {
                                    StmtIterator stmtI2 = model.listStatements(onlineAccount, property2, (RDFNode)null);
                                    Statement statement2 = stmtI2.nextStatement();
                                    System.out.println("      AccountProfile "+statement2.getObject());
                                }
                            }
                        }
                    }
                }
            }
         }  
        
        ResIterator iters = model.listSubjectsWithProperty(RDF.type,dimension);
        if (iters.hasNext()) {
            System.out.println("The database contains subjects of type dimension:");
            while (iters.hasNext()) {
                Resource resource = iters.nextResource();
                System.out.println("  " + resource.getLocalName());
            }
        } else {
            System.out.println("No simple String riNamespace+Dimension were found in the database");
        }
        
        iters = model.listResourcesWithProperty(RDF.type,categoryMatching);
        if (iters.hasNext()) {
            System.out.println("The database contains resources of type CategoryMatching:");
            while (iters.hasNext()) {
                Resource resource = iters.nextResource();
                System.out.println("  " + resource.getLocalName());
            }
        } else {
            System.out.println("No simple String "+riNamespace+
            		"CategoryMatching were found in the database");
        }
        
        iters = model.listSubjectsWithProperty(RDF.type,fixedCommunitiesTrust);
        if (iters.hasNext()) {
            System.out.println("The database contains resources of type fixedCommunitiesTrust:");
            while (iters.hasNext()) {
                Resource resource = iters.nextResource();
                System.out.println("  " + resource.getLocalName());
            }
        } else {
            System.out.println("No simple String riNamespace+" +
            		"FixedCommunitiesTrust were found in the database");
        }
        iters = model.listSubjectsWithProperty(RDF.type,trustBetweenCommunities);
        if (iters.hasNext()) {
            System.out.println("The database contains resources of type trustBetweenCommunities:");
            while (iters.hasNext()) {
                Resource resource = iters.nextResource();
                System.out.println("  " + resource.getLocalName());
            }
        } else {
            System.out.println("No simple String riNamespace+" +
            		"trustBetweenCommunities were found in the database");
        }
        iters = model.listSubjectsWithProperty(RDF.type,correlationBetweenDimension);
        if (iters.hasNext()) {
            System.out.println("The database contains resources of type CorrelationBetweenDimension:");
            while (iters.hasNext()) {
                Resource resource = iters.nextResource();
                System.out.println("  " + resource.getLocalName());
            }
        } else {
            System.out.println("No simple String riNamespace+" +
            		"CorrelationBetweenDimension were found in the database");
        }
        
        iters = model.listSubjectsWithProperty(RDF.type,logaritmicNumericTransformer);
        if (iters.hasNext()) {
            System.out.println("The database contains resources of type LogaritmicNumericTransformer:");
            while (iters.hasNext()) {
                Resource resource = iters.nextResource();
                System.out.println("  " + resource.getLocalName());
            }
        } else {
            System.out.println("No simple String riNamespace+" +
            		"LogaritmicNumericTransformer were found in the database");
        }
        iters = model.listSubjectsWithProperty(RDF.type,linealNumericTransformer);
        if (iters.hasNext()) {
            System.out.println("The database contains resources of type linealNumericTransformer:");
            while (iters.hasNext()) {
                Resource resource = iters.nextResource();
                System.out.println("  " + resource.getLocalName());
            }
        } else {
            System.out.println("No simple String riNamespace+" +
            		"linealNumericTransformer were found in the database");
        }        
        iters = model.listSubjectsWithProperty(RDF.type,sqrtNumericTransformer);
        if (iters.hasNext()) {
            System.out.println("The database contains resources of type SqrtNumericTransformer:");
            while (iters.hasNext()) {
                Resource resource = iters.nextResource();
                System.out.println("  " + resource.getLocalName());
            }
        } else {
            System.out.println("No simple String riNamespace+" +
            		"SqrtNumericTransformer were found in the database");
        }
        
        iters = model.listSubjectsWithProperty(RDF.type,metric);
        if (iters.hasNext()) {
            System.out.println("The database contains resources of type metric:");
            while (iters.hasNext()) {
                Resource resource = iters.nextResource();
                System.out.println("  " + resource.getLocalName());
                /* identifier */
                StmtIterator stmtI1 = model.listStatements(resource, identifier, (RDFNode)null);
				while(stmtI1.hasNext()) {
			    	Statement statement = stmtI1.nextStatement();
			    	if(statement.getObject().isLiteral()) {
			    		System.out.println("    identifier:"+statement.getObject());				    	
			    	} else if(statement.getObject().isResource()) {
			    		System.out.println("    identifier resource " +
			    				"impossible:"+statement.getObject());			    		
			    	} else {
			    		System.out.println("    identifier no literal no resource");
			    	}
			    }
				/* hasDimension */
				stmtI1 = model.listStatements(resource, hasDimension, (RDFNode)null);
				while(stmtI1.hasNext()) {
			    	Statement statement = stmtI1.nextStatement();
			    	if(statement.getObject().isLiteral()) {
			    		System.out.println("    hasDimension impossible:"+statement.getObject());				    	
			    	} else if(statement.getObject().isResource()) {
			    		System.out.println("    hasDimension resource:"+statement.getObject());
			    		Resource dimResource = statement.getObject().asResource();				    			
			    		StmtIterator stmtI2 = model.listStatements(dimResource, 
					   			(Property)null, (RDFNode)null);
					   	while(stmtI2.hasNext()) {
					    	Statement statement2 = stmtI2.nextStatement();
					    	System.out.println("      "+statement2.getPredicate().getLocalName()
					    			+" "+statement2.getObject());
					   	}
			    	} else {
			    		System.out.println("    hasDimension no literal no resource");
			    	}
			    }
				/* hasScale */
				stmtI1 = model.listStatements(resource, hasScale, (RDFNode)null);
				while(stmtI1.hasNext()) {
			    	Statement statement = stmtI1.nextStatement();
			    	if(statement.getObject().isLiteral()) {
			    		System.out.println("    hasScale impossible:"+statement.getObject());				    	
			    	} else if(statement.getObject().isResource()) {
			    		System.out.println("    hasScale resource:"+statement.getObject());
			    		Resource scaResource = statement.getObject().asResource();				    			
			    		StmtIterator stmtI2 = model.listStatements(scaResource, 
					   			(Property)null, (RDFNode)null);
					   	while(stmtI2.hasNext()) {
					    	Statement statement2 = stmtI2.nextStatement();
					    	System.out.println("      "+statement2.getPredicate().getLocalName()
					    			+" "+statement2.getObject());
					   	}
			    	} else {
			    		System.out.println("    hasScale no literal no resource");
			    	}
			    }
            }
        } else {
            System.out.println("No simple String riNamespace+" +
            		"metric were found in the database");
        }
        
        iters = model.listSubjectsWithProperty(RDF.type,collectingSystem);
        if (iters.hasNext()) {
            System.out.println("The database contains resources of type collectingSystem:");
            while (iters.hasNext()) {
                Resource resource = iters.nextResource();
                System.out.println("  " + resource.getLocalName());
                /* name */
                StmtIterator stmtI1 = model.listStatements(resource, name, (RDFNode)null);
				while(stmtI1.hasNext()) {
			    	Statement statement = stmtI1.nextStatement();
			    	if(statement.getObject().isLiteral()) {
			    		System.out.println("    name:"+statement.getObject());				    	
			    	} else if(statement.getObject().isResource()) {
			    		System.out.println("    name resource " +
			    				"impossible:"+statement.getObject());			    		
			    	} else {
			    		System.out.println("    name no literal no resource");
			    	}
			    }				
				/* uriFormat */
				stmtI1 = model.listStatements(resource, uriFormat, (RDFNode)null);
				while(stmtI1.hasNext()) {
			    	Statement statement = stmtI1.nextStatement();
			    	if(statement.getObject().isLiteral()) {
			    		System.out.println("    uriFormat:"+statement.getObject());				    	
			    	} else if(statement.getObject().isResource()) {
			    		System.out.println("    uriFormat resource impossible:"
			    				+statement.getObject());			    		
			    	} else {
			    		System.out.println("    uriFormat no literal no resource");
			    	}
			    }
            }
        } else {
            System.out.println("No simple String riNamespace+" +
            		"collectingSystem were found in the database");
        }        
        
        iters = model.listSubjectsWithProperty(RDF.type,community);
        if (iters.hasNext()) {
            System.out.println("The database contains resources of type community:");
            while (iters.hasNext()) {
                Resource resource = iters.nextResource();
                System.out.println("  " + resource.getLocalName());
                /* identifier */
                StmtIterator stmtI1 = model.listStatements(resource, identifier, (RDFNode)null);
				while(stmtI1.hasNext()) {
			    	Statement statement = stmtI1.nextStatement();
			    	if(statement.getObject().isLiteral()) {
			    		System.out.println("    identifier:"+statement.getObject());				    	
			    	} else if(statement.getObject().isResource()) {
			    		System.out.println("    identifier resource " +
			    				"impossible:"+statement.getObject());			    		
			    	} else {
			    		System.out.println("    identifier no literal no resource");
			    	}
			    }
				/* homePage */
                stmtI1 = model.listStatements(resource, homePage, (RDFNode)null);
				while(stmtI1.hasNext()) {
			    	Statement statement = stmtI1.nextStatement();
			    	if(statement.getObject().isLiteral()) {
			    		System.out.println("    homePage:"+statement.getObject());				    	
			    	} else if(statement.getObject().isResource()) {
			    		System.out.println("    homePage resource " +
			    				"impossible:"+statement.getObject());			    		
			    	} else {
			    		System.out.println("    homePage no literal no resource");
			    	}
			    }
				/* hasCategory */
				stmtI1 = model.listStatements(resource, hasCategory, (RDFNode)null);
				while(stmtI1.hasNext()) {
			    	Statement statement = stmtI1.nextStatement();
			    	if(statement.getObject().isLiteral()) {
			    		System.out.println("    hasCategory impossible:"+statement.getObject());				    	
			    	} else if(statement.getObject().isResource()) {
			    		System.out.println("    hasCategory resource:"
			    				+statement.getObject());
			    		Resource catResource = statement.getObject().asResource();				    			
			    		StmtIterator stmtI2 = model.listStatements(catResource, 
					   			(Property)null, (RDFNode)null);
					   	while(stmtI2.hasNext()) {
					    	Statement statement2 = stmtI2.nextStatement();
					    	System.out.println("      "+statement2.getPredicate().getLocalName()
					    			+" "+statement2.getObject());
					   	}
			    	} else {
			    		System.out.println("    hasCategory no literal no resource");
			    	}
			    }
				/* hasReputationModel */
				stmtI1 = model.listStatements(resource, hasReputationModel, (RDFNode)null);
				while(stmtI1.hasNext()) {
			    	Statement statement = stmtI1.nextStatement();
			    	if(statement.getObject().isLiteral()) {
			    		System.out.println("    hasReputationModel impossible:"+statement.getObject());				    	
			    	} else if(statement.getObject().isResource()) {
			    		System.out.println("    hasReputationModel resource:"
			    				+statement.getObject());
			    		Resource repResource = statement.getObject().asResource();				    			
			    		StmtIterator stmtI2 = model.listStatements(repResource, 
					   			(Property)null, (RDFNode)null);
					   	while(stmtI2.hasNext()) {
					    	Statement statement2 = stmtI2.nextStatement();
					    	if(reputationModule.getURI().equalsIgnoreCase(
					    			statement2.getPredicate().getURI())) {
					    		System.out.println("      "+reputationModule.getURI()+" property "
					    				+statement2.getObject());
					    		Resource modResource = statement2.getObject().asResource();				    			
					    		StmtIterator stmtI3 = model.listStatements(modResource, 
							   			(Property)null, (RDFNode)null);
					    		while(stmtI3.hasNext()) {
							    	Statement statement3 = stmtI3.nextStatement();
							    	if(statement3.getObject().isLiteral()) {
							    		System.out.println("         "+statement3.getPredicate().getLocalName()
							    				+":"+statement3.getObject());				    	
							    	} else if(statement3.getObject().isResource()) {
							    		System.out.println("         "+statement3.getPredicate().getLocalName()
							    				+" resource:"+statement3.getObject());
							    		Resource anyResource = statement3.getObject().asResource();				    			
							    		StmtIterator stmtI4 = model.listStatements(anyResource, 
									   			(Property)null, (RDFNode)null);
							    		if(mapsMetric.getURI().equalsIgnoreCase(
							    				statement3.getPredicate().getURI())) {
							    			
							    		} else if(importsFrom.getURI().equalsIgnoreCase(
							    				statement3.getPredicate().getURI())) {
							    			while(stmtI4.hasNext()) {
										    	Statement statement4 = stmtI4.nextStatement();
										    	if(importedCommunity.getURI().equals(statement4.getPredicate().getURI())) {
										    		if(statement4.getObject().isLiteral()) {
											    		System.out.println("                importedCommunity impossible:"
											    				+statement4.getObject());				    	
											    	} else if(statement4.getObject().isResource()) {
											    		System.out.println("                importedCommunity resource:"
											    				+statement4.getObject());
											    		Resource impResource = statement4.getObject().asResource();				    			
											    		StmtIterator stmtI5 = model.listStatements(impResource, 
													   			(Property)null, (RDFNode)null);
													   	while(stmtI5.hasNext()) {
													    	Statement statement5 = stmtI5.nextStatement();
													    	System.out.println("                      "
													    			+statement5.getPredicate().getLocalName()
													    			+" "+statement5.getObject());
													   	}
											    	} else {
											    		System.out.println("                "+
											    				"importedCommunity no literal no resource");
											    	}
										    	} else if(importedMetric.getURI().equals(statement4.getPredicate().getURI())) {
										    		if(statement4.getObject().isLiteral()) {
											    		System.out.println("                importedMetric impossible:"
											    				+statement4.getObject());				    	
											    	} else if(statement4.getObject().isResource()) {
											    		System.out.println("                importedMetric resource:"
											    				+statement4.getObject());
											    		Resource impResource = statement4.getObject().asResource();				    			
											    		StmtIterator stmtI5 = model.listStatements(impResource, 
													   			(Property)null, (RDFNode)null);
													   	while(stmtI5.hasNext()) {
													    	Statement statement5 = stmtI5.nextStatement();
													    	System.out.println("                      "
													    			+statement5.getPredicate().getLocalName()
													    			+" "+statement5.getObject());
													   	}
											    	} else {
											    		System.out.println("                "+
											    				"importedMetric no literal no resource");
											    	}
										    	} else if(collectsReputationBy.getURI().equals(statement4.getPredicate().getURI())) {
										    		if(statement4.getObject().isLiteral()) {
											    		System.out.println("                collectsReputationBy impossible:"
											    				+statement4.getObject());				    	
											    	} else if(statement4.getObject().isResource()) {
											    		System.out.println("                collectsReputationBy resource:"
											    				+statement4.getObject());
											    		Resource colResource = statement4.getObject().asResource();				    			
											    		StmtIterator stmtI5 = model.listStatements(colResource, 
													   			(Property)null, (RDFNode)null);
													   	while(stmtI5.hasNext()) {
													    	Statement statement5 = stmtI5.nextStatement();
													    	System.out.println("                      "
													    			+statement5.getPredicate().getLocalName()
													    			+" "+statement5.getObject());
													   	}
											    	} else {
											    		System.out.println("                "+
											    				"collectsReputationBy no literal no resource");
											    	}
										    	} else if(metricTransformation.getURI().equals(statement4.getPredicate().getURI())) {
										    		if(statement4.getObject().isLiteral()) {
											    		System.out.println("                metricTransformation impossible:"
											    				+statement4.getObject());				    	
											    	} else if(statement4.getObject().isResource()) {
											    		System.out.println("                metricTransformation resource:"
											    				+statement4.getObject());
											    		Resource metResource = statement4.getObject().asResource();				    			
											    		StmtIterator stmtI5 = model.listStatements(metResource, 
													   			(Property)null, (RDFNode)null);
													   	while(stmtI5.hasNext()) {
													    	Statement statement5 = stmtI5.nextStatement();
													    	System.out.println("                      "
													    			+statement5.getPredicate().getLocalName()
													    			+" "+statement5.getObject());
													   	}
											    	} else {
											    		System.out.println("                "+
											    				"metricTransformation no literal no resource");
											    	}
										    	} else if(trust.getURI().equals(statement4.getPredicate().getURI())) {
										    		if(statement4.getObject().isLiteral()) {
											    		System.out.println("                trust impossible:"
											    				+statement4.getObject());				    	
											    	} else if(statement4.getObject().isResource()) {
											    		System.out.println("                trust resource:"
											    				+statement4.getObject());
											    		Resource truResource = statement4.getObject().asResource();				    			
											    		StmtIterator stmtI5 = model.listStatements(truResource, 
													   			(Property)null, (RDFNode)null);
													   	while(stmtI5.hasNext()) {
													    	Statement statement5 = stmtI5.nextStatement();
													    	System.out.println("                      "
													    			+statement5.getPredicate().getLocalName()
													    			+" "+statement5.getObject());
													   	}
											    	} else {
											    		System.out.println("                "+
											    				"trust no literal no resource");
											    	}
										    	} else {
										    		System.out.println("            "+statement4.getPredicate().getLocalName()
										    			+" "+statement4.getObject());
										    	}
										   	}							    			
							    		} else {
							    			while(stmtI4.hasNext()) {
										    	Statement statement4 = stmtI4.nextStatement();
										    	System.out.println("            "+statement4.getPredicate().getLocalName()
										    			+" "+statement4.getObject());
										   	}
							    		}
							    	} else {
							    		System.out.println("         "+statement3.getPredicate().getLocalName()
							    				+" no literal no resource");
							    	}
							   	}
					    	} else {
					    		System.out.println("      "+statement2.getPredicate().getLocalName()
					    			+" "+statement2.getObject());
					    	}
					   	}
			    	} else {
			    		System.out.println("    hasReputationModel no literal no resource");
			    	}
			    }				
            }
        } else {
            System.out.println("No simple String riNamespace+" +
            		"community were found in the database");
        }
        
        
        
        
        
        
        /*
        iters = model.listSubjects();
        if (iters.hasNext()) {
            System.out.println("Subjects:");
            while (iters.hasNext()) {
                Resource resource = iters.nextResource();
                System.out.println("  " + resource.getLocalName());
               // node.
                
            }
        } 
        
        StmtIterator stms = model.listStatements();
        if (stms.hasNext()) {
            System.out.println("Statements:");
            while (stms.hasNext()) {
                Statement stm = stms.nextStatement();
                System.out.println("  " + stm.getSubject()+"-"+stm.getPredicate()
                		+"-"+stm.getObject());
               // node.
                
            }
        }
        */        
                     
        
	}
	
	private void addPropertiesAndResources(){
		
		resources.add(ResourceFactory.createResource(riNamespace + "Community"));
        /*resources.add(ResourceFactory.createResource(riNamespace + "CollectingSystem"));
        resources.add(ResourceFactory.createResource(riNamespace + "Metric"));
        resources.add(ResourceFactory.createResource(riNamespace + "SqrtNumericTransformer"));
        resources.add(ResourceFactory.createResource(riNamespace + "ExponentialNumericTransformer"));
        resources.add(ResourceFactory.createResource(riNamespace + "LogaritmicNumericTransformer"));
        resources.add(ResourceFactory.createResource(riNamespace + "LinealNumericTransformer"));
        resources.add(ResourceFactory.createResource(riNamespace + "CorrelationBetweenDimension"));
        resources.add(ResourceFactory.createResource(riNamespace + "TrustBetweenCommunities"));
        resources.add(ResourceFactory.createResource(riNamespace + "FixedCommunitiesTrust"));
        resources.add(ResourceFactory.createResource(riNamespace + "CategoryMatching"));
        resources.add(ResourceFactory.createResource(riNamespace + "hasDimension"));*/
        
        communitiesProperty.add(ResourceFactory.createProperty(riNamespace, "hasCategory"));
        communitiesProperty.add(ResourceFactory.createProperty(riNamespace, "hasReputationModel"));
        communitiesProperty.add(ResourceFactory.createProperty(riNamespace, "reputationModule"));
        communitiesProperty.add(ResourceFactory.createProperty(riNamespace, "mapsMetric"));
        communitiesProperty.add(ResourceFactory.createProperty(riNamespace, "importsFrom"));
        communitiesProperty.add(ResourceFactory.createProperty(riNamespace, "hasCategory"));
	}
	
}
