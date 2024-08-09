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
    
    public void testAddActorPass() throws Exception {
        String jsonInputString = "{ "
        		+ "actorId: \"1234567\", "
        		+ "name: \"John Smith\" "
        		+ "} ";
        HttpURLConnection connection = sendPutRequest("/api/v1/addActor", jsonInputString);

        int responseCode = connection.getResponseCode();
        
        assertEquals(200, responseCode);

        String response = getResponse(connection);
        assertTrue(response.contains("PUT request successful"));
    }
    
    public void testAddActorFail() throws Exception {
    	
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
