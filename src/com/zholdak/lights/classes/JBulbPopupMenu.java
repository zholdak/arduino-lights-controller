package com.zholdak.lights.classes;

import javax.swing.JPopupMenu;

public class JBulbPopupMenu extends JPopupMenu {

	private static final long serialVersionUID = 1L;
	
	private Bulb bulb;
	
	public JBulbPopupMenu() {
		super();
	}
	
	public Bulb getBulb() {
		return bulb;
	}
	
	public void setBulb(Bulb bulb) {
		this.bulb = bulb;
	}
}
