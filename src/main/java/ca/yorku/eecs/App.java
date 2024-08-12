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
import org.neo4j.driver.v1.exceptions.DatabaseException;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.*;

public class App //starter code
{  	
	private static DBNew db;
	
	public static DBNew getDb(){
		return db;
	}
	
	private static String uriDb = "bolt://localhost:7687";
	private static String username = "neo4j";
	private static String password = "12345678";
	
	private static HttpServer _server;
	
	
    static int PORT = 8080; //starter code /* !!!MAKE SURE TO TERMINATE THIS APP OTHERWISE THE PORT WILL REMAIN OCCUPIED!!! */
    public static void main(String[] args) throws IOException //starter code
    {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0); //starter code 
        server.start(); //starter code
        System.out.printf("Server started on port %d...\n", PORT); //starter code
        
        _server = server;
        
        DBUtil.connect(uriDb, username, password);
        db = new DBNew();
        
        try {
        	db.createConstraints();
        } catch (ClientException e) {
        	System.out.println("Constraints already exist.");
        }
        
        server.createContext("/api/v1/addActor", new AddActorHttpHandler(db));
        server.createContext("/api/v1/addMovie", new AddMovieHttpHandler(db));
        server.createContext("/api/v1/addRelationship", new AddRelationshipHttpHandler(db));
		server.createContext("/api/v1/getActor", new GetActorHttpHandler(db));
		server.createContext("/api/v1/getMovie", new GetMovieHttpHandler(db));
		server.createContext("/api/v1/hasRelationship", new HasRelationshipHttpHandler(db));
		server.createContext("/api/v1/computeBaconNumber", new ComputeBaconNumberHttpHandler(db));
		server.createContext("/api/v1/computeBaconPath", new ComputeBaconPathHttpHandler(db));
    }
    
    public static void closeServer() {
    	_server.stop(500);
    }
    
}

/**
 * Data class for info about JSON validation. Contains a boolean indicating whether it is valid and a String for additional info about the validation.
 */
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

/**
 * Class containing a function for the API endpoint classes to avoid code duplication
 */
class ResponseSender {
	/**
	 * Sends the given code and response message and then closes the HTTP exchange.
	 * @param exchange
	 * @param code
	 * @param response
	 * @throws IOException
	 */
	public void sendResponseAndClose(HttpExchange exchange, int code, String response) throws IOException {
        exchange.sendResponseHeaders(code, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
	}
}

/**
 * API endpoint for addActor.
 */
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
	        			
	        			if(db.getActorById(actorId) != null) {
	        				String response = "PUT request failed; Actor with actorId already exists. Data: " + requestBody;
	    	                responseSender.sendResponseAndClose(exchange, 400, response);
	        			} else {
	        				db.addActor(actorName, actorId);
	        				
	    	            	//respond with success message
	    	                String response = "PUT request successful. Data: " + requestBody;
	    	                responseSender.sendResponseAndClose(exchange, 200, response);
	        			}
	        			
	        		} catch (JSONException e) {
	        			e.printStackTrace();
	        		}
	            	
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
	
	/**
	 * Validates a JSON string for this API endpoint.
	 * 
	 * Criteria:
	 * 1. The syntax is correct
	 * 2. name is not null or empty
	 * 3. actorId is not null or empty
	 *  
	 * @param json the JSON string to validate
	 * @return a JSONValidationData object with property "valid" set to true if it's valid according to the aforementioned criteria, false otherwise
	 */
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

/**
 * API endpoint for addMovie.
 */
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
	        			
	        			if(db.getMovieById(movieId) != null) {
	        				String response = "PUT request failed; Movie with movieId already exists. Data: " + requestBody;
	    	                responseSender.sendResponseAndClose(exchange, 400, response);
	        			} else {
	    	            	//respond with success message
	    	                String response = "PUT request successful. Data: " + requestBody;
	    	                responseSender.sendResponseAndClose(exchange, 200, response);
	        				db.addMovie(movieId, movieName, movieRelease);
	        			}
	        			
	        		} catch (JSONException e) {
	        			e.printStackTrace();
	        		}
	            	
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
	
	/**
	 * Validates a JSON string for this API endpoint.
	 * 
	 * Criteria:
	 * 1. The syntax is correct
	 * 2. name is not null or empty
	 * 3. movieId is not null or empty
	 *  
	 * @param json the JSON string to validate
	 * @return a JSONValidationData object with property "valid" set to true if it's valid according to the aforementioned criteria, false otherwise
	 */
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
/**
 * API endpoint for addRelationship.
 */
class AddRelationshipHttpHandler implements HttpHandler {

    private DBNew db;
    
    ResponseSender responseSender = new ResponseSender();

    public AddRelationshipHttpHandler(DBNew db) {
        this.db = db;
    }

    @Override
    public void handle(HttpExchange exchange) {
        //try {
        	try {
	            if ("PUT".equals(exchange.getRequestMethod())) {
	                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
	                JSONObject jsonObject = new JSONObject(requestBody);
	
	                if (!jsonObject.has("actorId") || !jsonObject.has("movieId")) {
	                    responseSender.sendResponseAndClose(exchange, 400, "Missing required fields: actorId, movieId");
	                    return;
	                }
	
	                String actorId = jsonObject.getString("actorId");
	                String movieId = jsonObject.getString("movieId");
	
	                // make sure that the actor and movie exist
	                String actor = db.getActorById(actorId);
	                String movie = db.getMovieById(movieId);
	
	                if (actor == null) {
	                    responseSender.sendResponseAndClose(exchange, 404, "Actor not found");
	                    return;
	                }
	
	                if (movie == null) {
	                    responseSender.sendResponseAndClose(exchange, 404, "Movie not found");
	                    return;
	                }
	                
	                if(db.hasActedInRelationship(actorId, movieId)) {
	                	responseSender.sendResponseAndClose(exchange, 400, "PUT failed: relationship already exists");
	                } else {
	                	db.addActedInRelationship(actorId, movieId);
	                	responseSender.sendResponseAndClose(exchange, 200, "Relationship added successfully");
	                }
	
	            } else {
	                responseSender.sendResponseAndClose(exchange, 405, "Only PUT is supported");
	            }
	
	        } catch (JSONException e) {
	            try {
					responseSender.sendResponseAndClose(exchange, 400, "Invalid JSON format: " + e.getMessage());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	        } catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	/*
        } catch (Exception e) {
            try {
				responseSender.sendResponseAndClose(exchange, 500, "Internal Server Error: " + e.getMessage());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        }
        */
    }

}



/* //code with duplicate functionality from vader
class AddMovieHttpHandler implements HttpHandler {

    private DBNew db;

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

                JSONObject jsonObject = new JSONObject(requestBody);
                String movieId = jsonObject.getString("movieId");
                String name = jsonObject.getString("name");
                String release = jsonObject.getString("release");

                db.addMovie(movieId, name, release);

                // Respond with success message
                String response = "PUT request successful. Data: " + requestBody;
                responseSender.sendResponseAndClose(exchange, 200, response);

            } else {
                responseSender.sendResponseAndClose(exchange, 405, "Only PUT is supported");
            }

        } catch (JSONException e) {
            responseSender.sendResponseAndClose(exchange, 400, "Invalid JSON format");
        } 

	catch (IOException e) {
            responseSender.sendResponseAndClose(exchange, 500, "Internal Server Error");
        }

        System.out.println("Handle AddMovie finished");

    }

    private void responseSender.sendResponseAndClose(HttpExchange exchange, int code, String response) throws IOException {
        exchange.sendResponseHeaders(code, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
*/

/* //nonworking code from sharpjd
class AddRelationShipHttpHandler implements HttpHandler {

	private DBNew db;
	private ResponseSender responseSender = new ResponseSender();
	
	public AddRelationShipHttpHandler(DBNew db) {
		this.db = db;
	}
	
	@Override
	public void handle(HttpExchange exchange) {
		
		try {
			System.out.println("Got an AddRelationship request!");
			
			if ("PUT".equals(exchange.getRequestMethod())) {
				
	            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
	            System.out.println("Request body:" + requestBody);
	            
	            JSONValidationData validation = validateJSON(requestBody);
	            
	            if(validation.valid) {
	            	
	            	try {
	        			JSONObject jsonObject = new JSONObject(requestBody);
	        			String actorId = jsonObject.getString("actorId");
	        			String movieId = jsonObject.getString("movieId");
	        			
	        			String movieRelease = jsonObject.optString("release"); //can be empty
	        			
	        			boolean actorExists = db.actorExists(actorId);
	        			System.out.println(actorExists);
	        			boolean movieExists = db.movieExists(movieId);
	        			System.out.println(movieExists);
	        			
	        			boolean relationAlreadyExists = db.actedInRelationshipExists(actorId, movieId);
	        			
	        			System.out.println(relationAlreadyExists);
	        			
	        			StringBuilder response = new StringBuilder();
	        			
	        			if(!actorExists || !movieExists || relationAlreadyExists) {
	        				response.append("PUT request failed with the following message:\n");
	        				
	        				if(!actorExists) {
		        				response.append("the actorId specified does not exist.\n");
		        			}
		        			
		        			if(!movieExists) {
		        				response.append("the movieId specified does not exist.\n");
		        			}
		        			
		        			if(relationAlreadyExists) {
		        				response.append("the relationship already exists.\n");
		        			}
		        			
		        			response.append("Data: ");
		        			response.append(requestBody);
		        			response.append("\n");
		        			
		        			if(!actorExists || !movieExists)
		        				responseSender.sendResponseAndClose(exchange, 404, response.toString());
		        			else if(relationAlreadyExists)
		        				responseSender.sendResponseAndClose(exchange, 400, response.toString());
		        			else 
		        				throw new RuntimeException("why are we here");
		        			
	        			} else {
        					db.addActedInRelationship(actorId, movieId);
	        			}
	        			
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
        String actorId = null;
        String movieId = null;
        //skip checking for release date because that's optional
        
        try {
			jsonObject = new JSONObject(json);
			actorId = jsonObject.optString("actorId");
			movieId = jsonObject.optString("movieId");
			
			if(actorId == null || actorId.isEmpty()) {
	        	valid = false;
	        	message.append("Validation failed: actorId is empty or not found\n");
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
*/

/**
 * API endpoint for getActor.
 */
class GetActorHttpHandler implements HttpHandler {
    private DBNew db;
    private ResponseSender responseSender = new ResponseSender();

    public GetActorHttpHandler(DBNew db) {
        this.db = db;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            try {
                JSONObject jsonObject = new JSONObject(requestBody);
                String actorId = jsonObject.optString("actorId");

                if (actorId == null || actorId.isEmpty()) {
                    responseSender.sendResponseAndClose(exchange, 400, "Missing required field: actorId");
                    return;
                }

                String actorJson = db.getActorById(actorId);

                if (actorJson != null) {
                    responseSender.sendResponseAndClose(exchange, 200, actorJson);
                } 
		
		else {
                    responseSender.sendResponseAndClose(exchange, 404, "Actor not found");
                }
            } 
	    
	    catch (JSONException e) {
                responseSender.sendResponseAndClose(exchange, 400, "Invalid JSON format: " + e.getMessage());
            } 
	    
	    catch (Exception e) {
                responseSender.sendResponseAndClose(exchange, 500, "Internal Server Error: " + e.getMessage());
            }
        } 
	
	else {
            responseSender.sendResponseAndClose(exchange, 405, "Only GET is supported");
        }
    }
}

/**
 * API endpoint for getMovie.
 */
class GetMovieHttpHandler implements HttpHandler {
    private DBNew db;
    private ResponseSender responseSender = new ResponseSender();

    public GetMovieHttpHandler(DBNew db) {
        this.db = db;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            try {
                JSONObject jsonObject = new JSONObject(requestBody);
                String movieId = jsonObject.optString("movieId");

                if (movieId == null || movieId.isEmpty()) {
                    responseSender.sendResponseAndClose(exchange, 400, "Missing required field: movieId");
                    return;
                }

                String movieJson = db.getMovieById(movieId);

                if (movieJson != null) {
                    responseSender.sendResponseAndClose(exchange, 200, movieJson);
                } 
		
		else {
                    responseSender.sendResponseAndClose(exchange, 404, "Movie not found");
                }
            } 
	    
	    catch (JSONException e) {
                responseSender.sendResponseAndClose(exchange, 400, "Invalid JSON format: " + e.getMessage());
            } 
	    
	    catch (Exception e) {
                responseSender.sendResponseAndClose(exchange, 500, "Internal Server Error: " + e.getMessage());
            }
        } 
	
	else {
            responseSender.sendResponseAndClose(exchange, 405, "Only GET is supported");
        }
    }
}

/**
 * API endpoint for hasRelationship.
 */
class HasRelationshipHttpHandler implements HttpHandler {
    private DBNew db;
    private ResponseSender responseSender = new ResponseSender();

    public HasRelationshipHttpHandler(DBNew db) {
        this.db = db;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            try {
                JSONObject jsonObject = new JSONObject(requestBody);
                String actorId = jsonObject.optString("actorId");
                String movieId = jsonObject.optString("movieId");

                if (actorId == null || actorId.isEmpty() || movieId == null || movieId.isEmpty()) {
                    responseSender.sendResponseAndClose(exchange, 400, "Missing required fields: actorId, movieId");
                    return;
                }

                String actor = db.getActorById(actorId);
                String movie = db.getMovieById(movieId);

                if (actor == null) {
                    responseSender.sendResponseAndClose(exchange, 404, "Actor not found");
                    return;
                }

                if (movie == null) {
                    responseSender.sendResponseAndClose(exchange, 404, "Movie not found");
                    return;
                }

                boolean hasRelationship = db.hasActedInRelationship(actorId, movieId);
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("actorId", actorId);
                jsonResponse.put("movieId", movieId);
                jsonResponse.put("hasRelationship", hasRelationship);

                responseSender.sendResponseAndClose(exchange, 200, jsonResponse.toString());
            } 
	    
	    catch (JSONException e) {
                responseSender.sendResponseAndClose(exchange, 400, "Invalid JSON format: " + e.getMessage());
            } 
	    
	    catch (Exception e) {
                responseSender.sendResponseAndClose(exchange, 500, "Internal Server Error: " + e.getMessage());
            }
        } 
	
	else {
            responseSender.sendResponseAndClose(exchange, 405, "Only GET is supported");
        }
    }
}
    
    /**
	 * Validates a JSON string for this API endpoint.
	 * 
	 * Criteria:
	 * 1. The syntax is correct
	 * 3. actorId is not null or empty
	 *  
	 * @param json the JSON string to validate
	 * @return a JSONValidationData object with property "valid" set to true if it's valid according to the aforementioned criteria, false otherwise
	 */
	public JSONValidationData validateJSON(String json) {
		
		StringBuilder message = new StringBuilder();
		boolean valid = true;
		
		JSONObject jsonObject;
        String actorId = null;
        
        try {
			jsonObject = new JSONObject(json);
			actorId = jsonObject.optString("actorId");
	        
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

/**
 * API endpoint for ComputeBaconNumber
 */
class ComputeBaconNumberHttpHandler implements HttpHandler {

    private DBNew db;
    
    ResponseSender responseSender = new ResponseSender();

    public ComputeBaconNumberHttpHandler(DBNew db) {
        this.db = db;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
    	
    	System.out.println("Got a ComputeBaconNumber request");
    	
        //if ("GET".equals(exchange.getRequestMethod())) { //seems to prevent us from having a body
        	
        	String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        	
            JSONValidationData validation = validateJSON(requestBody);

            if(validation.valid) {
            	try {
            		
            		JSONObject data = new JSONObject(requestBody);
            		String actorId = data.getString("actorId");
            		
                    int baconNumber = db.computeBaconNumber(actorId);

                    if (baconNumber == -1) {
                        responseSender.sendResponseAndClose(exchange, 404, "No path to Kevin Bacon found");
                        return;
                    }

                    JSONObject jsonResponse = new JSONObject();
                    jsonResponse.put("actorId", actorId);
                    jsonResponse.put("baconNumber", baconNumber);

                    responseSender.sendResponseAndClose(exchange, 200, jsonResponse.toString());
                } catch (Exception e) {
                    responseSender.sendResponseAndClose(exchange, 500, "Internal Server Error: " + e.getMessage());
                }
            } else {
            	responseSender.sendResponseAndClose(exchange, 400, "GET request failed with message: " + validation.message);
            }
            
            
        //} else {
        	//System.out.println("wrong requst");
            //responseSender.sendResponseAndClose(exchange, 405, "Only GET is supported");
        //}
    }
    
    /**
	 * Validates a JSON string for this API endpoint.
	 * 
	 * Criteria:
	 * 1. The syntax is correct
	 * 3. actorId is not null or empty
	 *  
	 * @param json the JSON string to validate
	 * @return a JSONValidationData object with property "valid" set to true if it's valid according to the aforementioned criteria, false otherwise
	 */
	public JSONValidationData validateJSON(String json) {
		
		StringBuilder message = new StringBuilder();
		boolean valid = true;
		
		JSONObject jsonObject;
        String actorId = null;
        
        try {
			jsonObject = new JSONObject(json);
			actorId = jsonObject.optString("actorId");
	        
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

/**
 * API endpoint for computeBaconPath
 */
class ComputeBaconPathHttpHandler implements HttpHandler {

    private DBNew db;
    
    ResponseSender responseSender = new ResponseSender();

    public ComputeBaconPathHttpHandler(DBNew db) {
        this.db = db;
    }
    
    

    @Override
    public void handle(HttpExchange exchange) throws IOException {
    	
    	System.out.println("Got a ComputeBaconNumber request");

        	
    	String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    	
        JSONValidationData validation = validateJSON(requestBody);

        if(validation.valid) {
        	try {
        		
        		JSONObject data = new JSONObject(requestBody);
        		String actorId = data.getString("actorId");
        		
        		List<String> baconPath = db.computeBaconPath(actorId);

                if (baconPath == null || baconPath.isEmpty()) {
                    responseSender.sendResponseAndClose(exchange, 404, "No path to Kevin Bacon found");
                    return;
                }

                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("actorId", actorId);
                jsonResponse.put("baconPath", baconPath);

                responseSender.sendResponseAndClose(exchange, 200, jsonResponse.toString());

            } catch (Exception e) {
                responseSender.sendResponseAndClose(exchange, 500, "Internal Server Error: " + e.getMessage());
            }
        } else {
        	responseSender.sendResponseAndClose(exchange, 400, "GET request failed with message: " + validation.message);
        }

    }
    
    /**
	 * Validates a JSON string for this API endpoint.
	 * 
	 * Criteria:
	 * 1. The syntax is correct
	 * 3. actorId is not null or empty
	 *  
	 * @param json the JSON string to validate
	 * @return a JSONValidationData object with property "valid" set to true if it's valid according to the aforementioned criteria, false otherwise
	 */
	public JSONValidationData validateJSON(String json) {
		
		StringBuilder message = new StringBuilder();
		boolean valid = true;
		
		JSONObject jsonObject;
        String actorId = null;
        
        try {
			jsonObject = new JSONObject(json);
			actorId = jsonObject.optString("actorId");
	        
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

