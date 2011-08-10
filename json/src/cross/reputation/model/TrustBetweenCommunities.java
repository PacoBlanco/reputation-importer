package cross.reputation.model;

import java.util.ArrayList;
import java.util.List;

public class TrustBetweenCommunities {
	Double value;
	List<TrustBetweenCommunities> trustProvidedBy = 
		new ArrayList<TrustBetweenCommunities>();
	
	public TrustBetweenCommunities(){		
	}
	
	public void setValue(Double value) {
		this.value = value;
	}

	public Double getValue() {
		return value;
	}
	
	public void addTrustProvidedBy(TrustBetweenCommunities tbc) {
		trustProvidedBy.add(tbc);
	}

	public List<TrustBetweenCommunities> getTrustProvidedBy() {
		return trustProvidedBy;
	}
	
	public String toString(String offset) {
		String string = offset+"value:"+value;
		for(TrustBetweenCommunities truBetInside : trustProvidedBy) {
			string += "\n"+truBetInside.toString(offset+"     ");		
		}
		return string;
	}
}
