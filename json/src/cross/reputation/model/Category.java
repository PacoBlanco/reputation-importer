package cross.reputation.model;

public class Category {
	String name;
	String description;
	
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
	
}
