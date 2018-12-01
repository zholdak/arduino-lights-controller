package com.zholdak.lights.classes;

public class TableColumn {

	private int index;
	private String title;
	private Class<?> clazz;

	public TableColumn(int index, String title, Class<?> clazz) {
		this.index = index;
		this.title = title;
		this.clazz = clazz;
	}
	
	public TableColumn(String title, Class<?> clazz) {
		this.title = title;
		this.clazz = clazz;
	}
	
	public int getIndex() {
		return index;
	}
	public String getTitle() {
		return title;
	}
	public Class<?> getClazz() {
		return clazz;
	}
}
