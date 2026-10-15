package dev.joid.demo.ui.shader;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.render.transform.Rotation;
import dev.joid.lib.render.transform.Transformation;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.shader.impl.GradientShader;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.CircleNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.TransformNodeEffect;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;
import dev.joid.lib.ui.node.impl.resource.ResourceNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.box.BoundingBox;
import dev.joid.lib.utils.point.Point;

@UIData(background = true)
public class UIDemoShader extends UIDemo {

	private static final Color INK = new Color(153, 153, 153).lock();

	@Override
	public void init() {
		final TextInfo caption = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoShader.INK);
		final Resource image = Resource.of("assets/demo/textures/image/placeholder.webp");

		RectNode.create(55, 40, 200, 120).color(Color.RED).attach(this);
		TextNode.create(155, 175).text(Text.create("Solid", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(285, 40, 200, 120).color(Color.BLUE.toGradient(Color.GREEN)).attach(this);
		TextNode.create(385, 175).text(Text.create("Gradient", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(515, 40, 200, 120).color(Color.CYAN.toGradient(Color.MAGENTA, Point.create(0D, 0D), Point.create(0D, 1D))).attach(this);
		TextNode.create(615, 175).text(Text.create("Vertical gradient", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(745, 40, 200, 120).color(Color.ORANGE.toGradient(Color.BLUE, Point.create(0D, 0D), Point.create(1D, 1D))).attach(this);
		TextNode.create(845, 175).text(Text.create("Diagonal gradient", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(1045, 30, 60, 140).color(Color.WHITE).attach(this);
		RectNode.create(975, 40, 200, 120).color(Color.RED.withAlpha(0.5F)).attach(this);
		TextNode.create(1075, 175).text(Text.create("Transparent", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(1205, 40, 200, 120).color(Color.RED.toGradient(Color.RED.withAlpha(0F))).attach(this);
		TextNode.create(1305, 175).text(Text.create("Fading gradient", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(1435, 40, 200, 120).color(Color.RAINBOW).attach(this);
		TextNode.create(1535, 175).text(Text.create("Rainbow", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(1665, 40, 200, 120).color(Color.LOADING).attach(this);
		TextNode.create(1765, 175).text(Text.create("Loading", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(55, 240, 200, 120)
		.layer((node, mouseX, mouseY) -> DrawUtils.SHAPE.drawRoundedRect(0D, 0D, node.getWidth(), node.getHeight(), Color.WHITE, 20F))
		.attach(this);
		TextNode.create(155, 375).text(Text.create("Rounded rect", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(285, 240, 200, 120)
		.layer((node, mouseX, mouseY) -> DrawUtils.SHAPE.drawRoundedRect(0D, 0D, node.getWidth(), node.getHeight(), Color.WHITE, 30F, true, true, false, false))
		.attach(this);
		TextNode.create(385, 375).text(Text.create("Some corners", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(515, 240, 200, 120)
		.layer((node, mouseX, mouseY) -> DrawUtils.SHAPE.drawRoundedBorder(0D, 0D, node.getWidth(), node.getHeight(), Color.WHITE, 20F, 3D))
		.attach(this);
		TextNode.create(615, 375).text(Text.create("Rounded stroke", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(745, 240, 200, 120)
		.layer((node, mouseX, mouseY) -> DrawUtils.SHAPE.drawRoundedBorder(0D, 0D, node.getWidth(), node.getHeight(), Color.ORANGE, 30F, 1D))
		.attach(this);
		TextNode.create(845, 375).text(Text.create("Thin stroke", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(975, 240, 200, 120)
		.layer((node, mouseX, mouseY) -> DrawUtils.SHAPE.drawBorder(0D, 0D, node.getWidth(), node.getHeight(), Color.WHITE, 3D))
		.attach(this);
		TextNode.create(1075, 375).text(Text.create("Border", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(1205, 240, 200, 120)
		.layer((node, mouseX, mouseY) -> DrawUtils.SHAPE.drawBorder(0D, 0D, node.getWidth(), node.getHeight(), Color.WHITE, 6D))
		.attach(this);
		TextNode.create(1305, 375).text(Text.create("Filled border", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(1435, 240, 200, 120)
		.layer((node, mouseX, mouseY) -> DrawUtils.SHAPE.drawCircle(100D, 60D, Color.MAGENTA, 60D))
		.attach(this);
		TextNode.create(1535, 375).text(Text.create("Circle", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(1665, 240, 200, 120)
		.layer((node, mouseX, mouseY) -> DrawUtils.SHAPE.drawPolygon(Color.GREEN, Point.create(100D, 0D), Point.create(200D, 120D), Point.create(0D, 120D)))
		.attach(this);
		TextNode.create(1765, 375).text(Text.create("Polygon", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(55, 440, 200, 120)
		.layer((node, mouseX, mouseY) -> DrawUtils.SHAPE.drawLine(Color.WHITE, 3F, Point.create(0D, 110D), Point.create(200D, 10D)))
		.attach(this);
		TextNode.create(155, 575).text(Text.create("Line", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(285, 440, 200, 120)
		.layer((node, mouseX, mouseY) -> DrawUtils.SHAPE.drawLine(Color.WHITE, 3F, Point.create(0D, 100D), Point.create(50D, 20D), Point.create(100D, 100D), Point.create(150D, 20D), Point.create(200D, 100D)))
		.attach(this);
		TextNode.create(385, 575).text(Text.create("Polyline", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(515, 440, 200, 120)
		.layer((node, mouseX, mouseY) -> DrawUtils.SHAPE.drawCurvedLine(Color.WHITE, 3F, Point.create(0D, 110D), Point.create(100D, -90D), Point.create(200D, 110D)))
		.attach(this);
		TextNode.create(615, 575).text(Text.create("Curve", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(745, 440, 200, 120)
		.layer((node, mouseX, mouseY) -> DrawUtils.SHAPE.drawCurvedLine(Color.WHITE, 3F, Point.create(0D, 60D), Point.create(70D, -60D), Point.create(130D, 180D), Point.create(200D, 60D)))
		.attach(this);
		TextNode.create(845, 575).text(Text.create("Bezier", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(975, 440, 200, 120)
		.layer((node, mouseX, mouseY) -> DrawUtils.SHAPE.drawShape(DrawMode.LINE_LOOP, Color.CYAN, Point.create(100D, 0D), Point.create(200D, 60D), Point.create(100D, 120D), Point.create(0D, 60D)))
		.attach(this);
		TextNode.create(1075, 575).text(Text.create("Line loop", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(1205, 440, 200, 120)
		.layer((node, mouseX, mouseY) -> DrawUtils.SHAPE.drawShape(DrawMode.TRIANGLES, Color.ORANGE, Point.create(0D, 0D), Point.create(90D, 120D), Point.create(0D, 120D), Point.create(110D, 0D), Point.create(200D, 0D), Point.create(200D, 120D)))
		.attach(this);
		TextNode.create(1305, 575).text(Text.create("Triangles", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(1435, 440, 200, 120)
		.layer((node, mouseX, mouseY) -> GradientShader.use(Point.create(0D, 0D), Point.create(1D, 0D), Color.RED, Color.YELLOW, () -> DrawUtils.SHAPE.drawRect(0D, 0D, node.getWidth(), node.getHeight(), Color.WHITE), BoundingBox.create(0D, 0D, node.getWidth(), node.getHeight())))
		.attach(this);
		TextNode.create(1535, 575).text(Text.create("Shader bound", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		TextNode.create(1765, 470).text(Text.create("Gradient", TextInfo.create(DemoFont.MONTSERRAT, 44, Color.RED.toGradient(Color.YELLOW, Point.create(0D, 0D), Point.create(1D, 0D))))).anchorX(Align.CENTER).attach(this);
		TextNode.create(1765, 575).text(Text.create("Gradient text", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(75, 660, 160, 80)
		.color(Color.RED)
		.effect(TransformNodeEffect.create(Transformation.create().rotate(10D, Rotation.ROLL)))
		.attach(this);
		TextNode.create(155, 775).text(Text.create("Rotated", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(305, 660, 160, 80)
		.color(Color.ORANGE.toGradient(Color.PINK))
		.effect(TransformNodeEffect.create(Transformation.create().rotate(-8D, Rotation.ROLL)))
		.attach(this);
		TextNode.create(385, 775).text(Text.create("Rotated gradient", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(535, 660, 160, 80)
		.layer((node, mouseX, mouseY) -> DrawUtils.SHAPE.drawRoundedRect(0D, 0D, node.getWidth(), node.getHeight(), Color.WHITE, 20F))
		.effect(TransformNodeEffect.create(Transformation.create().rotate(10D, Rotation.ROLL)))
		.attach(this);
		TextNode.create(615, 775).text(Text.create("Rotated rounded", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(765, 660, 160, 80)
		.layer((node, mouseX, mouseY) -> DrawUtils.SHAPE.drawRoundedBorder(0D, 0D, node.getWidth(), node.getHeight(), Color.CYAN, 20F, 3D))
		.effect(TransformNodeEffect.create(Transformation.create().rotate(-10D, Rotation.ROLL)))
		.attach(this);
		TextNode.create(845, 775).text(Text.create("Rotated stroke", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(995, 660, 160, 80)
		.color(Color.BLUE)
		.layer((rect, mouseX, mouseY) -> DrawUtils.SHAPE.drawBorder(0D, 0D, rect.getWidth(), rect.getHeight(), Color.WHITE, 3D))
		.effect(TransformNodeEffect.create(Transformation.create().rotate(6D, Rotation.ROLL)))
		.attach(this);
		TextNode.create(1075, 775).text(Text.create("Rotated border", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(1225, 660, 160, 80)
		.layer((node, mouseX, mouseY) -> DrawUtils.SHAPE.drawPolygon(Color.GREEN, Point.create(0D, 80D), Point.create(160D, 80D), Point.create(110D, 0D), Point.create(30D, 0D)))
		.effect(TransformNodeEffect.create(Transformation.create().rotate(12D, Rotation.ROLL)))
		.attach(this);
		TextNode.create(1305, 775).text(Text.create("Rotated polygon", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode
		.create(1475, 640, 120, 120)
		.resource(image)
		.effect(TransformNodeEffect.create(Transformation.create().rotate(10D, Rotation.ROLL)))
		.attach(this);
		TextNode.create(1535, 775).text(Text.create("Rotated image", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(1685, 660, 160, 80)
		.layer((node, mouseX, mouseY) -> GradientShader.use(Point.create(0D, 0D), Point.create(1D, 0D), Color.RED, Color.YELLOW, () -> DrawUtils.SHAPE.drawRect(0D, 0D, node.getWidth(), node.getHeight(), Color.WHITE), BoundingBox.create(0D, 0D, node.getWidth(), node.getHeight())))
		.effect(TransformNodeEffect.create(Transformation.create().rotate(8D, Rotation.ROLL)))
		.attach(this);
		TextNode.create(1765, 775).text(Text.create("Rotated shader", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(95, 840, 120, 120).color(Color.RED.toGradient(Color.YELLOW)).effect(CircleNodeEffect.create()).attach(this);
		TextNode.create(155, 975).text(Text.create("Warm circle", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(325, 840, 120, 120).color(Color.CYAN.toGradient(Color.MAGENTA)).effect(CircleNodeEffect.create()).attach(this);
		TextNode.create(385, 975).text(Text.create("Cool circle", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(555, 840, 120, 120).color(Color.GREEN.toGradient(Color.BLUE, Point.create(0D, 0D), Point.create(0D, 1D))).effect(CircleNodeEffect.create()).attach(this);
		TextNode.create(615, 975).text(Text.create("Vertical circle", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(745, 840, 200, 120).color(Color.RED.toGradient(Color.BLUE)).effect(RoundedNodeEffect.create(20F)).attach(this);
		TextNode.create(845, 975).text(Text.create("Rounded gradient", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(975, 840, 200, 120).color(Color.CYAN.toGradient(Color.MAGENTA, Point.create(0D, 0D), Point.create(0D, 1D))).effect(RoundedNodeEffect.create(30F)).attach(this);
		TextNode.create(1075, 975).text(Text.create("Vertical rounded", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(1205, 840, 200, 120).color(Color.ORANGE.toGradient(Color.PINK)).effect(RoundedNodeEffect.create(60F)).attach(this);
		TextNode.create(1305, 975).text(Text.create("Pill gradient", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		TextNode.create(1535, 870).text(Text.create("Vertical", TextInfo.create(DemoFont.MONTSERRAT, 44, Color.CYAN.toGradient(Color.MAGENTA, Point.create(0D, 0D), Point.create(0D, 1D))))).anchorX(Align.CENTER).attach(this);
		TextNode.create(1535, 975).text(Text.create("Vertical text", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);

		TextNode
		.create(1765, 870)
		.text(Text.create("Bordered", TextInfo.create(DemoFont.MONTSERRAT, 44, Color.RED.toGradient(Color.BLUE))))
		.effect(BorderNodeEffect.create(Color.BLUE.toGradient(Color.RED), 2F))
		.anchorX(Align.CENTER)
		.attach(this);
		TextNode.create(1765, 975).text(Text.create("Bordered text", caption).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(this);
	}

}