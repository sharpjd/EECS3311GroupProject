package ca.yorku.eecs.DB;

import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.exceptions.ClientException;

import java.util.*;
import java.util.Map;

public class DBNew {
	
	public void createConstraints() throws ClientException {
		try(Session session = DBUtil.getSession()){
			
			Transaction transaction = session.beginTransaction();
			
			Statement query = new Statement("CREATE CONSTRAINT ON (m:Movie) ASSERT m.movieId IS UNIQUE");
			Statement query2 = new Statement("CREATE CONSTRAINT ON (a:Actor) ASSERT a.actorId IS UNIQUE");
			
			StatementResult result = transaction.run(query);
			StatementResult result2 = transaction.run(query2);
						
			System.out.println("Statement result: " + result.consume());
			System.out.println("Statement result: " + result2.consume());
			
			transaction.success();
			
			System.out.println("Finished inputting restraints");
		}
	}
	
	/**
	 * Sets the rating for every Movie node in the DB.
	 * @param defaultRating
	 */
	public void addRatingToAllMovies(String defaultRating) {
		try(Session session = DBUtil.getSession()){
			session.run("MATCH (m:Movie) SET m.rating = $rating", Map.of("rating", String.format("%.2f", defaultRating)));
		}
	}
	
	/**
	 * Sets the rating of a Movie with the specified title.
	 * @param movieTitle
	 * @param newRating
	 */
	public void updateMovieRating(String movieTitle, String newRating) {
		try(Session session = DBUtil.getSession()){
			session.run("MATCH (m:Movie {title: $title}) SET m.rating = $rating", Map.of("title", movieTitle, "rating", String.format("rating", String.format("%.1f", newRating))));
		}
	}
	
	/**
	 * Get a list of the titles of all movies that are above a certain rating.
	 * @param minRating
	 * @return
	 */
	public List<String> getMoviesWithRating(String minRating) {
		List<String> movies = new ArrayList<>();
		try(Session session = DBUtil.getSession()){
			StatementResult result = session.run("MATCH (m:Movie) WHERE m.rating >= $rating RETURN m.title AS title", Map.of("rating", String.format("%.1f", minRating)));
			while(result.hasNext()) {
				Record record = result.next();
				movies.add(record.get("title").asString());
			}
		}
		return movies;
	}
	
	/**
	 * Get a list of all the movie titles whose release dates match the specified year.
	 * @param year
	 * @return
	 */
	public List<String> getMoviesByReleaseYear(String year){
		List<String> movies = new ArrayList<>();
		try(Session session = DBUtil.getSession()){
			StatementResult result = session.run("MATCH (m:Movie) WHERE m.release = $release RETURN m.title AS title", Map.of("release", year));
			while(result.hasNext()) {
				Record record = result.next();
				movies.add(record.get("title").asString());
			}
					
		}
		return movies;
	}
	
	/**
	 * Add a list of awards (String) to the actor specified by name.
	 * @param actor the name of the actor
	 * @param awards a list containing the names of awards
	 */
	public void addAwards(String actor, List<String> awards) {
		try(Session session = DBUtil.getSession()){
			session.run("MATCH (a:Actor {name: $name}) SET a.awards = $awards",
	                Map.of("name", actor, "awards", awards));	
		}
	}
	
	/**
	 * Get a list of actors which have the specified award.
	 * @param award The name of the award
	 * @return
	 */
	public List<String> getActorsByAward(String award){
		List<String> actors = new ArrayList<>();
		try(Session session = DBUtil.getSession()){
			StatementResult result = session.run("MATCH (a:Actor) WHERE $award IN a.awards RETURN a.name AS name", Map.of("award", award));
			while(result.hasNext()) {
				Record record = result.next();
				actors.add(record.get("name").asString());
			}
		}
		return actors;
	}
	
	/**
	 * Add an Actor node to the database.
	 * @param actorName
	 * @param actorId
	 */
	public void addActor(String actorName, String actorId) {
		try(Session session = DBUtil.getSession()){
			
			Transaction transaction = session.beginTransaction();
			
			Statement query = new Statement("CREATE(a:Actor {name:$name, actorId:$actorId})",
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
	
	/**
	 * Whether the specified Actor node exists, by their ID
	 * @param actorId
	 * @return
	 */
	public boolean actorExists(String actorId) {
		try(Session session = DBUtil.getSession()){
			
			Transaction transaction = session.beginTransaction();
			
			Statement query = new Statement("MATCH(a: Actor) WHERE a.actorId = $actorId RETURN a;",
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
	
	/**
	 * Add a Movie node to the database.
	 * @param movieId
	 * @param name
	 * @param release
	 */
	public void addMovie(String movieId, String name, String release) {
        try (Session session = DBUtil.getSession()) {
            Transaction tx = session.beginTransaction();
            Statement query = new Statement("CREATE (m:Movie {movieId: $movieId, name: $name, release: $release})", 
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
			
			Statement query = new Statement("MATCH(m: Movie) WHERE m.movieId = movieId RETURN m;",
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
    
	/**
	 * Created an :ACTED_IN relationship from the specified Actor (by ID) to the specified Movie (by ID)
	 * @param actorId
	 * @param movieId
	 */
    public void addActedInRelationship(String actorId, String movieId) {
        try (Session session = DBUtil.getSession()) {
                Transaction tx = session.beginTransaction();
                
                Statement query = new Statement( //WARNING: spaces can make or break the syntax, add one after each line
                		"MATCH (a:Actor), (m:Movie) "
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

	
    /**
     * Get a JSON string of the specified Actor by ID that contains their name and ID
     * @param actorId
     * @return
     */
    public String getActorById(String actorId) {
	    try (Session session = DBUtil.getSession()) {    
                StatementResult result = session.run("MATCH (a:Actor {actorId: $actorId}) RETURN a.name AS name, a.actorId AS actorId", Map.of("actorId", actorId));

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
    
    
    /**
     * Get a JSON string of the specified Movie by ID that contains its name and ID
     * @param actorId
     * @return
     */
    public String getMovieById(String movieId) {
	    try (Session session = DBUtil.getSession()) {    
                StatementResult result = session.run("MATCH (m:Movie {movieId: $movieId}) RETURN m.name AS name, m.movieId AS movieId", Map.of("movieId", movieId));

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

    /**
	 * Whether the specified Actor (by ID) has an :ACTED_IN relationship with the specified Movie (by ID)
	 * @param actorId
	 * @param movieId
	 * @return
	 */
    public boolean hasActedInRelationship(String actorId, String movieId) {
    try (Session session = DBUtil.getSession()) {
        StatementResult result = session.run(
            "MATCH (a:Actor {actorId: $actorId})-[:ACTED_IN]->(m:Movie {movieId: $movieId}) " +
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

   /**
    * Compute the bacon number of the specified Actor (by ID)
    * @param actorId
    * @return
    */
   public int computeBaconNumber(String actorId) {
        try (Session session = DBUtil.getSession()) {
            StatementResult result = session.run(
                    "MATCH (bacon:Actor {actorId: 'nm0000102'}), (actor:Actor {actorId: $actorId}), " +
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

   /**
    * Return an ordered list of names representing the bacon path of the specified Actor (by ID)
    * @param actorId
    * @return
    */
   public List<String> computeBaconPath(String actorId) {
        try (Session session = DBUtil.getSession()) {
            StatementResult result = session.run(
                    "MATCH (bacon:Actor {actorId: 'nm0000102'}), (actor:Actor {actorId: $actorId}), " +
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
