package es.upm.dit;

import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import es.upm.dit.Property;

public class Ejecutor{
	
	static final public int SCRAPPY_EXECUTOR_LINE_COMMAND = 0;
	static final public int SCRAPPY_EXECUTOR_SERVER = 1;	
    static public int SCRAPPY_EXECUTOR_TYPE = Property.getSCRAPPY_EXECUTOR_TYPE();
    static public String URL_SERVER = Property.getURL_SERVER();
    static public String OPAL_SERVER = Property.getOPAL_SERVER();
    static public boolean stderrExit = false;
		
    //Esta funcion devuelve un proceso que es la ejecucion del comando
    //que se le pasa como argumento con el nombre c
	static private Process comando(String[] com) throws IOException{
        //return Runtime.getRuntime().exec(com);
    	/*for (Object property : System.getProperties().keySet()) {
    		System.out.println("Property: "+property+" value:"+System.getProperties().get(property));
    	}*/
    	List<String> commandList = new ArrayList<String>(Arrays.asList(com));
    	
    	if(System.getProperties().getProperty("os.name").toLowerCase().indexOf("windows") != -1) {
    		commandList.add(0,"scrappy.bat");    		
    	}
    	else {
    		//comand = "/var/lib/gems/1.8/gems/scrappy-0.1.9/bin/scrappy"
    		commandList.add(0,"scrappy");
    	}
    	String execute = "Execute: ";
    	for(int i = 0; i < commandList.size(); i++) {
    		execute += commandList.get(i)+" ";
    	}
    	System.out.println(execute);
    	Process p = Runtime.getRuntime().exec(commandList.toArray(new String[commandList.size()]));
		/*try {
			p.waitFor();
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		*/	
		return p;
    }
    
    //Un lector de la salida que provoca el comando
    static private BufferedReader salidaComando(Process p){
    	if(stderrExit) {
	    	BufferedReader brCleanUp = 
	            new BufferedReader (new InputStreamReader (p.getErrorStream()));
	    	String line;
	    	try {
	    		while ((line = brCleanUp.readLine ()) != null) {
		            System.out.println ("[Stderr] " + line);
		        }        
				brCleanUp.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
		return new BufferedReader(new InputStreamReader(p.getInputStream()));
    }
    
    //Tranforma los Buffers en texto legible
    static private String leerBufer(BufferedReader b) throws IOException{
    	String aux = "", aux2 = "";
        while( (aux2 = b.readLine()) != null ){
        	//System.out.println("Line:"+aux2);
            aux+=String.format("%s\n",aux2);
        }
        b.close();
        return aux;
    }
    
    static public String ejecuta_opal(String text) throws IOException{
    	text = URLEncoder.encode(text,"UTF-8");
    	String[] com = {"curl","-d", "text="+text+"&translation=0", OPAL_SERVER};
    	System.out.print("Execute:");
    	for(int i = 0; i < com.length; i++) {
    		System.out.print("\""+com[i]+"\" ");
    	}
    	System.out.println();
        Process p = Runtime.getRuntime().exec(com);
        return Ejecutor.leerBufer(Ejecutor.salidaComando(p));
    }
    /*
     * Ejecuta el programa scrappy con la url dada y el tipo (-l 0, -l 1)
     */
    static public String executeScrappy (String texto, String tipo) throws IOException{
    	if(SCRAPPY_EXECUTOR_TYPE == SCRAPPY_EXECUTOR_LINE_COMMAND) {
	    	String[] com = {"-f","ejson","-g",texto, "-l", tipo};
	    	Process p = Ejecutor.comando(com);
	    	return leerBufer(salidaComando(p));
    	} else {
    		System.out.println("\nExecute URL:"+URL_SERVER+URLEncoder.encode(texto,"UTF-8"));
    		Web file = new Web(URL_SERVER+URLEncoder.encode(texto),"UTF-8");
    		return file.getContent().toString();
    	}
    }
    			
    /*static public String executeScrappy (String urlServer, String texto, 
    		String tipo) throws IOException{	
    	Web file   = new Web(urlServer+URLEncoder.encode(texto),"UTF-8");
		return file.getContent().toString();
    }*/

    static public void ConfigureExtractorMode(int mode, String urlServer) {
    	SCRAPPY_EXECUTOR_TYPE = mode;
    	if(mode == SCRAPPY_EXECUTOR_SERVER && urlServer != null) {
    		URL_SERVER = urlServer;
    	}
    }
    
    static public void ConfigureOpalServer(String opalServer) {
    	OPAL_SERVER = opalServer;
    }
    
	public static int getSCRAPPY_EXECUTOR_TYPE() {
		return SCRAPPY_EXECUTOR_TYPE;
	}

	public static void setSCRAPPY_EXECUTOR_TYPE(int scrappy_executor_type) {
		SCRAPPY_EXECUTOR_TYPE = scrappy_executor_type;
	}

	public static String getURL_SERVER() {
		return URL_SERVER;
	}

	public static void setURL_SERVER(String url_server) {
		URL_SERVER = url_server;
	}   
    
}


