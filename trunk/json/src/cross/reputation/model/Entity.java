package cross.reputation.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Resource;

public class Entity {
	private Resource resource;
	private String uniqueIdentificator;
	private Map<Community, EntityIdentifier> identificatorInCommunities = 
		new HashMap<Community, EntityIdentifier>();
	private List<ReputationObject> hasReputation;
	private List<ReputationValue> hasValue;
	private List<ReputationEvaluation> hasEvaluation;
	
	public Entity() {		
	}
	
	public Entity(String uniqueIdentificator) {
		this.uniqueIdentificator = uniqueIdentificator;
	}
	
	public Resource getResource() {
		return resource;
	}	
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	public List<ReputationObject> getHasReputation() {
		return hasReputation;
	}
	public void setHasReputation(List<ReputationObject> hasReputation) {
		this.hasReputation = hasReputation;
	}
	public void addHasReputation(ReputationObject reputation) {
		if(hasReputation == null) {
			hasReputation = new ArrayList<ReputationObject>();
		}
		hasReputation.add(reputation);
	}
	public List<ReputationValue> getHasValue() {
		return hasValue;
	}
	public void setHasValue(List<ReputationValue> hasValue) {
		this.hasValue = hasValue;
	}
	public void addHasValue(ReputationValue reputation) {
		if(hasValue == null) {
			hasValue = new ArrayList<ReputationValue>();
		}
		hasValue.add(reputation);
	}
	public List<ReputationEvaluation> getHasEvaluation() {
		return hasEvaluation;
	}
	public void setHasEvaluation(List<ReputationEvaluation> hasEvaluation) {
		this.hasEvaluation = hasEvaluation;
	}
	public void addHasEvaluation(ReputationEvaluation reputation) {
		if(hasEvaluation == null) {
			hasEvaluation = new ArrayList<ReputationEvaluation>();
		}
		hasEvaluation.add(reputation);
	}
	public String getUniqueIdentificator() {
		return uniqueIdentificator;
	}
	public void setUniqueIdentificator(String uniqueIdentificator) {
		this.uniqueIdentificator = uniqueIdentificator;
	}
	public void addOnlineAccount(EntityIdentifier onlineAccount) throws Exception {
		if(onlineAccount.getBelongsTo() == null) {
			throw new Exception("belongsTo property of onlineAccount must be set"); 
		} else if(onlineAccount.getName() != null){
			identificatorInCommunities.put(onlineAccount.getBelongsTo(), onlineAccount);
		} else if(onlineAccount.getUrl() != null) {
			identificatorInCommunities.put(onlineAccount.getBelongsTo(), onlineAccount);
		} else {
			throw new Exception("accountName or accountProfilePage property of" +
					" onlineAccount must be set"); 
		}
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
	
	public String toString(String offset) {
		StringBuffer result = new StringBuffer(offset+"resource:"+resource.toString()+"\n");
		result.append(offset+"identifier:"+uniqueIdentificator+"\n");
		result.append(offset+"onlineAccount List size:"+((identificatorInCommunities==null)?
				"null":identificatorInCommunities.size()));
		if(identificatorInCommunities != null) {
			for(EntityIdentifier identifier : identificatorInCommunities.values()) {
				result.append("\n"+offset+" onlineAccount:"+identifier);
				result.append("\n"+identifier.toString(offset+"     ")+"\n");
			}			
		}
		result.append(offset+"hasReputation List size:"+
				((hasReputation == null)?"null":hasReputation.size()));
		if(hasReputation != null) {
			for(ReputationObject value : hasReputation) {
				result.append("\n"+offset+" reputationObject:"+value);
				result.append("\n"+value.toLimitedString(offset+"     "));
			}
		}
		result.append("\n"+offset+"hasValue List size:"+
				((hasValue == null)?"null":hasValue.size()));
		if(hasValue != null) {
			for(ReputationValue value : hasValue) {
				result.append("\n"+offset+" reputationValue:"+value);
				result.append("\n"+value.toLimitedString(offset+"     "));
			}
		}
		result.append("\n"+offset+"hasEvaluation size:"+
				((hasEvaluation == null)?"null":hasEvaluation.size()));
		if(hasEvaluation != null) {
			for(ReputationEvaluation value : hasEvaluation) {
				result.append("\n"+offset+" reputationEvaluation:"+value);
				result.append("\n"+value.toLimitedString(offset+"     "));
			}
		}
		return result.toString();
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
