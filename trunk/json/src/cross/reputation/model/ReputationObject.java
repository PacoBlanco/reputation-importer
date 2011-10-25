package cross.reputation.model;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;

public class ReputationObject {
	private Resource resource;
	private Community fromCommunity;
	private List<ReputationValue> hasValue;
	private Entity owner;
	
	public Resource getResource() {
		return resource;
	}	
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	public Community getFromCommunity() {
		return fromCommunity;
	}
	public void setFromCommunity(Community fromCommunity) {
		this.fromCommunity = fromCommunity;
	}
	public List<ReputationValue> getHasValue() {
		return hasValue;
	}
	public void setHasValue(List<ReputationValue> hasValue) {
		this.hasValue = hasValue;
	}
	public void addHasValue(ReputationValue value) {
		if(hasValue == null) {
			hasValue = new ArrayList<ReputationValue>();
		}
		hasValue.add(value);
	}
	public Entity getOwner() {
		return owner;
	}
	public void setOwner(Entity owner) {
		this.owner = owner;
	}
	
	public String toString(String offset) {
		String result = offset+"owner:"+owner+"\n";
		if(owner != null) {
			result += owner.toString(offset+"     ")+"\n";
		}
		result += offset+"fromCommunity:"+fromCommunity;
		if(fromCommunity != null) {
			result += "\n"+fromCommunity.toString(offset+"     ");
		}
		result += "\n"+offset+"hasValue size:"+
				((hasValue == null)?"null":hasValue.size());
		if(hasValue != null) {
			for(ReputationValue value : hasValue) {
				result += "\n"+offset+"reputationValue:"+value;
				result += "\n"+value.toString(offset+"     ");
			}
		}
		return result;
	}
	public String toLimitedString(String offset) {
		String result = offset+"owner:"+owner+"\n";
		if(owner != null) {
			result += offset+"     ";
			if(owner.getResource() != null) {
				result += owner.getResource()+" - ";
			}
			result += "Identifier:"+owner.getUniqueIdentificator()+"\n";
		}
		result += offset+"fromCommunity:"+fromCommunity+"\n";
		if(fromCommunity != null) {
			result += offset+"     ";
			if(fromCommunity.getResource() != null) {
				result += fromCommunity.getResource()+" - ";
			}
			result += "Name:"+fromCommunity.getName(); //.toLimitedString(offset+"     ");
		}
		result += "\n"+offset+"hasValue size:"+
				((hasValue == null)?"null":hasValue.size());
		if(hasValue != null) {
			for(ReputationValue value : hasValue) {
				result += "\n"+offset+"reputationValue:"+value;
				result += "\n"+value.toLimitedString(offset+"     ");
			}
		}
		return result;
	}
	
}
