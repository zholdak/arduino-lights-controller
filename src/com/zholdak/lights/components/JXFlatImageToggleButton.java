package com.zholdak.lights.components;

import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JToggleButton;

import com.zholdak.lights.classes.AbstractActionEx;

public class JXFlatImageToggleButton extends JToggleButton {

	private static final long serialVersionUID = 1L;
	
	private static MouseListener flatButtonMouseAdapter = new MouseAdapter() {

		public void mouseEntered(MouseEvent e) {
			Object source = e.getSource();
			if (source instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) source;
				button.setContentAreaFilled(true);
			}
		}
		
		public void mouseExited(MouseEvent e) {
			Object source = e.getSource();
			if (source instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) source;
				if( !button.isSelected() )
					button.setContentAreaFilled(false);
			}
		}
	};
	
	public JXFlatImageToggleButton(AbstractActionEx action) {
		addMouseListener(flatButtonMouseAdapter);
		setContentAreaFilled(false);
		setHideActionText(true);
		setMargin(new Insets(3,3,3,3));
		//setFocusable(false);
		setAction(action);
	}

	@Override
	public void setAction(Action action) {
		super.setAction(action);
		if( action instanceof AbstractActionEx )
			setIcon(((AbstractActionEx)action).getSmallIcon());
	}
}
