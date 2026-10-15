package dev.joid.showcase;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import dev.joid.demo.DemoFont;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.CircleNode;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.textfield.TextFieldNode;
import dev.joid.lib.ui.node.impl.structure.container.ContainerNode;
import dev.joid.lib.ui.node.impl.structure.slider.impl.DoubleSliderNode;
import dev.joid.lib.ui.node.impl.structure.toggle.ToggleNode;
import dev.joid.lib.utils.align.Align;

public class ShowPaperKit implements IShowKit {

	public static final Color INK    = Color.decode("#151413").lock();
	public static final Color CARD   = Color.decode("#FFFCF4").lock();
	public static final Color RULE   = Color.decode("#E2D9C6").lock();
	public static final Color MUTE   = Color.decode("#6F685C").lock();
	public static final Color PAPER  = Color.decode("#F2ECDF").lock();
	public static final Color TOMATO = Color.decode("#FF5533").lock();
	public static final Color BUTTER = Color.decode("#FFD24D").lock();

	@Override
	public void backdrop(final Node layer) {
		RectNode.create(0, 0, 1920, 1080).color(ShowPaperKit.PAPER).layer((node, mouseX, mouseY) -> {
			for (int y = 54; y < 1080; y += 54) {
				DrawUtils.SHAPE.drawRect(0D, y, 1920D, 2D, ShowPaperKit.RULE);
			}
			DrawUtils.SHAPE.drawRect(96D, 0D, 3D, 1080D, ShowPaperKit.TOMATO.withAlpha(0.45F));
		}).attach(layer);
		CircleNode.create(1610, -150, 520).color(ShowPaperKit.BUTTER).<CircleNode>y(() -> -150D + ShowPaperKit.wave(0D) * 18D).attach(layer);
		CircleNode.create(800, 900, 260).color(ShowPaperKit.TOMATO).<CircleNode>x(() -> 800D + ShowPaperKit.wave(0.3D) * 24D).attach(layer);
	}

	@Override
	public TextInfo display() {
		return TextInfo.create(DemoFont.PLAYFAIR_DISPLAY, FontWeight.REGULAR, 136F, ShowPaperKit.INK);
	}

	@Override
	public TextInfo accent() {
		return TextInfo.create(DemoFont.PLAYFAIR_DISPLAY, FontWeight.REGULAR, 136F, ShowPaperKit.TOMATO);
	}

	@Override
	public TextInfo title() {
		return TextInfo.create(DemoFont.PLAYFAIR_DISPLAY, FontWeight.REGULAR, 54F, ShowPaperKit.INK);
	}

	@Override
	public TextInfo label() {
		return ShowUI.font(FontWeight.BOLD, 20F, ShowPaperKit.INK).letterSpacing(0.12F);
	}

	@Override
	public TextInfo hint() {
		return ShowUI.font(FontWeight.MEDIUM, 25F, ShowPaperKit.MUTE);
	}

	@Override
	public RectNode panel(final double x, final double y, final double width, final double height) {
		return RectNode.create(x, y, width, height).color(Color.TRANSPARENT).layer((node, mouseX, mouseY) -> {
			DrawUtils.SHAPE.drawRect(16D, 16D, width, height, ShowPaperKit.INK);
			ShowPaperKit.box(0D, 0D, width, height, ShowPaperKit.CARD, 4D);
		});
	}

	@Override
	public RectNode divider(final double x, final double y, final double width) {
		return RectNode.create(x, y, width, 2D).color(ShowPaperKit.INK.withAlpha(0.18F));
	}

	@Override
	public RectNode choice(final double x, final double y, final String label, final BooleanSupplier selected) {
		return RectNode
		.create(x, y, 180, 64)
		.color(Color.TRANSPARENT)
		.layer((node, mouseX, mouseY) -> {
			final boolean on = selected.getAsBoolean();
			if (on) {
				DrawUtils.SHAPE.drawRect(8D, 8D, 180D, 64D, ShowPaperKit.INK);
			}
			ShowPaperKit.box(0D, 0D, 180D, 64D, on ? ShowPaperKit.TOMATO : ShowPaperKit.CARD, 3D);
			DrawUtils.TEXT.drawText(90D, 32D, Text.create(label.toUpperCase(), ShowUI.font(FontWeight.BOLD, 20F, ShowPaperKit.INK).letterSpacing(0.14F)).align(Align.CENTER));
		});
	}

	@Override
	public RectNode button(final double x, final double y, final double width, final Supplier<String> label, final boolean primary) {
		return RectNode
		.create(x, y, width, 72)
		.color(Color.TRANSPARENT)
		.layer((button, mouseX, mouseY) -> {
			final double push = button.hoverValue(4F);
			DrawUtils.SHAPE.drawRect(10D, 10D, width, 72D, ShowPaperKit.INK);
			ShowPaperKit.box(push, push, width, 72D, primary ? ShowPaperKit.TOMATO : ShowPaperKit.CARD, 3D);
			DrawUtils.TEXT.drawText(push + width / 2D, push + 36D, Text.create(label.get().toUpperCase(), ShowUI.font(FontWeight.BOLD, 21F, ShowPaperKit.INK).letterSpacing(0.14F)).align(Align.CENTER));
		})
		.hoverDuration(120L);
	}

	@Override
	public ToggleNode<Boolean, Boolean> toggle(final double x, final double y) {
		return Toggle.create(x, y);
	}

	@Override
	public DoubleSliderNode slider(final double x, final double y, final double width) {
		return Slider.create(x, y, width);
	}

	@Override
	public TextFieldNode field(final double x, final double y, final double width) {
		return Field.create(x, y, width).info(ShowUI.font(FontWeight.SEMI_BOLD, 25F, ShowPaperKit.INK)).marginHorizontal(24D);
	}

	private static void box(final double x, final double y, final double width, final double height, final Color fill, final double stroke) {
		DrawUtils.SHAPE.drawRect(x, y, width, height, ShowPaperKit.INK);
		DrawUtils.SHAPE.drawRect(x + stroke, y + stroke, width - stroke * 2D, height - stroke * 2D, fill);
	}

	private static double wave(final double phase) {
		return Math.sin((BridgeHandler.CLOCK.get().currentTimeMillis() / 9000D + phase) * Math.PI * 2D);
	}

	public static class Toggle extends ToggleNode<Boolean, Boolean> {

		private double knob;

		protected Toggle(final double x, final double y) {
			super(x, y, 76, 42);
			super.track(ContainerNode.create(0, 0, 0, 0).layer((node, mouseX, mouseY) -> {
				this.knob = super.getUi().lerpByFramerate(this.knob, super.isToggle() ? 1D : 0D, 0.8D, 0.001D, true);
				DrawUtils.SHAPE.drawRect(5D, 5D, 76D, 42D, ShowPaperKit.INK);
				ShowPaperKit.box(0D, 0D, 76D, 42D, ShowPaperKit.CARD.to(ShowPaperKit.TOMATO, (float) this.knob), 3D);
			}));
			super.knob(ContainerNode.create(0, 0, 28, 28).layer((node, mouseX, mouseY) -> ShowPaperKit.box(7D + 34D * this.knob - node.getX(), 0D, 28D, 28D, ShowPaperKit.CARD.to(ShowPaperKit.INK, (float) this.knob), 3D)));
		}

		public static Toggle create(final double x, final double y) {
			return new Toggle(x, y);
		}

	}

	public static class Slider extends DoubleSliderNode {

		protected Slider(final double x, final double y, final double width) {
			super(x, y, width, 36);
			super.track(ContainerNode.create(0, 0, 0, 0).layer((node, mouseX, mouseY) -> {
				final double center = 18D + super.getProgress() * (node.getWidth() - 36D);
				ShowPaperKit.box(0D, 10D, node.getWidth(), 16D, ShowPaperKit.CARD, 3D);
				ShowPaperKit.box(0D, 10D, center, 16D, ShowPaperKit.TOMATO, 3D);
				for (int i = 1; i < 10; i++) {
					DrawUtils.SHAPE.drawRect(node.getWidth() * i / 10D - 1D, 30D, 2D, 8D, ShowPaperKit.INK.withAlpha(0.35F));
				}
			}));
			super.thumb(ContainerNode.create(0, 0, 36, 36).layer((node, mouseX, mouseY) -> {
				DrawUtils.SHAPE.drawRect(5D, 5D, 36D, 36D, ShowPaperKit.INK);
				ShowPaperKit.box(0D, 0D, 36D, 36D, ShowPaperKit.BUTTER, 3D);
			}));
		}

		public static Slider create(final double x, final double y, final double width) {
			return new Slider(x, y, width);
		}

	}

	public static class Field extends TextFieldNode {

		private double focus;

		protected Field(final double x, final double y, final double width) {
			super(x, y, width, 68);
			super.background(ContainerNode.create(0, 0, 0, 0).layer((node, mouseX, mouseY) -> {
				this.focus = super.getUi().lerpByFramerate(this.focus, super.isFocused() ? 1D : 0D, 0.6D, 0.001D, true);
				DrawUtils.SHAPE.drawRect(8D, 8D, node.getWidth(), node.getHeight(), ShowPaperKit.INK.to(ShowPaperKit.TOMATO, (float) this.focus));
				ShowPaperKit.box(0D, 0D, node.getWidth(), node.getHeight(), Color.WHITE, 3D);
			}));
		}

		public static Field create(final double x, final double y, final double width) {
			return new Field(x, y, width);
		}

	}

}