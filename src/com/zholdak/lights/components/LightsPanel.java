package com.zholdak.lights.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.ToolTipManager;

import com.zholdak.lights.classes.Bulb;
import com.zholdak.lights.classes.IOnBulb;

public class LightsPanel extends JPanel implements MouseListener, MouseMotionListener {
	
	private static final long serialVersionUID = 1L;

	private static final int MIN_BULB_SPACE_SIZE = 2;
	
	private Bulb selectedBulb = null;
	private Bulb[] bulbs = new Bulb[0];
	private int countInGroup = 1;
	private Color initColor = Color.BLACK;
	private boolean skipPlacesOnUpdate = false;
	
	private JToolTip bulbToolTip = new JToolTip();
	
	private int savedToolTipInitialDelay = 0;
	
	private IOnBulb onBulb = null;
	
	public LightsPanel(Color initColor, int bulbsCount, int countInGroup) {

		setBackground(Color.BLACK);
		this.initColor = initColor;
		setCountInGroup(countInGroup);
		
		bulbs = new Bulb[bulbsCount];
		for(int i=0; i<bulbsCount; i++)
			bulbs[i] = new Bulb(i, initColor);
		
		addMouseListener(this);
		addMouseMotionListener(this);
		
		bulbToolTip.setComponent(this);
	}

	public Color getInitColor() {
		return initColor;
	}

	public void setCountInGroup(int countInGroup) {
		this.countInGroup = countInGroup;
	}

	public int getCountInGroup() {
		return countInGroup;
	}
	
	public Color[] getColors() {
		Color[] colors = new Color[bulbs.length];
		for(int i=0; i<bulbs.length; i++)
			colors[i] = new Color( bulbs[i].getColor().getRGB() );
		return colors;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		int cols = (int)Math.ceil( (float)bulbs.length / (float)countInGroup );
		
		int bulbDiameter = (getWidth() - (MIN_BULB_SPACE_SIZE + cols * MIN_BULB_SPACE_SIZE)) / cols;
		
		int groupShiftY = bulbDiameter/2;
		
		setSize(getWidth(), MIN_BULB_SPACE_SIZE*2 + bulbDiameter + ( (countInGroup - 1) * groupShiftY));
		setPreferredSize(getSize());

		int restOfSpace = (getWidth() - (bulbDiameter * cols + cols * MIN_BULB_SPACE_SIZE + MIN_BULB_SPACE_SIZE));
		
		int pos = MIN_BULB_SPACE_SIZE;
		for(int col=0; col<cols; col++) {
			for(int row=countInGroup-1; row>=0; row--) {
				int i = col*countInGroup + row;
				if( i < bulbs.length )
					drawBulb(g2d, bulbs[i].set(pos, MIN_BULB_SPACE_SIZE + groupShiftY*row, bulbDiameter));
			}
			pos += bulbDiameter + MIN_BULB_SPACE_SIZE + (restOfSpace-->0?1:0);
		}
	}
	
	private void drawBulb(Graphics2D g2d, Bulb b) {
		g2d.setColor(b.getColor());
		g2d.fillOval(b.getX(), b.getY(), b.getDiameter(), b.getDiameter());
		if( !skipPlacesOnUpdate ) {
			g2d.setColor( b.getColor().equals(getBackground()) ? Color.WHITE : getBackground() );
			g2d.drawOval(b.getX(), b.getY(), b.getDiameter(), b.getDiameter());
		}
	}

	public Bulb[] getBulbs() {
		return bulbs;
	}
	
	public int getBulbsCount() {
		return bulbs.length;
	}

	public Bulb getBulb(int i) {
		return bulbs[i];
	}
	
	private Bulb getBulb(Point p) {
		for(int i=0; i<bulbs.length; i++)
			if( bulbs[i].isHere(p) )
				return bulbs[i];
		return null;
	}
	
	public void setColors(Color[] colors) {
		if( bulbs.length != colors.length )
			throw new IllegalArgumentException("bulbs.length and colors.length must be the same");
		for(int i=0; i<bulbs.length; i++)
			bulbs[i].setColor(colors[i]);
		update();
	}

	public void update() {
		validate();
		repaint();
	}
	
	public void turnOffAll() {
		for(int i=0; i<bulbs.length; i++) {
			bulbs[i].setColor(Color.BLACK);
			bulbs[i].setLocked(false);
		}
		update();
	}

	public void shiftLeft() {
		for(int g=0; g<countInGroup; g++) {
			Bulb first = bulbs[0];
			for(int i=1; i<bulbs.length; i++)
				bulbs[i-1] = bulbs[i];
			bulbs[bulbs.length-1] = first;
		}
		reorderBulbs();
		update();
	}

	public void reorderBulbs() {
		for(int i=0; i<bulbs.length; i++)
			bulbs[i].setN(i);
		update();
	}
	
	public void shiftRight() {
		for(int g=0; g<countInGroup; g++) {
			Bulb last = bulbs[bulbs.length-1];
			for(int i=bulbs.length-2; i>=0; i--)
				bulbs[i+1] = bulbs[i];
			bulbs[0] = last;
		}
		reorderBulbs();
		update();
	}
	
	public void startAnimationMode() {
		skipPlacesOnUpdate = true;
	}

	public void stopAnimationMode() {
		skipPlacesOnUpdate = false;
	}
	
	public void setOnBulb(IOnBulb onBulb) {
		this.onBulb = onBulb;
	}
	
	@Override
	public void mouseClicked(MouseEvent me) {
	}

	@Override
	public void mousePressed(MouseEvent me) {
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		selectedBulb = getBulb(me.getPoint());
		if( me.isPopupTrigger() && onBulb != null ) {
			if( onBulb != null ) {
				ToolTipManager.sharedInstance().setInitialDelay(savedToolTipInitialDelay);
//				Action toolTipAction = LightsPanel.this.getActionMap().get("hideTip");
//				if( toolTipAction != null ) {
//					ActionEvent ae = new ActionEvent(LightsPanel.this, ActionEvent.ACTION_PERFORMED, "");
//				    toolTipAction.actionPerformed( ae );
//				}
//				//setToolTipText("");
				onBulb.popupTriggered(me, selectedBulb);
			}
		} else {
			if( selectedBulb != null ) {
				if( onBulb != null )
					onBulb.bulbClicked(selectedBulb);
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent me) {
		savedToolTipInitialDelay = ToolTipManager.sharedInstance().getInitialDelay();
		ToolTipManager.sharedInstance().setInitialDelay(0);
	}

	@Override
	public void mouseExited(MouseEvent me) {
		ToolTipManager.sharedInstance().setInitialDelay(savedToolTipInitialDelay);
	}

	@Override
	public void mouseDragged(MouseEvent me) {
	}

	@Override
	public void mouseMoved(MouseEvent me) {
		Bulb b = getBulb(me.getPoint());
		if( b != null ) {
			setToolTipText(String.format("<html>[<b>%d</b>] r:<b>0x%02X</b>(%d) g:<b>0x%02X</b>(%d) b:<b>0x%02X</b>(%d)</html>", 
					b.getN(),
					b.getColor().getRed(), b.getColor().getRed(),
					b.getColor().getGreen(), b.getColor().getGreen(),
					b.getColor().getBlue(), b.getColor().getBlue()
					));
		} else {
			setToolTipText("");
		}
	}
}
