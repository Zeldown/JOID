package dev.joid.lib.ui.node.impl.control.scrollbar;

import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class ScrollbarNode extends BaseScrollbarNode {

	private Node track;
	private Node thumb;

	protected ScrollbarNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		super.<ScrollbarNode>require("thumb", "give it one with thumb(...)", node -> node.thumb != null);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		super.draw(mouseX, mouseY);
		if (this.track != null) {
			this.track.layoutPosition(0D, 0D).layoutSize(super.getWidth(), super.getHeight());
		}

		final double length = super.getThumbLength();
		if (super.isHorizontal()) {
			this.thumb.layoutPosition((super.getWidth() - length) * super.getProgress(), 0D).layoutSize(length, super.getHeight());
		} else {
			this.thumb.layoutPosition(0D, (super.getHeight() - length) * super.getProgress()).layoutSize(super.getWidth(), length);
		}
	}

	public final <T extends ScrollbarNode> @NonNull T track(final @NonNull Node track) {
		this.track = super.part(this.track, track);
		return (T) this;
	}

	public final <T extends ScrollbarNode> @NonNull T thumb(final @NonNull Node thumb) {
		this.thumb = super.part(this.thumb, thumb);
		return (T) this;
	}

}