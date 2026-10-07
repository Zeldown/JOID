package dev.joid.demo.ui.shader;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.internal.JOID;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.render.modifier.Rotation;
import dev.joid.lib.render.modifier.Vector;
import dev.joid.lib.render.transform.operation.RotateOperation;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.shader.impl.BorderShader.BorderMode;
import dev.joid.lib.shader.impl.GradientShader;
import dev.joid.lib.ui.node.effect.NodeEffect.NodeEffectScope;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.CircleNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.effect.impl.TransformNodeEffect;
import dev.joid.lib.ui.node.impl.design.resource.ResourceNode;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode;
import dev.joid.lib.ui.node.property.overflow.OverflowProperty;
import dev.joid.lib.utils.align.Align;

public class UIDemoShader extends UIDemo {

	private static final Color INK = new Color(153, 153, 153);

	@Override
	public void init() {
		final TextInfo caption = TextInfo.create(DemoFont.MONTSERRAT, 18, UIDemoShader.INK);
		final RectNode container = RectNode.create(0, 0, 1920, 1080).color(Color.TRANSPARENT).overflow(OverflowProperty.SCROLL);
		FlexNode
		.vertical(40, 40, 1840)
		.margin(15)
		.body(flex -> {
			FlexNode
			.horizontal(0, 0, 154)
			.margin(20)
			.align(Align.END)
			.body(node -> {
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode.create(0, 0, 200, 120).color(Color.RED).attach(column);
					TextNode.create(0, 0).text(Text.create("Solid", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode.create(0, 0, 200, 120).color(Color.BLUE.toGradient(Color.GREEN)).attach(column);
					TextNode.create(0, 0).text(Text.create("Gradient", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode.create(0, 0, 200, 120).color(Color.WHITE).effect(RoundedNodeEffect.create(20F)).attach(column);
					TextNode.create(0, 0).text(Text.create("Rounded", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode.create(0, 0, 120, 120).color(Color.MAGENTA).effect(CircleNodeEffect.create()).attach(column);
					TextNode.create(0, 0).text(Text.create("Circle", caption)).attach(column);
				})
				.attach(node);
			})
			.attach(flex);
			FlexNode
			.horizontal(0, 0, 154)
			.margin(20)
			.align(Align.END)
			.body(node -> {
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode.create(0, 0, 200, 120).color(Color.GRAY).effect(ShadowNodeEffect.create(Color.WHITE.copyAlpha(0.5F), 16F, 0D, 8D)).attach(column);
					TextNode.create(0, 0).text(Text.create("Shadow", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 200, 120)
					.color(Color.WHITE)
					.effect(RoundedNodeEffect.create(20F))
					.effect(ShadowNodeEffect.create(Color.CYAN, 24F))
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Rounded glow", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 120, 120)
					.color(Color.MAGENTA)
					.effect(CircleNodeEffect.create())
					.effect(ShadowNodeEffect.create(Color.MAGENTA, 24F))
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Circle glow", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 200, 120)
					.color(Color.TRANSPARENT)
					.body(rect -> rect.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawRoundedBorder(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), Color.WHITE, 20F, 3D)))
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Rounded stroke", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 200, 120)
					.color(Color.BLUE)
					.body(rect -> rect.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawRoundedBorder(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), Color.ORANGE, 30F, 1D)))
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Thin stroke", caption)).attach(column);
				})
				.attach(node);
			})
			.attach(flex);
			FlexNode
			.horizontal(0, 0, 154)
			.margin(20)
			.align(Align.END)
			.body(node -> {
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode.create(0, 0, 200, 120).color(Color.RED.toGradient(Color.BLUE)).effect(RoundedNodeEffect.create(20F)).attach(column);
					TextNode.create(0, 0).text(Text.create("Rounded gradient", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 200, 120)
					.color(Color.CYAN.toGradient(Color.MAGENTA, new Vector4f(0F, 0F, 0F, 1F)))
					.effect(RoundedNodeEffect.create(30F))
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Vertical gradient", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode.create(0, 0, 200, 120).color(Color.ORANGE.toGradient(Color.PINK)).effect(RoundedNodeEffect.create(60F)).attach(column);
					TextNode.create(0, 0).text(Text.create("Pill gradient", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(20D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 160, 80)
					.color(Color.RED)
					.self(rect -> rect.effect(TransformNodeEffect.create(new RotateOperation(10D, Rotation.ROLL, Vector.create(() -> rect.getX() + 80D, () -> rect.getY() + 40D)))))
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Rotated", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(20D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 160, 80)
					.color(Color.ORANGE.toGradient(Color.PINK))
					.self(rect -> rect.effect(TransformNodeEffect.create(new RotateOperation(-8D, Rotation.ROLL, Vector.create(() -> rect.getX() + 80D, () -> rect.getY() + 40D)))))
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Rotated gradient", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(20D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 160, 80)
					.color(Color.TRANSPARENT)
					.self(rect -> rect.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawRoundedRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), Color.WHITE, 20F)).effect(TransformNodeEffect.create(new RotateOperation(10D, Rotation.ROLL, Vector.create(() -> rect.getX() + 80D, () -> rect.getY() + 40D)))))
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Rotated rounded", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(20D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 160, 80)
					.color(Color.TRANSPARENT)
					.self(rect -> rect.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawRoundedBorder(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), Color.CYAN, 20F, 3D)).effect(TransformNodeEffect.create(new RotateOperation(-10D, Rotation.ROLL, Vector.create(() -> rect.getX() + 80D, () -> rect.getY() + 40D)))))
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Rotated stroke", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(20D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 160, 80)
					.color(Color.BLUE)
					.self(rect -> rect.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawBorder(rect.getX(), rect.getY(), rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight(), Color.WHITE, 3D)).effect(TransformNodeEffect.create(new RotateOperation(6D, Rotation.ROLL, Vector.create(() -> rect.getX() + 80D, () -> rect.getY() + 40D)))))
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Rotated border", caption)).attach(column);
				})
				.attach(node);
			})
			.attach(flex);
			FlexNode
			.horizontal(0, 0, 154)
			.margin(20)
			.align(Align.END)
			.body(node -> {
				FlexNode
				.vertical(0, 0, 160)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode.create(0, 0, 120, 120).color(Color.RED.toGradient(Color.YELLOW)).effect(CircleNodeEffect.create()).attach(column);
					TextNode.create(0, 0).text(Text.create("Warm circle", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode.create(0, 0, 120, 120).color(Color.CYAN.toGradient(Color.MAGENTA)).effect(CircleNodeEffect.create()).attach(column);
					TextNode.create(0, 0).text(Text.create("Cool circle", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 120, 120)
					.color(Color.GREEN.toGradient(Color.BLUE, new Vector4f(0F, 0F, 0F, 1F)))
					.effect(CircleNodeEffect.create())
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Vertical circle", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(20D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 160, 80)
					.color(Color.TRANSPARENT)
					.self(rect -> rect.layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawPolygon(Color.GREEN, new Vector2d(rect.getX(), rect.getY() + 80D), new Vector2d(rect.getX() + 160D, rect.getY() + 80D), new Vector2d(rect.getX() + 110D, rect.getY()), new Vector2d(rect.getX() + 30D, rect.getY()))).effect(TransformNodeEffect.create(new RotateOperation(12D, Rotation.ROLL, Vector.create(() -> rect.getX() + 80D, () -> rect.getY() + 40D)))))
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Rotated polygon", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					ResourceNode
					.create(0, 0, 120, 120)
					.resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/placeholder.webp")))
					.self(image -> image.effect(TransformNodeEffect.create(new RotateOperation(10D, Rotation.ROLL, Vector.create(() -> image.getX() + 60D, () -> image.getY() + 60D)))))
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Rotated image", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(20D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 160, 80)
					.color(Color.GREEN.toGradient(Color.BLUE))
					.effect(RoundedNodeEffect.create(20F))
					.self(rect -> rect.effect(TransformNodeEffect.create(new RotateOperation(-10D, Rotation.ROLL, Vector.create(() -> rect.getX() + 80D, () -> rect.getY() + 40D)))))
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Rotated effect", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(20D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 160, 80)
					.color(Color.TRANSPARENT)
					.self(rect -> rect.layer((mouseX, mouseY) -> GradientShader.use(new Vector2f(0F, 0F), new Vector2f(1F, 0F), Color.RED, Color.YELLOW, () -> DrawUtils.SHAPE.drawRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), Color.WHITE), new Vector4f((float) rect.getX(), (float) rect.getY(), (float) (rect.getX() + rect.getWidth()), (float) (rect.getY() + rect.getHeight())))).effect(TransformNodeEffect.create(new RotateOperation(8D, Rotation.ROLL, Vector.create(() -> rect.getX() + 80D, () -> rect.getY() + 40D)))))
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Shader bound", caption)).attach(column);
				})
				.attach(node);
			})
			.attach(flex);
			FlexNode
			.horizontal(0, 0, 164)
			.margin(20)
			.align(Align.END)
			.body(node -> {
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 200, 120)
					.color(Color.RED.toGradient(Color.BLUE))
					.borderColor(Color.WHITE)
					.borderStroke(3)
					.effect(RoundedNodeEffect.create(20F))
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Rounded border", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 200, 120)
					.color(Color.ORANGE.toGradient(Color.PINK))
					.borderColor(Color.WHITE)
					.borderStroke(3)
					.effect(RoundedNodeEffect.create(40F))
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Pill border", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 120, 120)
					.color(Color.CYAN.toGradient(Color.MAGENTA))
					.borderColor(Color.WHITE)
					.borderStroke(3)
					.effect(CircleNodeEffect.create())
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Circle border", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 200, 120)
					.color(Color.RED)
					.borderColor(Color.CYAN)
					.borderStroke(3)
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Border", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 200, 120)
					.color(Color.BLUE)
					.borderColor(Color.WHITE)
					.borderStroke(3)
					.borderFill(false)
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Border without fill", caption)).attach(column);
				})
				.attach(node);
			})
			.attach(flex);
			FlexNode
			.horizontal(0, 0, 164)
			.margin(20)
			.align(Align.END)
			.body(node -> {
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 200, 120)
					.color(Color.GREEN.toGradient(Color.BLUE))
					.effect(RoundedNodeEffect.create(20F))
					.effect(BorderNodeEffect.create(Color.WHITE, 3F, BorderMode.OUT))
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Border out", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 200, 120)
					.color(Color.GREEN.toGradient(Color.BLUE))
					.effect(RoundedNodeEffect.create(20F))
					.effect(BorderNodeEffect.create(Color.WHITE, 3F, BorderMode.IN))
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Border in", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 120, 120)
					.color(Color.RED.toGradient(Color.YELLOW))
					.effect(CircleNodeEffect.create())
					.effect(BorderNodeEffect.create(Color.WHITE, 5F, BorderMode.OUT))
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Circle out", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 120, 120)
					.color(Color.RED.toGradient(Color.YELLOW))
					.effect(CircleNodeEffect.create())
					.effect(BorderNodeEffect.create(Color.WHITE, 5F, BorderMode.IN))
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Circle in", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode.create(0, 0, 200, 120).color(Color.ORANGE).effect(BorderNodeEffect.create(Color.WHITE, 4F, BorderMode.OUT)).attach(column);
					TextNode.create(0, 0).text(Text.create("Rect out", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode.create(0, 0, 200, 120).color(Color.ORANGE).effect(BorderNodeEffect.create(Color.WHITE, 4F, BorderMode.IN)).attach(column);
					TextNode.create(0, 0).text(Text.create("Rect in", caption)).attach(column);
				})
				.attach(node);
			})
			.attach(flex);
			FlexNode
			.horizontal(0, 0, 164)
			.margin(20)
			.align(Align.END)
			.body(node -> {
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 200, 120)
					.color(Color.GREEN.toGradient(Color.BLUE))
					.effect(RoundedNodeEffect.create(20F))
					.effect(BorderNodeEffect.create(Color.WHITE, 3F, BorderMode.OUT))
					.body(rect -> {
						ResourceNode
						.create(rect.dw(4), rect.dh(4), rect.dw(2), rect.dh(2))
						.resource(Resource.of("https://placehold.co/" + String.format("%.0f", Math.floor(rect.dw(2))) + "x" + String.format("%.0f", Math.floor(rect.dh(2))) + ".png"))
						.attach(rect);
					})
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Out on image", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 200, 120)
					.color(Color.GREEN.toGradient(Color.BLUE))
					.effect(RoundedNodeEffect.create(20F))
					.effect(BorderNodeEffect.create(Color.WHITE, 3F, BorderMode.IN))
					.body(rect -> {
						ResourceNode
						.create(rect.dw(4), rect.dh(4), rect.dw(2), rect.dh(2))
						.resource(Resource.of("https://placehold.co/" + String.format("%.0f", Math.floor(rect.dw(2))) + "x" + String.format("%.0f", Math.floor(rect.dh(2))) + ".png"))
						.attach(rect);
					})
					.attach(column);
					TextNode.create(0, 0).text(Text.create("In on image", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 120, 120)
					.color(Color.RED.toGradient(Color.YELLOW))
					.effect(CircleNodeEffect.create())
					.effect(BorderNodeEffect.create(Color.WHITE, 5F, BorderMode.OUT))
					.body(rect -> {
						ResourceNode
						.create(rect.dw(4), rect.dh(4), rect.dw(2), rect.dh(2))
						.resource(Resource.of("https://placehold.co/" + String.format("%.0f", Math.floor(rect.dw(2))) + "x" + String.format("%.0f", Math.floor(rect.dh(2))) + ".png"))
						.attach(rect);
					})
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Circle out image", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 120, 120)
					.color(Color.RED.toGradient(Color.YELLOW))
					.effect(CircleNodeEffect.create())
					.effect(BorderNodeEffect.create(Color.WHITE, 5F, BorderMode.IN))
					.body(rect -> {
						ResourceNode
						.create(rect.dw(4), rect.dh(4), rect.dw(2), rect.dh(2))
						.resource(Resource.of("https://placehold.co/" + String.format("%.0f", Math.floor(rect.dw(2))) + "x" + String.format("%.0f", Math.floor(rect.dh(2))) + ".png"))
						.attach(rect);
					})
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Circle in image", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 200, 120)
					.color(Color.ORANGE)
					.effect(BorderNodeEffect.create(Color.WHITE, 4F, BorderMode.OUT))
					.body(rect -> {
						ResourceNode
						.create(rect.dw(4), rect.dh(4), rect.dw(2), rect.dh(2))
						.resource(Resource.of("https://placehold.co/" + String.format("%.0f", Math.floor(rect.dw(2))) + "x" + String.format("%.0f", Math.floor(rect.dh(2))) + ".png"))
						.attach(rect);
					})
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Rect out image", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 200, 120)
					.color(Color.ORANGE)
					.effect(BorderNodeEffect.create(Color.WHITE, 4F, BorderMode.IN))
					.body(rect -> {
						ResourceNode
						.create(rect.dw(4), rect.dh(4), rect.dw(2), rect.dh(2))
						.resource(Resource.of("https://placehold.co/" + String.format("%.0f", Math.floor(rect.dw(2))) + "x" + String.format("%.0f", Math.floor(rect.dh(2))) + ".png"))
						.attach(rect);
					})
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Rect in image", caption)).attach(column);
				})
				.attach(node);
			})
			.attach(flex);
			FlexNode
			.horizontal(0, 0, 94)
			.margin(20)
			.align(Align.END)
			.body(node -> {
				FlexNode
				.vertical(0, 0, 360)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					TextNode
					.create(0, 0)
					.text(Text.create("Gradient Text", TextInfo.create(DemoFont.MONTSERRAT, 50, Color.RED.toGradient(Color.BLUE))))
					.effect(BorderNodeEffect.create(Color.BLUE.toGradient(Color.RED), 2F))
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Bordered gradient", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 360)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					TextNode
					.create(0, 0)
					.text(Text.create("Rainbow", TextInfo.create(DemoFont.MONTSERRAT, 50, Color.RED.toGradient(Color.YELLOW, new Vector4f(0F, 0F, 1F, 0F)))))
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Gradient", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 360)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					TextNode
					.create(0, 0)
					.text(Text.create("Vertical", TextInfo.create(DemoFont.MONTSERRAT, 50, Color.CYAN.toGradient(Color.MAGENTA, new Vector4f(0F, 0F, 0F, 1F)))))
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Vertical gradient", caption)).attach(column);
				})
				.attach(node);
			})
			.attach(flex);
			FlexNode
			.horizontal(0, 0, 164)
			.margin(20)
			.align(Align.END)
			.body(node -> {
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode.create(0, 0, 200, 120).color(Color.RED.copyAlpha(0.1F)).effect(BorderNodeEffect.create(Color.BLUE, 3F)).attach(column);
					TextNode.create(0, 0).text(Text.create("Alpha 0.1", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode.create(0, 0, 200, 120).color(Color.RED.copyAlpha(0.3F)).effect(BorderNodeEffect.create(Color.BLUE, 3F)).attach(column);
					TextNode.create(0, 0).text(Text.create("Alpha 0.3", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode.create(0, 0, 200, 120).color(Color.RED.copyAlpha(0.5F)).effect(BorderNodeEffect.create(Color.BLUE, 3F)).attach(column);
					TextNode.create(0, 0).text(Text.create("Alpha 0.5", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode.create(0, 0, 200, 120).color(Color.RED.copyAlpha(0.75F)).effect(BorderNodeEffect.create(Color.BLUE, 3F)).attach(column);
					TextNode.create(0, 0).text(Text.create("Alpha 0.75", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode.create(0, 0, 200, 120).color(Color.RED).effect(BorderNodeEffect.create(Color.BLUE, 3F)).attach(column);
					TextNode.create(0, 0).text(Text.create("Opaque", caption)).attach(column);
				})
				.attach(node);
			})
			.attach(flex);
			FlexNode
			.horizontal(0, 0, 164)
			.margin(40)
			.align(Align.END)
			.body(node -> {
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 200, 120)
					.color(Color.RED)
					.effect(CircleNodeEffect.create().scope(NodeEffectScope.SELF))
					.body(rect -> {
						TextNode.create(100, 60).text(Text.create("children node", TextInfo.create(DemoFont.MONTSERRAT, 28, Color.WHITE))).anchor(Align.CENTER).attach(rect);
					})
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Self scope", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 200, 120)
					.color(Color.RED)
					.effect(CircleNodeEffect.create().scope(NodeEffectScope.CHILDREN))
					.body(rect -> {
						TextNode.create(100, 60).text(Text.create("children node", TextInfo.create(DemoFont.MONTSERRAT, 28, Color.WHITE))).anchor(Align.CENTER).attach(rect);
					})
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Children scope", caption)).attach(column);
				})
				.attach(node);
			})
			.attach(flex);
			FlexNode
			.horizontal(0, 0, 154)
			.margin(20)
			.align(Align.END)
			.body(node -> {
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 200, 120)
					.color(Color.RED.toGradient(Color.BLUE))
					.hoveredColor(Color.GREEN.toGradient(Color.YELLOW))
					.effect(RoundedNodeEffect.create(20F))
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Hover gradient", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 200, 120)
					.color(Color.RED.toGradient(Color.BLUE))
					.hoveredColor(Color.CYAN)
					.effect(RoundedNodeEffect.create(20F))
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Hover to color", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 200, 120)
					.color(Color.ORANGE)
					.hoveredColor(Color.CYAN.toGradient(Color.MAGENTA))
					.effect(RoundedNodeEffect.create(20F))
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Hover to gradient", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 200, 120)
					.color(Color.RED)
					.hoveredColor(Color.GREEN)
					.effect(RoundedNodeEffect.create(20F))
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Hover color", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					RectNode
					.create(0, 0, 120, 120)
					.color(Color.RED.toGradient(Color.YELLOW))
					.hoveredColor(Color.BLUE.toGradient(Color.GREEN))
					.effect(CircleNodeEffect.create())
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Hover circle", caption)).attach(column);
				})
				.attach(node);
			})
			.attach(flex);
		})
		.attach(container);
		container.attach(this);
	}

}