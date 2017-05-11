package database;
import java.sql.*;
public class DBConnect {

		private Connection con;
		private Statement st;
		private ResultSet rs;
		//private int update;
	
		public DBConnect() {
			try{
				Class.forName("com.mysql.jdbc.Driver");
				
				con = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "");
				st = con.createStatement();
				
			}catch(Exception ex){
				System.out.println("Error: " + ex);
			}
	}
		public void getData(String query, String colName, String print){
			try{
				rs = st.executeQuery(query);
				System.out.println("Records from Database");
				while(rs.next()){
					String column = rs.getString(colName);
					System.out.println(print + column);
				}
				
			}catch(Exception ex){
				System.out.println(ex);
			}
		}

		public void putInData(String query){
			try{
				st = con.createStatement();
				System.out.println(st.executeUpdate(query));
			}catch(Exception ex){
				System.out.println(ex);
			}
		}
}
