package dev.joid.demo.ui.resource;

import dev.joid.demo.ui.UIDemo;
import dev.joid.internal.JOID;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.node.effect.impl.CircleNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.design.resource.ResourceNode;
import dev.joid.lib.ui.node.impl.design.resource.ResourceNode.StretchType;
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
	}

}