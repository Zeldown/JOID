package dev.joid.demo.ui.store;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.core.hook.store.DemoGlobalStore;
import dev.joid.demo.ui.core.hook.store.DemoLocalStore;
import dev.joid.demo.ui.core.hook.store.DemoPermanentStore;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoOtherStore extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoOtherStore.INK);
		final long now = BridgeHandler.CLOCK.get().currentTimeMillis();
		final DemoLocalStore local = super.useStore(DemoLocalStore.class, () -> new DemoLocalStore(now));
		final DemoGlobalStore global = super.useStore(DemoGlobalStore.class, () -> new DemoGlobalStore(now));
		final DemoPermanentStore permanent = super.useStore(DemoPermanentStore.class, () -> new DemoPermanentStore(now));

		RectNode
		.create(300, 400, 400, 200)
		.color(UIDemoOtherStore.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(200, 100).text(Text.create("Created at " + local.getTime(), info).alignX(Align.CENTER)).anchor(Align.CENTER).attach(rect);
			TextNode.create(200, 215).text(Text.create("Local store", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(760, 400, 400, 200)
		.color(UIDemoOtherStore.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(200, 100).text(Text.create("Created at " + global.getTime(), info).alignX(Align.CENTER)).anchor(Align.CENTER).attach(rect);
			TextNode.create(200, 215).text(Text.create("Global store", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1220, 400, 400, 200)
		.color(UIDemoOtherStore.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(200, 100).text(Text.create("Created at " + permanent.getTime(), info).alignX(Align.CENTER)).anchor(Align.CENTER).attach(rect);
			TextNode.create(200, 215).text(Text.create("Permanent store", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}