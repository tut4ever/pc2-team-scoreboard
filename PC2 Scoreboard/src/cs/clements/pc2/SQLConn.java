package cs.clements.pc2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLConn
{
	private Statement s;
	private Connection con;
	public SQLConn()
	{
		try
		{
			try
			{
				Class.forName("com.mysql.jdbc.Driver");
			}
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
			
			
			String url = "jdbc:mysql://192.168.0.1:3306";
			
			con = null;

			con = DriverManager.getConnection(url,"validator","contest3");
			s = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);		
			
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
	public Connection getConnection()
	{
		return con;
	}
	public Statement getStatement()
	{
		return s;
	}
	public void close()
	{
		try
		{
			con.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}
