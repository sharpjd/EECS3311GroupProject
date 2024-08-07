package ca.yorku.eecs.DB;

import org.neo4j.driver.v1.*;

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
	
	void insertActor(String actor) {
		
		System.out.println("insert an actor!");
		/*
		try (Session session = driver.session()){
			
			Transaction transaction = session.beginTransaction();
		    {
		       Statement query = new Statement( "TODO: a Cypher statement/query", Values.parameters("message", actor) );

		       StatementResult result = transaction.run( query );
		       transaction.success(); // mark success, actually commit will happen in transaction.close()
		       String greeting = result.single().get( 0 ).asString();
		       System.out.println( greeting );
		    }
		    
		}
		*/
	}
	
	

}
