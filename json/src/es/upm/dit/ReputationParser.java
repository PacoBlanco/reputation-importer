package es.upm.dit;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import cross.reputation.model.CollectingAlgorithmBehaviour;
import cross.reputation.model.CollectingSystem;
import cross.reputation.model.CollectingSystemBehaviour;
import cross.reputation.model.CollectionType;
import cross.reputation.model.Community;
import cross.reputation.model.Dimension;
import cross.reputation.model.DimensionCorrelation;
import cross.reputation.model.Entity;
import cross.reputation.model.EntityType;
import cross.reputation.model.ExponentialNumericTransformer;
import cross.reputation.model.FixedCommunitiesTrust;
import cross.reputation.model.ImportationUnit;
import cross.reputation.model.LinealNumericTransformer;
import cross.reputation.model.LogaritmicNumericTransformer;
import cross.reputation.model.Metric;
import cross.reputation.model.MetricMapping;
import cross.reputation.model.MetricTransformer;
import cross.reputation.model.ReputationAlgorithmI;
import cross.reputation.model.ReputationAlgorithmImplementation;
import cross.reputation.model.ReputationBehaviour;
import cross.reputation.model.ReputationImporterBehaviour;
import cross.reputation.model.ReputationModelBehaviour;
import cross.reputation.model.ReputationModule;
import cross.reputation.model.NumericScale;
import cross.reputation.model.NumericTransformer;
import cross.reputation.model.ReputationAlgorithm;
import cross.reputation.model.ReputationModel;
import cross.reputation.model.ReputationModuleBehaviour;
import cross.reputation.model.ReputationStep;
import cross.reputation.model.ReputationStepBehaviour;
import cross.reputation.model.ReputationValue;
import cross.reputation.model.ReputationalAction;
import cross.reputation.model.ReputationalActionBehaviour;
import cross.reputation.model.Scale;
import cross.reputation.model.SqrtNumericTransformer;
import cross.reputation.model.TrustBetweenCommunities;

public class ReputationParser {
	String riNamespace = "http://purl.org/reputationImport/0.1/";
	String dcNamespace = "http://purl.org/dc/elements/1.1/";
	Map<Resource,Class<? extends ReputationBehaviour>> reputationAlgorithmSubclasses;
	Map<Property,Class<? extends ReputationBehaviour>> reputationSubclassesProperties;
	Map<Property,Class<? extends ReputationBehaviour>> reputationSubclassSubjectProperties;
	Map<Resource,Set<Object>> cache = new HashMap<Resource,Set<Object>>();
	
	static public void main(String[] args) throws Exception {
		ReputationParser parser = new ReputationParser();
		//foaf.foafAgent("http://localhost/foafSample2.rdf");
		
		/* Tests with reflect.Proxy to add dinamically interfaces
		Class[] interfacesArray = new Class[] {ReputationAlgorithmI.class};		
		java.lang.reflect.Proxy proxy = Proxy.newProxyInstance(
				ReputationAlgorithm.class.getClassLoader(), interfacesArray, arg2)
		*/
		parser.getCrossReputationGlobalModelFromRDF("dir/model0.rdf");
	}
	
	public ReputationParser() throws Exception {
		reputationAlgorithmSubclasses();		
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
			printReputationModels(model);
        	printAllReputationAlgorithms(model);
			printCommunities(model);
			//printEntities(model);
			//printReputationValues(model);
			//printReputationObjects(model);
		} catch (Exception e) {
			e.printStackTrace();
		}               
	}
	
	@SuppressWarnings("unchecked")
	private void reputationAlgorithmSubclasses() throws Exception {
		reputationAlgorithmSubclasses = 
			new HashMap<Resource,Class<? extends ReputationBehaviour>>();			
		List<Class<? extends ReputationBehaviour>> subclasses = 
			ReputationAlgorithmImplementation.listSubclasses();
		while(!subclasses.isEmpty()) {
			Class<? extends ReputationBehaviour> subclass = subclasses.get(0);
			try {
				Method method = subclass.getMethod("listSubclasses");
				List<Class<? extends ReputationBehaviour>> subsubclasses = 
					(List<Class<? extends ReputationBehaviour>>) method.invoke(null);
				subclasses.addAll(subsubclasses);
			} catch (Exception e) {					
			}
			String ontologySubClass = null;
			if(subclass.getSimpleName().endsWith("Behaviour")) {
				ontologySubClass = subclass.getSimpleName().substring(
						0,subclass.getSimpleName().length()-9);
			}
			reputationAlgorithmSubclasses.put(ResourceFactory.createResource(
					riNamespace + ontologySubClass),subclass);
			subclasses.remove(0);			
		}
		
		reputationSubclassesProperties = 
			new HashMap<Property,Class<? extends ReputationBehaviour>>();
		reputationSubclassesProperties.put(ResourceFactory.
			createProperty(riNamespace + "reputationModule"),
			ReputationModuleBehaviour.class);
		reputationSubclassesProperties.put(ResourceFactory.
				createProperty(riNamespace + "definedByReputationModel"),
				ReputationModelBehaviour.class);
		reputationSubclassesProperties.put(ResourceFactory.
				createProperty(riNamespace + "hasReputationStep"),
				ReputationStepBehaviour.class);
		reputationSubclassesProperties.put(ResourceFactory.
				createProperty(riNamespace + "collectsReputationBy"),
				CollectingAlgorithmBehaviour.class);
		reputationSubclassSubjectProperties = 
			new HashMap<Property,Class<? extends ReputationBehaviour>>();
		reputationSubclassSubjectProperties.put(ResourceFactory.
				createProperty(riNamespace + "reputationModule"),
				ReputationModelBehaviour.class);
		reputationSubclassSubjectProperties.put(ResourceFactory.
				createProperty(riNamespace + "obtainsReputationBy"),
				ReputationModuleBehaviour.class);
		reputationSubclassSubjectProperties.put(ResourceFactory.
				createProperty(riNamespace + "mapsMetric"),
				ReputationImporterBehaviour.class);
		reputationSubclassSubjectProperties.put(ResourceFactory.
				createProperty(riNamespace + "importsFrom"),
				ReputationImporterBehaviour.class);
	}
	
	
	public Object getResourceFromCache(Resource resource, Class<?> clazz) {
		Set<Object> objects = cache.get(resource);
		if(objects == null) {
			return null;
		}
		//System.out.println(resource+" --- "+clazz+" --- "+objects.size());
		for(Object object : objects) {
			if(clazz.isInstance(object)) {
				return object;
			}
		}
		return null;
	}
	
	private void addResourceInstanceToCache(Resource resource, Object object) {
		//System.out.println(resource+"  ---  "+object);
		Set<Object> objects = cache.get(resource);
		if(objects == null) {
			objects = new HashSet<Object>();
			cache.put(resource,objects);
		}
		objects.add(object);		
	}
	
	public Community getLimitedCommunity(Model model, Resource resource) throws Exception {
		Community community = (Community) getResourceFromCache(resource, Community.class);
		if(community == null) {
			community = new Community();			
		} else {
			return community;
		}
		// Specific Attributes and Properties of Community Class //
		// name //
		Property identifier = ResourceFactory.createProperty(
				riNamespace + "identifier");
		StmtIterator stmtI1 = model.listStatements(resource, 
				identifier, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate identifier property //
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("identifier property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				community.setName(statement.getObject().asLiteral().getString());
			}
		}
		return community;
	}
	
	public Entity getEntity(Model model, Resource resource) throws Exception {
		Entity entity = (Entity) getResourceFromCache(resource, Entity.class);
		if(entity != null) {
			return entity;
		}
		entity = new Entity();
		addResourceInstanceToCache(resource,entity);
		
		// Specific Attributes and Properties of Community Class //
		
		return entity;
	}	
	
	public Community getCommunity(Model model, Resource resource) throws Exception {
		Community community = (Community) getResourceFromCache(resource, Community.class);
		if(community == null) {
			community = new Community();
			addResourceInstanceToCache(resource,community);
		} else {
			return community;
		}
		// Specific Attributes and Properties of Community Class //
		// name //
		Property identifier = ResourceFactory.createProperty(
				riNamespace + "identifier");
		StmtIterator stmtI1 = model.listStatements(resource, 
				identifier, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate identifier property //
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("identifier property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				community.setName(statement.getObject().asLiteral().getString());
			}
		}
		// homePage //
		Property homePage = ResourceFactory.createProperty(
				riNamespace + "homePage");
		stmtI1 = model.listStatements(resource, 
				homePage, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate homePage property //
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("homePage property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				community.setDomainName(statement.getObject().asLiteral().getString());
			}
		}
		// hasCategory //
		Property mapsMetric = ResourceFactory.createProperty(
				riNamespace + "hasCategory");		
		stmtI1 = model.listStatements(resource, 
				mapsMetric, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate hasCategory property //
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("hasCategory property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				Category category = (Category) getResourceFromCache(
						statement.getObject().asResource(), Category.class);
				if(category == null) {
					category = getCategory(model, 
							statement.getObject().asResource());					
				}
				community.addCategory(category);
			}
		}
		// hasReputationModel //
		Property hasReputationModel = ResourceFactory.createProperty(
				riNamespace + "hasReputationModel");		
		stmtI1 = model.listStatements(resource, 
				hasReputationModel, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate hasReputationModel property //
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("hasReputationModel property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				ReputationAlgorithmImplementation repAlg = (ReputationAlgorithmImplementation)
					getResourceFromCache(statement.getObject().asResource(),
					ReputationAlgorithmImplementation.class);
				if(repAlg == null) {
					repAlg = getReputationAlgorithm(model, 
							statement.getObject().asResource(),null);					
				}
				community.setReputationModel(repAlg);
			}
		}
		// hasEntity //
		Property hasEntity = ResourceFactory.createProperty(
				riNamespace + "hasEntity");		
		stmtI1 = model.listStatements(resource, 
				hasEntity, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate hasEntity property //
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("hasEntity property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				Entity entity = (Entity) getResourceFromCache(
						statement.getObject().asResource(), Entity.class);
				if(entity == null) {
					entity = getEntity(model, 
							statement.getObject().asResource());					
				}
				community.addEntity(entity);
			}
		}
		return community;
	}
	
	public void printCommunities(Model model) throws Exception {
		Resource community = ResourceFactory.createResource(
				riNamespace + "Community");
		ResIterator iters = model.listSubjectsWithProperty(
				RDF.type,community);
		if (iters.hasNext()) {
		    System.out.println("The database contains subjects" +
		    		" of type community:");
		    while (iters.hasNext()) {
		        Resource resource = iters.nextResource();
		        System.out.println("  " + resource.getLocalName());
		        Community communityInstance = 
		        	getCommunity(model,resource);
		        System.out.println(communityInstance.toString("     ")); 
		    }
		} else {
		    System.out.println("No simple String riNamespace+" +
		    		"community were found in the database");
		}
	}
	
	public ReputationAlgorithmImplementation getReputationImporter(
			Model model, Resource resource) throws Exception {
		ReputationAlgorithmImplementation repImp = (
				ReputationAlgorithmImplementation) getResourceFromCache(
						resource, ReputationAlgorithmImplementation.class);
		if(repImp != null) {			
			return repImp;
		}		
		List<Class<? extends ReputationBehaviour>> types =
			new ArrayList<Class<? extends ReputationBehaviour>>();
		types.add(ReputationImporterBehaviour.class);
		repImp = getReputationAlgorithm(model, resource,types);
		ReputationImporterBehaviour repImpBeh = null;
		for(ReputationBehaviour behaviour : repImp.getBehaviours()) {
			if(behaviour instanceof ReputationImporterBehaviour) {
				repImpBeh = (ReputationImporterBehaviour) behaviour;
			}
		}

		// Specific Attributes and Properties of ReputationImporter Class //		
		Property mapsMetric = ResourceFactory.createProperty(
				riNamespace + "mapsMetric");
		/* mapsMetric */
		StmtIterator stmtI1 = model.listStatements(resource, 
				mapsMetric, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate mapsMetric property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("mapsMetric property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				MetricMapping metMap = (MetricMapping) getResourceFromCache(
						statement.getObject().asResource(), MetricMapping.class);
				if(metMap == null) {
					metMap = getMetricMapping(model, 
							statement.getObject().asResource());					
				}
				repImpBeh.addMapsMetrics(metMap);
			}
		}
		Property importsFrom = ResourceFactory.createProperty(
				riNamespace + "importsFrom");
		/* importsFrom */
		stmtI1 = model.listStatements(resource, 
				importsFrom, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate importsFrom property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("importsFrom property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				ImportationUnit impUni = (ImportationUnit) getResourceFromCache(
						statement.getObject().asResource(), ImportationUnit.class);
				if(impUni == null) {
					impUni = getImportationUnit(model, 
							statement.getObject().asResource());					
				}
				repImpBeh.addImportsFrom(impUni);
			}
		}	
		return repImp;
	}
	
	public ReputationBehaviour getReputationImporterBehaviour(Model model, 
			Resource resource, Class<? extends ReputationBehaviour> type) throws Exception {
		ReputationImporterBehaviour behaviour = (
				ReputationImporterBehaviour) getResourceFromCache(
						resource, ReputationImporterBehaviour.class);
		if(behaviour != null) {			
			return behaviour;
		}
		behaviour = new ReputationImporterBehaviour();
		addResourceInstanceToCache(resource, behaviour);
		// Specific Attributes and Properties of ReputationImporter Class //		
		Property mapsMetric = ResourceFactory.createProperty(
				riNamespace + "mapsMetric");
		/* mapsMetric */
		StmtIterator stmtI1 = model.listStatements(resource, 
				mapsMetric, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate mapsMetric property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("mapsMetric property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				MetricMapping metMap = (MetricMapping) getResourceFromCache(
						statement.getObject().asResource(),MetricMapping.class);
				if(metMap == null) {
					metMap = getMetricMapping(model, 
							statement.getObject().asResource());					
				}
				behaviour.addMapsMetrics(metMap);				
			}
		}
		Property importsFrom = ResourceFactory.createProperty(
				riNamespace + "importsFrom");
		/* importsFrom */
		stmtI1 = model.listStatements(resource, 
				importsFrom, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate importsFrom property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("importsFrom property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				ImportationUnit impUni = (ImportationUnit) getResourceFromCache(
						statement.getObject().asResource(),ImportationUnit.class);
				if(impUni == null) {
					impUni = getImportationUnit(model, 
							statement.getObject().asResource());					
				}
				behaviour.addImportsFrom(impUni);
			}
		}
		return behaviour;
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
		        ReputationAlgorithmImplementation repImpInstance = 
		        	getReputationImporter(model,resource);
		        System.out.println(repImpInstance.toString("     ")); 
		    }
		} else {
		    System.out.println("No simple String riNamespace+" +
		    		"reputationImporter were found in the database");
		}
	}
	
	public ImportationUnit getImportationUnit(Model model, 
			Resource resource) throws Exception {
		ImportationUnit impUni = (ImportationUnit) getResourceFromCache(
				resource, ImportationUnit.class);
		if(impUni != null) {			
			return impUni;
		}
		impUni = new ImportationUnit();
		addResourceInstanceToCache(resource, impUni);
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
				Community community = (Community) getResourceFromCache(
						statement.getObject().asResource(), Community.class);
				if(community == null) {
					community = getCommunity(model, 
							statement.getObject().asResource());					
				}
				impUni.setImportedCommunity(community);
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
				Metric metric = (Metric) getResourceFromCache(
						statement.getObject().asResource(),Metric.class);
				if(metric == null) {
					metric = getMetric(model, 
							statement.getObject().asResource());					
				}
				impUni.setImportedMetric(metric);
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
				ReputationAlgorithmImplementation colAlg = (
						ReputationAlgorithmImplementation) getResourceFromCache(
						statement.getObject().asResource(),ReputationAlgorithmImplementation.class);
				if(colAlg == null) {
					colAlg = getReputationAlgorithm(model, 
							statement.getObject().asResource(),null);					
				}
				impUni.setCollectsReputationBy(colAlg);
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
				MetricTransformer metTra = (MetricTransformer) 
					getResourceFromCache(statement.getObject().asResource(),
							MetricTransformer.class);
				if(metTra == null) {
					metTra = getMetricTransformer(model, 
							statement.getObject().asResource(),null);					
				}
				impUni.setMetricTransformation(metTra);
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
				TrustBetweenCommunities truBet = (TrustBetweenCommunities) 
					getResourceFromCache(statement.getObject().asResource(),
							TrustBetweenCommunities.class);
				if(truBet == null) {
					truBet = getTrustBetweenCommunities(model, 
							statement.getObject().asResource(),null);					
				}
				impUni.setTrust(truBet);
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
		        System.out.println(impUniInstance.toString("     "));
		    }
		} else {
		    System.out.println("No simple String riNamespace+" +
		    		"ImportationUnit were found in the database");
		}
	}
	
	public MetricMapping getMetricMapping(Model model, 
			Resource resource) throws Exception {
		MetricMapping metMap = (MetricMapping) getResourceFromCache(
				resource, MetricMapping.class);
		if(metMap != null) {			
			return metMap;
		}
		metMap = new MetricMapping();
		addResourceInstanceToCache(resource, metMap);
		Property value = ResourceFactory.createProperty(
				riNamespace + "value");
		// value //
		StmtIterator stmtI1 = model.listStatements(resource, 
				value, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate value property //
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("value property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				metMap.setValue(statement.getObject().asLiteral().getDouble());
			}
		}		
		Property importedMetric = ResourceFactory.createProperty(
				riNamespace + "importedMetric");
		// importedMetric //
		stmtI1 = model.listStatements(resource, 
				importedMetric, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate importedMetric property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("importedMetric property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				Metric metric = (Metric) getResourceFromCache(
						statement.getObject().asResource(),Metric.class);
				if(metric == null) {
					metric = getMetric(model,statement.getObject().asResource());					
				}
				metMap.setImportedMetric(metric);
			}
		}
		Property resultMetric = ResourceFactory.createProperty(
				riNamespace + "resultMetric");
		// resultMetric //
		stmtI1 = model.listStatements(resource, 
				resultMetric, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate resultMetric property //
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("resultMetric property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				Metric metric = (Metric) getResourceFromCache(
						statement.getObject().asResource(),Metric.class);
				if(metric == null) {
					metric = getMetric(model,statement.getObject().asResource());					
				}
				metMap.setResultMetric(metric);				
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
		        System.out.println(metMapInstance.toString("     "));		        
		    }
		} else {
		    System.out.println("No simple String riNamespace+" +
		    		"MetricMappings were found in the database");
		}
	}
	
	public Metric getMetric(Model model, Resource resource) throws Exception {
		Metric metric = (Metric) getResourceFromCache(resource, Metric.class);
		if(metric != null) {			
			return metric;
		}
		metric = new Metric();
		addResourceInstanceToCache(resource, metric);
		// identifier //
		Property identifier = ResourceFactory.createProperty(
				riNamespace + "identifier");		
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
				Scale scale = (Scale) getResourceFromCache(
						statement.getObject().asResource(),Scale.class);
				if(scale == null) {
					scale = getScale(model,statement.getObject().asResource(),null);					
				}
				metric.setScale(scale);
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
				Dimension dimension = (Dimension) getResourceFromCache(
						statement.getObject().asResource(),Dimension.class);
				if(dimension == null) {
					dimension = getDimension(model, 
							statement.getObject().asResource());					
				}
				metric.setDimension(dimension);
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
		CategoricScale categoricScale = (CategoricScale) getResourceFromCache(
				resource, CategoricScale.class);
		if(categoricScale != null) {			
			return categoricScale;
		}
		categoricScale = (CategoricScale) getScale(model, resource, CategoricScale.class);
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
		NumericScale numericScale = (NumericScale) getResourceFromCache(
				resource, NumericScale.class);
		if(numericScale != null) {			
			return numericScale;
		}
		numericScale = (NumericScale) getScale(model, resource, NumericScale.class);
		// minimum //
		Property minimum = ResourceFactory.createProperty(
				riNamespace + "minimum");
		StmtIterator stmtI1 = model.listStatements(resource, 
				minimum, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate minimum property //
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("minimum property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				numericScale.setMinimum(statement.getObject().asLiteral().getDouble());
			}
		}
		Property maximum = ResourceFactory.createProperty(
				riNamespace + "maximum");
		// maximum //
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
		// step //
		stmtI1 = model.listStatements(resource, 
				step, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate step property //
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
	
	public Scale getScale(Model model, Resource resource, 
			Class<? extends Scale> type) throws Exception {
		if(type == null) {
			StmtIterator stmtI1 = model.listStatements(resource,
					RDF.type, (RDFNode)null);
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
		Scale scale = (Scale) getResourceFromCache(resource, type);
		if(scale != null) {			
			return scale;
		}		
		scale = type.newInstance();
		addResourceInstanceToCache(resource, scale);		
		// name //
		Property name = ResourceFactory.createProperty(riNamespace + "name");
		StmtIterator stmtI1 = model.listStatements(resource, 
				name, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate name property //
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
		//System.out.println(resource+" "+resource.getLocalName()
		//		+" "+resource.getNameSpace()+" "+
		//		resource.getURI()); //+" "+resource.getId());
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
	
	public EntityType getEntityType(Model model, 
			Resource resource) throws Exception {
		EntityType entityType = (EntityType) getResourceFromCache(
				resource, EntityType.class);
		if(entityType != null) {			
			return entityType;
		}		
		entityType = new EntityType();
		addResourceInstanceToCache(resource, entityType);
		// type //
		Property type = ResourceFactory.createProperty(riNamespace + "type");
		StmtIterator stmtI1 = model.listStatements(resource, 
				type, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate type property //
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("type property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				entityType.setType(statement.getObject().asLiteral().getString());
			}
		}
		return entityType;
	}
	
	public ReputationAlgorithmImplementation getReputationStep(
			Model model, Resource resource) throws Exception {
		ReputationAlgorithmImplementation reputationStep = 
			(ReputationAlgorithmImplementation) getResourceFromCache(
				resource, ReputationAlgorithmImplementation.class);
		if(reputationStep != null) {			
			return reputationStep;
		}
		List<Class<? extends ReputationBehaviour>> stepBehaviour =
			new ArrayList<Class<? extends ReputationBehaviour>>();
		stepBehaviour.add(ReputationStepBehaviour.class);
		reputationStep = getReputationAlgorithm(model, resource, stepBehaviour);
		ReputationStepBehaviour reputationStepBehaviour = null;
		for(ReputationBehaviour behaviour : reputationStep.getBehaviours()) {
			if(behaviour instanceof ReputationStepBehaviour) {
				reputationStepBehaviour = (ReputationStepBehaviour) behaviour;
			}
		}
		// stepIdentificator //
		Property stepIdentificator = ResourceFactory.createProperty(
				riNamespace + "stepIdentificator");		
		StmtIterator stmtI1 = model.listStatements(resource, 
				stepIdentificator, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate stepIdentificator property //
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("stepIdentificator property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				reputationStepBehaviour.setStepIdentificator(
						statement.getObject().asLiteral().getInt());		    	
			}
		}
		return reputationStep;
	}
	
	public ReputationBehaviour getReputationStepBehaviour(Model model, 
			Resource resource, Class<? extends ReputationBehaviour> type)
			throws Exception {
		ReputationStepBehaviour behaviour = (
				ReputationStepBehaviour) getResourceFromCache(
				resource, ReputationStepBehaviour.class);
		if(behaviour != null) {			
			return behaviour;
		}
		behaviour = new ReputationStepBehaviour();
		addResourceInstanceToCache(resource, behaviour);		
		
		// Specific Attributes and Properties of ReputationModule Class //
		// stepIdentificator //
		Property stepIdentificator = ResourceFactory.createProperty(
				riNamespace + "stepIdentificator");		
		StmtIterator stmtI1 = model.listStatements(resource, 
				stepIdentificator, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate stepIdentificator property //
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("stepIdentificator property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				behaviour.setStepIdentificator(
						statement.getObject().asLiteral().getInt());		    	
			}
		}
		return behaviour;
	}
	
	/*
	@SuppressWarnings("unchecked")
	public ReputationAlgorithm oldgetReputationAlgorithm(Model model, 
			Resource resource, Class<? extends ReputationAlgorithm> type) throws Exception {
		ReputationAlgorithm repAlg = null;
		if(type == null) {			
			StmtIterator stmtI1 = model.listStatements(resource, RDF.type, (RDFNode)null);
			Map<Resource,Method> childrenType = new HashMap<Resource,Method>();
			
			List<Class<? extends ReputationAlgorithm>> subclasses = 
				ReputationAlgorithm.listSubclasses();
			while(!subclasses.isEmpty()) {
				Class<?> subclass = subclasses.get(0);
				try {
					Method method = subclass.getMethod("listSubclasses");
					List<Class<? extends ReputationAlgorithm>> subsubclasses = 
						(List<Class<? extends ReputationAlgorithm>>) method.invoke(null);
					subclasses.addAll(subsubclasses);
				} catch (Exception e) {					
				}
				Method method = null;
				try {
					method = this.getClass().getMethod("get"+subclass.getSimpleName(),
							Model.class, Resource.class);
				} catch (Exception e) {
					try {
						method = this.getClass().getMethod("get"+subclass.getSimpleName(),
								Model.class, Resource.class, subclass.getClass());
					} catch (Exception e1) {
						throw new Exception("Not method get"+subclass.getSimpleName()+
								" with args: (Model, Resource) or (Model, Resource, Class)"
								+" for subclass "+subclass);
					}
				}
				childrenType.put(ResourceFactory.createResource(
						riNamespace + subclass.getSimpleName()),method);
				subclasses.remove(0);
				
			}
			while(stmtI1.hasNext()) {
				Statement typeStatement = stmtI1.nextStatement();
				for(Resource resourceType : childrenType.keySet()) {
					if(typeStatement.getObject().asResource().getURI().equals(
							resourceType.getURI())) {
						Method method = childrenType.get(resourceType);
						if(method.getParameterTypes().length == 2) {
							return (ReputationAlgorithm) method.invoke(this, model, resource);
						} else if(method.getParameterTypes().length == 3) {
							return (ReputationAlgorithm) method.invoke(this, model, resource, null);
						}
					}
				}								
			}
			if(type == null) {
				repAlg = new ReputationAlgorithm();		
			}			
		} else {
			repAlg = type.newInstance();
		}
		Property accesibility = ResourceFactory.
				createProperty(riNamespace + "accesibility");
		// accesibility //
		StmtIterator stmtI1 = model.listStatements(resource, 
				accesibility, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate accesibility property //
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("uriFormat property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				repAlg.addAccesibility(getAccessType(
						statement.getObject().asResource()));
			}
		}
		Property name = ResourceFactory.
				createProperty(riNamespace + "name");
		// name //
		stmtI1 = model.listStatements(resource, 
				name, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate name property //
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("name property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				repAlg.setName(statement.getObject().asLiteral().getString());		    	
			}
		}		
		Property resultCollectionType = ResourceFactory.
				createProperty(riNamespace + "resultCollectionType");
		// resultCollectionType //
		stmtI1 = model.listStatements(resource, 
				resultCollectionType, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate resultCollectionType property //
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("resultCollectionType property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				repAlg.setResultCollectionType(getCollectionType(
						statement.getObject().asResource()));		    	
			}
		}
		Property description = ResourceFactory.
				createProperty(dcNamespace + "description");
		// description //
		stmtI1 = model.listStatements(resource, 
				description, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// description name property //
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("name description of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				repAlg.setDescription(statement.getObject().asLiteral().getString());		    	
			}
		}
		Property entityType = ResourceFactory.
				createProperty(riNamespace + "entityType");
		// entityType //
		stmtI1 = model.listStatements(resource, 
				entityType, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate entityType property //
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
		// usesMetric //
		stmtI1 = model.listStatements(resource, 
				usesMetric, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate usesMetric property //
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
		// reputationSource //
		stmtI1 = model.listStatements(resource, 
				reputationSource, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate reputationSource property //
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
		// reputationResult //
		stmtI1 = model.listStatements(resource, 
				reputationResult, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate reputationResult property //
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
	*/
	
	public void addBehaviourToReputationAlgorithm(Model model, Resource resource, 
			Class<? extends ReputationBehaviour> subclass, 
			ReputationAlgorithmImplementation repAlg) throws Exception {
		ReputationBehaviour behaviour = (ReputationBehaviour
				) getResourceFromCache(resource, subclass);
		if(behaviour != null) {			
			repAlg.addBehaviour(behaviour);
			return;
		}
		Method method = null;						
		try {
			method = this.getClass().getMethod("get"+subclass.getSimpleName(),
					Model.class, Resource.class);
			behaviour = (ReputationBehaviour)method.invoke(this, 
					model, resource);
			repAlg.addBehaviour(behaviour);			
		} catch (NoSuchMethodException e) {
			try {
				//method = this.getClass().getMethod("get"+subclass.getSimpleName(),
				//		Model.class, Resource.class, subclass);
				method = this.getClass().getMethod("get"+subclass.getSimpleName(),
						Model.class, Resource.class, Class.class);
				behaviour = (ReputationBehaviour)method.invoke(
						this, model, resource, null);
				repAlg.addBehaviour(behaviour);				
			} catch (NoSuchMethodException e1) {
				e1.printStackTrace();
				for(Method methoz : this.getClass().getMethods()) {
					System.out.println(methoz);
				}
				throw new Exception("Not method get"+subclass.getSimpleName()+
						" with args: (Model, Resource) or (Model, Resource, Class)"
						+" for subclass "+subclass+" (method:"+method+")");
			} 
		}
	}
	
	public ReputationAlgorithmImplementation getReputationAlgorithm(Model model, 
			Resource resource, List<Class<? extends ReputationBehaviour>> types
			) throws Exception {
		ReputationAlgorithmImplementation repAlg = (
				ReputationAlgorithmImplementation) getResourceFromCache(
				resource, ReputationAlgorithmImplementation.class);
		if(repAlg != null) {			
			return repAlg;
		}		
		repAlg = new ReputationAlgorithmImplementation();
		addResourceInstanceToCache(resource, repAlg);
		if(types == null) {			
			StmtIterator stmtI1 = model.listStatements(resource, RDF.type, (RDFNode)null);						
			while(stmtI1.hasNext()) {
				Statement typeStatement = stmtI1.nextStatement();
				for(Resource resourceType : reputationAlgorithmSubclasses.keySet()) {
					if(typeStatement.getObject().asResource().getURI().equals(
							resourceType.getURI())) {
						addBehaviourToReputationAlgorithm(model, resource, 
								reputationAlgorithmSubclasses.get(resourceType),repAlg);
					}
				}								
			}
			for(Property property : reputationSubclassesProperties.keySet()) {
				stmtI1 = model.listStatements((Resource) null, property, resource);
				if(stmtI1.hasNext()) {
					addBehaviourToReputationAlgorithm(model, resource, 
							reputationSubclassesProperties.get(property),repAlg);					
				}
			}
			for(Property property : reputationSubclassSubjectProperties.keySet()) {
				stmtI1 = model.listStatements(resource, property, (RDFNode) null);
				if(stmtI1.hasNext()) {
					addBehaviourToReputationAlgorithm(model, resource, 
							reputationSubclassSubjectProperties.get(property),repAlg);
				}
			}			
		} else {
			for(Class<? extends ReputationBehaviour> type : types) {
				ReputationBehaviour behaviour = (
						ReputationBehaviour) getResourceFromCache(resource, type);
				if(behaviour == null) {
					behaviour = type.newInstance();
					addResourceInstanceToCache(resource, behaviour);
				}
				repAlg.addBehaviour(behaviour);				
			}
		}

		// Specific Attributes and Properties of ReputationalAlgorithm Class //
		setAttibutesAndProperties(model, resource, repAlg);
		return repAlg;
	}
	
	void setAttibutesAndProperties(Model model, Resource resource, 
			ReputationAlgorithmImplementation repAlg) throws Exception {
		Property accesibility = ResourceFactory.
				createProperty(riNamespace + "accesibility");
		// accesibility //
		StmtIterator stmtI1 = model.listStatements(resource, 
				accesibility, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate accesibility property //
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("uriFormat property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				repAlg.addAccesibility(getAccessType(
						statement.getObject().asResource()));
			}
		}
		Property name = ResourceFactory.
				createProperty(riNamespace + "name");
		// name //
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
				CollectionType collectionType = (CollectionType) getResourceFromCache(
						statement.getObject().asResource(),CollectionType.class);
				if(collectionType == null) {
					collectionType = getCollectionType(statement.getObject().asResource());				
				}
				repAlg.setResultCollectionType(collectionType);		    	
			}
		}
		Property description = ResourceFactory.
				createProperty(dcNamespace + "description");
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
				EntityType entityTypeIns = (EntityType) getResourceFromCache(
						statement.getObject().asResource(),EntityType.class);
				if(entityTypeIns == null) {
					entityTypeIns = getEntityType(model,
							statement.getObject().asResource());				
				}
				repAlg.addEntityType(entityTypeIns);		    	
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
				Metric metric = (Metric) getResourceFromCache(
						statement.getObject().asResource(),Metric.class);
				if(metric == null) {
					metric = getMetric(model,statement.getObject().asResource());				
				}
				repAlg.addUsesMetrics(metric);		    	
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
				ReputationValue reputationValue = (ReputationValue) getResourceFromCache(
						statement.getObject().asResource(),ReputationValue.class);
				if(reputationValue == null) {
					reputationValue = getReputationValue(model,
							statement.getObject().asResource());				
				}
				repAlg.addReputationSources(reputationValue);		    	
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
				ReputationValue reputationValue = (ReputationValue) getResourceFromCache(
						statement.getObject().asResource(),ReputationValue.class);
				if(reputationValue == null) {
					reputationValue = getReputationValue(model,
							statement.getObject().asResource());				
				}
				repAlg.addReputationResults(reputationValue);		    	
			}
		}
		// stepIdentifier //
		Property stepIdentificator = ResourceFactory.createProperty(
				riNamespace + "stepIdentifier");		
		stmtI1 = model.listStatements(resource, 
				stepIdentificator, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate stepIdentifier property //
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("stepIdentifier property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				repAlg.setStepIdentifier(
						statement.getObject().asLiteral().getInt());		    	
			}
		}
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
		        ReputationAlgorithmImplementation repAlgInstance = 
		        	getReputationAlgorithm(model,resource,null);
		        System.out.println(repAlgInstance.toString("     "));		        	        
		    }
		} else {
		    System.out.println("No simple String riNamespace+" +
		    		"ReputationAlgorithm were found in the database");
		}
	}
	
	public void printAllReputationAlgorithms(Model model) throws Exception {
		Set<Resource> resourcesCache = new HashSet<Resource>();
		Resource reputationAlgorithm = ResourceFactory.
				createResource(riNamespace + "ReputationAlgorithm");
		ResIterator iters = model.listSubjectsWithProperty(
				RDF.type,reputationAlgorithm);
		while (iters.hasNext()) {
		  	resourcesCache.add(iters.nextResource());
		}
		for(Resource subclass : reputationAlgorithmSubclasses.keySet()) {
			iters = model.listSubjectsWithProperty(RDF.type,subclass);
			while (iters.hasNext()) {
			   	resourcesCache.add(iters.nextResource());
			}
		}
		for(Property property : reputationSubclassesProperties.keySet()) {
			StmtIterator stmtI1 = model.listStatements((Resource) null, property, (RDFNode) null);
			while (stmtI1.hasNext()) {
				RDFNode object = stmtI1.nextStatement().getObject();
				if(object.isResource()) {
					resourcesCache.add(object.asResource());
				} else {
					throw new Exception("Property "+property.getURI()+" has the object "
							+object+" that is not a resource");
				}
			}			
		}
		if(!resourcesCache.isEmpty()) {		
			System.out.println("The database contains subjects" +
	    		" of class or superclass ReputationAlgorithm:");
			for(Resource resource : resourcesCache) {
		      	System.out.println("  " + resource.getLocalName());
			    ReputationAlgorithmImplementation repAlgInstance = 
			       	getReputationAlgorithm(model,resource,null);
			    System.out.println(repAlgInstance.toString("     "));
			}
		} else {
		    System.out.println("No simple String riNamespace+" +
		    		"ReputationAlgorithm were found in the database");
		}
	}
	
	public ReputationAlgorithmImplementation getReputationalAction(
			Model model, Resource resource) throws Exception {
		ReputationAlgorithmImplementation repAct = (
				ReputationAlgorithmImplementation) getResourceFromCache(
				resource, ReputationalActionBehaviour.class);
		if(repAct != null) {			
			return repAct;
		}		
		repAct = getCollectingAlgorithm(
				model, resource, ReputationalActionBehaviour.class);

		// Specific Attributes and Properties of ReputationalAction Class //
				
		return repAct;
	}
	
	public ReputationBehaviour getReputationalActionBehaviour(Model model, 
			Resource resource, Class<? extends ReputationBehaviour> type) throws Exception {
		ReputationalActionBehaviour behaviour = (
				ReputationalActionBehaviour) getResourceFromCache(
				resource, ReputationalActionBehaviour.class);
		if(behaviour != null) {			
			return behaviour;
		}		
		behaviour = new ReputationalActionBehaviour();
		addResourceInstanceToCache(resource, behaviour);
		
		// Specific Attributes and Properties of ReputationAction Class //
		
		return behaviour;
	}
	
	public ReputationAlgorithmImplementation getCollectingAlgorithm(Model model, 
			Resource resource, Class<? extends ReputationBehaviour> type) throws Exception {
		List<Class<? extends ReputationBehaviour>> types =
			new ArrayList<Class<? extends ReputationBehaviour>>();		
		if(type == null) {
			StmtIterator stmtI1 = model.listStatements(resource, RDF.type, (RDFNode)null);
			Resource reputationalAction = ResourceFactory.
					createResource(riNamespace + "ReputationalAction");
			Resource collectingSystem = ResourceFactory.
					createResource(riNamespace + "CollectingSystem");
			while(stmtI1.hasNext()) {
				Statement typeStatement = stmtI1.nextStatement();
				if(typeStatement.getObject().asResource().getURI().equals(
						reputationalAction.getURI())) {
					return getReputationalAction(model, resource);
				} else if (typeStatement.getObject().asResource().getURI().equals( 
						collectingSystem.getURI())) {
					return getCollectingSystem(model, resource);
				}				
			}
			if(type == null) {
				ReputationAlgorithmImplementation repAct = (
						ReputationAlgorithmImplementation) getResourceFromCache(
						resource, CollectingAlgorithmBehaviour.class);
				if(repAct != null) {			
					return repAct;
				}
				types.add(CollectingAlgorithmBehaviour.class);
			}
		} else {
			ReputationAlgorithmImplementation repAct = (
				ReputationAlgorithmImplementation) getResourceFromCache(resource, type);
			if(repAct != null) {			
				return repAct;
			}
			types.add(type);			
		}
		ReputationAlgorithmImplementation colAlg = 
			getReputationAlgorithm(model, resource, types);
		@SuppressWarnings("unused")
		CollectingAlgorithmBehaviour colAlgBeh = null;
		for(ReputationBehaviour behaviour : colAlg.getBehaviours()) {
			if(behaviour instanceof CollectingAlgorithmBehaviour) {
				colAlgBeh = (CollectingAlgorithmBehaviour) behaviour;
			}
		}
		// Specific Attributes and Properties of CollectingAlgorithm Class //
		
		return colAlg;
	}
	
	public ReputationBehaviour getCollectingAlgorithmBehaviour(Model model, 
			Resource resource, Class<? extends ReputationBehaviour> type) throws Exception {
		CollectingAlgorithmBehaviour behaviour = (CollectingAlgorithmBehaviour) 
			getResourceFromCache(resource, CollectingAlgorithmBehaviour.class);
		if(behaviour != null) {			
			return behaviour;
		}
		behaviour = new CollectingAlgorithmBehaviour();
		addResourceInstanceToCache(resource, behaviour);
		// Specific Attributes and Properties of CollectingAlgorithm Class //
		
		return behaviour;
	}
	
	public ReputationAlgorithmImplementation getReputationModule(Model model, 
			Resource resource) throws Exception {
		ReputationAlgorithmImplementation reputationModule = (ReputationAlgorithmImplementation) 
			getResourceFromCache(resource, ReputationAlgorithmImplementation.class);
		if(reputationModule != null) {			
			return reputationModule;
		}		
		List<Class<? extends ReputationBehaviour>> types =
			new ArrayList<Class<? extends ReputationBehaviour>>();
		types.add(ReputationModuleBehaviour.class);
		reputationModule = getReputationAlgorithm(model, resource, types);
		ReputationModuleBehaviour repModuleBeh = null;
		for(ReputationBehaviour behaviour : reputationModule.getBehaviours()) {
			if(behaviour instanceof ReputationModuleBehaviour) {
				repModuleBeh = (ReputationModuleBehaviour) behaviour;
			}
		}		
		// Specific Attributes and Properties of ReputationModule Class //		
		// obtainsReputationsBy //
		Property obtainsReputationsBy = ResourceFactory.createProperty(
				riNamespace + "obtainsReputationsBy");
		StmtIterator stmtI1 = model.listStatements(resource, 
				obtainsReputationsBy, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate obtainsReputationsBy property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("obtainsReputationsBy property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				ReputationAlgorithmImplementation repAlg = 
					(ReputationAlgorithmImplementation) getResourceFromCache(
					statement.getObject().asResource(),ReputationAlgorithmImplementation.class);
				if(repAlg == null) {
					repAlg = getReputationAlgorithm(model,
							statement.getObject().asResource(), null);				
				}
				repModuleBeh.addObtainsReputationsBy(repAlg);		    	
			}
		}
		return reputationModule;
	}
	
	public ReputationBehaviour getReputationModuleBehaviour(Model model, 
			Resource resource, Class<? extends ReputationBehaviour> type) throws Exception {
		ReputationModuleBehaviour behaviour = (ReputationModuleBehaviour) 
			getResourceFromCache(resource, ReputationModuleBehaviour.class);
		if(behaviour != null) {			
			return behaviour;
		}		
		behaviour = new ReputationModuleBehaviour();
		addResourceInstanceToCache(resource, behaviour);
		// Specific Attributes and Properties of ReputationModule Class //
		// obtainsReputationsBy //
		Property obtainsReputationsBy = ResourceFactory.createProperty(
				riNamespace + "obtainsReputationsBy");
		StmtIterator stmtI1 = model.listStatements(resource, 
				obtainsReputationsBy, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate obtainsReputationsBy property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("obtainsReputationsBy property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				ReputationAlgorithmImplementation repAlg = 
					(ReputationAlgorithmImplementation) getResourceFromCache(
					statement.getObject().asResource(),ReputationAlgorithmImplementation.class);
				if(repAlg == null) {
					repAlg = getReputationAlgorithm(model,
							statement.getObject().asResource(), null);				
				}
				behaviour.addObtainsReputationsBy(repAlg);				
			}
		}
		return behaviour;
	}
	
	public ReputationAlgorithmImplementation getReputationModel(Model model,
			Resource resource) throws Exception {
		ReputationAlgorithmImplementation repMod = (ReputationAlgorithmImplementation) 
			getResourceFromCache(resource, ReputationAlgorithmImplementation.class);
		if(repMod != null) {			
			return repMod;
		}		
		List<Class<? extends ReputationBehaviour>> types =
			new ArrayList<Class<? extends ReputationBehaviour>>();
		types.add(ReputationModelBehaviour.class);
		repMod = getReputationAlgorithm(model, resource, types);		
		ReputationModelBehaviour repModBeh = null;
		for(ReputationBehaviour behaviour : repMod.getBehaviours()) {
			if(behaviour instanceof ReputationModelBehaviour) {
				repModBeh = (ReputationModelBehaviour) behaviour;
			}
		}
		// reputationModule //
		Property reputationModule = ResourceFactory.createProperty(
				riNamespace + "reputationModule");
		StmtIterator stmtI1 = model.listStatements(resource, 
				reputationModule, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate reputationModel property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("reputationModule property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				ReputationAlgorithmImplementation repAlg = 
					(ReputationAlgorithmImplementation) getResourceFromCache(
					statement.getObject().asResource(),ReputationAlgorithmImplementation.class);
				if(repAlg == null) {
					repAlg = getReputationAlgorithm(model,
							statement.getObject().asResource(), null);				
				}
				repModBeh.addReputationModules(repAlg);		    	
			}
		}
		return repMod;
	}
	
	public ReputationBehaviour getReputationModelBehaviour(Model model, 
			Resource resource, Class<? extends ReputationBehaviour> type) throws Exception {
		ReputationModelBehaviour behaviour = (ReputationModelBehaviour) 
			getResourceFromCache(resource, ReputationModelBehaviour.class);
		if(behaviour != null) {			
			return behaviour;
		}
		behaviour = new ReputationModelBehaviour();
		addResourceInstanceToCache(resource, behaviour);
		// Specific Attributes and Properties of ReputationModel Class //
		// reputationModule //
		Property reputationModule = ResourceFactory.createProperty(
				riNamespace + "reputationModule");
		StmtIterator stmtI1 = model.listStatements(resource, 
				reputationModule, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate reputationModule property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("reputationModule property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				ReputationAlgorithmImplementation repAlg = 
					(ReputationAlgorithmImplementation) getResourceFromCache(
					statement.getObject().asResource(),ReputationAlgorithmImplementation.class);
				if(repAlg == null) {
					repAlg = getReputationAlgorithm(model,
							statement.getObject().asResource(), null);				
				}
				behaviour.addReputationModules(repAlg);		    	
			}
		}
		return behaviour;
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
		        System.out.println("  " + resource.getURI());
		        ReputationAlgorithmImplementation repModInstance = 
		        	getReputationModel(model,resource);
		        System.out.println(repModInstance.toString("     "));		        	        
		    }
		} else {
		    System.out.println("No simple String riNamespace+" +
		    		"ReputationModel were found in the database");
		}
	}
	
	public ReputationAlgorithmImplementation getCollectingSystem(
			Model model, Resource resource) throws Exception {
		ReputationAlgorithmImplementation colSys = (ReputationAlgorithmImplementation) 
			getResourceFromCache(resource, ReputationAlgorithmImplementation.class);
		if(colSys != null) {			
			return colSys;
		}
		colSys = getCollectingAlgorithm(model, resource, CollectingSystemBehaviour.class);
		CollectingSystemBehaviour colSysBeh = null;
		for(ReputationBehaviour behaviour : colSys.getBehaviours()) {
			if(behaviour instanceof CollectingAlgorithmBehaviour) {
				for(ReputationBehaviour subBehaviour : behaviour.getBehaviours()) {
					if(subBehaviour instanceof CollectingSystemBehaviour) {
						colSysBeh = (CollectingSystemBehaviour) subBehaviour;
					}
				}
			}
		}

		// Specific Attributes and Properties of CollectingSystem Class //
		
		// uriFormat //
		Property uriFormat = ResourceFactory.createProperty(
				riNamespace + "uriFormat");		
		StmtIterator stmtI1 = model.listStatements(resource, 
				uriFormat, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate uriFormat property */
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("uriFormat property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				colSysBeh.setUriFormat(new URI(
						statement.getObject().asLiteral().getString()));
			}
		}		
		return colSys;
	}
	
	public ReputationBehaviour getCollectingSystemBehaviour(Model model, 
			Resource resource, Class<? extends ReputationBehaviour> type) throws Exception {
		CollectingSystemBehaviour behaviour = (CollectingSystemBehaviour) 
			getResourceFromCache(resource, CollectingSystemBehaviour.class);
		if(behaviour != null) {			
			return behaviour;
		}
		behaviour = new CollectingSystemBehaviour();
		addResourceInstanceToCache(resource, behaviour);
		
		// Specific Attributes and Properties of CollectingAlgorithm Class //
		// uriFormat //
		Property uriFormat = ResourceFactory.createProperty(
				riNamespace + "uriFormat");
		StmtIterator stmtI1 = model.listStatements(resource, 
				uriFormat, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate uriFormat property */
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("uriFormat property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				behaviour.setUriFormat(new URI(
						statement.getObject().asLiteral().getString()));
			}
		}
		return behaviour;
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
		        ReputationAlgorithmImplementation colSysInstance = 
		        	getCollectingSystem(model,resource);
		        System.out.println(colSysInstance.toString("     "));
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
	
	@SuppressWarnings("unchecked")
	public MetricTransformer getMetricTransformer(
			Model model, Resource resource, 
			Class<? extends MetricTransformer> type) throws Exception {
		MetricTransformer metTra = (MetricTransformer) 
			getResourceFromCache(resource, MetricTransformer.class);
		if(metTra != null) {			
			return metTra;
		}
		if(type == null) {
			StmtIterator stmtI1 = model.listStatements(resource, RDF.type, (RDFNode)null);
			Map<Resource,Method> childrenType = new HashMap<Resource,Method>();
			List<Class<? extends MetricTransformer>> subclasses = 
				NumericTransformer.listSubclasses();
			while(!subclasses.isEmpty()) {
				Class<?> subclass = subclasses.get(0);
				try {
					Method method = subclass.getMethod("listSubclasses");
					List<Class<? extends MetricTransformer>> subsubclasses = 
						(List<Class<? extends MetricTransformer>>) method.invoke(null);
					subclasses.addAll(subsubclasses);
				} catch (Exception e) {					
				}
				childrenType.put(ResourceFactory.createResource(
						riNamespace + subclass.getSimpleName()), 
						this.getClass().getMethod("get"+subclass.getSimpleName(),
								Model.class, Resource.class));
				subclasses.remove(0);
			}
			while(stmtI1.hasNext()) {
				Statement typeStatement = stmtI1.nextStatement();
				for(Resource resourceType : childrenType.keySet()) {
					if(typeStatement.getObject().asResource().getURI().equals(
							resourceType.getURI())) {
						return (MetricTransformer) childrenType.get(
								resourceType).invoke(this, model, resource);
					}
				}								
			}
			if(type == null) {
				throw new Exception("Resource "+resource+" cannot be defined" +
					" as a direct instance of MetricMapping. You must not use" +
					" abstract subclasses");		
			}
		} else {
			
		}
		MetricTransformerInstances instances = 
			getMetricTransformer(model,resource);
		try {
			Constructor<?> constructor = type.getConstructor(Metric.class, 
				Metric.class, List.class);
			metTra = (MetricTransformer) constructor.newInstance(
					instances.sourceMetric,
					instances.destinationMetric,
					instances.correlationBetweenMetrics);
			addResourceInstanceToCache(resource, metTra);
			metTra.setDescription(instances.description);
		} catch(Exception e) {
			throw new Exception("Not constructor with args: (Metric, " +
					"Metric, List) exist for type:"+type);
		}
		return metTra;
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
				sourceInstance = (Metric) getResourceFromCache(
					statement.getObject().asResource(),Metric.class);
				if(sourceInstance == null) {
					sourceInstance = getMetric(model, statement.getObject().asResource());					
				}				    			    	
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
				destinationInstance = (Metric) getResourceFromCache(
						statement.getObject().asResource(),Metric.class);
				if(destinationInstance == null) {
					destinationInstance = getMetric(model, statement.getObject().asResource());					
				}					    				    	
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
				correlationInstance = (DimensionCorrelation) getResourceFromCache(
						statement.getObject().asResource(),DimensionCorrelation.class);
				if(correlationInstance == null) {
					correlationInstance = getDimensionCorrelation(model,
							statement.getObject().asResource());					
				}
				//TODO: validate dimensions are the same of source and destination metrics
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
				//TODO: validate scales are the same of source and destination metrics
				correlationsInstance.add(
						Double.parseDouble(statement.getObject().toString()));		    	
			}
		}		
		
		Property description = ResourceFactory.
				createProperty(dcNamespace + "description");
		// description //
		String descriptionInstance = null;
		stmtI1 = model.listStatements(resource, 
				description, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate description property //
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
		ExponentialNumericTransformer expNum = (ExponentialNumericTransformer) 
			getResourceFromCache(resource, ExponentialNumericTransformer.class);
		if(expNum != null) {			
			return expNum;
		}		
		MetricTransformerInstances metTraInstance = 
			getMetricTransformer(model, resource);
		Double baseInstance = getBaseObject(model, resource);		
		if(baseInstance == null) {
			expNum = new ExponentialNumericTransformer(
					metTraInstance.sourceMetric, metTraInstance.destinationMetric, 
					metTraInstance.correlationBetweenMetrics);			
		} else {
			expNum = new ExponentialNumericTransformer(
					metTraInstance.sourceMetric, metTraInstance.destinationMetric, 
					metTraInstance.correlationBetweenMetrics, baseInstance);
		}
		addResourceInstanceToCache(resource, expNum);
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
		SqrtNumericTransformer sqrtNum = (SqrtNumericTransformer) 
			getResourceFromCache(resource, SqrtNumericTransformer.class);
		if(sqrtNum != null) {			
			return sqrtNum;
		}
		MetricTransformerInstances metTraInstance = 
			getMetricTransformer(model, resource);
		sqrtNum = new SqrtNumericTransformer(
					metTraInstance.sourceMetric, metTraInstance.destinationMetric, 
					metTraInstance.correlationBetweenMetrics);
		addResourceInstanceToCache(resource, sqrtNum);
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
		LinealNumericTransformer linNum = (LinealNumericTransformer) 
			getResourceFromCache(resource, LinealNumericTransformer.class);
		if(linNum != null) {			
			return linNum;
		}
		MetricTransformerInstances metTraInstance = 
			getMetricTransformer(model, resource);
		linNum = new LinealNumericTransformer(
					metTraInstance.sourceMetric, metTraInstance.destinationMetric, 
					metTraInstance.correlationBetweenMetrics);		
		addResourceInstanceToCache(resource, linNum);
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
		// base //
		StmtIterator stmtI1 = model.listStatements(resource, 
				base, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate base property //
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
		LogaritmicNumericTransformer logNum = (LogaritmicNumericTransformer) 
			getResourceFromCache(resource, LogaritmicNumericTransformer.class);
		if(logNum != null) {			
			return logNum;
		}
		MetricTransformerInstances metTraInstance = 
			getMetricTransformer(model, resource);
		Double baseInstance = getBaseObject(model, resource);
		if(baseInstance == null) {
			logNum = new LogaritmicNumericTransformer(
					metTraInstance.sourceMetric, metTraInstance.destinationMetric, 
					metTraInstance.correlationBetweenMetrics);
		} else {
			logNum = new LogaritmicNumericTransformer(
					metTraInstance.sourceMetric, metTraInstance.destinationMetric, 
					metTraInstance.correlationBetweenMetrics, baseInstance);
		}
		addResourceInstanceToCache(resource, logNum);
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
		DimensionCorrelation dimCor = (DimensionCorrelation) 
			getResourceFromCache(resource, DimensionCorrelation.class);
		if(dimCor != null) {			
			return dimCor;
		}		
		dimCor = new DimensionCorrelation();
		addResourceInstanceToCache(resource, dimCor);
		Property sourceDimension = ResourceFactory.
				createProperty(riNamespace + "sourceDimension");		
		// sourceDimension //
        StmtIterator stmtI1 = model.listStatements(resource, 
        		sourceDimension, (RDFNode)null);
		while(stmtI1.hasNext()) {
	    	Statement statement = stmtI1.nextStatement();
	    	// validate sourceDimension property //
	    	if(!statement.getObject().isResource()) {	    		
	    		throw new Exception("sourceDimension property of resource:"+
	    				resource.getURI()+" is not a resource");
	    	} else {
	    		Dimension dimension = (Dimension) getResourceFromCache(
	    				statement.getObject().asResource(), Dimension.class);
				if(dimension == null) {			
					dimension = getDimension(model, statement.getObject().asResource());
				}
	    		dimCor.setSourceDimension(dimension);		    	
	    	}
	    }
		Property targetDimension = ResourceFactory.
				createProperty(riNamespace + "targetDimension");
		// targetDimension //
        stmtI1 = model.listStatements(resource, 
        		targetDimension, (RDFNode)null);
		while(stmtI1.hasNext()) {
	    	Statement statement = stmtI1.nextStatement();
	    	// validate targetDimension property //
	    	if(!statement.getObject().isResource()) {	    		
	    		throw new Exception("targetDimension property of resource:"+
	    				resource.getURI()+" is not a resource");
	    	} else {
	    		Dimension dimension = (Dimension) getResourceFromCache(
	    				statement.getObject().asResource(), Dimension.class);
				if(dimension == null) {			
					dimension = getDimension(model, statement.getObject().asResource());
				}
	    		dimCor.setTargetDimension(dimension);		    	
	    	}
	    }
		Property correlationValue = ResourceFactory.
				createProperty(riNamespace + "correlationValue");		
		// correlationValue //
		stmtI1 = model.listStatements(resource, 
				correlationValue, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate correlationValue property //
			if(!statement.getObject().isLiteral()) {	    		
				throw new Exception("correlationValue property of resource:"+
						resource.getURI()+" is not a literal");
			} else {
				dimCor.setCorrelationValue(
						statement.getObject().asLiteral().getDouble());		    	
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
			Model model, Resource resource, 
			Class<? extends TrustBetweenCommunities> type) throws Exception {
		TrustBetweenCommunities truBet = (TrustBetweenCommunities) 
			getResourceFromCache(resource, type);
		if(truBet != null) {			
			return truBet;
		}
		if(type != null) {
			truBet = type.newInstance();
			addResourceInstanceToCache(resource, truBet);
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
				addResourceInstanceToCache(resource, truBet);
			}
		}
		Property trustProvidedBy = ResourceFactory.
				createProperty(riNamespace + "trustProvidedBy");		
		// trustProvidedBy //
		StmtIterator stmtI1 = model.listStatements(resource, 
				trustProvidedBy, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate trustProvidedBy property //
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("trustProvidedBy property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				TrustBetweenCommunities truBetInside = (TrustBetweenCommunities) 
						getResourceFromCache(statement.getObject().asResource(),
								TrustBetweenCommunities.class);
				if(truBetInside == null) {			
					truBetInside = getTrustBetweenCommunities(
							model, statement.getObject().asResource(), null);
				}
	    		truBet.addTrustProvidedBy(truBetInside);		    	
			}
		}
		if(truBet.getValue() == null) {
			Property value = ResourceFactory.
					createProperty(riNamespace + "value");			
			// value //
			stmtI1 = model.listStatements(resource, 
					value, (RDFNode)null);
			while(stmtI1.hasNext()) {
				Statement statement = stmtI1.nextStatement();
				// validate value property //
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
			getResourceFromCache(resource, FixedCommunitiesTrust.class);
		if(fixCom != null) {			
			return fixCom;
		}
		fixCom = (FixedCommunitiesTrust) getTrustBetweenCommunities(
				model, resource, FixedCommunitiesTrust.class);
		Property communityScorer = ResourceFactory.
				createProperty(riNamespace + "communityScorer");		
		// communityScorer //
        StmtIterator stmtI1 = model.listStatements(resource, 
        		communityScorer, (RDFNode)null);
		while(stmtI1.hasNext()) {
	    	Statement statement = stmtI1.nextStatement();
	    	// validate originatingCategory property //
	    	if(!statement.getObject().isResource()) {	    		
	    		throw new Exception("communityScorer property of resource:"+
	    				resource.getURI()+" is not a resource");
	    	} else {
	    		Community community = (Community) getResourceFromCache(
	    				statement.getObject().asResource(), Community.class);
				if(community == null) {			
					//Community community = getCommunity(
					//		model, statement.getObject().asResource());
		    		community = getLimitedCommunity(
		    				model, statement.getObject().asResource());
				}
				fixCom.setCommunityScorer(community);		    	
	    	}
	    }
		Property communityScored = ResourceFactory.
				createProperty(riNamespace + "communityScored");		
		// communityScored //
		stmtI1 = model.listStatements(resource, 
				communityScored, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate communityScored property */
			if(!statement.getObject().isResource()) {	    		
				throw new Exception("communityScored property of resource:"+
						resource.getURI()+" is not a resource");
			} else {
				Community community = (Community) getResourceFromCache(
						statement.getObject().asResource(), Community.class);
				if(community == null) {			
					//Community community = getCommunity(
					//		model, statement.getObject().asResource());
		    		community = getLimitedCommunity(
		    				model, statement.getObject().asResource());
				}
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
	
	public CategoryMatching getCategoryMatching(Model model, 
			Resource resource) throws Exception {
		CategoryMatching catMat = (CategoryMatching) 
			getResourceFromCache(resource, CategoryMatching.class);
		if(catMat != null) {			
			return catMat;
		}
		catMat = (CategoryMatching) getTrustBetweenCommunities(
				model, resource, CategoryMatching.class);;
		// originatingCategory //
		Property originatingCategory = ResourceFactory.
				createProperty(riNamespace + "originatingCategory");		
		StmtIterator stmtI1 = model.listStatements(resource, 
        		originatingCategory, (RDFNode)null);
		while(stmtI1.hasNext()) {
	    	Statement statement = stmtI1.nextStatement();
	    	/* validate originatingCategory property */
	    	if(!statement.getObject().isResource()) {	    		
	    		throw new Exception("originatingCategory property of resource:"+
	    				resource.getURI()+" is not a resource");
	    	} else {
	    		Category category = (Category) getResourceFromCache(
	    				statement.getObject().asResource(), Category.class);
				if(category == null) {			
					category = getCategory(model, statement.getObject().asResource());
				}
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
				Category category = (Category) getResourceFromCache(
						statement.getObject().asResource(), Category.class);
				if(category == null) {			
					category = getCategory(model, statement.getObject().asResource());
				}
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
		Category category = (Category) 
			getResourceFromCache(resource, Category.class);
		if(category != null) {			
			return category;
		}
		// name //
		Property name = ResourceFactory.createProperty(riNamespace + "name");	
        StmtIterator stmtI1 = model.listStatements(resource, name, (RDFNode)null);
		while(stmtI1.hasNext()) {
	    	Statement statement = stmtI1.nextStatement();
	    	// validate name property //
	    	if(!statement.getObject().isLiteral()) {	    		
	    		throw new Exception("name property of resource:"+
	    				resource.getURI()+" is not a literal");
	    	}
	    	category = new Category(statement.getObject().asLiteral().getString());
	    	addResourceInstanceToCache(resource, category); 
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
		Dimension dimension = (Dimension) getResourceFromCache(resource, Dimension.class);
		if(dimension != null) {			
			return dimension;
		}
		Property name = ResourceFactory.createProperty(riNamespace + "name");		
		// name //
        StmtIterator stmtI1 = model.listStatements(resource, name, (RDFNode)null);
		while(stmtI1.hasNext()) {
	    	Statement statement = stmtI1.nextStatement();
	    	/* validate name property */
	    	if(!statement.getObject().isLiteral()) {	    		
	    		throw new Exception("name property of resource:"+
	    				resource.getURI()+" is not a literal");
	    	}
	    	dimension = new Dimension(statement.getObject().asLiteral().getString());
	    	addResourceInstanceToCache(resource, dimension);	    	    	
	    }
		Property description = ResourceFactory.createProperty(riNamespace + "description");		
		// description //
        stmtI1 = model.listStatements(resource, description, (RDFNode)null);
		while(stmtI1.hasNext()) {
	    	Statement statement = stmtI1.nextStatement();
	    	// validate description property //
	    	if(!statement.getObject().isLiteral()) {
	    		throw new Exception("description property of resource:"+
	    				resource.getURI()+" is not a literal");
	    	}
	    	if(dimension != null) {
	    		dimension.setDescription(statement.getObject().asLiteral().getString());
	    	}	    	
	    }
		return dimension;
	}
	
}
