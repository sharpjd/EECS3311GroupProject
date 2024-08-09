package ca.yorku.eecs.DB;

import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.Record;

import java.util.*;
import java.util.Map;

public class DBNew {
	
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
			session.run("MATCH (m:movie) SET m.rating = $rating", Map.of("rating", String.format("%.2f", defaultRating)));
		}
	}
	
	public void updateMovieRating(String movieTitle, String newRating) {
		try(Session session = DBUtil.getSession()){
			session.run("MATCH (m:movie {title: $title}) SET m.rating = $rating", Map.of("title", movieTitle, "rating", String.format("rating", String.format("%.1f", newRating))));
		}
	}
	
	public List<String> getMoviesWithRating(String minRating) {
		List<String> movies = new ArrayList<>();
		try(Session session = DBUtil.getSession()){
			StatementResult result = session.run("MATCH (m:movie) WHERE m.rating >= $rating RETURN m.title AS title", Map.of("rating", String.format("%.1f", minRating)));
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
			StatementResult result = session.run("MATCH (m:movie) WHERE m.release = $release RETURN m.title AS title", Map.of("release", year));
			while(result.hasNext()) {
				Record record = result.next();
				movies.add(record.get("title").asString());
			}
					
		}
		return movies;
	}
	public void addAwards(String actor, List<String> awards) {
		try(Session session = DBUtil.getSession()){
			session.run("MATCH (a:actor {name: $name}) SET a.awards = $awards",
	                Map.of("name", actor, "awards", awards));	
		}
	}
	
	public List<String> getActorsByAward(String award){
		List<String> actors = new ArrayList<>();
		try(Session session = DBUtil.getSession()){
			StatementResult result = session.run("MATCH (a:actor) WHERE $award IN a.awards RETURN a.name AS name", Map.of("award", award));
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
	
	public boolean actorExists(String actorId) {
		try(Session session = DBUtil.getSession()){
			
			Transaction transaction = session.beginTransaction();
			
			Statement query = new Statement("MATCH(a: actor) WHERE a.actorId = $actorId RETURN a;",
					Map.of("actorId", actorId));
			
			StatementResult result = transaction.run(query);
						
			/*
			 * for whatever reason, this causes the function to hang
			 * System.out.println("Statement result: " + result.single().get(0).asString());
			 * 
			 * and the below statement is also require in order for the function to not hang
			 */
			
			System.out.println("Statement result: " + result.consume()); //^^maybe something to do with lazy initialization?
			
			transaction.success();
			
			if(result.list().size() != 0) return true;
			else return false;
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
	
	public boolean movieExists(String movieId) {
		try(Session session = DBUtil.getSession()){
			
			Transaction transaction = session.beginTransaction();
			
			Statement query = new Statement("MATCH(m: movie) WHERE m.movieId = movieId RETURN m;",
					Map.of("movieId", movieId));
			
			StatementResult result = transaction.run(query);
						
			/*
			 * for whatever reason, this causes the function to hang
			 * System.out.println("Statement result: " + result.single().get(0).asString());
			 * 
			 * and the below statement is also require in order for the function to not hang
			 */
			
			System.out.println("Statement result: " + result.consume()); //^^maybe something to do with lazy initialization?
			
			transaction.success();
			
			if(result.list().size() != 0) return true;
			else return false;
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

    //Neo4j doesn't support constraints for duplicate relationships so this is necessary
    public boolean actedInRelationshipExists(String actorId, String movieId) {
    	try (Session session = DBUtil.getSession()) {
            Transaction tx = session.beginTransaction();
            
            Statement query = new Statement( //WARNING: spaces can make or break the syntax, add one after each line
            		"MATCH (a:actor), (m:movie) "
            		+ "WHERE a.actorId = $actorId AND m.movieId = $movieId AND (a)-[:ACTED_IN]->(m) "
            		+ "RETURN *; ", 
                   Map.of("actorId", actorId, "movieId", movieId)
                   );
            
            StatementResult result = tx.run(query);
            
            System.out.println("Statement result: " + result.consume());
            
            tx.success();
            
            if(result.list().size() != 0) return true;
			else return false;

    	}
    }
	
    public String getActorById(String actorId) {
	    try (Session session = DBUtil.getSession()) {    
                StatementResult result = session.run("MATCH (a:actor {actorId: $actorId}) RETURN a.name AS name, a.actorId AS actorId", Map.of("actorId", actorId));

		if (result.hasNext()) {
			Record record = result.next();
                	JSONObject jsonObject = new JSONObject();
                	try {
						jsonObject.put("name", record.get("name").asString());
						jsonObject.put("actorId", record.get("actorId").asString());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                	return jsonObject.toString();
		}

		//case if actor is not found
		else {
			return null;
		}
        }
    }

    public String getMovieById(String movieId) {
	    try (Session session = DBUtil.getSession()) {    
                StatementResult result = session.run("MATCH (m:movie {movieId: $movieId}) RETURN m.name AS name, m.movieId AS movieId", Map.of("movieId", movieId));

		if (result.hasNext()) {
			Record record = result.next();
                	JSONObject jsonObject = new JSONObject();
                	try {
						jsonObject.put("name", record.get("name").asString());
						jsonObject.put("movieId", record.get("movieId").asString());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                	return jsonObject.toString();
		}

		//case if movie is not found
		else {
			return null;
		}
        }
    }

    public boolean hasActedInRelationship(String actorId, String movieId) {
    try (Session session = DBUtil.getSession()) {
        StatementResult result = session.run(
            "MATCH (a:actor {actorId: $actorId})-[:ACTED_IN]->(m:movie {movieId: $movieId}) " +
            "RETURN count(*) > 0 AS hasRelationship",
            Map.of("actorId", actorId, "movieId", movieId)
        );
        if (result.hasNext()) {
            return result.next().get("hasRelationship").asBoolean();
        } else {
            return false; // there was no relationship found between the actor and the movie
        }
    }
    }

   public int computeBaconNumber(String actorId) {
        try (Session session = DBUtil.getSession()) {
            StatementResult result = session.run(
                    "MATCH (bacon:actor {actorId: 'nm0000102'}), (actor:actor {actorId: $actorId}), " +
                    "p = shortestPath((bacon)-[:ACTED_IN*]-(actor)) " +
                    "RETURN length(p)/2 AS baconNumber",
                    Map.of("actorId", actorId)
            );
            if (result.hasNext()) {
                return result.next().get("baconNumber").asInt();
            } else {
                return -1; // there was no path found or the actor is not connected to Kevin Bacon
            }
        }
    }

   public List<String> computeBaconPath(String actorId) {
        try (Session session = DBUtil.getSession()) {
            StatementResult result = session.run(
                    "MATCH (bacon:actor {actorId: 'nm0000102'}), (actor:actor {actorId: $actorId}), " +
                    "p = shortestPath((bacon)-[:ACTED_IN*]-(actor)) " +
                    "RETURN [n IN nodes(p) | coalesce(n.name, n.title)] AS path",
                    Map.of("actorId", actorId)
            );
            if (result.hasNext()) {
                return result.next().get("path").asList(Value::asString);
            } else {
                return null; // there was no path found or the actor is not connected to Kevin Bacon
            }
        }
    }
}
