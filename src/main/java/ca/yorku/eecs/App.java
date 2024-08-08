package ca.yorku.eecs;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

//our own imports
import ca.yorku.eecs.DB.DBFacade;
import ca.yorku.eecs.DB.DBNew;
import ca.yorku.eecs.DB.DBUtil;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.neo4j.driver.v1.*;
import java.nio.charset.StandardCharsets;

import org.json.*;

public class App //starter code
{  	
	static DBNew db;
	
	private static String uriDb = "bolt://localhost:7687";
	private static String username = "neo4j";
	private static String password = "12345678";
	
	
    static int PORT = 8080; //starter code /* !!!MAKE SURE TO TERMINATE THIS APP OTHERWISE THE PORT WILL REMAIN OCCUPIED!!! */
    public static void main(String[] args) throws IOException //starter code
    {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0); //starter code 
        server.start(); //starter code
        System.out.printf("Server started on port %d...\n", PORT); //starter code
        
        DBUtil.connect(uriDb, username, password);
        db = new DBNew();
        
        server.createContext("/api/v1/addActor", new AddActorHttpHandler(db));
    }
    
}


class AddActorHttpHandler implements HttpHandler {

	private DBNew db;
	
	public AddActorHttpHandler(DBNew db) {
		this.db = db;
	}
	
	@Override
	public void handle(HttpExchange exchange) {
		
		try {
			System.out.println("Got an AddActor request!");
			
			if ("PUT".equals(exchange.getRequestMethod())) {
				
	            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
	            System.out.println("Request body:" + requestBody);
	            
	            JSONValidationData validation = validateJSON(requestBody);
	            
	            if(validation.valid) {
	            	
	            	try {
	        			JSONObject jsonObject = new JSONObject(requestBody);
	        			String actorName = jsonObject.getString("name");
	        			String actorId = jsonObject.getString("actorId");
	        			
	        			//TODO: actually add the actor
	        			
	        		} catch (JSONException e) {
	        			e.printStackTrace();
	        		}
	            	
	            	//respond with success message
	                String response = "PUT request successful. Data: " + requestBody;
	                sendResponseAndClose(exchange, 200, response);
	            } else {
	            	
	            	//respond with fail message
	            	String response = "PUT request failed with the following message:\n"
	            			+ validation.message
	            			+ " Data: " + requestBody;
	            	sendResponseAndClose(exchange, 400, response);
	            }
	            
	            
	        } else {
	            sendResponseAndClose(exchange, 405, "Only PUT is supported");
	        }
			
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public JSONValidationData validateJSON(String json) {
		
		StringBuilder message = new StringBuilder();
		boolean valid = true;
		
		JSONObject jsonObject;
        String actorName = null;
        String actorId = null;
        
        try {
			jsonObject = new JSONObject(json);
			actorName = jsonObject.getString("name");
			actorId = jsonObject.getString("actorId");
		} catch (JSONException e) {
			message.append("Validation failed: JSON syntax error: " + e.getMessage());
			return new JSONValidationData(false, message.toString());
		}
        
        if(actorName == null || actorName.isEmpty()) {
        	valid = false;
        	message.append("Validation failed: actor name is empty\n");
        }
        
        if(actorId == null || actorName.isEmpty()) {
        	valid = false;
        	message.append("Validation failed: actorId is empty\n");
        }
        
        return new JSONValidationData(valid, message.toString());
		
	}
	
	private void sendResponseAndClose(HttpExchange exchange, int code, String response) throws IOException {
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
	}
	
}




class JSONValidationData {
	
	public final boolean valid;
	public String message = "default message";
	
	public JSONValidationData(boolean valid) {
		this.valid = valid;
	}
	
	public JSONValidationData(boolean valid, String message) {
		this.valid = valid;
		if(message != null) this.message = message;
	}
}
