package com.zholdak.lights.classes;

import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractButton;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public final class JXFlatImageDropdownButton extends JToggleButton {

	private static final long serialVersionUID = 1L;

	private static MouseListener flatButtonMouseAdapter = new MouseAdapter() {

		public void mouseEntered(MouseEvent e) {
			Object source = e.getSource();
			if (source instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) source;
				button.setContentAreaFilled(true);
			}
		}
		
		public void mouseExited(MouseEvent e) {
			Object source = e.getSource();
			if (source instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) source;
				if( !button.isSelected() )
					button.setContentAreaFilled(false);
			}
		}
	};
	
	public JXFlatImageDropdownButton(AbstractActionEx action, final JPopupMenu popupMenu) {
		
		addMouseListener(flatButtonMouseAdapter);
		setContentAreaFilled(false);
		setHideActionText(true);
		setMargin(new Insets(3,3,3,3));
		//setFocusable(false);
		setAction(action);
		
		if( popupMenu == null )
			throw new IllegalArgumentException("popupMenu can not be null");

		if( popupMenu.getComponentCount() == 1 )
			setAction( action ); // установить action на кнопку, если в меню только один элемент
		else {
			popupMenu.addPopupMenuListener(new PopupMenuListener() {
				
				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				}
				
				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
					setContentAreaFilled(false);
					setSelected(false);
				}
				
				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {
					setContentAreaFilled(false);
					setSelected(false);
				}
			});
			addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						popupMenu.show(JXFlatImageDropdownButton.this, 0, JXFlatImageDropdownButton.this.getHeight());
					}
				}
			});
		}		
		setIcon(action.getSmallIcon());
		setToolTipText(action.getLongDescription());
	}
}
