package dev.joid.demo;

import dev.joid.demo.ui.UIDemoChoice;
import dev.joid.internal.JOID;
import dev.joid.internal.font.DevFont;
import dev.joid.lib.bridge.ui.WindowUIBridge;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import lombok.NonNull;

public class DemoUIBridge extends WindowUIBridge {

	private static final Color BACKGROUND = new Color(50, 50, 50).freeze();

	private double interfaceScale;

	public DemoUIBridge() {
		super.background(DemoUIBridge.BACKGROUND);
		this.interfaceScale = 1D;
	}

	public void start() {
		JOID.open(new UIDemoChoice());
		super.load();
	}

	public final <T extends DemoUIBridge> @NonNull T interfaceScale(final double interfaceScale) {
		this.interfaceScale = interfaceScale;
		return (T) this;
	}

	@Override
	public double getInterfaceScale(final @NonNull UI ui) {
		return this.interfaceScale;
	}

	@Override
	public TextInfo getHoverInfo() {
		return DevFont.MONTSERRAT != null ? TextInfo.create(DevFont.MONTSERRAT, 20, Color.WHITE) : null;
	}

}