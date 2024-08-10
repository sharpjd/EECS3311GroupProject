package ca.yorku.eecs;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
        String jsonInputString = "{ "
        		+ "actorId: \"js1234567\", "
        		+ "name: \"John Smith\" "
        		+ "} ";
        HttpURLConnection connection = sendPutRequest("/api/v1/addActor", jsonInputString);

        int responseCode = connection.getResponseCode();
        
        assertEquals(200, responseCode);

        String response = getResponse(connection);
        assertTrue(response.contains("PUT request successful"));
        
        app.getDb().removeActor("js1234567"); //must remove it otherwise this test is not repeatable
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
        		+ "actorId: \"kb123456\", "
        		+ "name: \"Kevin Bacon\" "
        		+ "} ";        
        HttpURLConnection connection = sendPutRequest("/api/v1/addActor", addPerson1);
        HttpURLConnection connection2 = sendPutRequest("/api/v1/addActor", addPerson2);
        HttpURLConnection connection3 = sendPutRequest("/api/v1/addActor", addPerson3);
        HttpURLConnection connection4 = sendPutRequest("/api/v1/addActor", addKevinBacon);

        
        System.out.println("adding stuff: " + connection.getResponseCode());
        
        
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
         * Bacon Number for cc:
			cc → AM → jj → II → mm → TF → kb
			This path has 6 connections between cc and kb, so the Bacon Number for cc is 6.
         */
        
        HttpURLConnection connection14 = sendPutRequest("/api/v1/computerBaconNumber", "{ actorId: \"cc123\" }");
        int responseCode = connection14.getResponseCode();
        assertEquals(responseCode, 200);
        
        int result = app.getDb().computeBaconNumber("cc123");
        assertEquals(result, 6);
        
        
        /*
         * Bacon Path:
			The Bacon Path from cc is:
			cc → AM → jj → II → mm → TF → kb
			This path lists the actors and movies connecting cc to Kevin Bacon.
         */
        
        //assertEquals(200, responseCode);
        //assertEquals(true, true);

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
}
