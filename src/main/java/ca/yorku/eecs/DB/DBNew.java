package ca.yorku.eecs.DB;

import org.json.JSONArray;
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
	 * Sets the rating of a Movie with the specified title.
	 * @param movieTitle
	 * @param newRating
	 */
	public void addMovieRating(String movieId, String newRating) {
		try(Session session = DBUtil.getSession()){
			Transaction tx = session.beginTransaction();
			Statement query = new Statement("MATCH (m:Movie {movieId: $movieId}) SET m.rating = $rating", 
					Map.of("movieId", movieId, "rating", newRating));
			StatementResult result = tx.run(query);
			
			System.out.println("Statement result: " + result.consume());
			tx.success();
			tx.close();
			session.close();
		}
	}

	
	/**
	 * Get a list of the titles of all movies that are above a certain rating.
	 * @param minRating
	 * @return
	 */
	public String getMoviesWithRating(String minRating) {
	    try (Session session = DBUtil.getSession()) {
	        Transaction tx = session.beginTransaction();
	        
	        StatementResult result = tx.run(
	            "MATCH (m:Movie) "
	            + "WHERE toFloat(m.rating) >= toFloat($rating) "
	            + "RETURN m.movieId AS movieId, m.name AS name, m.rating AS rating", 
	            Map.of("rating", minRating)
	        );
	        if(!result.hasNext()) {
	        	return null;
	        }
	        
	        JSONArray movies = new JSONArray();
	        while (result.hasNext()) {
	            Record record = result.next();
	            Map<String, Object> map = new LinkedHashMap<>();
	            map.put("movieId", record.get("movieId").asString());
	            map.put("name", record.get("name").asString());
	            map.put("rating", record.get("rating").asString());
	            JSONObject movie = new JSONObject(map);
	            movies.put(movie);
	        }
	        
	        tx.success(); // Mark the transaction as successful
	        tx.close();
	        session.close();
	        return movies.toString();
	    }
	}
	
	/**
	 * Get a list of all the movie titles whose release dates match the specified year.
	 * @param year
	 * @return
	 */
	public String getMoviesByReleaseYear(String year){
		try(Session session = DBUtil.getSession()) {
			Transaction tx = session.beginTransaction();
			StatementResult result = tx.run("MATCH (m:Movie) "
				+ "WHERE m.release = $release "
				+ "RETURN m.movieId AS movieId, m.name AS name, m.release AS release", 
				Map.of("release", year)
			);
			if(!result.hasNext()) {
				return null;
			}
			JSONArray movies = new JSONArray();
			while(result.hasNext()) {
				Record record = result.next();	
				Map<String, Object> map = new LinkedHashMap<>();
				map.put("movieId", record.get("movieId").asString());
				map.put("name", record.get("name").asString());
				map.put("release", record.get("release").asString());
				JSONObject movie = new JSONObject(map);
				movies.put(movie);
			}
			tx.success();
			tx.close();
			session.close();
			return movies.toString();
		}
	}
	
	/**
	 * Add a list of awards (String) to the actor specified by name.
	 * @param actor the name of the actor
	 * @param awards a list containing the names of awards
	 */
	public void addAward(String actorId, String award) {
		try(Session session = DBUtil.getSession()){
			Transaction tx = session.beginTransaction();
			Statement query = new Statement("MATCH (a:Actor {actorId: $actorId}) SET a.award = $award",
	                Map.of("actorId", actorId, "award", award));	
			StatementResult result = tx.run(query);
			System.out.println("Statement result: " + result.consume());
			tx.success();
			tx.close();
			session.close();
		}
	}
	
	/**
	 * Get a list of actors which have the specified award.
	 * @param award The name of the award
	 * @return
	 */
	public String getActorsByAward(String award){
		try(Session session = DBUtil.getSession()) {
			Transaction tx = session.beginTransaction();
			StatementResult result = tx.run("MATCH (a:Actor) "
				+ "WHERE a.award = $award "
				+ "RETURN a.actorId AS actorId, a.name AS name, a.award AS award", 
				Map.of("award", award)
			);
			if(!result.hasNext()) {
				return null;
			}
			JSONArray actors = new JSONArray();
			while(result.hasNext()) {
				Record record = result.next();
				Map<String, Object> map = new LinkedHashMap<>();
				map.put("actorId", record.get("actorId").asString());
				map.put("name", record.get("name").asString());
				map.put("award", record.get("award").asString());
				JSONObject actor = new JSONObject(map);
				actors.put(actor);
			}
			tx.success();
			tx.close();
			session.close();
			return actors.toString();
			}
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
			
			//result.list();
			//result.single();
						
			/*
			 * for whatever reason, this causes the function to hang
			 * System.out.println("Statement result: " + result.single().get(0).asString());
			 * 
			 * and the below statement is also require in order for the function to not hang
			 */
			System.out.println("Statement result: " + result.consume()); //^^maybe something to do with lazy initialization?
			
			transaction.success();
			transaction.close();
			session.close();
		}
	}
	
	/**
	 * Remove an Actor node to the database.
	 * @param actorName
	 * @param actorId
	 */
	public void removeActor(String actorId) {
		try(Session session = DBUtil.getSession()){
			
			Transaction transaction = session.beginTransaction();
			
			Statement query = new Statement("MATCH (a: Actor) "
					+ "WHERE a.actorId = $actorId "
					+ "DETACH DELETE a; ",
					Map.of("actorId", actorId));
			
			StatementResult result = transaction.run(query);
						
			System.out.println("Statement result: " + result.consume()); //^^maybe something to do with lazy initialization?
			
			transaction.success();
		}
	}
	
	public void removeMovie(String movieId) {
		try(Session session = DBUtil.getSession()) {
			Transaction transaction = session.beginTransaction();
			
			Statement query = new Statement("MATCH (m: Movie) "
					+ "WHERE m.movieId = $movieId "
					+ "DETACH DELETE m; ",
					Map.of("movieId", movieId));
			StatementResult result = transaction.run(query);
			System.out.println("Statement result: " + result.consume());
			transaction.success();
			transaction.close();
			session.close();
		}
	}
	public void removeRating(String movieId) {
		try(Session session = DBUtil.getSession()) {
			Transaction transaction = session.beginTransaction();
			
			Statement query = new Statement("MATCH (m: Movie) "
					+ "WHERE m.movieId = $movieId "
					+ "DETACH DELETE m; ",
					Map.of("movieId", movieId));
			StatementResult result = transaction.run(query);
			System.out.println("Statement result: " + result.consume());
			transaction.success();
			transaction.close();
			session.close();
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
                tx.close();
                session.close();

        }
    }

    public void removeActedInRelationship(String actorId, String movieId) {
        try (Session session = DBUtil.getSession()) {
            Transaction tx = session.beginTransaction();
            
            Statement query = new Statement( 
                    "MATCH (a:Actor)-[r:ACTED_IN]->(m:Movie) "
                    + "WHERE a.actorId = $actorId AND m.movieId = $movieId "
                    + "DELETE r "
                    + "RETURN count(r) AS deletedCount;",
                    Map.of("actorId", actorId, "movieId", movieId)
                    );
            
            StatementResult result = tx.run(query);
            
            System.out.println("Statement result: " + result.consume());
            
            tx.success();
            tx.close();
            session.close();
        }
    }
    /**
     * Get a JSON string of the specified Actor by ID that contains their name and ID
     * @param actorId
     * @return
     */
    public String getActorById(String actorId) {
        try (Session session = DBUtil.getSession()) {
            // Start a transaction
            try (Transaction tx = session.beginTransaction()) {
                
                // Fetch the actor details
            	StatementResult actorResult = tx.run(
                    "MATCH (a:Actor {actorId: $actorId}) " +
                    "RETURN a.name AS name, a.actorId AS actorId", 
                    Map.of("actorId", actorId)
                );
        
                if (actorResult.hasNext()) {
                    Record actorRecord = actorResult.next();
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("actorId", actorRecord.get("actorId").asString());
                    map.put("name", actorRecord.get("name").asString());
        
                    // Fetch the list of movies the actor has acted in
                    StatementResult movieResult = tx.run(
                        "MATCH (a:Actor {actorId: $actorId})-[:ACTED_IN]->(m:Movie) " +
                        "RETURN m.movieId AS movieId", 
                        Map.of("actorId", actorId)
                    );
                    JSONArray movies = new JSONArray();
                    while (movieResult.hasNext()) {
                        Record movieRecord = movieResult.next();
                        movies.put(movieRecord.get("movieId").asString());
                    }
                    JSONObject actor = new JSONObject(map);
                    actor.put("movies", movies);
                    
                    // Commit the transaction
                    tx.success();
                    tx.close();
                    session.close();
                    return actor.toString();
                } else {
                    return null;
                }
            }catch(JSONException e) {
            	e.printStackTrace();
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
            // Start a transaction
            try (Transaction tx = session.beginTransaction()) {
                
                // Fetch the movie details
                StatementResult movieResult = tx.run(
                    "MATCH (m:Movie {movieId: $movieId}) " +
                    "RETURN m.name AS name, m.movieId AS movieId", 
                    Map.of("movieId", movieId)
                );

                if (movieResult.hasNext()) {
                    Record movieRecord = movieResult.next();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("movieId", movieRecord.get("movieId").asString());
                    jsonObject.put("name", movieRecord.get("name").asString());

                    // Fetch the list of actors associated with the movie
                    StatementResult actorResult = tx.run(
                        "MATCH (m:Movie {movieId: $movieId})<-[:ACTED_IN]-(a:Actor) " +
                        "RETURN a.actorId AS actorId", 
                        Map.of("movieId", movieId)
                    );

                    JSONArray actors = new JSONArray();
                    while (actorResult.hasNext()) {
                        Record actorRecord = actorResult.next();
                        actors.put(actorRecord.get("actorId").asString());
                    }
                    jsonObject.put("actors", actors);

                    // Commit the transaction
                    tx.close();

                    return jsonObject.toString();
                } else {
                    // Rollback the transaction if no movie found
                    return null;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null; // In case of JSON processing error
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
   
   
   public List<String> getMoviesOfActor(String id){
       try(Session session = DBUtil.getSession()){
           Transaction tx = session.beginTransaction();
           String query = "MATCH (a: actor {actorId: '" + id + "'})-[:ACTED_IN]->(fof) RETURN DISTINCT fof.id AS id;";
           StatementResult result = tx.run(query);
           List<String> movies = new ArrayList<>();
           while(result.hasNext()) {
               movies.add(result.next().get("id").asString());
           }
           tx.success();
           tx.close();
           session.close();
           return movies;
       }
   }
       
   public List<String> getActorsByMovie(String movieId) {
	    try (Session session = DBUtil.getSession()) {
	        Transaction tx = session.beginTransaction();
	        String query = "MATCH (m:Movie {movieId: '" + movieId + "'})<-[:ACTED_IN]-(a:Actor) RETURN DISTINCT a.name AS name;";
	        StatementResult result = tx.run(query);
	        List<String> actors = new ArrayList<>();
	        while (result.hasNext()) {
	            actors.add(result.next().get("name").asString());
	        }
	        tx.success();
	        tx.close();
	        session.close();
	        return actors;
	    }
	}
   /*
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
   */
}
