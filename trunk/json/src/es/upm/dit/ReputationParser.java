package es.upm.dit;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
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
import cross.reputation.model.CollectingAlgorithmBehaviour;
import cross.reputation.model.CollectingSystemBehaviour;
import cross.reputation.model.CollectionType;
import cross.reputation.model.Community;
import cross.reputation.model.Dimension;
import cross.reputation.model.DimensionCorrelation;
import cross.reputation.model.DiscardException;
import cross.reputation.model.Entity;
import cross.reputation.model.EntityIdentifier;
import cross.reputation.model.EntityType;
import cross.reputation.model.ExponentialNumericTransformer;
import cross.reputation.model.FixedCommunitiesTrust;
import cross.reputation.model.ImportationUnit;
import cross.reputation.model.LinealNumericTransformer;
import cross.reputation.model.LogaritmicNumericTransformer;
import cross.reputation.model.Metric;
import cross.reputation.model.MetricMapping;
import cross.reputation.model.MetricTransformer;
import cross.reputation.model.ModelException;
import cross.reputation.model.ReputationAlgorithmImplementation;
import cross.reputation.model.ReputationBehaviour;
import cross.reputation.model.ReputationEvaluation;
import cross.reputation.model.ReputationImporterBehaviour;
import cross.reputation.model.ReputationModelBehaviour;
import cross.reputation.model.NumericScale;
import cross.reputation.model.NumericTransformer;
import cross.reputation.model.ReputationModuleBehaviour;
import cross.reputation.model.ReputationObject;
import cross.reputation.model.ReputationStepBehaviour;
import cross.reputation.model.ReputationValue;
import cross.reputation.model.ReputationalActionBehaviour;
import cross.reputation.model.Scale;
import cross.reputation.model.ScaleCorrelation;
import cross.reputation.model.SqrtNumericTransformer;
import cross.reputation.model.TrustBetweenCommunities;
import cross.reputation.model.ValidateModel;

public class ReputationParser {
	String riNamespace = "http://purl.org/reputationImport/0.1/";
	String dcNamespace = "http://purl.org/dc/elements/1.1/";
	String foafNamespace = "http://xmlns.com/foaf/0.1/";
	private String base = "";
	private Map<Resource,Class<? extends ReputationBehaviour>> reputationAlgorithmSubclasses;
	private Map<Property,Class<? extends ReputationBehaviour>> reputationSubclassesProperties;
	private Map<Property,Class<? extends ReputationBehaviour>> reputationSubclassSubjectProperties;
	private Set<Resource> foafAgentClasses;
	private Model model;
	private Map<Resource,Set<Object>> cache = new HashMap<Resource,Set<Object>>();
	
	static public void main(String[] args) throws Exception {
		//String modelPath = "dir/model3.rdf";
		String modelPath = "dir/modelWithEntities.rdf";
		ReputationParser parser = new ReputationParser(modelPath);
		parser.getCrossReputationGlobalModelFromRDF();
	}
	
	public ReputationParser() throws Exception {
		reputationAlgorithmSubclasses();
		foafAgentClasses();
	}
	
	public ReputationParser(String modelPath) throws Exception {
		base = Util.getXmlBaseFromFile(modelPath);		
		setParametersFromModel(modelPath);
		reputationAlgorithmSubclasses();
		foafAgentClasses();
	}
	
	private void setParametersFromModel(String modelPath) throws Exception {
		// create an empty model
        model = ModelFactory.createOntologyModel(); // createDefaultModel();

        // use the FileManager to find the input file
        //FileManager.get().getFromCache(filenameOrURI)
        File modelFile = new File(modelPath);
        InputStream in = FileManager.get().open(modelPath);
        if (in == null) {
            throw new IllegalArgumentException(
                   "File: " + modelPath + " not found");
        }

        //if(model.getNsPrefixURI(""))
        // read the RDF/XML file
        if(es.upm.dit.Property.getBASE_URI_MODE() == 
        		es.upm.dit.Property.BASE_OR_ABSOLUTE_PATH){
        	model.read("file:"+modelFile.getAbsolutePath());
        } else if(es.upm.dit.Property.getBASE_URI_MODE() == 
        		es.upm.dit.Property.BASE_OR_FILE_PATH){
        	model.read("file:"+modelPath);
        } else {
        	model.read(in, "", null);
        }
               
        // create the reasoner factory and the reasoner
		Resource conf = model.createResource();
		//conf.addProperty( ReasonerVocabulary.PROPtraceOn, "true" );
		RDFSRuleReasoner reasoner = (RDFSRuleReasoner) RDFSRuleReasonerFactory.theInstance().create(conf);
		// Create inference model
		InfModel infModel = ModelFactory.createInfModel(reasoner, model);
		
		model = infModel;
        
		//RDFReader reader = model.getReader("RDF/XML");
        ModelException.sendMessage(ModelException.INFO, 
        		"Base Namespace:"+model.getNsPrefixURI(""));
                
        if(model.getNsPrefixURI("ri") != null) {
        	riNamespace = model.getNsPrefixURI("ri");
        	ModelException.sendMessage(ModelException.INFO, 
            		"ri namespace:"+riNamespace);
        }
        if(model.getNsPrefixURI("dc") != null) {
        	dcNamespace = model.getNsPrefixURI("dc");
        	ModelException.sendMessage(ModelException.INFO, 
            		"dc namespace:"+dcNamespace);
        }
        if(model.getNsPrefixURI("foaf") != null) {
        	foafNamespace = model.getNsPrefixURI("foaf");
        	ModelException.sendMessage(ModelException.INFO, 
            		"foafNamespace namespace:"+foafNamespace);
        }
	}	
	
	/*public void getCrossReputationGlobalModelFromRDF(String inputFileName){        
        // create an empty model
        model = ModelFactory.createOntologyModel(); // createDefaultModel();

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
        if(model.getNsPrefixURI("foaf") != null) {
        	foafNamespace = model.getNsPrefixURI("foaf");
            System.out.println("foafNamespace namespace:"+foafNamespace);
        }
        getCrossReputationGlobalModelFromRDF();
	}*/
	
    public void getCrossReputationGlobalModelFromRDF() {        	    
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
        	//printDimensions(model);
        	//printCategories(model);
        	//printCategoryMatchings(model);
        	//printFixedCommunitiesTrust(model);
        	//printTrustBetweenCommunities(model);
        	//printCollectingSystems(model);
        	//printDimensionCorrelations(model);
        	//printScaleCorrelations(model);
        	//printScales(model);
        	//printAllScales(model);
        	//printNumericScales(model);
        	//printMetrics(model);
        	//printLogaritmicNumericTransformers(model);
        	//printLinealNumericTransformers(model);
        	//printSqrtNumericTransformers(model);
        	//printMetricMappings(model);
        	//printImportationUnits(model);
        	//printReputationImporters(model);
        	//printReputationModels(model);
        	//printAllReputationAlgorithms(model);
        	printCommunities(model);
			printEntities(model);
			//printReputationValues(model);
			//printReputationObjects(model);
		} catch (Exception e) {
			e.printStackTrace();
		}               
	}
	
	private void foafAgentClasses() {
		if(foafAgentClasses == null) {
			foafAgentClasses = new HashSet<Resource>();
		}
		foafAgentClasses.add(ResourceFactory.createResource(foafNamespace + "Agent"));
		foafAgentClasses.add(ResourceFactory.createResource(foafNamespace + "Person"));
		foafAgentClasses.add(ResourceFactory.createResource(foafNamespace + "Organization"));
		foafAgentClasses.add(ResourceFactory.createResource(foafNamespace + "Group"));
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
		reputationSubclassSubjectProperties.put(ResourceFactory.
				createProperty(riNamespace + "uriFormat"),
				CollectingSystemBehaviour.class);
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
			community.setResource(resource);
		} else {
			return community;
		}
		// Limited Attributes and Properties of Community Class //
		// identifier //
		Property identifier = ResourceFactory.createProperty(
				riNamespace + "identifier");
		StmtIterator stmtI1 = model.listStatements(resource, 
				identifier, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate identifier property //
			if(!statement.getObject().isLiteral()) {	    		
				if(!ModelException.throwException(ModelException.COMMUNITY,
						"identifier property of Community resource:"+
						resource.getURI()+" is not a literal")) {
					return null;
				}
			} else {
				community.setName(statement.getObject().asLiteral().getString());
			}
		}
		return community;
	}
	
	public EntityIdentifier getFoafOnlineAccount(Model model, 
			Resource resource) throws Exception {
		EntityIdentifier onlAcc = (EntityIdentifier) getResourceFromCache(
				resource, EntityIdentifier.class);
		if(onlAcc != null) {
			return onlAcc;					
		}
		onlAcc = new EntityIdentifier();
		onlAcc.setResource(resource);
		addResourceInstanceToCache(resource, onlAcc);
		// Specific Attributes and Properties of Foaf:OnlineAccount Class //
		// foafAccountName //
		Property foafAccountName = ResourceFactory.createProperty(
				foafNamespace + "accountName");
		StmtIterator stmtI1 = model.listStatements(resource, 
				foafAccountName, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate name property //
			if(!statement.getObject().isLiteral()) {	    		
				if(!ModelException.throwException(ModelException.ONLINEACCOUNT,
						"name property of OnlineAccount resource:"+
						resource.getURI()+" is not a literal")) {
					return null;
				}
			} else {
				onlAcc.setName(statement.getObject().asLiteral().getString());
			}
		}
		// foafAccountProfilePage //
		Property foafAccountProfilePage = ResourceFactory.createProperty(
				foafNamespace + "accountProfilePage");
		stmtI1 = model.listStatements(resource, 
				foafAccountProfilePage, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate name property //
			if(!statement.getObject().isLiteral()) {	    		
				if(!ModelException.throwException(ModelException.ONLINEACCOUNT,
						"name property of OnlineAccount resource:"+
						resource.getURI()+" is not a literal")) {
					return null;
				}
			} else {
				onlAcc.setUrl(statement.getObject().asLiteral().getString());
			}
		}
		// belongsTo //
		Property belongsTo = ResourceFactory.createProperty(
				riNamespace + "belongsTo");		
		stmtI1 = model.listStatements(resource, 
				belongsTo, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate belongsTo property //
			if(!statement.getObject().isResource()) {	    		
				if(!ModelException.throwException(ModelException.ONLINEACCOUNT,
						"belongsTo property of resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
			} else {
				onlAcc.setBelongsTo(getLimitedCommunity(model, 
						statement.getObject().asResource()));
			}
		}
		return onlAcc;
	}
	
	public Entity getFoafAgent(Model model, Resource resource) throws Exception {
		Entity entity = getEntity(model, resource, Entity.class);
		// Specific Attributes and Properties of Foaf:Agent Class //
		// name //
		Property name = ResourceFactory.createProperty(
				foafNamespace + "name");
		StmtIterator stmtI1 = model.listStatements(resource, 
				name, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate name property //
			if(!statement.getObject().isLiteral()) {	    		
				if(!ModelException.throwException(ModelException.FOAFAGENT,
						"name property of resource foaf:Agent:"+
						resource.getURI()+" is not a literal")) {
					return null;
				}
			} else {
				entity.setUniqueIdentificator(statement.getObject(
						).asLiteral().getString());
			}
		}
		return entity;
	}
	
	public List<Entity> getEntities(boolean validate) throws Exception {
		List<Entity> entities = new ArrayList<Entity>();
		Resource entity = ResourceFactory.createResource(
				riNamespace + "Entity");
		ResIterator iters = model.listSubjectsWithProperty(
				RDF.type,entity);
		while (iters.hasNext()) {
			Resource resource = iters.nextResource();
			Entity entityIns = getEntity(model,resource,null);
			if(!validate || ValidateModel.validateEntity(entityIns)) {
				entities.add(entityIns);
			} else {
				ModelException.sendMessage(ModelException.WARNING,"entity(" +
						"resource:"+entityIns.getResource()+") dicarted");
			}			
		}
		return entities;
	}
	
	public Entity getEntity(Model model, Resource resource, Class<?> clazz) 
			throws Exception {
		//Or Entity.class or clazz.class
		Entity entity = (Entity) getResourceFromCache(resource, Entity.class);
		if(entity != null) {
			return entity;
		}
		if(clazz == null) {
			StmtIterator stmtI1 = model.listStatements(resource, RDF.type, (RDFNode)null);						
			while(stmtI1.hasNext()) {
				Statement typeStatement = stmtI1.nextStatement();
				for(Resource resourceType : foafAgentClasses) {
					if(typeStatement.getObject().asResource().getURI().equals(
							resourceType.getURI())) {
						return getFoafAgent(model, resource);
					}
				}								
			}
			//The default option:Entity class
			entity = new Entity();
			entity.setResource(resource);
			addResourceInstanceToCache(resource,entity);
		} else {		
			entity = (Entity) clazz.newInstance(); //new Entity();
			entity.setResource(resource);
			addResourceInstanceToCache(resource,entity);
		}		
		
		// Specific Attributes and Properties of Entity Class //
		// identifier //
		Property identifier = ResourceFactory.createProperty(
				riNamespace + "identifier");
		StmtIterator stmtI1 = model.listStatements(resource, 
				identifier, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate identifier property //
			if(!statement.getObject().isLiteral()) {	    		
				if(!ModelException.throwException(ModelException.ENTITY,
						"identifier property of Entity resource:"+
						resource.getURI()+" is not a literal")) {
					return null;
				}
			} else {
				entity.setUniqueIdentificator(statement.getObject(
						).asLiteral().getString());
			}
		}
		// hasReputation //
		Property hasReputation = ResourceFactory.createProperty(
				riNamespace + "hasMetric");		
		stmtI1 = model.listStatements(resource, 
				hasReputation, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate hasReputation property //
			if(!statement.getObject().isResource()) {	    		
				if(!ModelException.throwException(ModelException.ENTITY,
						"hasReputation property of Entity resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
			} else {
				ReputationObject repObj = (ReputationObject) getResourceFromCache(
						statement.getObject().asResource(), ReputationObject.class);
				if(repObj == null) {
					repObj = getReputationObject(model, 
							statement.getObject().asResource());					
				}
				entity.addHasReputation(repObj);
			}
		}
		// hasValue //
		Property hasValue = ResourceFactory.createProperty(
				riNamespace + "hasValue");		
		stmtI1 = model.listStatements(resource, 
				hasValue, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate hasValue property //
			if(!statement.getObject().isResource()) {	    		
				if(!ModelException.throwException(ModelException.ENTITY,
						"hasValue property of Entity resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
			} else {
				ReputationValue repVal = (ReputationValue) getResourceFromCache(
						statement.getObject().asResource(), ReputationValue.class);
				if(repVal == null) {
					repVal = getReputationValue(model, 
							statement.getObject().asResource());					
				}
				entity.addHasValue(repVal);
			}
		}
		// hasEvaluation //
		Property hasEvaluation = ResourceFactory.createProperty(
				riNamespace + "hasEvaluation");		
		stmtI1 = model.listStatements(resource, 
				hasEvaluation, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate hasEvaluation property //
			if(!statement.getObject().isResource()) {	    		
				if(!ModelException.throwException(ModelException.ENTITY,
						"hasEvaluation property of Entity resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
			} else {
				ReputationEvaluation repEva = (ReputationEvaluation) getResourceFromCache(
						statement.getObject().asResource(), ReputationEvaluation.class);
				if(repEva == null) {
					repEva = getReputationEvaluation(model, 
							statement.getObject().asResource());					
				}
				entity.addHasEvaluation(repEva);
			}
		}
		// foafAccount //
		Property foafAccount = ResourceFactory.createProperty(
				foafNamespace + "account");		
		stmtI1 = model.listStatements(resource, 
				foafAccount, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate foafAccount property //
			if(!statement.getObject().isResource()) {	    		
				if(!ModelException.throwException(ModelException.ENTITY,
						"foafAccount property of Entity resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
			} else {
				entity.addOnlineAccount(getFoafOnlineAccount(model, 
						statement.getObject().asResource()));
			}
		}
		// foafHoldsAccount //
		Property foafHoldsAccount = ResourceFactory.createProperty(
				foafNamespace + "holdsAccount");		
		stmtI1 = model.listStatements(resource, 
				foafHoldsAccount, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate foafHoldsAccount property //
			if(!statement.getObject().isResource()) {	    		
				if(!ModelException.throwException(ModelException.ENTITY,
						"foafHoldsAccount property of Entity resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
			} else {
				entity.addOnlineAccount(getFoafOnlineAccount(model, 
						statement.getObject().asResource()));
			}
		}		
		return entity;
	}
	
	public ReputationObject getReputationObject(Model model, 
			Resource resource) throws Exception {
		ReputationObject repObj = (ReputationObject) getResourceFromCache(
				resource, ReputationObject.class);
		if(repObj == null) {
			repObj = new ReputationObject();
			repObj.setResource(resource);
			addResourceInstanceToCache(resource,repObj);
		} else {
			return repObj;
		}
		// Specific Attributes and Properties of ReputationEvaluation Class //
		// fromCommunity //
		Property fromCommunity = ResourceFactory.createProperty(
				riNamespace + "fromCommunity");		
		StmtIterator stmtI1 = model.listStatements(resource, 
				fromCommunity, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate fromCommunity property //
			if(!statement.getObject().isResource()) {	    		
				if(!ModelException.throwException(ModelException.REPUTATIONOBJECT,
						"fromCommunity property of ReputationObject resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
			} else {
				Community community = (Community) getResourceFromCache(
						statement.getObject().asResource(), Community.class);
				if(community == null) {
					community = getCommunity(model, 
							statement.getObject().asResource());					
				}
				repObj.setFromCommunity(community);
			}
		}
		// hasValue //
		Property hasValue = ResourceFactory.createProperty(
				riNamespace + "hasValue");		
		stmtI1 = model.listStatements(resource, 
				hasValue, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate hasValue property //
			if(!statement.getObject().isResource()) {	    		
				if(!ModelException.throwException(ModelException.REPUTATIONOBJECT,
						"hasValue property of ReputationObject resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
			} else {
				ReputationValue repVal = (ReputationValue) getResourceFromCache(
						statement.getObject().asResource(), ReputationValue.class);
				if(repVal == null) {
					repVal = getReputationValue(model, 
							statement.getObject().asResource());					
				}
				repObj.addHasValue(repVal);
			}
		}
		// owner //
		Property owner = ResourceFactory.createProperty(
				riNamespace + "owner");		
		stmtI1 = model.listStatements(resource, 
				owner, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate owner property //
			if(!statement.getObject().isResource()) {	    		
				if(!ModelException.throwException(ModelException.REPUTATIONOBJECT,
						"owner property of ReputationObject resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
			} else {
				Entity entity = (Entity) getResourceFromCache(
						statement.getObject().asResource(), Entity.class);
				if(entity == null) {
					entity = getEntity(model,statement.getObject().asResource(),null);					
				}
				repObj.setOwner(entity);
			}
		}
		return repObj;
	}
	
	public ReputationEvaluation getReputationEvaluation(Model model, 
			Resource resource) throws Exception {
		ReputationEvaluation repEva = (ReputationEvaluation) getResourceFromCache(
				resource, ReputationEvaluation.class);
		if(repEva == null) {
			repEva = new ReputationEvaluation();
			repEva.setResource(resource);
			addResourceInstanceToCache(resource,repEva);
		} else {
			return repEva;
		}
		// Specific Attributes and Properties of ReputationEvaluation Class //
		// collectionIdentifier //
		Property identifier = ResourceFactory.createProperty(
				riNamespace + "collectionIdentifier");
		StmtIterator stmtI1 = model.listStatements(resource, 
				identifier, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate collectionIdentifier property //
			if(!statement.getObject().isLiteral()) {	    		
				if(!ModelException.throwException(ModelException.REPUTATIONEVALUATION,
						"collectionIdentifier property of ReputationEvaluation resource:"+
						resource.getURI()+" is not a literal")) {
					return null;
				}
			} else {
				repEva.setCollectionIdentifier(statement.getObject(
						).asLiteral().getString());
			}
		}
		// target //
		Property target = ResourceFactory.createProperty(
				riNamespace + "target");		
		stmtI1 = model.listStatements(resource, 
				target, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate target property //
			if(!statement.getObject().isResource()) {	    		
				if(!ModelException.throwException(ModelException.REPUTATIONEVALUATION,
						"target property of ReputationEvaluation resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
			} else {
				Entity targetImp = (Entity) getResourceFromCache(
						statement.getObject().asResource(), Entity.class);
				if(targetImp == null) {
					targetImp = getEntity(model,statement.getObject().asResource(),null);					
				}
				repEva.setTarget(targetImp);
			}
		}
		// hasMetric //
		Property hasMetric = ResourceFactory.createProperty(
				riNamespace + "hasMetric");		
		stmtI1 = model.listStatements(resource, 
				hasMetric, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate hasMetric property //
			if(!statement.getObject().isResource()) {	    		
				if(!ModelException.throwException(ModelException.REPUTATIONEVALUATION,
						"hasMetric property of ReputationEvaluation resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
			} else {
				Metric metric = (Metric) getResourceFromCache(
						statement.getObject().asResource(), Metric.class);
				if(metric == null) {
					metric = getMetric(model, 
							statement.getObject().asResource());					
				}
				repEva.setHasMetric(metric);
			}
		}
		return repEva;
	}	
	
	public ReputationValue getReputationValue(Model model, Resource resource) throws Exception {
		ReputationValue repVal = (ReputationValue) getResourceFromCache(
				resource, ReputationValue.class);
		if(repVal == null) {
			repVal = new ReputationValue();
			repVal.setResource(resource);
			addResourceInstanceToCache(resource,repVal);
		} else {
			return repVal;
		}
		// Specific Attributes and Properties of ReputationValue Class //
		// collectionIdentifier //
		Property identifier = ResourceFactory.createProperty(
				riNamespace + "collectionIdentifier");
		StmtIterator stmtI1 = model.listStatements(resource, 
				identifier, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate collectionIdentifier property //
			if(!statement.getObject().isLiteral()) {	    		
				if(!ModelException.throwException(ModelException.REPUTATIONVALUE,
						"collectionIdentifier property of resource:"+
						resource.getURI()+" is not a literal")) {
					return null;
				}
			} else {
				repVal.setCollectionIdentifier(statement.getObject(
						).asLiteral().getString());
			}
		}
		// timeStamp //
		Property homePage = ResourceFactory.createProperty(
				riNamespace + "timeStamp");
		stmtI1 = model.listStatements(resource, 
				homePage, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate timeStamp property //
			if(!statement.getObject().isLiteral()) {	    		
				if(!ModelException.throwException(ModelException.REPUTATIONVALUE,
						"timeStamp property of resource:"+
						resource.getURI()+" is not a literal")) {
					return null;
				}
			} else {
				DateFormat dt = DateFormat.getDateTimeInstance(); 
				repVal.setTimeStamp(dt.parse(statement.getObject(
						).asLiteral().getString()));
			}
		}
		// expirationTime //
		Property expirationTime = ResourceFactory.createProperty(
				riNamespace + "expirationTime");
		stmtI1 = model.listStatements(resource, 
				expirationTime, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate expirationTime property //
			if(!statement.getObject().isLiteral()) {	    		
				if(!ModelException.throwException(ModelException.REPUTATIONVALUE,
						"expirationTime property of resource:"+
						resource.getURI()+" is not a literal")) {
					return null;
				}
			} else {
				DateFormat dt = DateFormat.getDateTimeInstance(); 
				repVal.setExpirationTime(dt.parse(statement.getObject(
						).asLiteral().getString()));
			}
		}
		// obtainedBy //
		Property obtainedBy = ResourceFactory.createProperty(
				riNamespace + "obtainedBy");		
		stmtI1 = model.listStatements(resource, 
				obtainedBy, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate obtainedBy property //
			if(!statement.getObject().isResource()) {	    		
				if(!ModelException.throwException(ModelException.REPUTATIONVALUE,
						"obtainedBy property of resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
			} else {
				ReputationAlgorithmImplementation repAlg = (ReputationAlgorithmImplementation)
					getResourceFromCache(statement.getObject().asResource(), 
							ReputationAlgorithmImplementation.class);
				if(repAlg == null) {
					repAlg = getReputationAlgorithm(model, 
							statement.getObject().asResource(),null);					
				}
				repVal.setObtainedBy(repAlg);
			}
		}
		// owner //
		Property owner = ResourceFactory.createProperty(
				riNamespace + "owner");		
		stmtI1 = model.listStatements(resource, 
				owner, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate owner property //
			if(!statement.getObject().isResource()) {	    		
				if(!ModelException.throwException(ModelException.REPUTATIONVALUE,
						"owner property of resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
			} else {
				Entity entity = (Entity) getResourceFromCache(
						statement.getObject().asResource(), Entity.class);
				if(entity == null) {
					entity = getEntity(model,statement.getObject().asResource(),null);					
				}
				repVal.setOwner(entity);
			}
		}
		// hasEvaluation //
		Property hasEvaluation = ResourceFactory.createProperty(
				riNamespace + "hasEvaluation");		
		stmtI1 = model.listStatements(resource, 
				hasEvaluation, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate hasEvaluation property //
			if(!statement.getObject().isResource()) {	    		
				if(!ModelException.throwException(ModelException.REPUTATIONVALUE,
						"hasEvaluation property of resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
			} else {
				ReputationEvaluation repEva = (ReputationEvaluation) getResourceFromCache(
						statement.getObject().asResource(), ReputationEvaluation.class);
				if(repEva == null) {
					repEva = getReputationEvaluation(model, 
							statement.getObject().asResource());					
				}
				repVal.addHasEvaluations(repEva);
			}
		}
		return repVal;
	}
	
	public Community getCommunity(Model model, Resource resource) throws Exception {
		Community community = (Community) getResourceFromCache(resource, Community.class);
		if(community == null) {
			community = new Community();
			community.setResource(resource);
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
				if(!ModelException.throwException(ModelException.COMMUNITY,
						"identifier property of resource:"+
						resource.getURI()+" is not a literal")) {
					return null;
				}
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
				if(!ModelException.throwException(ModelException.COMMUNITY,
						"homePage property of resource:"+
						resource.getURI()+" is not a literal")) {
					return null;
				}
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
				if(!ModelException.throwException(ModelException.COMMUNITY,
						"hasCategory property of resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
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
				if(!ModelException.throwException(ModelException.COMMUNITY,
						"hasReputationModel property of Community resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
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
				if(!ModelException.throwException(ModelException.COMMUNITY,
						"hasEntity property of Community resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
			} else {
				Entity entity = (Entity) getResourceFromCache(
						statement.getObject().asResource(), Entity.class);
				if(entity == null) {
					entity = getEntity(model, 
							statement.getObject().asResource(),null);					
				}
				community.addEntity(entity);
			}
		}
		return community;
	}
	
	public List<Community> getCommunities(boolean validate) throws Exception {
		List<Community> communities = new ArrayList<Community>();
		Resource community = ResourceFactory.createResource(
				riNamespace + "Community");
		ResIterator iters = model.listSubjectsWithProperty(
				RDF.type,community);
		while (iters.hasNext()) {
			Resource resource = iters.nextResource();
			Community communityIns = getCommunity(model,resource);
			if(!validate || ValidateModel.validateCommunity(communityIns)) {
				communities.add(communityIns);
			} else {
				ModelException.sendMessage(ModelException.WARNING,"community(" +
						"resource:"+communityIns.getResource()+") dicarted");
			}
		}
		return communities;
	}
	
	public void printCommunities(Model model) throws Exception {
		List<Community> communities = getCommunities(true);
		if(!communities.isEmpty()) {
		    System.out.println("The database contains subjects" +
		    		" of type community:");
		    for(Community community : communities) {
		        System.out.println("  " + community.getResource().getLocalName());
		        System.out.println(community.toString("     ")); 
		    }
		} else {
		    System.out.println("No simple String riNamespace+" +
		    		"community were found in the database");
		}
	}
	
	public void printEntities(Model model) throws Exception {
		List<Entity> entities = getEntities(true);
		if(!entities.isEmpty()) {
		    System.out.println("The database contains subjects" +
		    		" of type entity:");
		    for(Entity entity : entities) {
		        System.out.println("  " + entity.getResource().getLocalName());
		        System.out.println(entity.toString("     ")); 
		    }
		} else {
		    System.out.println("No simple String riNamespace+" +
		    		"Entity were found in the database");
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
				if(!ModelException.throwException(ModelException.REPUTATIONIMPORTER,
						"mapsMetric property of ReputationAlgorithm resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
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
				if(!ModelException.throwException(ModelException.REPUTATIONALGORITHM,
						"importsFrom property of ReputationAlgorithm resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
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
				if(!ModelException.throwException(ModelException.REPUTATIONIMPORTER,
						"mapsMetric property of ReputationImporter resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
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
				if(!ModelException.throwException(ModelException.REPUTATIONIMPORTER,
						"importsFrom property of ReputationImporter resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
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
		impUni.setResource(resource);
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
				if(!ModelException.throwException(ModelException.IMPORTATIONUNIT,
						"importedCommunity property of ImportationUnit resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
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
				if(!ModelException.throwException(ModelException.IMPORTATIONUNIT,
						"importedMetric property of MetricMapping resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
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
				if(!ModelException.throwException(ModelException.IMPORTATIONUNIT,
						"collectsReputationBy property of ImportationUnit resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
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
				if(!ModelException.throwException(ModelException.IMPORTATIONUNIT,
						"metricTransformation property of ImportationUnit resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
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
				if(!ModelException.throwException(ModelException.IMPORTATIONUNIT,
						"trust property of importationUnit ImportationUnit resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
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
		metMap.setResource(resource);
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
				if(!ModelException.throwException(ModelException.METRICMAPPING,
						"value property of MetricMapping resource:"+
						resource.getURI()+" is not a literal")) {
					return null;
				}
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
				if(!ModelException.throwException(ModelException.METRICMAPPING,
						"importedMetric property of MetricMapping resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
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
				if(!ModelException.throwException(ModelException.METRICMAPPING,
						"resultMetric property of MetricMapping resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
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
	
	public List<Metric> getMetrics(boolean validate) throws Exception {
		List<Metric> metrics = new ArrayList<Metric>();
		Resource metric = ResourceFactory.createResource(
				riNamespace + "Metric");
		ResIterator iters = model.listSubjectsWithProperty(
				RDF.type,metric);
		while (iters.hasNext()) {
			Resource resource = iters.nextResource();
			Metric metricIns = getMetric(model, resource);
			if(!validate || ValidateModel.validateMetric(metricIns)) {
				metrics.add(metricIns);
			} else {
				ModelException.sendMessage(ModelException.WARNING,"entity(" +
						"resource:"+metricIns.getResource()+") dicarted");
			}			
		}
		return metrics;
	}
	
	public Metric getMetric(Model model, Resource resource) throws Exception {
		Metric metric = (Metric) getResourceFromCache(resource, Metric.class);
		if(metric != null) {			
			return metric;
		}
		metric = new Metric();
		metric.setResource(resource);
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
				if(!ModelException.throwException(ModelException.METRIC,
						"identificator property of Metric resource:"+
						resource.getURI()+" is not a literal")) {
					return null;
				}
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
				if(!ModelException.throwException(ModelException.METRIC,
						"description property of Metric resource:"+
						resource.getURI()+" is not a literal")) {
					return null;
				}
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
				if(!ModelException.throwException(ModelException.METRIC,
						"hasScale property of Metric resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
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
				if(!ModelException.throwException(ModelException.METRIC,
						"hasDimension property of Metric resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
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
				if(!ModelException.throwException(ModelException.SCALE,
						"allowValue property of CategoricScale resource:"+
						resource.getURI()+" is not a literal")) {
					return null;
				}
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
				if(!ModelException.throwException(ModelException.NUMERIC_SCALE,
						"minimum property of NumericScale resource:"+
						resource.getURI()+" is not a literal")) {
					return null;
				}
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
				if(!ModelException.throwException(ModelException.NUMERIC_SCALE,
						"maximum property of NumericScale resource:"+
						resource.getURI()+" is not a literal")) {
					return null;
				}
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
				if(!ModelException.throwException(ModelException.NUMERIC_SCALE,
						"step property of NumericScale resource:"+
						resource.getURI()+" is not a literal")){
					return null;
				}
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
				if(!ModelException.throwException(ModelException.SCALE,
						"Impossible to instanciate a generic Scale(resource:"+resource+"" +
						"Please, see the model and assign a specific type.")) {
					return null;
				}
			}
		}		
		Scale scale = (Scale) getResourceFromCache(resource, type);
		if(scale != null) {			
			return scale;
		}		
		scale = type.newInstance();
		scale.setResource(resource);
		addResourceInstanceToCache(resource, scale);		
		// name //
		Property name = ResourceFactory.createProperty(riNamespace + "name");
		StmtIterator stmtI1 = model.listStatements(resource, 
				name, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate name property //
			if(!statement.getObject().isLiteral()) {	    		
				if(!ModelException.throwException(ModelException.SCALE,
						"name property of scale resource:"+
						resource.getURI()+" is not a literal")) {
					return null;
				}
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
		    		"Scale were found in the database");
		}
	}
	
	public void printAllScales(Model model) throws Exception {
		Set<Resource> resourcesCache = new HashSet<Resource>();
		Set<Resource> scaleClasses = new HashSet<Resource>();		
		scaleClasses.add(ResourceFactory.
				createResource(riNamespace + "Scale"));
		scaleClasses.add(ResourceFactory.
				createResource(riNamespace + "NumericScale"));
		scaleClasses.add(ResourceFactory.
				createResource(riNamespace + "CategoricScale"));
		for(Resource scale : scaleClasses) {
			ResIterator iters = model.listSubjectsWithProperty(
					RDF.type,scale);
			while (iters.hasNext()) {
			  	resourcesCache.add(iters.nextResource());
			}
		}		
		
		if (!resourcesCache.isEmpty()) {
			System.out.println("The database contains subjects" +
    			" of type scale:");
		    for(Resource resource : resourcesCache) {
		        System.out.println("  " + resource.getLocalName());
		        System.out.println(getScale(model,resource,null).toString("     "));		        	        
		    }
		} else {
		    System.out.println("No simple String riNamespace+" +
		    		"Scale were found in the database");
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
				if(!ModelException.throwException(ModelException.ENTITYTYPE,
						"type property of EntityType resource:"+
						resource.getURI()+" is not a literal")) {
					return null;
				}
			} else {
				entityType.setType(statement.getObject().asLiteral().getString());
			}
		}
		return entityType;
	}
	
	/*
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
	}*/
	
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
			repAlg.addBehaviour(behaviour, repAlg);
			//behaviour.setRoot(repAlg);
			return;
		}
		Method method = null;						
		try {
			method = this.getClass().getMethod("get"+subclass.getSimpleName(),
					Model.class, Resource.class);
			behaviour = (ReputationBehaviour)method.invoke(this, 
					model, resource);
			repAlg.addBehaviour(behaviour, repAlg);
			//behaviour.setRoot(repAlg);
		} catch (NoSuchMethodException e) {
			try {
				//method = this.getClass().getMethod("get"+subclass.getSimpleName(),
				//		Model.class, Resource.class, subclass);
				method = this.getClass().getMethod("get"+subclass.getSimpleName(),
						Model.class, Resource.class, Class.class);
				behaviour = (ReputationBehaviour)method.invoke(
						this, model, resource, null);
				repAlg.addBehaviour(behaviour, repAlg);
				//behaviour.setRoot(repAlg);
			} catch (NoSuchMethodException e1) {
				e1.printStackTrace();
				for(Method methoz : this.getClass().getMethods()) {
					System.out.println(methoz);
				}
				throw new Exception("System Bug: Not method get"+subclass.getSimpleName()+
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
		repAlg.setResource(resource);
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
				repAlg.addBehaviour(behaviour, repAlg);
				//behaviour.setRoot(repAlg);
			}
		}

		// Specific Attributes and Properties of ReputationalAlgorithm Class //
		setAttibutesAndProperties(model, resource, repAlg);
		setApiParameters(model, resource, repAlg);
		return repAlg;
	}
	
	void setApiParameters(Model model, Resource resource, 
			ReputationAlgorithmImplementation repAlg) throws Exception {
		Property property = ResourceFactory.
				createProperty(riNamespace + "objectClass");
		// objectClass //
		StmtIterator stmtI1 = model.listStatements(resource, 
				property, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate objectClass property */
			if(!statement.getObject().isLiteral()) {	    		
				if(ModelException.throwException(ModelException.REPUTATIONALGORITHM,
						"objectClass property of ReputationAlgorithm resource:"+
						resource.getURI()+" is not a literal")) {
					repAlg = null;
					return;
				}
			} else {
				repAlg.setObjectClass(statement.getObject().asLiteral().getString());		    	
			}
		}
		property = ResourceFactory.
				createProperty(riNamespace + "algorithmPath");
		// algorithmPath //
		stmtI1 = model.listStatements(resource, 
				property, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate algorithmPath property */
			if(!statement.getObject().isLiteral()) {	    		
				if(ModelException.throwException(ModelException.REPUTATIONALGORITHM,
						"algorithmPath property of ReputationAlgorithm resource:"+
						resource.getURI()+" is not a literal")) {
					repAlg = null;
					return;
				}
			} else {
				repAlg.setAlgorithmPath(statement.getObject().asLiteral().getString());		    	
			}
		}
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
				AccessType type = getAccessType(statement.getObject().asResource());
				if(type == null) {
					if(!ModelException.throwException(ModelException.REPUTATIONALGORITHM, 
							"accessType(resource:"+statement.getObject().asResource()+
							") from ReputationAlgorithm(resource"+resource+") is not known")) {
						repAlg = null;
						return;
					}
				} else {
					repAlg.addAccesibility(type);
				}
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
				if(ModelException.throwException(ModelException.REPUTATIONALGORITHM,
						"name property of ReputationAlgorithm resource:"+
						resource.getURI()+" is not a literal")) {
					repAlg = null;
					return;
				}
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
				if(!ModelException.throwException(ModelException.REPUTATIONALGORITHM,
						"resultCollectionType property of ReputationAlgorithm resource:"+
						resource.getURI()+" is not a resource")) {
					repAlg = null;
					return;
				}
			} else {
				CollectionType collectionType = (CollectionType) getResourceFromCache(
						statement.getObject().asResource(),CollectionType.class);
				if(collectionType == null) {
					collectionType = getCollectionType(statement.getObject().asResource());				
				}
				if(collectionType == null) {
					ModelException.throwException(ModelException.REPUTATIONALGORITHM, 
							"collectionType(resource:"+statement.getObject().asResource()+
							" from ReputationAlgorithm(resource"+resource+") is not known");
				} else {
					repAlg.setResultCollectionType(collectionType);
				}
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
				if(!ModelException.throwException(ModelException.REPUTATIONALGORITHM,
						"dc:description property of ReputationAlgorithm resource:"+
						resource.getURI()+" is not a literal")) {
					repAlg = null;
					return;
				}
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
				if(!ModelException.throwException(ModelException.REPUTATIONALGORITHM,
						"entityType property of ReputationAlgorithm resource:"+
						resource.getURI()+" is not a resource")) {
					repAlg = null;
					return;
				}
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
				if(!ModelException.throwException(ModelException.REPUTATIONALGORITHM,
						"usesMetric property of ReputationAlgorithm resource:"+
						resource.getURI()+" is not a resource")) {
					repAlg = null;
					return;
				}
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
				if(!ModelException.throwException(ModelException.REPUTATIONALGORITHM,
						"reputationSource property of ReputationAlgorithm resource:"+
						resource.getURI()+" is not a resource")) {
					repAlg = null;
					return;
				}
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
				if(!ModelException.throwException(ModelException.REPUTATIONALGORITHM,
						"reputationResult property of ReputationAlgorithm resource:"+
						resource.getURI()+" is not a resource")) {
					repAlg = null;
					return;
				}
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
		/* definedByReputationModel */
		Property definedByReputationModel = ResourceFactory.createProperty(
				riNamespace + "definedByReputationModel");
		stmtI1 = model.listStatements(resource, 
				definedByReputationModel, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate definedByReputationModel property */
			if(!statement.getObject().isResource()) {	    		
				if(!ModelException.throwException(ModelException.REPUTATIONALGORITHM,
						"definedByReputationModel property of ReputationAlgorithm resource:"+
						resource.getURI()+" is not a resource")) {
					repAlg = null;
					return;
				}
			} else {
				repAlg.setDefinedByReputationModel(getReputationAlgorithm(
						model,statement.getObject().asResource(),null));		    	
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
				if(!ModelException.throwException(ModelException.REPUTATIONALGORITHM,
						"stepIdentifier property of resource:"+
						resource.getURI()+" is not a literal")) {
					repAlg = null;
					return;
				}
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
					if(!ModelException.throwException(ModelException.REPUTATIONALGORITHM,
							"Property "+property.getURI()+" has the object "
							+object+" that is not a resource when it must be a " +
							"ReputationAlgorithm instance")) {
						continue;
					}
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
			    System.out.println("  "+repAlgInstance);
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
				if(!ModelException.throwException(ModelException.REPUTATIONMODULE,
						"obtainsReputationsBy property of resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
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
			//System.out.println("CACHE FOUND with reputationModuleBehaviour:"+behaviour);
			return behaviour;
		}		
		behaviour = new ReputationModuleBehaviour();
		addResourceInstanceToCache(resource, behaviour);
		// Specific Attributes and Properties of ReputationModule Class //
		// obtainsReputationBy //
		Property obtainsReputationBy = ResourceFactory.createProperty(
				riNamespace + "obtainsReputationBy");
		StmtIterator stmtI1 = model.listStatements(resource, 
				obtainsReputationBy, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate obtainsReputationBy property */
			if(!statement.getObject().isResource()) {	    		
				if(!ModelException.throwException(ModelException.REPUTATIONMODULE,
						"obtainsReputationBy property of resource:"+
						resource.getURI()+" is not a resource")) {
							return null;
						}
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
				if(!ModelException.throwException(ModelException.REPUTATIONMODEL,
						"reputationModule property of resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
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
				if(!ModelException.throwException(ModelException.REPUTATIONMODEL,
						"reputationModule property of resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
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
				if(!ModelException.throwException(ModelException.COLLECTINGSYSTEM,
						"uriFormat property of CollectingSystem resource:"+
						resource.getURI()+" is not a literal")) {
					return null;
				}
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
				if(!ModelException.throwException(ModelException.COLLECTINGSYSTEM,
						"uriFormat property of CollectingSystem resource:"+
						resource.getURI()+" is not a literal")) {
					return null;
				}
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
	
	public List<MetricTransformer> getMetricTransformers(
			boolean validate) throws Exception {
		List<MetricTransformer> transformers = new ArrayList<MetricTransformer>();
		Resource metricTransformer = ResourceFactory.createResource(
				riNamespace + "MetricTransformer");
		ResIterator iters = model.listSubjectsWithProperty(
				RDF.type,metricTransformer);
		while (iters.hasNext()) {
			Resource resource = iters.nextResource();
			MetricTransformer tranformerIns = getMetricTransformer(model,resource,null);
			if(!validate || ValidateModel.validateMetricTransformer(tranformerIns)) {
				transformers.add(tranformerIns);
			} else {
				ModelException.sendMessage(ModelException.WARNING,"entity(" +
						"resource:"+tranformerIns.getResource()+") dicarted");
			}			
		}
		return transformers;
	}
	
	class MetricTransformerInstances {
		String identifier;
		Metric sourceMetric;
		Metric destinationMetric;
		List<Double> correlationBetweenMetrics;
		String description;
		
		MetricTransformerInstances(String identifier, Metric sourceMetric, 
				Metric destinationMetric, List<Double> correlationBetweenMetrics, 
				String description) {
			this.identifier = identifier;
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
				if(!ModelException.throwException(ModelException.METRICTRANSFORMER,
						"Resource "+resource+" cannot be defined" +
						" as a direct instance of MetricTransformer. You must not use" +
						" abstract subclasses")) {
					return null;
				}		
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
			metTra.setResource(resource);
			addResourceInstanceToCache(resource, metTra);
			metTra.setDescription(instances.description);
			metTra.setIdentifier(instances.identifier);
		} catch(Exception e) {
			throw new Exception("System Bug: Not constructor with args: (Metric, " +
					"Metric, List) exist for type:"+type);
		}
		return metTra;
	}
	
	public MetricTransformerInstances getMetricTransformer(
			Model model, Resource resource) throws Exception {
		Property identifier = ResourceFactory.
				createProperty(riNamespace + "identifier");
		/* identifier */
		String identifierInstance = null;		
		StmtIterator stmtI1 = model.listStatements(resource, 
				identifier, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate identifier property //
			if(!statement.getObject().isLiteral()) {	    		
				if(!ModelException.throwException(ModelException.METRICTRANSFORMER,
						"identifier property of MetricTransformer resource:"+
						resource.getURI()+" is not a literal")) {
					return null;
				}
			} else {
				identifierInstance = statement.getObject().asLiteral().getString();		    	
			}
		}
		Property sourceMetric = ResourceFactory.
				createProperty(riNamespace + "sourceMetric");
		/* sourceMetric */
		Metric sourceInstance = null;		
		stmtI1 = model.listStatements(resource, sourceMetric, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			/* validate sourceMetric property */
			if(!statement.getObject().isResource()) {	    		
				if(!ModelException.throwException(ModelException.METRICTRANSFORMER,
						"sourceMetric property of MetricTransformer resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
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
				if(!ModelException.throwException(ModelException.METRICTRANSFORMER,
						"destinationMetric property of MetricTransformer resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
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
		stmtI1 = model.listStatements(resource, 
				correlationBetweenDimensions, (RDFNode)null);
		while(stmtI1.hasNext()) {
			Statement statement = stmtI1.nextStatement();
			// validate correlationBetweenMetrics property //
			if(!statement.getObject().isResource()) {	    		
				if(!ModelException.throwException(ModelException.METRICTRANSFORMER,
						"correlationBetweenDimensions property of MetricTransformer resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
			} else {
				DimensionCorrelation correlationInstance = getDimensionCorrelation(
						model,statement.getObject().asResource());
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
			if(!statement.getObject().isResource()) {	    		
				if(!ModelException.throwException(ModelException.METRICTRANSFORMER,
						"correlationBetweenScales property of MetricTransformer resource:"+
						resource.getURI()+" is not a resource")) {
					return null;
				}
			} else {
				ScaleCorrelation correlationInstance = getScaleCorrelation(
						model,statement.getObject().asResource());
				//TODO: validate scales are the same of source and destination metrics
				correlationsInstance.add(correlationInstance.getCorrelationValue());						    	
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
				if(!ModelException.throwException(ModelException.METRICTRANSFORMER,
						"dc:description property of MetricTransformer resource:"+
						resource.getURI()+" is not a literal")) {
					return null;
				}
			} else {
				descriptionInstance = statement.getObject().asLiteral().getString();		    	
			}
		}
		return new MetricTransformerInstances(identifierInstance, sourceInstance, 
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
		Double baseInstance;
		try {
			baseInstance = getBaseObject(model, resource);
		} catch(DiscardException e) {
			return null;
		}	
		if(baseInstance == null) {
			expNum = new ExponentialNumericTransformer(
					metTraInstance.sourceMetric, metTraInstance.destinationMetric, 
					metTraInstance.correlationBetweenMetrics);			
		} else {
			expNum = new ExponentialNumericTransformer(
					metTraInstance.sourceMetric, metTraInstance.destinationMetric, 
					metTraInstance.correlationBetweenMetrics, baseInstance);
		}
		expNum.setResource(resource);
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
		sqrtNum.setResource(resource);
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
		linNum.setResource(resource);
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
		        System.out.println(linNumInstance.toReducedString("     "));
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
				if(!ModelException.throwException(ModelException.NUMERICTRANSFORMER,
						"base property of NumericTransformer resource:"+
						resource.getURI()+" is not a literal")) {
					throw new DiscardException("");
				}
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
		Double baseInstance;
		try {
			baseInstance = getBaseObject(model, resource);
		} catch(DiscardException e) {
			return null;
		}
		if(baseInstance == null) {
			logNum = new LogaritmicNumericTransformer(
					metTraInstance.sourceMetric, metTraInstance.destinationMetric, 
					metTraInstance.correlationBetweenMetrics);
		} else {
			logNum = new LogaritmicNumericTransformer(
					metTraInstance.sourceMetric, metTraInstance.destinationMetric, 
					metTraInstance.correlationBetweenMetrics, baseInstance);
		}
		logNum.setResource(resource);
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
	
	public ScaleCorrelation getScaleCorrelation(
			Model model, Resource resource) throws Exception {
		ScaleCorrelation scaCor = (ScaleCorrelation) 
			getResourceFromCache(resource, ScaleCorrelation.class);
		if(scaCor != null) {			
			return scaCor;
		}		
		scaCor = new ScaleCorrelation();
		scaCor.setResource(resource);
		addResourceInstanceToCache(resource, scaCor);
		Property sourceScale = ResourceFactory.
				createProperty(riNamespace + "sourceScale");		
		// sourceScale //
        StmtIterator stmtI1 = model.listStatements(resource, 
        		sourceScale, (RDFNode)null);
		while(stmtI1.hasNext()) {
	    	Statement statement = stmtI1.nextStatement();
	    	// validate sourceScale property //
	    	if(!statement.getObject().isResource()) {	    		
	    		if(!ModelException.throwException(ModelException.SCALECORRELATION,
						"sourceScale property of ScaleCorrelation resource:"+
	    				resource.getURI()+" is not a resource")) {
	    			return null;
	    		}
	    	} else {
	    		scaCor.setSourceScale(getScale(
						model, statement.getObject().asResource(),null));		    	
	    	}
	    }
		Property targetScale = ResourceFactory.
				createProperty(riNamespace + "targetScale");
		// targetScale //
        stmtI1 = model.listStatements(resource, 
        		targetScale, (RDFNode)null);
		while(stmtI1.hasNext()) {
	    	Statement statement = stmtI1.nextStatement();
	    	// validate targetScale property //
	    	if(!statement.getObject().isResource()) {	    		
	    		if(!ModelException.throwException(ModelException.SCALECORRELATION,
						"targetScale property of ScaleCorrelation resource:"+
	    				resource.getURI()+" is not a resource")) {
	    			return null;
	    		}
	    	} else {
	    		scaCor.setTargetScale(getScale(
						model, statement.getObject().asResource(),null));		    	
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
				if(!ModelException.throwException(ModelException.SCALECORRELATION,
						"correlationValue property of ScaleCorrelation resource:"+
						resource.getURI()+" is not a literal")) {
	    			return null;
	    		}
			} else {
				scaCor.setCorrelationValue(
						statement.getObject().asLiteral().getDouble());		    	
			}
		}		
		return scaCor;
	}
	
	public void printScaleCorrelations(Model model) throws Exception {
		Resource scaleCorrelation = ResourceFactory.
				createResource(riNamespace + "ScaleCorrelation");
		ResIterator iters = model.listSubjectsWithProperty(RDF.type,scaleCorrelation);
		if (iters.hasNext()) {
		    System.out.println("The database contains subjects of type scaleCorrelation:");
		    while (iters.hasNext()) {
		        Resource resource = iters.nextResource();
		        System.out.println("  " + resource.getLocalName());
		        ScaleCorrelation scaCorInstance = getScaleCorrelation(model,resource);		        
		        System.out.println(scaCorInstance.toString("     "));
		    }
		} else {
		    System.out.println("No simple String riNamespace+" +
		    		"ScaleCorrelation were found in the database");
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
		dimCor.setResource(resource);
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
	    		if(!ModelException.throwException(ModelException.DIMENSIONCORRELATION,
						"sourceDimension property of DimensionCorrelation resource:"+
	    				resource.getURI()+" is not a resource")) {
	    			return null;
	    		}
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
	    		if(!ModelException.throwException(ModelException.DIMENSIONCORRELATION,
						"targetDimension property of DimensionCorrelation resource:"+
	    				resource.getURI()+" is not a resource")) {
	    			return null;
	    		}
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
				if(!ModelException.throwException(ModelException.DIMENSIONCORRELATION,
						"correlationValue property of DimensionCorrelation resource:"+
						resource.getURI()+" is not a literal")) {
	    			return null;
	    		}
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
		        System.out.println(dimCorInstance.toString("     "));
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
			truBet.setResource(resource);
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
				truBet.setResource(resource);
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
				if(!ModelException.throwException(ModelException.TRUSTBETWEENCOMMUNITIES,
						"trustProvidedBy property of TrustBetweenCommunities resource:"+
						resource.getURI()+" is not a resource")) {
	    			return null;
	    		}
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
					if(!ModelException.throwException(ModelException.TRUSTBETWEENCOMMUNITIES,
							"value property of TrustBetweenCommunities resource:"+
							resource.getURI()+" is not a literal")) {
		    			return null;
		    		}
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
	    		if(!ModelException.throwException(ModelException.FIXEDCOMMUNITYTRUST,
						"communityScorer property of FixedCommunityTrust resource:"+
	    				resource.getURI()+" is not a resource")) {
	    			return null;
	    		}
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
				if(!ModelException.throwException(ModelException.FIXEDCOMMUNITYTRUST,
						"communityScored property of FixedCommunityTrust resource:"+
						resource.getURI()+" is not a resource")) {
	    			return null;
	    		}
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
                System.out.println(catMatInstance.toString("     "));
            }
        } else {
            System.out.println("No simple String riNamespace+Dimension were found" +
            		" in the database");
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
	    		if(!ModelException.throwException(ModelException.CATEGORYMATCHING,
						"originatingCategory property of CategoryMatching resource:"+
	    				resource.getURI()+" is not a resource")) {
	    			return null;
	    		}
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
				if(!ModelException.throwException(ModelException.CATEGORYMATCHING,
						"receivingCategory property of CategoryMatching resource:"+
						resource.getURI()+" is not a resource")) {
	    			return null;
	    		}
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
                System.out.println(categoryInstance.toString("     "));                				
            }
        } else {
            System.out.println("No simple String riNamespace+Dimension" +
            		" were found in the database");
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
	    		if(!ModelException.throwException(ModelException.CATEGORY,
						"name property of Category resource:"+
	    				resource.getURI()+" is not a literal")) {
	    			return null;
	    		}
	    	}
	    	category = new Category(statement.getObject().asLiteral().getString());
	    	category.setResource(resource);
	    	addResourceInstanceToCache(resource, category); 
	    }
		Property description = ResourceFactory.createProperty(dcNamespace + "description");		
		// description //
        stmtI1 = model.listStatements(resource, description, (RDFNode)null);
		while(stmtI1.hasNext()) {
	    	Statement statement = stmtI1.nextStatement();
	    	// validate description property //
	    	if(!statement.getObject().isLiteral()) {
	    		if(!ModelException.throwException(ModelException.CATEGORY,
						"description property of Category resource:"+
	    				resource.getURI()+" is not a literal")) {
	    			return null;
	    		}
	    	}
	    	if(category != null) {
	    		category.setDescription(statement.getObject().asLiteral().getString());
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
                System.out.println(dimensionInstance.toString("     "));                				
            }
        } else {
            System.out.println("No simple String riNamespace+Dimension were found" +
            		" in the database");
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
	    		if(!ModelException.throwException(ModelException.DIMENSION,
						"name property of Dimension resource:"+
	    				resource.getURI()+" is not a literal")) {
	    			return null;
	    		}
	    	}
	    	dimension = new Dimension(statement.getObject().asLiteral().getString());
	    	dimension.setResource(resource);
	    	addResourceInstanceToCache(resource, dimension);	    	    	
	    }
		Property description = ResourceFactory.createProperty(dcNamespace + "description");		
		// description //
        stmtI1 = model.listStatements(resource, description, (RDFNode)null);
		while(stmtI1.hasNext()) {
	    	Statement statement = stmtI1.nextStatement();
	    	// validate description property //
	    	if(!statement.getObject().isLiteral()) {
	    		if(!ModelException.throwException(ModelException.DIMENSION,
						"description property of Dimension resource:"+
	    				resource.getURI()+" is not a literal")) {
	    			return null;
	    		}
	    	}
	    	if(dimension != null) {
	    		dimension.setDescription(statement.getObject().asLiteral().getString());
	    	}	    	
	    }
		return dimension;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public String getRiNamespace() {
		return riNamespace;
	}

	public void setRiNamespace(String riNamespace) {
		this.riNamespace = riNamespace;
	}
	public String getDcNamespace() {
		return dcNamespace;
	}
	public void setDcNamespace(String dcNamespace) {
		this.dcNamespace = dcNamespace;
	}
	public String getFoafNamespace() {
		return foafNamespace;
	}
	public void setFoafNamespace(String foafNamespace) {
		this.foafNamespace = foafNamespace;
	}
	
}
