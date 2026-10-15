package dev.joid.showcase;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.control.toggle.ToggleNode;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;

public class ShowThemeNode extends ToggleNode<Boolean, Boolean> {

	private static final Color MOON        = Color.decode("#F5F3FF").lock();
	private static final Color TRACK_DARK  = Color.decode("#2A2846").lock();
	private static final Color TRACK_LIGHT = Color.decode("#E4E4EE").lock();

	protected ShowThemeNode(final double x, final double y) {
		super(x, y, 112, 56);
		super.speed(0.15F);
		super.track(ContainerNode.create(0, 0, 0, 0).layer((node, mouseX, mouseY) -> DrawUtils.SHAPE.drawRoundedRect(0D, 0D, 112D, 56D, this.getTrackColor(), 28F)));
		super.knob(ContainerNode.create(0, 0, 56, 56).layer((node, mouseX, mouseY) -> {
			final float k = super.getKnobProgress();
			DrawUtils.SHAPE.drawShadow(6D, 6D, 44D, 44D, ShowUI.AMBER.to(ShowUI.VIOLET, k).withAlpha(0.7F), 22F, 18F);
			DrawUtils.SHAPE.drawCircle(28D, 28D, ShowUI.AMBER.to(ShowThemeNode.MOON, k), 20D);
			if (k > 0.01F) {
				DrawUtils.SHAPE.drawCircle(28D + 9D * k, 28D - 7D * k, this.getTrackColor(), 15D * k);
			}
		}));
	}

	public static ShowThemeNode create(final double x, final double y) {
		return new ShowThemeNode(x, y);
	}

	private Color getTrackColor() {
		return ShowThemeNode.TRACK_LIGHT.to(ShowThemeNode.TRACK_DARK, super.getKnobProgress());
	}

}