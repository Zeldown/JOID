package dev.joid.showcase;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.structure.container.ContainerNode;
import dev.joid.lib.ui.node.impl.structure.toggle.ToggleNode;

public class ShowThemeNode extends ToggleNode<Boolean, Boolean> {

	private double knob;

	protected ShowThemeNode(final double x, final double y) {
		super(x, y, 112, 56);
		super.track(ContainerNode.create(0, 0, 0, 0).layer((node, mouseX, mouseY) -> {
			this.knob = super.getUi().lerpByFramerate(this.knob, super.isToggle() ? 1D : 0D, 0.5D, 0.001D, true);
			DrawUtils.SHAPE.drawRoundedRect(0D, 0D, 112D, 56D, this.getTrackColor(), 28F);
		}));
		super.knob(ContainerNode.create(0, 0, 56, 56).layer((node, mouseX, mouseY) -> {
			final float k = (float) this.knob;
			final double cx = 28D + 56D * this.knob - node.getX();
			DrawUtils.SHAPE.drawShadow(cx - 22D, 6D, 44D, 44D, ShowUI.AMBER.to(ShowUI.VIOLET, k).withAlpha(0.7F), 22F, 18F);
			DrawUtils.SHAPE.drawCircle(cx, 28D, ShowUI.AMBER.to(Color.decode("#F5F3FF"), k), 20D);
			if (k > 0.01F) {
				DrawUtils.SHAPE.drawCircle(cx + 9D * k, 28D - 7D * k, this.getTrackColor(), 15D * k);
			}
		}));
	}

	public static ShowThemeNode create(final double x, final double y) {
		return new ShowThemeNode(x, y);
	}

	private Color getTrackColor() {
		return Color.decode("#E4E4EE").to(Color.decode("#2A2846"), (float) this.knob);
	}

}