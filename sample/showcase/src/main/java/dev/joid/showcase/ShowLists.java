package dev.joid.showcase;

import java.util.ArrayList;
import java.util.List;

import dev.joid.demo.DemoFont;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.reorderable.ReorderableFlexNode;
import dev.joid.lib.ui.node.property.overflow.OverflowProperty;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.box.BoundingBox;
import dev.joid.lib.utils.signal.impl.primitive.StringSignal;

public class ShowLists extends ShowUI {

	private static final String[] TITLES  = {"Golden Hour", "Neon Tide", "Velvet", "Aurora", "Midnight", "Paper Planes", "Low Tide", "Saffron", "Glasshouse", "Cinder", "Blue Hour", "Driftwood"};
	private static final String[] ARTISTS = {"Amber Lane", "The Shallows", "Rosa Mendes", "North Lights", "Lo & Behold", "Kite Club", "The Shallows", "Amber Lane", "Mira Sol", "Ash & Oak", "North Lights", "Kite Club"};
	private static final String[] TIMES   = {"3:42", "4:05", "2:58", "5:11", "3:27", "3:49", "4:33", "2:41", "3:56", "4:18", "3:34", "4:02"};
	private static final Color[]  COLORS  = {ShowUI.AMBER, ShowUI.CYAN, ShowUI.PINK, ShowUI.EMERALD, ShowUI.VIOLET, ShowUI.SKY, ShowUI.CYAN, ShowUI.ORANGE, ShowUI.FUCHSIA, ShowUI.ORANGE, ShowUI.SKY, ShowUI.EMERALD};

	private final List<String> order = new ArrayList<>();
	private final StringSignal next  = StringSignal.of(ShowLists.TITLES[0]);

	@Override
	protected void scene() {
		this.backdrop();
		this.blob(-250, 400, 900, ShowUI.VIOLET.copyAlpha(0.40F), 9D, 0D, 50D);
		this.blob(1350, -250, 900, ShowUI.CYAN.copyAlpha(0.20F), 9D, 0.5D, 50D);

		for (final String title : ShowLists.TITLES) {
			this.order.add(title);
		}

		this.glass(440, 80, 1040, 920, 34F).attach(this);
		TextNode.create(490, 120).text(Text.create("Playlist", ShowUI.font(FontWeight.EXTRA_BOLD, 44F, ShowUI.TEXT))).attach(this);
		TextNode.create(492, 182).text(Text.create("12 tracks  ·  46 min  ·  drag to reorder, scroll for more", ShowUI.font(FontWeight.MEDIUM, 20F, ShowUI.MUTED))).attach(this);
		RectNode
		.create(1130, 124, 300, 56)
		.color(ShowUI.VIOLET.copyAlpha(0.18F))
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
		.scrollbar(ShowScrollbarNode.create(964, 0, 8, 150, BoundingBox.create(964, 0, 8, 730)))
		.body(area -> {
			final ReorderableFlexNode list = ReorderableFlexNode
			.vertical(20, 0, 920)
			.margin(12D)
			.onReorderEnd((flex, child, oldIndex, newIndex) -> {
				this.order.add(newIndex, this.order.remove(oldIndex));
				this.next.set(this.order.get(0));
			});
			list.body(flex -> {
				for (int i = 0; i < ShowLists.TITLES.length; i++) {
					this.row(list, i);
				}
			});
			list.attach(area);
		})
		.attach(this);
	}

	private void row(final ReorderableFlexNode list, final int index) {
		final Color color = ShowLists.COLORS[index];
		RectNode
		.create(0, 0, 920, 88)
		.<RectNode>self(row -> row.color(() -> list.isDragging(row) ? Color.decode("#2B2647") : Color.WHITE.copyAlpha(0.05F + row.hoverValue(0.05F))))
		.<RectNode>self(row -> row.effect(ShadowNodeEffect.create(color, 30F).color(() -> color.copyAlpha(list.isDragging(row) ? 0.55F : 0F))))
		.effect(RoundedNodeEffect.create(20F))
		.body(row -> {
			RectNode
			.create(16, 12, 64, 64)
			.color(ShowUI.diagonal(color, color.to(ShowUI.NIGHT, 0.45F)))
			.effect(RoundedNodeEffect.create(14F))
			.body(art -> {
				TextNode.create(32, 32).text(Text.create(ShowLists.TITLES[index].substring(0, 1), TextInfo.create(DemoFont.PLAYFAIR_DISPLAY, FontWeight.REGULAR, 32F, Color.WHITE))).anchor(Align.CENTER).attach(art);
			})
			.attach(row);
			TextNode.create(102, 30).text(Text.create(ShowLists.TITLES[index], ShowUI.font(FontWeight.SEMI_BOLD, 24F, ShowUI.TEXT))).anchorY(Align.CENTER).attach(row);
			TextNode.create(102, 62).text(Text.create(ShowLists.ARTISTS[index], ShowUI.font(FontWeight.MEDIUM, 18F, ShowUI.MUTED))).anchorY(Align.CENTER).attach(row);
			TextNode.create(810, 44).text(Text.create(ShowLists.TIMES[index], ShowUI.font(FontWeight.MEDIUM, 20F, ShowUI.MUTED))).anchorX(Align.END).anchorY(Align.CENTER).attach(row);
			for (int line = 0; line < 3; line++) {
				RectNode.create(848, 34 + line * 9, 28, 3).color(Color.WHITE.copyAlpha(0.3F)).effect(RoundedNodeEffect.create(1.5F)).attach(row);
			}
		})
		.hoverDuration(160L)
		.attach(list);
	}

}