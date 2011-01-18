package cs.clements.pc2.scoreboard;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

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
	
	//A Background image - Matthew will rewrite this part
	public static final String BACKGROUND = "background.png";
	
	//The novice file - should be an advanced one too
	public static final String NOVICE_TEAMS = "novice.txt";
	public static final String ADVANCED_TEAMS = "advanced.txt";
	public static final String SCORE_VALUES = "score values.txt";
	
	public static final Font DISPLAY_FONT = new Font("Lucida Console", Font.BOLD, 16);
	public static final int CHAR_WIDTH = 11;
	
	//public static final int REFRESH_TIME = 60000;
	
	//Self explanatory
	private static final int WINDOW_WIDTH = 500, WINDOW_HEIGHT=500;
	
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
			in = new Scanner(new File(NOVICE_TEAMS));
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		
		while (in.hasNext())
		{
			novTeams.add("Team " + in.nextLine());
		}
		in.close();
		
		try
		{
			in = new Scanner(new File(ADVANCED_TEAMS));
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		while (in.hasNext())
		{
			advTeams.add("Team " + in.nextLine());
		}
		in.close();
		
		try
		{
			in = new Scanner(new File(SCORE_VALUES));
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
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
			in = new Scanner(new File("login.txt"));
		}
		catch (FileNotFoundException e1)
		{
			System.out.println("No login file. Please create login.txt and try again");
			System.exit(1);
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
			System.exit(1);
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
			LinkedList<TeamScore> list = sc.name.contains("(adv)") ? s1 : s2;
			
			if (sc.name.contains("adv"))
			{
				s1.add(sc);
			}
			else if (sc.name.contains("nov"))
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
		
		for (int x = 1; i2.hasNext() && x <= 10; x++)
		{
			String line = x + ". " + i2.next();
			
			int lineWidth = CHAR_WIDTH * line.length();
			
			g.drawString(line, 250 - lineWidth / 2, 10 + 20 * x);
		}
		
		i2 = s2.iterator();
		
		for (int x = 1; i2.hasNext() && x <= 10; x++)
		{
			String line = x + ". " + i2.next();
			
			int lineWidth = CHAR_WIDTH * line.length();
			
			g.drawString(line, 250 - lineWidth / 2, 220 + 20 * x);
		}
	}
	
	public void addRun(IRun run, boolean add)
	{
		ITeam team = run.getTeam();
		String name = team.getDisplayName();
		name += novTeams.contains(name) ? " (nov)" : " (adv)";
		
		if (!scores.containsKey(name))
			return;
		
		
		if (run.getJudgementName().equals("Yes") && !run.isDeleted())
		{
			Point prevScore = scores.get(name);
			int score = (int) prevScore.getX();
			try
			{
				score += (add ? 1 : -1) * scoreValues.get(run.getProblem().getName());
			}
			catch (NullPointerException e)
			{
				e.printStackTrace();
				System.exit(1);
			}
			int time2 = (int) prevScore.getY();
			int time = Math.max((int) run.getSubmissionTime(), time2);
			scores.put(name, new Point(score, time));
			
			repaint();
		}
	}
	
	public void reloadScores()
	{
		scores.clear();
		actionPerformed(null);
	}
	public void reloadTeams()
	{
		novTeams = new HashSet<String>();
		advTeams = new HashSet<String>();
		
		Scanner in = null;
		try
		{
			in = new Scanner(new File(NOVICE_TEAMS));
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		
		while (in.hasNext())
		{
			novTeams.add("Team " + in.nextLine());
		}
		in.close();
		
		try
		{
			in = new Scanner(new File(ADVANCED_TEAMS));
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		while (in.hasNext())
		{
			advTeams.add("Team " + in.nextLine());
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
	
	public void keyReleased(KeyEvent e) 
	{
		
	}
	public void keyTyped(KeyEvent e) {}
	
	public void keyPressed(KeyEvent e)
	{
	//I seem to have forgotten how to check keypresses correctly
		
		/*	if(e.equals(KeyEvent.VK_R))
			reloadTeams();
		if(e.equals(KeyEvent.VK_S))
		{
			reloadScoreTable();
			System.out.println("Reload scores");
		}*/
		actionPerformed(null);
	}
	
	// Update scores to get initial scores or refresh scores
	public void actionPerformed(ActionEvent e)
	{
		scores = new HashMap<String, Point>();
		
		for (ITeam team : contest.getTeams())
		{
			if (team.isDisplayableOnScoreboard())
			{
				String name = team.getDisplayName();
				name += novTeams.contains(name) ? " (nov)" : " (adv)";
				
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