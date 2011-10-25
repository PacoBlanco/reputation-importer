package es.upm.dit;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class OpalExecutorService {
	private ExecutorService exec;
	private String userName;
	private double opalSum = 0;
	private int threads = Property.getTHREAD_NUMBER();
	private int timeThreshold = Property.getTimeThreshold();
	
	public OpalExecutorService(String userName) {
		exec = Executors.newFixedThreadPool(threads);
		this.userName = userName;		
	}
	
	public void execute(final String postURL) {
		exec.execute(new Runnable() {
			public void run(){
				try {
					double opalFromPost = Scrapper.informacionPostsSlackers(
							userName,Ejecutor.executeScrappy(postURL, "0"));					
					synchronized (OpalExecutorService.this) {
						opalSum += opalFromPost;						
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
