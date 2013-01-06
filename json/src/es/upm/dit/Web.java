package es.upm.dit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Get a web file.
 */
public final class Web {
    // Saved response.
    private java.util.Map<String,java.util.List<String>> responseHeader = null;
    private java.net.URL responseURL = null;
    private int responseCode = -1;
    private String MIMEtype  = null;
    private String charset   = null;
    private Object content   = null;
    private Object inputStream = null;
    private int length;
 
    /** Open a web file. */
    public Web( String urlString, String charset)
        throws java.net.MalformedURLException, java.io.IOException {
    	this.charset = charset;
        // Open a URL connection.    	
        final java.net.URL url = new java.net.URL( urlString );
        final java.net.URLConnection uconn = url.openConnection( );
        if ( !(uconn instanceof java.net.HttpURLConnection) )
            throw new java.lang.IllegalArgumentException(
                "URL protocol must be HTTP." );
        final java.net.HttpURLConnection conn =
            (java.net.HttpURLConnection)uconn; 
        // Set up a request.
        //TODO:Probar con timeout para ver que error da y tratarlo de forma diferente al refused
        conn.setConnectTimeout( 600000 );    // 10 sec
        conn.setReadTimeout( 600000 );       // 10 sec
        conn.setInstanceFollowRedirects( true );
        conn.setRequestProperty( "User-agent", "spider" ); 
        // Send the request.
        conn.connect( ); 
        // Get the response.
        responseHeader    = conn.getHeaderFields( );
        responseCode      = conn.getResponseCode( );
        responseURL       = conn.getURL( );
        length  		  = conn.getContentLength( ); 
        // Get the content.
        final java.io.InputStream stream = conn.getErrorStream( );
        if ( stream != null ) {
        	content = readStream( length, stream );
        }else if ( (inputStream = conn.getContent( )) != null &&
        		inputStream instanceof java.io.InputStream ) {
        	content = readStream( length, (java.io.InputStream)inputStream );
        }
        conn.disconnect( );
    }
    
    
    /** Open a web file. */
    public Web( String urlString )
        throws java.net.MalformedURLException, java.io.IOException {
        // Open a URL connection.
        final java.net.URL url = new java.net.URL( urlString );
        final java.net.URLConnection uconn = url.openConnection( );
        if ( !(uconn instanceof java.net.HttpURLConnection) )
            throw new java.lang.IllegalArgumentException(
                "URL protocol must be HTTP." );
        final java.net.HttpURLConnection conn =
            (java.net.HttpURLConnection)uconn;
 
        // Set up a request.
        conn.setConnectTimeout( 100000 );    // 100 sec
        conn.setReadTimeout( 100000 );       // 100 sec
        conn.setInstanceFollowRedirects( true );
        conn.setRequestProperty( "User-agent", "spider" );
 
        // Send the request.
        conn.connect( );
 
        // Get the response.
        responseHeader    = conn.getHeaderFields( );
        responseCode      = conn.getResponseCode( );
        responseURL       = conn.getURL( );
        length  		  = conn.getContentLength( );
        final String type = conn.getContentType( );
        if ( type != null ) {
            final String[] parts = type.split( ";" );
            MIMEtype = parts[0].trim( );
            for ( int i = 1; i < parts.length && charset == null; i++ ) {
                final String t  = parts[i].trim( );
                final int index = t.toLowerCase( ).indexOf( "charset=" );
                if ( index != -1 )
                    charset = t.substring( index+8 );
            }
        }
 
        // Get the content.
        final java.io.InputStream stream = conn.getErrorStream( );
        if ( stream != null ) {
        	content = readStream( length, stream );
        }else if ( (inputStream = conn.getContent( )) != null &&
        		inputStream instanceof java.io.InputStream ) {
        	content = readStream( length, (java.io.InputStream)inputStream );
        }
        conn.disconnect( );
    }
 
    /** Read stream bytes and transcode. */
    private Object readStream( int length, java.io.InputStream stream )
        throws java.io.IOException {
        /*
		final int buflen = Math.max( 1024, Math.max( length, stream.available() ) );
        byte[] buf   = new byte[buflen];
        byte[] bytes = null;

        for ( int nRead = stream.read(buf); nRead != -1; nRead = stream.read(buf) ) {
            if ( bytes == null ) {
                bytes = buf;
                buf   = new byte[buflen];
                continue;
            }
            final byte[] newBytes = new byte[ bytes.length + nRead ];
            System.arraycopy( bytes, 0, newBytes, 0, bytes.length );
            System.arraycopy( buf, 0, newBytes, bytes.length, nRead );
            bytes = newBytes;
            System.out.println("buflen:"+buflen+" bytes"+new String( buf, charset ));            
        }
 
        if ( charset == null )
            return bytes;
        try {
            return new String( bytes, charset );
        }
        catch ( java.io.UnsupportedEncodingException e ) { }
        return bytes;
		*/
        
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        StringBuffer aux = new StringBuffer(); 
        String aux2 = "";
        while( (aux2 = br.readLine()) != null ){
        	aux.append(aux2);
        }
        br.close();
        return aux.toString();
    }
 
    /** Get the content. */
    public java.io.InputStream getContentInInputStream () {
    	if(inputStream instanceof java.io.InputStream )
    		return (java.io.InputStream) inputStream;
    	return null;
    }
    
    /** Get the content streamed. */
    public Object getContent() {
    	return content;
    }
 
    /** Get the response code. */
    public int getResponseCode( ) {
        return responseCode;
    }
 
    /** Get the response header. */
    public java.util.Map<String,java.util.List<String>> getHeaderFields( ) {
        return responseHeader;
    }
 
    /** Get the URL of the received page. */
    public java.net.URL getURL( ) {
        return responseURL;
    }
 
    /** Get the MIME type. */
    public String getMIMEType( ) {
        return MIMEtype;
    }
}