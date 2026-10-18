package dev.joid.showcase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.joid.demo.DemoFont;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.impl.layout.ReorderableFlexNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.ui.node.property.OverflowProperty;
import dev.joid.lib.utils.align.Align;

public class ShowLists extends ShowUI {

	private static final Color DRAGGED = Color.decode("#2B2647").lock();

	private final StringSignal next = new StringSignal();

	@Override
	protected void scene() {
		final String[] times   = {"3:42", "4:05", "2:58", "5:11", "3:27", "3:49", "4:33", "2:41", "3:56", "4:18", "3:34", "4:02"};
		final String[] titles  = {"Golden Hour", "Neon Tide", "Velvet", "Aurora", "Midnight", "Paper Planes", "Low Tide", "Saffron", "Glasshouse", "Cinder", "Blue Hour", "Driftwood"};
		final Color[]  colors  = {ShowUI.AMBER, ShowUI.CYAN, ShowUI.PINK, ShowUI.EMERALD, ShowUI.VIOLET, ShowUI.SKY, ShowUI.CYAN, ShowUI.ORANGE, ShowUI.FUCHSIA, ShowUI.ORANGE, ShowUI.SKY, ShowUI.EMERALD};
		final String[] artists = {"Amber Lane", "The Shallows", "Rosa Mendes", "North Lights", "Lo & Behold", "Kite Club", "The Shallows", "Amber Lane", "Mira Sol", "Ash & Oak", "North Lights", "Kite Club"};

		super.backdrop();
		ShowUI.blob(-250, 400, 900, ShowUI.VIOLET.withAlpha(0.40F), 0D, 50D).attach(this);
		ShowUI.blob(1350, -250, 900, ShowUI.CYAN.withAlpha(0.20F), 0.5D, 50D).attach(this);

		final List<String> order = new ArrayList<>(Arrays.asList(titles));
		this.next.set(titles[0]);

		ShowUI.glass(440, 80, 1040, 920, 34F).attach(this);
		TextNode.create(490, 120).text(Text.create("Playlist", ShowUI.font(FontWeight.EXTRA_BOLD, 44F, ShowUI.TEXT))).attach(this);
		TextNode.create(492, 182).text(Text.create("12 tracks  ·  46 min  ·  drag to reorder, scroll for more", ShowUI.font(FontWeight.MEDIUM, 20F, ShowUI.MUTED))).attach(this);
		RectNode
		.create(1130, 124, 300, 56)
		.color(ShowUI.VIOLET.withAlpha(0.18F))
		.effect(RoundedNodeEffect.create(28F))
		.body(chip -> {
			TextNode.create(24, 28).text(Text.create("UP NEXT", ShowUI.font(FontWeight.BOLD, 15F, ShowUI.FAINT).letterSpacing(0.2F))).anchorY(Align.CENTER).attach(chip);
			TextNode.create(276, 28).text(Text.create(this.next.get(), ShowUI.font(FontWeight.BOLD, 21F, ShowUI.TEXT))).anchorX(Align.END).anchorY(Align.CENTER).attach(chip);
		})
		.attach(this);

		RectNode
		.create(470, 240, 980, 730)
		.color(Color.TRANSPARENT)
		.overflow(OverflowProperty.SCROLL)
		.scrollbar(ShowScrollbarNode.create(964, 0, 8, 730))
		.body(area -> {
			final ReorderableFlexNode list = ReorderableFlexNode
			.vertical(20, 0, 920)
			.onReorderEnd(event -> {
				order.add(event.getIndex(), order.remove(event.getPrevious()));
				this.next.set(order.get(0));
			})
			.margin(12D);
			list.body(flex -> {
				for (int i = 0; i < titles.length; i++) {
					this.row(list, titles[i], artists[i], times[i], colors[i]);
				}
			});
			list.attach(area);
		})
		.attach(this);
	}

	private void row(final ReorderableFlexNode list, final String title, final String artist, final String time, final Color color) {
		RectNode
		.create(0, 0, 920, 88)
		.<RectNode>self(row -> row.color(() -> list.isDragging(row) ? ShowLists.DRAGGED : Color.WHITE.withAlpha(0.05F + row.hoverValue(0.05F))))
		.<RectNode>self(row -> row.effect(ShadowNodeEffect.create(color, 30F).color(() -> color.withAlpha(list.isDragging(row) ? 0.55F : 0F))))
		.effect(RoundedNodeEffect.create(20F))
		.body(row -> {
			RectNode
			.create(16, 12, 64, 64)
			.color(ShowUI.diagonal(color, color.to(ShowUI.NIGHT, 0.45F)))
			.effect(RoundedNodeEffect.create(14F))
			.body(art -> {
				TextNode.create(32, 32).text(Text.create(title.substring(0, 1), TextInfo.create(DemoFont.PLAYFAIR_DISPLAY, 32F).color(Color.WHITE))).anchor(Align.CENTER).attach(art);
			})
			.attach(row);
			TextNode.create(102, 30).text(Text.create(title, ShowUI.font(FontWeight.SEMI_BOLD, 24F, ShowUI.TEXT))).anchorY(Align.CENTER).attach(row);
			TextNode.create(102, 62).text(Text.create(artist, ShowUI.font(FontWeight.MEDIUM, 18F, ShowUI.MUTED))).anchorY(Align.CENTER).attach(row);
			TextNode.create(810, 44).text(Text.create(time, ShowUI.font(FontWeight.MEDIUM, 20F, ShowUI.MUTED))).anchorX(Align.END).anchorY(Align.CENTER).attach(row);
			for (int line = 0; line < 3; line++) {
				RectNode.create(848, 34 + line * 9, 28, 3).color(Color.WHITE.withAlpha(0.3F)).effect(RoundedNodeEffect.create(1.5F)).attach(row);
			}
		})
		.hoverDuration(160L)
		.attach(list);
	}

}