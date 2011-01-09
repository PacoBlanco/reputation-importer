package json;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class Ejecutor{
    
	public Ejecutor(){
		super();
	}

    //Esta funcion devuelve un proceso que es la ejecucion del comando
    //que se le pasa como argumento con el nombre c
    private Process comando(String[] com) throws IOException{
        return Runtime.getRuntime().exec(com);
    }
    
    
    //Un lector de la salida que provoca el comando
    private BufferedReader salidaComando(Process p){
        return new BufferedReader(new InputStreamReader(p.getInputStream()));
    }
    
    //Tranforma los Buffers en texto legible
    private String leerBufer(BufferedReader b) throws IOException{
        String aux = "", aux2 = "";
        while( (aux2 = b.readLine()) != null ){
            aux+=String.format("%s\n",aux2);
        }
        return aux;
    }
    
    public String ejecuta_opal(String texto) throws IOException{
    	Ejecutor ejecutor = new Ejecutor();
    	String[] com = {"curl","-d", "text="+texto+"&translation=0", "http://localhost/opal.php"};
        Process p = ejecutor.comando(com);
        return ejecutor.leerBufer(ejecutor.salidaComando(p));
    }
    /*
     * Ejecuta el programa scrappy con la url dada y el tipo (-l 0, -l 1)
     */
    public String ejecuta_scrappy (String texto, String tipo) throws IOException{
    	Ejecutor ejecutor = new Ejecutor();
    	String[] com = {"/var/lib/gems/1.8/gems/scrappy-0.1.9/bin/scrappy","-f","ejson","-g",texto, "-l", tipo};
    	Process p = ejecutor.comando(com);
    	return ejecutor.leerBufer(ejecutor.salidaComando(p));
    }
}


