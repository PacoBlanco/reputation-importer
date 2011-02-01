package cross.reputation.model;

public class Dimension {
	private String name;
	private String description;
	
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
}
