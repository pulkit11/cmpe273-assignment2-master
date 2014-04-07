package edu.sjsu.cmpe.library.stomp;

import javax.jms.Connection;
import org.eclipse.jetty.server.Server;

import com.yammer.dropwizard.lifecycle.ServerLifecycleListener;

public class LibListener implements ServerLifecycleListener {
	private static STOMP apolloSTOMP; 
	 
	public LibListener(STOMP apolloSTOMP) {
		this.apolloSTOMP=apolloSTOMP;
	}
	
	@Override
	public void serverStarted(Server server) {
		Connection connect;
		try {
			connect = apolloSTOMP.makeConnection();
	        apolloSTOMP.subscribeTopic(connect);
		} catch (Exception e) {
			e.printStackTrace();
		}				
	}
}