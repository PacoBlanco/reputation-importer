package es.upm.dit;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class OpalExecutorService {
	ExecutorService exec;
	String userName;
	double opalSum = 0;
	int timeThreshold = 500;
	
	OpalExecutorService(int threads, String userName, int timeThreshold) {
		exec = Executors.newFixedThreadPool(threads);
		this.userName = userName;
		this.timeThreshold = timeThreshold;
	}
	
	public void execute(final String postURL) {
		exec.execute(new Runnable() {
			public void run(){
				try {
					if(postURL.contains("elhacker")){
						opalSum += Scrapper.informacionPostsSlackers(userName,
								Ejecutor.executeScrappy(postURL, "0"));
						opalSum += Scrapper.informacionPostsSlackers(userName,
								Ejecutor.executeScrappy(postURL+";start,15", "0"));
						opalSum += Scrapper.informacionPostsSlackers(userName,
								Ejecutor.executeScrappy(postURL+";start,30", "0"));
						opalSum += Scrapper.informacionPostsSlackers(userName,
								Ejecutor.executeScrappy(postURL+";start,45", "0"));
					}
					else{
						opalSum += Scrapper.informacionPostsSlackers(userName,
							Ejecutor.executeScrappy(postURL, "0"));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public Double shutdown() {
		exec.shutdown();
		try {
            boolean b = exec.awaitTermination(timeThreshold, TimeUnit.SECONDS);
            if (b){
            	return opalSum;            	
            }		                     
       } catch (InterruptedException e) {
            e.printStackTrace();
       }
       exec.shutdownNow();
       System.out.println("WARNING: opalExec is aborted without finishing");
       return opalSum;
	}
	
	
	
}
