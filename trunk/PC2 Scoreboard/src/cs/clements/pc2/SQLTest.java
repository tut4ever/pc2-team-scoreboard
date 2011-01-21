package cs.clements.pc2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLTest
{
	public static void main(String...args) throws SQLException
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
		}
		catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		
		java.sql.Statement s;
		
		String url = "jdbc:mysql://192.168.0.1:3306";
		
		Connection con = null;
		try
		{
			con = DriverManager.getConnection(url,"validator","contest3");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		s = con.createStatement();
		s.executeUpdate("DELETE FROM errors.hi");
		
		
		
		
		
		try
		{
			con.close();
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
