package ca.yorku.eecs.DB;

import org.neo4j.driver.v1.*;

import org.json.*;

/*
 * Handle lower level operations like node creation, deletion, etc. 
 */
class DBOperator {
	
	private Driver driver;
	private String uriDb;
	
	DBOperator() {
		uriDb = "bolt://localhost:7687";
		Config config = Config.builder().withoutEncryption().build();
		driver = GraphDatabase.driver(uriDb, AuthTokens.basic("neo4j", "12345678"), config);
	}
	
	void insertActor(String json) {
		
		String cypherStatement = null;
		
        JSONObject jsonObject;
        String actorName = null;
        String actorId = null;
		try {
			jsonObject = new JSONObject(json);
			actorName = jsonObject.getString("name");
			actorId = jsonObject.getString("actorId");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		cypherStatement = String.format("CREATE(a:actor {name:\"%s\", actorId:\"%s\"})", actorName, actorId);
		
		System.out.println("Cypher statement: " + cypherStatement);
		
		try (Session session = driver.session()){
			
			Transaction transaction = session.beginTransaction();
		    {
		       Statement query = new Statement(cypherStatement);

		       StatementResult result = transaction.run( query );
		       
		       //TODO it seems to be getting hung up after this
		       
		       transaction.success(); // mark success, actually commit will happen in transaction.close()
		       String greeting = result.single().get( 0 ).asString();
		       System.out.println( greeting );
		       
		       transaction.close();
		    }
		    
		}
		
		System.out.println("insertActor method ended");
		
	}
	
	

}
