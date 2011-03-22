package com.bukkit.vicwhiten.invadecheck;



import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.anjocaido.groupmanager.data.Group;
import org.anjocaido.groupmanager.data.User;
import org.anjocaido.groupmanager.dataholder.OverloadedWorldHolder;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bukkit.vicwhiten.invadecheck.InvadeCheck;

public class InvadeCommand implements CommandExecutor {
    private final InvadeCheck plugin;
    /*
	 * 0 - Fire
	 * 1 - Water
	 * 2 - Earth
	 * 3 - Air
	 */
    public final List<String> nations =Arrays.asList("Fire", "Water", "Earth", "Air");
    public final ChatColor[] nationColors = {ChatColor.DARK_RED, ChatColor.BLUE, ChatColor.GREEN, ChatColor.YELLOW};
    public int[] numberOnline = {0,0,0,0};
    public boolean[][] canInvade = {{true,true,true,true},{true,true,true,true},
    		{true,true,true,true},{true,true,true,true}};
    public boolean[] attemptingInvasion = {false, false, false, false};
    public Coords[] nationLocs = {new Coords(355,950,696,1293),new Coords(-968,-233,-674,73),
    		new Coords(241,-317,620,-16), new Coords(-975,1036,-677,1351)};

    public final int MIN = 60000;
    public final int TWENTY_FIVE_MIN = 1500000;
    public final int FIVE_MIN = 300000;
    public final String ALERT_PREFIX = ChatColor.GOLD + "[INVADE ALERT] " + ChatColor.WHITE;
    
    public InvadeCommand(InvadeCheck plugin) {
        this.plugin = plugin;
        System.out.println("Setting up /invade Command");
    }

    public boolean onCommand(CommandSender sender, 
    		Command command, 
    		String label, String[] args) {
    	System.out.println("Invade Command Called, " + label);
    	if(label == "defend" && args.length == 1)
    	{
    		return defendAttempt(sender, command, label, args);
    	}
    	if(args.length == 0)
    	{
    		return invadeStatus(sender, command, label, args);
    	}
    	if(args.length == 1)
    	{
    		return invadeAttempt(sender, command,label, args);
    	}
    	return false;
    	
    }
    
    public boolean defendAttempt(CommandSender sender, Command command, String label, String[] args)
    {
    	return false;
    }
    public boolean invadeAttempt(CommandSender sender, 
    		Command command, 
    		String label, String[] args)
    {
    	//check if the nation is already attempting to invade
    	Player player = (Player) sender;
    	User user = plugin.wd.getDefaultWorld().getUser(player.getName());
    	setOnlineCount(sender.getServer());
    	int invaderIndex = nations.indexOf(user.getGroupName());
    	int defenderIndex = nations.indexOf(upperFirst(args[0]));
    	if(invaderIndex == -1)
    	{
    		sender.sendMessage(ALERT_PREFIX + "You are not in a nation!");
    		return true;
    	}
    	
    	if(defenderIndex == -1)
    	{
    		return false;
    	}
    	if(invaderIndex == defenderIndex)
    	{
    		sender.sendMessage("You can't invade yourself silly goose!");
    		return true;
    	}
    	
    	if(!canInvade[invaderIndex][defenderIndex])
    	{
    		
    		sender.sendMessage(ALERT_PREFIX + "You cannot invade " + nationColors[defenderIndex]
    		                   + nations.get(defenderIndex) + ChatColor.WHITE + " at this time");
    		sender.sendMessage(ALERT_PREFIX + "You must wait 30min between invasions");
    		return true;
    	}
    	
    	if(numberOnline[defenderIndex] < plugin.invadeMin)
    	{
    		
    		sender.sendMessage(ALERT_PREFIX + nationColors[defenderIndex] + 
    				nations.get(defenderIndex) + ChatColor.WHITE + " does not have enough users online");
    		return true;
    	}
    	
    	//initilize the invasion attempt
    	if(!attemptingInvasion[invaderIndex])
    	{
    		attemptingInvasion[invaderIndex] = true;
    		//broadcast the invasion attempt
    		
    		sendNationMessage(invaderIndex, ALERT_PREFIX + nationColors[invaderIndex] + player.getName() + ChatColor.WHITE +
    		                  " has begun an attempt to invade " + nationColors[defenderIndex] + nations.get(defenderIndex));
    		sendNationMessage(invaderIndex, ALERT_PREFIX + "You must have half of your online users '/invade nation' in the next minute.");
    		
    		plugin.users.get(invaderIndex).get(defenderIndex).clear();
    		Timer invadeSetup = new Timer();
    		invadeSetup.schedule(new InvadeSetupTimerTask(invaderIndex, defenderIndex), MIN);
    	}
    	
    	

			plugin.users.get(invaderIndex).get(defenderIndex).put(player.getName(), true);
			
			sendNationMessage(invaderIndex, ALERT_PREFIX + "You have " + 
					plugin.users.get(invaderIndex).get(defenderIndex).size() + "/" +
					(numberOnline[invaderIndex] / 2) + " users needed to invade " + 
					nationColors[defenderIndex] + nations.get(defenderIndex));
		
    		return true;
    }
    public boolean invadeStatus(CommandSender sender, 
    		Command command, 
    		String label, String[] args)
    {
    	setOnlineCount(sender.getServer());
    	sender.sendMessage("Minimum number of users to invade: " + plugin.invadeMin);
    	
    	sender.sendMessage(ChatColor.DARK_RED + "FIRE" + ChatColor.WHITE + ": ONLINE - " + numberOnline[0] 
    	                   + ". INVASION " + isAllowed(numberOnline[0]));
    	sender.sendMessage(ChatColor.BLUE + "WATER" + ChatColor.WHITE +  ": ONLINE - " + numberOnline[1] 
    	                   + ". INVASION " + isAllowed(numberOnline[1]));
    	sender.sendMessage(ChatColor.GREEN + "EARTH" + ChatColor.WHITE +  ": ONLINE - " + numberOnline[2]
    	                   + ". INVASION " + isAllowed(numberOnline[2]));
    	sender.sendMessage(ChatColor.YELLOW + "AIR" + ChatColor.WHITE +  ": ONLINE - " + numberOnline[3]
    	                   + ". INVASION " + isAllowed(numberOnline[3]));

    	return true;
    }
    
    public void setOnlineCount(Server server)
    {

    	/*
    	 * 0 - Fire
    	 * 1 - Water
    	 * 2 - Earth
    	 * 3 - Air
    	 */
    	for(int i=0; i<4; i++)
    	{
    		numberOnline[i] = 0;
    	}
    	Player[] players = server.getOnlinePlayers();
    	OverloadedWorldHolder world = plugin.wd.getDefaultWorld();

    	for(Player player:players)
    	{
    		String group = world.getUser(player.getName()).getGroupName();	
    		int index = nations.indexOf(group);
    		if(index != -1)
    		{
    			numberOnline[index] ++;
    		}
    	}
    }
    public String isAllowed(int amount)
    {
    	if(amount >= plugin.invadeMin)
    	{
    		return ChatColor.GREEN + "ALLOWED";
    	}else return ChatColor.DARK_RED + "DISALLOWED";
    }
    
    public String getBoundaryName(int nationIndex)
    {
    	return nations.get(nationIndex).toLowerCase() + "boundary";
    }
    
    public String upperFirst(String name)
    {
    	return Character.toString(name.charAt(0)).toUpperCase() + name.substring(1);
    }
    
    public void slayIntruders(int invader, int defender)
    {
    	
    	Player[] players = plugin.getServer().getOnlinePlayers();
    	OverloadedWorldHolder world = plugin.wd.getDefaultWorld();

    	for(Player player:players)
    	{
    		String group = world.getUser(player.getName()).getGroupName();	
        	int index = nations.indexOf(group);
        	if(index == invader)
        	{
        		if(isContainedIn(player.getLocation(), defender))
        		{
        			player.setHealth(0);
        		}
        	}
        }
    }
    
    public boolean isContainedIn(Location player, int nationIndex)
    {
    	if(player.getX() >= nationLocs[nationIndex].x1 && player.getX() <= nationLocs[nationIndex].x2)
    	{
    		if(player.getZ() >= nationLocs[nationIndex].y1 && player.getZ() <= nationLocs[nationIndex].y2)
        	{
    			return true;
        	}
    	}
    	return false;
		
    }
    
    public void sendNationMessage(int nationIndex, String message)
    {
    	Player[] players = plugin.getServer().getOnlinePlayers();
    	OverloadedWorldHolder world = plugin.wd.getDefaultWorld();

    	for(Player player:players)
    	{
    		String group = world.getUser(player.getName()).getGroupName();	
        	int index = nations.indexOf(group);
        	if(index == nationIndex || group == "Admins")
        	{
        		player.sendMessage(message);
        	}
    	}
    }
    class InvadeConstructTimerTask extends TimerTask{
    	int invaderIndex;
    	int defenderIndex;
    	boolean beginning;
    	
    	public InvadeConstructTimerTask(int invader, int defender, boolean beg)
    	{
    		invaderIndex = invader;
    		defenderIndex = defender;
    		beginning = beg;
    	}
    	
    	public void run()
    	{
        	Group invaderGroup = plugin.wd.getDefaultWorld().getGroup(nations.get(invaderIndex));
        	if(beginning)
        	{
        		invaderGroup.removePermission("epiczones." + getBoundaryName(defenderIndex)+ ".entry.deny");
        		plugin.getServer().broadcastMessage(ALERT_PREFIX + nationColors[invaderIndex] +
        				nations.get(invaderIndex) + "'s" + ChatColor.WHITE + " Invasion of " +
        				nationColors[defenderIndex] + nations.get(defenderIndex) + ChatColor.WHITE + 
        				" has begun!");
        		Timer fiveMinTimer = new Timer();
        		
        		fiveMinTimer.schedule(new fiveMinWarningTimerTask(invaderIndex,defenderIndex), TWENTY_FIVE_MIN);
        	}else
        	{
        		invaderGroup.addPermission("epiczones." + getBoundaryName(defenderIndex)+ ".entry.deny");
        		plugin.getServer().broadcastMessage(ALERT_PREFIX + nationColors[invaderIndex] +
        				nations.get(invaderIndex) + "'s" + ChatColor.WHITE + " Invasion of " +
        				nationColors[defenderIndex] + nations.get(defenderIndex) + ChatColor.WHITE + 
        				" has ended!");
        		//setup cooldown
        		Timer cooldown = new Timer();
        		cooldown.schedule(new InvadeCooldownTimerTask(invaderIndex, defenderIndex), TWENTY_FIVE_MIN + FIVE_MIN);
        		
        		//slay invaders in the zone
        		slayIntruders(invaderIndex, defenderIndex);
        		
        	}
    	}
    }
    class InvadeCooldownTimerTask extends TimerTask{
    	int invaderIndex;
    	int defenderIndex;
    	
    	public InvadeCooldownTimerTask(int invader, int defender)
    	{
    		invaderIndex = invader;
    	}
    	
    	public void run()
    	{
    		canInvade[invaderIndex][defenderIndex] = true;
    		sendNationMessage(invaderIndex, nationColors[invaderIndex] + nations.get(invaderIndex)
    				+ ChatColor.WHITE + " can now invade " + nationColors[defenderIndex] + nations.get(defenderIndex));
    	}
    }
    class fiveMinWarningTimerTask extends TimerTask{
    	int invaderIndex;
    	int defenderIndex;
    	
    	public fiveMinWarningTimerTask(int invader, int defender)
    	{
    		invaderIndex = invader;
    		defenderIndex = defender;
    	}
    	
    	public void run()
    	{
    		plugin.getServer().broadcastMessage(ALERT_PREFIX + nationColors[invaderIndex] +
    				nations.get(invaderIndex) + "'s" + ChatColor.WHITE + " Invasion of " +
    				nationColors[defenderIndex] + nations.get(defenderIndex) + ChatColor.WHITE + 
    				" has 5 minutes left!");
    		Timer invadeStopTimer = new Timer();
 
    		invadeStopTimer.schedule(new InvadeConstructTimerTask(invaderIndex,defenderIndex,false), FIVE_MIN);
    	}
    }
    class InvadeSetupTimerTask extends TimerTask{

    	int invaderIndex;
    	int defenderIndex;
		public InvadeSetupTimerTask(int invader, int defender)
		{
			invaderIndex = invader;
			defenderIndex = defender;

		}
		public void run() {
			
			setOnlineCount(plugin.getServer());		
			if(plugin.users.get(invaderIndex).get(defenderIndex).size() >=(numberOnline[invaderIndex] / 2))
			{
				//invasion will begin
				canInvade[invaderIndex][defenderIndex] = false;
				attemptingInvasion[invaderIndex] = false;
				plugin.getServer().broadcastMessage(ALERT_PREFIX + nationColors[invaderIndex] + 
						nations.get(invaderIndex) + "\'s" + ChatColor.WHITE + " invasion attempt of " +
						nationColors[defenderIndex] + nations.get(defenderIndex) + ChatColor.WHITE + " has passed!");
				plugin.getServer().broadcastMessage(ALERT_PREFIX + nationColors[defenderIndex] +
						nations.get(defenderIndex) + ChatColor.WHITE + " has 5 minutes to prepare defenses");
				Timer startInvasion = new Timer();
				
				startInvasion.schedule(new InvadeConstructTimerTask(invaderIndex, defenderIndex, true), FIVE_MIN);
			}else{
				attemptingInvasion[invaderIndex] = false;
				
				sendNationMessage(invaderIndex, ALERT_PREFIX + nationColors[invaderIndex] + 
						nations.get(invaderIndex) + "\'s" + ChatColor.WHITE + " invasion attempt of " +
						nationColors[defenderIndex] + nations.get(defenderIndex) + ChatColor.WHITE + " has failed");
			}
		}
		
	}
}
