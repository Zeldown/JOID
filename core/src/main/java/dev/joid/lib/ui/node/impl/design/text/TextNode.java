package dev.joid.lib.ui.node.impl.design.text;

import java.util.function.Supplier;

import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.draw.text.utils.TextMode;
import dev.joid.lib.font.dto.FontBounds;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.utils.signal.Signal;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public class TextNode extends Node {

	private Text text;
	private TextMode mode;

	private boolean initialized;
	private double initialWidth;
	private double initialHeight;

	protected TextNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.text = null;
		this.mode = TextMode.NORMAL;
	}

	public static @NonNull TextNode create(final double x, final double y) {
		return new TextNode(x, y, 0, 0);
	}

	public static @NonNull TextNode create(final double x, final double y, final double width, final double height) {
		return new TextNode(x, y, width, height);
	}

	@Override
	public void init(final @NonNull UI ui) {
		if (!this.initialized) {
			this.initialWidth = super.getWidth();
			this.initialHeight = super.getHeight();
			this.initialized = true;
		}

		if (this.text == null || this.text.isEmpty() || this.text.getText().isEmpty()) {
			return;
		}

		if (this.mode == TextMode.NORMAL) {
			if (this.initialWidth == 0) {
				super.width(this.text.getWidth());
			}

			if (this.initialHeight == 0) {
				super.height(this.text.getHeight());
			}
		} else if (this.mode == TextMode.OVERFLOW) {
			if (this.initialHeight == 0) {
				super.height(this.text.getHeight());
			}
		} else if (this.mode == TextMode.SPLIT) {
			super.height(DrawUtils.TEXT.getLines(super.getWidth(), this.text).stream().mapToDouble(Text::getHeight).sum());
		}
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.text == null || this.text.isEmpty() || this.text.getText().isEmpty()) {
			return;
		}

		final FontBounds bounds = DrawUtils.TEXT.drawText(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.text, this.mode);
		if (this.mode == TextMode.NORMAL) {
			if (this.initialWidth == 0) {
				super.width(bounds.getWidth());
			}

			if (this.initialHeight == 0) {
				super.height(bounds.getHeight());
			}
		} else if (this.mode == TextMode.OVERFLOW) {
			if (this.initialHeight == 0) {
				super.height(bounds.getHeight());
			}
		} else if (this.mode == TextMode.SPLIT) {
			super.height(bounds.getHeight());
		}
	}

	@Override
	public void drawSkeleton(final double mouseX, final double mouseY) {
		if (super.getWidth() == 0 && this.text != null && !this.text.isEmpty()) {
			super.width(this.text.getWidth());
		}

		if (super.getHeight() == 0 && this.text != null && !this.text.isEmpty()) {
			super.height(this.text.getHeight());
		}

		super.drawSkeleton(mouseX, mouseY);
	}

	public final <T extends TextNode> @NonNull T text(final Text text) {
		return this.text(Signal.from(text));
	}

	public final <T extends TextNode> @NonNull T text(final @NonNull Supplier<Text> text) {
		return super.follow("text", text, value -> this.text = value);
	}

	public final <T extends TextNode> @NonNull T mode(final @NonNull TextMode mode) {
		return this.mode(Signal.from(mode));
	}

	public final <T extends TextNode> @NonNull T mode(final @NonNull Supplier<@NonNull TextMode> mode) {
		return super.follow("mode", mode, value -> this.mode = value);
	}

	public final <T extends TextNode> @NonNull T reset() {
		this.initialized = true;
		this.initialWidth = super.getWidth();
		this.initialHeight = super.getHeight();
		return (T) this;
	}

}