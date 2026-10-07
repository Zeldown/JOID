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
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class UIDemoStore extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoStore.INK);
		final DemoLocalStore local = super.useStore(DemoLocalStore.class, BridgeHandler.CLOCK.get().currentTimeMillis());
		final DemoGlobalStore global = super.useStore(DemoGlobalStore.class, BridgeHandler.CLOCK.get().currentTimeMillis());
		final DemoPermanentStore permanent = super.useStore(DemoPermanentStore.class, BridgeHandler.CLOCK.get().currentTimeMillis());

		RectNode
		.create(300, 400, 400, 200)
		.color(UIDemoStore.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(200, 100).text(Text.create("Created at " + local.getTime(), info, Align.CENTER)).anchor(Align.CENTER).attach(rect);
			TextNode.create(200, 215).text(Text.create("Local store", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(760, 400, 400, 200)
		.color(UIDemoStore.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(200, 100).text(Text.create("Created at " + global.getTime(), info, Align.CENTER)).anchor(Align.CENTER).attach(rect);
			TextNode.create(200, 215).text(Text.create("Global store", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1220, 400, 400, 200)
		.color(UIDemoStore.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(200, 100).text(Text.create("Created at " + permanent.getTime(), info, Align.CENTER)).anchor(Align.CENTER).attach(rect);
			TextNode.create(200, 215).text(Text.create("Permanent store", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}