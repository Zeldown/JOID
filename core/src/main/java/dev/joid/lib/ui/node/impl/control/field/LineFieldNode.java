package dev.joid.lib.ui.node.impl.control.field;

import java.util.function.Supplier;

import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.font.markup.TextMarkup;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.NodeEvent;
import dev.joid.lib.utils.align.Align;
import lombok.Getter;
import lombok.NonNull;

public abstract class LineFieldNode<V> extends FieldNode<V> {

	private static final EventType<NodeEvent<?>> SUBMIT = EventType.create("onSubmit");

	@Getter private Align alignX;
	@Getter private Align alignY;

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
	public void update() {
		super.fit(super.getWidth(), super.getInfo().ah(super.getMarginTop() + super.getMarginBottom()));
	}

	@Override
	protected void drawField() {
		final String text = super.getText();
		final TextInfo info = super.getInfo();
		if (this.xOffset > super.getShownInfo().getWidth(text)) {
			this.followCursorBackward();
		}

		if (this.xOffset < 0D) {
			this.xOffset = 0D;
		}

		final boolean isPlaceholder = text.isEmpty() && !super.isFocused();
		final double textX = this.getTextX(super.getX(), isPlaceholder ? super.getPlaceholder() : text);
		final double textY = this.alignY.isCenter() ? super.getY() + (super.getHeight() - info.getHeight()) / 2D : this.alignY.isEnd() ? super.getY() + super.getHeight() - info.getHeight() - super.getMarginBottom() : super.getY() + super.getMarginTop();
		final int[] tags = TextMarkup.tags(text, super.getShownInfo());
		final FieldSelection selection = super.getSelection();
		super.getUi().mask(super.getX() + super.getMarginLeft(), super.getY(), super.getWidth() - super.getMarginLeft() - super.getMarginRight(), super.getHeight(), () -> {
			DrawUtils.TEXT.drawText(textX, textY, isPlaceholder ? super.getPlaceholder() : text, isPlaceholder ? super.getPlaceholderInfo() : super.getShownInfo(), Align.START, Align.START);

			if (super.isFocused()) {
				DrawUtils.SHAPE.drawRect(textX + this.getX(text, tags, super.getCursorPosition()), textY, 2D, info.getHeight(), super.getShownCursorColor());
			}

			if (!selection.isEmpty()) {
				final double startX = textX + this.getX(text, tags, selection.getStart());
				DrawUtils.SHAPE.drawRect(startX, textY, textX + this.getX(text, tags, selection.getEnd()) - startX, info.getHeight(), super.getSelectionColor());
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

		if (super.blurNow()) {
			super.fire(LineFieldNode.SUBMIT, () -> NodeEvent.create(this));
		}

		return true;
	}

	@Override
	protected final int getPositionAt(final double mouseX, final double mouseY) {
		final String text = super.getText();
		final int[] tags = TextMarkup.tags(text, super.getShownInfo());
		return super.getLinePosition(text, tags, 0, text.length(), "", mouseX - this.getTextX(super.getAbsoluteX(), text));
	}

	@Override
	protected final void followCursorForward() {
		if (!this.isScrolling(super.getText())) {
			this.xOffset = 0D;
			return;
		}

		final double textX = this.getTextX(super.getX(), super.getText());
		final double cursorX = textX + this.getX(super.getText(), TextMarkup.tags(super.getText(), super.getShownInfo()), super.getCursorPosition());
		if (cursorX > super.getX() + super.getWidth() - super.getMarginRight() - super.getCursorMargin()) {
			this.xOffset += cursorX - (super.getX() + super.getWidth() - super.getMarginRight() - super.getCursorMargin());
		}
	}

	@Override
	protected final void followCursorBackward() {
		if (super.getCursorPosition() == 0 || !this.isScrolling(super.getText())) {
			this.xOffset = 0D;
			return;
		}

		final double textX = this.getTextX(super.getX(), super.getText());
		final double cursorX = textX + this.getX(super.getText(), TextMarkup.tags(super.getText(), super.getShownInfo()), super.getCursorPosition());
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

	private boolean isScrolling(final String shown) {
		return this.alignX.isStart() || super.getShownInfo().getWidth(shown) > super.getWidth() - super.getMarginLeft() - super.getMarginRight() - 2D;
	}

	private double getTextX(final double x, final String shown) {
		if (this.isScrolling(shown)) {
			return x + super.getMarginLeft() - this.xOffset;
		}

		if (this.alignX.isCenter()) {
			return x + (super.getWidth() - super.getShownInfo().getWidth(shown)) / 2D;
		}

		return x + super.getWidth() - super.getShownInfo().getWidth(shown) - super.getMarginRight() - 2D;
	}

	private double getX(final String text, final int[] tags, final int position) {
		return super.getLineX(text, tags, 0, text.length(), "", position);
	}

}