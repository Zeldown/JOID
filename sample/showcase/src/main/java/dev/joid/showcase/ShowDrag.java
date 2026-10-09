package dev.joid.showcase;

import dev.joid.demo.DemoFont;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.shader.impl.BorderShader.BorderMode;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.node.effect.NodeEffect.NodeEffectScope;
import dev.joid.lib.ui.node.effect.impl.BlurNodeEffect;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.CircleNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.impl.design.shape.CircleNode;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty.DraggableSnapType;
import dev.joid.lib.utils.align.Align;

public class ShowDrag extends ShowUI {

	private final StringSignal  title  = StringSignal.of("");
	private final StringSignal  artist = StringSignal.of("");
	private final Signal<Color> accent = Signal.of(ShowUI.VIOLET);

	@Override
	protected void scene() {
		this.backdrop();
		this.blob(-300, -300, 900, ShowUI.VIOLET.copyAlpha(0.35F), 8D, 0D, 50D);
		this.blob(1350, 450, 900, ShowUI.CYAN.copyAlpha(0.18F), 8D, 0.5D, 50D);

		this.glass(420, 90, 1080, 460, 36F).attach(this);
		CircleNode.create(475, 130, 360).color(this.accent.get().copyAlpha(0.5F)).effect(BlurNodeEffect.create(80F)).attach(this);

		final RectNode slot = RectNode
		.create(540, 160, 230, 300)
		.color(Color.WHITE.copyAlpha(0.04F))
		.effect(RoundedNodeEffect.create(26F))
		.effect(BorderNodeEffect.create(Color.WHITE.copyAlpha(0.28F), 2F, BorderMode.IN))
		.body(node -> {
			TextNode.create(115, 130).text(Text.create("+", ShowUI.font(FontWeight.LIGHT, 64F, ShowUI.FAINT))).anchor(Align.CENTER).attach(node);
			TextNode.create(115, 190).text(Text.create("Drop a record", ShowUI.font(FontWeight.MEDIUM, 20F, ShowUI.FAINT))).anchor(Align.CENTER).attach(node);
		})
		.attach(this);

		TextNode.create(830, 170).text(Text.create("NOW PLAYING", ShowUI.font(FontWeight.BOLD, 18F, ShowUI.FAINT).letterSpacing(0.25F))).attach(this);
		TextNode.create(826, 212).text(Text.create(this.title.get().isEmpty() ? "Nothing yet" : this.title.get(), TextInfo.create(DemoFont.PLAYFAIR_DISPLAY, FontWeight.REGULAR, 76F, ShowUI.TEXT))).attach(this);
		TextNode.create(830, 318).text(Text.create(this.artist.get().isEmpty() ? "Drag a record from your library" : this.artist.get(), ShowUI.font(FontWeight.MEDIUM, 24F, ShowUI.MUTED))).attach(this);
		RectNode.create(830, 400, 560, 8).color(Color.WHITE.copyAlpha(0.14F)).effect(RoundedNodeEffect.create(4F)).attach(this);
		RectNode
		.create(830, 400, 0, 8)
		.color(this.accent.get().toGradient(Color.WHITE))
		.width(this.title.get().isEmpty() ? 0D : 160D)
		.effect(RoundedNodeEffect.create(4F))
		.effect(ShadowNodeEffect.create(this.accent.get(), 12F))
		.attach(this);
		for (int i = 0; i < 5; i++) {
			final int index = i;
			RectNode
			.create(1300 + i * 18, 0, 10, 0)
			.color(this.accent.get())
			.<RectNode>height(() -> 10D + 34D * Math.abs(Math.sin(this.t() * 4.2D + index * 1.3D)))
			.<RectNode>y(() -> 372D - 10D - 34D * Math.abs(Math.sin(this.t() * 4.2D + index * 1.3D)))
			.visible(!this.title.get().isEmpty())
			.effect(RoundedNodeEffect.create(5F))
			.attach(this);
		}

		TextNode.create(150, 600).text(Text.create("Library", ShowUI.font(FontWeight.EXTRA_BOLD, 34F, ShowUI.TEXT))).attach(this);
		TextNode.create(1770, 610).text(Text.create("5 records", ShowUI.font(FontWeight.MEDIUM, 20F, ShowUI.FAINT))).anchorX(Align.END).attach(this);
		this.record(0, slot, "Golden Hour", "Amber Lane", ShowUI.AMBER, ShowUI.PINK);
		this.record(1, slot, "Neon Tide", "The Shallows", ShowUI.CYAN, ShowUI.VIOLET);
		this.record(2, slot, "Velvet", "Rosa Mendes", ShowUI.PINK, ShowUI.FUCHSIA);
		this.record(3, slot, "Aurora", "North Lights", ShowUI.EMERALD, ShowUI.SKY);
		this.record(4, slot, "Midnight", "Lo & Behold", ShowUI.VIOLET, Color.decode("#1E3A8A"));
	}

	private void record(final int index, final RectNode slot, final String name, final String by, final Color from, final Color to) {
		RectNode
		.create(150 + index * 330, 668, 230, 300)
		.color(Color.TRANSPARENT)
		.draggable(DraggableProperty.ui().snap(DraggableSnapType.OVERLAP, slot))
		.onSnap((node, target) -> {
			Signal.batch(() -> {
				this.title.set(name);
				this.artist.set(by);
				this.accent.set(from);
			});
		})
		.body(card -> {
			RectNode
			.create(0, 0, 230, 230)
			.color(ShowUI.diagonal(from, to))
			.<RectNode>y(() -> -12D * card.hoverValue(1F))
			.self(art -> art.effect(ShadowNodeEffect.create(from.copyAlpha(0.45F), 20F).blur(() -> 20F + art.hoverValue(26F)).offsetY(() -> 10D + art.hoverValue(14F))))
			.effect(RoundedNodeEffect.create(26F).scope(NodeEffectScope.CHILDREN))
			.body(art -> {
				CircleNode.create(70, 30, 210).color(Color.BLACK.copyAlpha(0.28F)).attach(art);
				RectNode.create(145, 105, 60, 60).color(Color.WHITE.copyAlpha(0.85F)).effect(CircleNodeEffect.create()).attach(art);
				CircleNode.create(169, 129, 12).color(Color.BLACK.copyAlpha(0.6F)).attach(art);
				TextNode.create(22, 14).text(Text.create(name.substring(0, 1), TextInfo.create(DemoFont.PLAYFAIR_DISPLAY, FontWeight.REGULAR, 60F, Color.WHITE))).attach(art);
			})
			.attach(card);
			TextNode.create(4, 246).text(Text.create(name, ShowUI.font(FontWeight.SEMI_BOLD, 24F, ShowUI.TEXT))).attach(card);
			TextNode.create(4, 278).text(Text.create(by, ShowUI.font(FontWeight.MEDIUM, 18F, ShowUI.MUTED))).attach(card);
		})
		.hoverDuration(220L)
		.attach(this);
	}

}