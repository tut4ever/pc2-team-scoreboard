package cs.clements.pc2.scoreboard;

import java.util.Scanner;
import java.util.HashMap;
import java.io.File;

public class Parser 
{
	HashMap<String,String> connections = new HashMap<String,String>();
	
	public static void main(String...args)
	{
		System.out.println(new Parser("connections.txt"));	
	}
	
	public Parser(String file)
	{
		Scanner in = null;
		try{in = new Scanner(new File(file));}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("File not found");
		}
		while(!in.nextLine().contains("TEAM")){}
		
		String connection = null;
		
		while((connection=in.nextLine()).contains("   TEAM"))
		{
			connections.put(connection.split(" ")[3],connection.substring(connection.indexOf("/")+1,connection.indexOf(",")));
		}
	}
	public String toString()
	{
		return connections.toString();
	}
	
}
