package com.github.intangir.RealmTracker;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.cubespace.Yamler.Config.Config;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.plugin.Plugin;

public class UUIDCache extends Config 
{
	public UUIDCache() {}
	public UUIDCache(Plugin plugin) {
		this.log = plugin.getProxy().getLogger();
		
		CONFIG_FILE = new File(plugin.getDataFolder(), "uuidcache.yml");
		
		uuids = new HashMap<String, String>();
	}

	transient Logger log;
	
	private Map<String, String> uuids;
	
	@Override
	public void init() {
		try {
			super.init();
		} catch (InvalidConfigurationException e) {
			log.warning("Couldn't Load " + CONFIG_FILE);
			e.printStackTrace();
		}
	}

	@Override
	public void save() {
		try {
			super.save();
		} catch (InvalidConfigurationException e) {
			log.warning("Couldn't Save " + CONFIG_FILE);
			e.printStackTrace();
		}
	}

	public boolean contains(String name) {
		return uuids.containsKey(name);
	}
	
	public void put(String name, String uuid) {
		if(!contains(name)) {
			uuids.put(name, uuid);
			save();
		}
	}
	
	public String get(String name) {
		return uuids.get(name);
	}
}
