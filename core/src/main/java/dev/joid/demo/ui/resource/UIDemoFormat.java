package dev.joid.demo.ui.resource;

import dev.joid.demo.ui.UIDemo;
import dev.joid.internal.JOID;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.node.impl.design.resource.ResourceNode;
import dev.joid.lib.ui.node.impl.design.video.VideoPlayerNode;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode;

public class UIDemoFormat extends UIDemo {

	@Override
	public void init() {
		final Resource logo = Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/vector/logo.svg"));
		FlexNode.horizontal(60, 60, 384).margin(40).body(flex -> {
			ResourceNode.create(0, 0, 24, 24).resource(logo).attach(flex);
			ResourceNode.create(0, 0, 48, 48).resource(logo).attach(flex);
			ResourceNode.create(0, 0, 96, 96).resource(logo).attach(flex);
			ResourceNode.create(0, 0, 192, 192).resource(logo).attach(flex);
			ResourceNode.create(0, 0, 384, 384).resource(logo).attach(flex);
		}).attach(this);

		ResourceNode.create(60, 560, 256, 256).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/still.webp"))).attach(this);
		ResourceNode.create(60, 560, 256, 256).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/videos/alpha.webm"))).attach(this);
		ResourceNode.create(376, 608, 160, 160).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/animated.webp"))).attach(this);
		ResourceNode.create(596, 608, 160, 160).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/animated.png"))).attach(this);
		ResourceNode.create(816, 608, 0, 160).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/gif.gif"))).attach(this);
		VideoPlayerNode
				.create(1200, 560, 256, 256)
				.resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/animated.webp")))
				.loop(true)
				.onClick((node, mouseX, mouseY, clickType) -> {
					final VideoPlayerNode player = (VideoPlayerNode) node;
					if (player.isPlaying()) {
						player.pause();
					} else {
						player.resume();
					}
				})
				.attach(this);
	}

}