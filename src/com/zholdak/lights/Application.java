package com.zholdak.lights;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.zholdak.lights.forms.MainForm;
import com.zholdak.utils.ConfirmationDialog;

public final class Application {

	public static final String ICONS_PATH = "/images/icons/";
	
	public static String[] CmdArgs = null;
	
	/**
	 * 
	 */
	public static void main(String[] args) {
		CmdArgs = args;
		new Application().start(args);
	}

	/**
	 * 
	 */
	public void start(String[] args) {

		// Чтение коммандной строки приложения
		CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
		} catch( CmdLineException e ) {
			System.out.println(e.getMessage());
			System.out.println("Usage:");
			parser.printUsage(System.out);
			System.exit(1);
		}
		
		try {
			
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
			
			begin();
			
		} catch( Exception e ) {
			e.printStackTrace();
			ConfirmationDialog.exception(null, e);
		}
	}	

	/**
	 * 
	 */
	private void begin() throws Exception {

		new MainForm().showForm();
		
//		Light lg1 = new Light(99, 1, 7, 4);
//		System.out.println(lg1.toString());
//
//		Light lg2 = Light.fromBytes( lg1.asBytes() );
//		System.out.println(lg2.toString());
//
//		System.out.println(String.format(
//				"%d %d %d",
//				lg2.getR255(), lg2.getG255(), lg2.getB255()
//				));
	}
	
}

//System.out.println(String.format("%d (%s %s)", s, String.format("%8s",Integer.toBinaryString(s>>8)).replace(' ','0'), String.format("%8s",Integer.toBinaryString(s&0xff)).replace(' ','0')));
//byte[] b = lg1.asBytes();
//System.out.println(String.format(
//		"%d %d %s %s",
//		(int)b[0]&0xff, (int)b[1]&0xff,
//		String.format("%8s",Integer.toBinaryString(b[0]&0xff)).replace(' ','0'),
//		String.format("%8s",Integer.toBinaryString((int)b[1]&0xff)).replace(' ','0')
//		));

