package cross.reputation.model;

import java.net.URI;
import java.util.List;

public class CollectingSystemBehaviour extends ReputationBehaviour {
	URI uriFormat;
	
	public URI getUriFormat() {
		return uriFormat;
	}
	public void setUriFormat(URI uriFormat) {
		this.uriFormat = uriFormat;
	}	
	
	public String toString(String offset) {
		String result = offset+"uriFormat:"+uriFormat;
		return result;
	}
	
	static public List<Class<? extends 
			ReputationBehaviour>> listSubclasses() {
		return null;
	}
}
