package dev.joid.lib.ui.node.impl.control.checkbox;

import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class CheckboxNode extends BaseCheckboxNode {

	private Node box;
	private Node mark;

	protected CheckboxNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		super.<CheckboxNode>require("mark", "give it one with mark(...)", node -> node.mark != null);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.box != null) {
			this.box.layoutPosition(0D, 0D).layoutSize(super.getWidth(), super.getHeight());
		}

		this.mark.visible(super.isChecked()).fit(super.getWidth(), super.getHeight());
		this.mark.layoutPosition((super.getWidth() - this.mark.getWidth()) / 2D, (super.getHeight() - this.mark.getHeight()) / 2D);
	}

	public final <T extends CheckboxNode> @NonNull T box(final @NonNull Node box) {
		this.box = super.part(this.box, box.zindex(-1));
		return (T) this;
	}

	public final <T extends CheckboxNode> @NonNull T mark(final @NonNull Node mark) {
		this.mark = super.part(this.mark, mark.visible(super.isChecked()));
		return (T) this;
	}

}