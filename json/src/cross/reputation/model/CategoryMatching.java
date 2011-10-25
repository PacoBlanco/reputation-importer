package cross.reputation.model;

public class CategoryMatching extends TrustBetweenCommunities {
	private Category originatingCategory;
	private Category receivingCategory;
		
	public CategoryMatching() {
		super();
	}
	
	public CategoryMatching(double value, Category originatingCategory,
			Category receivingCategory) {
		this.value = value;
		this.originatingCategory = originatingCategory;
		this.receivingCategory = receivingCategory;
	}
	
	public Category getOriginatingCategory() {
		return originatingCategory;
	}
	public void setOriginatingCategory(Category originatingCategory) {
		this.originatingCategory = originatingCategory;
	}
	public Category getReceivingCategory() {
		return receivingCategory;
	}
	public void setReceivingCategory(Category receivingCategory) {
		this.receivingCategory = receivingCategory;
	}
	
	public String toString(String offset) {
		String string = super.toString(offset);
		string += "\n"+offset+"originatingCategory:"+((originatingCategory==null)?"null":
			originatingCategory.getName())+"\n";
		string += offset+"receivingCategory:"+((receivingCategory==null)?"null":
			receivingCategory.getName());
		return string;
	}
}
