package com.zholdak.lights.classes;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.SortOrder;
import javax.swing.table.AbstractTableModel;

public abstract class AbstractTableModelEx<T> extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	protected List<T> objList = null;
	
	protected Map<Integer,TableColumn> columnsMap = new TreeMap<Integer,TableColumn>();

	@Override
	public int getRowCount() {
		return objList == null ? 0 : objList.size();
	}

	@Override
	public int getColumnCount() {
		return columnsMap.size();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
        return columnsMap.get(columnIndex).getClazz();
	}
	
	@Override
	public String getColumnName(int columnIndex) {
        return columnsMap.get(columnIndex).getTitle();
	}
	
	@Override
	public abstract Object getValueAt(int rowIndex, int columnIndex);
	
	protected void putCol(Enum<?> col, String title, Class<?> clazz) {
		putCol(col.ordinal(), title, clazz);
	}
	
	protected void putCol(int col, String title, Class<?> clazz) {
		columnsMap.put(col, new TableColumn(title, clazz));
	}
	
	protected boolean cmp(Enum<?> col, int idx) {
		return col.ordinal() == idx;
	}
	
	public T get(int rowIndex) {
		return objList == null ? null : objList.get(rowIndex);
	}

	public int getRowIndex(T obj) {
		return objList.indexOf(obj);
	}
	
	/**
	 * Получить экземпляр Enum-класса по его индексу 
	 */
	public static <E extends Enum<E>> Enum<E> getColByIndex(Class<E> enumClass, int index) {
		for (Enum<E> enumVal: enumClass.getEnumConstants()) 
			if( enumVal.ordinal() == index )
				return enumVal;
		return null;
	}
	
	public abstract void loadData() throws Exception;

	public abstract void refreshData() throws Exception;
	
	public abstract Enum<?>[] getColsInitiallyHidden();

	public abstract Enum<?> getDefaultSortCol();

	public abstract SortOrder getDefaultSortOrder();
}

