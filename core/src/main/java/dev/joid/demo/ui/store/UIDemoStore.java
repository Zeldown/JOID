package dev.joid.demo.ui.store;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.core.hook.store.DemoGlobalStore;
import dev.joid.demo.ui.core.hook.store.DemoLocalStore;
import dev.joid.demo.ui.core.hook.store.DemoPermanentStore;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.cache.FileCache;
import dev.joid.lib.cache.FileCacheArea;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.core.hook.property.UIProperty;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoStore extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	private static final FileCacheArea CACHE = FileCache.area("demo").retention(Duration.ofDays(1L));

	@UIProperty
	private int clicks;

	private String cache;

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoStore.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE);
		final long now = BridgeHandler.CLOCK.get().currentTimeMillis();
		final DemoLocalStore local = super.useStore(DemoLocalStore.class, () -> new DemoLocalStore(now));
		final DemoGlobalStore global = super.useStore(DemoGlobalStore.class, () -> new DemoGlobalStore(now));
		final DemoPermanentStore permanent = super.useStore(DemoPermanentStore.class, () -> new DemoPermanentStore(now));
		final long[] times = {local.getTime(), global.getTime(), permanent.getTime()};
		final String[] captions = {"Local store", "Global store", "Permanent store"};
		this.cache = "Not cached";

		for (int i = 0; i < times.length; i++) {
			final int index = i;
			RectNode
			.create(300 + index * 460, 400, 400, 200)
			.color(UIDemoStore.PLACEHOLDER)
			.body(rect -> {
				TextNode.create(200, 100).text(Text.create("Created at " + times[index], info).alignX(Align.CENTER)).anchor(Align.CENTER).attach(rect);
				TextNode.create(200, 215).text(Text.create(captions[index], info)).anchorX(Align.CENTER).attach(rect);
			})
			.attach(this);
		}

		RectNode
		.create(530, 680, 400, 200)
		.color(UIDemoStore.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 75, 120, 50)
			.color(UIDemoStore.INK)
			.onClick(event -> this.clicks++)
			.label(Text.create("+1", label))
			.attach(rect);
			TextNode.create(190, 100).text(() -> Text.create("Clicks: " + this.clicks, info).alignX(Align.START).alignY(Align.CENTER)).anchorY(Align.CENTER).attach(rect);
			TextNode.create(200, 215).text(Text.create("Remembered field", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(990, 680, 400, 200)
		.color(UIDemoStore.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 75, 120, 50)
			.color(UIDemoStore.INK)
			.onClick(event -> {
				final String time = String.valueOf(BridgeHandler.CLOCK.get().currentTimeMillis());
				try {
					final File file = UIDemoStore.CACHE.compute("time", entry -> Files.write(entry.toPath(), time.getBytes(StandardCharsets.UTF_8)));
					this.cache = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).equals(time) ? "Created" : "Reused";
				} catch (final IOException exception) {
					throw new UncheckedIOException(exception);
				}
			})
			.label(Text.create("Cache", label))
			.attach(rect);
			TextNode.create(190, 100).text(() -> Text.create(this.cache, info).alignX(Align.START).alignY(Align.CENTER)).anchorY(Align.CENTER).attach(rect);
			TextNode.create(200, 215).text(Text.create("File cache", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}