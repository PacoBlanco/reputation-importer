package json;

import java.io.IOException;
import java.net.MalformedURLException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;


public class Json extends Thread{
	
	private static String NombreUsuario = "";
	private static String ApellidoUsuario = "";
	private static String ApellidoUsuario_Espacio = "";
	
	
	public Json(String str){
		super(str);
	}
	
		
	/*
	 * Método auxiliar que calcula la información de los comentarios
	 */
	private static void Comentarios(JSONObject objeto, String usuario) throws IOException{
		
		
		if (objeto.has("http://purl.org/dc/elements/1.1/Comentarios")){
			
			//Parseamos la información de los comentarios, que es un objeto de objeto_principal que tiene un array, array_comentarios
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
	                		System.out.println("\n\nComentarios por " + usuario);
			                //Vamos sacando la información relevante, que son objetos, con un array, dentro de objeto_informacion_comentarios
		                	if (objeto_informacion_comentarios.has("http://purl.org/dc/elements/1.1/ComentadoPor")){
				                JSONArray array_comentado_por = objeto_informacion_comentarios.getJSONArray
				                	("http://purl.org/dc/elements/1.1/ComentadoPor");
				                String ComentadoPor = array_comentado_por.getString(0);
				                System.out.println("\n\nComentado por: " + ComentadoPor);
		                	}
		                	if (objeto_informacion_comentarios.has("http://purl.org/dc/elements/1.1/Fecha")){
				                JSONArray array_fecha_comentarios = objeto_informacion_comentarios.getJSONArray("http://purl.org/dc/elements/1.1/Fecha");
				                String FechaComentarios = array_fecha_comentarios.getString(0);
				                System.out.println("\nFecha: " + FechaComentarios);
		                	}
		                	if (objeto_informacion_comentarios.has("http://purl.org/dc/elements/1.1/ReputacionInternaComentarios")){
			                	JSONArray array_reputacion_comentarios = objeto_informacion_comentarios.getJSONArray
			                		("http://purl.org/dc/elements/1.1/ReputacionInternaComentarios");
			                	String ReputacionComentarios = array_reputacion_comentarios.getString(0);
			                	System.out.println("\nReputacion interna del comentarista: " + ReputacionComentarios);
			                }
		                	if (objeto_informacion_comentarios.has("http://purl.org/dc/elements/1.1/TextoComentarios")){
				                JSONArray array_texto_comentarios = objeto_informacion_comentarios.getJSONArray
				                	("http://purl.org/dc/elements/1.1/TextoComentarios");
				                String TextoComentarios = array_texto_comentarios.getString(0);
				                System.out.println("\nTexto del comentario: " + TextoComentarios);
				                opal_parser(TextoComentarios);
		                	}  
	                	}
	                	//Si no hay usuario, se sacan los comentarios de todos los usuarios
	                	if (objeto_informacion_comentarios.has("http://purl.org/dc/elements/1.1/ComentadoPor") && (usuario.isEmpty() == true)){
	                		System.out.println("\n\nComentarios:");
			                //Vamos sacando la información relevante, que son objetos, con un array, dentro de objeto_informacion_comentarios
		                	if (objeto_informacion_comentarios.has("http://purl.org/dc/elements/1.1/ComentadoPor")){
				                JSONArray array_comentado_por = objeto_informacion_comentarios.getJSONArray
				                	("http://purl.org/dc/elements/1.1/ComentadoPor");
				                String ComentadoPor = array_comentado_por.getString(0);
				                System.out.println("\n\nComentado por: " + ComentadoPor);
		                	}
		                	if (objeto_informacion_comentarios.has("http://purl.org/dc/elements/1.1/Fecha")){
				                JSONArray array_fecha_comentarios = objeto_informacion_comentarios.getJSONArray("http://purl.org/dc/elements/1.1/Fecha");
				                String FechaComentarios = array_fecha_comentarios.getString(0);
				                System.out.println("\nFecha: " + FechaComentarios);
		                	}
		                	if (objeto_informacion_comentarios.has("http://purl.org/dc/elements/1.1/ReputacionInternaComentarios")){
			                	JSONArray array_reputacion_comentarios = objeto_informacion_comentarios.getJSONArray
			                		("http://purl.org/dc/elements/1.1/ReputacionInternaComentarios");
			                	String ReputacionComentarios = array_reputacion_comentarios.getString(0);
			                	System.out.println("\nReputacion interna del comentarista: " + ReputacionComentarios);
			                }
		                	if (objeto_informacion_comentarios.has("http://purl.org/dc/elements/1.1/TextoComentarios")){
				                JSONArray array_texto_comentarios = objeto_informacion_comentarios.getJSONArray
				                	("http://purl.org/dc/elements/1.1/TextoComentarios");
				                String TextoComentarios = array_texto_comentarios.getString(0);
				                System.out.println("\nTexto del comentario: " + TextoComentarios);
				                opal_parser(TextoComentarios);
		                	}  
	                	}
	            	} 	
	        	}
	        } 
		}
	}
	
	/*
	 * Método auxiliar que calcula la información de la pregunta
	 */
	private static void Pregunta(JSONObject objeto, String usuario) throws IOException{
		
		//Sacamos el título y la url de la pregunta
		if (objeto.has("http://purl.org/dc/elements/1.1/Pregunta")){
			String url = objeto.getString("id");
			//Parseamos la información de la pregunta, que es un objeto de objeto_principal que tiene un array, array_pregunta
	        JSONArray array_pregunta = objeto.getJSONArray("http://purl.org/dc/elements/1.1/Pregunta");      
	        JSONObject objeto_pregunta = array_pregunta.getJSONObject(0);
	        //Si usuario ha creado una pregunta, se obtiene informacion de dicha pregunta
	        if (objeto_pregunta.has("http://purl.org/dc/elements/1.1/Creador") && (usuario.isEmpty() == false) 
	        		&& objeto_pregunta.getJSONArray("http://purl.org/dc/elements/1.1/Creador").getString(0).equals(usuario)){
	        	System.out.println("Preguntas por " + usuario);
	    		System.out.println("\n\nURL: " + url);
		        //Vamos sacando la información relevante, que son objetos, con un array, dentro de objeto_pregunta
		        if (objeto_pregunta.has("http://purl.org/dc/elements/1.1/TituloPregunta")){
			        JSONArray array_titulo = objeto_pregunta.getJSONArray("http://purl.org/dc/elements/1.1/TituloPregunta");
			        String Titulo = array_titulo.getString(0);
			        System.out.println("\nTitulo: " + Titulo);
		        }
		        if (objeto_pregunta.has("http://purl.org/dc/elements/1.1/Creador")){
			        JSONArray array_creador = objeto_pregunta.getJSONArray("http://purl.org/dc/elements/1.1/Creador");
			        String Creador = array_creador.getString(0);
			        System.out.println("\nCreador: " + Creador);
		        }
		        if (objeto_pregunta.has("http://purl.org/dc/elements/1.1/Fecha")){
			        JSONArray array_fecha = objeto_pregunta.getJSONArray("http://purl.org/dc/elements/1.1/Fecha");
			        String FechaPregunta = array_fecha.getString(0);
			        System.out.println("\nFecha: " + FechaPregunta);
		        }
		        if (objeto_pregunta.has("http://purl.org/dc/elements/1.1/ReputacionInternaCreador")){
			        JSONArray array_reputacion_creador = objeto_pregunta.getJSONArray
			        	("http://purl.org/dc/elements/1.1/ReputacionInternaCreador");
			        String ReputacionCreador = array_reputacion_creador.getString(0);
			        System.out.println("\nReputacion interna del creador: " + ReputacionCreador);
		        }
		        
		        System.out.println("\nTexto de la pregunta:");
		        if(objeto_pregunta.has("http://purl.org/dc/elements/1.1/TextoPregunta")){
			        JSONArray array_texto_pregunta = objeto_pregunta.getJSONArray("http://purl.org/dc/elements/1.1/TextoPregunta");
			        
			        if (array_texto_pregunta.size() > 1){
			        	for(int i=0;i<array_texto_pregunta.size();i++){
			        		
			        		String TextoPregunta = array_texto_pregunta.getString(i);
			            	System.out.println(TextoPregunta);
			        	}
			        }else{
			        	
			        	String TextoPregunta = array_texto_pregunta.getString(0);	
			        	System.out.println(TextoPregunta);
			        }
		        }
		        Comentarios(objeto_pregunta,"");
	        }
	        //Si no hay usuario, se sacan todas las preguntas
	        if (objeto_pregunta.has("http://purl.org/dc/elements/1.1/Creador") && (usuario.isEmpty() == true)){
	        	System.out.println("Pregunta:");
	    		System.out.println("\n\nURL: " + url);
		        //Vamos sacando la información relevante, que son objetos, con un array, dentro de objeto_pregunta
		        if (objeto_pregunta.has("http://purl.org/dc/elements/1.1/TituloPregunta")){
			        JSONArray array_titulo = objeto_pregunta.getJSONArray("http://purl.org/dc/elements/1.1/TituloPregunta");
			        String Titulo = array_titulo.getString(0);
			        System.out.println("\nTitulo: " + Titulo);
		        }
		        if (objeto_pregunta.has("http://purl.org/dc/elements/1.1/Creador")){
			        JSONArray array_creador = objeto_pregunta.getJSONArray("http://purl.org/dc/elements/1.1/Creador");
			        String Creador = array_creador.getString(0);
			        System.out.println("\nCreador: " + Creador);
		        }
		        if (objeto_pregunta.has("http://purl.org/dc/elements/1.1/Fecha")){
			        JSONArray array_fecha = objeto_pregunta.getJSONArray("http://purl.org/dc/elements/1.1/Fecha");
			        String FechaPregunta = array_fecha.getString(0);
			        System.out.println("\nFecha: " + FechaPregunta);
		        }
		        if (objeto_pregunta.has("http://purl.org/dc/elements/1.1/ReputacionInternaCreador")){
			        JSONArray array_reputacion_creador = objeto_pregunta.getJSONArray
			        	("http://purl.org/dc/elements/1.1/ReputacionInternaCreador");
			        String ReputacionCreador = array_reputacion_creador.getString(0);
			        System.out.println("\nReputacion interna del creador: " + ReputacionCreador);
		        }
		        
		        System.out.println("\nTexto de la pregunta:");
		        if(objeto_pregunta.has("http://purl.org/dc/elements/1.1/TextoPregunta")){
			        JSONArray array_texto_pregunta = objeto_pregunta.getJSONArray("http://purl.org/dc/elements/1.1/TextoPregunta");
			        
			        if (array_texto_pregunta.size() > 1){
			        	for(int i=0;i<array_texto_pregunta.size();i++){
			        		
			        		String TextoPregunta = array_texto_pregunta.getString(i);
			            	System.out.println(TextoPregunta);
			        	}
			        }else{
			        	
			        	String TextoPregunta = array_texto_pregunta.getString(0);	
			        	System.out.println(TextoPregunta);
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
		        	System.out.println("\n\nRespuestas por " + usuario + "\n");
			        //Vamos sacando la información relevante, que son objetos, con un array, dentro de objeto_respuestas
			        if(objeto_respuestas.has("http://purl.org/dc/elements/1.1/RespondidoPor")){
				        JSONArray array_respondido_por = objeto_respuestas.getJSONArray("http://purl.org/dc/elements/1.1/RespondidoPor");
				        String RespondidoPor = array_respondido_por.getString(0);
				        System.out.println("\nRespondido por: " + RespondidoPor);
			        }
			        if((objeto_respuestas.has("http://purl.org/dc/elements/1.1/RespondidoPor") == false) && 
			        		(objeto_respuestas.has("http://purl.org/dc/elements/1.1/RespondidoPor_SinPerfil") == true)){
			        	JSONArray array_respondido_por_sinperfil = objeto_respuestas.getJSONArray("http://purl.org/dc/elements/1.1/RespondidoPor_SinPerfil");
				        String RespondidoPor = array_respondido_por_sinperfil.getString(0);
				        System.out.println("\n\nRespondido por: " + RespondidoPor);
			        }
			        if(objeto_respuestas.has("http://purl.org/dc/elements/1.1/Fecha")){
				        JSONArray array_fecha = objeto_respuestas.getJSONArray("http://purl.org/dc/elements/1.1/Fecha");
				        String FechaRespuestas = array_fecha.getString(0); 
				        System.out.println("\nFecha: " + FechaRespuestas);
			        }
			        if(objeto_respuestas.has("http://purl.org/dc/elements/1.1/ReputacionInternaResponden")){
				        JSONArray array_reputacion_responden = objeto_respuestas.getJSONArray
				        	("http://purl.org/dc/elements/1.1/ReputacionInternaResponden");
				        String ReputacionResponden = array_reputacion_responden.getString(0);
				        System.out.println("\nReputacion interna del que responde: " + ReputacionResponden);
			        }
			        if(objeto_respuestas.has("http://purl.org/dc/elements/1.1/ReputacionInternaResponden") == false){
			        	System.out.println("Reputacion interna del que responde: No tiene");
			        }
			        if(objeto_respuestas.has("http://purl.org/dc/elements/1.1/FlagPositivo")){
			        	JSONArray array_flag = objeto_respuestas.getJSONArray("http://purl.org/dc/elements/1.1/FlagPositivo");
				        String FlagPositivo = array_flag.getString(0); 
			        	System.out.println("Flag positivo: " + FlagPositivo);
			        }
			        if(objeto_respuestas.has("http://purl.org/dc/elements/1.1/TextoRespuesta")){
				        System.out.println("\nTexto de la respuesta:");
				        JSONArray array_texto_respuestas = objeto_respuestas.getJSONArray("http://purl.org/dc/elements/1.1/TextoRespuesta");
				        String TextoRespuesta = "";
				        for(int i=0;i<array_texto_respuestas.size();i++){
				        	
			        		String str = array_texto_respuestas.getString(i);
			        		TextoRespuesta += "\n" + str;
			        	}  
				        System.out.println(TextoRespuesta);
				        opal_parser(TextoRespuesta);
			        }
			        Comentarios(objeto_respuestas,"");
		        }
		        //Si no hay usuario, se sacan todas las respuestas
		        if (objeto_respuestas.has("http://purl.org/dc/elements/1.1/RespondidoPor") && usuario.isEmpty() == true){
		        	System.out.println("\n\nRespuestas:\n");
			        //Vamos sacando la información relevante, que son objetos, con un array, dentro de objeto_respuestas
			        if(objeto_respuestas.has("http://purl.org/dc/elements/1.1/RespondidoPor")){
				        JSONArray array_respondido_por = objeto_respuestas.getJSONArray("http://purl.org/dc/elements/1.1/RespondidoPor");
				        String RespondidoPor = array_respondido_por.getString(0);
				        System.out.println("\nRespondido por: " + RespondidoPor);
			        }
			        if((objeto_respuestas.has("http://purl.org/dc/elements/1.1/RespondidoPor") == false) && 
			        		(objeto_respuestas.has("http://purl.org/dc/elements/1.1/RespondidoPor_SinPerfil") == true)){
			        	JSONArray array_respondido_por_sinperfil = objeto_respuestas.getJSONArray("http://purl.org/dc/elements/1.1/RespondidoPor_SinPerfil");
				        String RespondidoPor = array_respondido_por_sinperfil.getString(0);
				        System.out.println("\n\nRespondido por: " + RespondidoPor);
			        }
			        if(objeto_respuestas.has("http://purl.org/dc/elements/1.1/Fecha")){
				        JSONArray array_fecha = objeto_respuestas.getJSONArray("http://purl.org/dc/elements/1.1/Fecha");
				        String FechaRespuestas = array_fecha.getString(0); 
				        System.out.println("\nFecha: " + FechaRespuestas);
			        }
			        if(objeto_respuestas.has("http://purl.org/dc/elements/1.1/ReputacionInternaResponden")){
				        JSONArray array_reputacion_responden = objeto_respuestas.getJSONArray
				        	("http://purl.org/dc/elements/1.1/ReputacionInternaResponden");
				        String ReputacionResponden = array_reputacion_responden.getString(0);
				        System.out.println("\nReputacion interna del que responde: " + ReputacionResponden);
			        }
			        if(objeto_respuestas.has("http://purl.org/dc/elements/1.1/ReputacionInternaResponden") == false){
			        	System.out.println("Reputacion interna del que responde: No tiene");
			        }
			        if(objeto_respuestas.has("http://purl.org/dc/elements/1.1/FlagPositivo")){
			        	JSONArray array_flag = objeto_respuestas.getJSONArray("http://purl.org/dc/elements/1.1/FlagPositivo");
				        String FlagPositivo = array_flag.getString(0); 
			        	System.out.println("Flag positivo: " + FlagPositivo);
			        }
			        if(objeto_respuestas.has("http://purl.org/dc/elements/1.1/TextoRespuesta")){
				        System.out.println("\nTexto de la respuesta:");
				        JSONArray array_texto_respuestas = objeto_respuestas.getJSONArray("http://purl.org/dc/elements/1.1/TextoRespuesta");
				        String TextoRespuesta = "";
				        for(int i=0;i<array_texto_respuestas.size();i++){
				        	
			        		String str = array_texto_respuestas.getString(i);
			        		TextoRespuesta += "\n" + str;
			        	}  
				        System.out.println(TextoRespuesta);
				        opal_parser(TextoRespuesta);
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
        	System.out.println("Informacion de usuario:\n");
	        //Vamos sacando la información relevante, que son objetos, con un array, dentro de objeto_respuestas
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/Nombre")){
		        JSONArray array_user = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/Nombre");
		        String Nombre = array_user.getString(0);
		        System.out.println("\nNombre: " + Nombre);
	        }
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/Reputacion")){
		        JSONArray array_user = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/Reputacion");
		        String reputacion = array_user.getString(0);
		        System.out.println("\nReputacion: " + reputacion);
	        }
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/MiembroDesde")){
		        JSONArray array_miembro = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/MiembroDesde");
		        String Miembro = array_miembro.getString(0);
		        System.out.println("\nMiembro desde: " + Miembro);
	        }
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/VistoUltimaVez")){
		        JSONArray array_visto = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/VistoUltimaVez");
		        String Visto = array_visto.getString(0);
		        System.out.println("\nVisto por última vez: " + Visto);
	        }
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/PaginaWeb")){
		        JSONArray array_web = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/PaginaWeb");
		        String Web = array_web.getString(0);
		        System.out.println("\nPágina web: " + Web);
	        }
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/Localizacion")){
		        JSONArray array_local = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/Localizacion");
		        String Localizacion = array_local.getString(0);
		        System.out.println("\nLocalización: " + Localizacion);
	        }
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/Edad")){
		        JSONArray array_edad = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/Edad");
		        String Edad = array_edad.getString(0);
		        if(Edad.contains(" ")){
		        	Edad.replaceAll(" ", "");
		        }
		        System.out.println("\nEdad: " + Edad);
	        }
	        if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/Descripcion")){
		        JSONArray array_descript = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/Descripcion");
		        String Descripcion = array_descript.getString(0);
		        System.out.println("\nDescripción: " + Descripcion);
	        }
			if(objeto_usuarios.has("http://purl.org/dc/elements/1.1/PreguntasUsuario")){
				JSONArray array_pregunta = objeto_usuarios.getJSONArray("http://purl.org/dc/elements/1.1/PreguntasUsuario");
				JSONObject objeto_pregunta = array_pregunta.getJSONObject(0);
				if (objeto_pregunta.has("http://purl.org/dc/elements/1.1/NumeroPreguntas")){
					JSONArray array_numero = objeto_pregunta.getJSONArray("http://purl.org/dc/elements/1.1/NumeroPreguntas");
					int numero = array_numero.getInt(0);
					System.out.println("\nNumero de preguntas: " + numero);
				}
				if (objeto_pregunta.has("http://purl.org/dc/elements/1.1/InformacionPreguntas")){
					JSONArray array_info = objeto_pregunta.getJSONArray("http://purl.org/dc/elements/1.1/InformacionPreguntas");
					for (int i=0;i<array_info.size();i++){
						JSONObject objeto_info = array_info.getJSONObject(i);
						if (objeto_info.has("http://purl.org/dc/elements/1.1/TituloPregunta")){
							JSONArray array_titulo = objeto_info.getJSONArray("http://purl.org/dc/elements/1.1/TituloPregunta");
							String titulo = array_titulo.getString(0);
							System.out.println("\nTítulo: " + titulo);
						}
						if (objeto_info.has("http://purl.org/dc/elements/1.1/URL")){
							JSONArray array_url = objeto_info.getJSONArray("http://purl.org/dc/elements/1.1/URL");
							String url = array_url.getString(0);
							System.out.println("\nURL: " + url);
							new Json("0"+cuenta+url).start();
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
				System.out.println("\nNumero de respuestas: " + numero);
			}
			if (objeto_respuestas.has("http://purl.org/dc/elements/1.1/InformacionRespuestas")){
				JSONArray array_info = objeto_respuestas.getJSONArray("http://purl.org/dc/elements/1.1/InformacionRespuestas");
				for (int i=0;i<array_info.size();i++){
					JSONObject objeto_info = array_info.getJSONObject(i);
					if (objeto_info.has("http://purl.org/dc/elements/1.1/TituloRespuesta")){
						JSONArray array_titulo = objeto_info.getJSONArray("http://purl.org/dc/elements/1.1/TituloRespuesta");
						String titulo = array_titulo.getString(0);
						System.out.println("\nTítulo de últimas respuestas:\n" + titulo);
					}
					if (objeto_info.has("http://purl.org/dc/elements/1.1/URL")){
						JSONArray array_url = objeto_info.getJSONArray("http://purl.org/dc/elements/1.1/URL");
						String url = array_url.getString(0);
						System.out.println("\nURL: " + url);
						new Json("0"+cuenta+url).start();
					}
				}
			}
			
		}
		
		if (objeto.has("http://purl.org/dc/elements/1.1/CuentasRelacionadas") && cuenta.equals("cuentas")){
			JSONArray array_cuentas = objeto.getJSONArray("http://purl.org/dc/elements/1.1/CuentasRelacionadas");
			JSONObject objeto_cuentas = array_cuentas.getJSONObject(0);
        	System.out.println("Cuentas del usuario:\n");
        	if(objeto_cuentas.has("http://purl.org/dc/elements/1.1/URLCuentas")){
		        JSONArray array_url = objeto_cuentas.getJSONArray("http://purl.org/dc/elements/1.1/URLCuentas");
		        for (int i=0;i<array_url.size();i++){
			        String url_cuenta = array_url.getString(i);
			        System.out.println("\nURL Cuenta: " + url_cuenta);
			        new Json("1"+url_cuenta).start();
		        }
	        }
		}
	}
	
	/*
	 * Método privado auxiliar que llama a la clase Ejecutor, que a su vez ejecuta la herramienta OPAL para un texto dado.
	 */
	
	private static void opal_parser(String texto) throws IOException{
		Ejecutor e = new Ejecutor();
		String opal_xml = e.ejecuta_opal(texto);
		System.out.println(opal_xml);
	}
	
	/*
	 * Método privado auxiliar que llama a la clase Ejecutor, para ejecutar el scrappy con la url que le digamos, guardando el resultado en 
	 * un fichero en el escritorio, de nombre scrappy_dump.txt
	 */
	
	private static String ejecutar_scrappy (String url, String tipo) throws IOException{
		Ejecutor e = new Ejecutor();
		return e.ejecuta_scrappy(url, tipo);
	}
	/*
	 * Método privado auxiliar para buscar un usuario y parsear la informacion. Introducir el nombre de usuario.
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
		    	    	new Json("1"+url).start();   //"1" indica al scrappy que se ejecute con -l 1
		    	    	new Json("c"+url).start();   //"c" para que sólo se obtenga la información de las cuentas relacionadas del usuario
			            
		    	    }else
		    	    	System.out.println("No se ha encontrado el usuario.");
	    	    }
    		}catch(StringIndexOutOfBoundsException e){
    			System.out.println("El usuario no existe");
    		}
    	}
	}
	
	/*
	 * Sobreescribe el método run() de la clase Thread para sacar información de un usuario
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
					scrappy_dump = ejecutar_scrappy(dameNombre, "0");
				}
				if (getName().startsWith("1")){
					dameNombre = getName().replaceFirst("1", "");
					scrappy_dump = ejecutar_scrappy(dameNombre, "1");
				}
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
	            	System.out.println("\n\n-----------------------------------------------------------------------------------------------\n\n");
	            	System.out.println(dameNombre+"\n\n");
	            	Usuarios(objeto_dump,direccion_web);
	    	        Pregunta(objeto_dump, NombreUsuario+ApellidoUsuario_Espacio); 
	    	        Respuestas(objeto_dump, NombreUsuario+ApellidoUsuario_Espacio);  
	            }
				
			}
			if(getName().startsWith("c")){
				url = getName().replaceFirst("c", "");
				scrappy_dump = ejecutar_scrappy(url+"?tab=accounts", "0");
				JSONArray array = (JSONArray) JSONSerializer.toJSON(scrappy_dump);
	            for(int j=0;j<array.size();j++){
	            	JSONObject objeto_dump = array.getJSONObject(j);
	            	System.out.println("\n\n-----------------------------------------------------------------------------------------------\n\n");
	            	Usuarios(objeto_dump, "cuentas");
	            }
			}
	
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
    public static void main(String[] args) throws Exception {
    	
    	String usuario = "Ben Torell";
		if (usuario.contains(" ")){
			for(int i=0;i<usuario.indexOf(" ");i++){
				NombreUsuario += usuario.charAt(i);
			}
			for(int j=usuario.indexOf(" ")+1;j<usuario.length();j++){
				ApellidoUsuario += usuario.charAt(j);
			}			
		}
		ApellidoUsuario_Espacio = " " + ApellidoUsuario;
    	InformacionUsuario(NombreUsuario+"+"+ApellidoUsuario);
    }
}