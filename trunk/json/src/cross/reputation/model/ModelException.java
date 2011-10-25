package cross.reputation.model;

public class ModelException {
	public final static int SCALE = 2800;
	public final static int SCALE_SUM = 2801;
	public final static int CATEGORIC_SCALE = 100;
	public final static int NUMERIC_SCALE = 200;
	public final static int CATEGORY = 300;
	public final static int TRUSTBETWEENCOMMUNITIES = 400;
	public final static int CATEGORYMATCHING = 500;
	public final static int FIXEDCOMMUNITYTRUST = 600;
	public final static int COMMUNITY = 700;
	public final static int COMMUNITY_ENTITY = 701;
	public final static int COMMUNITY_MODEL = 702;
	public final static int COMMUNITY_METRIC = 703;
	public final static int DIMENSION = 800;
	public final static int ENTITY = 900;
	public final static int ONLINEACCOUNT = 1000;
	public final static int REPUTATIONOBJECT = 1100;
	public final static int REPUTATIONOBJECT_ENTITY = 1101;
	public final static int REPUTATIONVALUE = 1200;
	public final static int REPUTATIONVALUE_ENTITY = 1201;
	public final static int REPUTATIONVALUE_REPUTATIONALGORITHM = 1202;
	public final static int REPUTATIONEVALUATION = 1300;
	public final static int REPUTATIONEVALUATION_METRIC = 1301;
	public final static int REPUTATIONEVALUATION_ENTITY = 1302;
	public final static int REPUTATIONEVALUATION_REPUTATIONALGORITHM = 1303;
	public final static int METRICTRANSFORMER = 1400;
	public final static int METRICTRANSFORMER_IDENTIFIER = 1401;
	public final static int NUMERICTRANSFORMER = 3000;
	//static int METRICTRANSFORMER_METRIC = 1401;
	public final static int METRIC = 1500;
	public final static int REPUTATIONALGORITHM = 1600;
	public final static int REPUTATIONALGORITHM_DEFINEDBY = 1601;
	public final static int REPUTATIONALGORITHM_METRIC = 1602;
	public final static int REPUTATIONALGORITHM_BEHAVIOUR = 1603;
	public final static int REPUTATIONALGORITHM_USEMETRIC = 1604;
	public final static int REPUTATIONALGORITHM_COLLECTION = 1605;
	public final static int REPUTATIONMODEL = 1700;
	public final static int REPUTATIONMODULE = 1800;
	public final static int REPUTATIONIMPORTER = 1900;
	public final static int REPUTATIONIMPORTER_IMPORTATION = 1901;
	public final static int IMPORTATIONUNIT = 2000;
	public final static int IMPORTATIONUNIT_METRIC = 2001;
	public final static int REPUTATIONALGORITHM_METRIC_ERROR = 2002;
	public final static int IMPORTATIONUNIT_COMMUNITY = 2003;
	public final static int IMPORTATIONUNIT_COLLECTSREPUTATION = 2004;
	public final static int COLLECTINGALGORITHM = 2100;
	public final static int REPUTATIONALACTION = 2200;
	public final static int COLLECTINGSYSTEM = 2300;
	public final static int METRICMAPPING = 2400;
	public final static int DIMENSIONCORRELATION = 2500;
	public final static int SCALECORRELATION = 2600;
	public final static int ENTITYTYPE = 2700;
	public final static int FOAFAGENT = 2900;
	
	
	public final static int GET_MORE_ACCOUNTS = 10000;
	public final static int EXTRACT_REPUTATION_URL = 10100;
	public final static int ALGORITHM_IMPLEMENTATION_NOT_FOUND = 10200;
	public final static int NOT_URI_KNOWN = 10300;
	public final static int NOT_URI_FOUND = 10301;
	public final static int URI_NOT_SUPPORTED = 10302;
	public final static int ELEMENT_FROM_URI_NOT_FOUND = 10303;
	public final static int COLLECTINGSYSTEM_ERROR = 10400;
	public final static int SCRAPPY_ERROR = 10401;
	public final static int OPAL_ERROR = 10402;
	public final static int COLLECTINGSYSTEM_NOT_KNOWN = 10404;
	public final static int IMPORTATION_UNIT_COLLSYSTEM_INSIDE = 10500;
	public final static int SYSTEM_NAME_NOT_EXPECTED = 10501;
	public final static int OBJECT_WITH_IDENTIFIER_NOT_KNOWN = 10600;
	public final static int BASE_URI = 10700;
	
	
	public final static int DEBUG = 0;
	public final static int INFO = 1;
	public final static int WARNING = 2;
	public final static int ERROR = 3;
	
	static public boolean throwException(int exceptionType, String message) throws Exception {
		Integer type = ModelProperties.getModelProperties().get(exceptionType);
		if(type == ModelProperties.DISCARD) {
			sendMessage(WARNING,"DISCARD OPTION:"+message);
			return false;
		} else if(type == ModelProperties.NOTHING) {
			sendMessage(WARNING,message);
		} else if(type == ModelProperties.EXCEPTION) {
			throw new Exception(message);
		}
		return true;
	}
	
	static public void sendMessage(int level, String message) throws Exception {
		if(level >= ModelProperties.DEAFULT_TO_LOG) {
			String levelString = levelIntegerToString(level);
			System.out.println(levelString+":"+message);
		}
	}
	
	static public String levelIntegerToString(Integer level) throws Exception {
		if(level == INFO) {
			return "INFO";
		} else if(level == DEBUG) {
			return "DEBUG";
		} else if(level == ERROR) {
			return "ERROR";
		} else if(level == WARNING) {
			return "WARNING";
		}
		throw new Exception("System bug: level not correspond to any predefined type: "+level);
	}
}
