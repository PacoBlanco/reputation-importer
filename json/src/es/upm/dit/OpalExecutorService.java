package es.upm.dit;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class OpalExecutorService {
	
	private ExecutorService exec;
	private String userName;
	private double opalSum = 0;
	private int timeThreshold = Property.getTimeThreshold();
	
	OpalExecutorService(int threads, String userName, int timeThreshold) {
		exec = Executors.newFixedThreadPool(threads);
		this.userName = userName;
		this.timeThreshold = timeThreshold;
	}
	
	public synchronized void execute(final String postURL) {
		exec.execute(new Runnable() {
			public void run(){
				try {	
					opalSum += Scrapper.informacionPostsSlackers(userName,Ejecutor.executeScrappy(postURL, "0"));
					
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
