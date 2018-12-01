package com.zholdak.lights.classes;

import java.awt.Color;

import org.json.JSONArray;
import org.json.JSONObject;

public final class Frame {

	private static final String DELAY_AFTER_KEY = "d";
	private static final String COLORS_KEY = "c";
	private static final String COLOR_R_KEY = "r";
	private static final String COLOR_G_KEY = "g";
	private static final String COLOR_B_KEY = "b";
	
	public static final long DEFAULT_DELAY = 500;
	
	private Color[] colors;
	private long delayAfter = 0; // millis
	private int diffCount;
	
	public Frame(Color[] colors, long delayAfter) {
		this.colors = new Color[colors.length];
		for(int i=0; i<colors.length; i++)
			this.colors[i] = new Color( colors[i].getRGB() );
		this.diffCount = colors.length;
		this.delayAfter = delayAfter;
	}

	public Frame(JSONObject json) {
		this.delayAfter = json.getLong(DELAY_AFTER_KEY);
		JSONArray colorArray = json.getJSONArray(COLORS_KEY);
		this.colors = new Color[colorArray.length()];
		for(int i=0; i<this.colors.length; i++) {
			JSONObject colorObj = (JSONObject)colorArray.get(i);
			this.colors[i] = new Color(
					colorObj.getInt(COLOR_R_KEY),
					colorObj.getInt(COLOR_G_KEY),
					colorObj.getInt(COLOR_B_KEY)
					);
		}
	}
	
	public Color[] getColors() {
		return colors;
	}

	public void setColors(Color[] colors) {
		this.colors = new Color[colors.length];
		for(int i=0; i<colors.length; i++)
			this.colors[i] = new Color( colors[i].getRGB() );
		this.diffCount = colors.length;
	}

	public int getDiffCount() {
		return diffCount;
	}

	public void setDiffCount(int diffCount) {
		this.diffCount = diffCount;
	}

	public long getDelayAfter() {
		return delayAfter;
	}

	public void setDelayAfter(long delayAfter) {
		this.delayAfter = delayAfter;
	}

	public JSONObject asJSONObj() {
		
		JSONObject json = new JSONObject();
		json.put(DELAY_AFTER_KEY, (long)delayAfter);
		
		JSONArray colorsArray = new JSONArray();
		for(int i=0; i<colors.length; i++) {
			JSONObject colorObj = new JSONObject();
			colorObj.put(COLOR_R_KEY, colors[i].getRed());
			colorObj.put(COLOR_G_KEY, colors[i].getGreen());
			colorObj.put(COLOR_B_KEY, colors[i].getBlue());
			colorsArray.put(colorObj);
		}
		json.put(COLORS_KEY, colorsArray);
		
		return json;
	}
	
}
