package es.upm.dit;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.rulesys.RDFSRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.RDFSRuleReasonerFactory;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

import cross.reputation.model.AccessType;
import cross.reputation.model.CategoricScale;
import cross.reputation.model.Category;
import cross.reputation.model.CategoryMatching;
import cross.reputation.model.CollectingAlgorithm;
import cross.reputation.model.CollectingSystem;
import cross.reputation.model.CollectionType;
import cross.reputation.model.Community;
import cross.reputation.model.Dimension;
import cross.reputation.model.DimensionCorrelation;
import cross.reputation.model.EntityType;
import cross.reputation.model.ExponentialNumericTransformer;
import cross.reputation.model.FixedCommunitiesTrust;
import cross.reputation.model.ImportationUnit;
import cross.reputation.model.LinealNumericTransformer;
import cross.reputation.model.LogaritmicNumericTransformer;
import cross.reputation.model.Metric;
import cross.reputation.model.MetricMapping;
import cross.reputation.model.MetricTransformer;
import cross.reputation.model.ModuleInfo;
import cross.reputation.model.NumericScale;
import cross.reputation.model.ReputationAlgorithm;
import cross.reputation.model.ReputationModel;
import cross.reputation.model.ReputationStep;
import cross.reputation.model.ReputationValue;
import cross.reputation.model.ReputationalAction;
import cross.reputation.model.Scale;
import cross.reputation.model.SqrtNumericTransformer;
import cross.reputation.model.TrustBetweenCommunities;

public class ReputationParser {
	String riNamespace = "http://purl.org/reputationImport/0.1/";
	String dcNamespace = "http://purl.org/dc/elements/1.1/";
	
	static public void main(String[] args) {
		ReputationParser parser = new ReputationParser();
		//foaf.foafAgent("http://localhost/foafSample2.rdf");
		
		/*try {			
			Method method = parser.getClass().getMethod(
					"getNumericScale", Model.class, Resource.class);
			System.out.println(method.getName()+" "+method);
			for(Type type : method.getGenericParameterTypes()) {
				System.out.println("  "+type);
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}*/
		
		parser.getCrossReputationGlobalModelFromRDF("dir/model0.rdf");
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
        if(model.getNsPrefixURI("dc") != null) {
        	dcNamespace = model.getNsPrefixURI("dc");
            System.out.println("dc namespace:"+dcNamespace);
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
        
        try {
			printDimensions(model);
			printCategories(model);
			printCategoryMatchings(model);
			printFixedCommunitiesTrust(infModel);
			printTrustBetweenCommunities(model);
			printCollectingSystems(model);
			printDimensionCorrelations(model);
			printScales(model);
			printNumericScales(model);
			printMetrics(model);
			printLogaritmicNumericTransformers(model);
			printLinealNumericTransformers(model);
			printSqrtNumericTransformers(model);
			printMetricMappings(model);
			printImportationUnits(model);
			printReputationImporters(model);
			//printCommunity();			
			//printReputationValues();
			//printReputationObjects();
		} catch (Exception e) {
			e.printStackTrace();
		}
        /*
        ResIterator iters = model.listSubjectsWithProperty(RDF.type,community);
        if (iters.hasNext()) {
            System.out.println("The database contains resources of type community:");
            while (iters.hasNext()) {
                Resource resource = iters.nextResource();
                System.out.println("  " + resource.getLocalName());
                //  identifier //
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
				// homePage //
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
				// hasCategory
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
				// hasReputationModel
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
        }*/         
	}
	
	Community getCommunity(Model model, Resource resource) {
		Community community = new Community();
		
		return community;
	}
	
	public cross.reputation.model.ReputationImporter getReputationImporter(Model model, Resource resource) throws Exception {
		cross.reputation.model.ReputationImporter repImp = 
			(cross.reputation.model.ReputationImporter) 
			getReputationAlgorithm(model, resource, 
			cross.reputation.model.ReputationImporter.class);
			
		return repImp;
	}
	
	public void printReputationImporters(Model model) throws Exception {
		Resource reputationImporter = ResourceFactory.
				createResource(riNamespace + "ReputationImporter");
		ResIterator iters = model.listSubjectsWithProperty(
				RDF.type,reputationImporter);
		if (iters.hasNext()) {
		    System.out.println("The database contains subjects" +
		    		" of type reputationImporter:");
		    while (iters.hasNext()) {
		        Resource resource = iters.nextResource();
		        System.out.println("  " + resource.getLocalName());
		        cross.reputation.model.ReputationImporter repImpInstance = 
		        	getReputationImporter(model,resource);
		        for(MetricMapping metMap : repImpInstance.getMapsMetrics()) {
		        	System.out.println("     importedMetric:" +	metMap);
		        }
		        for(ImportationUnit impUnit : repImpInstance.getImportsFrom()) {
		        	System.out.println("     importsFrom:" +	impUnit);
		        }		        
		    }
		} else {
		    System.out.println("No simple String riNamespace+" +
		    		"reputationImporter were found in the database");
		}
	}
	
	public ImportationUnit getImportationUnit(Model model, Resource resource) throws Exception {
		ImportationUnit impUni = new ImportationUnit();
		Property importedCommunity = ResourceFactory.createProperty(
				riNamespace + "importedCommunity");
		/* importedCommunity */
		StmtIterator stmtI1 = model.listStatements(resource, 
				importedCommunity, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate importedCommunity property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("importedCommunity property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				impUni.setImportedCommunity(getCommunity(model, 
						statement.getObject().asResource()));
			}
		}
		Property importedMetric = ResourceFactory.createProperty(
				riNamespace + "importedMetric");
		/* importedMetric */
		stmtI1 = model.listStatements(resource, 
				importedMetric, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate importedMetric property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("importedMetric property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				impUni.setImportedMetric(getMetric(model, 
						statement.getObject().asResource()));
			}
		}
		Property collectsReputationBy = ResourceFactory.createProperty(
				riNamespace + "collectsReputationBy");
		/* collectsReputationBy */
		stmtI1 = model.listStatements(resource, 
				collectsReputationBy, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate collectsReputationBy property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("collectsReputationBy property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				impUni.setCollectsReputationBy(getCollectingAlgorithm(model, 
						statement.getObject().asResource(),null));
			}
		}
		Property metricTransformation = ResourceFactory.createProperty(
				riNamespace + "metricTransformation");
		/* metricTransformation */
		stmtI1 = model.listStatements(resource, 
				metricTransformation, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate metricTransformation property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("metricTransformation property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				impUni.setMetricTransformation(getMetricTransformer(model, 
						statement.getObject().asResource(),null));
			}
		}
		Property trust = ResourceFactory.createProperty(
				riNamespace + "trust");
		/* trust */
		stmtI1 = model.listStatements(resource, 
				trust, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate trust property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("trust property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				impUni.setTrust(getTrustBetweenCommunities(model, 
						statement.getObject().asResource(),null));
			}
		}
		return impUni;
	}
	
	public void printImportationUnits(Model model) throws Exception {
		Resource importationUnit = ResourceFactory.
				createResource(riNamespace + "ImportationUnit");
		ResIterator iters = model.listSubjectsWithProperty(
				RDF.type,importationUnit);
		if (iters.hasNext()) {
		    System.out.println("The database contains subjects" +
		    		" of type ImportationUnit:");
		    while (iters.hasNext()) {
		        Resource resource = iters.nextResource();
		        System.out.println("  " + resource.getLocalName());
		        ImportationUnit impUniInstance = 
		        	getImportationUnit(model,resource);
		        System.out.println("     importedMetric:" +
		        		impUniInstance.getImportedMetric());
		        if(impUniInstance.getImportedMetric() != null) {
		        	System.out.println("     "+		        		
		        		impUniInstance.getImportedMetric().toString("          "));
		        }
		        System.out.println("     importedCommunity:" +
		        		impUniInstance.getImportedCommunity());
		        if(impUniInstance.getImportedCommunity() != null) {
		        	System.out.println("     "+
		        		impUniInstance.getImportedCommunity().toString("          "));
		        }
		        System.out.println("     metricTransformation:" +
		        		impUniInstance.getMetricTransformation());
		        if(impUniInstance.getMetricTransformation() != null) {
		        	System.out.println("     "+
		        		impUniInstance.getMetricTransformation().toString("          "));
		        }
		        System.out.println("     collectsReputationBy:" +
		        		impUniInstance.getCollectsReputationBy());
		    }
		} else {
		    System.out.println("No simple String riNamespace+" +
		    		"ImportationUnit were found in the database");
		}
	}
	
	public MetricMapping getMetricMapping(Model model, Resource resource) throws Exception {
		MetricMapping metMap = new MetricMapping();
		Property value = ResourceFactory.createProperty(
				riNamespace + "value");
		/* value */
		StmtIterator stmtI1 = model.listStatements(resource, 
				value, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate value property */
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("value property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				metMap.setValue(statement.getObject().asLiteral().getDouble());
			}
		}		
		Property importedMetric = ResourceFactory.createProperty(
				riNamespace + "importedMetric");
		/* importedMetric */
		stmtI1 = model.listStatements(resource, 
				importedMetric, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate importedMetric property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("importedMetric property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				metMap.setImportedMetric(getMetric(model, 
						statement.getObject().asResource()));
			}
		}
		Property resultMetric = ResourceFactory.createProperty(
				riNamespace + "resultMetric");
		/* resultMetric */
		stmtI1 = model.listStatements(resource, 
				resultMetric, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate resultMetric property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("resultMetric property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				metMap.setResultMetric(getMetric(model, 
						statement.getObject().asResource()));
			}
		}
		return metMap;
	}
	
	public void printMetricMappings(Model model) throws Exception {
		Resource metricMapping = ResourceFactory.
				createResource(riNamespace + "MetricMapping");
		ResIterator iters = model.listSubjectsWithProperty(
				RDF.type,metricMapping);
		if (iters.hasNext()) {
		    System.out.println("The database contains subjects" +
		    		" of type MetricMappings:");
		    while (iters.hasNext()) {
		        Resource resource = iters.nextResource();
		        System.out.println("  " + resource.getLocalName());
		        MetricMapping metMapInstance = 
		        	getMetricMapping(model,resource);
		        System.out.println("     importedMetric:" +
		        		metMapInstance.getImportedMetric());
		        System.out.println("     resultMetric:" +
		        		metMapInstance.getResultMetric());
		        System.out.println("     value:" +
		        		metMapInstance.getValue());
		    }
		} else {
		    System.out.println("No simple String riNamespace+" +
		    		"MetricMappings were found in the database");
		}
	}
	
	public Metric getMetric(Model model, Resource resource) throws Exception {
		Metric metric = new Metric();
		Property identifier = ResourceFactory.createProperty(
				riNamespace + "identifier");
		// identifier //
		StmtIterator stmtI1 = model.listStatements(resource, 
				identifier, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate identificator property //
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("identificator property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				metric.setIdentifier(statement.getObject().asLiteral().getString());
			}
		}
		Property description = ResourceFactory.createProperty(
				dcNamespace + "description");
		// description //
		stmtI1 = model.listStatements(resource, 
				description, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate description property //
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("description property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				metric.setDescription(statement.getObject().asLiteral().getString());
			}
		}
		Property hasScale = ResourceFactory.createProperty(
				riNamespace + "hasScale");
		// hasScale //
		stmtI1 = model.listStatements(resource, 
				hasScale, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate hasScale property //
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("hasScale property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				metric.setScale(getScale(model, 
						statement.getObject().asResource(),null));
			}
		}
		Property hasDimension = ResourceFactory.createProperty(
				riNamespace + "hasDimension");
		// hasDimension //
		stmtI1 = model.listStatements(resource, 
				hasDimension, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate hasDimension property //
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("hasDimension property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				metric.setDimension(getDimension(model, 
						statement.getObject().asResource()));
			}
		}
		return metric;
	}
	
	public void printMetrics(Model model) throws Exception {
		Resource metric = ResourceFactory.
				createResource(riNamespace + "Metric");
		ResIterator iters = model.listSubjectsWithProperty(
				RDF.type,metric);
		if (iters.hasNext()) {
		    System.out.println("The database contains subjects" +
		    		" of type metric:");
		    while (iters.hasNext()) {
		        Resource resource = iters.nextResource();
		        System.out.println("  " + resource.getLocalName());
		        Metric metricInstance = 
		        	getMetric(model,resource);
		        System.out.println(metricInstance.toString("     "));
		        /*System.out.println("     identificator:" +
		        		metricInstance.getIdentifier());
		        System.out.println("     description:" +
		        		metricInstance.getDescription());
		        System.out.println("     scale:" +
		        		metricInstance.getScale());
		        System.out.println("     dimension:" +
		        		metricInstance.getDimension());*/
		    }
		} else {
		    System.out.println("No simple String riNamespace+" +
		    		"metric were found in the database");
		}
	}
	
	public ReputationValue getReputationValue(Model model, Resource resource) {
		ReputationValue reputationValue = new ReputationValue();
		
		return reputationValue;
	}
	
	public CategoricScale getCategoricScale(Model model, 
			Resource resource) throws Exception {
		CategoricScale categoricScale = (CategoricScale) getScale(
				model, resource, CategoricScale.class);
		Property allowValue = ResourceFactory.createProperty(
				riNamespace + "allowValue");
		// allowValue //
		StmtIterator stmtI1 = model.listStatements(resource, 
				allowValue, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate allowValue property //
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("allowValue property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				categoricScale.addCategories(statement.getObject().asLiteral().getString());
			}
		}		
		return categoricScale;
	}
	
	public NumericScale getNumericScale(Model model, 
			Resource resource) throws Exception {
		NumericScale numericScale = (NumericScale) getScale(
				model, resource, NumericScale.class);
		Property minimum = ResourceFactory.createProperty(
				riNamespace + "minimum");
		/* minimum */
		StmtIterator stmtI1 = model.listStatements(resource, 
				minimum, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate minimum property */
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("minimum property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				numericScale.setMinimum(statement.getObject().asLiteral().getDouble());
			}
		}
		Property maximum = ResourceFactory.createProperty(
				riNamespace + "maximum");
		/* maximum */
		stmtI1 = model.listStatements(resource, 
				maximum, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate maximum property */
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("maximum property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				numericScale.setMaximum(statement.getObject().asLiteral().getDouble());
			}
		}
		Property step = ResourceFactory.createProperty(
				riNamespace + "step");
		/* step */
		stmtI1 = model.listStatements(resource, 
				step, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate step property */
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("step property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				numericScale.setStep(statement.getObject().asLiteral().getDouble());
			}
		}
		return numericScale;
	}
	
	public void printNumericScales(Model model) throws Exception {
		Resource numericScale = ResourceFactory.
				createResource(riNamespace + "NumericScale");
		ResIterator iters = model.listSubjectsWithProperty(
				RDF.type,numericScale);
		if (iters.hasNext()) {
		    System.out.println("The database contains subjects" +
		    		" of type NumericScale:");
		    while (iters.hasNext()) {
		        Resource resource = iters.nextResource();
		        System.out.println("  " + resource.getLocalName());
		        NumericScale numScaInstance = 
		        	getNumericScale(model,resource);
		        System.out.println("     name:" +
		        		numScaInstance.getName());
		        System.out.println("     maximum:" +
		        		numScaInstance.getMaximum());
		        System.out.println("     minimum:" +
		        		numScaInstance.getMinimum());
		        System.out.println("     step:" +
		        		numScaInstance.getStep());
		    }
		} else {
		    System.out.println("No simple String riNamespace+" +
		    		"scale were found in the database");
		}
	}
	
	public Scale getScale(Model model, Resource resource, Class type) throws Exception {
		if(type == null) {
			StmtIterator stmtI1 = model.listStatements(resource, RDF.type, (RDFNode)null);
			Resource numericScale = ResourceFactory.
					createResource(riNamespace + "NumericScale");
			Resource categoricScale = ResourceFactory.
					createResource(riNamespace + "CategoricScale");
			while(stmtI1.hasNext()) {
				Statement typeStatement = stmtI1.nextStatement();
				if(typeStatement.getObject().asResource().getURI().equals(
						numericScale.getURI())) {
					Scale scale = getNumericScale(model, resource);
					return scale;
				} else if (typeStatement.getObject().asResource().getURI().equals( 
						categoricScale.getURI())) {
					Scale scale = getCategoricScale(model, resource);
					return scale;
				}				
			}
			if(type == null) {
				throw new Exception("Impossible to instanciate a generic Scale. " +
						"Please, see the model and assign a specific type.");			
			}
		}
		Scale scale = (Scale) type.newInstance();
		Property name = ResourceFactory.
				createProperty(riNamespace + "name");
		/* name */
		StmtIterator stmtI1 = model.listStatements(resource, 
				name, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate name property */
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("name property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				scale.setName(statement.getObject().asLiteral().getString());
			}
		}
		return scale;
	}
	
	public void printScales(Model model) throws Exception {
		Resource scale = ResourceFactory.
				createResource(riNamespace + "Scale");
		ResIterator iters = model.listSubjectsWithProperty(
				RDF.type,scale);
		if (iters.hasNext()) {
		    System.out.println("The database contains subjects" +
		    		" of type scale:");
		    while (iters.hasNext()) {
		        Resource resource = iters.nextResource();
		        System.out.println("  " + resource.getLocalName());
		        Scale scaleInstance = 
		        	getScale(model,resource,null);
		        System.out.println("     name:" +
		        		scaleInstance.getName());		        	        
		    }
		} else {
		    System.out.println("No simple String riNamespace+" +
		    		"scale were found in the database");
		}
	}
	
	public CollectionType getCollectionType(Resource resource) {
		if(!resource.getNameSpace().equals(riNamespace)) {
			return null;
		}
		if(resource.getLocalName().equalsIgnoreCase("list")) {
			return CollectionType.LIST;
		} else if(resource.getLocalName().equalsIgnoreCase("UNIQUE")) {
			return CollectionType.UNIQUE;			
		}
		return null;
	}
	
	public AccessType getAccessType(Resource resource) {
		if(!resource.getNameSpace().equals(riNamespace)) {
			return null;
		}
		if(resource.getLocalName().equalsIgnoreCase("downloadable")) {
			return AccessType.DOWNLOADABLE;
		} else if(resource.getLocalName().equalsIgnoreCase("VISIBLE")) {
			return AccessType.VISIBLE;			
		} else if(resource.getLocalName().equalsIgnoreCase("API")) {
			return AccessType.API;			
		} else if(resource.getLocalName().equalsIgnoreCase("PRIVATE")) {
			return AccessType.PRIVATE;			
		} else if(resource.getLocalName().equalsIgnoreCase("INTERNAL")) {
			return AccessType.INTERNAL;			
		}
		return null;
	}
	
	public EntityType getEntityType(Model model, Resource resource) throws Exception {
		EntityType entityType = new EntityType();
		Property type = ResourceFactory.
				createProperty(riNamespace + "type");
		/* type */
		StmtIterator stmtI1 = model.listStatements(resource, 
				type, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate accesibility property */
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("type property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				entityType.setType(statement.getObject().asLiteral().getString());
			}
		}
		return entityType;
	}
	
	public ReputationStep getReputationStep(
			Model model, Resource resource) throws Exception {
		ReputationStep reputationStep = (ReputationStep) getReputationAlgorithm(
				model, resource, ReputationStep.class);
		Property stepIdentificator = ResourceFactory.createProperty(
				riNamespace + "stepIdentificator");
		/* name */
		StmtIterator stmtI1 = model.listStatements(resource, 
				stepIdentificator, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate name property */
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("name property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				reputationStep.setStepIdentificator(
						statement.getObject().asLiteral().getInt());		    	
			}
		}
		return reputationStep;
	}
	
	public ReputationAlgorithm getReputationAlgorithm(Model model, 
			Resource resource, Class type) throws Exception {
		ReputationAlgorithm repAlg = null;
		if(type == null) {			
			StmtIterator stmtI1 = model.listStatements(resource, RDF.type, (RDFNode)null);
			Map<Resource,Method> childrenType = new HashMap<Resource,Method>();
			childrenType.put(ResourceFactory.createResource(
					riNamespace + "ReputationProcessStep"), 
					this.getClass().getMethod("getReputationStep",
							Model.class, Resource.class));
			childrenType.put(ResourceFactory.createResource(
					riNamespace + "ReputationImporter"), 
					this.getClass().getMethod("getReputationImporter",
							Model.class, Resource.class));
			childrenType.put(ResourceFactory.createResource(
					riNamespace + "CollectingAlgorithm"), 
					this.getClass().getMethod("getCollectingAlgorithm",
							Model.class, Resource.class, Class.class));
			childrenType.put(ResourceFactory.createResource(
					riNamespace + "ModuleInfo"), 
					this.getClass().getMethod("getModuleInfo",
							Model.class, Resource.class));
			childrenType.put(ResourceFactory.createResource(
					riNamespace + "ReputationModel"), 
					this.getClass().getMethod("getReputationalModel",
							Model.class, Resource.class));
			while(stmtI1.hasNext()) {
				Statement typeStatement = stmtI1.nextStatement();
				for(Resource resourceType : childrenType.keySet()) {
					if(typeStatement.getObject().asResource().getURI().equals(
							resourceType.getURI())) {
						childrenType.get(resourceType).invoke(model, resource, null);
					}
				}								
			}
			if(type == null) {
				repAlg = new ReputationAlgorithm();		
			}			
		} else {
			repAlg = (ReputationAlgorithm) type.newInstance();
		}
		Property accesibility = ResourceFactory.
				createProperty(riNamespace + "accesibility");
		/* accesibility */
		StmtIterator stmtI1 = model.listStatements(resource, 
				accesibility, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate accesibility property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("uriFormat property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				repAlg.setAccesibility(getAccessType(
						statement.getObject().asResource()));
			}
		}
		Property name = ResourceFactory.
				createProperty(riNamespace + "name");
		/* name */
		stmtI1 = model.listStatements(resource, 
				name, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate name property */
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("name property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				repAlg.setName(statement.getObject().asLiteral().getString());		    	
			}
		}		
		Property resultCollectionType = ResourceFactory.
				createProperty(riNamespace + "resultCollectionType");
		/* resultCollectionType */
		stmtI1 = model.listStatements(resource, 
				resultCollectionType, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate resultCollectionType property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("resultCollectionType property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				repAlg.setResultCollectionType(getCollectionType(
						statement.getObject().asResource()));		    	
			}
		}
		Property description = ResourceFactory.
				createProperty(riNamespace + "description");
		/* description */
		stmtI1 = model.listStatements(resource, 
				description, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* description name property */
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("name description of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				repAlg.setDescription(statement.getObject().asLiteral().getString());		    	
			}
		}
		Property entityType = ResourceFactory.
				createProperty(riNamespace + "entityType");
		/* entityType */
		stmtI1 = model.listStatements(resource, 
				entityType, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate entityType property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("entityType property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				repAlg.addEntityType(getEntityType(model,
						statement.getObject().asResource()));		    	
			}
		}
		Property usesMetric = ResourceFactory.
				createProperty(riNamespace + "usesMetric");
		/* usesMetric */
		stmtI1 = model.listStatements(resource, 
				usesMetric, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate usesMetric property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("usesMetric property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				repAlg.addUsesMetrics(getMetric(model,
						statement.getObject().asResource()));		    	
			}
		}
		Property reputationSource = ResourceFactory.
				createProperty(riNamespace + "reputationSource");
		/* reputationSource */
		stmtI1 = model.listStatements(resource, 
				reputationSource, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate reputationSource property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("reputationSource property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				repAlg.addReputationSources(getReputationValue(model,
						statement.getObject().asResource()));		    	
			}
		}
		Property reputationResult = ResourceFactory.
				createProperty(riNamespace + "reputationResult");
		/* reputationResult */
		stmtI1 = model.listStatements(resource, 
				reputationResult, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate reputationResult property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("reputationResult property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				repAlg.addReputationResults(getReputationValue(model,
						statement.getObject().asResource()));		    	
			}
		}
		return repAlg;
	}
	
	public void printReputationAlgorithms(Model model) throws Exception {
		Resource reputationAlgorithm = ResourceFactory.
				createResource(riNamespace + "ReputationAlgorithm");
		ResIterator iters = model.listSubjectsWithProperty(
				RDF.type,reputationAlgorithm);
		if (iters.hasNext()) {
		    System.out.println("The database contains subjects" +
		    		" of type ReputationAlgorithm:");
		    while (iters.hasNext()) {
		        Resource resource = iters.nextResource();
		        System.out.println("  " + resource.getLocalName());
		        ReputationAlgorithm repAlgInstance = 
		        	getReputationAlgorithm(model,resource,null);
		        System.out.println("     name:" +
		        		repAlgInstance.getName());		        	        
		    }
		} else {
		    System.out.println("No simple String riNamespace+" +
		    		"ReputationAlgorithm were found in the database");
		}
	}
	
	public ReputationalAction getReputationalAction(
			Model model, Resource resource) throws Exception {
		ReputationalAction repAct = (ReputationalAction)getCollectingAlgorithm(
				model, resource, ReputationalAction.class);
		return repAct;
	}
	
	public CollectingAlgorithm getCollectingAlgorithm(Model model, 
			Resource resource, Class<?> type) throws Exception {
		CollectingAlgorithm colAlg = null;
		if(type == null) {
			StmtIterator stmtI1 = model.listStatements(resource, RDF.type, (RDFNode)null);
			Resource reputationalAction = ResourceFactory.
					createResource(riNamespace + "ReputationalAction");
			Resource collectingSystem = ResourceFactory.
					createResource(riNamespace + "CategoryMatching");
			while(stmtI1.hasNext()) {
				Statement typeStatement = stmtI1.nextStatement();
				if(typeStatement.getObject().asResource().getURI().equals(
						reputationalAction.getURI())) {
					return getReputationalAction(model, resource);
				} else if (typeStatement.getObject().asResource().getURI().equals( 
						collectingSystem.getURI())) {
					return getCollectingSystem(model, resource);
				}
				//System.out.println(typeStatement.getObject().asResource().getURI() + "?=\n"+
				//		categoryMatching.getURI()+" "+typeStatement.getObject().asResource().getURI().equals( 
				//				categoryMatching.getURI()));
			}
			if(type == null) {
				type = CollectingAlgorithm.class;			
			}
		}
		colAlg = (CollectingAlgorithm) getReputationAlgorithm(model, resource, type);
		return colAlg;
	}
	
	public ModuleInfo getModuleInfo(Model model, 
			Resource resource) throws Exception {
		ModuleInfo moduleInfo = new ModuleInfo();
		Property collectReputationsBy = ResourceFactory.
				createProperty(riNamespace + "collectReputationsBy");		
		/* collectReputationsBy */
		StmtIterator stmtI1 = model.listStatements(resource, 
				collectReputationsBy, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate collectReputationsBy property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("collectReputationsBy property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				moduleInfo.addCollectsReputationsBy(getCollectingAlgorithm(model,
						statement.getObject().asResource(), null));		    	
			}
		}
		return moduleInfo;
	}
	
	public ReputationModel getReputationModel(Model model,
			Resource resource) throws Exception {
		ReputationModel repMod = (ReputationModel) getReputationAlgorithm(
				model, resource, ReputationModel.class);		
		Property reputationModule = ResourceFactory.
				createProperty(riNamespace + "reputationModule");		
		/* reputationModule */
		StmtIterator stmtI1 = model.listStatements(resource, 
				reputationModule, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate reputationResult property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("reputationResult property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				repMod.addReputationModules(getModuleInfo(model,
						statement.getObject().asResource()));		    	
			}
		}
		return repMod;
	}
	
	public void printReputationModels(Model model) throws Exception {
		Resource reputationModel = ResourceFactory.
				createResource(riNamespace + "ReputationModel");
		ResIterator iters = model.listSubjectsWithProperty(
				RDF.type,reputationModel);
		if (iters.hasNext()) {
		    System.out.println("The database contains subjects" +
		    		" of type ReputationModel:");
		    while (iters.hasNext()) {
		        Resource resource = iters.nextResource();
		        System.out.println("  " + resource.getLocalName());
		        ReputationModel repModInstance = 
		        	getReputationModel(model,resource);
		        System.out.println("     name:" +
		        		repModInstance.getName());		        	        
		    }
		} else {
		    System.out.println("No simple String riNamespace+" +
		    		"ReputationModel were found in the database");
		}
	}
	
	public CollectingSystem getCollectingSystem(
			Model model, Resource resource) throws Exception {
		CollectingSystem colSys = (CollectingSystem)getCollectingAlgorithm(
				model, resource, CollectingSystem.class);
		Property uriFormat = ResourceFactory.
				createProperty(riNamespace + "uriFormat");
		/* uriFormat */
		StmtIterator stmtI1 = model.listStatements(resource, 
				uriFormat, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate uriFormat property */
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("uriFormat property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				colSys.setUriFormat(new URI(
						statement.getObject().asLiteral().getString()));
			}
		}		
		return colSys;
	}
	
	public void printCollectingSystems(Model model) throws Exception {
		Resource collectingSystem = ResourceFactory.
				createResource(riNamespace + "CollectingSystem");
		ResIterator iters = model.listSubjectsWithProperty(
				RDF.type,collectingSystem);
		if (iters.hasNext()) {
		    System.out.println("The database contains subjects" +
		    		" of type CollectingSystem:");
		    while (iters.hasNext()) {
		        Resource resource = iters.nextResource();
		        System.out.println("  " + resource.getLocalName());
		        CollectingSystem colSysInstance = 
		        	getCollectingSystem(model,resource);
		        System.out.println("     name:" +
		        		colSysInstance.getName());
		        System.out.println("     URIFormat:" +
		        		colSysInstance.getUriFormat());		        
		    }
		} else {
		    System.out.println("No simple String riNamespace+" +
		    		"CollectingSystem were found in the database");
		}
	}
	
	class MetricTransformerInstances {
		Metric sourceMetric;
		Metric destinationMetric;
		List<Double> correlationBetweenMetrics;
		String description;
		
		MetricTransformerInstances(Metric sourceMetric, Metric destinationMetric,
				List<Double> correlationBetweenMetrics, String description) {
			this.sourceMetric = sourceMetric;
			this.destinationMetric = destinationMetric;
			this.correlationBetweenMetrics = correlationBetweenMetrics;
			this.description = description;
		}
	}
	
	public MetricTransformer getMetricTransformer(
			Model model, Resource resource, Class type) throws Exception {
		//MetricTransformer 
		return null;
	}
	
	public MetricTransformerInstances getMetricTransformer(
			Model model, Resource resource) throws Exception {
		Property sourceMetric = ResourceFactory.
				createProperty(riNamespace + "sourceMetric");
		/* sourceMetric */
		Metric sourceInstance = null;		
		StmtIterator stmtI1 = model.listStatements(resource, 
				sourceMetric, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate sourceMetric property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("sourceMetric property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				sourceInstance = getMetric(
						model, statement.getObject().asResource());	    			    	
			}
		}
		Property destinationMetric = ResourceFactory.
				createProperty(riNamespace + "destinationMetric");
		/* destinationMetric */
		Metric destinationInstance = null;
		stmtI1 = model.listStatements(resource, 
				destinationMetric, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate destinationMetric property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("destinationMetric property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				destinationInstance = getMetric(
						model, statement.getObject().asResource());	    				    	
			}
		}
				
		//TODO: correlationBetweenMetrics
		/*Property correlationBetweenMetrics = ResourceFactory.
		createProperty(riNamespace + "correlationBetweenMetrics");
		Double correlationInstance = null;
		stmtI1 = model.listStatements(resource, 
				correlationBetweenMetrics, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate correlationBetweenMetrics property //
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("correlationBetweenMetrics property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				correlationInstance =
						Double.parseDouble(statement.getObject().toString());		    	
			}
		}*/
		List<Double> correlationsInstance = new ArrayList<Double>();
		Property correlationBetweenDimensions = ResourceFactory.
		createProperty(riNamespace + "correlationBetweenDimensions");
		DimensionCorrelation correlationInstance = null;
		stmtI1 = model.listStatements(resource, 
				correlationBetweenDimensions, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate correlationBetweenMetrics property //
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("correlationBetweenDimensions property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				correlationInstance = getDimensionCorrelation(model,
						statement.getObject().asResource());
				correlationsInstance.add(correlationInstance.getCorrelationValue());
			}
		}
		Property correlationBetweenScales = ResourceFactory.
		createProperty(riNamespace + "correlationBetweenScales");
		stmtI1 = model.listStatements(resource, 
				correlationBetweenScales, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate correlationBetweenMetrics property //
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("correlationBetweenScales property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				correlationsInstance.add(
						Double.parseDouble(statement.getObject().toString()));		    	
			}
		}		
		
		Property description = ResourceFactory.
				createProperty(dcNamespace + "description");
		/* description */
		String descriptionInstance = null;
		stmtI1 = model.listStatements(resource, 
				description, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate description property */
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("description property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				descriptionInstance = statement.getObject().asLiteral().getString();		    	
			}
		}
		return new MetricTransformerInstances(sourceInstance, 
				destinationInstance, correlationsInstance, descriptionInstance);
	}
	
	public ExponentialNumericTransformer getExponentialNumericTransformer(
			Model model, Resource resource) throws Exception {
		MetricTransformerInstances metTraInstance = 
			getMetricTransformer(model, resource);
		Double baseInstance = getBaseObject(model, resource);
		ExponentialNumericTransformer expNum = null;
		if(baseInstance == null) {
			expNum = new ExponentialNumericTransformer(
					metTraInstance.sourceMetric, metTraInstance.destinationMetric, 
					metTraInstance.correlationBetweenMetrics);
		} else {
			expNum = new ExponentialNumericTransformer(
					metTraInstance.sourceMetric, metTraInstance.destinationMetric, 
					metTraInstance.correlationBetweenMetrics, baseInstance);
		}
		expNum.setDescription(metTraInstance.description);
		return expNum;
	}
	
	public void printExponentialNumericTransformers(Model model) throws Exception {
		Resource exponentialNumericTransformer = ResourceFactory.
				createResource(riNamespace + "ExponentialNumericTransformer");
		ResIterator iters = model.listSubjectsWithProperty(
				RDF.type,exponentialNumericTransformer);
		if (iters.hasNext()) {
		    System.out.println("The database contains subjects" +
		    		" of type ExponentialNumericTransformer:");
		    while (iters.hasNext()) {
		        Resource resource = iters.nextResource();
		        System.out.println("  " + resource.getLocalName());
		        ExponentialNumericTransformer expNumInstance = 
		        	getExponentialNumericTransformer(model,resource);
		        System.out.println("     sourceMetric:" +
		        		expNumInstance.getSourceMetric());
		        System.out.println("     destinationMetric:" +
		        		expNumInstance.getDestinationMetric());
		        System.out.println("     correlationBetMetrics:" +
		        		expNumInstance.getCorrelationBetweenMetrics());
		        System.out.println("     base:" +
		        		expNumInstance.getBase());
		        System.out.println("     scale:" +
		        		expNumInstance.getScale());
		    }
		} else {
		    System.out.println("No simple String riNamespace+" +
		    		"ExponentialNumericTransformer were found in the database");
		}
	}
	
	public SqrtNumericTransformer getSqrtNumericTransformer(
			Model model, Resource resource) throws Exception {
		MetricTransformerInstances metTraInstance = 
			getMetricTransformer(model, resource);
		SqrtNumericTransformer sqrtNum = null;		
		sqrtNum = new SqrtNumericTransformer(
					metTraInstance.sourceMetric, metTraInstance.destinationMetric, 
					metTraInstance.correlationBetweenMetrics);		
		sqrtNum.setDescription(metTraInstance.description);
		return sqrtNum;
	}
	
	public void printSqrtNumericTransformers(Model model) throws Exception {
		Resource sqrtNumericTransformer = ResourceFactory.
				createResource(riNamespace + "SqrtNumericTransformer");
		ResIterator iters = model.listSubjectsWithProperty(
				RDF.type,sqrtNumericTransformer);
		if (iters.hasNext()) {
		    System.out.println("The database contains subjects" +
		    		" of type SqrtNumericTransformer:");
		    while (iters.hasNext()) {
		        Resource resource = iters.nextResource();
		        System.out.println("  " + resource.getLocalName());
		        SqrtNumericTransformer sqrtNumInstance = 
		        	getSqrtNumericTransformer(model,resource);
		        System.out.println("     sourceMetric:" +
		        		sqrtNumInstance.getSourceMetric());
		        System.out.println("     destinationMetric:" +
		        		sqrtNumInstance.getDestinationMetric());
		        System.out.println("     correlationBetMetrics:" +
		        		sqrtNumInstance.getCorrelationBetweenMetrics());
		        System.out.println("     base:" +
		        		sqrtNumInstance.getBase());
		        System.out.println("     scale:" +
		        		sqrtNumInstance.getScale());
		    }
		} else {
		    System.out.println("No simple String riNamespace+" +
		    		"SqrtNumericTransformer were found in the database");
		}
	}
	
	public LinealNumericTransformer getLinealNumericTransformer(
			Model model, Resource resource) throws Exception {
		MetricTransformerInstances metTraInstance = 
			getMetricTransformer(model, resource);
		LinealNumericTransformer linNum = null;
		linNum = new LinealNumericTransformer(
					metTraInstance.sourceMetric, metTraInstance.destinationMetric, 
					metTraInstance.correlationBetweenMetrics);		
		linNum.setDescription(metTraInstance.description);
		return linNum;
	}
	
	public void printLinealNumericTransformers(Model model) throws Exception {
		Resource linealNumericTransformer = ResourceFactory.
				createResource(riNamespace + "LinealNumericTransformer");
		ResIterator iters = model.listSubjectsWithProperty(
				RDF.type,linealNumericTransformer);
		if (iters.hasNext()) {
		    System.out.println("The database contains subjects" +
		    		" of type LinealNumericTransformer:");
		    while (iters.hasNext()) {
		        Resource resource = iters.nextResource();
		        System.out.println("  " + resource.getLocalName());
		        LinealNumericTransformer linNumInstance = 
		        	getLinealNumericTransformer(model,resource);
		        System.out.println("     sourceMetric:" +
		        		linNumInstance.getSourceMetric());
		        System.out.println("     destinationMetric:" +
		        		linNumInstance.getDestinationMetric());
		        System.out.println("     correlationBetMetrics:" +
		        		linNumInstance.getCorrelationBetweenMetrics());
		        System.out.println("     difference:" +
		        		linNumInstance.getDifference());
		        System.out.println("     scale:" +
		        		linNumInstance.getScale());
		    }
		} else {
		    System.out.println("No simple String riNamespace+" +
		    		"LinealNumericTransformer were found in the database");
		}
	}
	
	public Double getBaseObject(Model model, Resource resource) throws Exception {
		Property base = ResourceFactory.
				createProperty(riNamespace + "base");
		/* base */
		StmtIterator stmtI1 = model.listStatements(resource, 
				base, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate base property */
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("base property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				return statement.getObject().asLiteral().getDouble();
				//return Double.parseDouble(statement.getObject().toString());		    	
			}
		}
		return null;
	}
	
	public LogaritmicNumericTransformer getLogaritmicNumericTransformer(
			Model model, Resource resource) throws Exception {
		MetricTransformerInstances metTraInstance = 
			getMetricTransformer(model, resource);
		Double baseInstance = getBaseObject(model, resource);
		LogaritmicNumericTransformer logNum = null;
		if(baseInstance == null) {
			logNum = new LogaritmicNumericTransformer(
					metTraInstance.sourceMetric, metTraInstance.destinationMetric, 
					metTraInstance.correlationBetweenMetrics);
		} else {
			logNum = new LogaritmicNumericTransformer(
					metTraInstance.sourceMetric, metTraInstance.destinationMetric, 
					metTraInstance.correlationBetweenMetrics, baseInstance);
		}
		logNum.setDescription(metTraInstance.description);
		return logNum;
	}
	
	public void printLogaritmicNumericTransformers(Model model) throws Exception {
		Resource logaritmicNumericTransformer = ResourceFactory.
				createResource(riNamespace + "LogaritmicNumericTransformer");
		ResIterator iters = model.listSubjectsWithProperty(
				RDF.type,logaritmicNumericTransformer);
		if (iters.hasNext()) {
		    System.out.println("The database contains subjects" +
		    		" of type LogaritmicNumericTransformer:");
		    while (iters.hasNext()) {
		        Resource resource = iters.nextResource();
		        System.out.println("  " + resource.getLocalName());
		        LogaritmicNumericTransformer logNumInstance = 
		        	getLogaritmicNumericTransformer(model,resource);
		        System.out.println("     sourceMetric:" +
		        		logNumInstance.getSourceMetric());
		        System.out.println("     destinationMetric:" +
		        		logNumInstance.getDestinationMetric());
		        System.out.println("     correlationBetMetrics:" +
		        		logNumInstance.getCorrelationBetweenMetrics());
		        System.out.println("     base:" +
		        		logNumInstance.getBase());
		        System.out.println("     difference:" +
		        		logNumInstance.getDifference());
		        System.out.println("     scale:" +
		        		logNumInstance.getScale());
		    }
		} else {
		    System.out.println("No simple String riNamespace+" +
		    		"LogaritmicNumericTransformer were found in the database");
		}
	}	
	
	public DimensionCorrelation getDimensionCorrelation(
			Model model, Resource resource) throws Exception {
		DimensionCorrelation dimCor = new DimensionCorrelation();
		Property sourceDimension = ResourceFactory.
				createProperty(riNamespace + "sourceDimension");		
		/* sourceDimension */
        StmtIterator stmtI1 = model.listStatements(resource, 
        		sourceDimension, (RDFNode)null);
		while(stmtI1.hasNext()) {
	    	Statement statement = stmtI1.nextStatement();
	    	/* validate sourceDimension property */
	    	if(!statement.getObject().isResource()) {	    		
	    		throw new Exception("sourceDimension property of resource:"+
	    				resource.getURI()+" is not a resource");
	    	} else {
	    		Dimension dimension = getDimension(model, statement.getObject().asResource());
	    		dimCor.setSourceDimension(dimension);		    	
	    	}
	    }
		Property targetDimension = ResourceFactory.
				createProperty(riNamespace + "targetDimension");
		/* targetDimension */
        stmtI1 = model.listStatements(resource, 
        		targetDimension, (RDFNode)null);
		while(stmtI1.hasNext()) {
	    	Statement statement = stmtI1.nextStatement();
	    	/* validate targetDimension property */
	    	if(!statement.getObject().isResource()) {	    		
	    		throw new Exception("targetDimension property of resource:"+
	    				resource.getURI()+" is not a resource");
	    	} else {
	    		Dimension dimension = getDimension(model, statement.getObject().asResource());
	    		dimCor.setTargetDimension(dimension);		    	
	    	}
	    }
		Property correlationValue = ResourceFactory.
				createProperty(riNamespace + "correlationValue");		
		/* correlationValue */
		stmtI1 = model.listStatements(resource, 
				correlationValue, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate correlationValue property */
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("correlationValue property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				dimCor.setCorrelationValue(
						statement.getObject().asLiteral().getDouble());
				//dimCor.setCorrelationValue(
				//		Double.parseDouble(statement.getObject().toString()));		    	
			}
		}		
		return dimCor;
	}	
	
	public void printDimensionCorrelations(Model model) throws Exception {
		Resource dimensionCorrelation = ResourceFactory.
				createResource(riNamespace + "DimensionCorrelation");
		ResIterator iters = model.listSubjectsWithProperty(RDF.type,dimensionCorrelation);
		if (iters.hasNext()) {
		    System.out.println("The database contains subjects of type dimensionCorrelation:");
		    while (iters.hasNext()) {
		        Resource resource = iters.nextResource();
		        System.out.println("  " + resource.getLocalName());
		        DimensionCorrelation dimCorInstance = getDimensionCorrelation(model,resource);
		        System.out.println("     sourceDimension:" +
		        		dimCorInstance.getSourceDimension().getName());
		        System.out.println("     targetDimension:" +
		        		dimCorInstance.getTargetDimension().getName());
		        System.out.println("     correlationValue:" +
		        		dimCorInstance.getCorrelationValue());
		    }
		} else {
		    System.out.println("No simple String riNamespace+" +
		    		"DimensionCorrelation were found in the database");
		}
	}
	
	public TrustBetweenCommunities getTrustBetweenCommunities(
			Model model, Resource resource, Class type) throws Exception {
		TrustBetweenCommunities truBet = null;
		if(type != null) {
			truBet = (TrustBetweenCommunities) type.newInstance();
		} else {
			StmtIterator stmtI1 = model.listStatements(resource, RDF.type, (RDFNode)null);
			Resource fixedCommunitiesTrust = ResourceFactory.
					createResource(riNamespace + "FixedCommunitiesTrust");
			Resource categoryMatching = ResourceFactory.
					createResource(riNamespace + "CategoryMatching");
			while(stmtI1.hasNext()) {
				Statement typeStatement = stmtI1.nextStatement();
				if(typeStatement.getObject().asResource().getURI().equals(
						fixedCommunitiesTrust.getURI())) {
					return getFixedCommunitiesTrust(model, resource);
				} else if (typeStatement.getObject().asResource().getURI().equals( 
						categoryMatching.getURI())) {
					return getCategoryMatching(model, resource);
				}			
			}
			if(truBet == null) {
				truBet = new TrustBetweenCommunities();
			}
		}
		Property trustProvidedBy = ResourceFactory.
				createProperty(riNamespace + "trustProvidedBy");		
		/* trustProvidedBy */
		StmtIterator stmtI1 = model.listStatements(resource, 
				trustProvidedBy, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate trustProvidedBy property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("trustProvidedBy property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				TrustBetweenCommunities truBetInside = getTrustBetweenCommunities(
						model, statement.getObject().asResource(), null);
				truBet.addTrustProvidedBy(truBetInside);		    	
			}
		}
		if(truBet.getValue() == null) {
			Property value = ResourceFactory.
					createProperty(riNamespace + "value");			
			/* value */
			stmtI1 = model.listStatements(resource, 
					value, (RDFNode)null);
			while(stmtI1.hasNext()) {
				Statement statement = stmtI1.nextStatement();
				/* validate value property */
				if(!statement.getObject().isLiteral()) {	    		
					throw new Exception("value property of resource:"+
							resource.getURI()+" is not a literal");
				} else {
					truBet.setValue(statement.getObject().asLiteral().getDouble());
					//truBet.setValue(Double.parseDouble(statement.getObject().toString()));		    	
				}
			}
		}
		return truBet;
	}
	
	public void printTrustBetweenCommunities(Model model) throws Exception {
		Resource trustBetweenCommunities = ResourceFactory.
				createResource(riNamespace + "TrustBetweenCommunities");		
		ResIterator iters = model.listSubjectsWithProperty(RDF.type,trustBetweenCommunities);
		if (iters.hasNext()) {
		    System.out.println("The database contains subjects of type TrustBetweenCommunities:");
		    while (iters.hasNext()) {
                Resource resource = iters.nextResource();
                System.out.println("  " + resource.getLocalName());
                TrustBetweenCommunities truBetInstance = null;
                
                /* boolean isASubclass = false;
                Resource fixedCommunitiesTrust = ResourceFactory.
						createResource(riNamespace + "FixedCommunitiesTrust");
				Resource categoryMatching = ResourceFactory.
						createResource(riNamespace + "CategoryMatching");
                StmtIterator stmtI1 = model.listStatements(resource, RDF.type, (RDFNode)null);
				while(stmtI1.hasNext()) {
					Statement statementType = stmtI1.nextStatement();
					if(statementType.getObject().asResource().getURI().equals( 
							fixedCommunitiesTrust.getURI())) {
						truBetInstance = getFixedCommunitiesTrust(model,resource);
						isASubclass = true;
						break;
					} else if (statementType.getObject().asResource().getURI().equals( 
							categoryMatching.getURI())) {
						truBetInstance = getFixedCommunitiesTrust(model,resource);
						isASubclass = true;
						break;						
					}					
				}
				if(!isASubclass) {
					truBetInstance = getTrustBetweenCommunities(model,resource);
				}*/
                
                truBetInstance = getTrustBetweenCommunities(model,resource, null);
				if(truBetInstance != null) {					
			        System.out.println("     class:" +
			        		truBetInstance.getClass());
			        System.out.println(truBetInstance.toString("     "));			        
				}
		    }		    
		} else {
		    System.out.println("No simple String riNamespace+Dimension were found in the database");
		}
	}
	
	public void printFixedCommunitiesTrust(Model model) throws Exception {
		Resource fixedCommunitiesTrust = ResourceFactory.
				createResource(riNamespace + "FixedCommunitiesTrust");
		ResIterator iters = model.listSubjectsWithProperty(RDF.type,fixedCommunitiesTrust);
        if (iters.hasNext()) {
            System.out.println("The database contains subjects of type FixedCommunitiesTrust:");
            while (iters.hasNext()) {
                Resource resource = iters.nextResource();
                System.out.println("  " + resource.getLocalName());
                FixedCommunitiesTrust fixComInstance = getFixedCommunitiesTrust(model,resource);
                System.out.println("     communityScorer:" +
                		fixComInstance.getCommunityScorer().getName());
                System.out.println("     communityScored:" +
                		fixComInstance.getCommunityScorer().getName());
                System.out.println("     value:" +
                		fixComInstance.getValue());
            }
        } else {
            System.out.println("No simple String riNamespace+Dimension were found in the database");
        }
	}
	
	public FixedCommunitiesTrust getFixedCommunitiesTrust(
			Model model, Resource resource) throws Exception {
		FixedCommunitiesTrust fixCom = (FixedCommunitiesTrust) 
			getTrustBetweenCommunities(model, resource, FixedCommunitiesTrust.class);
		Property communityScorer = ResourceFactory.
				createProperty(riNamespace + "communityScorer");		
		/* communityScorer */
        StmtIterator stmtI1 = model.listStatements(resource, 
        		communityScorer, (RDFNode)null);
		while(stmtI1.hasNext()) {
	    	Statement statement = stmtI1.nextStatement();
	    	/* validate originatingCategory property */
	    	if(!statement.getObject().isResource()) {	    		
	    		throw new Exception("communityScorer property of resource:"+
	    				resource.getURI()+" is not a resource");
	    	} else {
	    		Community community = getCommunity(model, statement.getObject().asResource());
	    		fixCom.setCommunityScorer(community);		    	
	    	}
	    }
		Property communityScored = ResourceFactory.
				createProperty(riNamespace + "communityScored");		
		/* communityScored */
		stmtI1 = model.listStatements(resource, 
				communityScored, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate communityScored property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("communityScored property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				Community community = getCommunity(model, statement.getObject().asResource());
	    		fixCom.setCommunityScored(community);		    	
			}
		}		
		return fixCom;
	}
	
	public void printCategoryMatchings(Model model) throws Exception {
		Resource categoryMatching = ResourceFactory.
				createResource(riNamespace + "CategoryMatching");
		ResIterator iters = model.listSubjectsWithProperty(RDF.type,categoryMatching);
        if (iters.hasNext()) {
            System.out.println("The database contains subjects of type categoryMatching:");
            while (iters.hasNext()) {
                Resource resource = iters.nextResource();
                System.out.println("  " + resource.getLocalName());
                CategoryMatching catMatInstance = getCategoryMatching(model,resource);
                System.out.println("     originatingCategory:" +
                		catMatInstance.getOriginatingCategory().getName());
                System.out.println("     receivingCategory:" +
                		catMatInstance.getReceivingCategory().getName());
                System.out.println("     value:" +
                		catMatInstance.getValue());
            }
        } else {
            System.out.println("No simple String riNamespace+Dimension were found in the database");
        }
	}
	
	public CategoryMatching getCategoryMatching(Model model, Resource resource) throws Exception {
		CategoryMatching catMat = (CategoryMatching) 
			getTrustBetweenCommunities(model, resource, CategoryMatching.class);;
		Property originatingCategory = ResourceFactory.
				createProperty(riNamespace + "originatingCategory");		
		/* originatingCategory */
        StmtIterator stmtI1 = model.listStatements(resource, 
        		originatingCategory, (RDFNode)null);
		while(stmtI1.hasNext()) {
	    	Statement statement = stmtI1.nextStatement();
	    	/* validate originatingCategory property */
	    	if(!statement.getObject().isResource()) {	    		
	    		throw new Exception("originatingCategory property of resource:"+
	    				resource.getURI()+" is not a resource");
	    	} else {
	    		Category category = getCategory(model, statement.getObject().asResource());
	    		catMat.setOriginatingCategory(category);		    	
	    	}
	    }
		Property receivingCategory = ResourceFactory.
				createProperty(riNamespace + "receivingCategory");		
		/* receivingCategory */
		stmtI1 = model.listStatements(resource, 
				receivingCategory, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate receivingCategory property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("receivingCategory property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				Category category = getCategory(model, statement.getObject().asResource());
				catMat.setReceivingCategory(category);		    	
			}
		}		
		return catMat;
	}
	
	public void printCategories(Model model) throws Exception {
		Resource category = ResourceFactory.createResource(riNamespace + "Category");
		ResIterator iters = model.listSubjectsWithProperty(RDF.type,category);
        if (iters.hasNext()) {
            System.out.println("The database contains subjects of type category:");
            while (iters.hasNext()) {
                Resource resource = iters.nextResource();
                System.out.println("  " + resource.getLocalName());
                Category categoryInstance = getCategory(model,resource);
                System.out.println("     name:" + categoryInstance.getName());                				
            }
        } else {
            System.out.println("No simple String riNamespace+Dimension were found in the database");
        }
	}
	
	public Category getCategory(Model model, Resource resource) throws Exception {
		Category category = null;
		Property name = ResourceFactory.createProperty(riNamespace + "name");		
		/* name */
        StmtIterator stmtI1 = model.listStatements(resource, name, (RDFNode)null);
		while(stmtI1.hasNext()) {
	    	Statement statement = stmtI1.nextStatement();
	    	category = new Category(statement.getObject().asLiteral().getString());
	    	//category = new Category(statement.getObject().toString());
	    	/* validate name property */
	    	if(!statement.getObject().isLiteral()) {	    		
	    		throw new Exception("name property of resource:"+
	    				resource.getURI()+" is not a literal");
	    	}
	    }		
		return category;
	}
	
	public void printDimensions(Model model) throws Exception {
		Resource dimension = ResourceFactory.createResource(riNamespace + "Dimension");
		ResIterator iters = model.listSubjectsWithProperty(RDF.type,dimension);
        if (iters.hasNext()) {
            System.out.println("The database contains subjects of type dimension:");
            while (iters.hasNext()) {
                Resource resource = iters.nextResource();
                System.out.println("  " + resource.getLocalName());
                Dimension dimensionInstance = getDimension(model,resource);
                System.out.println("     name:" + dimensionInstance.getName());
                if(dimensionInstance.getDescription() != null) {
                	System.out.println("     description:" + dimensionInstance.getName());
                }				
            }
        } else {
            System.out.println("No simple String riNamespace+Dimension were found in the database");
        }
	}
	
	public Dimension getDimension(Model model, Resource resource) throws Exception {
		Dimension dimension = null;
		Property name = ResourceFactory.createProperty(riNamespace + "name");		
		/* name */
        StmtIterator stmtI1 = model.listStatements(resource, name, (RDFNode)null);
		while(stmtI1.hasNext()) {
	    	Statement statement = stmtI1.nextStatement();
	    	/* validate name property */
	    	if(!statement.getObject().isLiteral()) {	    		
	    		throw new Exception("name property of resource:"+
	    				resource.getURI()+" is not a literal");
	    	}
	    	dimension = new Dimension(statement.getObject().asLiteral().getString());
	    	//dimension = new Dimension(statement.getObject().toString());	    	
	    }
		Property description = ResourceFactory.createProperty(riNamespace + "description");		
		/* description */
        stmtI1 = model.listStatements(resource, description, (RDFNode)null);
		while(stmtI1.hasNext()) {
	    	Statement statement = stmtI1.nextStatement();
	    	/* validate description property */
	    	if(!statement.getObject().isLiteral()) {
	    		throw new Exception("description property of resource:"+
	    				resource.getURI()+" is not a literal");
	    	}
	    	if(dimension != null) {
	    		dimension.setDescription(statement.getObject().asLiteral().getString());
	    		//dimension.setDescription(statement.getObject().toString());
	    	}	    	
	    }
		return dimension;
	}
	
}
