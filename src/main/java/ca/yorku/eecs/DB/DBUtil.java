package ca.yorku.eecs.DB;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;

public class DBUtil {
	private static Driver driver;
	
	public static void connect(String uri, String user, String password) {
		Config config = Config.builder().withoutEncryption().build();
		driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
	}
	
	public static Session getSession() {
		return driver.session();
	}
	
	public static void close() {
		driver.close();
	}
}
