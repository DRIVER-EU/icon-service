package eu.driver.icon.controller.util;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public class IconMap {

	private static IconMap aMe;
	
	private HashMap<String, BufferedImage> iconMap = new HashMap<String, BufferedImage>();
	
	private IconMap() {
		
	}
	
	public static IconMap getInstance() {
		if (IconMap.aMe == null) {
			IconMap.aMe = new IconMap();
		}
		return IconMap.aMe;
		 
	}
	
	public void addIcon(String key, BufferedImage icon) {
		iconMap.put(key, icon);
	}
	
	public BufferedImage getIcon(String key) {
		return iconMap.get(key);
	}
	
	public void clearMap() {
		iconMap.clear();
	}

}
