package com.zholdak.lights.forms;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import jssc.SerialPortException;
import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;

import com.zholdak.lights.classes.Program;
import com.zholdak.lights.classes.Serial;
import com.zholdak.utils.ConfirmationDialog;
import com.zholdak.utils.Utils;

public class ConsoleDialog extends JDialog implements KeyListener, WindowListener {
	
	private static final long serialVersionUID = 1L;

	private static final int CMD_PUT = 1;
	//private static final int CMD_GET = 2;
	private static final int CMD_DELETE = 3;
	
	private Component parent;
	
	private JPanel contentPanel;
	private JTextField commandEdit;
	private JTextPane outTextPane;
	private Serial serial;
	private Program prog;
	
	private List<String> cmdHistory = new ArrayList<String>();
	private int historyListPos = -1;
	
	public ConsoleDialog(Serial serial, Program prog) {
		
		this.serial = serial;
		this.prog = prog;
		
		setModal(true);
		setResizable(true);
		setBounds(0, 0, 568, 336);
		setIconImage(Toolkit.getDefaultToolkit().getImage(MainForm.class.getResource("/images/icons/sd_card_x16.png")));
		setTitle("Консоль контроллера flash-памяти");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		getContentPane().setLayout(new BorderLayout(0,0));
		
		contentPanel = new JPanel();
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.setLayout(new MigLayout("wrap", "[grow,fill]", "[][grow,fill]"));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		commandEdit = new JTextField();
		contentPanel.add(commandEdit, "");
		commandEdit.setFont(new Font("Consolas", Font.PLAIN, 12));
		commandEdit.addKeyListener(this);
		
		outTextPane = new JTextPane();
		JScrollPane outTextPaneScroll = new JScrollPane(outTextPane);
		contentPanel.add(outTextPaneScroll);
		outTextPane.setFont(new Font("Consolas", Font.PLAIN, 12));
		
		addWindowListener(this);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				commandEdit.requestFocus();
			}
		});
		
		try {
			serial.writeBytesRaw(new byte[]{Serial.DATA_TERMINATOR, Serial.CONTROLLER_EXIT});
		} catch (SerialPortException | InterruptedException e) {
			e.printStackTrace();
			ConfirmationDialog.exception(null, e);
		} 
	}

	public ConsoleDialog showDialog() {
		setLocationRelativeTo(parent);
		setVisible(true);
		return this;
	}
	
	public void closeDialog() {
		ConsoleDialog.this.dispose();
	}

	@Override
	public void keyPressed(KeyEvent ke) {
		if( ke.getSource().equals(commandEdit) ) {
			if( ke.getKeyCode() == KeyEvent.VK_ENTER ) {
				executeCommand();
			}
			else if( ke.getKeyCode() == KeyEvent.VK_UP ) {
				if( historyListPos < 0 && cmdHistory.size() > 0 ) {
					historyListPos = cmdHistory.size() - 1;
					commandEdit.setText( cmdHistory.get(historyListPos) );
				}
				else if( historyListPos > 0 ) {
					commandEdit.setText( cmdHistory.get(--historyListPos) );
				}
			}
			else if( ke.getKeyCode() == KeyEvent.VK_DOWN ) {
				if( historyListPos < cmdHistory.size() - 1 ) {
					commandEdit.setText( cmdHistory.get(++historyListPos) );
				}
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent ke) {
	}

	@Override
	public void keyTyped(KeyEvent ke) {
	}
	
	/**
	 * 
	 */
	private void executeCommand() {
		String commandText = commandEdit.getText().trim();
		commandEdit.setText("");
		
		if( commandText.isEmpty() ) {
			outString("Command is empty\n\n");
			tryHelpForHelp();
			return;
		}
		
		String[] cmdArray = StringUtils.split(commandText, " ", 2);
		String cmd = cmdArray[0].trim().toLowerCase();
		String cmdArgs = cmdArray.length == 1 ? null : cmdArray[1];
		
		addToHistory(commandText);
		
		if( cmd.matches("^h(e(l(p)?)?)?$") ) {
			showHelp();
			return;
		}
		else if( cmd.matches("^cl(e(a(r)?)?)?$") ) {
			if( cmdArgs != null && cmdArgs.trim().toLowerCase().matches("^h(i(s(t(o(r(y)?)?)?)?)?)?$") )
				cmdHistory.clear();
			else
				outTextPane.setText("");
			return;
		}
		else if( cmd.matches("^la(s(t)?)?$") ) {
			showLastCommands();
			return;
		}
		else if( cmd.matches("^li(s(t)?)?$") ) {
			listProgs();
			return;
		}
		else if( cmd.matches("^in(f(o)?)?$") ) {
			sdInfo();
			return;
		}
		else if( cmd.matches("^wr(i(t(e)?)?)?$") ) {
			cmd(CMD_PUT, cmdArgs);
			return;
		}
		else if( cmd.matches("^de(l(e(t(e)?)?)?)?$") ) {
			cmd(CMD_DELETE, cmdArgs);
			return;
		}
		else if( cmd.matches("^(clo(s(e)?)?|ex(i(t)?)?)$") ) {
			closeDialog();
			return;
		}
		else {
			outString("Unknown command\n");
			tryHelpForHelp();
		}
	}

	private void closeCmd() {
		try {
			serial.writeBytesRaw(new byte[]{Serial.CONTROLLER_EXIT});
		} catch (SerialPortException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void cmd(int cmd, String cmdArgs) {

		byte[] data = new byte[]{};
		
		if( cmd == CMD_PUT && prog == null ) {
			outString("Prog empty\n\n");
			return;
		}
		if( cmdArgs == null ) {
			outString("Illegal number of params\n");
			tryHelpForHelp();
			return;
		}
		String name = cmdArgs.toUpperCase();
		if( !name.matches("^[0-9A-Z-_]{1,8}(\\.[0-9A-Z-_]{1,3})?$") ) {
			outString(String.format("Invalid filename '%s'\n\n", name));
			return;
		}
		try {
			if( cmd == CMD_PUT ) {
				outString(String.format("write '%s'\n", name));
				serial.writeBytesRaw(new byte[]{Serial.SD_PUT_FILE});
			} 
			else if( cmd == CMD_DELETE ) {
				outString(String.format("delete '%s'\n", name));
				serial.writeBytesRaw(new byte[]{Serial.SD_DEL_FILE});
			}
			serial.writeBytesRaw(name.getBytes());
			serial.writeBytesRaw(new byte[]{Serial.DATA_TERMINATOR});

			data = serial.readBytes(Serial.DATA_TERMINATOR, Serial.TIMEOUT, 1);
			
		} catch (SerialPortException | InterruptedException e) {
			e.printStackTrace();
		}
		if( data.length == 0 )
			outString("Filename Write failed. Timeout?\n\n");
		else if( data.length > Serial.MAX_BYTES )
			outString("Filename Maximum allowed bytes to read reached\n\n");
		else if( data[0] == Serial.SD_BYTE_TIMEOUT )
			outString("Filename NOT wrote, timeout\n\n");
		else if( data[0] == Serial.SD_BYTE_EMPTY_FILENAME )
			outString("Filename Empty filename\n\n");
		else if( data[0] == Serial.SD_BYTE_CANT_DELETE )
			outString("Can't delete\n\n");
		else if( data[0] == Serial.SD_BYTE_CANT_OPEN_FOR_WRITE ) 
			outString("Can't open for write\n\n");
		else if( data[0] == Serial.SD_BYTE_INIT_ERROR )
			outString("SD init error\n\n");
		else if( data[0] == Serial.SD_BYTE_ERROR )
			outString("Unknown error\n\n");
		else {

			outString(String.format("Filename OK\n"));
			repaint();
			
			if( cmd == CMD_PUT ) {
				
				byte[] progBytes;
				try {
					progBytes = prog.asBytes();
				} catch (IOException e) {
					outString(String.format("Prog to binary conversion error\n\n"));
					progBytes = null;
					e.printStackTrace();
					return;
				}
				
				try {
					serial.writeBytesRaw(new byte[]{
							(byte)((progBytes.length >> 24) & 0xff),
							(byte)((progBytes.length >> 16) & 0xff),
							(byte)((progBytes.length >> 8) & 0xff),	
							(byte)(progBytes.length & 0xff)
					});
					for(byte b: progBytes) {
						serial.writeBytesRaw(new byte[]{b});
						Thread.sleep(1);
					}

					data = serial.readBytes(Serial.DATA_TERMINATOR, Serial.TIMEOUT, 1);
					
				} catch (SerialPortException | InterruptedException e) {
					e.printStackTrace();
				}

				if( data.length == 0 )
					outString("Prog data Write failed. Timeout?\n\n");
				else if( data.length > Serial.MAX_BYTES )
					outString("Prog data Maximum allowed bytes to read reached\n\n");
				else if( data[0] == Serial.SD_BYTE_TIMEOUT )
					outString("Prog data NOT wrote, timeout\n\n");
				else if( data[0] == Serial.SD_BYTE_INCOMPLETE_DATA )
					outString("Incomplete data\n\n");
				else if( data[0] == Serial.SD_BYTE_CANT_WRITE )
					outString("Can't write\n\n");
				else if( data[0] == Serial.SD_BYTE_INIT_ERROR )
					outString("SD init error\n\n");
				else if( data[0] == Serial.SD_BYTE_ERROR )
					outString("Unknown error\n\n");
				else
					outString(String.format("OK\n\n"));
				
			} else
				outString(String.format("OK\n\n"));
		}
	}
	
	private void sdInfo() {

		byte[] data = new byte[]{};
		
		try {
			
			serial.writeBytesRaw(new byte[]{Serial.SD_INFO});
			
			data = serial.readBytes(Serial.DATA_TERMINATOR, Serial.TIMEOUT, Serial.MAX_BYTES);
			
		} catch (SerialPortException | InterruptedException e) {
			e.printStackTrace();
		}
		
		if( data.length == 0 )
			outString("Read failed\n\n");
		else if( data.length > Serial.MAX_BYTES )
			outString("Maximum allowed bytes to read reached\n\n");
		else {
			try {
				String inData = new String(data).trim();
				String[] parts = StringUtils.split(inData, "/");
				if( parts.length != 2 )
					throw new RuntimeException(String.format("Wrong data '%s' received", inData));
				Long sdSize = Long.parseLong(parts[1]);
				String s = String.format("SD FAT%s", parts[0].trim());
				outString(String.format("= %12s %s%d\n\n",
						s,
						Utils.repeatString(" ", 10-sdSize.toString().length()), sdSize
						));
				
			} catch(Exception e) {
				outString("Failed to parse with message '"+e.getMessage()+"'\n\n");
			}
		}
	}
	
	private void listProgs() {
		
		byte[] data = new byte[]{};
				
		try {
			
			serial.writeBytesRaw(new byte[]{Serial.SD_GET_FILE_LIST});
			
			data = serial.readBytes(Serial.DATA_TERMINATOR, Serial.TIMEOUT, Serial.MAX_BYTES);
			
		} catch (SerialPortException | InterruptedException e) {
			e.printStackTrace();
		}
		
		if( data.length == 0 )
			outString("Read failed or no progs\n\n");
		else if( data.length > Serial.MAX_BYTES )
			outString("Maximum allowed bytes to read reached\n\n");
		else {
			String[] parts = StringUtils.split(new String(data).trim(), ":");
			if( parts.length == 0 )
				outString("List empty\n\n");
			else {
				Integer totalSize = 0;
				for(String part: parts) {
					String[] file = StringUtils.split(part.trim(), "/");
					try {
						Integer size = Integer.parseInt(file[1]);
						totalSize += size;
						outString(String.format("* %s%s %s%d\n",
								file[0].trim(), Utils.repeatString(" ", 12-file[0].trim().length()),
								Utils.repeatString(" ", 10-size.toString().length()), size
								));
					} catch(Exception e) {
						outString(String.format("* (file entry error '%s' with message '%s')", part, e.getMessage()));
					}
				}
				String s = String.format("%d prog(s)", parts.length);
				outString(String.format("= %12s %s%d\n\n",
						s,
						Utils.repeatString(" ", 10-totalSize.toString().length()), totalSize
						));
			}
		}
	}
	
	private void showLastCommands() {
		StringBuilder sb = new StringBuilder();
		for(String cmd: cmdHistory)
			sb.append(" "+cmd+"\n");
		outString("Your last commands are:\n"+sb.toString()+"\n");
	}
	
	private void tryHelpForHelp() {
		outString("Type 'help' for help\n\n");
	}
	
	private void showHelp() {
		outString("This is help for you:\n"
				+ " 'help' this help\n"
				+ " 'close/exit' close this colsole\n"
				+ " 'clear' clear output\n"
				+ " 'last' show last commands history\n"
				+ " 'clear history' clear command history\n"
				+ " 'info' show sd card info\n"
				+ " 'list' progs list on flash\n"
				+ " 'delete <FILENAME>' delete prog with 'FILENAME'\n"
				+ " 'write <FILENAME>' write current prog with 'FILENAME'\n"
				+ " 'shuffle' shuffle progs\n\n");
	}
	
	private void outString(String str) {
		try {
			StyledDocument doc = outTextPane.getStyledDocument();
			doc.insertString(doc.getLength(), str, null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	private void addToHistory(String cmd) {
		if( cmdHistory.size() == 0 || !cmdHistory.get(cmdHistory.size()-1).equals(cmd) ) 
			cmdHistory.add(cmd);
		historyListPos = -1;
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}
	@Override
	public void windowClosed(WindowEvent e) {
		closeCmd();
	}
	@Override
	public void windowClosing(WindowEvent e) {
	}
	@Override
	public void windowDeactivated(WindowEvent e) {
	}
	@Override
	public void windowDeiconified(WindowEvent e) {
	}
	@Override
	public void windowIconified(WindowEvent e) {
	}
	@Override
	public void windowOpened(WindowEvent e) {
	}
}
