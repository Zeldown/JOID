package dev.joid.showcase;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.design.textfield.TextFieldNode;

public class ShowFieldNode extends TextFieldNode {

	private final Color accent;

	private double focus;
	private long   last;

	protected ShowFieldNode(final double x, final double y, final double width, final Color accent) {
		super(x, y, width, 68);
		this.accent = accent;
	}

	public static ShowFieldNode create(final double x, final double y, final double width, final Color accent) {
		return new ShowFieldNode(x, y, width, accent);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		final long now = BridgeHandler.CLOCK.get().currentTimeMillis();
		final double delta = this.last == 0L ? 10D : (now - this.last) / 1000D;
		this.last = now;
		this.focus += ((super.isFocused() ? 1D : 0D) - this.focus) * (1D - Math.exp(-delta * 12D));
		final float f = (float) this.focus;
		if (f > 0.01F) {
			DrawUtils.SHAPE.drawShadow(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.accent.copyAlpha(0.55F * f), 18F, 22F);
		}
		DrawUtils.SHAPE.drawRoundedRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.decode("#151327").to(Color.decode("#1D1838"), f), 18F);
		DrawUtils.SHAPE.drawRoundedBorder(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.WHITE.copyAlpha(0.14F).to(this.accent, f), 18F, 2D);
		super.draw(mouseX, mouseY);
	}

}