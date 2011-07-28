package es.upm.dit;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Property { // Static

	private static int timeThreshold = 500;
	private static String OPAL_SERVER = "http://localhost/opal.php";
	private static String URL_SERVER = "http://localhost:3434/ejson/";
	private static final int SCRAPPY_EXECUTOR_LINE_COMMAND = 0;
	private static final int SCRAPPY_EXECUTOR_SERVER = 1;
	private static int SCRAPPY_EXECUTOR_TYPE = SCRAPPY_EXECUTOR_LINE_COMMAND;
	private static int THREAD_NUMBER = 1;
	private static int POSTS_NUMBER = 70;
	
	
    public static void setValues() {

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
        }
        catch(IOException e) {
            e.printStackTrace();
        }
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
