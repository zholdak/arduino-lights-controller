package com.zholdak.lights.classes;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class Program {

	private static final String FRAMES_KEY = "f";
	
	public static final int PROG_COLOR_BYTES = 4;
	
	private String name;
	private File file = null;
	private List<Frame> frames = new ArrayList<Frame>();
	private boolean hasUsavedData = true;
	
	public Program(int count) {
		Color[] colors = new Color[count];
		for(int i=0; i<count; i++)
			colors[i] = Color.BLACK;
		frames.add( new Frame(colors, 0) );
		this.name = "Unnamed programm";
		hasUsavedData = true;
	}

	public Program(File file) throws JSONException, IOException {
		
		FileInputStream fis = null;
		ZipInputStream zis = null;
		ByteArrayOutputStream baos = null;
		
		try {
			
			if( file.getName().endsWith("lcprog") ) {
				
				fis = new FileInputStream(file);
				
				baos = new ByteArrayOutputStream();
				int bytesRead;
				byte[] tempBuffer = new byte[8192];
				while( (bytesRead = fis.read(tempBuffer)) != -1 )
					baos.write(tempBuffer, 0, bytesRead);
				fis.close();
				
			} else {
				
				zis = new ZipInputStream( new FileInputStream(file) );
				zis.getNextEntry();
				
				baos = new ByteArrayOutputStream();
				int bytesRead;
				byte[] tempBuffer = new byte[8192];
				while( (bytesRead = zis.read(tempBuffer)) != -1 )
					baos.write(tempBuffer, 0, bytesRead);
				zis.closeEntry();
			}
			
			JSONObject json = new JSONObject(new String(baos.toByteArray()));
			JSONArray framesArray = json.getJSONArray(FRAMES_KEY);
			for(int i=0; i<framesArray.length(); i++)
				frames.add( new Frame((JSONObject)framesArray.get(i)) );
			this.file = file;
			this.name = file.getName().replaceAll("\\.lcprog(z?)$","");
			hasUsavedData = false;
			
		} finally {
			if( fis != null ) {
				fis.close();
			}
		}
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		hasUsavedData = true;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public List<Frame> getFrames() {
		return frames;
	}
	
	public void addFrame(Frame frame) {
		frames.add(frame);
		hasUsavedData = true;
	}

	public void insertFrame(int i, Frame frame) {
		frames.add(i, frame);
		hasUsavedData = true;
	}

	public void replaceFrame(int i, Frame frame) {
		frames.set(i, frame);
		hasUsavedData = true;
	}
	
	public Frame getLastFrame() {
		return frames.get(frames.size()-1);
	}
	
	public Frame getFrame(int i) {
		return frames.get(i);
	}
	
	public int getFramesCount() {
		return frames.size();
	}
	
	public void delFrames(int[] f) {
		List<Frame> toDel = new ArrayList<Frame>();
		for(int n: f)
			if( n > 0 )
				toDel.add( frames.get(n) );
		frames.removeAll(toDel);
		hasUsavedData = true;
	}
	
	public void append(Program prog) {
		if( prog.getFrame(0).getColors().length != getFrame(0).getColors().length )
			throw new IllegalArgumentException("Program that appends contains different colors count. Forget to adapt it?");
		for(Frame f: prog.getFrames())
			addFrame(f);
		hasUsavedData = true;
	}
	
	public boolean isHasUsavedData() {
		return hasUsavedData;
	}

	public JSONObject asJSONObj() {
		
		JSONObject json = new JSONObject();

		JSONArray array = new JSONArray();
		for(int i=0; i<frames.size(); i++)
			array.put(frames.get(i).asJSONObj());
		json.put(FRAMES_KEY, array);
		
		return json;
	}
	
	public void save(File file) throws IOException {
		if( file == null )
			throw new IllegalArgumentException("file nann't be null");
		
		ZipOutputStream zos = null;
		
		try {
			
			zos = new ZipOutputStream( new FileOutputStream(file) );
			zos.setLevel(9);
			zos.putNextEntry(new ZipEntry(file.getName()));
			
			zos.write( asJSONObj().toString().getBytes() );

			this.file = file;
			this.name = file.getName().replaceAll("\\.lcprog(z?)$","");

			hasUsavedData = false;
			
		} finally {
			if( zos != null )
				zos.close();
		}
	}
	
	public void saveAsBinary(File file) throws IOException {
		
		if( file == null )
			throw new IllegalArgumentException("file nann't be null");
		
		FileOutputStream fis = null;
		
		try {
			
			fis = new FileOutputStream(file);
			fis.write( asBytes() );
			fis.flush();
			
		} finally {
			if( fis != null )
				fis.close();
		}
	}

	public byte[] asBytes() throws IOException {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		for(int frameNo=1; frameNo<frames.size(); frameNo++) {
			Map<Integer,Color> map = Program.buildColorsDiff(frames.get(frameNo).getColors(), frames.get(frameNo-1).getColors());
			byte[] bytesBuff = new byte[map.size()*PROG_COLOR_BYTES];
			int buffPos = 0;
			for(Entry<Integer,Color> entry: map.entrySet()) {
				byte[] colorBytesBuff = colorToBytes(entry.getKey(), entry.getValue());
				System.arraycopy(colorBytesBuff, 0, bytesBuff, buffPos, colorBytesBuff.length);
				buffPos += PROG_COLOR_BYTES;
			}
			baos.write((byte)0);
			baos.write((byte)map.size() & 0xff);
			int d = (int)frames.get(frameNo).getDelayAfter();
			baos.write( new byte[]{ (byte)(d >> 8 & 0xff) } );
			baos.write( new byte[]{ (byte)(d & 0xff) } );
			//fis.write( ByteBuffer.allocate(2).putShort((short)d).array() );
			baos.write(bytesBuff);
		}
		
		return baos.toByteArray();
	}	
	
	public static byte[] colorToBytes(int n, Color c) {
		if( n == Serial.DATA_TERMINATOR )
			throw new IllegalArgumentException(String.format("n cann't to equals %02X", Serial.DATA_TERMINATOR));
		return new byte[] {
				(byte)n,
				(byte)(c.getRed() == Serial.DATA_TERMINATOR ? c.getRed() + 1 : c.getRed()),
				(byte)(c.getGreen() == Serial.DATA_TERMINATOR ? c.getGreen() + 1 : c.getGreen()),
				(byte)(c.getBlue() == Serial.DATA_TERMINATOR ? c.getBlue() + 1 : c.getBlue())
			};
	}
	
	public static Map<Integer,Color> buildColorsDiff(Color[] currColors, Color[] prevColors) {
		Map<Integer,Color> map = new HashMap<Integer,Color>();
		if( prevColors != null && prevColors.length != currColors.length )
			throw new RuntimeException("prevColors.length and currColors.length MUST be the same!");
		for(int i=0; i<currColors.length; i++)
			if( prevColors == null || !currColors[i].equals(prevColors[i]) )
				map.put(i, currColors[i]);
		return map;
	}
	
	public Program adapt(int count, Color initColor) {
		for(Frame frame: frames) {
			if( frame.getColors().length != count ) {
				Color[] colors = new Color[count];
				System.arraycopy(frame.getColors(), 0, colors, 0, Math.min(count, frame.getColors().length));
				if( frame.getColors().length < count )
					Arrays.fill(colors, frame.getColors().length-1, count, initColor);
				frame.setColors(colors);
			}
		}
		return this;
	}
}
