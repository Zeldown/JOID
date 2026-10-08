package dev.joid.showcase;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.structure.checkbox.CheckboxNode;

public class ShowSwitchNode extends CheckboxNode {

	private final Color on;

	private double knob;
	private long   last;

	protected ShowSwitchNode(final double x, final double y, final Color on) {
		super(x, y, 76, 42);
		this.on = on;
	}

	public static ShowSwitchNode create(final double x, final double y, final Color on) {
		return new ShowSwitchNode(x, y, on);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		final long now = BridgeHandler.CLOCK.get().currentTimeMillis();
		final double target = super.isChecked() ? 1D : 0D;
		final double delta = this.last == 0L ? 10D : (now - this.last) / 1000D;
		this.last = now;
		this.knob += (target - this.knob) * (1D - Math.exp(-delta * 16D));
		final float k = (float) this.knob;
		if (k > 0.01F) {
			DrawUtils.SHAPE.drawShadow(super.getX(), super.getY(), 76D, 42D, this.on.copyAlpha(0.7F * k), 21F, 18F);
		}
		DrawUtils.SHAPE.drawRoundedRect(super.getX(), super.getY(), 76D, 42D, Color.WHITE.copyAlpha(0.14F).to(this.on, k), 21F);
		DrawUtils.SHAPE.drawCircle(super.getX() + 21D + 34D * this.knob, super.getY() + 21D, Color.WHITE, 16D);
	}

}