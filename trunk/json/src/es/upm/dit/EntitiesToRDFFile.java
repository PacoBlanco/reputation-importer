package es.upm.dit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.vocabulary.RDF;

import cross.reputation.model.Entity;
import cross.reputation.model.EntityIdentifier;
import cross.reputation.model.JenaVocabulary;
import cross.reputation.model.ModelException;
import cross.reputation.model.ReputationEvaluation;
import cross.reputation.model.ReputationObject;
import cross.reputation.model.ReputationValue;

public class EntitiesToRDFFile {
	String base = "";
	
	public void writeToRDFFile(Map<String,Entity> entities, String filePath, 
			ReputationParser reputationParser) throws Exception {
		if(entities == null) {
			return;
		}
		Model model;
		if(es.upm.dit.Property.getIMPORTATION_MODEL_MODE() ==
				es.upm.dit.Property.INTEGRATION) {
			model = reputationParser.getModel();
			for(Entity entity : entities.values()) {
				 updateEntityToModel(model, entity, reputationParser, base);			 		
			}
		} else {
			model = ModelFactory.createDefaultModel();
			for(Entity entity : entities.values()) {
				 addEntityToModel(model, entity, reputationParser, base);			 		
			}
		}		
		File file = new File(filePath);
	    FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		RDFWriter writer = model.getWriter("RDF/XML-ABBREV");
		writer.setProperty("allowBadURIs", true);
		if(es.upm.dit.Property.getIMPORTATION_BASE_URI() ==
				es.upm.dit.Property.XML_BASE) {
			if(es.upm.dit.Property.getImportation_xml_base() == null ||
					es.upm.dit.Property.getImportation_xml_base().trim().isEmpty()) {	
				base = reputationParser.getBase();
			} else {
				base = es.upm.dit.Property.getImportation_xml_base();
			}
			if(base == null) {
				ModelException.throwException(ModelException.BASE_URI, "Base URI Mode in "+
						"importation Document is set to xml:base but no xml:base was defined" +
						"in property file or in the parsed document");
			}
			writer.setProperty("xmlbase", base);
		}
		if(es.upm.dit.Property.getIMPORTATION_BASE_URI() ==
				es.upm.dit.Property.FILE_PATH) {
			base = filePath;
			writer.setProperty("xmlbase", base);
		}
		if(es.upm.dit.Property.getIMPORTATION_BASE_URI() ==
				es.upm.dit.Property.ABSOLUTE_PATH) {
			base = file.getAbsolutePath();
			writer.setProperty("xmlbase", base);
		}
		if(reputationParser.getRiNamespace() != null) {
			model.setNsPrefix( "ri", reputationParser.getRiNamespace() );
		}
		if(reputationParser.getFoafNamespace() != null) {
			model.setNsPrefix( "foaf", reputationParser.getFoafNamespace() );
		}
		if(reputationParser.getDcNamespace() != null) {
			model.setNsPrefix( "dc", reputationParser.getDcNamespace() );
		}
	    // now write the model in XML form to a file
		writer.write(model, fos, base);
	    //model.write(fos, "RDF/XML-ABBREV");
		try {
			fos.close();
		} catch (IOException e1) {			
		}
	    try {
			ModelException.sendMessage(ModelException.INFO, "Entities model was" +
					" written in file with path:"+file.getAbsolutePath());
		} catch (Exception e) {			
		}
	}
	
	static public void updateEntityToModel(Model model, Entity entity, 
			ReputationParser reputationParser, String base) {
		if(entity.getResource() == null) {
			addEntityToModel(model, entity, reputationParser, base);
			return;
		}
		Resource entityRes = entity.getResource();
		if(!entityRes.hasProperty(RDF.type, JenaVocabulary.entity)) {
			entityRes.addProperty(RDF.type, JenaVocabulary.entity);
		}
		Property property = model.createProperty(reputationParser
				 .getRiNamespace(), "identifier");
		if(entity.getUniqueIdentificator() != null) {
			Statement statement = entityRes.getProperty(property);
			if(statement == null) {
				entityRes.addProperty(property, 
						 entity.getUniqueIdentificator());
			} else {
				statement.changeObject(entity.getUniqueIdentificator());
			}
		}
		property = model.createProperty(reputationParser
				 .getFoafNamespace(), "onlineAccount");
		if(entity.getIdentificatorInCommunities() != null) {
			for(EntityIdentifier entityIdentifier : 
				 	entity.getIdentificatorInCommunities().values()) {
				Resource accResource = updateOnlineAccount(model, 
						entityIdentifier, reputationParser, base, entity);
				if(accResource == null) {
					 continue;
				}
				entityRes.addProperty(property,accResource);
			}
		}
		property = model.createProperty(reputationParser
				 .getRiNamespace(), "hasEvaluation");
		if(entity.getHasEvaluation() != null) {
			for(ReputationEvaluation repEva : entity.getHasEvaluation()) {
				Resource repEvaResource = updateReputationEvaluationToModel(model, 
						repEva, reputationParser, base, entity, null);
				if(repEvaResource == null) {
					 continue;
				}
				entityRes.addProperty(property,repEvaResource);
			}
		}
		property = model.createProperty(reputationParser
				 .getRiNamespace(), "hasValue");
		if(entity.getHasValue() != null) {
			for(ReputationValue repVal : entity.getHasValue()) {
				Resource repValResource = updateReputationValueToModel(model, 
						repVal, reputationParser, base, entity, null);
				if(repValResource == null) {
					 continue;
				}
				entityRes.addProperty(property,repValResource);
			}
		}
		property = model.createProperty(reputationParser
				 .getRiNamespace(), "hasReputation");
		if(entity.getHasReputation() != null) {
			for(ReputationObject repObj : entity.getHasReputation()) {
				Resource repObjResource = updateReputationObjectToModel(model, 
						repObj, reputationParser, base, entity);
				if(repObjResource == null) {
					 continue;
				}
				entityRes.addProperty(property,repObjResource);
			}
		}
	}
	
	static public Resource updateReputationObjectToModel(Model model, 
			ReputationObject reputationObject, 
			ReputationParser reputationParser, String base, Entity owner) {
		if(reputationObject.getResource() == null) {
			return addReputationObjectToModel(model, reputationObject, 
					reputationParser, base, owner);
		}
		Resource repObjResource = reputationObject.getResource();
		if(!repObjResource.hasProperty(RDF.type, JenaVocabulary.reputationObject)) {
			repObjResource.addProperty(RDF.type, JenaVocabulary.reputationObject);
		}
		Entity finalOwner = getFinalOwner(null, null, reputationObject, 
					owner, true);
		if(finalOwner != null) {			
			Property property = model.createProperty(reputationParser
					 .getRiNamespace(), "owner");
			Statement statement = repObjResource.getProperty(property);
			if(statement == null) {
				repObjResource.addProperty(property, finalOwner.getResource());
			} else {
				statement.changeObject(finalOwner.getResource());
			}
		}
		Property property = model.createProperty(reputationParser
				 .getRiNamespace(), "fromCommunity");
		Statement statement = repObjResource.getProperty(property);
		if(reputationObject.getFromCommunity() != null) {			
			if(reputationObject.getFromCommunity().getResource() != null) {
				if(statement == null) {
					repObjResource.addProperty(property, 
							reputationObject.getFromCommunity().getResource());
				} else {
					statement.changeObject(reputationObject.getFromCommunity().getResource());
				}
			}
		}
		property = model.createProperty(reputationParser
				 .getRiNamespace(), "hasValue");
		if(reputationObject.getHasValue() != null) {
			for(ReputationValue repVal : reputationObject.getHasValue()) {
				Resource repValResource = updateReputationValueToModel(model, 
						repVal, reputationParser, base, owner, reputationObject);
				if(repValResource == null) {
					 continue;
				}
				repObjResource.addProperty(property,repValResource);
			}
		}
		return null;
	}
	
	static public Resource updateReputationValueToModel(Model model, 
			ReputationValue reputationValue, 
			ReputationParser reputationParser, String base, Entity owner, 
			ReputationObject repObj) {
		if(reputationValue.getResource() == null) {
			return addReputationValueToModel(model,reputationValue, 
					reputationParser, base, owner, repObj);
		}
		Resource repValResource = reputationValue.getResource();
		if(!repValResource.hasProperty(RDF.type, JenaVocabulary.reputationValue)) {
			repValResource.addProperty(RDF.type, JenaVocabulary.reputationValue);
		}
		Entity finalOwner = getFinalOwner(null, reputationValue, repObj, 
					owner, true);
		if(finalOwner != null) {			
			Property property = model.createProperty(reputationParser
					 .getRiNamespace(), "owner");
			Statement statement = repValResource.getProperty(property);
			if(statement == null) {
				repValResource.addProperty(property, finalOwner.getResource());
			} else {
				statement.changeObject(finalOwner.getResource());
			}
		}
		Property property = model.createProperty(reputationParser
				 .getRiNamespace(), "obtainedBy");
		Statement statement = repValResource.getProperty(property);
		if(reputationValue.getObtainedBy() != null) {			
			if(reputationValue.getObtainedBy().getResource() != null) {
				if(statement == null) {
					repValResource.addProperty(property, 
							reputationValue.getObtainedBy().getResource());
				} else {
					statement.changeObject(reputationValue.getObtainedBy().getResource());
				}
			}
		}
		if(reputationValue.getCollectionIdentifier() != null) {
			property = model.createProperty(reputationParser
					 .getRiNamespace(), "collectionIdentifier");
			statement = repValResource.getProperty(property);			
			if(statement == null) {
				repValResource.addLiteral(property, 
						reputationValue.getCollectionIdentifier());
			} else {
				statement.changeObject(reputationValue.getCollectionIdentifier());
			}
		}
		Calendar cal = GregorianCalendar.getInstance();
		if(reputationValue.getTimeStamp() != null) {
			property = model.createProperty(reputationParser
					 .getRiNamespace(), "timeStamp");			
			cal.setTime(reputationValue.getTimeStamp());
			Literal date = model.createTypedLiteral(cal);
			statement = repValResource.getProperty(property);			
			if(statement == null) {
				repValResource.addLiteral(property, date);
			} else {
				statement.changeObject(date);
			}
		}
		if(reputationValue.getExpirationTime() != null) {
			property = model.createProperty(reputationParser
					 .getRiNamespace(), "expirationDate");
			statement = repValResource.getProperty(property);
			cal.setTime(reputationValue.getExpirationTime());
			Literal date = model.createTypedLiteral(cal);
			if(statement == null) {
				repValResource.addLiteral(property, date);
			} else {
				statement.changeObject(date);
			}
		}
		property = model.createProperty(reputationParser
				 .getRiNamespace(), "hasEvaluation");
		if(reputationValue.getHasEvaluations() != null) {
			for(ReputationEvaluation repEva : reputationValue.getHasEvaluations()) {
				Resource repEvaResource = updateReputationEvaluationToModel(model, 
						repEva, reputationParser, base, owner, reputationValue);
				if(repEvaResource == null) {
					 continue;
				}
				repValResource.addProperty(property,repEvaResource);
			}
		}		
		return null;
	}
	
	static public Resource updateReputationEvaluationToModel(Model model, 
			ReputationEvaluation reputationEvaluation, 
			ReputationParser reputationParser, String base, Entity owner,
			ReputationValue repValue) {
		if(reputationEvaluation.getResource() == null) {
			return addReputationEvaluationToModel(model,reputationEvaluation, 
					reputationParser, base, owner, repValue);
		}
		Resource repEvaResource = reputationEvaluation.getResource();
		if(!repEvaResource.hasProperty(RDF.type, JenaVocabulary.reputationEvaluation)) {
			repEvaResource.addProperty(RDF.type, JenaVocabulary.reputationEvaluation);
		}
		Entity finalOwner = getFinalOwner(reputationEvaluation, repValue, null, 
				owner, true);
		if(finalOwner != null) {			
			Property property = model.createProperty(reputationParser
					 .getRiNamespace(), "owner");
			Statement statement = repEvaResource.getProperty(property);
			if(statement == null) {
				repEvaResource.addProperty(property, finalOwner.getResource());
			} else {
				statement.changeObject(finalOwner.getResource());
			}
		}	
		
		Property property = model.createProperty(reputationParser
				 .getRiNamespace(), "hasMetric");
		Statement statement = repEvaResource.getProperty(property);
		if(reputationEvaluation.getHasMetric() != null &&
				reputationEvaluation.getHasMetric().getResource() != null) {
			if(statement == null) {
				repEvaResource.addProperty(property, 
						reputationEvaluation.getHasMetric().getResource());
			} else {
				statement.changeObject(reputationEvaluation.getHasMetric().getResource());
			}
		}
		if(reputationEvaluation.getCollectionIdentifier() != null) {
			property = model.createProperty(reputationParser
					 .getRiNamespace(), "collectionIdentifier");
			statement = repEvaResource.getProperty(property);			
			if(statement == null) {
				repEvaResource.addLiteral(property, 
						reputationEvaluation.getCollectionIdentifier());
			} else {
				statement.changeObject(reputationEvaluation.getCollectionIdentifier());
			}
		}
		if(reputationEvaluation.getValue() != null) {
			property = model.createProperty(reputationParser
					 .getRiNamespace(), "value");
			statement = repEvaResource.getProperty(property);			
			if(statement == null) {
				repEvaResource.addLiteral(property, 
						reputationEvaluation.getValue());
			} else {
				statement.changeObject(reputationEvaluation.getValue().toString());
			}
		}
		return null;		
	}
	
	static public Resource updateOnlineAccount(Model model, EntityIdentifier entityIdentifier, 
			ReputationParser reputationParser, String base, Entity owner) {		
		if(entityIdentifier.getResource() != null) {
			Resource accResource = entityIdentifier.getResource();
			Property property = model.createProperty(reputationParser
					 .getFoafNamespace(), "accountName");
			Statement statement = accResource.getProperty(property);			
			if(statement == null) {
				accResource.addLiteral(property, 
						entityIdentifier.getName());
			} else {
				statement.changeObject(entityIdentifier.getName());
			}
			property = model.createProperty(reputationParser
					 .getFoafNamespace(), "accountProfilePage");
			statement = accResource.getProperty(property);			
			if(statement == null) {
				accResource.addLiteral(property, 
						entityIdentifier.getUrl());
			} else {
				statement.changeObject(entityIdentifier.getUrl());
			}
			property = model.createProperty(reputationParser
					 .getRiNamespace(), "belongsTo");
			statement = accResource.getProperty(property);
			if(entityIdentifier.getBelongsTo().getResource() != null) {
				if(statement == null) {
					accResource.addProperty(property, 
							entityIdentifier.getBelongsTo().getResource());
				} else {
					statement.changeObject(entityIdentifier.getBelongsTo().getResource());
				}
			}
			return null;
		} else {
			return addOnlineAccountToModel(model, entityIdentifier,reputationParser, base, owner);
		}
	}
	
	static public void addEntityToModel(Model model, Entity entity, 
			ReputationParser reputationParser, String base) {
		 Resource entityRes = null;
		 if(entity.getResource() != null) {
			 entityRes = model.createResource(getResourceString(
					 entity.getResource().getURI(),base));
		 } else {
			 if(entity.getUniqueIdentificator() == null) {
				 return;
			 }
			 entityRes = model.createResource(getResourceString(
					 base+entity.getUniqueIdentificator(),base));
		 }
		 entityRes.addProperty(RDF.type, JenaVocabulary.entity);
		 if(entity.getUniqueIdentificator() != null) {
			 Property uniqIdProperty = model.createProperty(reputationParser
					 .getRiNamespace(), "identifier");
			 entityRes.addProperty(uniqIdProperty, 
					 entity.getUniqueIdentificator());
		 } else {
			return; 
		 }
		 if(entity.getIdentificatorInCommunities() != null) {
			 for(EntityIdentifier entityIdentifier : 
				 	entity.getIdentificatorInCommunities().values()) {
				 Property property = model.createProperty(reputationParser
						 .getFoafNamespace(), "onlineAccount");
				 Resource resource = addOnlineAccountToModel(model, 
						 entityIdentifier, reputationParser, base, entity);
				 if(resource == null) {
					 continue;
				 }
				 entityRes.addProperty(property,resource);
			 }
		 }
		 if(entity.getHasReputation() != null) {
			 for(ReputationObject reputationObject : 
				 	entity.getHasReputation()) {
				 Property property = model.createProperty(reputationParser
						 .getRiNamespace(), "hasReputation");
				 Resource resource = addReputationObjectToModel(model, 
						 reputationObject, reputationParser, base, entity);
				 if(resource == null) {
					 continue;
				 }
				 entityRes.addProperty(property,resource);
			 }
		 }
		 if(entity.getHasValue() != null) {
			 for(ReputationValue reputationValue : 
				 	entity.getHasValue()) {
				 Property property = model.createProperty(reputationParser
						 .getRiNamespace(), "hasValue");
				 Resource resource = addReputationValueToModel(model, 
						 reputationValue, reputationParser, base, entity, null);
				 if(resource == null) {
					 continue;
				 }
				 entityRes.addProperty(property,resource);
			 } 
		 }
		 if(entity.getHasEvaluation() != null) {
			 for(ReputationEvaluation reputationEvaluation : 
				 	entity.getHasEvaluation()) {
				 Property property = model.createProperty(reputationParser
						 .getRiNamespace(), "hasEvaluation");
				 Resource resource = addReputationEvaluationToModel(model, 
						 reputationEvaluation, reputationParser, base, entity, null);
				 if(resource == null) {
					 continue;
				 }
				 entityRes.addProperty(property,resource);
			 } 
		 }
		 
	}
	
	static public String getResourceString(String resourceString, String base) {
		/*System.out.println(resourceString);
		resourceString = resourceString.replace(" ", "");
		if(resourceString.startsWith("#")) {
			resourceString = resourceString.replaceAll("#", "");
			resourceString = base + resourceString;
		}
		System.out.println(resourceString);*/
		return resourceString;
	}
	
	static public Entity getFinalOwner(ReputationEvaluation repEvaluation,
			ReputationValue repValue, ReputationObject repObject, 
			Entity owner, boolean needResourceSet) {
		if(repEvaluation != null && repEvaluation.getTarget() != null && 
				repEvaluation.getTarget().getUniqueIdentificator() != null) {
			if(!needResourceSet || repEvaluation.getTarget().getResource() != null) {
				return repEvaluation.getTarget();
			}
		} 
		if(repValue != null && repValue.getOwner() != null && 
				repValue.getOwner().getUniqueIdentificator() != null) {
			if(!needResourceSet || repValue.getOwner().getResource() != null) {
				return repObject.getOwner();
			}
		} 
		if(repObject != null && repObject.getOwner() != null && 
				repObject.getOwner().getUniqueIdentificator() != null) {
			if(!needResourceSet || repObject.getOwner().getResource() != null) {
				return repObject.getOwner();
			}
		} else if(owner != null && owner.getUniqueIdentificator() != null) {
			if(!needResourceSet || owner.getResource() != null) {
				return owner;
			}
		} 
		return null;
	}			
	
	static public Resource addReputationObjectToModel(Model model, 
			ReputationObject reputationObject, 
			ReputationParser reputationParser, String base, Entity owner) {
		if(reputationObject.getFromCommunity() == null ||
				reputationObject.getFromCommunity().getResource() == null ||
				reputationObject.getFromCommunity().getName() == null) {
			return null;
		}
		Entity finalOwner = getFinalOwner(null, null, reputationObject, 
				owner, true);
		if(finalOwner == null) {
			return null;
		}
		Resource repObjectRes = null;
		if(reputationObject.getResource() != null) {
			repObjectRes = model.createResource(getResourceString(
					reputationObject.getResource().getURI(),base));
		} else if(owner != null) {
			repObjectRes = model.createResource();
		} else {
			repObjectRes = model.createResource(getResourceString(
				base+finalOwner.getUniqueIdentificator()+"-"+
				reputationObject.getFromCommunity().getName(),base));
		}
		repObjectRes.addProperty(RDF.type, JenaVocabulary.reputationObject);
		Property property = model.createProperty(reputationParser
				 .getRiNamespace(), "owner");
		repObjectRes.addProperty(property, finalOwner.getResource());
		property = model.createProperty(reputationParser
				 .getRiNamespace(), "fromCommunity");
		repObjectRes.addProperty(property, reputationObject.getFromCommunity().getResource());
		if(reputationObject.getHasValue() != null) {
			for(ReputationValue reputationValue : 
				reputationObject.getHasValue()) {
				 Property properti = model.createProperty(reputationParser
						 .getRiNamespace(), "hasValue");
				 Resource resource = addReputationValueToModel(model, 
						 reputationValue, reputationParser, base, finalOwner, reputationObject);
				 if(resource == null) {
					 continue;
				 }
				 repObjectRes.addProperty(properti,resource);
			 }
		}
		return repObjectRes;
	}
	
	static public Resource addReputationValueToModel(Model model, 
			ReputationValue reputationValue, 
			ReputationParser reputationParser, String base, Entity owner, 
			ReputationObject repObj) {
		Entity finalOwner = getFinalOwner(null, reputationValue, repObj, 
				owner, true);
		if(finalOwner == null) {
			return null;
		}
		Resource repValueRes = null;
		if(reputationValue.getResource() != null) {
			repValueRes = model.createResource(getResourceString(
					reputationValue.getResource().getURI(),base));
		} else if(owner != null || repObj != null){
			repValueRes = model.createResource();
		} else {
			if(reputationValue.getObtainedBy().getResource().getURI() != null) {
				repValueRes = model.createResource(getResourceString(
						base+finalOwner.getUniqueIdentificator()
						+"-"+reputationValue.getObtainedBy().getResource(),base));
			} else {
				repValueRes = model.createResource(getResourceString(
					base+finalOwner.getUniqueIdentificator()
					+"-"+reputationValue.getObtainedBy().getName(),base));
			}
		}
		repValueRes.addProperty(RDF.type, JenaVocabulary.reputationValue);
		Property property = model.createProperty(reputationParser
				 .getRiNamespace(), "owner");
		repValueRes.addProperty(property, finalOwner.getResource());
		property = model.createProperty(reputationParser
				 .getRiNamespace(), "obtainedBy");
		repValueRes.addProperty(property,reputationValue.getObtainedBy().getResource());
		if(reputationValue.getTimeStamp() != null) {
			property = model.createProperty(reputationParser
					 .getRiNamespace(), "timeStamp");
			//repObjectRes.addProperty(property,reputationValue.getTimeStamp());
			Calendar cal = GregorianCalendar.getInstance();
			cal.setTime(reputationValue.getTimeStamp());
			Literal date = model.createTypedLiteral(cal);
			repValueRes.addLiteral(property,date);
		}
		if(reputationValue.getExpirationTime() != null) {
			property = model.createProperty(reputationParser
					 .getRiNamespace(), "expirationTime");
			//repObjectRes.addProperty(property,reputationValue.getExpirationTime());
			Calendar cal = GregorianCalendar.getInstance();
			cal.setTime(reputationValue.getExpirationTime());
			Literal date = model.createTypedLiteral(cal);
			repValueRes.addLiteral(property,date);
		}
		if(reputationValue.getCollectionIdentifier() != null) {
			property = model.createProperty(reputationParser
					 .getRiNamespace(), "collectionIdentifier");
			repValueRes.addProperty(property,reputationValue.getCollectionIdentifier());
		}
		if(reputationValue.getCollectionIdentifier() != null) {
			repValueRes.addProperty(property,reputationValue.getCollectionIdentifier());
		}
		if(reputationValue.getHasEvaluations() != null) {
			for(ReputationEvaluation reputationEvaluation : 
				reputationValue.getHasEvaluations()) {
				Property properti = model.createProperty(reputationParser
						 .getRiNamespace(), "hasEvaluation");
				Resource resource = addReputationEvaluationToModel(model, 
					 reputationEvaluation, reputationParser, base, finalOwner, reputationValue);
				if(resource == null) {
					continue;
				}
				repValueRes.addProperty(properti,resource);
			}
		}
		return repValueRes;
	}	
	
	static public Resource addReputationEvaluationToModel(Model model, 
			ReputationEvaluation reputationEvaluation, 
			ReputationParser reputationParser, String base, Entity owner,
			ReputationValue repValue) {
		if(reputationEvaluation.getValue() == null) {
			return null;
		}
		Entity finalOwner = getFinalOwner(reputationEvaluation, repValue,
				null, owner, true);
		if(finalOwner == null) {
			return null;
		}
		Resource repEvaluationRes = null;
		if(reputationEvaluation.getResource() != null) {
			repEvaluationRes = model.createResource(getResourceString(
					reputationEvaluation.getResource().getURI(),base));
		} else if(owner != null || repValue != null) {
			repEvaluationRes = model.createResource();
		} else {
			if(reputationEvaluation.getCollectionIdentifier() != null) {
				repEvaluationRes = model.createResource(getResourceString(
						base+finalOwner.getUniqueIdentificator()+"-"+
						reputationEvaluation.getCollectionIdentifier()+"-"+
						reputationEvaluation.getValue(),base));
			} else {
				repEvaluationRes = model.createResource(getResourceString(
					base+finalOwner.getUniqueIdentificator()+"-"+
					reputationEvaluation.getValue(),base));
			}
		}
		repEvaluationRes.addProperty(RDF.type, JenaVocabulary.reputationEvaluation);
		Property property = model.createProperty(reputationParser
				 .getRiNamespace(), "target");
		repEvaluationRes.addProperty(property, finalOwner.getResource());
		property = model.createProperty(reputationParser
				 .getRiNamespace(), "hasMetric");
		if(reputationEvaluation.getHasMetric() != null &&
				reputationEvaluation.getHasMetric().getResource() != null) {
			repEvaluationRes.addProperty(property,
					reputationEvaluation.getHasMetric().getResource());
		}
		if(reputationEvaluation.getCollectionIdentifier() != null) {
			property = model.createProperty(reputationParser
					 .getRiNamespace(), "collectionIdentifier");
			repEvaluationRes.addProperty(property,
					reputationEvaluation.getCollectionIdentifier());
		}
		if(reputationEvaluation.getValue() != null) {
			property = model.createProperty(reputationParser
					 .getRiNamespace(), "value");
			//repEvaluationRes.addProperty(property, reputationEvaluation.getValue().toString());
			repEvaluationRes.addLiteral(property,reputationEvaluation.getValue());
		}
		return repEvaluationRes;
	}
	
	static public Resource addOnlineAccountToModel(Model model, 
			EntityIdentifier entityIdentifier, ReputationParser reputationParser, 
			String base, Entity owner) {
		if(entityIdentifier.getName() == null && entityIdentifier.getUrl() == null) {
			return null;
		}
		/*if(entityIdentifier.getBelongsTo() == null || 
				entityIdentifier.getBelongsTo().getResource() == null) {
			return null;
		}*/
		Resource onlineAccount = null;
		if(entityIdentifier.getResource() != null) {
			onlineAccount = model.createResource(getResourceString(
					entityIdentifier.getResource().getURI(),base));
		} else if(owner != null) {
			onlineAccount = model.createResource();
		} else if(entityIdentifier.getUrl() != null) {
			onlineAccount = model.createResource(getResourceString
					(base+entityIdentifier.getUrl(),base));
		} else if(entityIdentifier.getName() != null) {
			if(entityIdentifier.getBelongsTo() != null && 
					entityIdentifier.getBelongsTo().getName() != null) {
				onlineAccount = model.createResource(getResourceString(
						base+entityIdentifier.getName()+
						entityIdentifier.getBelongsTo().getName(),base));
			} else {
				onlineAccount = model.createResource(getResourceString(
					base+entityIdentifier.getName(),base));
			}
		} 
		onlineAccount.addProperty(RDF.type, JenaVocabulary.onlineAccount);
		if(entityIdentifier.getName() != null) {
			Property property = model.createProperty(reputationParser
					 .getFoafNamespace(), "accountName");
			onlineAccount.addProperty(property, 
					entityIdentifier.getName());
		}
		if(entityIdentifier.getUrl() != null) {
			Property property = model.createProperty(reputationParser
					 .getFoafNamespace(), "accountProfilePage");
			onlineAccount.addProperty(property, 
					entityIdentifier.getUrl());
		}
		if(entityIdentifier.getBelongsTo() != null && 
				entityIdentifier.getBelongsTo().getResource() != null) {
			Property property = model.createProperty(reputationParser
					.getRiNamespace(), "belongsTo");
			onlineAccount.addProperty(property,
					entityIdentifier.getBelongsTo().getResource());			
		}
		return onlineAccount;
	}
}
