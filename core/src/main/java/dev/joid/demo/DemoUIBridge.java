package dev.joid.demo;

import dev.joid.demo.ui.UIDemoChoice;
import dev.joid.internal.JOID;
import dev.joid.internal.font.DevFont;
import dev.joid.lib.bridge.ui.WindowUIBridge;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.TextInfo;

public class DemoUIBridge extends WindowUIBridge {

	private static final Color BACKGROUND = new Color(50, 50, 50);

	public DemoUIBridge() {
		super.background(DemoUIBridge.BACKGROUND);
	}

	public void start() {
		JOID.open(new UIDemoChoice());
		super.load();
	}

	@Override
	public TextInfo getHoverInfo() {
		return DevFont.MONTSERRAT != null ? TextInfo.create(DevFont.MONTSERRAT, 20, Color.WHITE) : null;
	}

}