package cross.reputation.model;

import java.net.URI;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;

public class CollectingSystem extends CollectingAlgorithm {
	URI uriFormat;
	
	public URI getUriFormat() {
		return uriFormat;
	}
	public void setUriFormat(URI uriFormat) {
		this.uriFormat = uriFormat;
	}	
	
	static public List<Class<? extends ReputationAlgorithm>> listSubclasses() {
		return null;
	}
	
	public String toString(String offset) {
		String result = offset+"uriFormat:"+uriFormat;
		result += "\n"+super.toString(offset);
		return result;
	}
}
