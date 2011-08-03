package cross.reputation.model;

public class FixedCommunitiesTrust extends TrustBetweenCommunities {
	Community communityScorer;
	Community communityScored;
	
	public FixedCommunitiesTrust() {
		super();
	}
	
	public FixedCommunitiesTrust(Community communityScorer, 
			Community communityScored, double value) {
		this.communityScorer = communityScorer;
		this.communityScored = communityScored;
		this.value = value;
	}
	
	public Community getCommunityScorer() {
		return communityScorer;
	}
	public void setCommunityScorer(Community communityScorer) {
		this.communityScorer = communityScorer;
	}
	public Community getCommunityScored() {
		return communityScored;
	}
	public void setCommunityScored(Community communityScored) {
		this.communityScored = communityScored;
	}
	
	public String toString(String offset) {
		String string = super.toString(offset);
		string += offset+"communityScorer:"+communityScorer+"\n";
		string += offset+"communityScored:"+communityScored+"\n";
		return string;
	}
	
}
