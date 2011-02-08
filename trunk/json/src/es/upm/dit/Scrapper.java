package es.upm.dit;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;


public class Scrapper extends Thread{
	
	private static String NombreUsuario = "";
	//private static String ApellidoUsuario = "";
	private static String ApellidoUsuario_Espacio = "";
	
	
	public Scrapper(String str){
		super(str);
	}
	
		
	/*
	 * Metodo auxiliar que calcula la informacion de los comentarios
	 */
	private static void Comentarios(JSONObject objeto, String usuario) throws IOException{		
		if (objeto.has("http://purl.org/dc/elements/1.1/Comentarios")){
			
			//Parseamos la informacion de los comentarios, que es un objeto de objeto_principal que tiene un array, array_comentarios
	        JSONArray array_comentarios = objeto.getJSONArray("http://purl.org/dc/elements/1.1/Comentarios");
	        
	        //array_comentarios tiene a su vez n objetos, pero sólo nos interesa InformacionComentarios
	        Object[] nuevo_array = array_comentarios.toArray();
	        String texto = "InformacionComentarios";
	        
	        for(int i=0;i<nuevo_array.length;i++){
	        	
	        	String comparacion = nuevo_array[i].toString();
	        	if (comparacion.regionMatches(34, texto, 0, 22)){
	        		
	                JSONObject objeto_informacion = array_comentarios.getJSONObject(i);
	                JSONArray array_informacion_comentarios = objeto_informacion.getJSONArray("http://purl.org/dc/elements/1.1/InformacionComentarios");
	                
	            	for(int j=0;j<array_informacion_comentarios.size();j++){
	                	JSONObject objeto_informacion_comentarios = array_informacion_comentarios.getJSONObject(j);
	                	//Si usuario ha creado un comentario, se saca informacion de dicho comentario
	                	if (objeto_informacion_comentarios.has("http://purl.org/dc/elements/1.1/ComentadoPor") && (usuario.isEmpty() == false) &&
	                			objeto_informacion_comentarios.getJSONArray("http://purl.org/dc/elements/1.1/ComentadoPor")
	                				.getString(0).equals(usuario)){
	                		System.out.println("Comentarios por " + usuario);
			                //Vamos sacando la información relevante, que son objetos, con un array, dentro de objeto_informacion_comentarios
		                	if (objeto_informacion_comentarios.has("http://purl.org/dc/elements/1.1/ComentadoPor")){
				                JSONArray array_comentado_por = objeto_informacion_comentarios.getJSONArray
				                	("http://purl.org/dc/elements/1.1/ComentadoPor");
				                String ComentadoPor = array_comentado_por.getString(0);
				                System.out.println("  Comentado por: " + ComentadoPor);
		                	}
		                	if (objeto_informacion_comentarios.has("http://purl.org/dc/elements/1.1/Fecha")){
				                JSONArray array_fecha_comentarios = objeto_informacion_comentarios.getJSONArray("http://purl.org/dc/elements/1.1/Fecha");
				                String FechaComentarios = array_fecha_comentarios.getString(0);
				                System.out.println("    Fecha: " + FechaComentarios);
		                	}
		                	if (objeto_informacion_comentarios.has("http://purl.org/dc/elements/1.1/ReputacionInternaComentarios")){
			                	JSONArray array_reputacion_comentarios = objeto_informacion_comentarios.getJSONArray
			                		("http://purl.org/dc/elements/1.1/ReputacionInternaComentarios");
			                	String ReputacionComentarios = array_reputacion_comentarios.getString(0);
			                	System.out.println("    Reput interna comentarista: " + ReputacionComentarios);
			                }
		                	if (objeto_informacion_comentarios.has("http://purl.org/dc/elements/1.1/TextoComentarios")){
				                JSONArray array_texto_comentarios = objeto_informacion_comentarios.getJSONArray
				                	("http://purl.org/dc/elements/1.1/TextoComentarios");
				                String textoComentarios = array_texto_comentarios.getString(0);
				                //System.out.println("  Texto del comentario: " + textoComentarios);
				                String opal_xml = Ejecutor.ejecuta_opal(textoComentarios);
				        		System.out.println(opal_xml);
		                	}  
	                	}
	                	//Si no hay usuario, se sacan los comentarios de todos los usuarios
	                	if (objeto_informacion_comentarios.has("http://purl.org/dc/elements/1.1/ComentadoPor") && (usuario.isEmpty() == true)){
	                		System.out.println("Comentarios:");
			                //Vamos sacando la información relevante, que son objetos, con un array, dentro de objeto_informacion_comentarios
		                	if (objeto_informacion_comentarios.has("http://purl.org/dc/elements/1.1/ComentadoPor")){
				                JSONArray array_comentado_por = objeto_informacion_comentarios.getJSONArray
				                	("http://purl.org/dc/elements/1.1/ComentadoPor");
				                String ComentadoPor = array_comentado_por.getString(0);
				                System.out.println("  Comentado por: " + ComentadoPor);
		                	}
		                	if (objeto_informacion_comentarios.has("http://purl.org/dc/elements/1.1/Fecha")){
				                JSONArray array_fecha_comentarios = objeto_informacion_comentarios.getJSONArray("http://purl.org/dc/elements/1.1/Fecha");
				                String FechaComentarios = array_fecha_comentarios.getString(0);
				                System.out.println("    Fecha: " + FechaComentarios);
		                	}
		                	if (objeto_informacion_comentarios.has("http://purl.org/dc/elements/1.1/ReputacionInternaComentarios")){
			                	JSONArray array_reputacion_comentarios = objeto_informacion_comentarios.getJSONArray
			                		("http://purl.org/dc/elements/1.1/ReputacionInternaComentarios");
			                	String ReputacionComentarios = array_reputacion_comentarios.getString(0);
			                	System.out.println("    Reput interna comentarista: " + ReputacionComentarios);
			                }
		                	if (objeto_informacion_comentarios.has("http://purl.org/dc/elements/1.1/TextoComentarios")){
				                JSONArray array_texto_comentarios = objeto_informacion_comentarios.getJSONArray
				                	("http://purl.org/dc/elements/1.1/TextoComentarios");
				                String textoComentarios = array_texto_comentarios.getString(0);
				                //System.out.println("\n    Texto del comentario: " + textoComentarios);
				                //opal_parser(textoComentarios);
				                String opal_xml = Ejecutor.ejecuta_opal(textoComentarios);
				        		System.out.println(opal_xml);				                
		                	}  
	                	}
	            	} 	
	        	}
	        } 
		}
	}
	
	/*
	 * Metodo auxiliar que calcula la informacion de la pregunta
	 */
	private static void Pregunta(JSONObject objeto, String usuario) throws IOException{		
		//Sacamos el tiulo y la url de la pregunta
		if (objeto.has("http://purl.org/dc/elements/1.1/Pregunta")){
			String url = objeto.getString("id");
			//Parseamos la información de la pregunta, que es un objeto de objeto_principal que tiene un array, array_pregunta
	        JSONArray array_pregunta = objeto.getJSONArray("http://purl.org/dc/elements/1.1/Pregunta");      
	        JSONObject objeto_pregunta = array_pregunta.getJSONObject(0);
	        //Si usuario ha creado una pregunta, se obtiene informacion de dicha pregunta
	        if (objeto_pregunta.has("http://purl.org/dc/elements/1.1/Creador") && (usuario.isEmpty() == false) 
	        		&& objeto_pregunta.getJSONArray("http://purl.org/dc/elements/1.1/Creador").getString(0).equals(usuario)){
	        	System.out.println("Preguntas por " + usuario);
	    		System.out.println("  URL: " + url);
		        //Vamos sacando la información relevante, que son objetos, con un array, dentro de objeto_pregunta
		        if (objeto_pregunta.has("http://purl.org/dc/elements/1.1/TituloPregunta")){
			        JSONArray array_titulo = objeto_pregunta.getJSONArray("http://purl.org/dc/elements/1.1/TituloPregunta");
			        String Titulo = array_titulo.getString(0);
			        //System.out.println("\n  Titulo: " + Titulo);
		        }
		        if (objeto_pregunta.has("http://purl.org/dc/elements/1.1/Creador")){
			        JSONArray array_creador = objeto_pregunta.getJSONArray("http://purl.org/dc/elements/1.1/Creador");
			        String Creador = array_creador.getString(0);
			        System.out.println("  Creador: " + Creador);
		        }
		        if (objeto_pregunta.has("http://purl.org/dc/elements/1.1/Fecha")){
			        JSONArray array_fecha = objeto_pregunta.getJSONArray("http://purl.org/dc/elements/1.1/Fecha");
			        String FechaPregunta = array_fecha.getString(0);
			        System.out.println("  Fecha: " + FechaPregunta);
		        }
		        if (objeto_pregunta.has("http://purl.org/dc/elements/1.1/ReputacionInternaCreador")){
			        JSONArray array_reputacion_creador = objeto_pregunta.getJSONArray
			        	("http://purl.org/dc/elements/1.1/ReputacionInternaCreador");
			        String ReputacionCreador = array_reputacion_creador.getString(0);
			        System.out.println("  Reput interna creador: " + ReputacionCreador);
		        }		        
		        //System.out.println("\nTexto de la pregunta:");
		        if(objeto_pregunta.has("http://purl.org/dc/elements/1.1/TextoPregunta")){
			        JSONArray array_texto_pregunta = objeto_pregunta.getJSONArray("http://purl.org/dc/elements/1.1/TextoPregunta");
			        for(int i=0;i<array_texto_pregunta.size();i++){			        		
		        		String TextoPregunta = array_texto_pregunta.getString(i);
		            	//System.out.println(TextoPregunta);
		        	}
		        }
		        Comentarios(objeto_pregunta,"");
	        }
	        //Si no hay usuario, se sacan todas las preguntas
	        if (objeto_pregunta.has("http://purl.org/dc/elements/1.1/Creador") && (usuario.isEmpty() == true)){
	        	System.out.println("Pregunta:");
	    		System.out.println("  URL: " + url);
		        //Vamos sacando la información relevante, que son objetos, con un array, dentro de objeto_pregunta
		        if (objeto_pregunta.has("http://purl.org/dc/elements/1.1/TituloPregunta")){
			        JSONArray array_titulo = objeto_pregunta.getJSONArray("http://purl.org/dc/elements/1.1/TituloPregunta");
			        String Titulo = array_titulo.getString(0);
			        //System.out.println("\n  Titulo: " + Titulo);
		        }
		        if (objeto_pregunta.has("http://purl.org/dc/elements/1.1/Creador")){
			        JSONArray array_creador = objeto_pregunta.getJSONArray("http://purl.org/dc/elements/1.1/Creador");
			        String Creador = array_creador.getString(0);
			        System.out.println("  Creador: " + Creador);
		        }
		        if (objeto_pregunta.has("http://purl.org/dc/elements/1.1/Fecha")){
			        JSONArray array_fecha = objeto_pregunta.getJSONArray("http://purl.org/dc/elements/1.1/Fecha");
			        String FechaPregunta = array_fecha.getString(0);
			        System.out.println("  Fecha: " + FechaPregunta);
		        }
		        if (objeto_pregunta.has("http://purl.org/dc/elements/1.1/ReputacionInternaCreador")){
			        JSONArray array_reputacion_creador = objeto_pregunta.getJSONArray
			        	("http://purl.org/dc/elements/1.1/ReputacionInternaCreador");
			        String ReputacionCreador = array_reputacion_creador.getString(0);
			        System.out.println("  Reput interna creador: " + ReputacionCreador);
		        }		        
		        //System.out.println("\nTexto de la pregunta:");
		        if(objeto_pregunta.has("http://purl.org/dc/elements/1.1/TextoPregunta")){
			        JSONArray array_texto_pregunta = objeto_pregunta.getJSONArray("http://purl.org/dc/elements/1.1/TextoPregunta");
			        for(int i=0;i<array_texto_pregunta.size();i++){			        		
		        		String TextoPregunta = array_texto_pregunta.getString(i);
		            	//System.out.println(TextoPregunta);
		        	}
		        }
		        Comentarios(objeto_pregunta,"");
	        }
		}
	}
	
	/*
	 * Método auxiliar que calcula la información de las respuestas
	 */
	private static void Respuestas(JSONObject objeto, String usuario) throws IOException{
		
		//Parseamos la información de las respuestass, que es un objeto de objeto_principal que tiene un array, array_respuestas
		if(objeto.has("http://purl.org/dc/elements/1.1/Respuestas")){
			
	        JSONArray array_respuestas = objeto.getJSONArray("http://purl.org/dc/elements/1.1/Respuestas"); 
	         
	        for(int k=0;k<array_respuestas.size();k++){
		        JSONObject objeto_respuestas = array_respuestas.getJSONObject(k);
		        //Si usuario ha escrito alguna respuesta, se saca informacion de dicha respuesta
		        if (objeto_respuestas.has("http://purl.org/dc/elements/1.1/RespondidoPor") && usuario.isEmpty() == false
		        		&& objeto_respuestas.getJSONArray("http://purl.org/dc/elements/1.1/RespondidoPor").getString(0).equals(usuario)){
		        	System.out.println("Respuestas por " + usuario + "\n");
			        //Vamos sacando la información relevante, que son objetos, con un array, dentro de objeto_respuestas
			        if(objeto_respuestas.has("http://purl.org/dc/elements/1.1/RespondidoPor")){
				        JSONArray array_respondido_por = objeto_respuestas.getJSONArray("http://purl.org/dc/elements/1.1/RespondidoPor");
				        String RespondidoPor = array_respondido_por.getString(0);
				        System.out.println("  Respondido por: " + RespondidoPor);
			        }
			        if((objeto_respuestas.has("http://purl.org/dc/elements/1.1/RespondidoPor") == false) && 
			        		(objeto_respuestas.has("http://purl.org/dc/elements/1.1/RespondidoPor_SinPerfil") == true)){
			        	JSONArray array_respondido_por_sinperfil = objeto_respuestas.getJSONArray("http://purl.org/dc/elements/1.1/RespondidoPor_SinPerfil");
				        String RespondidoPor = array_respondido_por_sinperfil.getString(0);
				        System.out.println("  Respondido por sin perfil: " + RespondidoPor);
			        }
			        if(objeto_respuestas.has("http://purl.org/dc/elements/1.1/Fecha")){
				        JSONArray array_fecha = objeto_respuestas.getJSONArray("http://purl.org/dc/elements/1.1/Fecha");
				        String FechaRespuestas = array_fecha.getString(0); 
				        System.out.println("  Fecha: " + FechaRespuestas);
			        }
			        if(objeto_respuestas.has("http://purl.org/dc/elements/1.1/ReputacionInternaResponden")){
				        JSONArray array_reputacion_responden = objeto_respuestas.getJSONArray
				        	("http://purl.org/dc/elements/1.1/ReputacionInternaResponden");
				        String ReputacionResponden = array_reputacion_responden.getString(0);
				        System.out.println("  Reput interna del que responde: " + ReputacionResponden);
			        }
			        if(objeto_respuestas.has("http://purl.org/dc/elements/1.1/ReputacionInternaResponden") == false){
			        	System.out.println("  Reput interna del que responde: No tiene");
			        }
			        if(objeto_respuestas.has("http://purl.org/dc/elements/1.1/FlagPositivo")){
			        	JSONArray array_flag = objeto_respuestas.getJSONArray("http://purl.org/dc/elements/1.1/FlagPositivo");
				        String FlagPositivo = array_flag.getString(0); 
			        	System.out.println("  Flag positivo: " + FlagPositivo);
			        }
			        if(objeto_respuestas.has("http://purl.org/dc/elements/1.1/TextoRespuesta")){
				        //System.out.println("\nTexto de la respuesta:");
				        JSONArray array_texto_respuestas = objeto_respuestas.getJSONArray("http://purl.org/dc/elements/1.1/TextoRespuesta");
				        String textoRespuesta = "";
				        for(int i=0;i<array_texto_respuestas.size();i++){				        	
			        		String str = array_texto_respuestas.getString(i);
			        		textoRespuesta += "\n" + str;
			        	}  
				        //System.out.println(TextoRespuesta);
				        //opal_parser(textoRespuesta);
				        String opal_xml = Ejecutor.ejecuta_opal(textoRespuesta);
		        		System.out.println(opal_xml);
			        }
			        Comentarios(objeto_respuestas,"");
		        }
		        //Si no hay usuario, se sacan todas las respuestas
		        if (objeto_respuestas.has("http://purl.org/dc/elements/1.1/RespondidoPor") && usuario.isEmpty() == true){
		        	System.out.println("Respuestas:\n");
			        //Vamos sacando la información relevante, que son objetos, con un array, dentro de objeto_respuestas
			        if(objeto_respuestas.has("http://purl.org/dc/elements/1.1/RespondidoPor")){
				        JSONArray array_respondido_por = objeto_respuestas.getJSONArray("http://purl.org/dc/elements/1.1/RespondidoPor");
				        String RespondidoPor = array_respondido_por.getString(0);
				        System.out.println("  Respondido por: " + RespondidoPor);
			        }
			        if((objeto_respuestas.has("http://purl.org/dc/elements/1.1/RespondidoPor") == false) && 
			        		(objeto_respuestas.has("http://purl.org/dc/elements/1.1/RespondidoPor_SinPerfil") == true)){
			        	JSONArray array_respondido_por_sinperfil = objeto_respuestas.getJSONArray("http://purl.org/dc/elements/1.1/RespondidoPor_SinPerfil");
				        String RespondidoPor = array_respondido_por_sinperfil.getString(0);
				        System.out.println("  Respondido por: " + RespondidoPor);
			        }
			        if(objeto_respuestas.has("http://purl.org/dc/elements/1.1/Fecha")){
				        JSONArray array_fecha = objeto_respuestas.getJSONArray("http://purl.org/dc/elements/1.1/Fecha");
				        String FechaRespuestas = array_fecha.getString(0); 
				        System.out.println("  Fecha: " + FechaRespuestas);
			        }
			        if(objeto_respuestas.has("http://purl.org/dc/elements/1.1/ReputacionInternaResponden")){
				        JSONArray array_reputacion_responden = objeto_respuestas.getJSONArray
				        	("http://purl.org/dc/elements/1.1/ReputacionInternaResponden");
				        String ReputacionResponden = array_reputacion_responden.getString(0);
				        System.out.println("  Reput interna del que responde: " + ReputacionResponden);
			        }
			        if(objeto_respuestas.has("http://purl.org/dc/elements/1.1/ReputacionInternaResponden") == false){
			        	System.out.println("  Reput interna del que responde: No tiene");
			        }
			        if(objeto_respuestas.has("http://purl.org/dc/elements/1.1/FlagPositivo")){
			        	JSONArray array_flag = objeto_respuestas.getJSONArray("http://purl.org/dc/elements/1.1/FlagPositivo");
				        String FlagPositivo = array_flag.getString(0); 
			        	System.out.println("  Flag positivo: " + FlagPositivo);
			        }
			        if(objeto_respuestas.has("http://purl.org/dc/elements/1.1/TextoRespuesta")){
				        //System.out.println("\nTexto de la respuesta:");
				        JSONArray array_texto_respuestas = objeto_respuestas.getJSONArray("http://purl.org/dc/elements/1.1/TextoRespuesta");
				        String textoRespuesta = "";
				        for(int i=0;i<array_texto_respuestas.size();i++){				        	
			        		String str = array_texto_respuestas.getString(i);
			        		textoRespuesta += "\n" + str;
			        	}  
				        //System.out.println(TextoRespuesta);
				        //opal_parser(textoRespuesta);
				        String opal_xml = Ejecutor.ejecuta_opal(textoRespuesta);
		        		System.out.println(opal_xml);
			        }
			        Comentarios(objeto_respuestas,"");
		        }     
	        }
		}
	}
	
	/*
	 * Método privado auxiliar que parsea la información de un usuario en concreto
	 */
	
	private static void Usuarios(JSONObject objeto, String cuenta) throws IOException{
		if (objeto.has("http://purl.org/dc/elements/1.1/Usuario") && !cuenta.equals("cuentas")){
			JSONArray array_usuarios = objeto.getJSONArray("http://purl.org/dc/elements/1.1/Usuario");
			JSONObject objeto_usuarios = array_usuarios.getJSONObject(0);
        	System.out.println("Informacion de usuario:");
	        //Vamos sacando la información relevante, que son objetos, con un array, dentro de objeto_respuestas
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/Nombre")){
		        JSONArray array_user = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/Nombre");
		        String Nombre = array_user.getString(0);
		        System.out.println("  Nombre: " + Nombre);
	        }
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/Reputacion")){
		        JSONArray array_user = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/Reputacion");
		        String reputacion = array_user.getString(0);
		        System.out.println("  Reputacion: " + reputacion);
	        }
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/MiembroDesde")){
		        JSONArray array_miembro = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/MiembroDesde");
		        String Miembro = array_miembro.getString(0);
		        System.out.println("  Miembro desde: " + Miembro);
	        }
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/VistoUltimaVez")){
		        JSONArray array_visto = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/VistoUltimaVez");
		        String Visto = array_visto.getString(0);
		        System.out.println("  Visto ultima vez: " + Visto);
	        }
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/PaginaWeb")){
		        JSONArray array_web = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/PaginaWeb");
		        String Web = array_web.getString(0);
		        System.out.println("  Pagina web: " + Web);
	        }
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/Localizacion")){
		        JSONArray array_local = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/Localizacion");
		        String Localizacion = array_local.getString(0);
		        //System.out.println("\nLocalización: " + Localizacion);
	        }
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/Edad")){
		        JSONArray array_edad = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/Edad");
		        String Edad = array_edad.getString(0);
		        if(Edad.contains(" ")){
		        	Edad.replaceAll(" ", "");
		        }
		        //System.out.println("\nEdad: " + Edad);
	        }
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/Descripcion")){
		        JSONArray array_descript = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/Descripcion");
		        String Descripcion = array_descript.getString(0);
		        System.out.println("  Descripcion: " + Descripcion);
	        }
			if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/PreguntasUsuario")){
				JSONArray array_pregunta = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/PreguntasUsuario");
				JSONObject objeto_pregunta = array_pregunta.getJSONObject(0);
				if (objeto_pregunta.has("http://purl.org/dc/elements/1.1/NumeroPreguntas")){
					JSONArray array_numero = objeto_pregunta.getJSONArray("http://purl.org/dc/elements/1.1/NumeroPreguntas");
					int numero = array_numero.getInt(0);
					System.out.println("  Numero de preguntas: " + numero);
				}
				if (objeto_pregunta.has("http://purl.org/dc/elements/1.1/InformacionPreguntas")){
					JSONArray array_info = objeto_pregunta.getJSONArray("http://purl.org/dc/elements/1.1/InformacionPreguntas");
					for (int i=0;i<array_info.size();i++){
						JSONObject objeto_info = array_info.getJSONObject(i);
						if (objeto_info.has("http://purl.org/dc/elements/1.1/TituloPregunta")){
							JSONArray array_titulo = objeto_info.getJSONArray("http://purl.org/dc/elements/1.1/TituloPregunta");
							String titulo = array_titulo.getString(0);
							//System.out.println("    Titulo: " + titulo);
						}
						if (objeto_info.has("http://purl.org/dc/elements/1.1/URL")){
							JSONArray array_url = objeto_info.getJSONArray("http://purl.org/dc/elements/1.1/URL");
							String url = array_url.getString(0);
							System.out.println("    URL: " + url);
							//Launch new thread for each question with zero level!
							//new Json("0"+cuenta+url).start();
						}	
					}
				}	
			}
		}
		if(objeto.has("http://purl.org/dc/elements/1.1/RespuestasUsuario")){
			JSONArray array_respuestas = objeto.getJSONArray("http://purl.org/dc/elements/1.1/RespuestasUsuario");
			JSONObject objeto_respuestas = array_respuestas.getJSONObject(0);
			if (objeto_respuestas.has("http://purl.org/dc/elements/1.1/NumeroRespuestas")){
				JSONArray array_numero = objeto_respuestas.getJSONArray("http://purl.org/dc/elements/1.1/NumeroRespuestas");
				int numero = array_numero.getInt(0);
				System.out.println("  Numero de respuestas: " + numero);
			}
			if (objeto_respuestas.has("http://purl.org/dc/elements/1.1/InformacionRespuestas")){
				JSONArray array_info = objeto_respuestas.getJSONArray("http://purl.org/dc/elements/1.1/InformacionRespuestas");
				for (int i=0;i<array_info.size();i++){
					JSONObject objeto_info = array_info.getJSONObject(i);
					if (objeto_info.has("http://purl.org/dc/elements/1.1/TituloRespuesta")){
						JSONArray array_titulo = objeto_info.getJSONArray("http://purl.org/dc/elements/1.1/TituloRespuesta");
						String titulo = array_titulo.getString(0);
						//System.out.println("    Titulo ultimas resp:\n" + titulo);
					}
					if (objeto_info.has("http://purl.org/dc/elements/1.1/URL")){
						JSONArray array_url = objeto_info.getJSONArray("http://purl.org/dc/elements/1.1/URL");
						String url = array_url.getString(0);
						System.out.println("    URL: " + url);
						//Launch new thread for each last answer with zero level!						
						//new Json("0"+cuenta+url).start();
					}
				}
			}
			
		}
		if (objeto.has("http://purl.org/dc/elements/1.1/CuentasRelacionadas") && cuenta.equals("cuentas")){
			JSONArray array_cuentas = objeto.getJSONArray("http://purl.org/dc/elements/1.1/CuentasRelacionadas");
			JSONObject objeto_cuentas = array_cuentas.getJSONObject(0);
        	System.out.println(" Cuentas del usuario:");
        	if(objeto_cuentas.has("http://purl.org/dc/elements/1.1/URLCuentas")){
		        JSONArray array_url = objeto_cuentas.getJSONArray("http://purl.org/dc/elements/1.1/URLCuentas");
		        for (int i=0;i<array_url.size();i++){
			        String url_cuenta = array_url.getString(i);
			        System.out.println("   URL Cuenta: " + url_cuenta);
			        //Launch new thread for each account with one level of !
			        
			        //new Json("1"+url_cuenta).start();
		        }
	        }
		}
	}
	
	/*
	 * Metodo privado auxiliar que llama a la clase Ejecutor, que a su vez ejecuta
	 * la herramienta OPAL para un texto dado.
	 *	
	private static void opal_parser(String texto) throws IOException{
		String opal_xml = Ejecutor.ejecuta_opal(texto);
		System.out.println(opal_xml);
	}*/
	
	/*
	 * Metodo privado auxiliar que llama a la clase Ejecutor, para ejecutar el scrappy con 
	 * la url que le digamos, guardando el resultado en 
	 * un fichero en el escritorio, de nombre scrappy_dump.txt
	 *	
	private static String ejecutar_scrappy (String url, String tipo) throws IOException{
		return Ejecutor.ejecuta_scrappy(url, tipo);
	}*/
	
	/*
	 * Metodo privado auxiliar para buscar un usuario y parsear la informacion. 
	 * Introducir el nombre de usuario.
	 */
	private static void InformacionUsuario (String usuario) throws MalformedURLException, IOException{
		//Para obtener la url del usuario que queramos
		String web_inicio = "http://www.google.com/search?q=site:serverfault.com/users+%22";
		String web_fin = "%22";
    	Web file   = new Web(web_inicio+usuario+web_fin);
    	String MIME    = file.getMIMEType( );
    	Object content = file.getContent( );
    	String coma = "\"";
    	String url = "";
    	if ( MIME.equals( "text/html" ) && content instanceof String ){
    		try{
	    	    String html = content.toString();
	      	    int indice_inicial = html.indexOf("serverfault.com/users/");
	    	    int indice_final = html.indexOf(coma, indice_inicial);
	    	    if (indice_final != -1){
		    	    for (int i=indice_inicial;i<indice_final;i++){
		    	    	url += html.charAt(i);
		    	    	
		    	    }	   
		    	    if(!url.contains("%")){
		    	    	/*new Json("1"+url).start();   //"1" indica al scrappy que se ejecute con -l 1
		    	    	try {Thread.sleep(1000);
						} catch (InterruptedException e) { e.printStackTrace();}
						*/
		    	    	new Scrapper("c"+url).start();   //"c" para que sólo se obtenga la información de las cuentas relacionadas del usuario
			            
		    	    }else
		    	    	System.out.println("No se ha encontrado el usuario.");
	    	    }
    		}catch(StringIndexOutOfBoundsException e){
    			System.out.println("El usuario no existe");
    		}
    	}
	}
	
	/*
	 * Sobreescribe el metodo run() de la clase Thread para sacar informacion de un usuario
	 */
	public void run(){
		try {
			String direccion_web = "";
			String scrappy_dump = "";
			String dameNombre = "";
			String url = "";
			if(!getName().startsWith("c")){
				if (getName().startsWith("0")){
					dameNombre = getName().replaceFirst("0", "");
					scrappy_dump = Ejecutor.executeScrappy(dameNombre, "0");
				}
				if (getName().startsWith("1")){
					dameNombre = getName().replaceFirst("1", "");
					scrappy_dump = Ejecutor.executeScrappy(dameNombre, "1");
				}
				try {
					//System.out.println("Sale del comando");		            
					JSONArray array = (JSONArray) JSONSerializer.toJSON(scrappy_dump);
					if(getName().contains("http://")){
						for(int i=7;i<dameNombre.indexOf("/", 7);i++){
							direccion_web += dameNombre.charAt(i);
						}
					}else{
						for(int i=0;i<dameNombre.indexOf("/");i++){
							direccion_web += dameNombre.charAt(i);
						}
					}
					for(int j=0;j<array.size();j++){
		            	JSONObject objeto_dump = array.getJSONObject(j);
		            	System.out.println("\n-----------------------------------------------------------------------------------------------\n");
		            	System.out.println(dameNombre+"\n");
		            	Usuarios(objeto_dump,direccion_web);
		    	        Pregunta(objeto_dump, NombreUsuario+ApellidoUsuario_Espacio); 
		    	        Respuestas(objeto_dump, NombreUsuario+ApellidoUsuario_Espacio);  
		            }
				} catch(net.sf.json.JSONException e) {
					e.printStackTrace();
					System.out.println("Invalid JSON String:"+scrappy_dump);
				}
			}
			if(getName().startsWith("c")){
				url = getName().replaceFirst("c", "") + "?tab=accounts";
				scrappy_dump = Ejecutor.executeScrappy(url, "0");
				//System.out.println("DUMP:\n"+scrappy_dump);
				JSONArray array = (JSONArray) JSONSerializer.toJSON(scrappy_dump);
	            for(int j=0;j<array.size();j++){
	            	JSONObject objeto_dump = array.getJSONObject(j);
	            	Usuarios(objeto_dump, "cuentas");
	            }
			}
	
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
    public static void main(String[] args) throws Exception {
    	String usuario = "Ben Torell"; //wayne koorts"; //Tiberiu Ana"; //"Ben Torell";
    	String apellidoUsuario = "";
		if (usuario.contains(" ")){
			for(int i=0;i<usuario.indexOf(" ");i++){
				NombreUsuario += usuario.charAt(i);
			}
			for(int j=usuario.indexOf(" ")+1;j<usuario.length();j++){
				apellidoUsuario += usuario.charAt(j);
			}			
		}
		System.out.println("Script iniciado sobre: "+NombreUsuario+" "+apellidoUsuario);   	
		ApellidoUsuario_Espacio = " " + apellidoUsuario;
    	//InformacionUsuario(NombreUsuario+"+"+apellidoUsuario);
		UserAccounts("Gavin Sharp", "ohloh.net");
		
    }
    
    
    private static Double Reputation(JSONObject objeto, String cuenta) throws IOException {
    	String reputation = null;
		if (objeto.has("http://purl.org/dc/elements/1.1/Usuario")){
			JSONArray array_usuarios = objeto.getJSONArray("http://purl.org/dc/elements/1.1/Usuario");
			JSONObject objeto_usuarios = array_usuarios.getJSONObject(0);
        	System.out.println("Informacion de usuario:");
	        //Vamos sacando la información relevante, que son objetos, con un array, dentro de objeto_respuestas
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/Nombre")){
		        JSONArray array_user = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/Nombre");
		        String Nombre = array_user.getString(0);
		        System.out.println("  Nombre: " + Nombre);
	        }
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/Reputacion")){
		        JSONArray array_user = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/Reputacion");
		        reputation = array_user.getString(0);
		        System.out.println("  Reputacion: " + reputation);
	        }
	        //Ohloh-----------------------------------------------------------------------------------------------------
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/ReputacionOhloh")){
		        JSONArray array_user = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/ReputacionOhloh");
		        reputation = array_user.getString(0);
		        System.out.println("  Reputacion: " + reputation);
	        }
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/Ranking")){
		        JSONArray array_user = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/Ranking");
		        reputation = array_user.getString(0);
		        System.out.println("  Ranking: " + reputation);
	        }
	        //----------------------------------------------------------------------------------------------------------
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/MiembroDesde")){
		        JSONArray array_miembro = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/MiembroDesde");
		        String Miembro = array_miembro.getString(0);
		        System.out.println("  Miembro desde: " + Miembro);
	        }
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/VistoUltimaVez")){
		        JSONArray array_visto = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/VistoUltimaVez");
		        String Visto = array_visto.getString(0);
		        System.out.println("  Visto ultima vez: " + Visto);
	        }
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/PaginaWeb")){
		        JSONArray array_web = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/PaginaWeb");
		        String Web = array_web.getString(0);
		        System.out.println("  Pagina web: " + Web);
	        }
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/Localizacion")){
		        JSONArray array_local = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/Localizacion");
		        String Localizacion = array_local.getString(0);
		        //System.out.println("\nLocalización: " + Localizacion);
	        }
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/Edad")){
		        JSONArray array_edad = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/Edad");
		        String Edad = array_edad.getString(0);
		        if(Edad.contains(" ")){
		        	Edad.replaceAll(" ", "");
		        }
		        //System.out.println("\nEdad: " + Edad);
	        }
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/Descripcion")){
		        JSONArray array_descript = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/Descripcion");
		        String Descripcion = array_descript.getString(0);
		        System.out.println("  Descripcion: " + Descripcion);
	        }
			if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/PreguntasUsuario")){
				JSONArray array_pregunta = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/PreguntasUsuario");
				JSONObject objeto_pregunta = array_pregunta.getJSONObject(0);
				if (objeto_pregunta.has("http://purl.org/dc/elements/1.1/NumeroPreguntas")){
					JSONArray array_numero = objeto_pregunta.getJSONArray("http://purl.org/dc/elements/1.1/NumeroPreguntas");
					int numero = array_numero.getInt(0);
					System.out.println("  Numero de preguntas: " + numero);
				}
				if (objeto_pregunta.has("http://purl.org/dc/elements/1.1/InformacionPreguntas")){
					JSONArray array_info = objeto_pregunta.getJSONArray("http://purl.org/dc/elements/1.1/InformacionPreguntas");
					for (int i=0;i<array_info.size();i++){
						JSONObject objeto_info = array_info.getJSONObject(i);
						if (objeto_info.has("http://purl.org/dc/elements/1.1/TituloPregunta")){
							JSONArray array_titulo = objeto_info.getJSONArray("http://purl.org/dc/elements/1.1/TituloPregunta");
							String titulo = array_titulo.getString(0);
							//System.out.println("    Titulo: " + titulo);
						}
						if (objeto_info.has("http://purl.org/dc/elements/1.1/URL")){
							JSONArray array_url = objeto_info.getJSONArray("http://purl.org/dc/elements/1.1/URL");
							String url = array_url.getString(0);
							System.out.println("    URL: " + url);
							//Launch new thread for each question with zero level!
							//new Json("0"+cuenta+url).start();
						}	
					}
				}	
			}
		}
		if(objeto.has("http://purl.org/dc/elements/1.1/RespuestasUsuario")){
			JSONArray array_respuestas = objeto.getJSONArray("http://purl.org/dc/elements/1.1/RespuestasUsuario");
			JSONObject objeto_respuestas = array_respuestas.getJSONObject(0);
			if (objeto_respuestas.has("http://purl.org/dc/elements/1.1/NumeroRespuestas")){
				JSONArray array_numero = objeto_respuestas.getJSONArray("http://purl.org/dc/elements/1.1/NumeroRespuestas");
				int numero = array_numero.getInt(0);
				System.out.println("  Numero de respuestas: " + numero);
			}
			if (objeto_respuestas.has("http://purl.org/dc/elements/1.1/InformacionRespuestas")){
				JSONArray array_info = objeto_respuestas.getJSONArray("http://purl.org/dc/elements/1.1/InformacionRespuestas");
				for (int i=0;i<array_info.size();i++){
					JSONObject objeto_info = array_info.getJSONObject(i);
					if (objeto_info.has("http://purl.org/dc/elements/1.1/TituloRespuesta")){
						JSONArray array_titulo = objeto_info.getJSONArray("http://purl.org/dc/elements/1.1/TituloRespuesta");
						String titulo = array_titulo.getString(0);
						//System.out.println("    Titulo ultimas resp:\n" + titulo);
					}
					if (objeto_info.has("http://purl.org/dc/elements/1.1/URL")){
						JSONArray array_url = objeto_info.getJSONArray("http://purl.org/dc/elements/1.1/URL");
						String url = array_url.getString(0);
						System.out.println("    URL: " + url);
						//Launch new thread for each last answer with zero level!						
						//new Json("0"+cuenta+url).start();
					}
				}
			}			
		}
		try {
			//return (reputation == null ? null : Double.parseDouble(reputation));			
			return (reputation == null ? null : NumberFormat.getInstance(Locale.US).parse(reputation).doubleValue());
		} catch (ParseException e) {
			System.out.println("Error: Reputation Puntuation cannot be parsed to a double :"+reputation);
			return null;
		}
	}
    
    static public Double ExtractReputation(String url) throws IOException {
    	String scrappy_dump = Ejecutor.executeScrappy(url, "1");
    	try {
			JSONArray array = (JSONArray) JSONSerializer.toJSON(scrappy_dump);
			String direccionWeb = "";
			if(url.contains("http://")){
				direccionWeb = url.substring(7);				
			}
			if(url.indexOf("/") != -1) {
				direccionWeb += url.substring(0,url.indexOf("/"));				
			}
			for(int j=0; j<array.size(); j++){
            	JSONObject objeto_dump = array.getJSONObject(j);
            	System.out.println("-----------------------------------------------------------------------------------------------");
            	Double reputation = Reputation(objeto_dump,direccionWeb);
            	if(reputation != null)
            		return reputation;
    	        //Pregunta(objeto_dump, NombreUsuario+ApellidoUsuario_Espacio); 
    	        //Respuestas(objeto_dump, NombreUsuario+ApellidoUsuario_Espacio);  
            }
		} catch(net.sf.json.JSONException e) {
			e.printStackTrace();
			System.out.println("Invalid JSON String:"+scrappy_dump);
		}
		return null;
    }
    
    
    static public List<String> getMoreAccounts(String name, String initialSite) throws Exception {
    	//System.out.println("Informacion usuario iniciado sobre:"+name);   	
		//ApellidoUsuario_Espacio = " " + apellidoUsuario;
    	return UserAccounts(name.replace(" ", "+"), initialSite);
    }
    
    //Para obtener la url del usuario que queramos	
    private static List<String> UserAccounts (String usuario, String initialSite) 
    throws MalformedURLException, IOException{
    	String coma = "\"";
    	String url = "";
    	if (initialSite.equals("ohloh.net")){
    		initialSite = initialSite.toLowerCase();
    		String web = "https://www.ohloh.net/people?sort=kudo_position&q="+usuario;
    		Web file = new Web (web);
        	String MIME    = file.getMIMEType( );
        	Object content = file.getContent( );
	    	if ( MIME.equals( "text/html" ) && content instanceof String ){
	    		try{
		    	    String html = content.toString();
		    	    int indice_inicial = html.toLowerCase().indexOf("<a href='/accounts/");
		    	    int indice_final = html.indexOf("'>", indice_inicial);
		    	    if (indice_final != -1){
		    	    	url = html.substring(indice_inicial+9, indice_final);
			    	    //System.out.println("URL devuelta:"+url);
			    	    if(!url.contains("%")){
			    	    	List<String> accounts = new ArrayList<String>();
			    	    	url = initialSite + url;
			    	    	accounts.add(url);
				            return accounts;			           
				        } else
			    	    	System.out.println("No se ha encontrado el usuario.");
		    	    }
	    		}catch(StringIndexOutOfBoundsException e){
	    			System.out.println("El usuario no existe");
	    		}
	    	}
    	}else{
			initialSite = initialSite.toLowerCase();
			String web_inicio = "http://www.google.com/search?q=site:"+initialSite+"/users+%22";
			String web_fin = "%22";
			//System.out.println("URL:"+web_inicio+usuario+web_fin);
	    	Web file   = new Web(web_inicio+usuario+web_fin);
	    	String MIME    = file.getMIMEType( );
	    	Object content = file.getContent( );
	    	if ( MIME.equals( "text/html" ) && content instanceof String ){
	    		try{
		    	    String html = content.toString();
		    	    int indice_inicial = html.toLowerCase().indexOf(initialSite+"/users/");
		    	    int indice_final = html.indexOf(coma, indice_inicial);
		    	    if (indice_final != -1){
		    	    	url = html.substring(indice_inicial, indice_final);
			    	    //System.out.println("URL devuelta:"+url);
			    	    if(!url.contains("%")){
			    	    	List<String> accounts = new ArrayList<String>();
			    	    	accounts.add(url);
			    	    	url += "?tab=accounts";
							String scrappy_dump = Ejecutor.executeScrappy(url, "0");
							JSONArray array = (JSONArray) JSONSerializer.toJSON(scrappy_dump);
				            for(int j=0;j<array.size();j++){
				            	JSONObject objeto_dump = array.getJSONObject(j);
				            	accounts.addAll(GetAccounts(objeto_dump));
				            }
				            return accounts;			           
				        } else
			    	    	System.out.println("No se ha encontrado el usuario.");
		    	    }
	    		}catch(StringIndexOutOfBoundsException e){
	    			System.out.println("El usuario no existe");
	    		}
	    	}
    	}
    	return null;
	}
    
    static private List<String> GetAccounts(JSONObject objeto) throws IOException {
    	List<String> accounts = new ArrayList<String>();
    	if (objeto.has("http://purl.org/dc/elements/1.1/CuentasRelacionadas")){
			JSONArray array_cuentas = objeto.getJSONArray("http://purl.org/dc/elements/1.1/CuentasRelacionadas");
			JSONObject objeto_cuentas = array_cuentas.getJSONObject(0);
        	//System.out.println(" Cuentas del usuario:");
        	if(objeto_cuentas.has("http://purl.org/dc/elements/1.1/URLCuentas")){
		        JSONArray array_url = objeto_cuentas.getJSONArray("http://purl.org/dc/elements/1.1/URLCuentas");
		        for (int i=0;i<array_url.size();i++){
		        	accounts.add(array_url.getString(i));
		        	//System.out.println("   "+array_url.getString(i));
		        }
	        }
		}
    	return accounts;
    }
}