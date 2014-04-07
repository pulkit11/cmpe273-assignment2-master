package edu.sjsu.cmpe.procurement.stomp;


import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.fusesource.stomp.jms.StompJmsDestination;

import com.sun.jersey.api.client.Client;

import edu.sjsu.cmpe.procurement.config.ProcurementServiceConfiguration;
import edu.sjsu.cmpe.procurement.domain.BookRequest;
import edu.sjsu.cmpe.procurement.domain.ShippedBook;

public class STOMP {
	/*
	 * Creating instance of ProcurementServiceConfiguration
	 */
	ProcurementServiceConfiguration configuration = new ProcurementServiceConfiguration();
	private BookRequest bookReuqest = null;
	private String order_book_isbns;
	String queueName = configuration.getStompQueueName();
	String topicName = configuration.getStompTopicName();
	String User = configuration.getApolloUser();
	String Pass = configuration.getApolloPassword();
	String Host = configuration.getApolloHost();
	String url = "http://54.193.56.218:9000/orders";
	int apolloPort = configuration.getApolloPort();
	Client client;	

	public STOMP() {
		
	}
	
	
	public Connection makeConnection() throws Exception {
		StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
        factory.setBrokerURI("tcp://" + Host + ":" + apolloPort);
        Connection connection = factory.createConnection(User, Pass);
        return connection;
	}
	
	
	public BookRequest reveiveQueueMessage(Connection connection) throws Exception {
		connection.start();
		bookReuqest = new BookRequest();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination dest = new StompJmsDestination(queueName);
        MessageConsumer consumer = session.createConsumer(dest);
        
            while(true) {
            	
            	/**Wait for message for 5 sec*/
            	Message msg = consumer.receive(5000);
            	if( msg instanceof  TextMessage ) {
            		String body = ((TextMessage) msg).getText();
            		System.out.println("Received message = " + body);
            		order_book_isbns = body.substring(10);
            		bookReuqest.getOrder_book_isbns().add(Integer.parseInt(order_book_isbns));
            	
            	}
            	if (msg == null) break;
            }
        return bookReuqest; 
	}
	
	
	public void publishTopicMessage(Connection connection, ShippedBook text) throws Exception {
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        for (int isbn_count=0;isbn_count<text.getShipped_books().size();isbn_count++)
        {
        	Destination dest = new StompJmsDestination(topicName + text.getShipped_books().get(isbn_count).getCategory());
        	MessageProducer producer = session.createProducer(dest);
        	producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        	TextMessage msg = session.createTextMessage(text.getShipped_books().get(isbn_count).getIsbn() +  
        			":" + text.getShipped_books().get(isbn_count).getTitle() + 
        			":" + text.getShipped_books().get(isbn_count).getCategory() + 
        			":" + text.getShipped_books().get(isbn_count).getCoverimage());
        	producer.send(msg);
        }
        
	}
	
	
	public void endConnection(Connection connection) throws Exception {
		connection.close();
	}
}
