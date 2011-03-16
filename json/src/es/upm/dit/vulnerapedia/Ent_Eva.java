package es.upm.dit.vulnerapedia;

import java.util.List;

import cross.reputation.model.Community;
import cross.reputation.model.Entity;
import cross.reputation.model.Evaluation;

public class Ent_Eva {
	//Evaluation evaluation;
	Double value = null;
	Entity entity;
	
	static final int USER = 0;
	static final int USER_PREDEFINED = 1;
	int type = USER;
	
	public Ent_Eva(Entity entity, Double value) {
		this.entity = entity;
		this.value = value;
		this.type = USER_PREDEFINED;
	}
	
	public Ent_Eva(List<Evaluation> evaluations, Entity entity, Community destCommunity) {
		this.entity = entity;
		this.type = USER;
		for(Evaluation evaluation : evaluations) {
			if(evaluation.getDestinationEntity() != entity || 
					evaluation.getCommunity() != destCommunity) {
				continue;
			}
			if(evaluation.getValues().size() == 1) {
				value = (Double) evaluation.getValues().get(0);
			}
		}
	}
	
	public Double getValue() {
		return value;
	}
	
	public Entity getEntity() {
		return entity;
	}
	
	public int getType() {
		return type;
	}
}
