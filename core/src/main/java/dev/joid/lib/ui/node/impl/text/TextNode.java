package dev.joid.lib.ui.node.impl.text;

import java.util.function.Supplier;

import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.TextMode;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.FontBounds;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class TextNode extends Node {

	private Text text;
	private TextMode mode;

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
		this.measure();
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.hasText()) {
			final FontBounds bounds = DrawUtils.TEXT.drawText(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.text, this.mode);
			this.fitText(bounds.getWidth(), bounds.getHeight());
		}
	}

	@Override
	public void drawSkeleton(final double mouseX, final double mouseY) {
		this.measure();
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

	private boolean hasText() {
		return this.text != null && !this.text.isEmpty() && !this.text.getText().isEmpty();
	}

	private void measure() {
		if (this.hasText()) {
			this.fitText(this.text.getWidth(), this.mode == TextMode.SPLIT ? DrawUtils.TEXT.getLines(super.getWidth(), this.text).stream().mapToDouble(Text::getHeight).sum() : this.text.getHeight());
		}
	}

	private void fitText(final double width, final double height) {
		if (this.mode == TextMode.SPLIT) {
			super.layoutSize(super.getWidth(), height);
		} else {
			super.fit(this.mode == TextMode.NORMAL ? width : super.getWidth(), height);
		}
	}

}