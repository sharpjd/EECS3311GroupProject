package ca.yorku.eecs;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

import org.neo4j.driver.v1.*;

public class App 
{  	
    static int PORT = 8081; //default was 8080, but conflicted with my computer
    public static void main(String[] args) throws IOException
    {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        server.start();
        System.out.printf("Server started on port %d...\n", PORT);
    }
}
