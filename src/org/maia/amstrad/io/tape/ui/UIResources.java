package org.maia.amstrad.io.tape.ui;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class UIResources {

	private static String iconFolder = "/icons/";

	public static Icon amstradIcon = loadIcon(iconFolder + "amstrad.png");

	public static String openCodeInspectorLabel = "Inspect code";

	public static Icon openCodeInspectorIcon = loadIcon(iconFolder + "code-trace-glass32.png");

	public static String clearSelectionLabel = "Clear selection";

	public static Icon clearSelectionIcon = loadIcon(iconFolder + "unselect32.png");

	public static Dimension sourceCodeViewSize = new Dimension(1024, 600);

	public static Dimension byteCodeViewSize = new Dimension(1024, 600);

	public static Dimension sourceCodeInspectorViewSize = new Dimension(1024, 300);

	public static Dimension byteCodeInspectorViewSize = new Dimension(1024, 300);

	public static Dimension audioInspectorViewSize = new Dimension(1024, 284);

	public static int audioProfileViewHeight = 240;

	public static int audioPositionViewHeight = 24;

	private static float[] hsbComps = new float[3];

	private static float[] rgbaComps = new float[4];

	public static Color adjustBrightness(Color color, double factor) {
		if (factor == 0)
			return color;
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsbComps);
		double brightness = hsbComps[2];
		double darkness = 1.0 - brightness;
		if (factor >= 0) {
			// increase brightness
			brightness = 1.0 - darkness * (1.0 - factor);
		} else {
			// increase darkness
			darkness = 1.0 - brightness * (1.0 + factor);
			brightness = 1.0 - darkness;
		}
		int rgba = (color.getAlpha() << 24)
				| (Color.HSBtoRGB(hsbComps[0], hsbComps[1] * (float) (Math.min(1.0, 1.0 - factor)), (float) brightness) & 0x00ffffff);
		return new Color(rgba, true);
	}

	public static Color setTransparency(Color color, double transparency) {
		color.getRGBColorComponents(rgbaComps);
		rgbaComps[3] = (float) (1.0 - transparency);
		return new Color(rgbaComps[0], rgbaComps[1], rgbaComps[2], rgbaComps[3]);
	}

	private static Icon loadIcon(String resourceName) {
		return new ImageIcon(UIResources.class.getResource(resourceName));
	}

}