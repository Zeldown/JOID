package dev.joid.demo.ui.store;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.core.hook.store.DemoGlobalStore;
import dev.joid.demo.ui.core.hook.store.DemoLocalStore;
import dev.joid.demo.ui.core.hook.store.DemoPermanentStore;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.core.hook.property.UIProperty;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoStore extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@UIProperty
	private int clicks;

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoStore.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE);
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

		RectNode
		.create(760, 680, 400, 200)
		.color(UIDemoStore.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 75, 120, 50)
			.color(UIDemoStore.INK)
			.onClick(event -> this.clicks++)
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("+1", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(190, 100).text(() -> Text.create("Clicks: " + this.clicks, info, Align.START, Align.CENTER)).anchorY(Align.CENTER).attach(rect);
			TextNode.create(200, 215).text(Text.create("Remembered field", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}