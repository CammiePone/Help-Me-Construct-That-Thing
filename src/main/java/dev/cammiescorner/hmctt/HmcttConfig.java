package dev.cammiescorner.hmctt;

import eu.midnightdust.lib.config.MidnightConfig;

public class HmcttConfig extends MidnightConfig {
	@Entry(min = 0, max = 1) public static float blockTransparency = 0.6F;
	@Entry(min = 0, max = 1) public static float blockScale = 1F;
	@Entry public static boolean animateBlocks = false;
	@Entry public static String wrongBlockColour = "#ff0000";
//	public static String toolItem = "minecraft:feather";
}
