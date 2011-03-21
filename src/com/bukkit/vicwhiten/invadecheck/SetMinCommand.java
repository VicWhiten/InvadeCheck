package com.bukkit.vicwhiten.invadecheck;





import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetMinCommand implements CommandExecutor {
    private final InvadeCheck plugin;

    public SetMinCommand(InvadeCheck plugin) {
        this.plugin = plugin;
        System.out.println("Setting up /invademin Command");
    }

    public boolean onCommand(CommandSender sender, 
    		Command command, 
    		String label, String[] args) {
    	if(args.length != 1)
    	{
    		return false;
    	}
    	if(!canChangeGroup((Player)sender))
    	{
    		sender.sendMessage("You do not have the required permissions.");
    		return true;
    	}
    	try{
    	plugin.invadeMin = Integer.parseInt(args[0]);
    	sender.sendMessage("Invasion Minimum set to " + plugin.invadeMin);
    	}catch (Exception e)
    	{
    		return false;
    	}
    	
    	
    	return true;	
    }
    
    public boolean canChangeGroup(Player player){
        return plugin.gm.getWorldsHolder().getWorldPermissions(player).has(player,"invadecheck.setmin");
}
    
}
    