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
import org.neo4j.driver.v1.exceptions.ClientException;

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
        
        try {
        	db.createConstraints();
        } catch (ClientException e) {
        	//do nothing if it already exists
        }
        
        server.createContext("/api/v1/addActor", new AddActorHttpHandler(db));
        server.createContext("/api/v1/addMovie", new AddMovieHttpHandler(db));
    }
    
}


class AddActorHttpHandler implements HttpHandler {

	private DBNew db;
	private ResponseSender responseSender = new ResponseSender();
	
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
	        			
	        			db.addActor(actorName, actorId);
	        			
	        		} catch (JSONException e) {
	        			e.printStackTrace();
	        		}
	            	
	            	//respond with success message
	                String response = "PUT request successful. Data: " + requestBody;
	                responseSender.sendResponseAndClose(exchange, 200, response);
	            } else {
	            	
	            	//respond with fail message
	            	String response = "PUT request failed with the following message:\n"
	            			+ validation.message
	            			+ " Data: " + requestBody;
	            	responseSender.sendResponseAndClose(exchange, 400, response);
	            }
	            
	            
	        } else {
	        	responseSender.sendResponseAndClose(exchange, 405, "Only PUT is supported");
	        }
			
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Handle AddActor finished");
		
	}
	
	public JSONValidationData validateJSON(String json) {
		
		StringBuilder message = new StringBuilder();
		boolean valid = true;
		
		JSONObject jsonObject;
        String actorName = null;
        String actorId = null;
        
        try {
			jsonObject = new JSONObject(json);
			actorName = jsonObject.optString("name");
			actorId = jsonObject.optString("actorId");
			
			if(actorName == null || actorName.isEmpty()) {
	        	valid = false;
	        	message.append("Validation failed: actor name is empty or not found\n");
	        }
	        
	        if(actorId == null || actorId.isEmpty()) {
	        	valid = false;
	        	message.append("Validation failed: actorId is empty or not found\n");
	        }
			
		} catch (JSONException e) {
			message.append("Validation failed: JSON syntax error: " + e.getMessage());
			return new JSONValidationData(false, message.toString());
		}
        
        return new JSONValidationData(valid, message.toString());
		
	}
	
}

class AddMovieHttpHandler implements HttpHandler {

	private DBNew db;
	private ResponseSender responseSender = new ResponseSender();
	
	public AddMovieHttpHandler(DBNew db) {
		this.db = db;
	}
	
	@Override
	public void handle(HttpExchange exchange) {
		
		try {
			System.out.println("Got an AddMovie request!");
			
			if ("PUT".equals(exchange.getRequestMethod())) {
				
	            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
	            System.out.println("Request body:" + requestBody);
	            
	            JSONValidationData validation = validateJSON(requestBody);
	            
	            if(validation.valid) {
	            	
	            	try {
	        			JSONObject jsonObject = new JSONObject(requestBody);
	        			String movieName = jsonObject.getString("name");
	        			String movieId = jsonObject.getString("movieId");
	        			
	        			String movieRelease = jsonObject.optString("release"); //can be empty
	        			
	        			db.addMovie(movieId, movieName, movieRelease);
	        			
	        		} catch (JSONException e) {
	        			e.printStackTrace();
	        		}
	            	
	            	//respond with success message
	                String response = "PUT request successful. Data: " + requestBody;
	                responseSender.sendResponseAndClose(exchange, 200, response);
	            } else {
	            	
	            	//respond with fail message
	            	String response = "PUT request failed with the following message:\n"
	            			+ validation.message
	            			+ " Data: " + requestBody;
	            	responseSender.sendResponseAndClose(exchange, 400, response);
	            }
	            
	            
	        } else {
	        	responseSender.sendResponseAndClose(exchange, 405, "Only PUT is supported");
	        }
			
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Handle AddMovie finished");
		
	}
	
	public JSONValidationData validateJSON(String json) {
		
		StringBuilder message = new StringBuilder();
		boolean valid = true;
		
		JSONObject jsonObject;
        String movieName = null;
        String movieId = null;
        //skip checking for release date because that's optional
        
        try {
			jsonObject = new JSONObject(json);
			movieName = jsonObject.optString("name");
			movieId = jsonObject.optString("movieId");
			
			if(movieName == null || movieName.isEmpty()) {
	        	valid = false;
	        	message.append("Validation failed: movie name is empty or not found\n");
	        }
	        
	        if(movieId == null || movieId.isEmpty()) {
	        	valid = false;
	        	message.append("Validation failed: movieId is empty or not found\n");
	        }
			
		} catch (JSONException e) {
			message.append("Validation failed: JSON syntax error: " + e.getMessage());
			return new JSONValidationData(false, message.toString());
		}
        
        return new JSONValidationData(valid, message.toString());
		
	}
	
	
	
}


class ResponseSender {
	public void sendResponseAndClose(HttpExchange exchange, int code, String response) throws IOException {
        exchange.sendResponseHeaders(code, response.getBytes().length);
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
