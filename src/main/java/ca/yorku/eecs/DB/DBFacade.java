package ca.yorku.eecs.DB;

/*
 * Facade that uses DBOperator to operate or return higher level results  
 */
public class DBFacade {
	
	private DBOperator operator;
	
	public DBFacade() {
		operator = new DBOperator();
	}
	
	public void insertActor(String json) {
		operator.insertActor(json);
	}

}
