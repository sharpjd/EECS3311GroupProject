package ca.yorku.eecs;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
    	try {
			setUpBeforeClass();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
    
    static App app;
    
    //needs to be called manually
    public static void setUpBeforeClass() throws Exception {
        
        app = new App();
        app.main(null); 
    }

    //needs to be called manually (but idk how because we can't change starter code)
    //(doesn't seem necessary to do so either)
    public static void tearDownAfterClass() throws Exception {
        App.closeServer();
    }
    
    public void testaddActorPass() throws Exception {
    	
    	Thread.sleep(500); //prevents a null
    	
    	app.getDb().removeActor("js1234567"); //must remove it otherwise this test is not repeatable
    	
        String jsonInputString = "{ "
        		+ "actorId: \"js1234567\", "
        		+ "name: \"John Smith\" "
        		+ "} ";
        HttpURLConnection connection = sendPutRequest("/api/v1/addActor", jsonInputString);

        int responseCode = connection.getResponseCode();
        
        assertEquals(200, responseCode);

        String response = getResponse(connection);
        assertTrue(response.contains("PUT request successful"));
    } 
    
    public void testaddActorFail() throws Exception {
    	
    	//case 1
        String jsonInputString = "{ " 
        		+ "name: \"John Smith\" " //missing actorId 
        		+ "} ";
        HttpURLConnection connection = sendPutRequest("/api/v1/addActor", jsonInputString);
        
        int responseCode = connection.getResponseCode();
        
        assertEquals(400, responseCode);
        
        //case 2
        jsonInputString = "{ " 
        		+ "actorId: \"12345\" " //missing name 
        		+ "} ";
        connection = sendPutRequest("/api/v1/addActor", jsonInputString);

        responseCode = connection.getResponseCode();
        
        assertEquals(400, responseCode);
        
        //case 3
        jsonInputString = "{ " 
        		+ "" //missing both 
        		+ "} ";
        connection = sendPutRequest("/api/v1/addActor", jsonInputString);

        responseCode = connection.getResponseCode();
        
        assertEquals(400, responseCode);
        
        //case 4
        jsonInputString = "{ " 
        		+ "asdfasdf" //malformed syntax 
        		+ " ";
        connection = sendPutRequest("/api/v1/addActor", jsonInputString);

        responseCode = connection.getResponseCode();
        
        assertEquals(400, responseCode);
    }
    
    
    /* 
     * 
     * 
     * !!!!
     * Some of these tests might need to be run later in the order because 
     * they depend on using functions like addActor, addMovie. etc.
     * !!!! 
     * 
     * 
     */
    
    public void testcomputeBaconNumberPass() throws Exception {
    	
    	initializeBaconPassStuff();
        
        Thread.sleep(500);
        
        HttpURLConnection connection14 = sendGetRequest("/api/v1/computeBaconNumber", "{ actorId: \"cc123\" }");
        
        Thread.sleep(500);
        
        int responseCode = connection14.getResponseCode();
        
        assertEquals(200, responseCode);
        
        JSONObject data = new JSONObject(getResponse(connection14));
        
        assertEquals(3, data.getInt("baconNumber"));
        
    }
    
    public void testcomputeBaconNumberFail() throws Exception {
    	
    	initializeBaconFailStuff();
        
        Thread.sleep(500);
        
        //no path to bacon
        HttpURLConnection connection1 = sendGetRequest("/api/v1/computeBaconNumber", "{ actorId: \"pp1234567\" }");
        
        //actor not found
        HttpURLConnection connection2 = sendGetRequest("/api/v1/computeBaconNumber", "{ actorId: \"asdklhfku\" }");
        
        //bad syntax
        HttpURLConnection connection3 = sendGetRequest("/api/v1/computeBaconNumber", "{  ");
        
        //missing info
        HttpURLConnection connection4 = sendGetRequest("/api/v1/computeBaconNumber", "{  }");
        
        Thread.sleep(500);
        
        assertEquals(404, connection1.getResponseCode());
        assertEquals(404, connection2.getResponseCode());
        assertEquals(400, connection3.getResponseCode());
        assertEquals(400, connection4.getResponseCode());
        
    }
    
    public void testcomputeBaconPathPass() throws Exception {
    	
    	initializeBaconPassStuff();
        
        Thread.sleep(500);
        
        HttpURLConnection connection14 = sendGetRequest("/api/v1/computeBaconPath", "{ actorId: \"cc123\" }");
        
        Thread.sleep(500);
        
        int responseCode = connection14.getResponseCode();
        
        assertEquals(200, responseCode);
        
        JSONObject data = new JSONObject(getResponse(connection14));
        
        //System.out.println("bacon path " + data);
        
        assertEquals("[\"Kevin Bacon\",\"Transformers\",\"Max Maxwell\",\"Inside Out 4\",\"Judy Judith\",\"Among Us\",\"Cooper Copper\"]", data.getString("baconPath"));
    	
    	/*
         * Bacon Path:
			The Bacon Path from cc is:
			cc → AM → jj → II → mm → TF → kb
			This path lists the actors and movies connecting cc to Kevin Bacon.
         */
    }
    


    private HttpURLConnection sendPutRequest(String endpoint, String jsonInputString) throws Exception {
        URL url = new URL("http://localhost:" + app.PORT + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setDoOutput(true);

        try(OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        return connection;
    }
    
    private HttpURLConnection sendGetRequest(String endpoint, String jsonInputString) throws Exception {
        URL url = new URL("http://localhost:" + app.PORT + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        return connection;
    }

    private String getResponse(HttpURLConnection connection) throws Exception {
        try(java.io.BufferedReader in = new java.io.BufferedReader(
            new java.io.InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = in.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }
    
    void initializeBaconPassStuff() throws Exception{
    	//put people into the thing first
        String addPerson1 = "{ "
        		+ "actorId: \"cc123\", "
        		+ "name: \"Cooper Copper\" "
        		+ "} ";
        String addPerson2 = "{ "
        		+ "actorId: \"mm1234\", "
        		+ "name: \"Max Maxwell\" "
        		+ "} ";
        String addPerson3 = "{ "
        		+ "actorId: \"jj12345\", "
        		+ "name: \"Judy Judith\" "
        		+ "} ";        
        String addKevinBacon = "{ "
        		+ "actorId: \"nm0000102\", " //do NOT change this
        		+ "name: \"Kevin Bacon\" "
        		+ "} ";        
        HttpURLConnection connection = sendPutRequest("/api/v1/addActor", addPerson1);
        HttpURLConnection connection2 = sendPutRequest("/api/v1/addActor", addPerson2);
        HttpURLConnection connection3 = sendPutRequest("/api/v1/addActor", addPerson3);
        HttpURLConnection connection4 = sendPutRequest("/api/v1/addActor", addKevinBacon);
        
        /*
         * WE HAVE TO CONSUME THEM OTHERWISE THEY NEVER COMPLETE
         */
        System.out.println(connection.getResponseCode());
        System.out.println(connection2.getResponseCode());
        System.out.println(connection3.getResponseCode());
        System.out.println(connection4.getResponseCode());
        
        String addMovie1 = "{ "
        		+ "movieId: \"am123\", "
        		+ "name: \"Among Us\" "
        		+ "} ";
        String addMovie2 = "{ "
        		+ "movieId: \"ii1234\", "
        		+ "name: \"Inside Out 4\" "
        		+ "} ";
        String addMovie3 = "{ "
        		+ "movieId: \"tf12345\", "
        		+ "name: \"Transformers\" "
        		+ "} ";
        HttpURLConnection connection5 = sendPutRequest("/api/v1/addMovie", addMovie1);
        HttpURLConnection connection6 = sendPutRequest("/api/v1/addMovie", addMovie2);
        HttpURLConnection connection7 = sendPutRequest("/api/v1/addMovie", addMovie3);
        
        /*
         * WE HAVE TO CONSUME THEM OTHERWISE THEY NEVER COMPLETE
         */
        System.out.println(connection5.getResponseCode());
        System.out.println(connection6.getResponseCode());
        System.out.println(connection7.getResponseCode());
        
        String addRelation1 = "{ "
        		+ "actorId: \"cc123\", "
        		+ "movieId: \"am123\" "
        		+ "} ";
        String addRelation2 = "{ "
        		+ "actorId: \"jj12345\", "
        		+ "movieId: \"am123\" "
        		+ "} ";
        String addRelation3 = "{ "
        		+ "actorId: \"jj12345\", "
        		+ "movieId: \"ii1234\" "
        		+ "} ";
        String addRelation4 = "{ "
        		+ "actorId: \"mm1234\", "
        		+ "movieId: \"ii1234\" "
        		+ "} ";
        String addRelation5 = "{ "
        		+ "actorId: \"mm1234\", "
        		+ "movieId: \"tf12345\" "
        		+ "} ";
        String addRelation6 = "{ "
        		+ "actorId: \"kb123456\", "
        		+ "movieId: \"tf12345\" "
        		+ "} ";
        HttpURLConnection connection8 = sendPutRequest("/api/v1/addRelationship", addRelation1);
        HttpURLConnection connection9 = sendPutRequest("/api/v1/addRelationship", addRelation2);
        HttpURLConnection connection10 = sendPutRequest("/api/v1/addRelationship", addRelation3);
        HttpURLConnection connection11 = sendPutRequest("/api/v1/addRelationship", addRelation4);
        HttpURLConnection connection12 = sendPutRequest("/api/v1/addRelationship", addRelation5);
        HttpURLConnection connection13 = sendPutRequest("/api/v1/addRelationship", addRelation6);
        
        /*
         * WE HAVE TO CONSUME THEM OTHERWISE THEY NEVER COMPLETE
         */
        System.out.println(connection8.getResponseCode());
        System.out.println(connection9.getResponseCode());
        System.out.println(connection10.getResponseCode());
        System.out.println(connection11.getResponseCode());
        System.out.println(connection12.getResponseCode());
        System.out.println(connection13.getResponseCode());
        
        /* 
         * Bacon Number for cc:
			cc → AM → jj → II → mm → TF → kb
			This path has 6 connections between cc and kb, so the Bacon Number for cc is 6.
         */
    }
    
    void initializeBaconFailStuff() throws Exception {
    	String addPerson1 = "{ "
        		+ "actorId: \"pp1234567\", "
        		+ "name: \"Petra Peter\" "
        		+ "} ";
        HttpURLConnection connection = sendPutRequest("/api/v1/addActor", addPerson1);
    }
}
