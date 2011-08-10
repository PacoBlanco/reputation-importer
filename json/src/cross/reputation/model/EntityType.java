package cross.reputation.model;

public class EntityType {
	String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}	
	
	public String toString(String offset) {
		return offset+"type:"+type;
	}
}
