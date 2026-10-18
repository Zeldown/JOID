package dev.joid.demo;

import dev.joid.demo.ui.UIDemoChoice;
import dev.joid.internal.JOID;
import dev.joid.internal.font.DevFont;
import dev.joid.lib.bridge.ui.WindowUIBridge;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import lombok.NonNull;

public abstract class DemoUIBridge extends WindowUIBridge {

	private static final Color BACKGROUND = new Color(50, 50, 50).lock();

	private double interfaceScale;

	protected DemoUIBridge() {
		super.background(DemoUIBridge.BACKGROUND);
		this.interfaceScale = 1D;
	}

	public void start() {
		super.hoverInfo(TextInfo.create(DevFont.MONTSERRAT, 20).color(Color.WHITE));
		this.openMenu();
		super.load();
	}

	protected void openMenu() {
		JOID.open(new UIDemoChoice());
	}

	public final <T extends DemoUIBridge> @NonNull T interfaceScale(final double interfaceScale) {
		this.interfaceScale = interfaceScale;
		return (T) this;
	}

	@Override
	public double getInterfaceScale(final @NonNull UI ui) {
		return this.interfaceScale;
	}

}