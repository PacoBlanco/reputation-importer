package cross.reputation.model;

import java.util.Date;
import java.util.List;

public class ReputationValue {
	ReputationAlgorithm obtainedBy;
	String collectionIdentifier;
	Date timeStamp;
	Date expirationTime;
	List<Evaluation> hasEvaluations;
	Entity owner;
}
