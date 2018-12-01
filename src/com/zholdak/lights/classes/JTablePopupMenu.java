package com.zholdak.lights.classes;

import javax.swing.JPopupMenu;

public class JTablePopupMenu extends JPopupMenu {

	private static final long serialVersionUID = 1L;
	
	private int colorIndex = -1, row = -1, col = -1;
	
	public JTablePopupMenu() {
		super();
	}

	public int getColorIndex() {
		return colorIndex;
	}

	public void setColorIndex(int colorIndex) {
		this.colorIndex = colorIndex;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}
}
