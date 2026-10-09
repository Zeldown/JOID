package dev.joid.showcase;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.structure.checkbox.CheckboxNode;

public class ShowThemeNode extends CheckboxNode {

	private long   last;
	private double knob;

	protected ShowThemeNode(final double x, final double y) {
		super(x, y, 112, 56);
	}

	public static ShowThemeNode create(final double x, final double y) {
		return new ShowThemeNode(x, y);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		final long now = BridgeHandler.CLOCK.get().currentTimeMillis();
		final double delta = this.last == 0L ? 10D : (now - this.last) / 1000D;
		this.last = now;
		this.knob += ((super.isChecked() ? 1D : 0D) - this.knob) * (1D - Math.exp(-delta * 10D));
		final float k = (float) this.knob;
		final Color track = Color.decode("#E4E4EE").to(Color.decode("#2A2846"), k);
		final double cx = super.getX() + 28D + 56D * this.knob;
		final double cy = super.getY() + 28D;
		DrawUtils.SHAPE.drawRoundedRect(super.getX(), super.getY(), 112D, 56D, track, 28F);
		DrawUtils.SHAPE.drawShadow(cx - 22D, cy - 22D, 44D, 44D, ShowUI.AMBER.to(ShowUI.VIOLET, k).copyAlpha(0.7F), 22F, 18F);
		DrawUtils.SHAPE.drawCircle(cx, cy, ShowUI.AMBER.to(Color.decode("#F5F3FF"), k), 20D);
		if (k > 0.01F) {
			DrawUtils.SHAPE.drawCircle(cx + 9D * k, cy - 7D * k, track, 15D * k);
		}
	}

}