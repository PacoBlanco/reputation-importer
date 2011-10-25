package cross.reputation.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Resource;

public class CategoricScale extends Scale {
	private Resource resource;
	private Set<String> categories;
	
	public CategoricScale() {		
	}
	
	
	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}
	public Set<String> getCategories() {
		return categories;
	}
	public void setCategories(Set<String> categories) {
		this.categories = categories;
	}
	public void addCategories(String category) {
		if(categories == null) {
			categories = new HashSet<String>(); 
		}
		categories.add(category);
	}
	
	public CategoricScale(String name, Set<String> categories) {
		super(name);
		this.categories = categories;
	}
	
	public boolean belongToScale(Category category) {
		return categories.contains(category);
		//Mirar si contains compara punteros o strings
	}

	@Override
	public Object adaptToScale(Object value) {
		if(value instanceof String && categories.contains(value))
			return value;
		return null;
	}

	@Override
	public Object addTrust(Object value, Double trust) {		
		return value;
	}

	@Override
	public Object doAverage(Object value, int elements) {
		return value;
	}

	@Override
	public Object sumValues(Object value, Object valueToSum) {
		if(value == null)
			return valueToSum;
		return value;
	}
	@Override
	public Object mulValues(Object value, Object valueToSum, Double weight) {
		if(value == null)
			return valueToSum;
		return value;
	}
	@Override
	public Object aggregateValues(Map<CommunityMetricToImport,Object> values) {
		return null;
	}

	@Override
	public String toString(String offset) {
		String string = offset+"name:"+getName()+"\n";
		string += offset+"categories:";
		for(String category : categories) {
			string += category+", ";
		}
		return string;
	}
}
