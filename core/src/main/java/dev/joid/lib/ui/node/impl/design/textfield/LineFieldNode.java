package dev.joid.lib.ui.node.impl.design.textfield;

import java.util.function.Supplier;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.callback.registry.NodeCallbackRegistry;
import dev.joid.lib.ui.node.impl.design.textfield.callback.NodeTextFieldEnterCallback;
import dev.joid.lib.utils.align.Align;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public abstract class LineFieldNode<V> extends FieldNode<V> {

	private static final int CALLBACK_ENTER = NodeCallbackRegistry.next(NodeTextFieldEnterCallback.class);

	private Align verticalAlignment;
	private Align horizontalAlignment;

	private double xOffset;

	protected LineFieldNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.horizontalAlignment = Align.START;
		this.verticalAlignment   = Align.CENTER;

		super.marginHorizontal(2D);
		super.marginVertical(10D);
		super.cursorMargin(15D);
	}

	@Override
	protected void drawField() {
		final String text = super.getText();
		final TextInfo info = super.getInfo();
		if (super.getHeight() == 0 && info != null) {
			super.height(info.ah(super.getMarginTop() + super.getMarginBottom()));
		}

		if (this.xOffset > super.getShownInfo().getWidth(text)) {
			super.decreaseCursor(0);
		}

		if (this.xOffset < 0D) {
			this.xOffset = 0D;
		}

		this.xOffset = super.getUi().lerpByFramerate(this.xOffset, this.xOffset, 0.5D, 0.2D, true);

		final boolean isPlaceholder = text.isEmpty() && !super.isFocused();
		double tmpTextY = super.getY() + super.getMarginTop();

		if (this.verticalAlignment.isCenter()) {
			tmpTextY = super.getY() + (super.getHeight() - info.getHeight()) / 2D;
		} else if (this.verticalAlignment.isEnd()) {
			tmpTextY = super.getY() + super.getHeight() - info.getHeight() - super.getMarginBottom();
		}

		final double textX = this.getTextX(super.getX(), isPlaceholder ? super.getPlaceholder() : text);
		final double textY = tmpTextY;
		final int[] tags = FieldNode.tags(text, super.getShownInfo());
		super.getUi().mask(super.getX() + super.getMarginLeft(), super.getY(), super.getWidth() - super.getMarginLeft() - super.getMarginRight(), super.getHeight(), () -> {
			DrawUtils.TEXT.drawText(textX, textY, isPlaceholder ? super.getPlaceholder() : text, isPlaceholder ? super.getShownInfo().copy().color(new Color(info.getColor().r, info.getColor().g, info.getColor().b, 0.5F)) : super.getShownInfo(), Align.START, Align.START);

			if (super.isFocused()) {
				final double cursorX = textX + this.getX(text, tags, super.getCursorPos());
				final float cursorOpacity = (float) ((Math.sin(2D * Math.PI * (BridgeHandler.CLOCK.get().currentTimeMillis() % 2000) / 1000) + 1D) / 2F);
				final Color cursorColor = new Color(info.getColor().r, info.getColor().g, info.getColor().b, cursorOpacity);
				DrawUtils.SHAPE.drawRect(cursorX, textY, 2D, info.getHeight(), cursorColor);
			}

			if (super.getSelectionStart() != -1) {
				final double cursorX = textX + this.getX(text, tags, super.getCursorPos());
				final double selectionX = textX + this.getX(text, tags, super.getSelectionStart());

				if (selectionX > cursorX) {
					DrawUtils.SHAPE.drawRect(cursorX, textY, selectionX - cursorX, info.getHeight(), new Color(50, 152, 253, 100));
				} else {
					DrawUtils.SHAPE.drawRect(selectionX, textY, cursorX - selectionX, info.getHeight(), new Color(50, 152, 253, 100));
				}
			}
		});
	}

	@Override
	protected final boolean isMultiline() {
		return false;
	}

	@Override
	protected final boolean handleKey(final @NonNull Key key) {
		if (key == Key.ESCAPE) {
			super.restore();
			super.focus(false);
			return true;
		}

		if (key != Key.ENTER && key != Key.NUMPAD_ENTER) {
			return false;
		}

		super.focus(false);
		super.executeCallback(LineFieldNode.CALLBACK_ENTER, DispatchContext.create(), super.getText());
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

	public final <T extends LineFieldNode<V>> @NonNull T verticalAlign(final @NonNull Align verticalAlignment) {
		return this.verticalAlign(Signal.from(verticalAlignment));
	}

	public final <T extends LineFieldNode<V>> @NonNull T verticalAlign(final @NonNull Supplier<@NonNull Align> verticalAlignment) {
		return super.follow("verticalAlignment", verticalAlignment, value -> this.verticalAlignment = value);
	}

	public final <T extends LineFieldNode<V>> @NonNull T horizontalAlign(final @NonNull Align horizontalAlignment) {
		return this.horizontalAlign(Signal.from(horizontalAlignment));
	}

	public final <T extends LineFieldNode<V>> @NonNull T horizontalAlign(final @NonNull Supplier<@NonNull Align> horizontalAlignment) {
		return super.follow("horizontalAlignment", horizontalAlignment, value -> this.horizontalAlignment = value);
	}

	public final <T extends LineFieldNode<V>> @NonNull T onEnter(final @NonNull NodeTextFieldEnterCallback<T> callback) {
		super.registerCallback(LineFieldNode.CALLBACK_ENTER, callback);
		return (T) this;
	}

	private final boolean isScrolling(final @NonNull String shown) {
		return this.horizontalAlignment.isStart() || super.getShownInfo().getWidth(shown) > super.getWidth() - super.getMarginLeft() - super.getMarginRight() - 2D;
	}

	private final double getTextX(final double x, final @NonNull String shown) {
		if (this.isScrolling(shown)) {
			return x + super.getMarginLeft() - this.xOffset;
		}

		if (this.horizontalAlignment.isCenter()) {
			return x + (super.getWidth() - super.getShownInfo().getWidth(shown)) / 2D;
		}

		return x + super.getWidth() - super.getShownInfo().getWidth(shown) - super.getMarginRight() - 2D;
	}

	private final double getX(final @NonNull String text, final @NonNull int[] tags, final int position) {
		final int end = position < text.length() && tags[position] != -1 ? tags[position] : position;
		return super.getShownInfo().getWidth(text.substring(0, end));
	}

}