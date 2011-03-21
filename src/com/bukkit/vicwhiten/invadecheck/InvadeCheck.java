//The Package
package com.bukkit.vicwhiten.invadecheck;
//All the imports
import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.dataholder.worlds.WorldsHolder;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.epicsagaonline.bukkit.EpicZones.EpicZones;




public class InvadeCheck extends JavaPlugin{
	//constants for easy access
    public int invadeMin = 8;
    public GroupManager gm;
    public WorldsHolder wd;
    public EpicZones ez;
	

	
    //called when plugin is disabled
	public void onDisable() {
		System.out.println("Invade Check Disabled");
	
	}

	//called when plugin starts
	public void onEnable() {
		//check if Group Manager is setup
		System.out.println("InvadeCheck Plugin Enabled!");
		 Plugin p = this.getServer().getPluginManager().getPlugin("GroupManager");
	        if (p != null) {
	            if (!this.getServer().getPluginManager().isPluginEnabled(p)) {
	                this.getServer().getPluginManager().enablePlugin(p);
	            }
	            gm = (GroupManager) p;
	            wd = gm.getWorldsHolder();
	        } else {
	            this.getPluginLoader().disablePlugin(this);
	        }
	        
			//check if Group Manager is setup
			System.out.println("InvadeCheck Plugin Enabled!");
			 Plugin p2 = this.getServer().getPluginManager().getPlugin("EpicZones");
		        if (p2 != null) {
		            if (!this.getServer().getPluginManager().isPluginEnabled(p2)) {
		                this.getServer().getPluginManager().enablePlugin(p2);
		            }
		            ez = (EpicZones) p2;
		            
		        } else {
		            this.getPluginLoader().disablePlugin(this);
		        }
	        
	        
	    //setup the commands
		getCommand("invade").setExecutor(new InvadeCommand(this));
		getCommand("invademin").setExecutor(new SetMinCommand(this));
	}

	   
	   

}
