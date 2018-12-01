package com.zholdak.lights.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JButton;

import org.jdesktop.swingx.JXButton;

import com.zholdak.lights.classes.AbstractActionEx;

public class JXFlatColorButton extends JXButton {

	private static final long serialVersionUID = 1L;
	
	private Color color;
	
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
				button.setContentAreaFilled(false);
			}
		}
	};
	
	public JXFlatColorButton(AbstractActionEx action, Color initialColor) {
		this.color = initialColor;
		addMouseListener(flatButtonMouseAdapter);
		setContentAreaFilled(false);
		setHideActionText(true);
		setMargin(new Insets(3,3,3,3));
		//setFocusable(false);
		setAction(action);
		setHorizontalAlignment(JButton.RIGHT);
		setFocusPainted(false);
	}

	public void setColor(Color color) {
		this.color = color;
		repaint();
	}
	
	public Color getColor() {
		return color;
	}

	@Override
	public void setAction(Action action) {
		super.setAction(action);
		if( action instanceof AbstractActionEx )
			setIcon(((AbstractActionEx)action).getSmallIcon());
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(color);
		g.fillRect(5, 5, getWidth() - getInsets().left - getInsets().right - getIconTextGap() - ((AbstractActionEx)getAction()).getSmallIcon().getIconWidth(), getHeight() - 10);
	}
}
