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
    
    public void testaddMoviePass() throws Exception {
    	
    	Thread.sleep(500); //prevents a null
    	
    	app.getDb().removeMovie("tt1234567"); //must remove it otherwise this test is not repeatable
    	
        String jsonInputString = "{ "
        		+ "movieId: \"tt1234567\", "
        		+ "name: \"Sample Movie\", "
        		+ "release: \"2024\" "
        		+ "} ";
        HttpURLConnection connection = sendPutRequest("/api/v1/addMovie", jsonInputString);

        int responseCode = connection.getResponseCode();
        
        assertEquals(200, responseCode);

        String response = getResponse(connection);
        assertTrue(response.contains("PUT request successful"));
    } 
    
    public void testaddMovieFail() throws Exception {
    	
    	//case 1
        String jsonInputString = "{ " 
        		+ "name: \"Sample Movie\", " //missing movieId
        		+ "release: \"2024\" "
        		+ "} ";
        HttpURLConnection connection = sendPutRequest("/api/v1/addMovie", jsonInputString);
        
        int responseCode = connection.getResponseCode();
        
        assertEquals(400, responseCode);
        
        //case 2
        jsonInputString = "{ " 
        		+ "movieId: \"tt1234567\" " //missing name 
        		+ "} ";
        connection = sendPutRequest("/api/v1/addMovie", jsonInputString);

        responseCode = connection.getResponseCode();
        
        assertEquals(400, responseCode);
        
        //case 3
        jsonInputString = "{ " 
        		+ "" //missing all 
        		+ "} ";
        connection = sendPutRequest("/api/v1/addMovie", jsonInputString);

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
    
    public void testAddRatingPass() throws Exception {
    	
    	sendPutRequest("/api/v1/addMovie", " { movieId: \"cp2077\", name:\"Cyberpunk 2077\" }").getResponseCode();
    	
    	Thread.sleep(500);
        String jsonInputString = "{ "
        		+ "movieId: \"cp2077\", " 
        		+ "rating: \"8.5\" "
        		+ "} ";
        HttpURLConnection connection = sendPutRequest("/api/v1/addRating", jsonInputString);

        int responseCode = connection.getResponseCode();
        
        assertEquals(200, responseCode);

        String response = getResponse(connection);
        assertTrue(response.contains("PUT request successful"));
    }
    
    public void testAddRatingFail() throws Exception {
        String jsonInputString = "{ \"rating\": \"8.5\" }";  // movieId is missing
        HttpURLConnection connection = sendPutRequest("/api/v1/addRating", jsonInputString);

        int responseCode = connection.getResponseCode();
        assertEquals(400, responseCode);
    }
    
    public void testAddAwardPass() throws Exception {
    	
    	sendPutRequest("/api/v1/addActor", " { actorId: \"jc6789\", name:\"Jerry Cherry\" }").getResponseCode();
    	
    	Thread.sleep(500);
        String jsonInputString = "{ "
        		+ "actorId: \"jc6789\", "
        		+ "award: \"Best Actor\" "
        		+"} ";
        HttpURLConnection connection = sendPutRequest("/api/v1/addAward", jsonInputString);
        int responseCode = connection.getResponseCode();
        assertEquals(200, responseCode);

        String response = getResponse(connection);
        assertTrue(response.contains("PUT request successful"));
    }
    
    public void testAddAwardFail() throws Exception {
        String jsonInputString = "{ \"actorId\": \"nm0000001\" }";  // award is missing
        HttpURLConnection connection = sendPutRequest("/api/v1/addAward", jsonInputString);

        int responseCode = connection.getResponseCode();
        assertEquals(400, responseCode);
    }
    
    public void testaddRelationshipPass() throws Exception {
        Thread.sleep(500);
        app.getDb().removeActedInRelationship("oo6789", "tn890"); //otherwise test not repeatable
       
        sendPutRequest("/api/v1/addActor", " { actorId: \"oo6789\", name:\"Olivia Oliver\" }").getResponseCode();
        sendPutRequest("/api/v1/addMovie", " { movieId: \"tn890\", name:\"TNMT\" }").getResponseCode();
        
        String jsonInputString = "{ "
        		+ "actorId: \"oo6789\", "
        		+ "movieId: \"tn890\" "
        		+ "} ";
        HttpURLConnection connection = sendPutRequest("/api/v1/addRelationship", jsonInputString);
        int responseCode = connection.getResponseCode();
        
        assertEquals(200, responseCode);

        String response = getResponse(connection);
        assertTrue(response.contains("Relationship added successfully"));
    }

    public void testaddRelationshipFail() throws Exception {
        // Missing actorId
        String jsonInputString = "{ movieId: \"tt1234567\" }";
        HttpURLConnection connection = sendPutRequest("/api/v1/addRelationship", jsonInputString);
        assertEquals(400, connection.getResponseCode());

        // Missing movieId
        jsonInputString = "{ actorId: \"nm0000001\" }";
        connection = sendPutRequest("/api/v1/addRelationship", jsonInputString);
        assertEquals(400, connection.getResponseCode());

        // Actor or movie not found
        jsonInputString = "{ actorId: \"nm9999999\", movieId: \"tt1234567\" }";
        connection = sendPutRequest("/api/v1/addRelationship", jsonInputString);
        assertEquals(404, connection.getResponseCode());

        // Malformed JSON
        jsonInputString = "{";
        connection = sendPutRequest("/api/v1/addRelationship", jsonInputString);
        assertEquals(400, connection.getResponseCode());
    }

    // Initialization methods for setting up the test environment
    private void initializeGetActorPassStuff() throws Exception {
        // Add an actor for testing
        String jsonInputString = "{ actorId: \"jc1234567890\", name: \"John Cena\" }";
        HttpURLConnection connection = sendPutRequest("/api/v1/addActor", jsonInputString);
        System.out.println(connection.getResponseCode());
        
        String movieJsonInputString = "{ movieId: \"tt1234567\", name: \"Sample Movie\", release: \"2024\" }";
        connection = sendPutRequest("/api/v1/addMovie", movieJsonInputString);
        System.out.println(connection.getResponseCode());
        
        String relationshipJsonInputString = "{ actorId: \"jc1234567890\", movieId: \"tt1234567\" }";
        connection = sendPutRequest("/api/v1/addRelationship", relationshipJsonInputString);
        System.out.println(connection.getResponseCode());
    }

    private void initializeGetMoviePassStuff() throws Exception {
        // Add a movie and actor for testing
        String movieJsonInputString = "{ movieId: \"tt1234567\", name: \"Sample Movie\", release: \"2024\" }";
        HttpURLConnection connection = sendPutRequest("/api/v1/addMovie", movieJsonInputString);
        System.out.println(connection.getResponseCode());

        String jsonInputString = "{ actorId: \"nm0000001\", name: \"John Doe\" }";
        connection = sendPutRequest("/api/v1/addActor", jsonInputString);
        System.out.println(connection.getResponseCode());

        String relationshipJsonInputString = "{ actorId: \"nm0000001\", movieId: \"tt1234567\" }";
        connection = sendPutRequest("/api/v1/addRelationship", relationshipJsonInputString);
        System.out.println(connection.getResponseCode());
    }


    public void testgetActorPass() throws Exception {
    	
    	initializeGetActorPassStuff();
    	
    	String id = "jc1234567890";
    	HttpURLConnection connection = sendGetRequest("/api/v1/getActor?actorId=" + id);

    	int responseCode = connection.getResponseCode();
    	assertEquals(200, responseCode);

    	JSONObject data = new JSONObject(getResponse(connection));
    	assertEquals("jc1234567890", data.getString("actorId"));
    	assertEquals("John Cena", data.getString("name"));
    	assertEquals("["+ "\"tt1234567\""+"]", data.getString("movies"));
    }


    public void testgetActorFail() throws Exception {
    	String id = "nonexistant";
        // actor not found
        HttpURLConnection connection = sendGetRequest("/api/v1/getActor?actorId=" + id);
        assertEquals(404, connection.getResponseCode());

        // missing actorId
        connection = sendGetRequest("/api/v1/getActor?");
        assertEquals(400, connection.getResponseCode());
    }


    public void testgetMoviePass() throws Exception {
    	
    	initializeGetMoviePassStuff();
    	
        Thread.sleep(500);
        String id = "tt1234567";
        HttpURLConnection connection = sendGetRequest("/api/v1/getMovie?movieId=" + id);

        int responseCode = connection.getResponseCode();
        assertEquals(200, responseCode);

        JSONObject data = new JSONObject(getResponse(connection));
        assertEquals("tt1234567", data.getString("movieId"));
        assertEquals("Sample Movie", data.getString("name"));
        assert(data.getString("actors").contains("nm0000001"));

    	/*
        Thread.sleep(500);
        String id = "m1";
        HttpURLConnection connection = sendGetRequest("/api/v1/getMovie?movieId=" + id);

        int responseCode = connection.getResponseCode();
        assertEquals(200, responseCode);

        JSONObject data = new JSONObject(getResponse(connection));
        assertEquals("m1", data.getString("movieId"));
        assertEquals("Jungle Book", data.getString("name"));
        assertEquals("["+"\"nm1\""+"]", data.getString("actors"));
        */
    }

    public void testgetMovieFail() throws Exception {
    	String id = "nonexistant";
        // Movie not found
        HttpURLConnection connection = sendGetRequest("/api/v1/getMovie?movieId=" + id);
        assertEquals(404, connection.getResponseCode());

        // Missing movieId
        connection = sendGetRequest("/api/v1/getMovie?");
        assertEquals(400, connection.getResponseCode());
    }
    public void testGetMovieByReleasePass() throws Exception {
    	
    	HttpURLConnection setup = sendPutRequest("/api/v1/addMovie", "{ movieId:\"zz1234567\", name:\"Zootopia\", release:\"1999\" }");
    	setup.getResponseCode();    	
    	
       	String release = "1999";
       	HttpURLConnection connection = sendGetRequest("/api/v1/getMovieByRelease?release=" + release);
       	int responseCode = connection.getResponseCode();
       	assertEquals(200, responseCode);

       	String response = getResponse(connection);
       	assertTrue(response.contains("movieId"));
       	assertTrue(response.contains("name"));
       }
       
       public void testGetMovieByReleaseFail() throws Exception {
       	String release = "1994";
           HttpURLConnection connection = sendGetRequest("/api/v1/getMovieByRelease?release=" + release); // Future year
           int responseCode = connection.getResponseCode();
           assertEquals(404, responseCode);
       }
       
       public void testGetMovieByRatingPass() throws Exception {  	
       	
    	   sendPutRequest("/api/v1/addRating", "{ movieId:\"cc1337\", rating:\"6.9\" }").getResponseCode();
    	   
    	   HttpURLConnection setup = sendPutRequest("/api/v1/addMovie", "{ movieId:\"cc1337\", name:\"Chippi Chippi Chappa Chappa\", rating:\"6.9\" }");
    	   setup.getResponseCode();
    	   
       	   String rating = "6.9";
           HttpURLConnection connection = sendGetRequest("/api/v1/getMovieByRating?rating=" + rating);
           int responseCode = connection.getResponseCode();
           assertEquals(200, responseCode);

           String response = getResponse(connection);
           assertTrue(response.contains("movieId"));
           assertTrue(response.contains("name"));
       }
       
       public void testGetMovieByRatingFail() throws Exception {
       	
       	String jsonInputString = "10.0";
           HttpURLConnection connection = sendGetRequest("/api/v1/getMovieByRating?rating=" + jsonInputString);
           Thread.sleep(500);
           int responseCode = connection.getResponseCode();
           assertEquals(404, responseCode);
       }
       
       public void testGetActorByAwardPass() throws Exception {
    	   
    	   String addPerson = "{ "
           		+ "actorId: \"rc696969\", "
           		+ "name: \"Ricardo Milos\""
           		+ "} ";
    	   sendPutRequest("/api/v1/addActor", addPerson).getResponseCode();
    	   
    	   String addAward = "{ "
              		+ "actorId: \"rc696969\", "
              		+ "award: \"Best Looking\""
              		+ "} ";
    	   
    	   sendPutRequest("/api/v1/addAward", addAward).getResponseCode();
    	   
    	   
       	String award = "Best%20Looking"; //%20 means space
           HttpURLConnection connection = sendGetRequest("/api/v1/getActorsByAward?award=" + award);
           int responseCode = connection.getResponseCode();
           assertEquals(200, responseCode);

           String response = getResponse(connection);
           assertTrue(response.contains("actorId"));
           assertTrue(response.contains("name"));
       }
       
       public void testGetActorByAwardFail() throws Exception {
       	String jsonInputString = "Unknown Award";
           HttpURLConnection connection = sendGetRequest("/api/v1/getActorsByAward?award=" + jsonInputString);

           int responseCode = connection.getResponseCode();
           assertEquals(404, responseCode);
       }
//
//
//    public void testhasRelationshipPass() throws Exception {
//    initializeHasRelationshipPassStuff();
//
//    String jsonInputString = "{ actorId: \"nm0000001\", movieId: \"tt1234567\" }";
//    HttpURLConnection connection = sendGetRequest("/api/v1/hasRelationship", jsonInputString);
//
//    
//    int responseCode = connection.getResponseCode();
//    assertEquals(200, responseCode);
//
//    JSONObject data = new JSONObject(getResponse(connection));
//    assertEquals("nm0000001", data.getString("actorId"));
//    assertEquals("tt1234567", data.getString("movieId"));
//    assertTrue(data.getBoolean("hasRelationship"));
//   }
//
//   public void testhasRelationshipFail() throws Exception {
//    // Missing actorId
//    String jsonInputString = "{ movieId: \"tt1234567\" }";
//    HttpURLConnection connection = sendGetRequest("/api/v1/hasRelationship", jsonInputString);
//    assertEquals(400, connection.getResponseCode());
//
//    // Missing movieId
//    jsonInputString = "{ actorId: \"nm0000001\" }";
//    connection = sendGetRequest("/api/v1/hasRelationship", jsonInputString);
//    assertEquals(400, connection.getResponseCode());
//
//    // Actor not found
//    jsonInputString = "{ actorId: \"nm9999999\", movieId: \"tt1234567\" }";
//    connection = sendGetRequest("/api/v1/hasRelationship", jsonInputString);
//    assertEquals(404, connection.getResponseCode());
//
//    // Movie not found
//    jsonInputString = "{ actorId: \"nm0000001\", movieId: \"tt9999999\" }";
//    connection = sendGetRequest("/api/v1/hasRelationship", jsonInputString);
//    assertEquals(404, connection.getResponseCode());
//
//    // Malformed JSON
//    jsonInputString = "{";
//    connection = sendGetRequest("/api/v1/hasRelationship", jsonInputString);
//    assertEquals(400, connection.getResponseCode());
//}
   
//   /* 
//    * 
//    * 
//    * !!!!
//    * Some of these tests might need to be run later in the order because 
//    * they depend on using functions like addActor, addMovie. etc.
//    * !!!! 
//    * 
//    * 
//    */
//	
//
//
//private void initializeHasRelationshipPassStuff() throws Exception {
//    // Add an actor for testing
//    String jsonInputString = "{ actorId: \"nm0000001\", name: \"John Doe\" }";
//    HttpURLConnection connection = sendPutRequest("/api/v1/addActor", jsonInputString);
//    System.out.println(connection.getResponseCode());
//
//    // Add a movie for testing
//    String movieJsonInputString = "{ movieId: \"tt1234567\", name: \"Sample Movie\", release: \"2024\" }";
//    connection = sendPutRequest("/api/v1/addMovie", movieJsonInputString);
//    System.out.println(connection.getResponseCode());
//
//    // Create a relationship between the actor and the movie
//    String relationshipJsonInputString = "{ actorId: \"nm0000001\", movieId: \"tt1234567\" }";
//    connection = sendPutRequest("/api/v1/addRelationship", relationshipJsonInputString);
//    System.out.println(connection.getResponseCode());
//}
//
//   
//    public void testcomputeBaconNumberPass() throws Exception {
//    	
//    	initializeBaconPassStuff();
//        
//        Thread.sleep(500);
//        
//        HttpURLConnection connection14 = sendGetRequest("/api/v1/computeBaconNumber", "{ actorId: \"cc123\" }");
//        
//        Thread.sleep(500);
//        
//        int responseCode = connection14.getResponseCode();
//        
//        assertEquals(200, responseCode);
//        
//        JSONObject data = new JSONObject(getResponse(connection14));
//        
//        assertEquals(3, data.getInt("baconNumber"));
//        
//    }
//    
//    public void testcomputeBaconNumberFail() throws Exception {
//    	
//    	initializeBaconFailStuff();
//        
//        Thread.sleep(500);
//        
//        //no path to bacon
//        HttpURLConnection connection1 = sendGetRequest("/api/v1/computeBaconNumber", "{ actorId: \"pp1234567\" }");
//        
//        //actor not found
//        HttpURLConnection connection2 = sendGetRequest("/api/v1/computeBaconNumber", "{ actorId: \"asdklhfku\" }");
//        
//        //bad syntax
//        HttpURLConnection connection3 = sendGetRequest("/api/v1/computeBaconNumber", "{  ");
//        
//        //missing info
//        HttpURLConnection connection4 = sendGetRequest("/api/v1/computeBaconNumber", "{  }");
//        
//        Thread.sleep(500);
//        
//        assertEquals(404, connection1.getResponseCode());
//        assertEquals(404, connection2.getResponseCode());
//        assertEquals(400, connection3.getResponseCode());
//        assertEquals(400, connection4.getResponseCode());
//        
//    }
//    
//    public void testcomputeBaconPathPass() throws Exception {
//    	
//    	initializeBaconPassStuff();
//        
//        Thread.sleep(500);
//        
//        HttpURLConnection connection14 = sendGetRequest("/api/v1/computeBaconPath", "{ actorId: \"cc123\" }");
//        
//        Thread.sleep(500);
//        
//        int responseCode = connection14.getResponseCode();
//        
//        assertEquals(200, responseCode);
//        
//        JSONObject data = new JSONObject(getResponse(connection14));
//        
//        //System.out.println("bacon path " + data);
//        
//        assertEquals("[\"Kevin Bacon\",\"Transformers\",\"Max Maxwell\",\"Inside Out 4\",\"Judy Judith\",\"Among Us\",\"Cooper Copper\"]", data.getString("baconPath"));
//    	
//    	/*
//         * Bacon Path:
//			The Bacon Path from cc is:
//			cc → AM → jj → II → mm → TF → kb
//			This path lists the actors and movies connecting cc to Kevin Bacon.
//         */
//    }
//    
//    public void testcomputeBaconPathFail() throws Exception {
//    	
//    	initializeBaconFailStuff();
//        
//        Thread.sleep(500);
//        
//        //no path to bacon
//        HttpURLConnection connection1 = sendGetRequest("/api/v1/computeBaconNumber", "{ actorId: \"pp1234567\" }");
//        
//        //actor not found
//        HttpURLConnection connection2 = sendGetRequest("/api/v1/computeBaconNumber", "{ actorId: \"asdklhfku\" }");
//        
//        //bad syntax
//        HttpURLConnection connection3 = sendGetRequest("/api/v1/computeBaconNumber", "{  ");
//        
//        //missing info
//        HttpURLConnection connection4 = sendGetRequest("/api/v1/computeBaconNumber", "{  }");
//        
//        Thread.sleep(500);
//        
//        assertEquals(404, connection1.getResponseCode());
//        assertEquals(404, connection2.getResponseCode());
//        assertEquals(400, connection3.getResponseCode());
//        assertEquals(400, connection4.getResponseCode());
//        
//    }
//    


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
    
    private HttpURLConnection sendGetRequest(String endpoint) throws Exception {
        URL url = new URL("http://localhost:" + app.PORT + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setDoOutput(false);

        return connection;
    }
//    private HttpURLConnection sendGetRequest(String endpoint, String queryParams) throws Exception {
//        URL url = new URL("http://localhost:" + app.PORT + endpoint + "?" + queryParams);
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("GET");
//        connection.setRequestProperty("Content-Type", "application/json; utf-8");
//        connection.setDoOutput(false); // GET requests typically don't use output streams
//
//        return connection;
//    }
//    
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
