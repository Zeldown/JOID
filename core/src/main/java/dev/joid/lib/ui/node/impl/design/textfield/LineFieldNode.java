package dev.joid.lib.ui.node.impl.design.textfield;

import java.util.function.Supplier;

import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.NodeEvent;
import dev.joid.lib.utils.align.Align;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class LineFieldNode<V> extends FieldNode<V> {

	private static final EventType<NodeEvent<?>> SUBMIT = EventType.create("onSubmit");

	private Align alignX;
	private Align alignY;

	private double xOffset;

	protected LineFieldNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.alignX = Align.START;
		this.alignY = Align.CENTER;

		super.marginHorizontal(2D);
		super.marginVertical(10D);
		super.cursorMargin(15D);
	}

	@Override
	protected void drawField() {
		final String text = super.getText();
		final TextInfo info = super.getInfo();
		if (super.getHeight() == 0) {
			super.height(info.ah(super.getMarginTop() + super.getMarginBottom()));
		}

		if (this.xOffset > super.getShownInfo().getWidth(text)) {
			super.decreaseCursor(0);
		}

		if (this.xOffset < 0D) {
			this.xOffset = 0D;
		}

		final boolean isPlaceholder = text.isEmpty() && !super.isFocused();
		double tmpTextY = super.getY() + super.getMarginTop();

		if (this.alignY.isCenter()) {
			tmpTextY = super.getY() + (super.getHeight() - info.getHeight()) / 2D;
		} else if (this.alignY.isEnd()) {
			tmpTextY = super.getY() + super.getHeight() - info.getHeight() - super.getMarginBottom();
		}

		final double textX = this.getTextX(super.getX(), isPlaceholder ? super.getPlaceholder() : text);
		final double textY = tmpTextY;
		final int[] tags = FieldNode.tags(text, super.getShownInfo());
		final FieldSelection selection = super.getSelection();
		super.getUi().mask(super.getX() + super.getMarginLeft(), super.getY(), super.getWidth() - super.getMarginLeft() - super.getMarginRight(), super.getHeight(), () -> {
			DrawUtils.TEXT.drawText(textX, textY, isPlaceholder ? super.getPlaceholder() : text, isPlaceholder ? super.getPlaceholderInfo() : super.getShownInfo(), Align.START, Align.START);

			if (super.isFocused()) {
				DrawUtils.SHAPE.drawRect(textX + this.getX(text, tags, super.getCursorPos()), textY, 2D, info.getHeight(), super.getCursorColor());
			}

			if (!selection.isEmpty()) {
				final double startX = textX + this.getX(text, tags, selection.getStart());
				DrawUtils.SHAPE.drawRect(startX, textY, textX + this.getX(text, tags, selection.getEnd()) - startX, info.getHeight(), FieldNode.SELECTION_COLOR);
			}
		});
	}

	@Override
	protected final boolean isMultiline() {
		return false;
	}

	@Override
	protected final boolean handleKey(final @NonNull Key key) {
		if (key != Key.ENTER && key != Key.NUMPAD_ENTER) {
			return false;
		}

		super.blurNow();
		super.fire(LineFieldNode.SUBMIT, () -> NodeEvent.create(this));
		return true;
	}

	@Override
	protected final int getPositionAt(final double mouseX, final double mouseY) {
		final String text = super.getText();
		final int[] tags = FieldNode.tags(text, super.getShownInfo());
		final double x = mouseX - this.getTextX(super.getAbsoluteX(), text);
		int previous = 0;
		for (int i = 0; i < text.length(); i++) {
			if (tags[i] != -1) {
				continue;
			}

			final double charWidth = super.getShownInfo().getWidth(FieldNode.opened(text, tags, i) + text.charAt(i));
			if (x < this.getX(text, tags, i) + charWidth / 2D) {
				return previous;
			}

			previous = i + 1;
		}

		return previous;
	}

	@Override
	protected final void followCursorForward() {
		if (!this.isScrolling(super.getText())) {
			this.xOffset = 0D;
			return;
		}

		final double textX = this.getTextX(super.getX(), super.getText());
		final double cursorX = textX + this.getX(super.getText(), FieldNode.tags(super.getText(), super.getShownInfo()), super.getCursorPos());
		if (cursorX > super.getX() + super.getWidth() - super.getMarginRight() - super.getCursorMargin()) {
			this.xOffset += cursorX - (super.getX() + super.getWidth() - super.getMarginRight() - super.getCursorMargin());
		}
	}

	@Override
	protected final void followCursorBackward() {
		if (super.getCursorPos() == 0) {
			this.xOffset = 0D;
			return;
		}

		if (!this.isScrolling(super.getText())) {
			this.xOffset = 0D;
			return;
		}

		final double textX = this.getTextX(super.getX(), super.getText());
		final double cursorX = textX + this.getX(super.getText(), FieldNode.tags(super.getText(), super.getShownInfo()), super.getCursorPos());
		if (cursorX < super.getX() + super.getMarginLeft() + super.getCursorMargin()) {
			this.xOffset -= super.getX() + super.getMarginLeft() + super.getCursorMargin() - cursorX;
		}
	}

	public final <T extends LineFieldNode<V>> @NonNull T align(final @NonNull Align align) {
		return this.align(Signal.from(align));
	}

	public final <T extends LineFieldNode<V>> @NonNull T align(final @NonNull Supplier<@NonNull Align> align) {
		this.alignX(align);
		return this.alignY(align);
	}

	public final <T extends LineFieldNode<V>> @NonNull T alignX(final @NonNull Align alignX) {
		return this.alignX(Signal.from(alignX));
	}

	public final <T extends LineFieldNode<V>> @NonNull T alignX(final @NonNull Supplier<@NonNull Align> alignX) {
		return super.follow("alignX", alignX, value -> this.alignX = value);
	}

	public final <T extends LineFieldNode<V>> @NonNull T alignY(final @NonNull Align alignY) {
		return this.alignY(Signal.from(alignY));
	}

	public final <T extends LineFieldNode<V>> @NonNull T alignY(final @NonNull Supplier<@NonNull Align> alignY) {
		return super.follow("alignY", alignY, value -> this.alignY = value);
	}

	public final <T extends LineFieldNode<V>> @NonNull T onSubmit(final @NonNull EventListener<NodeEvent<T>> listener) {
		return super.listen(LineFieldNode.SUBMIT, listener);
	}

	private boolean isScrolling(final @NonNull String shown) {
		return this.alignX.isStart() || super.getShownInfo().getWidth(shown) > super.getWidth() - super.getMarginLeft() - super.getMarginRight() - 2D;
	}

	private double getTextX(final double x, final @NonNull String shown) {
		if (this.isScrolling(shown)) {
			return x + super.getMarginLeft() - this.xOffset;
		}

		if (this.alignX.isCenter()) {
			return x + (super.getWidth() - super.getShownInfo().getWidth(shown)) / 2D;
		}

		return x + super.getWidth() - super.getShownInfo().getWidth(shown) - super.getMarginRight() - 2D;
	}

	private double getX(final @NonNull String text, final @NonNull int[] tags, final int position) {
		final int end = position < text.length() && tags[position] != -1 ? tags[position] : position;
		return super.getShownInfo().getWidth(text.substring(0, end));
	}

}