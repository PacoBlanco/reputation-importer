package cross.reputation.model;

import java.util.HashMap;
import java.util.Map;

public class Entity {
	private String uniqueIdentificator;
	private Map<Community, EntityIdentifier> identificatorInCommunities = 
		new HashMap<Community, EntityIdentifier>();
	
	public Entity(String uniqueIdentificator) {
		this.uniqueIdentificator = uniqueIdentificator;
	}
	
	public String getUniqueIdentificator() {
		return uniqueIdentificator;
	}
	public void setUniqueIdentificator(String uniqueIdentificator) {
		this.uniqueIdentificator = uniqueIdentificator;
	}
	public void addIdentificatorInCommunities(Community community, 
			EntityIdentifier identifier) {
		if(community != null && identifier != null) {
			identificatorInCommunities.put(community, identifier);
		} else {
			if(community != null)
				System.out.println("Error: identifier cannot be null to add" +
					"Identificator to ("+this.getUniqueIdentificator()+","+community.getName()+")");
			else if(identifier != null) {
				System.out.println("Error: community cannot be null to add " +
					"Identificator to ("+this.getUniqueIdentificator()+","+identifier.getName()+")");
			} else {
				System.out.println("Error: community and identifier cannot be null to add " +
						"Identificator to ("+this.getUniqueIdentificator()+")");
			}
		}
	}
	public Map<Community, EntityIdentifier> getIdentificatorInCommunities() {
		return identificatorInCommunities;
	}
	public void setIdentificatorInCommunities(
			Map<Community, EntityIdentifier> identificatorInCommunities) {
		this.identificatorInCommunities = identificatorInCommunities;
	}	
	
	public class UserEntity extends Entity {
		UserEntity(String uniqueIdentificator) {
			super(uniqueIdentificator);
		}
		//String name;
	}	
	public class GroupEntity extends Entity {
		//String name;
		GroupEntity(String uniqueIdentificator) {
			super(uniqueIdentificator);
		}
	}
	public class ItemEntity extends Entity {
		//String name;
		ItemEntity(String uniqueIdentificator) {
			super(uniqueIdentificator);
		}
	}
	public class ServiceEntity extends Entity {
		//String name;
		ServiceEntity(String uniqueIdentificator) {
			super(uniqueIdentificator);
		}
	}
	public class LocationEntity extends Entity {
		//String name;
		LocationEntity(String uniqueIdentificator) {
			super(uniqueIdentificator);
		}
	}
}
