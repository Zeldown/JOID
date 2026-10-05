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
import dev.joid.lib.ui.node.property.overflow.OverflowProperty;

public class UIDemoResource extends UIDemo {

	@Override
	public void init() {
		FlexNode.vertical(10, 10, 200).margin(10).body(flex -> {
			ResourceNode.create(0, 0).resource("https://placehold.co/100x100.png").attach(flex);
			ResourceNode.create(0, 0).resource("https://placehold.co/400x400.png").effect(RoundedNodeEffect.create(10F)).size(100, 100).attach(flex);
			ResourceNode.create(0, 0).resource("https://placehold.co/400x400.png").effect(CircleNodeEffect.create()).size(100, 100).attach(flex);
			ResourceNode.create(0, 0).resource("https://placehold.co/400x800.png").stretch(StretchType.CONTAIN).size(100, 100).overflow(OverflowProperty.HIDDEN).attach(flex);
			ResourceNode.create(0, 0).resource("https://placehold.co/800x400.png").height(100).attach(flex);
			ResourceNode.create(0, 0).resource("https://placehold.co/400x800.png").width(100).attach(flex);

			ResourceNode.create(0, 0).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/gif.gif")).linear()).height(100).attach(flex);
		}).attach(this);

		final Resource vector = Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/vector/placeholder.svg"));
		FlexNode.vertical(220, 10, 200).margin(10).body(flex -> {
			ResourceNode.create(0, 0).resource(vector).size(100, 100).attach(flex);
			ResourceNode.create(0, 0).resource(vector).size(50, 50).attach(flex);
			ResourceNode.create(0, 0).resource(vector).size(24, 24).attach(flex);
			ResourceNode.create(0, 0).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/placeholder.webp"))).size(100, 100).attach(flex);
			ResourceNode.create(0, 0).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/placeholder-animated.webp"))).size(100, 100).attach(flex);
			ResourceNode.create(0, 0).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/placeholder-animated.png"))).size(100, 100).attach(flex);
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
	}

}