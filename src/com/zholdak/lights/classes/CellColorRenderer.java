package com.zholdak.lights.classes;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class CellColorRenderer extends JLabel implements ListCellRenderer<Color> {
	
	private static final long serialVersionUID = 1L;

	boolean b = false;
	
	public CellColorRenderer() {
		setOpaque(true);
	}

	@Override
	public void setBackground(Color bg) {
		if( !b )
			return;
		super.setBackground(bg);
	}
	
	@Override
	public Component getListCellRendererComponent(JList<? extends Color> list, Color value, int index, boolean isSelected, boolean cellHasFocus) {
		b = true;
		setText( ColorUtils.getColorNameFromColor(value) );
		setBackground((Color) value);
		//setForeground((Color) value);
		b = false;
		return this;
	}

}