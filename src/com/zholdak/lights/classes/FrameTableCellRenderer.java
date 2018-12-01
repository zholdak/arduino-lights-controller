package com.zholdak.lights.classes;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class FrameTableCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;

	private Frame frame = null;
	
	@Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);
		frame = (Frame)value;
		setMinimumSize(new Dimension(frame.getColors().length*4, getHeight()));
		return this;
	}	
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		int colorWidth = (getWidth() / frame.getColors().length) - 1;
		int restWight = getWidth() - (colorWidth * frame.getColors().length + (frame.getColors().length-1));
		
		int pos = 0;
		for(Color color: frame.getColors()) {
			int w = colorWidth + (restWight-->0 ? 1 : 0);
			g2d.setColor(color);
			g2d.fillRect(pos, 0, w, getHeight());
			
			pos += w + 1;
		}
	}
	
	public int getColorIndex(JTable table, Point p) {
    	int row = table.rowAtPoint(p);
    	int col = table.columnAtPoint(p);
		Rectangle r = table.getCellRect(row, col, false);
		Frame f = ((ProgramTableModel)table.getModel()).get(table.convertRowIndexToModel(row));
		int colorWidth = (r.width / f.getColors().length) - 1;
		int restWight = r.width - (colorWidth * f.getColors().length + (f.getColors().length-1));
		int pos = 0;
		for(int i=0; i<f.getColors().length; i++) {
			int w = colorWidth + (restWight-->0 ? 1 : 0);
			if( new Rectangle(pos, 0, w, 1).contains(p.x - r.x, 0) )
				return i;
			pos += w + 1;
		}
		return -1;
	}
}
