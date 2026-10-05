package dev.joid.demo.ui.store;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.store.store.DemoGlobalStore;
import dev.joid.demo.ui.store.store.DemoLocalStore;
import dev.joid.demo.ui.store.store.DemoPermanentStore;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode;
import dev.joid.lib.utils.align.Align;

public class UIDemoStore extends UIDemo {

	@Override
	public void init() {
		super.useStore(DemoLocalStore.class, BridgeHandler.CLOCK.get().currentTimeMillis());
		super.useStore(DemoGlobalStore.class, BridgeHandler.CLOCK.get().currentTimeMillis());
		super.useStore(DemoPermanentStore.class, BridgeHandler.CLOCK.get().currentTimeMillis());

		FlexNode
		.vertical(0, 1080 / 2, 1920)
		.align(Align.CENTER)
		.body(flex -> {
			TextNode
			.create(0, 0)
			.text(Text.create("", TextInfo.create(DemoFont.MONTSERRAT, 30).color(Color.WHITE), Align.CENTER))
			.<TextNode>onInit(node -> {
				final DemoLocalStore store = node.useStore(DemoLocalStore.class);
				node.getText().text("DemoLocalStore: " + store.getTime());
			})
			.attach(flex);

			TextNode
			.create(0, 0)
			.text(Text.create("", TextInfo.create(DemoFont.MONTSERRAT, 30).color(Color.WHITE), Align.CENTER))
			.<TextNode>onInit(node -> {
				final DemoGlobalStore store = node.useStore(DemoGlobalStore.class);
				node.getText().text("DemoGlobalStore: " + store.getTime());
			})
			.attach(flex);

			TextNode
			.create(0, 0)
			.text(Text.create("", TextInfo.create(DemoFont.MONTSERRAT, 30).color(Color.WHITE), Align.CENTER))
			.<TextNode>onInit(node -> {
				final DemoPermanentStore store = node.useStore(DemoPermanentStore.class);
				node.getText().text("DemoPermanentStore: " + store.getTime());
			})
			.attach(flex);
		})
		.attach(this);
	}

}