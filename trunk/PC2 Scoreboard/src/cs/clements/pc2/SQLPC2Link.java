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
import java.util.HashSet;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.Timer;

import edu.csus.ecs.pc2.api.IContest;
import edu.csus.ecs.pc2.api.IRun;
import edu.csus.ecs.pc2.api.ServerConnection;
import edu.csus.ecs.pc2.api.exceptions.LoginFailureException;
import edu.csus.ecs.pc2.api.listener.IRunEventListener;

public class SQLPC2Link implements KeyListener, WindowListener, ActionListener, IRunEventListener 
{
	private static final int REFRESH_TIME = 30000;
	private IContest contest;
	private ServerConnection server;
	private JFrame frame;
	
	private HashSet<Integer> trackedRuns;
	public SQLPC2Link(String...args)
	{
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
		
		String login = in.nextLine();
		String password = in.nextLine();
		
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
				trackedRuns.add(runNumber);
				
				byte[]sourceFile = run.getSourceCodeFileContents()[0];
				
				@SuppressWarnings("unused")
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
				
				//TODO: INSERT INTO x () VALUES Team number, MD5 of source
			}
			
		}
		
		
		frame.setTitle("PC^2 --> SQL Link");
		
		
	}
	
	public void exit()
	{
		try
		{
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
