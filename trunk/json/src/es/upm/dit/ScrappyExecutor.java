package es.upm.dit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScrappyExecutor extends Thread implements Executor {
	
	public String execute(String texto) throws IOException{
		String type = "0";
    	if(Ejecutor.SCRAPPY_EXECUTOR_TYPE == Ejecutor.SCRAPPY_EXECUTOR_LINE_COMMAND) {
	    	String[] com = {"-f","ejson","-g",texto, "-l", type};
	    	Process p = command(com);
	    	return readBuffer(outputFromCommand(p));
    	} else {
    		System.out.println("\nExecute URL:"+ Ejecutor.URL_SERVER +
    				URLEncoder.encode(texto,"UTF-8"));
    		Web file = new Web(Ejecutor.URL_SERVER+URLEncoder.encode(texto),"UTF-8");
    		return file.getContent().toString();
    	}
    }
	
	static public String executeScrappy (String texto, String tipo) throws IOException{
    	if(Ejecutor.SCRAPPY_EXECUTOR_TYPE == Ejecutor.SCRAPPY_EXECUTOR_LINE_COMMAND) {
	    	String[] com = {"-f","ejson","-g",texto, "-l", tipo};
	    	Process p = command(com);
	    	return readBuffer(outputFromCommand(p));
    	} else {
    		System.out.println("\nExecute URL:"+ Ejecutor.URL_SERVER +
    				URLEncoder.encode(texto,"UTF-8"));
    		Web file = new Web(Ejecutor.URL_SERVER+URLEncoder.encode(texto),"UTF-8");
    		return file.getContent().toString();
    	}
    }
	
	//Esta funcion devuelve un proceso que es la ejecucion del comando
    //que se le pasa como argumento con el nombre c
	static private Process command(String[] com) throws IOException{
        List<String> commandList = new ArrayList<String>(Arrays.asList(com));
    	
    	if(System.getProperties().getProperty("os.name").toLowerCase().indexOf("windows") != -1) {
    		commandList.add(0,"scrappy.bat");    		
    	}
    	else {
    		commandList.add(0,"scrappy");
    	}
    	String execute = "Execute: ";
    	for(int i = 0; i < commandList.size(); i++) {
    		execute += commandList.get(i)+" ";
    	}
    	System.out.println(execute);
    	Process p = Runtime.getRuntime().exec(commandList.toArray(new String[commandList.size()]));
		return p;
    }
    
    //Un lector de la salida que provoca el comando
    static private BufferedReader outputFromCommand(Process p){
    	if(Ejecutor.stderrExit) {
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
    static private String readBuffer(BufferedReader b) throws IOException{
    	String aux = "", aux2 = "";
        while( (aux2 = b.readLine()) != null ){
        	//System.out.println("Line:"+aux2);
            aux+=String.format("%s\n",aux2);
        }
        b.close();
        return aux;
    }

}
