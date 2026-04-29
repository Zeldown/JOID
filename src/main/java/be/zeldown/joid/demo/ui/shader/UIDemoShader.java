package be.zeldown.joid.demo.ui.shader;

import javax.vecmath.Vector4f;

import be.zeldown.joid.demo.DemoFont;
import be.zeldown.joid.demo.ui.UIDemo;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.text.builder.Text;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import be.zeldown.joid.lib.resource.Resource;
import be.zeldown.joid.lib.shader.impl.BorderShader.BorderMode;
import be.zeldown.joid.lib.ui.node.effect.NodeEffect.NodeEffectScope;
import be.zeldown.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import be.zeldown.joid.lib.ui.node.effect.impl.CircleNodeEffect;
import be.zeldown.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import be.zeldown.joid.lib.ui.node.impl.design.resource.ResourceNode;
import be.zeldown.joid.lib.ui.node.impl.design.shape.RectNode;
import be.zeldown.joid.lib.ui.node.impl.design.text.TextNode;
import be.zeldown.joid.lib.ui.node.impl.structure.flex.FlexNode;
import be.zeldown.joid.lib.ui.node.property.overflow.OverflowProperty;
import be.zeldown.joid.lib.utils.align.Align;

public class UIDemoShader extends UIDemo {

	@Override
	public void init() {
		final RectNode container = RectNode.create(0, 0, 1920, 1080).color(Color.TRANSPARENT).overflow(OverflowProperty.SCROLL);
		FlexNode.vertical(40, 40, 1840).margin(15).body(flex -> {
			FlexNode.horizontal(0, 0, 120).margin(20).body(node -> {
				RectNode.create(0, 0, 200, 120).color(Color.RED).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.BLUE.toGradient(Color.GREEN)).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.WHITE).effect(RoundedNodeEffect.create(20F)).attach(node);
				RectNode.create(0, 0, 120, 120).color(Color.MAGENTA).effect(CircleNodeEffect.create()).attach(node);
			}).attach(flex);
			FlexNode.horizontal(0, 0, 120).margin(20).body(node -> {
				RectNode.create(0, 0, 200, 120).color(Color.RED.toGradient(Color.BLUE)).effect(RoundedNodeEffect.create(20F)).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.CYAN.toGradient(Color.MAGENTA, new Vector4f(0F, 0F, 0F, 1F))).effect(RoundedNodeEffect.create(30F)).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.ORANGE.toGradient(Color.PINK)).effect(RoundedNodeEffect.create(60F)).attach(node);
			}).attach(flex);
			FlexNode.horizontal(0, 0, 120).margin(20).body(node -> {
				RectNode.create(0, 0, 120, 120).color(Color.RED.toGradient(Color.YELLOW)).effect(CircleNodeEffect.create()).attach(node);
				RectNode.create(0, 0, 120, 120).color(Color.CYAN.toGradient(Color.MAGENTA)).effect(CircleNodeEffect.create()).attach(node);
				RectNode.create(0, 0, 120, 120).color(Color.GREEN.toGradient(Color.BLUE, new Vector4f(0F, 0F, 0F, 1F))).effect(CircleNodeEffect.create()).attach(node);
			}).attach(flex);
			FlexNode.horizontal(0, 0, 130).margin(20).body(node -> {
				RectNode.create(0, 0, 200, 120).color(Color.RED.toGradient(Color.BLUE)).border(Color.WHITE, 3).effect(RoundedNodeEffect.create(20F)).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.ORANGE.toGradient(Color.PINK)).border(Color.WHITE, 3).effect(RoundedNodeEffect.create(40F)).attach(node);
				RectNode.create(0, 0, 120, 120).color(Color.CYAN.toGradient(Color.MAGENTA)).border(Color.WHITE, 3).effect(CircleNodeEffect.create()).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.RED).border(Color.CYAN, 3).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.BLUE).border(Color.WHITE, 3, false).attach(node);
			}).attach(flex);
			FlexNode.horizontal(0, 0, 130).margin(20).body(node -> {
				RectNode.create(0, 0, 200, 120).color(Color.GREEN.toGradient(Color.BLUE)).effect(RoundedNodeEffect.create(20F)).effect(BorderNodeEffect.create(Color.WHITE, 3F, BorderMode.OUT)).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.GREEN.toGradient(Color.BLUE)).effect(RoundedNodeEffect.create(20F)).effect(BorderNodeEffect.create(Color.WHITE, 3F, BorderMode.IN)).attach(node);
				RectNode.create(0, 0, 120, 120).color(Color.RED.toGradient(Color.YELLOW)).effect(CircleNodeEffect.create()).effect(BorderNodeEffect.create(Color.WHITE, 5F, BorderMode.OUT)).attach(node);
				RectNode.create(0, 0, 120, 120).color(Color.RED.toGradient(Color.YELLOW)).effect(CircleNodeEffect.create()).effect(BorderNodeEffect.create(Color.WHITE, 5F, BorderMode.IN)).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.ORANGE).effect(BorderNodeEffect.create(Color.WHITE, 4F, BorderMode.OUT)).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.ORANGE).effect(BorderNodeEffect.create(Color.WHITE, 4F, BorderMode.IN)).attach(node);
			}).attach(flex);
			FlexNode.horizontal(0, 0, 130).margin(20).body(node -> {
				RectNode.create(0, 0, 200, 120).color(Color.GREEN.toGradient(Color.BLUE)).effect(RoundedNodeEffect.create(20F)).effect(BorderNodeEffect.create(Color.WHITE, 3F, BorderMode.OUT)).body(rect -> {
					ResourceNode
					.create(rect.dw(4), rect.dh(4), rect.dw(2), rect.dh(2))
					.resource(Resource.of("https://placehold.co/" + String.format("%.0f", Math.floor(rect.dw(2))) + "x" + String.format("%.0f", Math.floor(rect.dh(2))) + ".png"))
					.attach(rect);
				}).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.GREEN.toGradient(Color.BLUE)).effect(RoundedNodeEffect.create(20F)).effect(BorderNodeEffect.create(Color.WHITE, 3F, BorderMode.IN)).body(rect -> {
					ResourceNode
					.create(rect.dw(4), rect.dh(4), rect.dw(2), rect.dh(2))
					.resource(Resource.of("https://placehold.co/" + String.format("%.0f", Math.floor(rect.dw(2))) + "x" + String.format("%.0f", Math.floor(rect.dh(2))) + ".png"))
					.attach(rect);
				}).attach(node);
				RectNode.create(0, 0, 120, 120).color(Color.RED.toGradient(Color.YELLOW)).effect(CircleNodeEffect.create()).effect(BorderNodeEffect.create(Color.WHITE, 5F, BorderMode.OUT)).body(rect -> {
					ResourceNode
					.create(rect.dw(4), rect.dh(4), rect.dw(2), rect.dh(2))
					.resource(Resource.of("https://placehold.co/" + String.format("%.0f", Math.floor(rect.dw(2))) + "x" + String.format("%.0f", Math.floor(rect.dh(2))) + ".png"))
					.attach(rect);
				}).attach(node);
				RectNode.create(0, 0, 120, 120).color(Color.RED.toGradient(Color.YELLOW)).effect(CircleNodeEffect.create()).effect(BorderNodeEffect.create(Color.WHITE, 5F, BorderMode.IN)).body(rect -> {
					ResourceNode
					.create(rect.dw(4), rect.dh(4), rect.dw(2), rect.dh(2))
					.resource(Resource.of("https://placehold.co/" + String.format("%.0f", Math.floor(rect.dw(2))) + "x" + String.format("%.0f", Math.floor(rect.dh(2))) + ".png"))
					.attach(rect);
				}).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.ORANGE).effect(BorderNodeEffect.create(Color.WHITE, 4F, BorderMode.OUT)).body(rect -> {
					ResourceNode
					.create(rect.dw(4), rect.dh(4), rect.dw(2), rect.dh(2))
					.resource(Resource.of("https://placehold.co/" + String.format("%.0f", Math.floor(rect.dw(2))) + "x" + String.format("%.0f", Math.floor(rect.dh(2))) + ".png"))
					.attach(rect);
				}).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.ORANGE).effect(BorderNodeEffect.create(Color.WHITE, 4F, BorderMode.IN)).body(rect -> {
					ResourceNode
					.create(rect.dw(4), rect.dh(4), rect.dw(2), rect.dh(2))
					.resource(Resource.of("https://placehold.co/" + String.format("%.0f", Math.floor(rect.dw(2))) + "x" + String.format("%.0f", Math.floor(rect.dh(2))) + ".png"))
					.attach(rect);
				}).attach(node);
			}).attach(flex);
			FlexNode.horizontal(0, 0, 60).margin(20).body(node -> {
				TextNode.create(0, 0).text(Text.create("Gradient Text", TextInfo.create(DemoFont.MONTSERRAT, 50, Color.RED.toGradient(Color.BLUE)))).effect(BorderNodeEffect.create(Color.BLUE.toGradient(Color.RED), 2F)).attach(node);
				TextNode.create(0, 0).text(Text.create("Rainbow", TextInfo.create(DemoFont.MONTSERRAT, 50, Color.RED.toGradient(Color.YELLOW, new Vector4f(0F, 0F, 1F, 0F))))).attach(node);
				TextNode.create(0, 0).text(Text.create("Vertical", TextInfo.create(DemoFont.MONTSERRAT, 50, Color.CYAN.toGradient(Color.MAGENTA, new Vector4f(0F, 0F, 0F, 1F))))).attach(node);
			}).attach(flex);
			FlexNode.horizontal(0, 0, 130).margin(20).body(node -> {
				RectNode.create(0, 0, 200, 120).color(Color.RED.copyAlpha(0.1F)).effect(BorderNodeEffect.create(Color.BLUE, 3F)).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.RED.copyAlpha(0.3F)).effect(BorderNodeEffect.create(Color.BLUE, 3F)).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.RED.copyAlpha(0.5F)).effect(BorderNodeEffect.create(Color.BLUE, 3F)).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.RED.copyAlpha(0.75F)).effect(BorderNodeEffect.create(Color.BLUE, 3F)).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.RED).effect(BorderNodeEffect.create(Color.BLUE, 3F)).attach(node);
			}).attach(flex);
			FlexNode.horizontal(0, 0, 130).margin(40).body(node -> {
				final RectNode selfScoped = RectNode.create(0, 0, 200, 120).color(Color.RED).effect(CircleNodeEffect.create().scope(NodeEffectScope.SELF));
				TextNode.create(100, 60).text(Text.create("children node", TextInfo.create(DemoFont.MONTSERRAT, 28, Color.WHITE))).anchor(Align.CENTER).attach(selfScoped);
				selfScoped.attach(node);

				final RectNode childrenScoped = RectNode.create(0, 0, 200, 120).color(Color.RED).effect(CircleNodeEffect.create().scope(NodeEffectScope.CHILDREN));
				TextNode.create(100, 60).text(Text.create("children node", TextInfo.create(DemoFont.MONTSERRAT, 28, Color.WHITE))).anchor(Align.CENTER).attach(childrenScoped);
				childrenScoped.attach(node);
			}).attach(flex);
			FlexNode.horizontal(0, 0, 120).margin(20).body(node -> {
				RectNode.create(0, 0, 200, 120).color(Color.RED.toGradient(Color.BLUE), Color.GREEN.toGradient(Color.YELLOW)).effect(RoundedNodeEffect.create(20F)).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.RED.toGradient(Color.BLUE), Color.CYAN).effect(RoundedNodeEffect.create(20F)).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.ORANGE, Color.CYAN.toGradient(Color.MAGENTA)).effect(RoundedNodeEffect.create(20F)).attach(node);
				RectNode.create(0, 0, 200, 120).color(Color.RED, Color.GREEN).effect(RoundedNodeEffect.create(20F)).attach(node);
				RectNode.create(0, 0, 120, 120).color(Color.RED.toGradient(Color.YELLOW), Color.BLUE.toGradient(Color.GREEN)).effect(CircleNodeEffect.create()).attach(node);
			}).attach(flex);
		}).attach(container);
		container.attach(this);
	}

}