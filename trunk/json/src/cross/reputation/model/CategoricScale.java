package cross.reputation.model;

import java.util.List;

public class CategoricScale extends Scale {
	private List<String> categories;
	
	public CategoricScale(String name, List<String> categories) {
		super(name);
		this.categories = categories;
	}
	
	public boolean belongToScale(String category) {
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
}
