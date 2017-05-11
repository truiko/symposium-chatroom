package database;

public class Test {

	public static void main (String[] args){
		DBConnect connect =  new DBConnect();
		//connect.getData("select * from songs", "Title", "Title: ");
		System.out.println("");
		//connect.getData("select * from tanks", "TANK_NAME", "Tank: ");
		
		connect.putInData("INSERT INTO songs " + "VALUES ('cat', 'Joyce')");
				
	}

}
