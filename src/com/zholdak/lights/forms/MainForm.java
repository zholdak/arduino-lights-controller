package com.zholdak.lights.forms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableCellRenderer;

import jssc.SerialPortException;
import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.ArrayUtils;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.action.AbstractActionExt;

import com.zholdak.lights.classes.AbstractActionEx;
import com.zholdak.lights.classes.Bulb;
import com.zholdak.lights.classes.Frame;
import com.zholdak.lights.classes.FrameTableCellRenderer;
import com.zholdak.lights.classes.IOnBulb;
import com.zholdak.lights.classes.IOnColorChoosing;
import com.zholdak.lights.classes.JBulbPopupMenu;
import com.zholdak.lights.classes.JTablePopupMenu;
import com.zholdak.lights.classes.JXFlatImageDropdownButton;
import com.zholdak.lights.classes.Program;
import com.zholdak.lights.classes.ProgramTableModel;
import com.zholdak.lights.classes.Serial;
import com.zholdak.lights.components.JXFlatColorButton;
import com.zholdak.lights.components.JXFlatImageButton;
import com.zholdak.lights.components.JXFlatImageToggleButton;
import com.zholdak.lights.components.LightsPanel;
import com.zholdak.utils.ConfirmationDialog;
import com.zholdak.utils.WinRegistry;

public final class MainForm
	extends JFrame
	implements ChangeListener, ItemListener, ListSelectionListener, MouseListener, IOnBulb, WindowListener {

	private static final long serialVersionUID = 1L;

	private static final String AppRegKey = "SOFTWARE\\zholdak.com\\lights";
	private static final String SaveLastDirRegKey = "last_dir";
	private static final String PortNumRegKey = "com";
	
	private JPanel mainPanel;
	private LightsPanel lightsPanel;
	private JSlider lightsSlider; //, colorsSlider;
	private JXFlatColorButton colorButton;
//	private JComboBox<Color> colorsCombobox;
	private JBulbPopupMenu bulbPopupMenu;
	private JTablePopupMenu tablePopupMenu;
	private JSpinner groupSpinner, delaySpinner, comportSpinner; //, levelSpinner
	private JCheckBox lockCheck;
	private JLabel progLabel;
	private JXFlatImageToggleButton runProgButton, connectButton;
	private JXTable table;
	private ProgramTableModel model;
	
	private Color[] lastColors = null;
	
	private Program currentProg = null;
	private Frame[] clipboardFrames = null;
	
	private boolean programmaticalyChanging = false;

	private Serial serial;

	private AbstractActionEx sdAction = new AbstractActionEx("SD", "SD", "sd_card_x16", "sd_card_x32") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent ae) {
			if( serial == null || !serial.isOpened() )
				return;
			new ConsoleDialog(serial, currentProg).showDialog();
		}
	};
	
	private AbstractActionEx chooseColorAction = new AbstractActionEx("Выбрать цвет", "Выбрать цвет", "color_management_x16", "color_management_x32") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent ae) {
			ColorPickerDialog olorPickerDialog = new ColorPickerDialog(MainForm.this, colorButton.getColor(), null).showDialog();
			if( olorPickerDialog.getSelectedColor() != null )
				colorButton.setColor(olorPickerDialog.getSelectedColor());
		}
	};
	
	private AbstractActionEx addToProgAction = new AbstractActionEx("Добавить в программу", "Добавить в программу", "add_x16", "add_x32") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent ae) {
			try {
				checkAndInitProg();
				if( Program.buildColorsDiff(
						lightsPanel.getColors(),
						currentProg.getLastFrame().getColors()
						).size() > 0 ) {
					currentProg.addFrame( new Frame(lightsPanel.getColors(), ((SpinnerNumberModel)delaySpinner.getModel()).getNumber().longValue()) );
					progChanged();
					int row = table.getRowCount()-1;
					table.getSelectionModel().setSelectionInterval(row, row);
					table.scrollRectToVisible(new Rectangle(table.getCellRect(row, 0, true)));
				} else {
					progChanged();
					ConfirmationDialog.warning(MainForm.this, "Нет изменений с предыдущим фреймом");
				}
			} catch (Exception e) {
				e.printStackTrace();
				ConfirmationDialog.exception(null, e);
			}
		}
	};

	private AbstractActionEx insAfterToProgAction = new AbstractActionEx("Вставить после", "Вставить в программу после выделенного фрэйма", "table_row_insert_x16", "table_row_insert_x32") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent ae) {
			if( currentProg == null )
				return;
			try {
				int n = table.convertRowIndexToModel(table.getSelectedRow());
				checkAndInitProg();
				if( Program.buildColorsDiff(
						lightsPanel.getColors(),
						currentProg.getFrame(n).getColors()
						).size() > 0 ) {
					currentProg.insertFrame(n+1, new Frame(lightsPanel.getColors(), ((SpinnerNumberModel)delaySpinner.getModel()).getNumber().longValue()) );
					progChanged();
					int row = table.convertRowIndexToView(n+1);
					table.getSelectionModel().setSelectionInterval(row, row);
					table.scrollRectToVisible(new Rectangle(table.getCellRect(row, 0, true)));
				} else {
					progChanged();
					ConfirmationDialog.warning(MainForm.this, "Нет изменений с предыдущим фреймом");
				}
			} catch (Exception e) {
				e.printStackTrace();
				ConfirmationDialog.exception(null, e);
			}
		}
	};
	
	private AbstractActionEx insBeforeToProgAction = new AbstractActionEx("Вставить перед", "Вставить в программу перед выделенным фреймом") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent ae) {
			int n;
			if( currentProg == null || currentProg.getFramesCount() < 2 || (n = table.convertRowIndexToModel(table.getSelectedRow())) < 1 )
				return;
			try {
				checkAndInitProg();
				if( Program.buildColorsDiff(
						lightsPanel.getColors(),
						currentProg.getFrame(n-1).getColors()
						).size() > 0 ) {
					currentProg.insertFrame(n, new Frame(lightsPanel.getColors(), ((SpinnerNumberModel)delaySpinner.getModel()).getNumber().longValue()) );
					progChanged();
					int row = table.convertRowIndexToView(n);
					table.getSelectionModel().setSelectionInterval(row, row);
					table.scrollRectToVisible(new Rectangle(table.getCellRect(row, 0, true)));
				} else {
					progChanged();
					ConfirmationDialog.warning(MainForm.this, "Нет изменений с предыдущим фреймом");
				}
			} catch (Exception e) {
				e.printStackTrace();
				ConfirmationDialog.exception(null, e);
			}
		}
	};
	
	private AbstractActionEx saveFrameProgAction = new AbstractActionEx("Сохранить фрэйм", "Сохранить фрейм", "disk_x16", "disk_x32") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent ae) {
			if( currentProg == null || table.getSelectedRow() <= 0 )
				return;

			try{
				int n = table.convertRowIndexToModel(table.getSelectedRow());
				Frame frame = new Frame(lightsPanel.getColors(), ((SpinnerNumberModel)delaySpinner.getModel()).getNumber().longValue() );
				currentProg.replaceFrame(n, frame);
				progChanged();
				int row = table.convertRowIndexToView(n);
				table.getSelectionModel().setSelectionInterval(row, row);
				table.scrollRectToVisible(new Rectangle(table.getCellRect(row, 0, true)));
			} catch (Exception e) {
				e.printStackTrace();
				ConfirmationDialog.exception(null, e);
			}
		}
	};
	
	private AbstractActionEx newProgAction = new AbstractActionEx("Новая программа", "Создать новую программу", "page_x16", "page_x32") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent ae) {
			if( currentProg == null || (currentProg != null && !currentProg.isHasUsavedData()) )
				startNewProg();
			else if( currentProg != null && currentProg.isHasUsavedData() && new ConfirmationDialog(MainForm.this, ConfirmationDialog.ICON_QUESTION, "Новая программа", "Текущая программа не сохранена. Начать новую программу?", new String[]{"Да", "Нет"}, ConfirmationDialog.BUTTON_1).showDialog() == ConfirmationDialog.BUTTON_1 )
				startNewProg();
		}
	};

	private AbstractActionEx openProgAction = new AbstractActionEx("Загрузить программу", "Загрузить программу с диска", "folder_x16", "folder_x32") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent ae) {
			if( currentProg == null || (currentProg != null && !currentProg.isHasUsavedData()) )
				loadProg(false);
			else if( currentProg != null && currentProg.isHasUsavedData() && new ConfirmationDialog(MainForm.this, ConfirmationDialog.ICON_QUESTION, "Загрузка программы", "Текущая программа не сохранена. Загрузить другую программу?", new String[]{"Да", "Нет"}, ConfirmationDialog.BUTTON_1).showDialog() == ConfirmationDialog.BUTTON_1 ) {
				loadProg(false);
			}
		}
	};

	private AbstractActionEx openAndAppendProgAction = new AbstractActionEx("Добавить программу", "Загрузить программу с диска и добавить в конец текущей") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent ae) {
			checkAndInitProg();
			loadProg(true);
		}
	};
	
	private AbstractActionEx saveProgAction = new AbstractActionEx("Сохранить программу", "Сохранить программу на диск", "disk_x16", "disk_x32") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent ae) {
			if( currentProg != null && currentProg.isHasUsavedData() && new ConfirmationDialog(MainForm.this, ConfirmationDialog.ICON_QUESTION, "Сохранение", "Сохранить программу?", new String[]{"Да", "Нет"}, ConfirmationDialog.BUTTON_1).showDialog() == ConfirmationDialog.BUTTON_1 )
				saveProg(false);
		}
	};

	private AbstractActionEx saveAsProgAction = new AbstractActionEx("Сохранить программу как", "Сохранить программу на диск как", "save_as_x16", "save_as_x32") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent ae) {
			saveProg(true);
		}
	};

	private AbstractActionEx saveBinToFileAction = new AbstractActionEx("Сохранить бинарник программы", "Сохранить бинарную программу в файл", "color_swatch_x16", "color_swatch_x32") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent ae) {
			if( currentProg == null ) 
				return;
			
			try {
				
				JFileChooser fc = new JFileChooser( WinRegistry.readString(WinRegistry.HKEY_CURRENT_USER, AppRegKey, SaveLastDirRegKey) ) {
					private static final long serialVersionUID = 1L;
					@Override
					public void approveSelection() {
						if( getSelectedFile() != null && getSelectedFile().exists() ) {
							int result = JOptionPane.showConfirmDialog(this,
			                        "Файл программы уже существует. Перезаписать?", "Файл существует",
			                        JOptionPane.YES_NO_CANCEL_OPTION);
			                switch (result) {
			                case JOptionPane.YES_OPTION:
			                    super.approveSelection();
			                    return;
			                case JOptionPane.CANCEL_OPTION:
			                    cancelSelection();
			                    return;
			                default:
			                    return;
			                }
						}
						super.approveSelection();
					}
				};
				fc.setDialogTitle("Выберите каталог где сохранить файл");
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fc.setSelectedFile(new File(currentProg.getName()+".lcbin"));
				
				if( fc.showSaveDialog(MainForm.this) == JFileChooser.APPROVE_OPTION && fc.getSelectedFile() != null)
					currentProg.saveAsBinary(fc.getSelectedFile());
				
			} catch (IOException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
				ConfirmationDialog.exception(null, e);
			}
			
		}
	};
	
	private AbstractActionEx delProgFramesAction = new AbstractActionEx("Удалить строки", "Удалить строки из программы", "trash_x16", "trash_x32") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent ae) {
			if( currentProg != null && new ConfirmationDialog(MainForm.this, ConfirmationDialog.ICON_QUESTION, "Удаление", "Удалить выделенные строки программы?", new String[]{"Да", "Нет"}, ConfirmationDialog.BUTTON_1).showDialog() == ConfirmationDialog.BUTTON_1 ) {
				try {
					int[] selRows = table.getSelectedRows();
					int[] delRows = new int[selRows.length];
					for(int i=0; i<selRows.length; i++)
						delRows[i] = table.convertRowIndexToModel(selRows[i]);
					currentProg.delFrames( delRows );
					lightsPanel.reorderBulbs();
					progChanged();
				} catch (Exception e) {
					e.printStackTrace();
					ConfirmationDialog.exception(null, e);
				}
			}
		}
	};

	private AbstractActionEx copyProgFramesAction = new AbstractActionEx("Копировать в буфер", "Копировать выделенные фреймы в буфер", "copy_x16", "copy_x32") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent ae) {
			if( currentProg == null || table.getSelectedRowCount() == 0 )
				return;
			clipboardFrames = new Frame[table.getSelectedRowCount()];
			int n = 0;
			for(int i: table.getSelectedRows()) {
				clipboardFrames[n++] = model.get( table.convertRowIndexToModel( i ) );
			}
		}
	};

	private AbstractActionEx pasteProgFramesAction = new AbstractActionEx("Вставить из буфера", "Вставить фреймы из буфера", "paste_x16", "paste_x32") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent ae) {
			if( currentProg == null || clipboardFrames == null )
				return;
			try {
				int n = table.getSelectedRowCount() == 0 ? table.getRowCount() : table.convertRowIndexToModel(table.getSelectedRow()) + 1;
				for(Frame frame: clipboardFrames)
					currentProg.insertFrame(n++, new Frame( frame.getColors(), frame.getDelayAfter()));
				//clipboardFrames = null;
				progChanged();
			} catch (Exception e) {
				e.printStackTrace();
				ConfirmationDialog.exception(null, e);
			}
		}
	};
	
	private AbstractActionEx pasteOverProgFramesAction = new AbstractActionEx("Зменить из буфера", "Заменить фреймы из буфера") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent ae) {
			if( currentProg == null || clipboardFrames == null )
				return;
			if( table.getSelectedRowCount() == 0 ) {
				pasteProgFramesAction.actionPerformed(null);
				return;
			}
			
			try {
				
				int n = table.convertRowIndexToModel(table.getSelectedRow());
				for(Frame frame: clipboardFrames) {
					System.out.println(""+n+" "+table.getRowCount());
					if( n < table.getRowCount() )
						currentProg.replaceFrame(n, frame);
					else
						currentProg.addFrame(frame);
					n++;
				}
				clipboardFrames = null;
				progChanged();
			} catch (Exception e) {
				e.printStackTrace();
				ConfirmationDialog.exception(null, e);
			}
		}
	};
	
	private AbstractActionEx reverseProgFramesAction = new AbstractActionEx("Инвертировать", "Инвертировать выбранные фреймы", "arrow_reverse_x16", "arrow_reverse_x32") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent ae) {
			if( currentProg == null || table.getRowCount() < 3 || table.getSelectedRowCount() < 2 )
				return;
			try {
				
				int i;
				
				Frame[] frames = new Frame[table.getSelectedRowCount()];
				i = 0;
				for(int n: table.getSelectedRows())
					frames[i++] = model.get(table.convertRowIndexToModel(n));
				ArrayUtils.reverse(frames);

				i = 0;
				for(int n: table.getSelectedRows())
					currentProg.replaceFrame(table.convertRowIndexToModel(n), frames[i++]);
				
				progChanged();
			} catch (Exception e) {
				e.printStackTrace();
				ConfirmationDialog.exception(null, e);
			}
		}
	};
	
	private AbstractActionEx connectAction = new AbstractActionEx("Отобразить", "Отобразить", "connect_x16", "connect_x32") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent ae) {
			try {
				
				if( connectButton.isSelected() ) {
					if( serial != null )
						serial.close();
					
					Integer serialNum = ((SpinnerNumberModel)comportSpinner.getModel()).getNumber().intValue();
					
					serial = new Serial( serialNum );
					// послать что угодно, чтобы контроллер понял, что мы начинаем управлять напрямую ;)
					serial.writeBytes(";)".getBytes()); 
					
					WinRegistry.createKey(WinRegistry.HKEY_CURRENT_USER, AppRegKey);
					WinRegistry.writeStringValue(WinRegistry.HKEY_CURRENT_USER, AppRegKey, PortNumRegKey, serialNum.toString());
					
				} else {
					if( serial != null ) {
						try {
							serial.writeBytesRaw(new byte[]{Serial.DATA_TERMINATOR, Serial.RETURN_TO_PLAYER});
						} catch (SerialPortException | InterruptedException e) {
							e.printStackTrace();
						} 
						serial.close();
					}
					serial = null;
				}
				
			} catch (Exception e) {
				//showButton.setSelected(false);
				e.printStackTrace();
				ConfirmationDialog.exception(null, e);
			}
		}
	};

	private AbstractActionEx runProgAction = new AbstractActionEx("Выполнить программу", "Выполнить программу", "lightning_x16", "lightning_x32") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent ae) {
			if( runProgButton.isSelected() ) {

				if( currentProg == null || currentProg.getFramesCount() < 2 ) {
					runProgButton.setSelected(false);
					return;
				}
				
				int startFrame = 0;
				if( table.getSelectedRow() > 0 )
					startFrame = table.convertRowIndexToModel(table.getSelectedRow());
				
				runner = new ProgRunner(startFrame);
				runner.execute();
			} else {
				if( runner != null && !runner.isCancelled() )
					runner.cancel(true);
			}
		}
	};
	
	private AbstractActionEx fixAction = new AbstractActionEx("Фикс", "Фиксировать лампочки в позиции ползунка", "lock_x16", "lock_x32") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent ae) {
			// придерживаться групп?
			if( lockCheck.isSelected() ) {
				// залочить все лампы в группе
				for(int i=0; i<lightsPanel.getCountInGroup(); i++) {
					int n = lightsSlider.getValue()*lightsPanel.getCountInGroup()+i;
					if( n < lightsPanel.getBulbsCount() )
						lightsPanel.getBulb(n).setLocked(true);
				}
			} else {
				// залочить лампочку
				lightsPanel.getBulb(lightsSlider.getValue()).setLocked(true);
			}
		}
	};

	private AbstractActionEx resetAction = new AbstractActionEx("Сброс всех", "Сброс всех лампочек", "clean_x16", "clean_x32") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent ae) {
			lightsPanel.turnOffAll();
			showBulbs();
		}
	};

	private AbstractActionEx leftAction = new AbstractActionEx("Влево", "Сметить влево", "arrow_left_x16", "arrow_left_x32") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent ae) {
			lightsPanel.shiftLeft();
			showBulbs();
		}
	};

	private AbstractActionEx rightAction = new AbstractActionEx("Вправо", "Сметить вправо", "arrow_right_x16", "arrow_right_x32") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent ae) {
			lightsPanel.shiftRight();
			showBulbs();
		}
	};

	private AbstractActionEx wandAction = new AbstractActionEx("Дополнительно", "Дополнительные инструменты", "wand_x16", "wand_x32") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent ae) {
		}
	};

	private AbstractActionExt bulbWhiteAction = new AbstractActionExt("Белый") {
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent ae) {
			//System.out.println("!! "+ae.getSource().getClass().getName());
			setBulbColor(bulbPopupMenu.getBulb(), Color.WHITE);
		}
	};

	private AbstractActionExt bulbRedAction = new AbstractActionExt("Красный") {
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent ae) {
			setBulbColor(bulbPopupMenu.getBulb(), Color.RED);
		}
	};

	private AbstractActionExt bulbGreenAction = new AbstractActionExt("Зелёный") {
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent ae) {
			setBulbColor(bulbPopupMenu.getBulb(), Color.GREEN);
		}
	};

	private AbstractActionExt bulbBlueAction = new AbstractActionExt("Синий") {
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent ae) {
			setBulbColor(bulbPopupMenu.getBulb(), Color.BLUE);
		}
	};

	private AbstractActionExt bulbYellowAction = new AbstractActionExt("Жёлтый") {
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent ae) {
			setBulbColor(bulbPopupMenu.getBulb(), Color.YELLOW);
		}
	};

	private AbstractActionExt bulbMagentaAction = new AbstractActionExt("Фиолетовый") {
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent ae) {
			setBulbColor(bulbPopupMenu.getBulb(), Color.MAGENTA);
		}
	};

	private AbstractActionExt bulbCyanAction = new AbstractActionExt("Голубой") {
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent ae) {
			setBulbColor(bulbPopupMenu.getBulb(), Color.CYAN);
		}
	};
	
	private AbstractActionExt bulbOffAction = new AbstractActionExt("Выключить") {
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent ae) {
			setBulbColor(bulbPopupMenu.getBulb(), Color.BLACK);
		}
	};

	private AbstractActionEx changeColorAllAction = new AbstractActionEx("Заменить этот цвет", "Заменить этот цвет на всех лампочках") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent ae) {
			Color c = new Color( bulbPopupMenu.getBulb().getColor().getRGB() );
			for(Bulb b: lightsPanel.getBulbs())
				if( b.getColor().equals( c ) )
					b.setColor( colorButton.getColor() );
			lightsPanel.update();
			update();
			showBulbs();
		}
	};

	private AbstractActionEx changeColorAllSelectedAction = new AbstractActionEx("Заменить этот цвет в выделенных", "Заменить этот цвет на всех лампочках в выделенных фреймах") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent ae) {

			if( table.getSelectedRowCount() > 0 ) {

				Color c = null;
				
				if( ae.getSource() instanceof JMenuItem ) {
					if( ((JMenuItem)ae.getSource()).getParent() instanceof JTablePopupMenu ) {
						int row = ((JTablePopupMenu)((JMenuItem)ae.getSource()).getParent()).getRow();
						int idx = ((JTablePopupMenu)((JMenuItem)ae.getSource()).getParent()).getColorIndex();
						if( row >= 0 && idx >= 0 )
							c = new Color( model.get(table.convertRowIndexToModel( row )).getColors()[idx].getRGB() );
					} else if( ((JMenuItem)ae.getSource()).getParent() instanceof JBulbPopupMenu ) {
						c = new Color( ((JBulbPopupMenu)((JMenuItem)ae.getSource()).getParent()).getBulb().getColor().getRGB() );
					}
				}

				if( c != null ) {
				
					for(int n: table.getSelectedRows()) {
						Frame frame = model.get(table.convertRowIndexToModel(n));
						Color[] newColors = new Color[frame.getColors().length];
						for(int i=0; i<frame.getColors().length; i++) {
							if( frame.getColors()[i].equals( c ) )
								newColors[i] = new Color( colorButton.getColor().getRGB() );
							else
								newColors[i] = new Color( frame.getColors()[i].getRGB() );
						}
						frame.setColors(newColors);
					}
				}
				
			} else {
				ConfirmationDialog.warning(MainForm.this, "Не выбраны фреймы");
			}
			if( table.getSelectedRow() >= 0 )
				lightsPanel.setColors( model.get(table.convertRowIndexToModel(table.getSelectedRow())).getColors() );
			lightsPanel.update();
			update();
			showBulbs();
		}
	};

	private AbstractActionEx changeTimeAllSelectedAction = new AbstractActionEx("Заменить время выделенных", "Заменить время во всех выделенных фреймах") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent ae) {

			if( table.getSelectedRowCount() > 0 ) {
				
				for(int n: table.getSelectedRows()) {
					model.get(table.convertRowIndexToModel(n)).setDelayAfter( ((SpinnerNumberModel)delaySpinner.getModel()).getNumber().longValue());
				}				
			} else {
				ConfirmationDialog.warning(MainForm.this, "Не выбраны фреймы");
			}
			update();
			showBulbs();
		}
	};
	
	private AbstractActionEx pickColorAction = new AbstractActionEx("Взять цвет", "Взять цвет", "color_picker_x16", "color_picker_x32") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent ae) {
			colorButton.setColor(bulbPopupMenu.getBulb().getColor());
		}
	};

	private AbstractActionEx setColorAction = new AbstractActionEx("Установить цвет", "Установить цвет", "color_management_x16", "color_management_x32") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(final ActionEvent ae) {
			Color savedColor = new Color(bulbPopupMenu.getBulb().getColor().getRGB());
			ColorPickerDialog colorPickerDialog = new ColorPickerDialog(MainForm.this, bulbPopupMenu.getBulb().getColor(), new IOnColorChoosing() {
				@Override
				public void colorChanged(Color color) {
					setBulbColor(bulbPopupMenu.getBulb(), color);
//					b.setColor(color);
//					lightsPanel.update();
//					showBulbs();
				}}).showDialog();
			bulbPopupMenu.getBulb().setColor( colorPickerDialog.getSelectedColor() == null ? savedColor : colorPickerDialog.getSelectedColor() );
			lightsPanel.update();
			showBulbs();
		}
	};
	
//	private AbstractActionEx hiLevelAction = new AbstractActionEx("Увеличить уровень", "Увеличить уровень у всех цветов на один") {
//		private static final long serialVersionUID = 1L;
//		public void onActionPerformed(ActionEvent ae) {
//			for(int i=0; i<lightsPanel.getBulbsCount(); i++) {
//				Color color = lightsPanel.getBulb(i).getColor();
//				if( color.equals(Color.BLACK) || color.getRed() == 255 || color.getGreen() == 255 || color.getBlue() == 255 ) // это уже максимальный уровень
//					continue;
////				lightsPanel.getBulb(i).setColor( new Color(
////						color.getRed() == 0 ? 0 : Bulb.incLevel(color.getRed(), 1),
////						color.getGreen() == 0 ? 0 : Bulb.incLevel(color.getGreen(), 1),
////						color.getBlue() == 0 ? 0 : Bulb.incLevel(color.getBlue(), 1)
////						));
//			}
//			lightsPanel.update();
//			showBulbs();
//		}
//	};
//
//	private AbstractActionEx lowLevelAction = new AbstractActionEx("Уменьшить уровень", "Уменьшить уровень у всех цветов на один") {
//		private static final long serialVersionUID = 1L;
//		public void onActionPerformed(ActionEvent ae) {
//			for(int i=0; i<lightsPanel.getBulbsCount(); i++) {
//				Color color = lightsPanel.getBulb(i).getColor();
////				lightsPanel.getBulb(i).setColor( new Color(
////						Bulb.decLevel(color.getRed(), 1),
////						Bulb.decLevel(color.getGreen(), 1),
////						Bulb.decLevel(color.getBlue(), 1)
////						));
//			}
//			lightsPanel.update();
//			showBulbs();
//		}
//	};
	
	private class ProgFrame {
		private int n;
		private Frame frame;
		public ProgFrame(int n, Frame frame) {
			this.n = n;
			this.frame = frame;
		}
		public int getN() {
			return n;
		}
		public Frame getFrame() {
			return frame;
		}
	}
	
	private class ProgRunner extends SwingWorker<Object, Object> {
		private int startFrame = 0;
		public ProgRunner(int startFrame) {
			this.startFrame = startFrame;
		}
		@Override
		protected Object doInBackground() throws Exception {

			try {
				lightsPanel.startAnimationMode();
				for(;;) {
	
					for(int i=startFrame; i<currentProg.getFramesCount(); i++) {
	
						List<Object> pf = new ArrayList<Object>();
						
						if( isCancelled() ) {
							pf.add(new ProgFrame(i, null));
							process(pf);
							break;
						}
							
						pf.add(new ProgFrame(i, currentProg.getFrame(i)));
						process(pf);
					}
					startFrame = 1;
	
					if( isCancelled() ) {
						break;
					}
				}
			} finally {
				lightsPanel.stopAnimationMode();
				lightsPanel.update();
			}
			return null;
		}
		@Override
		protected void process(List<Object> chunks) {
			super.process(chunks);
			if( chunks != null ) {
				for(Object chunk: chunks) {
					ProgFrame pf = (ProgFrame)chunk;
					try {
						
						if( pf.getFrame() != null ) {
							lightsPanel.setColors(pf.getFrame().getColors());
							Thread.sleep(pf.getFrame().getDelayAfter());
						} else {
							int row = table.convertRowIndexToView(pf.getN());
							table.getSelectionModel().setSelectionInterval(row, row);
							table.scrollRectToVisible(new Rectangle(table.getCellRect(row, 0, true)));
							table.repaint();
							lightsPanel.update();
						}
						
						showBulbs();
						
					} catch (InterruptedException e) { }
				}
			}
		}
	};

	private ProgRunner runner = null;
	
	public MainForm() throws SerialPortException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		startUpdate();
		
		setResizable(true);
		setIconImage(Toolkit.getDefaultToolkit().getImage(MainForm.class.getResource("/images/icons/color_swatch_x16.png")));
		setTitle("Составление программы для гирлянды");
		setBounds(0, 0, 1280, 800);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null); // Отцентрировать в экране
		
		addWindowListener(this);
		
		getContentPane().setLayout( new BorderLayout(0, 0) );
		mainPanel = new JPanel();
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		mainPanel.setLayout(new MigLayout("wrap", "[grow]", "[][][][][]0px[grow,fill]"));
		{
		
			JPanel lPanel = new JPanel();
			lPanel.setLayout(new MigLayout("wrap", "[fill,grow][][]5px[]10px[]10px[]2px[]10px[]0px[]10px[]0px[]0px[]10px[]10px[]", "0px[]0px"));
			mainPanel.add(lPanel, "grow");
			{
				lPanel.add(createLabel("Гирлянда", true), "left");

				lPanel.add(createLabel("Порт"), "");
				int portNum = 1;
				try {
					String s = WinRegistry.readString(WinRegistry.HKEY_CURRENT_USER, AppRegKey, PortNumRegKey);
					if( s != null )
						portNum = Integer.valueOf(s);
				} catch(NumberFormatException e) { }
				comportSpinner = new JSpinner(new SpinnerNumberModel(portNum, 1, 99, 1));
				lPanel.add(comportSpinner, "width 30px");

				connectButton = new JXFlatImageToggleButton(connectAction);
				lPanel.add(connectButton);

				lPanel.add(new JXFlatImageButton(sdAction));
				
//				final JPopupMenu flashPopupMenu = new JPopupMenu();
//				flashPopupMenu.add(saveBinToFileAction);
//				
//				JXFlatImageButton flashButton = new JXFlatImageButton(flashToEEPROMAction);
//				flashButton.addMouseListener(new MouseAdapter() {
//					public void mouseReleased(MouseEvent me) {
//						if( me.isPopupTrigger() ) {
//							flashPopupMenu.show(me.getComponent(), me.getX(), me.getY());
//						}
//					};
//				});
//				lPanel.add(flashButton);
				
				
				delaySpinner = new JSpinner(new SpinnerNumberModel(Frame.DEFAULT_DELAY, 0, 10000, 50));
				lPanel.add(delaySpinner, "width 40px");

				lPanel.add(createLabel("ms"), "");
				
				final JPopupMenu insPopupMenu = new JPopupMenu();
				insPopupMenu.add(insBeforeToProgAction);
				
				JXFlatImageButton insProgButton = new JXFlatImageButton(insAfterToProgAction);
				insProgButton.addMouseListener(new MouseAdapter() {
					public void mouseReleased(MouseEvent me) {
						if( me.isPopupTrigger() ) {
							insPopupMenu.show(me.getComponent(), me.getX(), me.getY());
						}
					};
				});
				
				JPopupMenu wandPopupMenu =  new JPopupMenu();
//				wandPopupMenu.add(hiLevelAction);
//				wandPopupMenu.add(lowLevelAction);
				
				lPanel.add(new JXFlatImageButton(leftAction));
				lPanel.add(new JXFlatImageButton(rightAction));
				lPanel.add(new JXFlatImageButton(addToProgAction));
				lPanel.add(insProgButton);
				lPanel.add(new JXFlatImageButton(saveFrameProgAction));
				lPanel.add(new JXFlatImageButton(resetAction));
				lPanel.add(new JXFlatImageDropdownButton(wandAction, wandPopupMenu));
			}
			
			lightsPanel = new LightsPanel(Color.BLACK, 100, 2);
			mainPanel.add(lightsPanel, "grow");
			{
				lightsPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
				lightsPanel.setOnBulb(this);

				bulbPopupMenu = new JBulbPopupMenu();
				bulbPopupMenu.add( setColorAction );
				bulbPopupMenu.add( pickColorAction );
				bulbPopupMenu.add( changeColorAllAction );
				bulbPopupMenu.add( changeColorAllSelectedAction );
				bulbPopupMenu.addSeparator();
				bulbPopupMenu.add( bulbWhiteAction );
				bulbPopupMenu.add( bulbRedAction );
				bulbPopupMenu.add( bulbGreenAction );
				bulbPopupMenu.add( bulbBlueAction );
				bulbPopupMenu.add( bulbYellowAction );
				bulbPopupMenu.add( bulbMagentaAction );
				bulbPopupMenu.add( bulbCyanAction );
				bulbPopupMenu.addSeparator();
				bulbPopupMenu.add( bulbOffAction );
				bulbPopupMenu.add( resetAction );
				
			}

			final JPanel contolsPanel = new JPanel();
			contolsPanel.setLayout(new MigLayout("wrap", "[]10px[]10px[][fill,grow][]", "[]0px[]"));
			mainPanel.add(contolsPanel, "grow");
			{
			
				contolsPanel.add(createLabel("Цвет"));
				contolsPanel.add(createLabel("В группе"));
				contolsPanel.add(createLabel("Блок группы"), "wrap");
				
//				Color[] colors = {Color.BLACK, Color.WHITE, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.MAGENTA, Color.CYAN};
//				colorsCombobox = new JComboBox<Color>(colors);
//				//contolsPanel.add(colorsCombobox, "growx");
//				colorsCombobox.setRenderer(new CellColorRenderer());
//				colorsCombobox.setFont(new Font("Arial", Font.BOLD, 14));
//				colorsCombobox.setMaximumRowCount(20);
//				colorsCombobox.setEditable(false);
//				colorsCombobox.setSelectedItem(Color.BLACK);
//				colorsCombobox.addItemListener(this);

				colorButton = new JXFlatColorButton(chooseColorAction, Color.BLACK);
				contolsPanel.add(colorButton, "width 100px");
				
//				colorsSlider = new JSlider();
//				contolsPanel.add(colorsSlider, "width 300px");
//				colorsSlider.setUI(new ColorsGradientSliderUI(colorsSlider));
//				colorsSlider.setMinimum(1);
//				//colorsSlider.setSize(500, colorsSlider.getHeight());
//				colorsSlider.setMinimumSize(colorsSlider.getSize());
//				colorsSlider.setMaximum(300);
//				colorsSlider.setPaintTicks(true);
//				colorsSlider.setValue(0);
//				colorsSlider.addChangeListener(this);
				
//				levelSpinner = new JSpinner(new SpinnerNumberModel(7, 0, Bulb.MAX_LEVEL, 1));
//				contolsPanel.add(levelSpinner, "growx");
//				levelSpinner.addChangeListener(this);
				
				groupSpinner = new JSpinner(new SpinnerNumberModel(lightsPanel.getCountInGroup(), 1, lightsPanel.getBulbsCount()/2, 1));
				contolsPanel.add(groupSpinner, "growx");
				groupSpinner.addChangeListener(this);
				
				lockCheck = new JCheckBox();
				contolsPanel.add(lockCheck, "center");
				lockCheck.setSelected(true);
				lockCheck.addItemListener(this);

				lightsSlider = new JSlider();
				contolsPanel.add(lightsSlider, "grow");
				lightsSlider.setMinimum(0);
				lightsSlider.setMinorTickSpacing(1);
				lightsSlider.setPaintTicks(true);
				lightsSlider.setValue(0);
				lightsSlider.addChangeListener(this);
				UIManager.put("Slider.focus", UIManager.get("Slider.background"));

				contolsPanel.add(new JXFlatImageButton(fixAction));
			}

			JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
			mainPanel.add(separator, "grow");
			separator.setPreferredSize(new Dimension(getWidth(),1));
			separator.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY));
			
			JPanel progPanel = new JPanel();
			progPanel.setLayout(new MigLayout("wrap", "[][fill,grow][][][]20px[][]10px[]10px[]20px[]", "[]0px"));
			mainPanel.add(progPanel, "grow");
			{
				progLabel = createLabel("Программа", true);
				progPanel.add(progLabel, "left");

				progPanel.add(createLabel(""), "");
				
				final JPopupMenu savePopupMenu = new JPopupMenu();
				savePopupMenu.add(saveAsProgAction);
				savePopupMenu.add(saveBinToFileAction);
				
				JXFlatImageButton saveProgButton = new JXFlatImageButton(saveProgAction);
				saveProgButton.addMouseListener(new MouseAdapter() {
					public void mouseReleased(MouseEvent me) {
						if( me.isPopupTrigger() ) {
							savePopupMenu.show(me.getComponent(), me.getX(), me.getY());
						}
					};
				});

				final JPopupMenu pastePopupMenu = new JPopupMenu();
				pastePopupMenu.add(pasteOverProgFramesAction);
				
				JXFlatImageButton pasteButton = new JXFlatImageButton(pasteProgFramesAction);
				pasteButton.addMouseListener(new MouseAdapter() {
					public void mouseReleased(MouseEvent me) {
						if( me.isPopupTrigger() ) {
							pastePopupMenu.show(me.getComponent(), me.getX(), me.getY());
						}
					};
				});

				final JPopupMenu openPopupMenu = new JPopupMenu();
				openPopupMenu.add(openAndAppendProgAction);

				JXFlatImageButton openButton = new JXFlatImageButton(openProgAction);
				openButton.addMouseListener(new MouseAdapter() {
					public void mouseReleased(MouseEvent me) {
						if( me.isPopupTrigger() ) {
							openPopupMenu.show(me.getComponent(), me.getX(), me.getY());
						}
					};
				});
				
				runProgButton = new JXFlatImageToggleButton(runProgAction);
				
				progPanel.add(new JXFlatImageButton(newProgAction));
				progPanel.add(openButton);
				progPanel.add(saveProgButton);
				progPanel.add(new JXFlatImageButton(copyProgFramesAction));
				progPanel.add(pasteButton);
				progPanel.add(new JXFlatImageButton(reverseProgFramesAction));
				progPanel.add(new JXFlatImageButton(delProgFramesAction));
				progPanel.add(runProgButton);
			}

			model = new ProgramTableModel();
			
			table = new JXTable(model);
			mainPanel.add(new JScrollPane(table), "grow");
			{
				table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				table.setHorizontalScrollEnabled(true);
				table.setEditable(false);
				table.setAutoStartEditOnKeyStroke(false);
				table.setShowVerticalLines(false);
				table.setGridColor(Color.LIGHT_GRAY);
				table.setRowSelectionAllowed(true);
				table.setAutoCreateRowSorter(false);
				table.setRowSorter(null);
				table.setColumnControlVisible(false);
				table.setFont(new Font("Arial", Font.PLAIN, 12));
				table.getSelectionModel().addListSelectionListener(this);
				table.addMouseListener(this);
				table.getColumn(ProgramTableModel.Col.N.ordinal()).setMaxWidth(50);
				table.getColumn(ProgramTableModel.Col.BULBS_COUNT.ordinal()).setMaxWidth(100);
				table.getColumn(ProgramTableModel.Col.DELAY_AFTER.ordinal()).setMaxWidth(100);
				table.setDefaultRenderer(Frame.class, new FrameTableCellRenderer());
				table.setHorizontalScrollEnabled(true);

				tablePopupMenu = new JTablePopupMenu();
				tablePopupMenu.add(changeColorAllSelectedAction);
				tablePopupMenu.add(changeTimeAllSelectedAction);
				
				
				table.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseReleased(MouseEvent me) {
						
						Point tblPoint = me.getPoint();
						
						JTable table = (JTable)me.getSource();
				    	int row = table.rowAtPoint(tblPoint);
			        	int col = table.columnAtPoint(tblPoint);
				    	
				    	if( !table.isRowSelected(row) ) // если нет никакого выделения -- делаем его чтобы хоть что-то было выделено
				    		table.getSelectionModel().setSelectionInterval(row, row);
				        if( row < 0 ) // снимаем выделение если щёлкнули в "пустой" области (где нет строк и столбцов)
				        	table.clearSelection();
				        
				        if( me.isPopupTrigger() ) {
				        	if( row >= 0 && col >=0 ) {
				        		int colorIndex = -1; // обязательно здесь инициализация несущ.значением
				        		TableCellRenderer cellRenderer = table.getCellRenderer(row, col);
					        	if( cellRenderer instanceof FrameTableCellRenderer) {
						        	colorIndex = ((FrameTableCellRenderer)cellRenderer).getColorIndex(table, me.getPoint());
					        	}
					        	tablePopupMenu.setColorIndex(colorIndex);
					        	tablePopupMenu.setRow(row);
					        	tablePopupMenu.setCol(col);
					        	tablePopupMenu.show(me.getComponent(), me.getX(), me.getY());
				        	}
				        }
				    }			
				});
			}			
		}
		
		initSlider();
		
		finishUpdate();
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				table.packAll();
			}
		});
	}
	
	/**
	 * 
	 */
	public MainForm showForm() {
		setVisible(true);
		toFront();
		return this;
	}

	/**
	 * 
	 */
	private void startNewProg() {
		try {
			currentProg = null;
			lightsPanel.turnOffAll();
			checkAndInitProg();
			progChanged();
		} catch (Exception e) {
			e.printStackTrace();
			ConfirmationDialog.exception(null, e);
		}
	}
	
	/**
	 * 
	 */
	private void loadProg(boolean append) {
		try {
			JFileChooser fc = new JFileChooser(
					WinRegistry.readString(WinRegistry.HKEY_CURRENT_USER, AppRegKey, SaveLastDirRegKey)
					);
			fc.setDialogTitle("Выберите файл программы");
			fc.setFileFilter(new FileFilter() {
				public String getDescription() {
					return "Программа (*.lcprog|*.lcprogz)";
				}
				public boolean accept(File f) {
					return f.isDirectory() || f.getName().toLowerCase().matches("^.+\\.lcprog(z?)$");
				}
			});
			fc.showOpenDialog(null);

			if( fc.getSelectedFile() != null ) {
				
				WinRegistry.createKey(WinRegistry.HKEY_CURRENT_USER, AppRegKey);
				WinRegistry.writeStringValue(WinRegistry.HKEY_CURRENT_USER, AppRegKey, SaveLastDirRegKey, fc.getSelectedFile().getParent());
				
				lightsPanel.turnOffAll();
				
				Program prog = new Program(fc.getSelectedFile() );
				if( prog.getFrame(0).getColors().length > lightsPanel.getColors().length ) {
					if( new ConfirmationDialog(MainForm.this, ConfirmationDialog.ICON_QUESTION, "Подтверждение", "<html>Загружаемая программа содержит больше лампочек, чем на панели.<br/>Количество в программе будет урезано. Продолжать?</html>", new String[]{"Да", "Нет"}, ConfirmationDialog.BUTTON_1).showDialog() == ConfirmationDialog.BUTTON_1 ) {
						prog.adapt(lightsPanel.getBulbsCount(), lightsPanel.getInitColor());
						if( append )
							currentProg.append(prog);
						else
							currentProg = prog;
						progChanged();
					}
				} else if( prog.getFrame(0).getColors().length < lightsPanel.getColors().length ) {
					if( new ConfirmationDialog(MainForm.this, ConfirmationDialog.ICON_QUESTION, "Подтверждение", "<html>Загружаемая программа содержит меньше лампочек, чем на панели.<br/>Количество в программе будет расширено. Продолжать?</html>", new String[]{"Да", "Нет"}, ConfirmationDialog.BUTTON_1).showDialog() == ConfirmationDialog.BUTTON_1 ) {
						prog.adapt(lightsPanel.getBulbsCount(), lightsPanel.getInitColor());
						if( append )
							currentProg.append(prog);
						else
							currentProg = prog;
						progChanged();
					}
				} else {
					if( append )
						currentProg.append(prog);
					else
						currentProg = prog;
					progChanged();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			ConfirmationDialog.exception(null, e);
		}
	}
	
	/**
	 * 
	 */
	private boolean checkAndInitProg() {
		try {
			if( currentProg == null ) {
				currentProg = new Program(lightsPanel.getBulbsCount());
				progChanged();
			}
			return true;
		} catch( Exception e ) {
			e.printStackTrace();
			ConfirmationDialog.exception(null, e);
			return false;
		}
	}

	/**
	 * 
	 */
	private void progChanged() throws Exception {
		if( currentProg == null )
			return;
		
		if( currentProg.getFramesCount() > 1 )
			for(int i=1; i<currentProg.getFramesCount(); i++)
				currentProg.getFrame(i).setDiffCount(
						Program.buildColorsDiff(
								currentProg.getFrame(i).getColors(),
								currentProg.getFrame(i-1).getColors()
								).size() );
		
		model.setProgram(currentProg);
		model.fireTableDataChanged();
		table.repaint();
		
		progLabel.setText(String.format("<html>Программа &laquo;%s%s%s&raquo;<html>", (currentProg.isHasUsavedData()?"<i>":""), currentProg.getName(), (currentProg.isHasUsavedData()?"</i>":"") ));
	}
	
	/**
	 * 
	 */
	private synchronized void showBulbs() {
		if( !connectButton.isSelected() )
			return;

		Color[] colors = lightsPanel.getColors();
		Map<Integer,Color> map = Program.buildColorsDiff(colors, lastColors);
		byte[] bytes = new byte[map.size()*4];
		int i = 0;
		for(Entry<Integer,Color> entry: map.entrySet()) {
			Bulb bulb = lightsPanel.getBulb( entry.getKey() );
			byte[] b = bulb.asBytes();
			System.arraycopy(b, 0, bytes, i, b.length);
			i += b.length;
		}

		if( bytes.length > 0 ) {
		
//			String s = "";
//			for(byte b: bytes)
//				s += String.format("%02X", b);
//			System.out.println(""+s);
			
			try {

				if( serial != null && serial.isOpened() )
					serial.writeBytes(bytes);
				
			} catch (Exception e) {
				e.printStackTrace();
				ConfirmationDialog.exception(null, e);
			}
		}
		
		lastColors = colors;
	}
	
	/**
	 * 
	 */
	private synchronized boolean startUpdate() {
		if( programmaticalyChanging )
			return false;
		programmaticalyChanging = true;
		return true;
	}
	
	/**
	 * 
	 */
	private synchronized void finishUpdate() {
		programmaticalyChanging = false;
	}
	
	/**
	 * 
	 */
	public void update() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				MainForm.this.validate();
				MainForm.this.repaint();
			}
		});
	}
	
	
	/**
	 * 
	 */
	private void initSlider() {
		lightsSlider.setMaximum(
				(
						lockCheck.isSelected()
						? (int)Math.ceil( (float)lightsPanel.getBulbsCount() / (float)lightsPanel.getCountInGroup() )
						: lightsPanel.getBulbsCount()
				) - 1 );
		lightsSlider.setValue(0);
	}
	
	/**
	 * 
	 */
	private JLabel createLabel(String text) {
		return createLabel(text, false);
	}
	private JLabel createLabel(String text, boolean isBold) {
		JLabel label = new JLabel(text);
		label.setFont(new Font("Arial", isBold ? Font.BOLD : Font.PLAIN, 12));
		return label;
	}
	
	private void saveProg(boolean saveAs) {
		
		checkAndInitProg();
		
		try {
		
			if( currentProg.getFile() == null || saveAs ) {
			
				JFileChooser fc = new JFileChooser( WinRegistry.readString(WinRegistry.HKEY_CURRENT_USER, AppRegKey, SaveLastDirRegKey) ) {
					private static final long serialVersionUID = 1L;
					@Override
					public void approveSelection() {
						if( getSelectedFile() != null && getSelectedFile().exists() ) {
							int result = JOptionPane.showConfirmDialog(this,
			                        "Файл программы уже существует. Перезаписать?", "Файл существует",
			                        JOptionPane.YES_NO_CANCEL_OPTION);
			                switch (result) {
			                case JOptionPane.YES_OPTION:
			                    super.approveSelection();
			                    return;
			                case JOptionPane.CANCEL_OPTION:
			                    cancelSelection();
			                    return;
			                default:
			                    return;
			                }
						}
						super.approveSelection();
					}
				};
				fc.setDialogTitle("Выберите каталог где сохранить файл");
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fc.setSelectedFile(new File(currentProg.getName()+".lcprogz"));
				
				if( fc.showSaveDialog(MainForm.this) == JFileChooser.APPROVE_OPTION && fc.getSelectedFile() != null)
					currentProg.save(fc.getSelectedFile());
			} else
				currentProg.save(currentProg.getFile());

			progChanged();
			
		} catch (Exception e) {
			e.printStackTrace();
			ConfirmationDialog.exception(null, e);
		}
	}
	
	/**
	 * 
	 */
	@Override
	public void stateChanged(ChangeEvent ce) {
		if( !startUpdate() )
			return;
		try {
			
			if( ce.getSource().equals(lightsSlider) ) {
				
				// придерживаться групп?
				if( lockCheck.isSelected() ) {
					// выключить незаблокированные
					for(int i=0; i<lightsPanel.getBulbsCount(); i++)
						if( !lightsPanel.getBulb(i).isLocked() )
							lightsPanel.getBulb(i).setColor(Color.BLACK);
					// засветить все лампы в группе
					for(int i=0; i<lightsPanel.getCountInGroup(); i++) {
						int n = lightsSlider.getValue()*lightsPanel.getCountInGroup()+i;
						if( n < lightsPanel.getBulbsCount() )
							if( !lightsPanel.getBulb(n).isLocked() )
								lightsPanel.getBulb(n).setColor(colorButton.getColor());
					}
				} else {
					// включить или выключить незаблокированные лампы
					for(int i=0; i<lightsPanel.getBulbsCount(); i++) {
						Bulb b = lightsPanel.getBulb(i);
						if( !b.isLocked() )
							if( i == lightsSlider.getValue() )
								lightsPanel.getBulb(i).setColor(colorButton.getColor());
							else
								lightsPanel.getBulb(i).setColor(Color.BLACK);
					}
				}
				
				lightsPanel.update();
				showBulbs();
			}
			else if( ce.getSource().equals(groupSpinner) ) {
				lightsPanel.setCountInGroup((int)groupSpinner.getValue());
				initSlider();
				lightsPanel.update();
				update();
			}
			
		} finally {
			finishUpdate();
		}
		
	}

	/**
	 * 
	 */
	@Override
	public void bulbClicked(Bulb bulb) {
		setBulbColor(bulb, colorButton.getColor());
	}

	private void setBulbColor(Bulb bulb, Color color) {
		if( !startUpdate() )
			return;
		try {
			for(Bulb b: getBulbs(bulb)) {
				b.setColor(color);
				b.setLocked(true);
			}
			lightsPanel.update();
			update();
			showBulbs();
		} finally {
			finishUpdate();
		}
	}

	/**
	 * 
	 */
	@Override
	public void popupTriggered(MouseEvent me, Bulb bulb) {
		if( bulb != null ) {
			bulbPopupMenu.setBulb(bulb);
			bulbPopupMenu.show(me.getComponent(), me.getX(), me.getY());
		}
	}
	
	/**
	 * Получить бульбу или все остальные с учётом группировки 
	 */
	private Bulb[] getBulbs(Bulb b) {
		if( !lockCheck.isSelected() ) // группы нет, поэтому и вернём её саму
			return new Bulb[] {b};
		List<Bulb> list = new ArrayList<Bulb>(); // лист, а не массив, потому как вначале не знаем точного размера массива -- это может быть конец ленты и там не хватает ламп для полной группы
		int gc = (int)groupSpinner.getValue();
		int gn = (int)((Math.ceil((float)(b.getN()+1) / (float)gc) - 1) * gc);
		for(int i=0; i<gc; i++) {
			int n = gn + i;
			if( n < lightsPanel.getBulbsCount() )
				list.add(lightsPanel.getBulb(n));
		}
		return list.toArray(new Bulb[list.size()]);
	}
	
	/**
	 * 
	 */
	@Override
	public void itemStateChanged(ItemEvent ie) {
		if( !startUpdate() )
			return;
		try {
			if( ie.getSource().equals(lockCheck) ) {
				initSlider();
				lightsPanel.update();
				update();
			}
		} finally {
			finishUpdate();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent lse) {
		if( lse.getValueIsAdjusting() || !startUpdate() )
			return;
		try {

			if( lse.getSource().equals(table.getSelectionModel()) ) {
				
		        if( table.getSelectedRowCount() > 0 ) {
		        	Frame frame = model.get(table.convertRowIndexToModel(table.getSelectedRow()));
		        	lightsPanel.setColors(frame.getColors());
		        	delaySpinner.setValue(frame.getDelayAfter());
					showBulbs();
		        } else {
		        	lightsPanel.turnOffAll();
		        }
				
				toggleActions();
			}
			
		} finally {
			finishUpdate();
		}
	}
	
	public void toggleActions() {
		
		;
	}

	@Override
	public void mouseClicked(MouseEvent me) {
	}

	@Override
	public void mousePressed(MouseEvent me) {
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		
		if( me.getSource().equals(table) ) {
			
	    	// где отпустили мышь
	    	int row = table.rowAtPoint(me.getPoint());
	    	// если нет никакого выделения -- делаем его чтобы хоть что-то было выделено
	    	if( !table.isRowSelected(row) ) {
	    		table.getSelectionModel().setSelectionInterval(row, row);
				toggleActions();
	    	}
	        if( row < 0 ) {
	        	table.clearSelection();
				toggleActions();
	        } else if( table.getSelectedRowCount() == 1 && me.getClickCount() == 2 ) {
	        	;
	        } 
	        if( me.isPopupTrigger() ) {
        		//int col = table.columnAtPoint( me.getPoint() );
        		//popupMenu.show(me.getComponent(), me.getX(), me.getY());
	        }
		}
	}

	@Override
	public void mouseEntered(MouseEvent me) {
	}
	@Override
	public void mouseExited(MouseEvent me) {
	}

	@Override
	public void windowClosed(WindowEvent we) {
		if( serial != null && serial.isOpened() && connectButton.isSelected() ) {
			try {
				serial.writeBytesRaw(new byte[]{Serial.DATA_TERMINATOR, Serial.RETURN_TO_PLAYER});
				Thread.sleep(100);
				serial.close();
			} catch (SerialPortException | InterruptedException e) {
				e.printStackTrace();
			} 
		}
	}

	@Override
	public void windowActivated(WindowEvent e) {
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
