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
        server.createContext("/api/v1/addRelationship", new AddRelationShipHttpHandler(db));
		server.createContext("/api/v1/getActor", new GetActorHttpHandler(db));
		server.createContext("/api/v1/getMovie", new GetMovieHttpHandler(db));
		server.createContext("/api/v1/hasRelationship", new HasRelationshipHttpHandler(db));
		server.createContext("/api/v1/computeBaconNumber", new ComputeBaconNumberHttpHandler(db));
		server.createContext("/api/v1/computeBaconPath", new ComputeBaconPathHttpHandler(db));
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


class ResponseSender {
	public void sendResponseAndClose(HttpExchange exchange, int code, String response) throws IOException {
        exchange.sendResponseHeaders(code, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
	}
}

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
                sendResponseAndClose(exchange, 200, response);

            } else {
                sendResponseAndClose(exchange, 405, "Only PUT is supported");
            }

        } catch (JSONException e) {
            sendResponseAndClose(exchange, 400, "Invalid JSON format");
        } 
	
	catch (IOException e) {
            sendResponseAndClose(exchange, 500, "Internal Server Error");
        }

        System.out.println("Handle AddMovie finished");

    }

    private void sendResponseAndClose(HttpExchange exchange, int code, String response) throws IOException {
        exchange.sendResponseHeaders(code, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}

class AddRelationshipHttpHandler implements HttpHandler {

    private DBNew db;

    public AddRelationshipHttpHandler(DBNew db) {
        this.db = db;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if ("PUT".equals(exchange.getRequestMethod())) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                JSONObject jsonObject = new JSONObject(requestBody);

                if (!jsonObject.has("actorId") || !jsonObject.has("movieId")) {
                    sendResponseAndClose(exchange, 400, "Missing required fields: actorId, movieId");
                    return;
                }

                String actorId = jsonObject.getString("actorId");
                String movieId = jsonObject.getString("movieId");

                // make sure that the actor and movie exist
                String actor = db.getActorById(actorId);
                String movie = db.getMovieById(movieId);

                if (actor == null) {
                    sendResponseAndClose(exchange, 404, "Actor not found");
                    return;
                }

                if (movie == null) {
                    sendResponseAndClose(exchange, 404, "Movie not found");
                    return;
                }

                db.addActedInRelationship(actorId, movieId);

                sendResponseAndClose(exchange, 200, "Relationship added successfully");

            } else {
                sendResponseAndClose(exchange, 405, "Only PUT is supported");
            }

        } catch (JSONException e) {
            sendResponseAndClose(exchange, 400, "Invalid JSON format: " + e.getMessage());
        } catch (Exception e) {
            sendResponseAndClose(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void sendResponseAndClose(HttpExchange exchange, int code, String response) throws IOException {
        exchange.sendResponseHeaders(code, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}

class GetActorHttpHandler implements HttpHandler {

    private DBNew db;

    public GetActorHttpHandler(DBNew db) {
        this.db = db;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = queryToMap(query);
            String actorId = params.get("actorId");

            if (actorId == null || actorId.isEmpty()) {
                sendResponseAndClose(exchange, 400, "Missing required field: actorId");
                return;
            }

            try {
                String actorJson = db.getActorById(actorId);

                if (actorJson != null) {
                    sendResponseAndClose(exchange, 200, actorJson);
                } else {
                    sendResponseAndClose(exchange, 404, "Actor not found");
                }
            } catch (Exception e) {
                sendResponseAndClose(exchange, 500, "Internal Server Error: " + e.getMessage());
            }
        } else {
            sendResponseAndClose(exchange, 405, "Only GET is supported");
        }
    }

    private Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            result.put(entry[0], entry[1]);
        }
        return result;
    }

    private void sendResponseAndClose(HttpExchange exchange, int code, String response) throws IOException {
        exchange.sendResponseHeaders(code, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}

class GetMovieHttpHandler implements HttpHandler {

    private DBNew db;

    public GetMovieHttpHandler(DBNew db) {
        this.db = db;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = queryToMap(query);
            String movieId = params.get("movieId");

            if (movieId == null || movieId.isEmpty()) {
                sendResponseAndClose(exchange, 400, "Missing required field: movieId");
                return;
            }

            try {
                String movieJson = db.getMovieById(movieId);

                if (movieJson != null) {
                    sendResponseAndClose(exchange, 200, movieJson);
                } else {
                    sendResponseAndClose(exchange, 404, "Movie not found");
                }
            } catch (Exception e) {
                sendResponseAndClose(exchange, 500, "Internal Server Error: " + e.getMessage());
            }
        } else {
            sendResponseAndClose(exchange, 405, "Only GET is supported");
        }
    }

    private Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            result.put(entry[0], entry[1]);
        }
        return result;
    }

    private void sendResponseAndClose(HttpExchange exchange, int code, String response) throws IOException {
        exchange.sendResponseHeaders(code, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}

class HasRelationshipHttpHandler implements HttpHandler {

    private DBNew db;

    public HasRelationshipHttpHandler(DBNew db) {
        this.db = db;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = queryToMap(query);
            String actorId = params.get("actorId");
            String movieId = params.get("movieId");

            if (actorId == null || actorId.isEmpty() || movieId == null || movieId.isEmpty()) {
                sendResponseAndClose(exchange, 400, "Missing required fields: actorId, movieId");
                return;
            }

            try {
                String actor = db.getActorById(actorId);
                String movie = db.getMovieById(movieId);

                if (actor == null) {
                    sendResponseAndClose(exchange, 404, "Actor not found");
                    return;
                }

                if (movie == null) {
                    sendResponseAndClose(exchange, 404, "Movie not found");
                    return;
                }

                boolean hasRelationship = db.hasActedInRelationship(actorId, movieId);
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("actorId", actorId);
                jsonResponse.put("movieId", movieId);
                jsonResponse.put("hasRelationship", hasRelationship);

                sendResponseAndClose(exchange, 200, jsonResponse.toString());
            } catch (Exception e) {
                sendResponseAndClose(exchange, 500, "Internal Server Error: " + e.getMessage());
            }
        } else {
            sendResponseAndClose(exchange, 405, "Only GET is supported");
        }
    }

    private Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            result.put(entry[0], entry[1]);
        }
        return result;
    }

    private void sendResponseAndClose(HttpExchange exchange, int code, String response) throws IOException {
        exchange.sendResponseHeaders(code, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}

class ComputeBaconNumberHttpHandler implements HttpHandler {

    private DBNew db;

    public ComputeBaconNumberHttpHandler(DBNew db) {
        this.db = db;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = queryToMap(query);
            String actorId = params.get("actorId");

            if (actorId == null || actorId.isEmpty()) {
                sendResponseAndClose(exchange, 400, "Missing required field: actorId");
                return;
            }

            try {
                int baconNumber = db.computeBaconNumber(actorId);

                if (baconNumber == -1) {
                    sendResponseAndClose(exchange, 404, "No path to Kevin Bacon found");
                    return;
                }

                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("actorId", actorId);
                jsonResponse.put("baconNumber", baconNumber);

                sendResponseAndClose(exchange, 200, jsonResponse.toString());
            } catch (Exception e) {
                sendResponseAndClose(exchange, 500, "Internal Server Error: " + e.getMessage());
            }
        } else {
            sendResponseAndClose(exchange, 405, "Only GET is supported");
        }
    }

    private Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            result.put(entry[0], entry[1]);
        }
        return result;
    }

    private void sendResponseAndClose(HttpExchange exchange, int code, String response) throws IOException {
        exchange.sendResponseHeaders(code, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}

class ComputeBaconPathHttpHandler implements HttpHandler {

    private DBNew db;

    public ComputeBaconPathHttpHandler(DBNew db) {
        this.db = db;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = queryToMap(query);
            String actorId = params.get("actorId");

            if (actorId == null || actorId.isEmpty()) {
                sendResponseAndClose(exchange, 400, "Missing required field: actorId");
                return;
            }

            try {
                List<String> baconPath = db.computeBaconPath(actorId);

                if (baconPath == null || baconPath.isEmpty()) {
                    sendResponseAndClose(exchange, 404, "No path to Kevin Bacon found");
                    return;
                }

                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("actorId", actorId);
                jsonResponse.put("baconPath", baconPath);

                sendResponseAndClose(exchange, 200, jsonResponse.toString());
            } catch (Exception e) {
                sendResponseAndClose(exchange, 500, "Internal Server Error: " + e.getMessage());
            }
        } else {
            sendResponseAndClose(exchange, 405, "Only GET is supported");
        }
    }

    private Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            result.put(entry[0], entry[1]);
        }
        return result;
    }

    private void sendResponseAndClose(HttpExchange exchange, int code, String response) throws IOException {
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
