package cs.clements.pc2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.Timer;

import edu.csus.ecs.pc2.api.IContest;
import edu.csus.ecs.pc2.api.IRun;
import edu.csus.ecs.pc2.api.ServerConnection;
import edu.csus.ecs.pc2.api.exceptions.LoginFailureException;
import edu.csus.ecs.pc2.api.listener.IRunEventListener;

//Will store runs in the SQL database
public class SQLPC2Link implements KeyListener, WindowListener, ActionListener, IRunEventListener 
{
	private static final int REFRESH_TIME = 5000;
	private IContest contest;
	private ServerConnection server;
	private JFrame frame;

	
	private SQLConn con;
	private Statement s;
	private HashSet<Integer> trackedRuns;
	public SQLPC2Link(String...args)
	{
		con = new SQLConn();
		con.getConnection();
		s = con.getStatement();
		
		trackedRuns = new HashSet<Integer>();
		
		server = new ServerConnection();
		
		Scanner in = null;
		
		try
		{
			in = new Scanner(new File("login.txt"));
		}
		catch (FileNotFoundException e1)
		{
			System.out.println("No login file. Please create login.txt and try again");
			System.exit(1);
		}
		
		/*String login = in.nextLine();
		String password = in.nextLine();*/
		
		String login = "administrator2";
		String password = "admin2";
		
		in.close();
		
		try
		{
			contest = server.login(login, password);
		}
		catch (LoginFailureException e)
		{
			System.out.println("Incorrect Login. Please check the file and try again.");
			System.exit(1);
		}
		
		frame = new JFrame("PC^2 --> SQL Link");
		frame.setSize(50,50);
		frame.setResizable(true);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.addWindowListener(this);
		frame.addKeyListener(this);	
		frame.setVisible(true);
		
		getRuns();
		
		new Timer(REFRESH_TIME, this).start();
	}
	
	//Memory friendly
	private IRun[]runs;
	private int runNumber;
	private void getRuns()
	{
		frame.setTitle("Refreshing... " + "PC^2 --> SQL Link");
		runs = contest.getRuns();
		for(IRun run: runs)
		{
			runNumber = run.getNumber();
						
			if(!trackedRuns.contains(runNumber))
			{	
				byte[][]sourceFile1;
				try
				{
					sourceFile1 = run.getSourceCodeFileContents();
				}
				catch(Exception e)
				{
					continue;
				}
				
				byte[]sourceFile;
				if(sourceFile1.length == 0)
					continue;
				else
					sourceFile = sourceFile1[0];
				
				trackedRuns.add(runNumber);
				String md5 = null;
				
				try
				{
					MessageDigest algorithm = MessageDigest.getInstance("MD5");
					algorithm.reset();
					algorithm.update(sourceFile);
					byte messageDigest[] = algorithm.digest();
				            
					StringBuffer hexString = new StringBuffer();
					for (int i=0;i<messageDigest.length;i++)
					{
						hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
					}
					
					md5 = hexString.toString();
					
					//String foo = messageDigest.toString();
					//System.out.println("sessionid "+sessionid+" md5 version is "+hexString.toString());
					//sessionid=hexString+"";
				}
				catch(NoSuchAlgorithmException e)
				{
					System.out.println("MD5 Error");
					e.printStackTrace();
					System.exit(1);
				}
				
				//Team Number: run.getTeam().getAccountNumber()
				//md5 = md5
				//Run Number = runNumber
				
				
				try
				{
					//Add check for duplicate run number
					
					
					ResultSet r = s.executeQuery("SELECT * FROM errorsystem.errors WHERE md5='" + md5 + "'");
					//System.out.println(con.getConnection());
					s = con.getConnection().createStatement();
					ResultSet r1 = s.executeQuery("SELECT * FROM errorsystem.errors WHERE md5='" + md5 + "' AND error=''");
					
					if(r1.first())
					{
						s.executeUpdate("UPDATE errorsystem.errors SET team='"+ run.getTeam().getAccountNumber() + "', run='" + runNumber + "' WHERE md5='" + md5 + "' AND error='' LIMIT 1");
						System.out.println("Updated run " + runNumber + " in SQL");
					}
					else if(!r.first())
					{
						s.executeUpdate("INSERT INTO errorsystem.errors (md5, run, team, error) VALUES ('" + md5 + "','" + runNumber + "','" + run.getTeam().getAccountNumber() +  "','')");
						System.out.println("Added run " + runNumber + " to SQL");
					}
					else
					{
						//System.out.println("Replacing a non-blank run?");
						s.executeUpdate("UPDATE errorsystem.errors SET team='"+ run.getTeam().getAccountNumber() + "', run='" + runNumber + "' WHERE md5='" + md5 + "' LIMIT 1");
						System.out.println("Updated run " + runNumber + " in SQL");
					}
				}
				catch(com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException e)
				{
					System.out.println("Integrity Exception");
				}
				catch(SQLException e)
				{
					e.printStackTrace();
					trackedRuns.remove(new Integer(runNumber));
					
					try
					{
						Thread.sleep(500);
					}
					catch (InterruptedException e1)
					{
						e1.printStackTrace();
					}
				}
				
				
				
				
				
				/*
				 * 
				 * if(SELECT * FROM errors WHERE md5=java md5)
				 * 	UPDATE errors SET team=team number, run=run number WHERE md5=java md5 LIMIT 1;
				 * else
				 * 	INSERT INTO errors (run, team, md5) VALUES ('','','')
				 * 
				 * 
				 * 
				 * 
				 */
			}
			
		}
		
		
		frame.setTitle("PC^2 --> SQL Link");
		
		
	}
	
	public void exit()
	{
		try
		{
			con.close();
			if(server.logoff())
				System.out.println("Logged off");
			else
				System.out.println("Logoff failed");
		}
		catch(Exception e)
		{
			System.out.println("ServerConnection Exception Shutting Down");
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	
	public static void main(String...args)
	{
		new SQLPC2Link(args);
	}
	@Override
	public void keyPressed(KeyEvent arg0)
	{
		int keyCode = arg0.getKeyCode();
		switch(keyCode)
		{
			case KeyEvent.VK_ESCAPE:
				exit();
				break;
		}
		
	}
	@Override
	public void keyReleased(KeyEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void keyTyped(KeyEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowActivated(WindowEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowClosed(WindowEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowClosing(WindowEvent arg0)
	{
		exit();		
	}
	@Override
	public void windowDeactivated(WindowEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowDeiconified(WindowEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowIconified(WindowEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowOpened(WindowEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		
		getRuns();
		
	}

	@Override
	public void runCheckedOut(IRun arg0, boolean arg1)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void runCompiling(IRun arg0, boolean arg1)
	{
		getRuns();
		
	}

	@Override
	public void runDeleted(IRun arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void runExecuting(IRun arg0, boolean arg1)
	{
		
	}

	@Override
	public void runJudged(IRun arg0, boolean arg1)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void runJudgingCanceled(IRun arg0, boolean arg1)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void runSubmitted(IRun arg0)
	{
		
	}

	@Override
	public void runUpdated(IRun arg0, boolean arg1)
	{
		getRuns();
		
	}

	@Override
	public void runValidating(IRun arg0, boolean arg1)
	{
		// TODO Auto-generated method stub
	}
}
