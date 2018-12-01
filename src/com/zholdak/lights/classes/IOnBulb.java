package com.zholdak.lights.classes;

import java.awt.event.MouseEvent;

public interface IOnBulb {

	public void bulbClicked(Bulb bulb);
	public void popupTriggered(MouseEvent me, Bulb bulb);
}
