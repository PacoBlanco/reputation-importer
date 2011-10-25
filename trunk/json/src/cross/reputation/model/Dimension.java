package cross.reputation.model;

import com.hp.hpl.jena.rdf.model.Resource;

public class Dimension {
	private Resource resource;
	private String name;
	private String description;
	
	public Resource getResource() {
		return resource;
	}	
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	public Dimension(String name) {
		this.name = name;
	}		
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String toString(String offset) {
		StringBuilder result = new StringBuilder(offset+"name:"+name+"\n");
		result.append(offset+"description:"+description);
		return result.toString();
	}
}
