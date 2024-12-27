package be.zeldown.joid.demo.ui.resource;

import be.zeldown.joid.demo.ui.UIDemo;
import be.zeldown.joid.lib.ui.node.impl.design.resource.ResourceNode;
import be.zeldown.joid.lib.ui.node.impl.design.resource.ResourceNode.StretchType;
import be.zeldown.joid.lib.ui.node.impl.structure.flex.FlexNode;

public class UIDemoResource extends UIDemo {

	@Override
	public void init() {
		FlexNode.vertical(10, 10, 200).margin(10).body(flex -> {
			ResourceNode.create(0, 0).resource("https://placehold.co/100x100.png").attach(flex);
			ResourceNode.create(0, 0).resource("https://placehold.co/400x400.png").size(100, 100).attach(flex);
			ResourceNode.create(0, 0).resource("https://placehold.co/400x800.png").stretch(StretchType.CONTAIN).size(100, 100).stencilBox(100, 100).attach(flex);
			ResourceNode.create(0, 0).resource("https://placehold.co/800x400.png").height(100).attach(flex);
			ResourceNode.create(0, 0).resource("https://placehold.co/400x800.png").width(100).attach(flex);
		}).attach(this);
	}

}