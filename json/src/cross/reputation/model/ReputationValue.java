package cross.reputation.model;

import java.util.Date;
import java.util.List;

public class ReputationValue {
	ReputationAlgorithm obtainedBy;
	String collectionIdentifier;
	Date timeStamp;
	Date expirationTime;
	List<Evaluation> hasEvaluations;
	Entity owner;
	
	public ReputationAlgorithm getObtainedBy() {
		return obtainedBy;
	}
	public void setObtainedBy(ReputationAlgorithm obtainedBy) {
		this.obtainedBy = obtainedBy;
	}
	public String getCollectionIdentifier() {
		return collectionIdentifier;
	}
	public void setCollectionIdentifier(String collectionIdentifier) {
		this.collectionIdentifier = collectionIdentifier;
	}
	public Date getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	public Date getExpirationTime() {
		return expirationTime;
	}
	public void setExpirationTime(Date expirationTime) {
		this.expirationTime = expirationTime;
	}
	public List<Evaluation> getHasEvaluations() {
		return hasEvaluations;
	}
	public void setHasEvaluations(List<Evaluation> hasEvaluations) {
		this.hasEvaluations = hasEvaluations;
	}
	public Entity getOwner() {
		return owner;
	}
	public void setOwner(Entity owner) {
		this.owner = owner;
	}
	
	public String toString(String offset) {
		String result = offset+"owner:"+owner+"\n";
		result += offset+"obtainedBy:"+obtainedBy+"\n";
		result += offset+"collectionIdentifier:"+collectionIdentifier+"\n";
		result += offset+"timeStamp:"+timeStamp+"\n";
		result += offset+"expirationTime:"+expirationTime+"\n";
		result += offset+"hasEvaluations size:"+
				((hasEvaluations == null)?"null":hasEvaluations.size());
		for(Evaluation evaluation : hasEvaluations) {
			result += "\n"+offset+"evaluation:"+evaluation;
		}
		return result;
	}
}
