package dev.joid.showcase;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.structure.container.ContainerNode;
import dev.joid.lib.ui.node.impl.structure.toggle.ToggleNode;

public class ShowSwitchNode extends ToggleNode<Boolean, Boolean> {

	private final Color on;

	protected ShowSwitchNode(final double x, final double y, final Color on) {
		super(x, y, 76, 42);
		this.on = on;
		super.track(ContainerNode.create(0, 0, 0, 0).layer((node, mouseX, mouseY) -> {
			final float k = super.getKnobProgress();
			if (k > 0.01F) {
				DrawUtils.SHAPE.drawShadow(0D, 0D, 76D, 42D, this.on.withAlpha(0.7F * k), 21F, 18F);
			}
			DrawUtils.SHAPE.drawRoundedRect(0D, 0D, 76D, 42D, Color.WHITE.withAlpha(0.14F).to(this.on, k), 21F);
		}));
		super.knob(ContainerNode.create(0, 0, 42, 42).layer((node, mouseX, mouseY) -> DrawUtils.SHAPE.drawCircle(21D, 21D, Color.WHITE, 16D)));
	}

	public static ShowSwitchNode create(final double x, final double y, final Color on) {
		return new ShowSwitchNode(x, y, on);
	}

}