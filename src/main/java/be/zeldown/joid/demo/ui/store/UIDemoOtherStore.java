package be.zeldown.joid.demo.ui.store;

import be.zeldown.joid.demo.DemoFont;
import be.zeldown.joid.demo.ui.UIDemo;
import be.zeldown.joid.demo.ui.store.store.DemoGlobalStore;
import be.zeldown.joid.demo.ui.store.store.DemoLocalStore;
import be.zeldown.joid.demo.ui.store.store.DemoPermanentStore;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.text.builder.Text;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import be.zeldown.joid.lib.ui.node.impl.design.text.TextNode;
import be.zeldown.joid.lib.ui.node.impl.structure.flex.FlexNode;
import be.zeldown.joid.lib.utils.align.Align;

public class UIDemoOtherStore extends UIDemo {

	@Override
	public void init() {
		super.useStore(DemoLocalStore.class, System.currentTimeMillis());
		super.useStore(DemoGlobalStore.class, System.currentTimeMillis());
		super.useStore(DemoPermanentStore.class, System.currentTimeMillis());

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