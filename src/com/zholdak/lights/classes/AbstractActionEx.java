package com.zholdak.lights.classes;

import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.jdesktop.swingx.action.AbstractActionExt;

import com.zholdak.lights.Application;

public abstract class AbstractActionEx extends AbstractActionExt {

	private static final long serialVersionUID = 1L;
	
	public AbstractActionEx() {
		super();
	}

	public AbstractActionEx(String title, String hint) {
		super();
		super.setName(title);
		super.setShortDescription(hint);
	}

	public AbstractActionEx(String title, String hint, String smallIcon, String largeIcon) {
		super();
		super.setName(title);
		super.setShortDescription(hint);
		super.setSmallIcon(new ImageIcon(Application.class.getResource(String.format("%s%s.png", Application.ICONS_PATH, smallIcon))));
		super.setLargeIcon(new ImageIcon(Application.class.getResource(String.format("%s%s.png", Application.ICONS_PATH, largeIcon))));
	}

	public AbstractActionEx(String title, String hint, String smallIcon, String largeIcon, KeyStroke acceleratorKeyStroke) {
		super();
		super.setName(title);
		super.setShortDescription(hint);
		super.setSmallIcon(new ImageIcon(Application.class.getResource(String.format("%s%s.png", Application.ICONS_PATH, smallIcon))));
		super.setLargeIcon(new ImageIcon(Application.class.getResource(String.format("%s%s.png", Application.ICONS_PATH, largeIcon))));
		super.setAccelerator(acceleratorKeyStroke);
	}
	
	public abstract void onActionPerformed(ActionEvent e);
	
	@Override
	public void actionPerformed(ActionEvent e) {
		onActionPerformed(e);
	}
}
