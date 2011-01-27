package cs.clements.pc2;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Scanner;
import java.util.HashMap;
import java.io.File;


//Parses Team IPs from a PC^2 export of logins. Puts in a map and will log to SQL
public class IPToTeamParser 
{
	HashMap<String,String> connections = new HashMap<String,String>();
	
	public static void main(String...args) throws Exception
	{
		System.out.println(new IPToTeamParser("connections.txt"));	
	}
	
	public IPToTeamParser(String file) throws Exception
	{
		Scanner in = null;
		try{in = new Scanner(new File(file));}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("File not found");
		}
		while(!in.nextLine().contains("TEAM")){}
		
		String connection = null, teamName = null;;
		
		while((connection=in.nextLine()).contains("   TEAM") && !connection.contains("client"))
		{
			//Fixes some capitalization things that will otherwise have to be fixed later. Untested!
			teamName = connection.split(" ")[3];
			teamName = (teamName.length() > 1?teamName.substring(0,1).toUpperCase() + teamName.substring(1).toLowerCase():teamName);
			
			try
			{
				connections.put(teamName,connection.substring(connection.indexOf("/")+1,connection.indexOf(",")));
			}
			catch(Exception e)
			{
				System.out.println("Error with " + teamName);
			}
			
		}
		
		SQLConn con = new SQLConn();
		
		Statement s = con.getStatement();
		 
		s.executeUpdate("DELETE FROM errorsystem.logins");
		
		
		Iterator<String> it = connections.keySet().iterator();
		while(it.hasNext())
		{
			s = con.getStatement();
			String team = it.next();
			s.executeUpdate("INSERT INTO errorsystem.logins (ip, team) VALUES ('" + connections.get(team) + "','" + team.substring(team.toLowerCase().indexOf("m")+1) + "')");
		}
		
		con.close();
		
		
	}
	public String toString()
	{
		return connections.toString();
	}
	
}
