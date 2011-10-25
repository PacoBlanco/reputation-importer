package es.upm.dit;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cross.reputation.model.ModelException;

public class Util {
	static public String getXmlBaseFromFile(String modelPath) 
			throws SAXException, IOException, ParserConfigurationException {
		File file = new File(modelPath);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);
		doc.getDocumentElement().normalize();
		NodeList nodeLst = doc.getElementsByTagName("rdf:RDF");
		if(nodeLst.getLength() >= 1) {
			Node node2 = nodeLst.item(0);
			NamedNodeMap attributes = node2.getAttributes();
			for(int index = 0; index < attributes.getLength(); index++) {
				Node attribute = attributes.item(index);
				if(attribute.getNodeName().equalsIgnoreCase("xml:base")) {
					return attribute.getNodeValue();
				} 
			}
		} else {
			try {
				ModelException.sendMessage(ModelException.WARNING, "rdf:DRF not found in the"
						+" Document to parse. Jena should launch a error.");
			} catch (Exception e) {}
		}
		try {
			ModelException.sendMessage(ModelException.INFO, "xml:base not found in the"
					+" Document to parse.");
		} catch (Exception e) {}
		return doc.getBaseURI();
	}
}
