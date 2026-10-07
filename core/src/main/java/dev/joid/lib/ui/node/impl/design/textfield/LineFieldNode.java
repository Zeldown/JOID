package dev.joid.lib.ui.node.impl.design.textfield;

import java.util.function.Supplier;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.node.callback.registry.NodeCallbackRegistry;
import dev.joid.lib.ui.node.impl.design.textfield.callback.NodeTextFieldEnterCallback;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.key.Key;
import dev.joid.lib.utils.signal.Signal;
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
	protected final void drawField() {
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
		super.getUi().mask(super.getX() + super.getMarginLeft(), super.getY(), super.getWidth() - super.getMarginLeft() - super.getMarginRight(), super.getHeight(), () -> {
			DrawUtils.TEXT.drawText(textX, textY, isPlaceholder ? super.getPlaceholder() : text, isPlaceholder ? super.getShownInfo().copy().color(new Color(info.getColor().r, info.getColor().g, info.getColor().b, 0.5F)) : super.getShownInfo(), Align.START, Align.START);

			if (super.isFocused()) {
				final String beforeCursor = text.substring(0, super.getCursorPos());
				final double cursorX = textX + super.getShownInfo().getWidth(beforeCursor);
				final float cursorOpacity = (float) ((Math.sin(2D * Math.PI * (BridgeHandler.CLOCK.get().currentTimeMillis() % 2000) / 1000) + 1D) / 2F);
				final Color cursorColor = new Color(info.getColor().r, info.getColor().g, info.getColor().b, cursorOpacity);
				DrawUtils.SHAPE.drawRect(cursorX, textY, 2D, info.getHeight(), cursorColor);
			}

			if (super.getSelectionStart() != -1) {
				final String beforeCursor = text.substring(0, super.getCursorPos());
				final double cursorX = textX + super.getShownInfo().getWidth(beforeCursor);

				final String beforeSelection = text.substring(0, super.getSelectionStart());
				final double selectionX = textX + super.getShownInfo().getWidth(beforeSelection);

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
		if (key != Key.ENTER && key != Key.NUMPAD_ENTER && key != Key.ESCAPE) {
			return false;
		}

		super.focus(false);
		super.executeCallback(LineFieldNode.CALLBACK_ENTER, InternalContext.create(), super.getText());
		return true;
	}

	@Override
	protected final void placeCursor(final double mouseX, final double mouseY) {
		final String text = super.getText();
		final double textX = this.getTextX(super.getAbsoluteX(), text);
		for (int i = 0; i < text.length(); i++) {
			final String beforeCursor = text.substring(0, i);
			final String cursorChar = text.substring(i, i + 1);
			final double cursorX = textX + super.getShownInfo().getWidth(beforeCursor) + super.getShownInfo().dw(cursorChar, 2);
			if (mouseX < cursorX) {
				super.cursorPosition(i);
				break;
			}
			if (i == text.length() - 1) {
				super.cursorPosition(text.length());
			}
		}
	}

	@Override
	protected final void followCursorForward() {
		if (!this.isScrolling(super.getText())) {
			this.xOffset = 0D;
			return;
		}

		final double textX = this.getTextX(super.getX(), super.getText());

		final String beforeCursor = super.getText().substring(0, super.getCursorPos());
		final double cursorX = textX + super.getShownInfo().getWidth(beforeCursor);
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

		final String beforeCursor = super.getText().substring(0, super.getCursorPos());
		final double cursorX = textX + super.getShownInfo().getWidth(beforeCursor);
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

}