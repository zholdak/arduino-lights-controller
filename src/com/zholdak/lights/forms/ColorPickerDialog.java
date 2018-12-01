package com.zholdak.lights.forms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.zholdak.lights.classes.AbstractActionEx;
import com.zholdak.lights.classes.IOnColorChoosing;

public class ColorPickerDialog extends JDialog implements ChangeListener {
	
	private static final long serialVersionUID = 1L;

	private Component parent;
	
	protected JPanel contentPanel;
	
	protected JPanel buttonPane;
	protected JButton okButton;
	protected JButton cancelButton;
	
	private JColorChooser colorChooser;
	private BulbColorPreviewPane colorPreviewPane;
	
	private Color selectedColor = null;
	
	private IOnColorChoosing onColorChoosing;
	
	protected AbstractActionEx okAction = new AbstractActionEx("OK", "OK") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent e) {
			selectedColor = colorChooser.getColor();
			closeDialog();
		}
	};

	protected AbstractActionEx closeAction = new AbstractActionEx("Отмена", "Отмена") {
		private static final long serialVersionUID = 1L;
		public void onActionPerformed(ActionEvent e) {
			closeDialog();
		}
	};
	
	public class BulbColorPreviewPane extends JPanel {
		private static final long serialVersionUID = 1L;
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D)g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g2d.setColor(Color.BLACK);
			g.fillRect(0, 0, getWidth(), getHeight());
			
			g2d.setColor(colorChooser.getColor());
			g2d.fillOval(10, 10, getWidth() - 20, getWidth() - 20);
		}
		@Override
		public Dimension getPreferredSize() {
			return new Dimension(100, 100);
		}
		@Override
		public void setForeground(Color fg) {
			super.setForeground(fg);
		}
	}
	
	public ColorPickerDialog(Component parent, Color initialColor, IOnColorChoosing onColorChoosing) {
		
		this.parent = parent;
		this.onColorChoosing = onColorChoosing;
		
		setModal(true);
		setResizable(false);
		setTitle("Выбор цвета");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		getContentPane().setLayout(new BorderLayout(0,0));
		
		contentPanel = new JPanel();
		//contentPanel.setN
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.setLayout(new BorderLayout(0,0));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		colorPreviewPane = new BulbColorPreviewPane();
		
		colorChooser = new JColorChooser(initialColor);
		colorChooser.setPreviewPanel(colorPreviewPane);
		
		colorChooser.getSelectionModel().addChangeListener(this);
		AbstractColorChooserPanel[] panels=colorChooser.getChooserPanels();
		for( AbstractColorChooserPanel p: panels ) {
			if( p.getDisplayName().equals("HSL") )
				colorChooser.removeChooserPanel(p);
		}
		
		contentPanel.add(colorChooser, BorderLayout.CENTER);

		buttonPane = new JPanel();
		buttonPane.setBorder(new MatteBorder(1, 0, 0, 0, (Color) UIManager.getColor("Button.darkShadow")));
		FlowLayout fl_buttonPane = new FlowLayout(FlowLayout.RIGHT);
		fl_buttonPane.setHgap(10);
		buttonPane.setLayout(fl_buttonPane);
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		
		okButton = new JButton(okAction);
		okButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
		buttonPane.add(okButton);

		cancelButton = new JButton(closeAction);
		cancelButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
		buttonPane.add(cancelButton);
		
		pack();
	}

	public Color getSelectedColor() {
		return selectedColor;
	}
	
	public ColorPickerDialog showDialog() {
		setLocationRelativeTo(parent);
		setVisible(true);
		return this;
	}
	
	public void closeDialog() {
		ColorPickerDialog.this.dispose();
	}
	
	@Override
	public void stateChanged(ChangeEvent ce) {
		if( ce.getSource().equals(colorChooser.getSelectionModel()) ) {
			if( onColorChoosing != null )
				onColorChoosing.colorChanged(colorChooser.getColor());
		}
	}
}
