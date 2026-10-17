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
		final long[] times = {local.getTime(), global.getTime(), permanent.getTime()};
		final String[] captions = {"Local store", "Global store", "Permanent store"};

		for (int i = 0; i < times.length; i++) {
			final int index = i;
			RectNode
			.create(300 + index * 460, 400, 400, 200)
			.color(UIDemoOtherStore.PLACEHOLDER)
			.body(rect -> {
				TextNode.create(200, 100).text(Text.create("Created at " + times[index], info).alignX(Align.CENTER)).anchor(Align.CENTER).attach(rect);
				TextNode.create(200, 215).text(Text.create(captions[index], info)).anchorX(Align.CENTER).attach(rect);
			})
			.attach(this);
		}
	}

}