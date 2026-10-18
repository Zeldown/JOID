package dev.joid.demo.ui.shader;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.node.effect.impl.DemoInvertNodeEffect;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.render.transform.Scale;
import dev.joid.lib.render.transform.Transformation;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.shader.impl.GradientShader;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.effect.impl.BlurNodeEffect;
import dev.joid.lib.ui.node.effect.impl.MaskNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.effect.impl.TransformNodeEffect;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;
import dev.joid.lib.ui.node.impl.resource.ResourceNode;
import dev.joid.lib.ui.node.impl.resource.ResourceNode.StretchType;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.ui.node.layer.NodeLayer;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.geom.BoundingBox;
import dev.joid.lib.utils.geom.Point;

@UIData(background = true)
public class UIDemoShader extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo caption = TextInfo.create(DemoFont.MONTSERRAT, 24).color(UIDemoShader.INK);
		final Resource image = Resource.of("assets/demo/textures/image/placeholder.webp");
		final Resource mask = Resource.of("assets/demo/textures/vector/mask.svg");
		final Color[] fills = {
			UIDemoShader.INK,
			UIDemoShader.PLACEHOLDER.toGradient(UIDemoShader.INK),
			UIDemoShader.PLACEHOLDER.toGradient(UIDemoShader.INK, Point.of(0D, 0D), Point.of(0D, 1D)),
			UIDemoShader.PLACEHOLDER.toGradient(UIDemoShader.INK, Point.of(0D, 0D), Point.of(1D, 1D)),
			UIDemoShader.INK.withAlpha(0.5F),
			UIDemoShader.INK.toGradient(UIDemoShader.INK.withAlpha(0F)),
			Color.RAINBOW,
			Color.LOADING
		};
		final String[] fillNames = {"Solid", "Gradient", "Vertical gradient", "Diagonal gradient", "Transparent", "Fading gradient", "Rainbow", "Loading"};
		final NodeLayer[] layers = {
			(node, mouseX, mouseY) -> DrawUtils.SHAPE.drawRoundedRect(0D, 0D, node.getWidth(), node.getHeight(), Color.WHITE, 20F),
			(node, mouseX, mouseY) -> DrawUtils.SHAPE.drawRoundedRect(0D, 0D, node.getWidth(), node.getHeight(), Color.WHITE, 30F, true, true, false, false),
			(node, mouseX, mouseY) -> DrawUtils.SHAPE.drawRoundedBorder(0D, 0D, node.getWidth(), node.getHeight(), Color.WHITE, 20F, 3D),
			(node, mouseX, mouseY) -> DrawUtils.SHAPE.drawRoundedBorder(0D, 0D, node.getWidth(), node.getHeight(), UIDemoShader.INK, 30F, 1D),
			(node, mouseX, mouseY) -> DrawUtils.SHAPE.drawBorder(0D, 0D, node.getWidth(), node.getHeight(), Color.WHITE, 3D),
			(node, mouseX, mouseY) -> DrawUtils.SHAPE.drawBorder(0D, 0D, node.getWidth(), node.getHeight(), Color.WHITE, 6D),
			(node, mouseX, mouseY) -> DrawUtils.SHAPE.drawCircle(100D, 60D, UIDemoShader.INK, 60D),
			(node, mouseX, mouseY) -> DrawUtils.SHAPE.drawPolygon(UIDemoShader.INK, Point.of(100D, 0D), Point.of(200D, 120D), Point.of(0D, 120D)),
			(node, mouseX, mouseY) -> DrawUtils.SHAPE.drawLine(Color.WHITE, 3F, Point.of(0D, 110D), Point.of(200D, 10D)),
			(node, mouseX, mouseY) -> DrawUtils.SHAPE.drawLine(Color.WHITE, 3F, Point.of(0D, 100D), Point.of(50D, 20D), Point.of(100D, 100D), Point.of(150D, 20D), Point.of(200D, 100D)),
			(node, mouseX, mouseY) -> DrawUtils.SHAPE.drawCurvedLine(Color.WHITE, 3F, Point.of(0D, 110D), Point.of(100D, -90D), Point.of(200D, 110D)),
			(node, mouseX, mouseY) -> DrawUtils.SHAPE.drawCurvedLine(Color.WHITE, 3F, Point.of(0D, 60D), Point.of(70D, -60D), Point.of(130D, 180D), Point.of(200D, 60D)),
			(node, mouseX, mouseY) -> DrawUtils.SHAPE.drawShape(DrawMode.LINE_LOOP, UIDemoShader.INK, Point.of(100D, 0D), Point.of(200D, 60D), Point.of(100D, 120D), Point.of(0D, 60D)),
			(node, mouseX, mouseY) -> DrawUtils.SHAPE.drawShape(DrawMode.TRIANGLES, UIDemoShader.INK, Point.of(0D, 0D), Point.of(90D, 120D), Point.of(0D, 120D), Point.of(110D, 0D), Point.of(200D, 0D), Point.of(200D, 120D)),
			(node, mouseX, mouseY) -> GradientShader.use(Point.of(0D, 0D), Point.of(1D, 0D), UIDemoShader.PLACEHOLDER, UIDemoShader.INK, () -> DrawUtils.SHAPE.drawRect(0D, 0D, node.getWidth(), node.getHeight(), Color.WHITE), BoundingBox.create(0D, 0D, node.getWidth(), node.getHeight()))
		};
		final String[] names = {"Rounded rect", "Some corners", "Rounded stroke", "Thin stroke", "Border", "Filled border", "Circle", "Polygon", "Line", "Polyline", "Curve", "Bezier", "Line loop", "Triangles", "Shader bound"};

		RectNode.create(1045, 30, 60, 140).color(Color.WHITE).attach(this);
		for (int i = 0; i < fills.length; i++) {
			RectNode.create(55 + i * 230, 40, 200, 120).color(fills[i]).attach(this);
			TextNode.create(155 + i * 230, 175).text(Text.create(fillNames[i], caption)).anchorX(Align.CENTER).attach(this);
		}

		for (int i = 0; i < layers.length; i++) {
			ContainerNode.create(55 + i % 8 * 230, 240 + i / 8 * 200, 200, 120).layer(layers[i]).attach(this);
			TextNode.create(155 + i % 8 * 230, 375 + i / 8 * 200).text(Text.create(names[i], caption)).anchorX(Align.CENTER).attach(this);
		}

		TextNode.create(1765, 470).text(Text.create("Gradient", TextInfo.create(DemoFont.MONTSERRAT, 44).color(UIDemoShader.PLACEHOLDER.toGradient(UIDemoShader.INK, Point.of(0D, 0D), Point.of(1D, 0D))))).anchorX(Align.CENTER).attach(this);
		TextNode.create(1765, 575).text(Text.create("Gradient text", caption)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(515, 640, 200, 120)
		.layer((node, mouseX, mouseY) -> super.mask(40D, 20D, 120D, 80D, () -> DrawUtils.SHAPE.drawRect(0D, 0D, node.getWidth(), node.getHeight(), UIDemoShader.INK)))
		.attach(this);
		TextNode.create(615, 775).text(Text.create("Mask", caption)).anchorX(Align.CENTER).attach(this);

		ContainerNode
		.create(745, 640, 200, 120)
		.layer((node, mouseX, mouseY) -> super.mask(mask, 40D, 0D, 120D, 120D, () -> DrawUtils.SHAPE.drawRect(0D, 0D, node.getWidth(), node.getHeight(), UIDemoShader.INK)))
		.attach(this);
		TextNode.create(845, 775).text(Text.create("Image mask", caption)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(975, 640, 200, 120).resource(image).stretch(StretchType.COVER).effect(DemoInvertNodeEffect.create(1F)).attach(this);
		TextNode.create(1075, 775).text(Text.create("Custom effect", caption)).anchorX(Align.CENTER).attach(this);

		RectNode
		.create(1205, 640, 200, 120)
		.color(UIDemoShader.PLACEHOLDER)
		.effect(MaskNodeEffect.create())
		.body(rect -> {
			ResourceNode
			.create(0, 0, 200, 120)
			.resource(image)
			.stretch(StretchType.COVER)
			.effect(BlurNodeEffect.create(2F))
			.effect(ShadowNodeEffect.create(UIDemoShader.INK, 16F))
			.effect(TransformNodeEffect.create(Transformation.create().scale(Scale.create(12D, 12D, 1D))))
			.attach(rect);
		})
		.attach(this);
		TextNode.create(1305, 775).text(Text.create("Zoomed effects", caption)).anchorX(Align.CENTER).attach(this);
	}

}