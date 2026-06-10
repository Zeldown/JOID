package be.zeldown.joid.lib.ui.node.impl.design.textfield;

import java.util.function.BiFunction;

import org.lwjgl.input.Keyboard;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.callback.registry.NodeCallbackRegistry;
import be.zeldown.joid.lib.ui.node.impl.design.textfield.callback.NodeTextFieldChangeCallback;
import be.zeldown.joid.lib.ui.node.impl.design.textfield.callback.NodeTextFieldEnterCallback;
import be.zeldown.joid.lib.ui.node.impl.design.textfield.callback.NodeTextFieldFocusCallback;
import be.zeldown.joid.lib.utils.align.Align;
import be.zeldown.joid.lib.utils.click.ClickType;
import be.zeldown.joid.lib.utils.clipboard.ClipboardUtils;
import be.zeldown.joid.lib.utils.context.InternalContext;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public class TextFieldNode extends Node {

	private static final int CALLBACK_CHANGE = NodeCallbackRegistry.next(NodeTextFieldChangeCallback.class);
	private static final int CALLBACK_FOCUS  = NodeCallbackRegistry.next(NodeTextFieldFocusCallback.class);
	private static final int CALLBACK_ENTER  = NodeCallbackRegistry.next(NodeTextFieldEnterCallback.class);

	private String   text;
	private String   placeholder;
	private TextInfo info;
	private Align    horizontalAlignment;
	private Align    verticalAlignment;

	private boolean focused;
	private BiFunction<String, String, String> filter;
	private int maxTextLength;

	private int selectionStart;
	private int cursorPos;

	private double marginLeft;
	private double marginRight;
	private double marginTop;
	private double marginBottom;
	private double cursorMargin;

	private double xOffset;

	private boolean firstInput;
	private boolean inputting;
	private long    lastInput;
	private int     inputType;

	protected TextFieldNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.text          = "";
		this.placeholder   = "";
		this.focused       = false;
		this.filter        = (oldText, newText) -> newText;
		this.maxTextLength = -1;

		this.horizontalAlignment = Align.START;
		this.verticalAlignment   = Align.CENTER;

		this.selectionStart = -1;

		this.marginHorizontal(2D);
		this.marginVertical(10D);
		this.cursorMargin(15D);
	}

	public static @NonNull TextFieldNode create(final double x, final double y, final double width) {
		return new TextFieldNode(x, y, width, 0D);
	}

	public static @NonNull TextFieldNode create(final double x, final double y, final double width, final double height) {
		return new TextFieldNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (super.getHeight() == 0 && this.info != null) {
			super.height(this.info.ah(this.marginTop + this.marginBottom));
		}

		if (this.xOffset > this.info.getWidth(this.text)) {
			this.decreaseCursor(0);
		}

		if (this.cursorPos < 0) {
			this.cursorPos = 0;
		}

		if (this.cursorPos > this.text.length()) {
			this.cursorPos = this.text.length();
		}

		if (this.selectionStart > this.text.length()) {
			this.selectionStart = this.text.length();
		}

		if (this.xOffset < 0D) {
			this.xOffset = 0D;
		}

		this.xOffset = super.getUi().lerpByFramerate(this.xOffset, this.xOffset, 0.5D, 0.2D, true);

		final boolean isPlaceholder = this.text.isEmpty() && !this.focused;
		double tmpTextX = super.getX() + this.marginLeft - this.xOffset;
		double tmpTextY = super.getY() + this.marginTop;

		if (this.horizontalAlignment.isCenter()) {
			tmpTextX = super.getX() + (super.getWidth() - this.info.getWidth(isPlaceholder ? this.placeholder : this.text)) / 2D;
		} else if (this.horizontalAlignment.isEnd()) {
			tmpTextX = super.getX() + super.getWidth() - this.info.getWidth(isPlaceholder ? this.placeholder : this.text) - this.marginRight;
		}

		if (this.verticalAlignment.isCenter()) {
			tmpTextY = super.getY() + (super.getHeight() - this.info.getHeight()) / 2D;
		} else if (this.verticalAlignment.isEnd()) {
			tmpTextY = super.getY() + super.getHeight() - this.info.getHeight() - this.marginBottom;
		}

		final double textX = tmpTextX;
		final double textY = tmpTextY;
		super.getUi().mask(super.getX() + this.marginLeft, super.getY(), super.getWidth() - this.marginLeft - this.marginRight, super.getHeight(), () -> {
			DrawUtils.TEXT.drawText(textX, textY, isPlaceholder ? this.placeholder : this.text, isPlaceholder ? this.info.copy().color(new Color(this.info.getColor().r, this.info.getColor().g, this.info.getColor().b, 0.5F)) : this.info, Align.START, Align.START);

			if (this.focused) {
				final String beforeCursor = this.text.substring(0, this.cursorPos);
				final double cursorX = textX + this.info.getWidth(beforeCursor);
				final Color cursorColor = new Color(this.info.getColor());
				final float cursorOpacity = (float) ((Math.sin(2D * Math.PI * (System.currentTimeMillis() % 2000) / 1000) + 1D) / 2F);
				cursorColor.a = cursorOpacity;
				DrawUtils.SHAPE.drawRect(cursorX, textY, 2D, this.info.getHeight(), cursorColor);
			}

			if (this.selectionStart != -1) {
				final String beforeCursor = this.text.substring(0, this.cursorPos);
				final double cursorX = textX + this.info.getWidth(beforeCursor);

				final String beforeSelection = this.text.substring(0, this.selectionStart);
				final double selectionX = textX + this.info.getWidth(beforeSelection);

				if (selectionX > cursorX) {
					DrawUtils.SHAPE.drawRect(cursorX, textY, selectionX - cursorX, this.info.getHeight(), new Color(50, 152, 253, 100));
				} else {
					DrawUtils.SHAPE.drawRect(selectionX, textY, cursorX - selectionX, this.info.getHeight(), new Color(50, 152, 253, 100));
				}
			}
		});

		if (this.inputting && System.currentTimeMillis() - this.lastInput >= (this.firstInput ? 500 : 100)) {
			this.firstInput = false;

			if (!Keyboard.isKeyDown(this.inputType)) {
				this.inputting = false;
				return;
			}

			if (this.inputType == Keyboard.KEY_DELETE) {
				if (this.cursorPos >= this.text.length()) {
					this.inputting = false;
					return;
				}

				this.setText(this.text.substring(0, this.cursorPos) + this.text.substring(this.cursorPos + 1));
			} else if (this.inputType == Keyboard.KEY_BACK) {
				if (this.cursorPos <= 0) {
					this.inputting = false;
					return;
				}

				this.setText(this.text.substring(0, this.cursorPos - 1) + this.text.substring(this.cursorPos));
				this.decreaseCursor(1);
			} else if (this.inputType == Keyboard.KEY_LEFT) {
				this.decreaseCursor(1);
			} else if (this.inputType == Keyboard.KEY_RIGHT) {
				this.increaseCursor(1);
			}

			this.lastInput = System.currentTimeMillis();
		}
	}

	@Override
	public final void keyPressed(final char c, final int keyCode, final @NonNull InternalContext context) {
		if (context.isCancelled() || !this.focused) {
			return;
		}

		context.cancel(() -> {
			if (keyCode == Keyboard.KEY_LEFT) {
				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
					if (this.selectionStart == -1) {
						this.selectionStart = this.cursorPos;
					}
				} else {
					this.selectionStart = -1;
				}

				this.holdInput(keyCode);
				this.decreaseCursor(1);
				return;
			}

			if (keyCode == Keyboard.KEY_RIGHT) {
				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
					if (this.selectionStart == -1) {
						this.selectionStart = this.cursorPos;
					}
				} else {
					this.selectionStart = -1;
				}

				this.holdInput(keyCode);
				this.increaseCursor(1);
				return;
			}

			if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER || keyCode == Keyboard.KEY_ESCAPE) {
				this.focused(false);
				this.executeCallback(TextFieldNode.CALLBACK_ENTER, InternalContext.create(), this.text);
				return;
			}

			if (keyCode == Keyboard.KEY_BACK) {
				if (this.deleteSelection(true) || this.cursorPos <= 0) {
					return;
				}

				this.holdInput(keyCode);
				this.setText(this.text.substring(0, this.cursorPos - 1) + this.text.substring(this.cursorPos));
				this.decreaseCursor(1);
				return;
			}

			if (keyCode == Keyboard.KEY_DELETE) {
				if (this.deleteSelection(true) || this.cursorPos >= this.text.length()) {
					return;
				}

				this.holdInput(keyCode);
				this.setText(this.text.substring(0, this.cursorPos) + this.text.substring(this.cursorPos + 1));
				return;
			}

			if (keyCode == Keyboard.KEY_HOME) {
				this.cursorPos = 0;
				this.decreaseCursor(0);
				return;
			}

			if (keyCode == Keyboard.KEY_END) {
				this.cursorPos = this.text.length();
				this.increaseCursor(0);
				return;
			}

			if (keyCode == Keyboard.KEY_A && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
				this.selectionStart = 0;
				this.cursorPos = this.text.length();
				return;
			}

			if (keyCode == Keyboard.KEY_C && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
				if (this.selectionStart == -1) {
					return;
				}

				if (this.selectionStart < this.cursorPos) {
					ClipboardUtils.setClipboard(this.text.substring(this.selectionStart, this.cursorPos));
				} else {
					ClipboardUtils.setClipboard(this.text.substring(this.cursorPos, this.selectionStart));
				}

				return;
			}

			if (keyCode == Keyboard.KEY_X && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
				if (this.selectionStart == -1) {
					return;
				}

				if (this.selectionStart < this.cursorPos) {
					ClipboardUtils.setClipboard(this.text.substring(this.selectionStart, this.cursorPos));
					this.setText(this.text.substring(0, this.selectionStart) + this.text.substring(this.cursorPos));
					this.cursorPos -= this.cursorPos - this.selectionStart;
				} else {
					ClipboardUtils.setClipboard(this.text.substring(this.cursorPos, this.selectionStart));
					this.setText(this.text.substring(0, this.cursorPos) + this.text.substring(this.selectionStart));
				}

				this.selectionStart = -1;
				return;
			}

			String textToAdd = Character.toString(c);
			if (keyCode == Keyboard.KEY_V && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
				textToAdd = ClipboardUtils.getClipboard();
			}

			final char[] achar = textToAdd.toCharArray();
			final int i = achar.length;

			final StringBuilder stringbuilder = new StringBuilder();
			for (int j = 0; j < i; ++j) {
				final char c0 = achar[j];
				if (c0 != 167 && c0 >= 32 && c0 != 127 && c0 <= 563) {
					stringbuilder.append(c0);
				}
			}
			textToAdd = stringbuilder.toString();

			if (textToAdd.isEmpty()) {
				return;
			}

			this.deleteSelection(false);
			this.setText(this.text.substring(0, this.cursorPos) + textToAdd + this.text.substring(this.cursorPos));
			this.increaseCursor(textToAdd.length());
		});
	}

	private final void setText(String newText) {
		if (newText == null) {
			newText = "";
		}

		final String oldText = this.text == null ? "" : this.text;
		newText = this.filter.apply(oldText, newText);
		if (this.maxTextLength >= 0 && newText.length() > this.maxTextLength) {
			newText = newText.substring(0, this.maxTextLength);
		}

		final String finalNewText = newText;
		if (!finalNewText.equals(oldText)) {
			this.executeCallback(TextFieldNode.CALLBACK_CHANGE, InternalContext.create(), () -> {
				this.text = finalNewText;
			}, oldText, finalNewText);
		} else {
			this.text = finalNewText;
		}
	}

	private final void holdInput(final int keyCode) {
		this.firstInput = true;
		this.inputting  = true;
		this.lastInput  = System.currentTimeMillis();
		this.inputType  = keyCode;
	}

	@Override
	public final void mousePressed(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
		if (context.isCancelled() || !this.isHovered(mouseX, mouseY)) {
			this.focused(false);
			this.selectionStart = -1;
			return;
		}

		context.cancel(() -> {
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
				if (this.selectionStart == -1) {
					this.selectionStart = this.cursorPos;
				}
			} else {
				this.selectionStart = -1;
			}

			double tmpTextX = super.getX() + this.marginLeft - this.xOffset;

			if (this.horizontalAlignment.isCenter()) {
				tmpTextX = super.getX() + (super.getWidth() - this.info.getWidth(this.text)) / 2D;
			} else if (this.horizontalAlignment.isEnd()) {
				tmpTextX = super.getX() + super.getWidth() - this.info.getWidth(this.text) - this.marginRight;
			}

			final double textX = tmpTextX;
			for (int i = 0; i < this.text.length(); i++) {
				final String beforeCursor = this.text.substring(0, i);
				final String cursorChar = this.text.substring(i, i + 1);
				final double cursorX = textX + this.info.getWidth(beforeCursor) + this.info.dw(cursorChar, 2);
				if (mouseX < cursorX) {
					this.cursorPos = i;
					break;
				}
				if (i == this.text.length() - 1) {
					this.cursorPos = this.text.length();
				}
			}

			this.focused(true);
		});
	}

	private final void decreaseCursor(final int value) {
		this.cursorPos -= value;
		if (this.cursorPos < 0) {
			this.cursorPos = 0;
		}

		if (this.cursorPos == 0) {
			this.xOffset = 0;
			return;
		}

		double tmpTextX = super.getX() + this.marginLeft - this.xOffset;

		if (this.horizontalAlignment.isCenter()) {
			tmpTextX = super.getX() + (super.getWidth() - this.info.getWidth(this.text)) / 2D;
		} else if (this.horizontalAlignment.isEnd()) {
			tmpTextX = super.getX() + super.getWidth() - this.info.getWidth(this.text) - this.marginRight;
		}

		final double textX = tmpTextX;

		final String beforeCursor = this.text.substring(0, this.cursorPos);
		final double cursorX = textX + this.info.getWidth(beforeCursor);
		if (cursorX < super.getX() + this.marginLeft + this.cursorMargin) {
			this.xOffset -= super.getX() + this.marginLeft + this.cursorMargin - cursorX;
		}
	}

	private final void increaseCursor(final int value) {
		this.cursorPos += value;
		if (this.cursorPos > this.text.length()) {
			this.cursorPos = this.text.length();
		}

		double tmpTextX = super.getX() + this.marginLeft - this.xOffset;

		if (this.horizontalAlignment.isCenter()) {
			tmpTextX = super.getX() + (super.getWidth() - this.info.getWidth(this.text)) / 2D;
		} else if (this.horizontalAlignment.isEnd()) {
			tmpTextX = super.getX() + super.getWidth() - this.info.getWidth(this.text) - this.marginRight;
		}

		final double textX = tmpTextX;

		final String beforeCursor = this.text.substring(0, this.cursorPos);
		final double cursorX = textX + this.info.getWidth(beforeCursor);
		if (cursorX > super.getX() + super.getWidth() - this.marginRight - this.cursorMargin) {
			this.xOffset += cursorX - (super.getX() + super.getWidth() - this.marginRight - this.cursorMargin);
		}
	}

	private final boolean deleteSelection(final boolean filter) {
		if (this.selectionStart == -1) {
			return false;
		}

		final String oldText = this.text;
		if (this.selectionStart < this.cursorPos) {
			if (filter) {
				final String newText = this.filter.apply(this.text, this.text.substring(0, this.selectionStart) + this.text.substring(this.cursorPos));
				this.executeCallback(TextFieldNode.CALLBACK_CHANGE, InternalContext.create(), () -> {
					this.text = newText;
				}, oldText, newText);
			} else {
				final String newText = this.text.substring(0, this.selectionStart) + this.text.substring(this.cursorPos);
				this.executeCallback(TextFieldNode.CALLBACK_CHANGE, InternalContext.create(), () -> {
					this.text = newText;
				}, oldText, newText);
			}
			this.cursorPos -= oldText.length() - this.text.length();
		} else if (filter) {
			final String newText = this.filter.apply(this.text, this.text.substring(0, this.cursorPos) + this.text.substring(this.selectionStart));
			this.executeCallback(TextFieldNode.CALLBACK_CHANGE, InternalContext.create(), () -> {
				this.text = newText;
			}, oldText, newText);
		} else {
			final String newText = this.text.substring(0, this.cursorPos) + this.text.substring(this.selectionStart);
			this.executeCallback(TextFieldNode.CALLBACK_CHANGE, InternalContext.create(), () -> {
				this.text = newText;
			}, oldText, newText);
		}

		this.selectionStart = -1;
		return true;
	}

	public final <T extends TextFieldNode> @NonNull T text(final @NonNull String text) {
		this.setText(text);
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T placeholder(final @NonNull String placeholder) {
		this.placeholder = placeholder;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T info(final @NonNull TextInfo textInfo) {
		this.info = textInfo;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T align(final @NonNull Align horizontal, final @NonNull Align vertical) {
		this.horizontalAlignment = horizontal;
		this.verticalAlignment   = vertical;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T horizontalAlign(final @NonNull Align align) {
		this.horizontalAlignment = align;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T verticalAlign(final @NonNull Align align) {
		this.verticalAlignment = align;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T focused(final boolean focused) {
		if (this.focused == focused) {
			return (T) this;
		}

		super.executeCallback(TextFieldNode.CALLBACK_FOCUS, InternalContext.create(), () -> {
			this.focused = focused;
		});
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T filter(final @NonNull BiFunction<String, String, String> filter) {
		this.filter = filter;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T maxTextLength(final int maxTextLength) {
		this.maxTextLength = maxTextLength;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T margin(final double margin) {
		this.marginLeft  = margin;
		this.marginRight = margin;
		this.marginTop   = margin;
		this.marginBottom = margin;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T margin(final double margin, final double cursorMargin) {
		this.marginLeft   = margin;
		this.marginRight  = margin;
		this.marginTop    = margin;
		this.marginBottom = margin;
		this.cursorMargin = cursorMargin;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T marginHorizontal(final double margin) {
		this.marginLeft = margin;
		this.marginRight = margin;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T marginVertical(final double margin) {
		this.marginTop = margin;
		this.marginBottom = margin;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T marginLeft(final double marginLeft) {
		this.marginLeft = marginLeft;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T marginRight(final double marginRight) {
		this.marginRight = marginRight;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T marginTop(final double marginTop) {
		this.marginTop = marginTop;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T marginBottom(final double marginBottom) {
		this.marginBottom = marginBottom;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T cursorMargin(final double cursorMargin) {
		this.cursorMargin = cursorMargin;
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T cursorPosition(final int cursorPos) {
		this.cursorPos = Math.min(Math.max(0, cursorPos), this.text.length());
		return (T) this;
	}

	/* [ Callback Section ] */
	public final <T extends TextFieldNode> @NonNull T onChange(final @NonNull NodeTextFieldChangeCallback<T> callback) {
		super.registerCallback(TextFieldNode.CALLBACK_CHANGE, callback);
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T onFocus(final @NonNull NodeTextFieldFocusCallback<T> callback) {
		super.registerCallback(TextFieldNode.CALLBACK_FOCUS, callback);
		return (T) this;
	}

	public final <T extends TextFieldNode> @NonNull T onEnter(final @NonNull NodeTextFieldEnterCallback<T> callback) {
		super.registerCallback(TextFieldNode.CALLBACK_ENTER, callback);
		return (T) this;
	}

}