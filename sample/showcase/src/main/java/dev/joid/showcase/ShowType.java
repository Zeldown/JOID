package dev.joid.showcase;

import java.util.Locale;

import dev.joid.demo.DemoFont;
import dev.joid.demo.font.effect.DemoRainbowTextEffect;
import dev.joid.demo.font.effect.DemoWaveTextEffect;
import dev.joid.demo.font.markup.DemoTextMarkup;
import dev.joid.internal.JOID;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.render.transform.Scale;
import dev.joid.lib.render.transform.Transformation;
import dev.joid.lib.render.transform.Vector;
import dev.joid.lib.ui.node.effect.NodeEffect.NodeEffectScope;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.TransformNodeEffect;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class ShowType extends ShowUI {

	@Override
	protected void scene() {
		final double pivotX = 128D;
		final double pivotY = 236D;

		super.backdrop();
		ShowUI.blob(1250, -350, 900, ShowUI.FUCHSIA.withAlpha(0.28F), 0D, 50D).attach(this);
		ShowUI.blob(-350, 600, 900, ShowUI.CYAN.withAlpha(0.20F), 0.5D, 50D).attach(this);

		final long start = BridgeHandler.CLOCK.get().currentTimeMillis();

		ContainerNode
		.create(0, 0, 1920, 1080)
		.effect(TransformNodeEffect.create(Transformation.create().translate(Vector.create(() -> this.zoom() * (700D - pivotX), () -> this.zoom() * (470D - pivotY))).scale(Scale.create(() -> 1D + 5D * this.zoom(), () -> 1D + 5D * this.zoom(), () -> 1D), Vector.create(pivotX, pivotY))).scope(NodeEffectScope.CHILDREN))
		.body(container -> {
			TextNode.create(122, 160).text(Text.create("MSDF TEXT", ShowUI.font(FontWeight.BOLD, 18F, ShowUI.FAINT).letterSpacing(0.3F))).attach(container);
			final int[] sizes = {14, 18, 24, 32, 44, 60, 80, 108};
			final FontWeight[] weights = {FontWeight.MEDIUM, FontWeight.MEDIUM, FontWeight.SEMI_BOLD, FontWeight.SEMI_BOLD, FontWeight.BOLD, FontWeight.EXTRA_BOLD, FontWeight.BLACK, FontWeight.BLACK};
			double y = 226D;
			for (int i = 0; i < sizes.length; i++) {
				final float progress = i / (float) (sizes.length - 1);
				final Color color = i == sizes.length - 1 ? ShowUI.FUCHSIA.toGradient(ShowUI.CYAN) : ShowUI.MUTED.to(ShowUI.TEXT, progress);
				final TextInfo info = ShowUI.font(weights[i], sizes[i], color).effects(ShowRiseTextEffect.of(start + 150L + i * 90L, 18D));
				TextNode.create(120, y).text(Text.create("Crisp at " + sizes[i] + " px", info)).attach(container);
				y += sizes[i] * 1.22D + 20D;
			}

			TextNode.create(1040, 170).text(Text.create("<i>Design</i> once,", TextInfo.create(DemoFont.PLAYFAIR_DISPLAY, FontWeight.REGULAR, 80F, ShowUI.TEXT).markups(DemoTextMarkup.inst()).effects(ShowRiseTextEffect.of(start + 300L, 35D)))).attach(container);
			TextNode.create(1040, 272).text(Text.create("render <c=fbbf24><b>anywhere</b></c>.", TextInfo.create(DemoFont.PLAYFAIR_DISPLAY, FontWeight.REGULAR, 80F, ShowUI.TEXT).markups(DemoTextMarkup.inst()).effects(ShowRiseTextEffect.of(start + 650L, 35D)))).attach(container);
			TextNode.create(1040, 430).text(Text.create("Hello, world", TextInfo.create(DemoFont.PACIFICO, 92F, Color.WHITE).effects(DemoWaveTextEffect.inst(), DemoRainbowTextEffect.inst()))).attach(container);
			TextNode.create(1044, 610).text(Text.create("DECODING SIGNAL " + JOID.VERSION, ShowUI.font(FontWeight.EXTRA_BOLD, 46F, ShowUI.CYAN).letterSpacing(0.08F).effects(ShowDecodeTextEffect.of(start + 500L, ShowUI.PINK, 70D)))).attach(container);
			TextNode.create(1044, 716).text(Text.create("<b>Bold</b>, <i>italic</i>, <u>underlined</u>, <h>highlighted</h>, <c=f472b6>colored</c>", ShowUI.font(FontWeight.REGULAR, 32F, ShowUI.TEXT).markups(DemoTextMarkup.inst()))).attach(container);
			TextNode.create(1044, 810).text(Text.create("One distance-field atlas per font:", ShowUI.font(FontWeight.MEDIUM, 30F, ShowUI.MUTED).effects(ShowRiseTextEffect.of(start + 900L, 14D)))).attach(container);
			TextNode.create(1044, 856).text(Text.create("every size, every weight, every zoom level.", ShowUI.font(FontWeight.MEDIUM, 30F, ShowUI.MUTED).effects(ShowRiseTextEffect.of(start + 1100L, 14D)))).attach(container);
		})
		.attach(this);

		RectNode
		.create(1700, 84, 120, 56)
		.color(Color.WHITE.withAlpha(0.08F))
		.effect(RoundedNodeEffect.create(28F))
		.body(pill -> {
			TextNode.create(60, 28).text(Text.create(() -> String.format(Locale.US, "%.1f×", 1D + 5D * this.zoom()), ShowUI.font(FontWeight.BOLD, 24F, ShowUI.TEXT))).anchor(Align.CENTER).attach(pill);
		})
		.attach(this);
	}

	private double zoom() {
		return super.ease(TweenEquations.QUINT_INOUT, 2.2D, 3.6D) - super.ease(TweenEquations.QUINT_INOUT, 5.0D, 6.3D);
	}

}