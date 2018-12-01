//package com.zholdak.lights.classes;
//
//import java.awt.Color;
//
//public class Light {
//
//	public static final int MAX_COLOR_VALUE = 7; 
//	
//	private int n, r, g, b;
//
//	public Light(int n) {
//		this(n, 0, 0, 0);
//	}
//	
//	public Light(int n, int r, int g, int b) {
//		if( n > 0x7f )
//			throw new IllegalArgumentException("n cann't be grater than 127 (0x7f");
//		this.n = n;
//		if( r > MAX_COLOR_VALUE )
//			throw new IllegalArgumentException("r cann't be grater than " + MAX_COLOR_VALUE);
//		this.r = r;
//		if( b > MAX_COLOR_VALUE )
//			throw new IllegalArgumentException("b cann't be grater than " + MAX_COLOR_VALUE);
//		this.g = g;
//		if( g > MAX_COLOR_VALUE )
//			throw new IllegalArgumentException("g cann't be grater than " + MAX_COLOR_VALUE);
//		this.b = b;
//	}
//
//	public static Light fromBytes(byte[] bytes) {
//		if( bytes.length != 2 )
//			throw new IllegalArgumentException("bytes.length must be 2");
//		int d = bytes[0] << 8 | bytes[1];
//		return new Light(
//				d >> 9 & 0x7f,
//				d >> 6 & MAX_COLOR_VALUE,
//				d >> 3 & MAX_COLOR_VALUE,
//				d & MAX_COLOR_VALUE
//				);
//	}
//
//	public int getN() {
//		return n;
//	}
//
//	public Light setColor(Color color) {
//		this.r = getLevel(color.getRed());
//		this.g = getLevel(color.getGreen());
//		this.b = getLevel(color.getBlue());
//		return this;
//	}
//	
//	public Light setR(int r) {
//		this.r = r;
//		return this;
//	}
//
//	public int getR() {
//		return r;
//	}
//
//	public int getR255() {
//		return Light.get255(r);
//	}
//	
//	public Light setG(int g) {
//		this.g = g;
//		return this;
//	}
//
//	public int getG() {
//		return g;
//	}
//
//	public int getG255() {
//		return Light.get255(g);
//	}
//	
//	public Light setB(int b) {
//		this.b = b;
//		return this;
//	}
//
//	public int getB() {
//		return b;
//	}
//
//	public int getB255() {
//		return Light.get255(b);
//	}
//	
//	public Color getColor() {
//		return new Color(Light.get255(r), Light.get255(g), Light.get255(b));
//	}
//	
//	public byte[] asBytes() {
//		return Light.asBytes(this);
//	}
//	
//	public static byte[] asBytes(Light l) {
//		int s = ((l.getN() & 0x7f) << 9) | ((l.getR() & MAX_COLOR_VALUE) << 6) | ((l.getG() & MAX_COLOR_VALUE) << 3) | (l.getB() & MAX_COLOR_VALUE);
//		return new byte[] { (byte)((s >> 8) & 0xff), (byte)(s & 0xff) };
//	}
//
//	public static int get255(int l) {
//		return 255 * l / MAX_COLOR_VALUE;
//	}
//	
//	public static int getLevel(int c) {
//		return c * MAX_COLOR_VALUE / 255;
//	}
//	
//	@Override
//	public String toString() {
//		return "Light [n=" + n + ", r=" + r + ", g=" + g + ", b=" + b + "]";
//	}
//}
