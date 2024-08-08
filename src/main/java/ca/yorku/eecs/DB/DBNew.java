package ca.yorku.eecs.DB;

import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.Record;

import java.util.*;
import java.util.Map;

public class DBNew {
	
	public void addRatingToAllMovies(double defaultRating) {
		try(Session session = DBUtil.getSession()){
			session.run("MATCH (m:Movie) SET m.rating = $rating", Map.of("rating", String.format("%.2f", defaultRating)));
		}
	}
	
	public void updateMovieRating(String movieTitle, double newRating) {
		try(Session session = DBUtil.getSession()){
			session.run("MATCH (m:Movie {title: $title}) SET m.rating = $rating", Map.of("title", movieTitle, "rating", String.format("rating", String.format("%.1f", newRating))));
		}
	}
	
	public List<String> getMoviesWithRating(double minRating) {
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
	
	public List<String> getMoviesByReleaseYear(int year){
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
	public void addAwards(String actor, List<String> awards) {
		try(Session session = DBUtil.getSession()){
			session.run("MATCH (a:Actor {name: $name}) SET a.awards = $awards",
	                Map.of("name", actor, "awards", awards));	
		}
	}
	
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
	
	
	
	
	
}