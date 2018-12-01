package com.zholdak.lights.classes;

import javax.swing.SortOrder;

public final class ProgramTableModel extends AbstractTableModelEx<Frame> {

	private static final long serialVersionUID = 1L;

	public static enum Col {
		N, BULBS_COUNT, DELAY_AFTER, FRAME;
	}
	
	private Program prog;
	
	public ProgramTableModel() {
		putCol(Col.N, "№", Integer.class);
		putCol(Col.BULBS_COUNT, "Ламочек", Integer.class);
		putCol(Col.DELAY_AFTER, "Задержка", Integer.class);
		putCol(Col.FRAME, "Лампочки", Frame.class);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Frame frame = prog.getFrame(rowIndex);
		if( cmp(Col.N, columnIndex) )
			return rowIndex;
		else if( cmp(Col.BULBS_COUNT, columnIndex) )
			return rowIndex == 0 ? frame.getColors().length : frame.getDiffCount();
		else if( cmp(Col.DELAY_AFTER, columnIndex) )
			return frame.getDelayAfter();
		else if( cmp(Col.FRAME, columnIndex) )
			return frame;
		else
			return null;
	}

	@Override
	public void loadData() throws Exception {
		objList = prog.getFrames();
	}

	@Override
	public void refreshData() throws Exception {
	}

	public void setProgram(Program prog) throws Exception {
		this.prog = prog;
		loadData();
	}
	
	@Override
	public Enum<?>[] getColsInitiallyHidden() {
		return null;
	}

	@Override
	public Enum<?> getDefaultSortCol() {
		return null;
	}

	@Override
	public SortOrder getDefaultSortOrder() {
		return null;
	}
	
}
