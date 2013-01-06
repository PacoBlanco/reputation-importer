package cross.reputation.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ModelProperties {
	final static int DISCARD = 0;
	final static int NOTHING = 1;
	final static int EXCEPTION = 2;
	static int DEFAULT = EXCEPTION;
	static int DEAFULT_TO_LOG = ModelException.INFO;
	
	private static Map<Integer,Integer> MODEL_PROPERTIES;
	
	static void initializeProperties(String propertyPath) throws IOException {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(propertyPath));
			Integer defaultProcess = findProcessTypeByString(
					properties.getProperty("default_process"));
			if(defaultProcess != null) {
				DEFAULT = defaultProcess; 
			}
			initializeModelProperties();
			Integer defaultToLog = findLevelTypeByString(properties.getProperty("level_to_log"));
			if(defaultToLog != null) {
				DEAFULT_TO_LOG = defaultToLog; 
			}
			setProperty(properties,"SCALE",ModelException.SCALE);
			setProperty(properties,"SCALE_SUM",ModelException.SCALE_SUM);
			setProperty(properties,"CATEGORIC_SCALE",ModelException.CATEGORIC_SCALE);
			setProperty(properties,"NUMERIC_SCALE",ModelException.NUMERIC_SCALE);
			setProperty(properties,"CATEGORY",ModelException.CATEGORY);
			setProperty(properties,"TRUSTBETWEENCOMMUNITIES",
					ModelException.TRUSTBETWEENCOMMUNITIES);
			setProperty(properties,"CATEGORYMATCHING",ModelException.CATEGORYMATCHING);
			setProperty(properties,"FIXEDCOMMUNITYTRUST",ModelException.FIXEDCOMMUNITYTRUST);
			setProperty(properties,"COMMUNITY",ModelException.COMMUNITY);
			setProperty(properties,"COMMUNITY_ENTITY",ModelException.COMMUNITY_ENTITY);
			setProperty(properties,"COMMUNITY_MODEL",ModelException.COMMUNITY_MODEL);
			setProperty(properties,"COMMUNITY_MODEL",ModelException.COMMUNITY_METRIC);
			setProperty(properties,"DIMENSION",ModelException.DIMENSION);
			setProperty(properties,"ENTITY",ModelException.ENTITY);
			setProperty(properties,"ONLINEACCOUNT",ModelException.ONLINEACCOUNT);
			setProperty(properties,"REPUTATIONOBJECT",ModelException.REPUTATIONOBJECT);
			setProperty(properties,"REPUTATIONOBJECT_ENTITY",
					ModelException.REPUTATIONOBJECT_ENTITY);
			setProperty(properties,"REPUTATIONOBJECT_REPUATIONVALUE",
					ModelException.REPUTATIONOBJECT_REPUATIONVALUE);
			setProperty(properties,"REPUTATIONVALUE",ModelException.REPUTATIONVALUE);
			setProperty(properties,"REPUTATIONVALUE_ENTITY",
					ModelException.REPUTATIONVALUE_ENTITY);
			setProperty(properties,"REPUTATIONVALUE_REPUTATIONALGORITHM",
					ModelException.REPUTATIONVALUE_REPUTATIONALGORITHM);
			setProperty(properties,"REPUTATIONEVALUATION",
					ModelException.REPUTATIONEVALUATION);
			setProperty(properties,"REPUTATIONEVALUATION_METRIC",
					ModelException.REPUTATIONEVALUATION_METRIC);
			setProperty(properties,"REPUTATIONEVALUATION_ENTITY",
					ModelException.REPUTATIONEVALUATION_ENTITY);
			setProperty(properties,"REPUTATIONEVALUATION_REPUTATIONALGORITHM",
					ModelException.REPUTATIONEVALUATION_REPUTATIONALGORITHM);
			setProperty(properties,"METRICTRANSFORMER",ModelException.METRICTRANSFORMER);
			setProperty(properties,"METRICTRANSFORMER_IDENTIFIER",
					ModelException.METRICTRANSFORMER_IDENTIFIER);
			setProperty(properties,"NUMERICTRANSFORMER",ModelException.NUMERICTRANSFORMER);
			setProperty(properties,"METRIC",ModelException.METRIC);
			setProperty(properties,"REPUTATIONALGORITHM",ModelException.REPUTATIONALGORITHM);
			setProperty(properties,"REPUTATIONALGORITHM_DEFINEDBY",
					ModelException.REPUTATIONALGORITHM_DEFINEDBY);
			setProperty(properties,"REPUTATIONALGORITHM_METRIC",
					ModelException.REPUTATIONALGORITHM_METRIC);
			setProperty(properties,"REPUTATIONALGORITHM_METRIC",
					ModelException.REPUTATIONALGORITHM_METRIC_ERROR);
			setProperty(properties,"REPUTATIONALGORITHM_BEHAVIOUR",
					ModelException.REPUTATIONALGORITHM_BEHAVIOUR);
			setProperty(properties,"REPUTATIONALGORITHM_USEMETRIC",
					ModelException.REPUTATIONALGORITHM_USEMETRIC);
			setProperty(properties,"REPUTATIONALGORITHM_COLLECTION",
					ModelException.REPUTATIONALGORITHM_COLLECTION);
			setProperty(properties,"REPUTATIONMODEL",ModelException.REPUTATIONMODEL);
			setProperty(properties,"REPUTATIONMODULE",ModelException.REPUTATIONMODULE);
			setProperty(properties,"REPUTATIONIMPORTER",ModelException.REPUTATIONIMPORTER);
			setProperty(properties,"REPUTATIONIMPORTER_IMPORTATION",
					ModelException.REPUTATIONIMPORTER_IMPORTATION);
			setProperty(properties,"IMPORTATIONUNIT",ModelException.IMPORTATIONUNIT);
			setProperty(properties,"IMPORTATIONUNIT_METRIC",
					ModelException.IMPORTATIONUNIT_METRIC);
			setProperty(properties,"IMPORTATIONUNIT_COMMUNITY",
					ModelException.IMPORTATIONUNIT_COMMUNITY);
			setProperty(properties,"IMPORTATIONUNIT_COLLECTSREPUTATION",
					ModelException.IMPORTATIONUNIT_COLLECTSREPUTATION);
			setProperty(properties,"IMPORTATIONUNIT_METRICTRANSFORMER",
					ModelException.IMPORTATIONUNIT_METRICTRANSFORMER);
			setProperty(properties,"COLLECTINGALGORITHM",ModelException.COLLECTINGALGORITHM);
			setProperty(properties,"COLLECTINGSYSTEM",ModelException.COLLECTINGSYSTEM);
			setProperty(properties,"METRICMAPPING",ModelException.METRICMAPPING);
			setProperty(properties,"DIMENSIONCORRELATION",ModelException.DIMENSIONCORRELATION);
			setProperty(properties,"SCALECORRELATION",ModelException.SCALECORRELATION);
			setProperty(properties,"ENTITYTYPE",ModelException.ENTITYTYPE);
			setProperty(properties,"GET_MORE_ACCOUNTS",ModelException.GET_MORE_ACCOUNTS);
			setProperty(properties,"EXTRACT_REPUTATION_URL",ModelException.EXTRACT_REPUTATION_URL);
			setProperty(properties,"ALGORITHM_IMPLEMENTATION_NOT_FOUND",
					ModelException.ALGORITHM_IMPLEMENTATION_NOT_FOUND);
			setProperty(properties,"ALGORITHM_IMPLEMENTATION_ERROR",
					ModelException.ALGORITHM_IMPLEMENTATION_ERROR);
			setProperty(properties,"NOT_URI_KNOWN",ModelException.NOT_URI_KNOWN);
			setProperty(properties,"NOT_URI_FOUND",ModelException.NOT_URI_FOUND);
			setProperty(properties,"URI_NOT_SUPPORTED",ModelException.URI_NOT_SUPPORTED);
			setProperty(properties,"ELEMENT_FROM_URI_NOT_FOUND",
					ModelException.ELEMENT_FROM_URI_NOT_FOUND);
			setProperty(properties,"COLLECTINGSYSTEM_ERROR",ModelException.COLLECTINGSYSTEM_ERROR);
			setProperty(properties,"SCRAPPY_ERROR",ModelException.SCRAPPY_ERROR);
			setProperty(properties,"OPAL_ERROR",ModelException.OPAL_ERROR);
			setProperty(properties,"COLLECTINGSYSTEM_NOT_KNOWN",
					ModelException.COLLECTINGSYSTEM_NOT_KNOWN);
			setProperty(properties,"IMPORTATION_UNIT_COLLSYSTEM_INSIDE",
					ModelException.IMPORTATION_UNIT_COLLSYSTEM_INSIDE);
			setProperty(properties,"SYSTEM_NAME_NOT_EXPECTED",
					ModelException.SYSTEM_NAME_NOT_EXPECTED);
			setProperty(properties,"OBJECT_WITH_IDENTIFIER_NOT_KNOWN",
					ModelException.OBJECT_WITH_IDENTIFIER_NOT_KNOWN);
			setProperty(properties,"BASE_URI",
					ModelException.BASE_URI);
			setProperty(properties,"SCRAPPY_CONNECTION_ERROR",
					ModelException.SCRAPPY_CONNECTION_ERROR);
		} catch (IOException ioe) {
			throw ioe;
		}
	}
	
	public static Integer findLevelTypeByString(String levelType) {
		if(levelType == null){
			return null;
		}
		if(levelType.toLowerCase().contains("discard")) {
			return DISCARD;
		}
		if(levelType.toLowerCase().contains("nothing")) {
			return NOTHING;
		}
		if(levelType.toLowerCase().contains("exception")) {
			return EXCEPTION;
		}
		return DEFAULT;
	}
	
	public static Integer findProcessTypeByString(String processType) {
		if(processType == null){
			return null;
		}
		if(processType.toLowerCase().contains("info")) {
			return ModelException.INFO;
		}
		if(processType.toLowerCase().contains("debug")) {
			return ModelException.DEBUG;
		}
		if(processType.toLowerCase().contains("error")) {
			return ModelException.ERROR;
		}
		if(processType.toLowerCase().contains("warning")) {
			return ModelException.WARNING;
		}
		return null;
	}
	
	private static void setProperty(Properties properties,String propertyName, 
			Integer propertyID) {
		Integer propertyProcess = findProcessTypeByString(properties.getProperty(propertyName));
		if(propertyProcess != null) {
			MODEL_PROPERTIES.put(propertyID, propertyProcess);
		}
	}
	
	private static Map<Integer,Integer> createMap() {
		Map<Integer,Integer> result;
		if(MODEL_PROPERTIES == null) {
			result = new HashMap<Integer,Integer>();
			result.put(ModelException.SCALE, DEFAULT);
			result.put(ModelException.SCALE_SUM, DEFAULT);
			result.put(ModelException.CATEGORIC_SCALE, DEFAULT);
			result.put(ModelException.NUMERIC_SCALE, DEFAULT);
			result.put(ModelException.CATEGORY, DEFAULT);
			result.put(ModelException.TRUSTBETWEENCOMMUNITIES, DEFAULT);
			result.put(ModelException.CATEGORYMATCHING, DEFAULT);
			result.put(ModelException.FIXEDCOMMUNITYTRUST, DEFAULT);
			result.put(ModelException.COMMUNITY, DEFAULT);
			result.put(ModelException.COMMUNITY_ENTITY, DEFAULT);
			result.put(ModelException.COMMUNITY_MODEL, DEFAULT);
			result.put(ModelException.COMMUNITY_METRIC,NOTHING);
			result.put(ModelException.DIMENSION, DEFAULT);
			result.put(ModelException.ENTITY, DEFAULT);
			result.put(ModelException.ONLINEACCOUNT, DEFAULT);
			result.put(ModelException.REPUTATIONOBJECT, DEFAULT);
			result.put(ModelException.REPUTATIONOBJECT_ENTITY, DEFAULT);
			result.put(ModelException.REPUTATIONOBJECT_REPUATIONVALUE, DEFAULT);
			result.put(ModelException.REPUTATIONVALUE, DEFAULT);
			result.put(ModelException.REPUTATIONVALUE_ENTITY, DEFAULT);
			result.put(ModelException.REPUTATIONVALUE_REPUTATIONALGORITHM, DEFAULT);
			result.put(ModelException.REPUTATIONEVALUATION, DEFAULT);
			result.put(ModelException.REPUTATIONEVALUATION_METRIC, DEFAULT);
			result.put(ModelException.REPUTATIONEVALUATION_ENTITY, DEFAULT);
			result.put(ModelException.REPUTATIONEVALUATION_REPUTATIONALGORITHM, DEFAULT);
			result.put(ModelException.METRICTRANSFORMER, DEFAULT);
			result.put(ModelException.METRICTRANSFORMER_IDENTIFIER, NOTHING);
			result.put(ModelException.NUMERICTRANSFORMER, DEFAULT);
			result.put(ModelException.METRIC, DEFAULT);
			result.put(ModelException.REPUTATIONALGORITHM, DEFAULT);
			result.put(ModelException.REPUTATIONALGORITHM_DEFINEDBY, DEFAULT);
			result.put(ModelException.REPUTATIONALGORITHM_METRIC, NOTHING);
			result.put(ModelException.REPUTATIONALGORITHM_METRIC_ERROR, DEFAULT);
			result.put(ModelException.REPUTATIONALGORITHM_BEHAVIOUR, DEFAULT);
			result.put(ModelException.REPUTATIONALGORITHM_USEMETRIC, DEFAULT);
			result.put(ModelException.REPUTATIONALGORITHM_COLLECTION, NOTHING);
			result.put(ModelException.REPUTATIONMODEL, DEFAULT);
			result.put(ModelException.REPUTATIONMODULE, DEFAULT);
			result.put(ModelException.REPUTATIONIMPORTER, DEFAULT);
			result.put(ModelException.REPUTATIONIMPORTER_IMPORTATION, DEFAULT);
			result.put(ModelException.IMPORTATIONUNIT, DEFAULT);
			result.put(ModelException.IMPORTATIONUNIT_METRIC, NOTHING);
			result.put(ModelException.IMPORTATIONUNIT_COMMUNITY,DEFAULT);
			result.put(ModelException.IMPORTATIONUNIT_COLLECTSREPUTATION,DEFAULT);
			result.put(ModelException.IMPORTATIONUNIT_METRICTRANSFORMER,DEFAULT);
			result.put(ModelException.COLLECTINGALGORITHM, DEFAULT);
			result.put(ModelException.COLLECTINGSYSTEM, DEFAULT);
			result.put(ModelException.METRICMAPPING, DEFAULT);
			result.put(ModelException.DIMENSIONCORRELATION, DEFAULT);
			result.put(ModelException.SCALECORRELATION, DEFAULT);
			result.put(ModelException.ENTITYTYPE, DEFAULT);
			result.put(ModelException.FOAFAGENT, DEFAULT);
			result.put(ModelException.GET_MORE_ACCOUNTS, DISCARD);
			result.put(ModelException.EXTRACT_REPUTATION_URL, DEFAULT);
			result.put(ModelException.ALGORITHM_IMPLEMENTATION_NOT_FOUND, DEFAULT);
			result.put(ModelException.ALGORITHM_IMPLEMENTATION_ERROR, NOTHING);
			result.put(ModelException.NOT_URI_KNOWN, DEFAULT);
			result.put(ModelException.NOT_URI_FOUND, DEFAULT);
			result.put(ModelException.URI_NOT_SUPPORTED, DEFAULT);
			result.put(ModelException.ELEMENT_FROM_URI_NOT_FOUND, DEFAULT);
			result.put(ModelException.COLLECTINGSYSTEM_ERROR, DEFAULT);
			result.put(ModelException.SCRAPPY_ERROR, DISCARD);
			result.put(ModelException.OPAL_ERROR, DEFAULT);
			result.put(ModelException.COLLECTINGSYSTEM_NOT_KNOWN, DEFAULT);
			result.put(ModelException.IMPORTATION_UNIT_COLLSYSTEM_INSIDE, DEFAULT);
			result.put(ModelException.SYSTEM_NAME_NOT_EXPECTED, DEFAULT);
			result.put(ModelException.OBJECT_WITH_IDENTIFIER_NOT_KNOWN, DEFAULT);
			result.put(ModelException.BASE_URI, DEFAULT);
			result.put(ModelException.SCRAPPY_CONNECTION_ERROR, DEFAULT);
		} else {
			result = MODEL_PROPERTIES;
		}
		return result;
	}
	
	public static Map<Integer,Integer> getModelProperties() {
		if(MODEL_PROPERTIES == null) {
			MODEL_PROPERTIES = createMap();
		}
		return MODEL_PROPERTIES;
	}
	
	static void initializeModelProperties() {
		if(MODEL_PROPERTIES == null) {
			MODEL_PROPERTIES = createMap();
		}		
	}
	
}
