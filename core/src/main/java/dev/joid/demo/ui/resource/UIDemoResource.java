package dev.joid.demo.ui.resource;

import dev.joid.demo.ui.UIDemo;
import dev.joid.internal.JOID;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.node.effect.impl.CircleNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.design.resource.ResourceNode;
import dev.joid.lib.ui.node.impl.design.resource.ResourceNode.StretchType;
import dev.joid.lib.ui.node.impl.design.resource.ResourcePlayerNode;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode;
import dev.joid.lib.utils.key.Key;

public class UIDemoResource extends UIDemo {

	@Override
	public void init() {
		FlexNode.vertical(10, 10, 200).margin(10).body(flex -> {
			ResourceNode.create(0, 0).resource(Resource.of("https://placehold.co/100x100.png")).attach(flex);
			ResourceNode.create(0, 0).resource(Resource.of("https://placehold.co/400x400.png")).effect(RoundedNodeEffect.create(10F)).width(100).height(100).attach(flex);
			ResourceNode.create(0, 0).resource(Resource.of("https://placehold.co/400x400.png")).effect(CircleNodeEffect.create()).width(100).height(100).attach(flex);
			ResourceNode.create(0, 0).resource(Resource.of("https://placehold.co/400x800.png")).stretch(StretchType.COVER).width(100).height(100).attach(flex);
			ResourceNode.create(0, 0).resource(Resource.of("https://placehold.co/400x800.png")).stretch(StretchType.CONTAIN).width(100).height(100).attach(flex);
			ResourceNode.create(0, 0).resource(Resource.of("https://placehold.co/800x400.png")).height(100).attach(flex);
			ResourceNode.create(0, 0).resource(Resource.of("https://placehold.co/400x800.png")).width(100).attach(flex);

			ResourceNode.create(0, 0).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/gif.gif")).linear()).height(100).attach(flex);
		}).attach(this);

		final Resource vector = Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/vector/placeholder.svg"));
		FlexNode.vertical(220, 10, 200).margin(10).body(flex -> {
			ResourceNode.create(0, 0).resource(vector).width(24).height(24).attach(flex);
			ResourceNode.create(0, 0).resource(vector).width(50).height(50).attach(flex);
			ResourceNode.create(0, 0).resource(vector).width(100).height(100).attach(flex);
			ResourceNode.create(0, 0).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/placeholder.webp"))).width(100).height(100).attach(flex);
			ResourceNode.create(0, 0).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/placeholder-animated.webp"))).width(100).height(100).attach(flex);
			ResourceNode.create(0, 0).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/placeholder-animated.png"))).width(100).height(100).attach(flex);
			ResourcePlayerNode.create(0, 0, 100, 100).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/videos/placeholder.webm"))).loop(true).attach(flex);
			ResourcePlayerNode
			.create(0, 0, 100, 100)
			.resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/placeholder-animated.webp")))
			.loop(true)
			.onClick((node, mouseX, mouseY, clickType) -> {
				final ResourcePlayerNode player = (ResourcePlayerNode) node;
				if (player.isPlaying()) {
					player.pause();
				} else {
					player.resume();
				}
			})
			.attach(flex);
		}).attach(this);

		final ResourcePlayerNode video = ResourcePlayerNode
				.create(430, 10, 640, 360)
				.resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/videos/video.mp4")))
				.loop(true)
				.attach(this);
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
				video.x(430).y(10).width(640).height(360);
			}
		}, Key.F);
	}

}