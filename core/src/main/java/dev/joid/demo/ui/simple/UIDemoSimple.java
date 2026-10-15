package dev.joid.demo.ui.simple;

import java.util.Arrays;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;
import dev.joid.lib.ui.node.impl.progress.ProgressNode;
import dev.joid.lib.ui.node.impl.progress.ProgressNode.ProgressDirection;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

@UIData(background = true, anchorX = Align.END, anchorY = Align.START)
public class UIDemoSimple extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoSimple.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE);
		final IntegerSignal left = new IntegerSignal(0);
		final IntegerSignal right = new IntegerSignal(0);
		final IntegerSignal starts = new IntegerSignal(0);
		final IntegerSignal ends = new IntegerSignal(0);

		RectNode
		.create(80, 40, 320, 260)
		.color(UIDemoSimple.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(100, 50, 120, 120).color(UIDemoSimple.INK).attach(rect);
			TextNode.create(160, 275).text(Text.create("Rect", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 40, 320, 260)
		.color(UIDemoSimple.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(100, 50, 120, 120).color(UIDemoSimple.INK).hoveredColor(Color.WHITE).attach(rect);
			TextNode.create(160, 275).text(Text.create("Hovered color", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 40, 320, 260)
		.color(UIDemoSimple.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(50, 60, 90, 100).color(Color.WHITE).borderColor(UIDemoSimple.INK).borderStroke(10D).attach(rect);
			RectNode.create(180, 60, 90, 100).color(Color.WHITE).borderColor(UIDemoSimple.INK).borderStroke(10D).borderFill(false).attach(rect);
			TextNode.create(160, 275).text(Text.create("Border corners", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 40, 320, 260)
		.color(UIDemoSimple.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(100, 50, 120, 120).color(UIDemoSimple.INK).borderColor(UIDemoSimple.INK).hoveredBorderColor(Color.WHITE).borderStroke(8D).attach(rect);
			TextNode.create(160, 275).text(Text.create("Hovered border", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 40, 320, 260)
		.color(UIDemoSimple.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(100, 50, 120, 120)
			.color(Color.WHITE)
			.layer((square, mouseX, mouseY) -> DrawUtils.SHAPE.drawCircle(square.dw(2), square.dh(2), UIDemoSimple.INK, 40D))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Layer", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(80, 380, 320, 260)
		.color(UIDemoSimple.PLACEHOLDER)
		.body(rect -> {
			CircleNode.create(100, 50, 120).color(UIDemoSimple.INK).attach(rect);
			TextNode.create(160, 275).text(Text.create("Circle", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 380, 320, 260)
		.color(UIDemoSimple.PLACEHOLDER)
		.body(rect -> {
			CircleNode.create(100, 50, 120).color(UIDemoSimple.INK).hoveredColor(Color.WHITE).attach(rect);
			TextNode.create(160, 275).text(Text.create("Hovered circle", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 380, 320, 260)
		.color(UIDemoSimple.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(100, 50, 120, 120).color(UIDemoSimple.INK).hover(() -> "A single line").attach(rect);
			TextNode.create(160, 275).text(Text.create("Hover text", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 380, 320, 260)
		.color(UIDemoSimple.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(100, 50, 120, 120).color(UIDemoSimple.INK).hover(() -> Arrays.asList("First line", "Second line", "Third line")).attach(rect);
			TextNode.create(160, 275).text(Text.create("Hover lines", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 380, 320, 260)
		.color(UIDemoSimple.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 30, 240, 180)
			.color(Color.WHITE)
			.hover(() -> "Parent")
			.body(parent -> {
				RectNode.create(70, 40, 100, 100).color(UIDemoSimple.INK).hover(() -> "Child").attach(parent);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Nested hover", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(80, 720, 320, 260)
		.color(UIDemoSimple.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoSimple.INK)
			.onClick(event -> left.increment())
			.handleMousePressed(event -> {
				if (!event.getButton().isLeft() && event.getNode().isHovered()) {
					right.increment();
				}
			})
			.label(Text.create("Click", label))
			.attach(rect);
			TextNode.create(40, 120).text(Text.create("Left: " + left.get(), info)).attach(rect);
			TextNode.create(40, 165).text(Text.create("Other: " + right.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Click", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 720, 320, 260)
		.color(UIDemoSimple.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 80)
			.color(UIDemoSimple.INK)
			.hoveredColor(Color.WHITE)
			.onHoverStart(event -> starts.increment())
			.onHoverEnd(event -> ends.increment())
			.attach(rect);
			TextNode.create(40, 145).text(Text.create("Start: " + starts.get(), info)).attach(rect);
			TextNode.create(40, 190).text(Text.create("End: " + ends.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Hover events", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 720, 320, 260)
		.color(UIDemoSimple.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(40, 50, 110, 120).color(UIDemoSimple.INK).hoveredColor(Color.WHITE).attach(rect);
			RectNode.create(170, 50, 110, 120).color(UIDemoSimple.INK).hoveredColor(Color.WHITE).hoverDuration(1500L).hoverEquation(TweenEquations.BOUNCE_OUT).attach(rect);
			TextNode.create(160, 275).text(Text.create("Hover speed", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 720, 320, 260)
		.color(UIDemoSimple.PLACEHOLDER)
		.body(rect -> {
			ProgressNode.create(40, 40, 160, 30).background(Color.WHITE).foreground(UIDemoSimple.INK).progress(0.25F).attach(rect);
			ProgressNode.create(40, 100, 160, 30).background(Color.WHITE).foreground(UIDemoSimple.INK).direction(ProgressDirection.RIGHT_TO_LEFT).progress(0.5F).attach(rect);
			ProgressNode.create(220, 40, 25, 180).background(Color.WHITE).foreground(UIDemoSimple.INK).direction(ProgressDirection.TOP_TO_BOTTOM).progress(0.75F).attach(rect);
			ProgressNode.create(255, 40, 25, 180).background(Color.WHITE).foreground(UIDemoSimple.INK).direction(ProgressDirection.BOTTOM_TO_TOP).progress(0.6F).attach(rect);
			TextNode.create(160, 275).text(Text.create("Progress", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 720, 320, 260)
		.color(UIDemoSimple.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(100, 50, 120, 120)
			.color(UIDemoSimple.INK.withAlpha(0.4F))
			.hoveredColor(Color.WHITE)
			.hover(() -> "Disabled: no hover color, no click")
			.onClick(event -> System.out.println("[UIDemoSimple] never clicked"))
			.enabled(false)
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Disabled", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		ContainerNode
		.create(0, 0, 0, 0)
		.zindex(Integer.MAX_VALUE)
		.interactive(false)
		.onDraw(event -> DrawUtils.SHAPE.drawCircle(event.getMouseX(), event.getMouseY(), UIDemoSimple.INK, 6D))
		.attach(this);
	}

}