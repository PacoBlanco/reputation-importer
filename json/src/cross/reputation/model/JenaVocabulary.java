package cross.reputation.model;

import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class JenaVocabulary {
	static String riNamespace = "http://purl.org/reputationImport/0.1/";
	static String foafNamespace = "http://purl.org/dc/elements/1.1/";
	
	static final String nEntity = "Entity";
	public static Resource entity = null;
	static final String nOnlineAccount = "OnlineAccount";
	public static Resource onlineAccount = null;
	static final String nReputationValue = "ReputationValue";
	public static Resource reputationValue = null;
	static final String nReputationObject = "ReputationObject";
	public static Resource reputationObject = null;
	static final String nReputationEvaluation = "ReputationEvaluation";
	public static Resource reputationEvaluation = null;
	static final String nCategoricScale = "CategoricScale";
	public static Resource categoricScale = null;
	static final String nCategory = "Category";
	public static Resource category = null;
	static final String nCategoryMatching = "CategoryMatching";
	public static Resource categoryMatching = null;
	static final String nCollectingAlgorithm = "CollectingAlgorithm";
	public static Resource collectingAlgorithm = null;
	static final String nCollectingSystem = "CollectingSystem";
	public static Resource collectingSystem = null;
	static final String nCollectionType = "ollectionType";
	public static Resource collectionType = null;
	static final String nCommunity = "Community";
	public static Resource community = null;
	static final String nDimension = "Dimension";
	public static Resource dimension = null;
	static final String nDimensionCorrelation = "DimensionCorrelation";
	public static Resource dimensionCorrelation = null;
	static final String nEntityType = "EntityType";
	public static Resource entityType = null;
	static final String nExponentialNumericTransformer = "ExponentialNumericTransformer";
	public static Resource exponentialNumericTransformer = null;
	static final String nFixedCommunitiesTrust = "FixedCommunitiesTrust";
	public static Resource fixedCommunitiesTrust = null;
	static final String nImportationUnit = "ImportationUnit";
	public static Resource importationUnit = null;
	static final String nLinealNumericTransformer = "LinealNumericTransformer";
	public static Resource linealNumericTransformer = null;
	static final String nLogaritmicNumericTransformer = "LogaritmicNumericTransformer";
	public static Resource logaritmicNumericTransformer = null;
	static final String nMetric = "Metric";
	public static Resource metric = null;
	static final String nMetricMapping = "MetricMapping";
	public static Resource metricMapping = null;
	static final String nMetricTransformer = "MetricTransformer";
	public static Resource metricTransformer = null;
	static final String nNumericScale = "NumericScale";
	public static Resource numericScale = null;
	static final String nNumericTransformer = "NumericTransformer";
	public static Resource numericTransformer = null;
	static final String nReputationalAction = "ReputationalAction";
	public static Resource reputationalAction = null;
	static final String nReputationAlgorithm = "ReputationAlgorithm";
	public static Resource reputationAlgorithm = null;
	static final String nReputationImporter = "ReputationImporter";
	public static Resource reputationImporter = null;
	static final String nReputationModel = "ReputationModel";
	public static Resource reputationModel = null;
	static final String nReputationModule = "ReputationModule";
	public static Resource reputationModule = null;
	static final String nScale = "Scale";
	public static Resource scale = null;
	static final String nScaleCorrelation = "ScaleCorrelation";
	public static Resource scaleCorrelation = null;
	static final String nSqrtNumericTransformer = "SqrtNumericTransformer";
	public static Resource sqrtNumericTransformer = null;
	static final String nTrustBetweenCommunities = "TrustBetweenCommunities";
	public static Resource trustBetweenCommunities = null;
	
	static {
		entity = new ResourceImpl(riNamespace+nEntity);
		onlineAccount = new ResourceImpl(foafNamespace+nOnlineAccount);
		reputationValue = new ResourceImpl(riNamespace+nReputationValue);
		reputationObject = new ResourceImpl(riNamespace+nReputationObject);
		reputationEvaluation = new ResourceImpl(riNamespace+nReputationEvaluation);
		categoricScale = ResourceFactory.createResource(
				riNamespace+nCategoricScale);
		category = ResourceFactory.createResource(riNamespace+nCategory);
		categoryMatching = ResourceFactory.createResource(
				riNamespace+nCategoryMatching);
		collectingAlgorithm = ResourceFactory.createResource(
				riNamespace+nCollectingAlgorithm);
		collectingSystem = ResourceFactory.createResource(
				riNamespace+nCollectingSystem);
		collectionType = ResourceFactory.createResource(
				riNamespace+nCollectionType);
		community = ResourceFactory.createResource(riNamespace+nCommunity);
		dimension = ResourceFactory.createResource(riNamespace+nDimension);
		dimensionCorrelation = ResourceFactory.createResource(
				riNamespace+nDimensionCorrelation);
		entityType = ResourceFactory.createResource(
				riNamespace+nEntityType);
		exponentialNumericTransformer = ResourceFactory.createResource(
				riNamespace+nExponentialNumericTransformer);
		fixedCommunitiesTrust = ResourceFactory.createResource(
				riNamespace+nFixedCommunitiesTrust);
		importationUnit = ResourceFactory.createResource(
				riNamespace+nImportationUnit);
		linealNumericTransformer = ResourceFactory.createResource(
				riNamespace+nLinealNumericTransformer);
	}

	public static String getRiNamespace() {
		return riNamespace;
	}

	public static void setRiNamespace(String riNamespace) {
		JenaVocabulary.riNamespace = riNamespace;
	}

	public static String getFoafNamespace() {
		return foafNamespace;
	}

	public static void setFoafNamespace(String foafNamespace) {
		JenaVocabulary.foafNamespace = foafNamespace;
	}	
	
}
