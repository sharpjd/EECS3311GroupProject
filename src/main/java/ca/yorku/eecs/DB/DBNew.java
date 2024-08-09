package ca.yorku.eecs.DB;

import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.json.*;
import java.util.*;
import java.util.Map;

public class DBNew {
	private JSONObject response;
	
	public void createConstraints() {
		try(Session session = DBUtil.getSession()){
			
			Transaction transaction = session.beginTransaction();
			
			Statement query = new Statement("CREATE CONSTRAINT ON (m:movie) ASSERT m.movieId IS UNIQUE");
			Statement query2 = new Statement("CREATE CONSTRAINT ON (a:actor) ASSERT a.actorId IS UNIQUE");
			
			StatementResult result = transaction.run(query);
			StatementResult result2 = transaction.run(query2);
						
			System.out.println("Statement result: " + result.consume());
			System.out.println("Statement result: " + result2.consume());
			
			transaction.success();
			
			System.out.println("Finished inputting restraints");
		}
	}
	
	public void addRatingToAllMovies(String defaultRating) {
		try(Session session = DBUtil.getSession()){
			session.run("MATCH (m:movie) "
					+ "SET m.rating = $rating", 
					Map.of("rating", String.format("%.2f", defaultRating)));
		}
	}
	
	public void updateMovieRating(String movieTitle, String newRating) {
		try(Session session = DBUtil.getSession()){
			session.run("MATCH (m:movie {title: $title}) "
					+ "SET m.rating = $rating",
					Map.of("title", movieTitle, "rating", String.format("rating", String.format("%.1f", newRating))));
		}
	}
	
	public List<String> getMoviesWithRating(String minRating) {
		List<String> movies = new ArrayList<>();
		try(Session session = DBUtil.getSession()){
			StatementResult result = session.run("MATCH (m:movie) "
					+ "WHERE m.rating >= $rating "
					+ "RETURN m.title AS title",
					Map.of("rating", String.format("%.1f", minRating)));
			while(result.hasNext()) {
				Record record = result.next();
				movies.add(record.get("title").asString());
			}
		}
		return movies;
	}
	
	public List<String> getMoviesByReleaseYear(String year){
		List<String> movies = new ArrayList<>();
		try(Session session = DBUtil.getSession()){
			StatementResult result = session.run("MATCH (m:movie) "
					+ "WHERE m.release = $release "
					+ "RETURN m.title AS title", 
					Map.of("release", year)
					);
			while(result.hasNext()) {
				Record record = result.next();
				movies.add(record.get("title").asString());
			}
					
		}
		return movies;
	}
	public void addAwards(String actor, List<String> awards) {
		try(Session session = DBUtil.getSession()){
			session.run("MATCH (a:actor {name: $name}) "
					+ "SET a.awards = $awards",
	                Map.of("name", actor, "awards", awards));	
		}
	}
	
	public List<String> getActorsByAward(String award){
		List<String> actors = new ArrayList<>();
		try(Session session = DBUtil.getSession()){
			StatementResult result = session.run("MATCH (a:actor) "
					+ "WHERE $award IN a.awards "
					+ "RETURN a.name AS name", 
					Map.of("award", award)
					);
			while(result.hasNext()) {
				Record record = result.next();
				actors.add(record.get("name").asString());
			}
		}
		return actors;
	}
	
	public void addActor(String actorName, String actorId) {
		try(Session session = DBUtil.getSession()){
			
			Transaction transaction = session.beginTransaction();
			
			Statement query = new Statement("CREATE(a:actor {name:$name, actorId:$actorId})",
					Map.of("name", actorName, "actorId", actorId));
			
			StatementResult result = transaction.run(query);
						
			/*
			 * for whatever reason, this causes the function to hang
			 * System.out.println("Statement result: " + result.single().get(0).asString());
			 * 
			 * and the below statement is also require in order for the function to not hang
			 */
			System.out.println("Statement result: " + result.consume()); //^^maybe something to do with lazy initialization?
			
			transaction.success();
		}
	}
	
	public void addMovie(String movieId, String name, String release) {
        try (Session session = DBUtil.getSession()) {
            Transaction tx = session.beginTransaction();
            Statement query = new Statement("CREATE (m:movie {movieId: $movieId, name: $name, release: $release})", 
                    Map.of("movieId", movieId, "name", name, "release", release));
            StatementResult result = tx.run(query);

            //result.list();

            System.out.println("Statement result: " + result.consume());

            tx.success();
        }
    }
    
	public void addActedInRelationship(String actorId, String movieId) {
	      try (Session session = DBUtil.getSession()) {
	              Transaction tx = session.beginTransaction();
	              
	              Statement query = new Statement( //WARNING: spaces can make or break the syntax, add one after each line
	                      "MATCH (a:actor), (m:movie) "
	                      + "WHERE a.actorId = $actorId AND m.movieId = $movieId "
	                      + "CREATE (a)-[r:ACTED_IN]->(m) "
	                      + "RETURN type(r); ", 
	                     Map.of("actorId", actorId, "movieId", movieId)
	                     );
	              
	              StatementResult result = tx.run(query);
	              
	              System.out.println("Statement result: " + result.consume());
	              
	              tx.success();

	      }
	}
    public Map<String, Object> getActor(String actorId) {
        Map<String, Object> actorDetails = new HashMap<>();
        try (Session session = DBUtil.getSession()) {
            StatementResult result = session.run(
                "MATCH (a:actor {actorId: $actorId}) "
                + "RETURN a",
                Map.of("actorId", actorId)
            );
            if (result.hasNext()) {
                Record record = result.next();
                actorDetails = record.get("a").asMap();
            }
        }
        return actorDetails;
    }

    public Map<String, Object> getMovie(String movieId) {
        Map<String, Object> movieDetails = new HashMap<>();
        try (Session session = DBUtil.getSession()) {
            StatementResult result = session.run(
                "MATCH (m:movie {movieId: $movieId}) "
                + "RETURN m",
                Map.of("movieId", movieId)
            );
            if (result.hasNext()) {
                Record record = result.next();
                movieDetails = record.get("m").asMap();
            }
        }
        return movieDetails;
    }
    public int getBaconNumber(String actorId) {

        try(Session session = DBUtil.getSession()){

            try(Transaction tx = session.beginTransaction()){

                // First we see whether our input query is Kevin Bacon or not, in which case we return 0
                if(actorId.equals("nm0000102")) {

                    session.close();
                    response = new JSONObject().put("baconNumber", "0" );
                    return 200;
                }
                //Now we have to check whether an actor with the input actor even exists or not
                if(!actorPresent(actorId)) {
                    session.close();
                    return 400;
                }

                /* This is the part where we initiate the search process and calculating Bacon's Number
                Taken from Neo4j Docs - On Shortest Paths in Neo4j DB
                * Link: https://neo4j.com/docs/cypher-manual/current/clauses/match/#query-shortest-path */
                StatementResult checkPath = tx.run(("MATCH (a:Actor {id: '%s'})," +
                        " (b:Actor {id: 'nm0000102'}), p = shortestPath((a)-[*..15]-(b)) RETURN p").formatted(actorId));

                // We have to see if a path exists or not using an if condition
                if(!checkPath.hasNext()) {
                    session.close();
                    return 404;
                }

                /* We get the `p` value from our CYPHER query, which is double of what we need
                   The `p` value is basically the nodes traversed in finding the shortest path.
                    We get the number of nodes traversed through the .get().size() method and then
                    divide by 2 to get the correct number of connections between input and Kevin Bacon
                    SOURCE: Neo4j Manual, extracted from code version of queries*/

                String baconNumber = String.valueOf((checkPath.next().get("p").size())/2);
                response = new JSONObject().put("baconNumber", baconNumber);
                return 200;
            }

            // We also have to set up the internal server error status codes
            catch (Exception e){
                return 500;
            }
        }
        catch (Exception e){
            return 500;
        }
    }
    
    public boolean actorPresent(String id) {
        try(Session session = DBUtil.getSession()){
            Transaction tx = session.beginTransaction();
            String query = "MATCH (a: Actor) WHERE a.id = '%s' RETURN a".formatted(id);
            StatementResult result = tx.run(query);
            Boolean isPresent = result.hasNext();
            tx.commitAsync();
            return isPresent;
        }
    }

    /*
     * This method returns:
     * True - if a movie with the given id is already present in the database
     * False - if a movie with the given id is not present in the database
     */
    public boolean moviePresent(String id) {
        try(Session session = DBUtil.getSession()){
            Transaction tx = session.beginTransaction();
            String query = "MATCH (m: Movie) WHERE m.id = '%s' RETURN m".formatted(id);
            StatementResult result = tx.run(query);
            Boolean isPresent = result.hasNext();
            tx.commitAsync();
            return isPresent;
        }
    }
	
	
	
}
