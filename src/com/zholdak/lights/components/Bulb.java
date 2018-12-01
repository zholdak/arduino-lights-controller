package com.zholdak.lights.components;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;

import com.zholdak.lights.classes.Serial;

public class Bulb {

	public static final int MAX_LEVEL = 7; 

	private Color color;
	private int n, x, y, diameter; 
	private Rectangle rect;
	private boolean locked = false;
	
	public Bulb(int n, Color color) {
		this.n = n;
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = new Color(color.getRGB());
	}
	
	public void setN(int n) {
		this.n = n;
	}

	public int getN() {
		return n;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getDiameter() {
		return diameter;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public boolean isHere(Point p) {
		return rect.contains(p);
	}
	
	public Bulb set(int x, int y, int diameter) {
		this.x = x;
		this.y = y;
		this.diameter = diameter;
		this.rect = new Rectangle(x, y, diameter, diameter);
		return this;
	}

//	public byte[] asLevelBytes() {
//		return Bulb.asLevelBytes(this);
//	}
	
//	public static byte[] asLevelBytes(Bulb b) {
//		int s = ((b.getN() & 0x7f) << 9) | ((b.getRedLevel() & MAX_LEVEL) << 6) | ((b.getGreenLevel() & MAX_LEVEL) << 3) | (b.getBlueLevel() & MAX_LEVEL);
//		return new byte[] { (byte)((s >> 8) & 0xff), (byte)(s & 0xff) };
//	}

	public byte[] asBytes() {
		return asBytes(this);
	}
	
	public static byte[] asBytes(Bulb b) {
		if( b.getN() == Serial.DATA_TERMINATOR )
			throw new IllegalArgumentException(String.format("bulb number cann't to equals %02X", Serial.DATA_TERMINATOR));
		return new byte[] {
				(byte)b.getN(),
				(byte)(b.getColor().getRed() == Serial.DATA_TERMINATOR ? b.getColor().getRed() + 1 : b.getColor().getRed()),
				(byte)(b.getColor().getGreen() == Serial.DATA_TERMINATOR ? b.getColor().getGreen() + 1 : b.getColor().getGreen()),
				(byte)(b.getColor().getBlue() == Serial.DATA_TERMINATOR ? b.getColor().getBlue() + 1 : b.getColor().getBlue())
			};
	}

	@Override
	public String toString() {
		return "Bulb [color=" + color + ", n=" + n + "]";
	}
}
