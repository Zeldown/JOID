package dev.joid.showcase;

import java.util.Locale;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.font.effect.DemoRainbowTextEffect;
import dev.joid.demo.ui.font.effect.DemoWaveTextEffect;
import dev.joid.demo.ui.font.markup.DemoTextMarkup;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.render.transform.Scale;
import dev.joid.lib.render.transform.Transformation;
import dev.joid.lib.render.transform.Vector;
import dev.joid.lib.ui.node.effect.NodeEffect.NodeEffectScope;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.TransformNodeEffect;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.container.ContainerNode;
import dev.joid.lib.utils.align.Align;

public class ShowType extends ShowUI {

	private static final double PIVOT_X = 128D;
	private static final double PIVOT_Y = 236D;

	@Override
	protected void scene() {
		this.backdrop();
		this.blob(1250, -350, 900, ShowUI.FUCHSIA.copyAlpha(0.28F), 7D, 0D, 50D);
		this.blob(-350, 600, 900, ShowUI.CYAN.copyAlpha(0.20F), 7D, 0.5D, 50D);

		final long start = BridgeHandler.CLOCK.get().currentTimeMillis();
		ContainerNode
		.create(0, 0, 1920, 1080)
		.effect(TransformNodeEffect.create(Transformation.create().translate(Vector.create(() -> this.zoom() * (700D - ShowType.PIVOT_X), () -> this.zoom() * (470D - ShowType.PIVOT_Y))).scale(Scale.create(() -> 1D + 5D * this.zoom(), () -> 1D + 5D * this.zoom(), () -> 1D), Vector.create(ShowType.PIVOT_X, ShowType.PIVOT_Y))).scope(NodeEffectScope.CHILDREN))
		.body(container -> {
			TextNode.create(122, 160).text(Text.create("MSDF TEXT", ShowUI.font(FontWeight.BOLD, 18F, ShowUI.FAINT).letterSpacing(0.3F))).attach(container);
			final int[] sizes = {14, 18, 24, 32, 44, 60, 80, 108};
			final FontWeight[] weights = {FontWeight.MEDIUM, FontWeight.MEDIUM, FontWeight.SEMI_BOLD, FontWeight.SEMI_BOLD, FontWeight.BOLD, FontWeight.EXTRA_BOLD, FontWeight.BLACK, FontWeight.BLACK};
			double y = 226D;
			for (int i = 0; i < sizes.length; i++) {
				final float progress = i / (float) (sizes.length - 1);
				final Color color = i == sizes.length - 1 ? ShowUI.FUCHSIA.toGradient(ShowUI.CYAN) : ShowUI.MUTED.to(ShowUI.TEXT, progress);
				final TextInfo info = ShowUI.font(weights[i], sizes[i], color).effects(new ShowRiseTextEffect(start + 150L + i * 90L, 18D));
				TextNode.create(120, y).text(Text.create("Crisp at " + sizes[i] + " px", info)).attach(container);
				y += sizes[i] * 1.22D + 20D;
			}

			TextNode.create(1040, 170).text(Text.create("<i>Design</i> once,", TextInfo.create(DemoFont.PLAYFAIR_DISPLAY, FontWeight.REGULAR, 80F, ShowUI.TEXT).markups(DemoTextMarkup.inst()).effects(new ShowRiseTextEffect(start + 300L, 35D)))).attach(container);
			TextNode.create(1040, 272).text(Text.create("render <c=fbbf24><b>anywhere</b></c>.", TextInfo.create(DemoFont.PLAYFAIR_DISPLAY, FontWeight.REGULAR, 80F, ShowUI.TEXT).markups(DemoTextMarkup.inst()).effects(new ShowRiseTextEffect(start + 650L, 35D)))).attach(container);
			TextNode.create(1040, 430).text(Text.create("Hello, world", TextInfo.create(DemoFont.PACIFICO, 92F, Color.WHITE).effects(DemoWaveTextEffect.inst(), DemoRainbowTextEffect.inst()))).attach(container);
			TextNode.create(1044, 610).text(Text.create("DECODING SIGNAL 8.0.0", ShowUI.font(FontWeight.EXTRA_BOLD, 46F, ShowUI.CYAN).letterSpacing(0.08F).effects(new ShowDecodeTextEffect(start + 500L, 70D, ShowUI.PINK)))).attach(container);
			TextNode.create(1044, 716).text(Text.create("<b>Bold</b>, <i>italic</i>, <u>underlined</u>, <h>highlighted</h>, <c=f472b6>colored</c>", ShowUI.font(FontWeight.REGULAR, 32F, ShowUI.TEXT).markups(DemoTextMarkup.inst()))).attach(container);
			TextNode.create(1044, 810).text(Text.create("One distance-field atlas per font:", ShowUI.font(FontWeight.MEDIUM, 30F, ShowUI.MUTED).effects(new ShowRiseTextEffect(start + 900L, 14D)))).attach(container);
			TextNode.create(1044, 856).text(Text.create("every size, every weight, every zoom level.", ShowUI.font(FontWeight.MEDIUM, 30F, ShowUI.MUTED).effects(new ShowRiseTextEffect(start + 1100L, 14D)))).attach(container);
		})
		.attach(this);

		RectNode
		.create(1700, 84, 120, 56)
		.color(Color.WHITE.copyAlpha(0.08F))
		.effect(RoundedNodeEffect.create(28F))
		.body(pill -> {
			TextNode.create(60, 28).text(Text.create(() -> String.format(Locale.US, "%.1f×", 1D + 5D * this.zoom()), ShowUI.font(FontWeight.BOLD, 24F, ShowUI.TEXT))).anchor(Align.CENTER).attach(pill);
		})
		.attach(this);
	}

	private double zoom() {
		return this.ease(TweenEquations.QUINT_INOUT, 2.2D, 3.6D) - this.ease(TweenEquations.QUINT_INOUT, 5.0D, 6.3D);
	}

}