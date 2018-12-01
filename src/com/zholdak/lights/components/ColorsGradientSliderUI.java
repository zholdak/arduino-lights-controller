package com.zholdak.lights.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;

import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

public final class ColorsGradientSliderUI extends BasicSliderUI {

    private static float[] fracs = {0.0f, 0.2f, 0.4f, 0.6f, 0.8f, 1.0f};
    private LinearGradientPaint lgp;

    public ColorsGradientSliderUI(JSlider slider) {
        super(slider);
    }

    @Override
    public void paintTrack(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Rectangle tr = trackRect;
        Point2D start = new Point2D.Float(tr.x, tr.y);
        Point2D end = new Point2D.Float(tr.width, tr.height);
        Color[] colors = {Color.MAGENTA, Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW, Color.RED};
        if( !this.slider.isEnabled() ) {
        	for( int i=0; i<colors.length; i++) {
        		float[] hsv = Color.RGBtoHSB( colors[i].getRed(), colors[i].getGreen(), colors[i].getBlue(), null);
        		colors[i] = new Color( Color.HSBtoRGB(hsv[0], 0.25f, hsv[2]));
        	}
        }
        lgp = new LinearGradientPaint(start, end, fracs, colors);
        g2d.setPaint(lgp);
        g2d.fillRect(tr.x, tr.y + 3, tr.width, tr.height - 8);
    }

    @Override
    public void paintFocus(Graphics g) {
    	// doing nothing to remove focus border
    }
    
//    @Override
//    public void paintThumb(Graphics g) {
//        Graphics2D g2d = (Graphics2D) g;
//        g2d.setRenderingHint(
//            RenderingHints.KEY_ANTIALIASING,
//            RenderingHints.VALUE_ANTIALIAS_ON);
//        Rectangle tr = thumbRect;
//        int tw2 = tr.width / 2;
//        g2d.setColor(Color.white);
//        g2d.drawLine(tr.x+1, tr.y+1, tr.x + tr.width, tr.y+1);
//        g2d.drawLine(tr.x+1, tr.y, tr.x + tw2 + 1, tr.y + tr.height - 1);
//        g2d.drawLine(tr.x + tr.width, tr.y, tr.x + tw2 + 1, tr.y + tr.height - 1);
//        g2d.setColor(Color.black);
//        g2d.drawLine(tr.x, tr.y, tr.x + tr.width - 1, tr.y);
//        g2d.drawLine(tr.x, tr.y, tr.x + tw2, tr.y + tr.height - 1);
//        g2d.drawLine(tr.x + tr.width - 1, tr.y, tr.x + tw2, tr.y + tr.height - 1);
//    }
}

