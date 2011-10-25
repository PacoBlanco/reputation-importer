package cross.reputation.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;

public class ReputationValue {
	private Resource resource;
	private ReputationAlgorithmImplementation obtainedBy;
	private String collectionIdentifier;
	private Date timeStamp;
	private Date expirationTime;
	private List<ReputationEvaluation> hasEvaluations;
	private Entity owner;
	
	public Resource getResource() {
		return resource;
	}	
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	public ReputationAlgorithmImplementation getObtainedBy() {
		return obtainedBy;
	}
	public void setObtainedBy(ReputationAlgorithmImplementation obtainedBy) {
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
	public List<ReputationEvaluation> getHasEvaluations() {
		return hasEvaluations;
	}
	public void setHasEvaluations(List<ReputationEvaluation> hasEvaluations) {
		this.hasEvaluations = hasEvaluations;
	}
	public void addHasEvaluations(ReputationEvaluation hasEvaluation) {
		if(hasEvaluations == null) {
			hasEvaluations = new ArrayList<ReputationEvaluation>();
		}
		hasEvaluations.add(hasEvaluation);
	}
	public Entity getOwner() {
		return owner;
	}
	public void setOwner(Entity owner) {
		this.owner = owner;
	}
	
	public String toString(String offset) {
		StringBuilder result = new StringBuilder(offset+"owner:"+owner+"\n");
		result.append(offset+"obtainedBy:"+obtainedBy+"\n");
		result.append(offset+"collectionIdentifier:"+collectionIdentifier+"\n");
		result.append(offset+"timeStamp:"+timeStamp+"\n");
		result.append(offset+"expirationTime:"+expirationTime+"\n");
		result.append(offset+"hasEvaluations size:"+
				((hasEvaluations == null)?"null":hasEvaluations.size()));
		if(hasEvaluations != null) {
			for(ReputationEvaluation evaluation : hasEvaluations) {
				result.append("\n"+offset+"evaluation:"+evaluation);
				result.append("\n"+evaluation.toString(offset+"     "));
			}
		}
		return result.toString();
	}
	public String toLimitedString(String offset) {
		StringBuilder result = new StringBuilder(offset+"owner:"+
				(owner==null?"null":owner.getUniqueIdentificator())+"\n");
		result.append(offset+"obtainedBy:"+
				(obtainedBy==null?"null":obtainedBy.getName())+"\n");
		result.append(offset+"collectionIdentifier:"+collectionIdentifier+"\n");
		result.append(offset+"timeStamp:"+timeStamp+"\n");
		result.append(offset+"expirationTime:"+expirationTime+"\n");
		result.append(offset+"hasEvaluations size:"+
				((hasEvaluations == null)?"null":hasEvaluations.size()));
		if(hasEvaluations != null) {
			for(ReputationEvaluation evaluation : hasEvaluations) {
				result.append("\n"+offset+"evaluation:"+evaluation);
				result.append("\n"+evaluation.toLimitedString(offset+"     "));
			}
		}
		return result.toString();
	}
}
