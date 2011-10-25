package cross.reputation.model;

import com.hp.hpl.jena.rdf.model.Resource;

public class Category {
	private Resource resource;
	private String name;
	private String description;	
	
	public Resource getResource() {
		return resource;
	}
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	public Category(String name) {
		this.name = name;
	}	
	public String getName() {
		return name;
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
