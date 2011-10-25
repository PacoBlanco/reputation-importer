package es.upm.dit;

public class ExecutorFactory {
	Executor newInstance(String name) {
		if(name == "Scrappy") {
			return new ScrappyExecutor();
		} 
		return null;
		//else if(name == "Opal") {
		//	return new OpalExecutorService();
		//}
	}
}
