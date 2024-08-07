package ca.yorku.eecs;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

//our own imports
import ca.yorku.eecs.DB.DBFacade;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.neo4j.driver.v1.*;
import java.nio.charset.StandardCharsets;

public class App //starter code
{  	
	static DBFacade dbFacade;
	
    static int PORT = 8080; //starter code /* !!!MAKE SURE TO TERMINATE THIS APP OTHERWISE THE PORT WILL REMAIN OCCUPIED!!! */
    public static void main(String[] args) throws IOException //starter code
    {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0); //starter code 
        server.start(); //starter code
        System.out.printf("Server started on port %d...\n", PORT); //starter code
        
        dbFacade = new DBFacade();
        
        server.createContext("/api/v1/addActor", new AddActorHttpHandler(dbFacade));
    }
    
}


class AddActorHttpHandler implements HttpHandler {

	private DBFacade dbFacade;
	
	public AddActorHttpHandler(DBFacade dbFacade) {
		this.dbFacade = dbFacade;
	}
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		
		System.out.println("Got an AddActor request!");
		
		if ("PUT".equals(exchange.getRequestMethod())) {
            // Read the request body
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            
            System.out.println("Request body:" + requestBody);
            
            dbFacade.insertActor(requestBody);
            
            // Respond with a success message
            String response = "PUT request received. Data: " + requestBody;
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else {
            // Method not allowed
            exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
        }
	}
	
}
