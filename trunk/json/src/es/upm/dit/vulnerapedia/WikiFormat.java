package es.upm.dit.vulnerapedia;

public class WikiFormat {
	static public String AddObjectProperty(String propertyName, String value) {
		return "[["+propertyName+"::"+value+"]]";
	}
	
	static public String AddOccultObjectProperty(String propertyName, String value) {
		return "[["+propertyName+"::"+value+"| ]]";
	}
	
	static public String AddSetObjectProperty(String propertyName, String value) {
		return "{{#set:\n|"+propertyName+"="+value+"\n}}";		
	}
	
	static public String AddDatatypeProperty(String propertyName, String value) {
		return "[["+propertyName+":="+value+"]]";
	}
	
	static public String AddReference(String propertyName, String pseudonimum) {
		if(pseudonimum == null)
			return "[[:"+propertyName+"]]";
		else {
			return "[[:"+propertyName+"|"+pseudonimum+"]]";
		}
	}
	
	static public String AddPropertyReference(String propertyName, String pseudonum) {
		if(propertyName.startsWith("Property:")) {
			propertyName.substring(9);
		}
		if(pseudonum == null)
			return "[[Property:"+propertyName+"|"+propertyName+"]]";
		else {
			return "[[Property:"+propertyName+"|"+pseudonum+"]]";
		}
	}
	
	static public String AddPropertyTitle2(String titleName) {
		return "== [[Property:"+titleName+"|"+titleName+"]] ==";
	}
	
	static public String AddClass(String category) {
		if(category.startsWith("Category:")) {
			return "[["+category+"]]";
		}
		return "[[Category:"+category+"]]";
	}
	
	static public String AddTableStatement(int border) {
		if(border < 0) {
			return "{| border=\"0\"\n";
		}
		return "{| border=\""+border+"\"\n";
		//width=80%
		// cellpadding=\"10\" cellspacing=\"30\"
	}
	
	static public String AddNewRow() {
		return "|-\n";
	}
	
	static public String AddHeaderTable(String header) {
		return "! "+header+"\n";
	}
	
	static public String AddColumnTable(String value) {
		return "| "+value+"\n";
	}
	
	static public String AddClosingTable () {
		return "|}\n";
	}
	
	static public String AddTitle3(String titleName) {
		return "<u>'''"+titleName+"'''</u>";
	}
	
	static public String EscapeSymbols(String line) {
		return line.replace("[", "&#x5B;").replace("]", "&#x5D;").
				replace("|", "&#x7C;").replace("::","&#x3A;&#x3A;").
				replace("!","&#x21;").replace("{","&#x7B;");
	}
	
}
