package cross.reputation.model;

import com.hp.hpl.jena.rdf.model.Resource;

public class EntityIdentifier {
	private Resource resource;
	private String name;
	private String url;
	private Community belongsTo;
	
	public EntityIdentifier() {		
	}
	
	public EntityIdentifier(String name, String url) {
		this.name = name;
		this.url = url;
	}
	
	public Resource getResource() {
		return resource;
	}	
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Community getBelongsTo() {
		return belongsTo;
	}
	public void setBelongsTo(Community belongsTo) {
		this.belongsTo = belongsTo;
	}

	public String toString(String offset) {
		StringBuilder result = new StringBuilder(offset+"accountName:"+name);
		result.append("\n"+offset+"accountProfilePage:"+url);
		result.append("\n"+offset+"belongsTo:"+belongsTo);
		if(belongsTo != null) {
			result.append("\n"+belongsTo.toString(offset+"     "));
		}
		return result.toString();
	}
}
