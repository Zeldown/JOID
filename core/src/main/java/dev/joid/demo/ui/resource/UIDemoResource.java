package dev.joid.demo.ui.resource;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.internal.JOID;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.node.effect.impl.CircleNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.design.resource.ResourceNode;
import dev.joid.lib.ui.node.impl.design.resource.ResourceNode.StretchType;
import dev.joid.lib.ui.node.impl.design.resource.ResourcePlayerNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.key.Key;

public class UIDemoResource extends UIDemo {

	private static final Color INK = new Color(153, 153, 153);

	@Override
	public void init() {
		final TextInfo caption = TextInfo.create(DemoFont.MONTSERRAT, 18, UIDemoResource.INK);
		final Resource vector = Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/vector/placeholder.svg"));

		FlexNode
		.vertical(40, 40, 880)
		.margin(20)
		.body(flex -> {
			FlexNode
			.horizontal(0, 0, 134)
			.margin(20)
			.align(Align.END)
			.body(node -> {
				FlexNode
				.vertical(0, 0, 160)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					ResourceNode.create(0, 0).resource(Resource.of("https://placehold.co/100x100.png")).attach(column);
					TextNode.create(0, 0).text(Text.create("Natural size", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					ResourceNode
					.create(0, 0)
					.resource(Resource.of("https://placehold.co/400x400.png"))
					.effect(RoundedNodeEffect.create(10F))
					.width(100)
					.height(100)
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Rounded", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					ResourceNode
					.create(0, 0)
					.resource(Resource.of("https://placehold.co/400x400.png"))
					.effect(CircleNodeEffect.create())
					.width(100)
					.height(100)
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Circle", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					ResourceNode
					.create(0, 0)
					.resource(Resource.of("https://placehold.co/400x800.png"))
					.stretch(StretchType.COVER)
					.width(100)
					.height(100)
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Cover", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					ResourceNode
					.create(0, 0)
					.resource(Resource.of("https://placehold.co/400x800.png"))
					.stretch(StretchType.CONTAIN)
					.width(100)
					.height(100)
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Contain", caption)).attach(column);
				})
				.attach(node);
			})
			.attach(flex);
			FlexNode
			.horizontal(0, 0, 234)
			.margin(20)
			.align(Align.END)
			.body(node -> {
				FlexNode
				.vertical(0, 0, 200)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					ResourceNode.create(0, 0).resource(Resource.of("https://placehold.co/800x400.png")).height(100).attach(column);
					TextNode.create(0, 0).text(Text.create("Height only", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					ResourceNode.create(0, 0).resource(Resource.of("https://placehold.co/400x800.png")).width(100).attach(column);
					TextNode.create(0, 0).text(Text.create("Width only", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					ResourceNode
					.create(0, 0)
					.resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/gif.gif")).linear())
					.height(100)
					.attach(column);
					TextNode.create(0, 0).text(Text.create("GIF", caption)).attach(column);
				})
				.attach(node);
			})
			.attach(flex);
			FlexNode
			.horizontal(0, 0, 134)
			.margin(20)
			.align(Align.END)
			.body(node -> {
				FlexNode
				.vertical(0, 0, 160)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					ResourceNode.create(0, 0).resource(vector).width(24).height(24).attach(column);
					TextNode.create(0, 0).text(Text.create("SVG 24", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					ResourceNode.create(0, 0).resource(vector).width(50).height(50).attach(column);
					TextNode.create(0, 0).text(Text.create("SVG 50", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					ResourceNode.create(0, 0).resource(vector).width(100).height(100).attach(column);
					TextNode.create(0, 0).text(Text.create("SVG 100", caption)).attach(column);
				})
				.attach(node);
			})
			.attach(flex);
			FlexNode
			.horizontal(0, 0, 134)
			.margin(20)
			.align(Align.END)
			.body(node -> {
				FlexNode
				.vertical(0, 0, 160)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					ResourceNode
					.create(0, 0)
					.resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/placeholder.webp")))
					.width(100)
					.height(100)
					.attach(column);
					TextNode.create(0, 0).text(Text.create("WebP", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					ResourceNode
					.create(0, 0)
					.resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/placeholder-animated.webp")))
					.width(100)
					.height(100)
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Animated WebP", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					ResourceNode
					.create(0, 0)
					.resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/placeholder-animated.png")))
					.width(100)
					.height(100)
					.attach(column);
					TextNode.create(0, 0).text(Text.create("APNG", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					ResourcePlayerNode
					.create(0, 0, 100, 100)
					.resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/videos/placeholder.webm")))
					.loop(true)
					.attach(column);
					TextNode.create(0, 0).text(Text.create("WebM", caption)).attach(column);
				})
				.attach(node);
				FlexNode
				.vertical(0, 0, 160)
				.margin(8D)
				.align(Align.CENTER)
				.body(column -> {
					ResourcePlayerNode
					.create(0, 0, 100, 100)
					.resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/placeholder-animated.webp")))
					.loop(true)
					.<ResourcePlayerNode>onClick((player, mouseX, mouseY, clickType) -> {
						if (player.isPlaying()) {
							player.pause();
						} else {
							player.resume();
						}
					})
					.attach(column);
					TextNode.create(0, 0).text(Text.create("Click to pause", caption)).attach(column);
				})
				.attach(node);
			})
			.attach(flex);
		})
		.attach(this);

		final ResourcePlayerNode video = ResourcePlayerNode
				.create(1000, 40, 640, 360)
				.resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/videos/video.mp4")))
				.loop(true)
				.attach(this);

		TextNode.create(1320, 408).text(Text.create("Video", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		this.keybind(() -> {
			if (video.isPlaying()) {
				video.pause();
			} else {
				video.resume();
			}
		}, Key.SPACE);
		this.keybind(() -> {
			if (video.getWidth() < this.getWidth()) {
				video.x(0).y(0).width(this.getWidth()).height(this.getHeight());
			} else {
				video.x(1000).y(40).width(640).height(360);
			}
		}, Key.F);
	}

}