package cs.clements.pc2.scoreboard;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.net.URI;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import edu.csus.ecs.pc2.api.IContest;
import edu.csus.ecs.pc2.api.IRun;
import edu.csus.ecs.pc2.api.ITeam;
import edu.csus.ecs.pc2.api.ServerConnection;
import edu.csus.ecs.pc2.api.exceptions.LoginFailureException;
import edu.csus.ecs.pc2.api.listener.IRunEventListener;

public class Scoreboard extends JPanel implements IRunEventListener, ActionListener, KeyListener
{
	//Ignore
	private static final long serialVersionUID = -7302013807793076433L;
	
	public static final String BACKGROUND = "background.png";
	
	private String SERVER_IP = "http://127.0.0.1";
	
	//Made these non-static because of the IP
	public String NOVICE_TEAMS = new File("novice.txt").exists() ? "novice.txt" : SERVER_IP + "/scoreboard/novice.txt";
	public String ADVANCED_TEAMS = new File("advanced.txt").exists() ? "advanced.txt" : SERVER_IP + "/scoreboard/advanced.txt";
	public String SCORE_VALUES = new File("score_values.txt").exists() ? "score_values.txt" : SERVER_IP + "/scoreboard/score_values.txt";
	
	public static final Font DISPLAY_FONT = new Font("Lucida Console", Font.BOLD, 22);
	public static final int CHAR_WIDTH = 19;
	
	//public static final int REFRESH_TIME = 60000;
	
	//Self explanatory
	private static final int WINDOW_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width, WINDOW_HEIGHT=Toolkit.getDefaultToolkit().getScreenSize().height;
	
	// Maps problems to point values
	static HashMap<String, Integer> scoreValues;
	
	// Stores scores of teams
	static HashMap<String, Point> scores;
	
	// List of novice teams
	static HashSet<String> novTeams;
	
	// List of advanced teams
	static HashSet<String> advTeams;
	
	//PC^2 contest object
	IContest contest;
	
	//Background Image - Matthew will rewrite this part. It should automatically ignore it for now.
	Image background;
	
	public static void main(String[]asdf) throws Exception
	{
		new Scoreboard();
	}
	
	public Scoreboard()
	{
		scoreValues = new HashMap<String, Integer>();
		scores = new HashMap<String, Point>();
		novTeams = new HashSet<String>();
		advTeams = new HashSet<String>();
		
		Scanner in = null;
		try
		{
			in = new Scanner(new File("IP.txt"));
			SERVER_IP = in.nextLine();
			
			NOVICE_TEAMS = new File("novice.txt").exists() ? "novice.txt" : SERVER_IP + "/scoreboard/novice.txt";
			ADVANCED_TEAMS = new File("advanced.txt").exists() ? "advanced.txt" : SERVER_IP + "/scoreboard/advanced.txt";
			SCORE_VALUES = new File("score_values.txt").exists() ? "score_values.txt" : SERVER_IP + "/scoreboard/score_values.txt";
		}
		catch(Exception e)
		{
			System.out.println("IP File (IP.txt) error");
			e.printStackTrace();
		}
		in.close();
		
		
		try
		{
			in = new Scanner((new URI(NOVICE_TEAMS).isAbsolute() ? new URI(NOVICE_TEAMS).toURL().openStream() : new FileInputStream(new File(new URI(NOVICE_TEAMS).toString()))));
		}
		catch (Exception e)
		{
			System.out.println("Novice list (" + NOVICE_TEAMS + ") not found");
			e.printStackTrace();
		}
		
		while (in.hasNext())
		{
			novTeams.add("team" + in.nextLine());
		}
		in.close();
		
		try
		{
			in = new Scanner((new URI(ADVANCED_TEAMS).isAbsolute() ? new URI(ADVANCED_TEAMS).toURL().openStream() : new FileInputStream(new File(new URI(ADVANCED_TEAMS).toString()))));
		}
		catch (Exception e)
		{
			System.out.println("Advanced list (" + ADVANCED_TEAMS + ") not found");
			e.printStackTrace();
		}
		while (in.hasNext())
		{
			advTeams.add("team" + in.nextLine());
		}
		in.close();
		
		try
		{
			in = new Scanner((new URI(SCORE_VALUES).isAbsolute() ? new URI(SCORE_VALUES).toURL().openStream() : new FileInputStream(new File(new URI(SCORE_VALUES).toString()))));
		}
		catch (Exception e)
		{
			System.out.println("Score values list (" + SCORE_VALUES + ") not found");
			e.printStackTrace();
		}
		while(in.hasNext())
		{
			String[] scoreval = in.nextLine().split("/");
			scoreValues.put(scoreval[0], Integer.parseInt(scoreval[1]));
		}
		File backgroundFile = new File(BACKGROUND);
		if(backgroundFile.exists())
			try
			{
				background = ImageIO.read(backgroundFile);
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		//Connects to the PC^2 server
		ServerConnection server = new ServerConnection();
		
		try
		{
			//Yay for convoluted one-liners
			in = new File("login.txt").exists() ? new Scanner( new File("login.txt")) : new Scanner(new URI(SERVER_IP + "/scoreboard/login.txt").toURL().openStream());
		}
		catch (FileNotFoundException e1)
		{
			System.out.println("No login file (login.txt). Please create login.txt and try again");
		}
		catch (IOException e1)
		{
			System.out.println("IOException");
		}
		catch (URISyntaxException e1)
		{
			System.out.println("URISyntax");
		}

		//This could be a lot nicer, but there's no need
		String login = in.nextLine();
		String password = in.nextLine();
		
		in.close();
		
		//PC^2 side of things to login
		try
		{
			contest = server.login(login, password);
		}
		catch (LoginFailureException e)
		{
			System.out.println("Incorrect Login. Please check the file and try again.");
		}
		
		//Gets Scores from PC^2
		actionPerformed(null);
		
		JFrame frame = new JFrame("PC^2 Scoreboard!");
		frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(this);
		frame.setVisible(true);
		
		frame.addKeyListener(this);
		
		contest.addRunListener(this);
		
		repaint();
		//new Timer(REFRESH_TIME, this).start();
	}
	
	public void paintComponent(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		g.setColor(Color.white);
		g.fillRect(0,0,WINDOW_WIDTH, WINDOW_HEIGHT);
		g.drawImage(background, 0, 0, null);
		g.setColor(Color.BLACK);
		g.setFont(DISPLAY_FONT);
		
		Iterator<Map.Entry<String, Point>> i = scores.entrySet().iterator();
		LinkedList<TeamScore> s1 = new LinkedList<TeamScore>();
		LinkedList<TeamScore> s2 = new LinkedList<TeamScore>();
		
		while (i.hasNext())
		{
			Map.Entry<String, Point> sd = i.next();
			TeamScore sc = new TeamScore(sd.getKey(), (int) sd.getValue().getX(), (int) sd.getValue().getY());
			
			
			@SuppressWarnings("unused")
			LinkedList<TeamScore> list = advTeams.contains("team"+sc.name.substring(sc.name.lastIndexOf(" ")+1)) ? s1 : s2;
			
			if (advTeams.contains("team"+sc.name.substring(sc.name.lastIndexOf(" ")+1)))
			{
				s1.add(sc);
			}
			else if (novTeams.contains("team"+sc.name.substring(sc.name.lastIndexOf(" ")+1)))
			{
				s2.add(sc);
			}
			else
			{
				System.out.println("Erroneous team added");
			}
		}
		
		Collections.sort(s1);
		Collections.sort(s2);
		
		Iterator<TeamScore> i2 = s1.iterator();
		g.setFont(g.getFont().deriveFont(Font.BOLD,36));
		g.drawString("Advanced", WINDOW_WIDTH/2 - CHAR_WIDTH * "Advanced".length() / 2, 30);
		g.drawString("Novice", WINDOW_WIDTH/2 - CHAR_WIDTH * "Novice".length() / 2, WINDOW_HEIGHT/2 - 10);
		g.setFont(g.getFont().deriveFont(Font.BOLD,30));
		
		for (int x = 1; i2.hasNext() && x <= 10; x++)
		{
			String line = x + ". " + i2.next();
			
			int lineWidth = CHAR_WIDTH * line.length();
			
			g.drawString(line, WINDOW_WIDTH/2 - lineWidth / 2, 60 + 35 * x);
		}
		
		i2 = s2.iterator();
		
		for (int x = 1; i2.hasNext() && x <= 10; x++)
		{
			String line = x + ". " + i2.next();
			
			int lineWidth = CHAR_WIDTH * line.length();
			
			g.drawString(line, WINDOW_WIDTH/2 - lineWidth / 2, WINDOW_HEIGHT/2 + 25 + 35 * x);
		}
	}
	
	public void addRun(IRun run, boolean add)
	{
		ITeam team = run.getTeam();
		String name = team.getLoginName();
		if(novTeams.contains(name))
			name ="Team "+name.substring(name.lastIndexOf("m")+1);
		else if(advTeams.contains(name))
			name ="Team "+name.substring(name.lastIndexOf("m")+1);
		
		
		if (!scores.containsKey(name))
			return;
		
		
		if (run.getJudgementName().equals("Yes") && run.isFinalJudged() &&!run.isDeleted())
		{
			Point prevScore = scores.get(name);
			int score = (int) prevScore.getX();
			try
			{
				score += (add ? 1 : -1) * scoreValues.get(run.getProblem().getName());
			}
			catch (NullPointerException e)
			{
				//Problem on PC^2 
				System.out.println("Problem Not Found: " + run.getProblem().getName());
			}
			int time2 = (int) prevScore.getY();
			int time = Math.max((int) run.getSubmissionTime(), time2);
			scores.put(name, new Point(score, time));
			
			repaint();
		}
	}

	public void reloadTeams()
	{
		novTeams = new HashSet<String>();
		advTeams = new HashSet<String>();
		
		Scanner in = null;
		try
		{
			in = new Scanner((new URI(NOVICE_TEAMS).isAbsolute() ? new URI(NOVICE_TEAMS).toURL().openStream() : new FileInputStream(new File(new URI(NOVICE_TEAMS).toString()))));
		}
		catch (Exception e)
		{
			System.out.println("Novice list (" + NOVICE_TEAMS + ") not found");
			e.printStackTrace();
		}
		
		while (in.hasNext())
		{
			novTeams.add("team" + in.nextLine());
		}
		in.close();
		
		try
		{
			in = new Scanner((new URI(ADVANCED_TEAMS).isAbsolute() ? new URI(ADVANCED_TEAMS).toURL().openStream() : new FileInputStream(new File(new URI(ADVANCED_TEAMS).toString()))));
		}
		catch (Exception e)
		{
			System.out.println("Advanced list (" + ADVANCED_TEAMS + ") not found");
			e.printStackTrace();
		}
		while (in.hasNext())
		{
			advTeams.add("team" + in.nextLine());
		}
		in.close();
		
		
		System.out.println("Teams reloaded");
		
	}
	public void runCheckedOut(IRun run, boolean isFinal) {}
	public void runCompiling(IRun run, boolean isFinal) {}
	
	//Remove run
	public void runDeleted(IRun run) {addRun(run, false);}
	public void runExecuting(IRun run, boolean isFinal) {}
	
	//Refresh runs on this
	public void runJudged(IRun run, boolean isFinal) {actionPerformed(null);}
	public void runJudgingCanceled(IRun run, boolean isFinal) {}
	public void runSubmitted(IRun run) {}
	
	//Refresh runs on this
	public void runUpdated(IRun run, boolean isFinal) {actionPerformed(null);}//}if (true) return; addRun(run, true);}
	
	public void runValidating(IRun run, boolean isFinal) {}
	
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
	public void keyPressed(KeyEvent e)
	{
		if(e.getKeyCode()==KeyEvent.VK_R)
			reloadTeams();
		if(e.getKeyCode()==KeyEvent.VK_W)
			writeTeamList();
		actionPerformed(null);
	}
	
	
	public boolean writeTeamList()
	{
		FileWriter writer=null;
		
		Iterator<Map.Entry<String, Point>> i = scores.entrySet().iterator();
		LinkedList<TeamScore> s1 = new LinkedList<TeamScore>();
		LinkedList<TeamScore> s2 = new LinkedList<TeamScore>();
		
		while (i.hasNext())
		{
			Map.Entry<String, Point> sd = i.next();
			TeamScore sc = new TeamScore(sd.getKey(), (int) sd.getValue().getX(), (int) sd.getValue().getY());
			
			
			@SuppressWarnings("unused")
			LinkedList<TeamScore> list = advTeams.contains("team"+sc.name.substring(sc.name.lastIndexOf(" ")+1)) ? s1 : s2;
			
			if (advTeams.contains("team"+sc.name.substring(sc.name.lastIndexOf(" ")+1)))
			{
				s1.add(sc);
			}
			else if (novTeams.contains("team"+sc.name.substring(sc.name.lastIndexOf(" ")+1)))
			{
				s2.add(sc);
			}
			else
			{
				System.out.println("Erroneous team added");
			}
		}
		
		Collections.sort(s1);
		Collections.sort(s2);
		
		Iterator<TeamScore> i2 = s1.iterator();
		try 
		{
			writer = new FileWriter(new File("Team Scores.txt"));
			writer.write("------ADVANCED------\n\n");
			for (int x = 1; i2.hasNext(); x++)
			{
				TeamScore a = i2.next();
				writer.write(a.name +"|"+ a.score+"\n");
			}
			
			i2 = s2.iterator();
			writer.write("\n------NOVICE------\n\n");
			for (int x = 1; i2.hasNext(); x++)
			{
				TeamScore a = i2.next();
				writer.write(a.name +"|"+ a.score+"\n");
			}
			writer.close();
			System.out.println("Score file written.");
			return true;
		} 
		catch (IOException e) 
		{
			System.out.println("Problem writing team list to file.");
			e.printStackTrace();
		}
		return false;
	}
	
	// Update scores to get initial scores or refresh scores
	public void actionPerformed(ActionEvent e)
	{
		scores = new HashMap<String, Point>();
		
		for (ITeam team : contest.getTeams())
		{
			if (team.isDisplayableOnScoreboard())
			{
				String name = team.getLoginName();
				if (novTeams.contains(name) || advTeams.contains(name))
					name ="Team "+name.substring(name.lastIndexOf("m")+1);
				else continue;
				
				scores.put(name, new Point(0, 0));
			}
		}
		
		for (IRun run : contest.getRuns())
		{
			addRun(run, true);
		}
		
		System.out.println("Scores refreshed");
	}
}

class TeamScore implements Comparable<TeamScore>
{
	String name;
	int score;
	int lastRunTime;
	
	public TeamScore(String a, int b, int c)
	{
		name = a;
		score = b;
		lastRunTime = c;
	}
	
	public int compareTo(TeamScore o)
	{
		return o.score == score ? lastRunTime - o.lastRunTime : o.score - score;
	}
	
	public String toString()
	{
		return name + ": " + score + " pts @ " + lastRunTime + " s";
	}
}