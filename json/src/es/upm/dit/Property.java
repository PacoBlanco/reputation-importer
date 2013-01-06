package es.upm.dit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import cross.reputation.model.ModelException;

public class Property { // Static

	private static int timeThreshold = 500;
	private static String OPAL_SERVER = "http://localhost/opal.php";
	private static String URL_SERVER = "http://localhost:3434/ejson/";
	public static final int SCRAPPY_EXECUTOR_LINE_COMMAND = 0;
	public static final int SCRAPPY_EXECUTOR_SERVER = 1;
	private static int SCRAPPY_EXECUTOR_TYPE = SCRAPPY_EXECUTOR_LINE_COMMAND;
	private static int THREAD_NUMBER = 1;
	private static int POSTS_NUMBER = 70;
	public static final int XML_BASE = 0;
	public static final int BASE_OR_FILE_PATH = 1;
	public static final int BASE_OR_ABSOLUTE_PATH = 2;
	public static final int EMPTY = 1;
	public static final int FILE_PATH = 2;
	public static final int ABSOLUTE_PATH = 3;	
	private static int BASE_URI_MODE = XML_BASE;
	private static int IMPORTATION_BASE_URI = EMPTY;
	private static String importation_xml_base;
	public static final int REPUTATION = 0;
	public static final int ALL_RESOURCES = 1;
	public static final int INTEGRATION = 2;
	private static int IMPORTATION_MODEL_MODE = REPUTATION;
	private static String model_file_path = "dir/modelWithEntities.rdf";
	private static String importation_model_file_path = "entities.rdf";
	private static Set<String> system_identifier_filter = new HashSet<String>();
	
    public static void setValues() throws Exception {
    	Properties props = new Properties();
        try {
        	props.load(new FileInputStream("property.properties"));

            if(props.getProperty("timeThreshold") != null) {
            	setTimeThreshold(Integer.parseInt(props.getProperty("timeThreshold")));
            }
            if(props.getProperty("OPAL_SERVER") != null) {
            	setOPAL_SERVER(props.getProperty("OPAL_SERVER"));
            }
            if(props.getProperty("URL_SERVER") != null) {
            	setURL_SERVER(props.getProperty("URL_SERVER"));
            }
            if(props.getProperty("THREAD_NUMBER") != null) {
            	setTHREAD_NUMBER(Integer.parseInt(props.getProperty("THREAD_NUMBER")));
            }
            if(props.getProperty("POSTS_NUMBER") != null) {
            	setPOSTS_NUMBER(Integer.parseInt(props.getProperty("POSTS_NUMBER")));
            }
            if(props.getProperty("SCRAPPY_EXECUTOR_TYPE") != null) {
            	if(props.getProperty("SCRAPPY_EXECUTOR_TYPE").equals("SCRAPPY_EXECUTOR_LINE_COMMAND"))
            		setSCRAPPY_EXECUTOR_TYPE(SCRAPPY_EXECUTOR_LINE_COMMAND);
            	if(props.getProperty("SCRAPPY_EXECUTOR_TYPE").equals("SCRAPPY_EXECUTOR_SERVER"))
            		setSCRAPPY_EXECUTOR_TYPE(SCRAPPY_EXECUTOR_SERVER);
            } else {
            	if(System.getProperties().getProperty("os.name").toLowerCase().indexOf("windows") != -1) {
            		setSCRAPPY_EXECUTOR_TYPE(Ejecutor.SCRAPPY_EXECUTOR_SERVER);
        		} else {
        			setSCRAPPY_EXECUTOR_TYPE(Ejecutor.SCRAPPY_EXECUTOR_LINE_COMMAND);
        		}
            }
            if(props.getProperty("base_URI_mode") != null) {
            	if(props.getProperty("base_URI_mode").equalsIgnoreCase(
            			"base_or_file_path")) {
            		setBASE_URI_MODE(BASE_OR_FILE_PATH);
            	}else if(props.getProperty("base_URI_mode").equalsIgnoreCase(
            			"base_or_absolute_path")) {
            		setBASE_URI_MODE(BASE_OR_ABSOLUTE_PATH);
            	}
            }
            if(props.getProperty("importation_base_URI") != null) {
            	if(props.getProperty("importation_base_URI").equalsIgnoreCase(
		    			"xml:base")) {
		    		setBASE_URI_MODE(XML_BASE);
		    		importation_xml_base = props.getProperty("importation_xml_base");
		    	} else if(props.getProperty("importation_base_URI").equalsIgnoreCase(
		    			"file_path")) {
		    		setBASE_URI_MODE(FILE_PATH);
		    	} else if(props.getProperty("importation_base_URI").equalsIgnoreCase(
		    			"absolute_path")) {
		    		setBASE_URI_MODE(ABSOLUTE_PATH);
		    	}
		    }
            if(props.getProperty("importation_model_mode") != null) {
            	if(props.getProperty("importation_model_mode").equalsIgnoreCase(
		    			"all_resources")) {
		    		setIMPORTATION_MODEL_MODE(ALL_RESOURCES);
		    	} else if(props.getProperty("importation_model_mode").equalsIgnoreCase(
		    			"integration")) {
		    		setIMPORTATION_MODEL_MODE(INTEGRATION);
		    	} else {
		    		setIMPORTATION_MODEL_MODE(REPUTATION);
		    	}
		    }
            if(props.getProperty("model_file_path") != null) {
            	setModel_file_path(props.getProperty("model_file_path"));
            	File modelFile = new File(getModel_file_path());
            	ModelException.sendMessage(ModelException.INFO, 
            			"Model file path is:"+modelFile.getAbsolutePath());
            } else {
            	File modelFile = new File(getModel_file_path());
            	ModelException.sendMessage(ModelException.WARNING, 
            			"Not model file path set. Default:"+modelFile.getAbsolutePath());
            }
            if(props.getProperty("importation_model_file_path") != null) {
            	setImportation_model_file_path(props.getProperty("importation_model_file_path"));
            	File modelFile = new File(getImportation_model_file_path());
            	ModelException.sendMessage(ModelException.INFO, 
            			"Reputation Importation Result file path is:"+modelFile.getAbsolutePath());
            } else {
            	File modelFile = new File(getImportation_model_file_path());
            	ModelException.sendMessage(ModelException.WARNING, 
            		"Not importation model file path set. Default:"+modelFile.getAbsolutePath());
            }
            if(props.getProperty("system_identifier_filter") != null) {
            	String systems[] = props.getProperty("system_identifier_filter").split(",");
            	if(!(systems.length == 1 && systems[0].trim().isEmpty())) {
            		system_identifier_filter.addAll(Arrays.asList(systems));
            	}
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    
	public static Set<String> getSystem_identifier_filter() {
		return system_identifier_filter;
	}
	public static void setSystem_identifier_filter(
			Set<String> system_identifier_filter) {
		Property.system_identifier_filter = system_identifier_filter;
	}
	public static String getModel_file_path() {
		return model_file_path;
	}
	public static void setModel_file_path(String model_file_path) {
		Property.model_file_path = model_file_path;
	}
	public static String getImportation_model_file_path() {
		return importation_model_file_path;
	}
	public static void setImportation_model_file_path(
			String importation_model_file_path) {
		Property.importation_model_file_path = importation_model_file_path;
	}
	public static int getIMPORTATION_MODEL_MODE() {
		return IMPORTATION_MODEL_MODE;
	}
	public static void setIMPORTATION_MODEL_MODE(int importation_model_mode) {
		IMPORTATION_MODEL_MODE = importation_model_mode;
	}
	public static String getImportation_xml_base() {
		return importation_xml_base;
	}
	public static void setImportation_xml_base(String importation_xml_base) {
		Property.importation_xml_base = importation_xml_base;
	}
	public static int getBASE_URI_MODE() {
		return BASE_URI_MODE;
	}
	public static void setBASE_URI_MODE(int base_uri_mode) {
		BASE_URI_MODE = base_uri_mode;
	}
	public static int getIMPORTATION_BASE_URI() {
		return IMPORTATION_BASE_URI;
	}
	public static void setIMPORTATION_BASE_URI(int importation_base_uri) {
		IMPORTATION_BASE_URI = importation_base_uri;
	}
	public static void setTimeThreshold(int time) {
		timeThreshold = time;
	}

	public static int getTimeThreshold() {
		return timeThreshold;
	}

	public static void setOPAL_SERVER(String oPAL_SERVER) {
		OPAL_SERVER = oPAL_SERVER;
	}

	public static String getOPAL_SERVER() {
		return OPAL_SERVER;
	}

	public static void setURL_SERVER(String uRL_SERVER) {
		URL_SERVER = uRL_SERVER;
	}

	public static String getURL_SERVER() {
		return URL_SERVER;
	}

	public static void setSCRAPPY_EXECUTOR_TYPE(int sCRAPPY_EXECUTOR_TYPE) {
		SCRAPPY_EXECUTOR_TYPE = sCRAPPY_EXECUTOR_TYPE;
	}

	public static int getSCRAPPY_EXECUTOR_TYPE() {
		return SCRAPPY_EXECUTOR_TYPE;
	}

	public static void setTHREAD_NUMBER(int tHREAD_NUMBER) {
		THREAD_NUMBER = tHREAD_NUMBER;
	}

	public static int getTHREAD_NUMBER() {
		return THREAD_NUMBER;
	}

	public static void setPOSTS_NUMBER(int pOSTS_NUMBER) {
		POSTS_NUMBER = pOSTS_NUMBER;
	}

	public static int getPOSTS_NUMBER() {
		return POSTS_NUMBER;
	}    
    
}
