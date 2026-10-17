package dev.joid.showcase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.ui.node.effect.impl.CircleNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.geom.Point;

public class ShowTheme extends ShowUI {

	private static final Color CARD_DARK    = Color.decode("#17162C").lock();
	private static final Color LINE_DARK    = Color.decode("#24223A").lock();
	private static final Color PAGE_DARK    = Color.decode("#0B0A18").lock();
	private static final Color SIDE_DARK    = Color.decode("#131226").lock();
	private static final Color LINE_LIGHT   = Color.decode("#E7E5F0").lock();
	private static final Color MUTED_DARK   = Color.decode("#A1A1B5").lock();
	private static final Color PAGE_LIGHT   = Color.decode("#F3F2F8").lock();
	private static final Color TEXT_LIGHT   = Color.decode("#16142A").lock();
	private static final Color ACTIVE_DARK  = Color.decode("#C4B5FD").lock();
	private static final Color CURVE_LIGHT  = Color.decode("#0F0E1A").lock();
	private static final Color MUTED_LIGHT  = Color.decode("#6B6880").lock();
	private static final Color ACTIVE_LIGHT = Color.decode("#5B21B6").lock();

	private final BooleanSignal dark  = BooleanSignal.create(false);
	private final TweenAnimator level = TweenAnimator.create(0F);

	@Override
	protected void scene() {
		RectNode.create(0, 0, 1920, 1080).color(() -> this.tone(ShowTheme.PAGE_LIGHT, ShowTheme.PAGE_DARK)).attach(this);
		RectNode.create(0, 0, 300, 1080).color(() -> this.tone(Color.WHITE, ShowTheme.SIDE_DARK)).attach(this);
		RectNode.create(299, 0, 1, 1080).color(() -> this.line()).attach(this);

		RectNode
		.create(48, 56, 52, 52)
		.color(ShowUI.diagonal(ShowUI.FUCHSIA, ShowUI.VIOLET))
		.effect(RoundedNodeEffect.create(16F))
		.effect(ShadowNodeEffect.create(ShowUI.VIOLET.withAlpha(0.6F), 20F))
		.body(logo -> {
			TextNode.create(26, 26).text(Text.create("J", ShowUI.font(FontWeight.BLACK, 28F, Color.WHITE))).anchor(Align.CENTER).attach(logo);
		})
		.attach(this);
		TextNode.create(118, 82).text(() -> Text.create("Studio", ShowUI.font(FontWeight.EXTRA_BOLD, 28F, this.text()))).anchorY(Align.CENTER).attach(this);
		final String[] items = {"Overview", "Analytics", "Library", "Team", "Settings"};
		for (int i = 0; i < items.length; i++) {
			final boolean active = i == 0;
			final String item = items[i];
			RectNode.create(32, 170 + i * 68, 236, 54).color(() -> active ? ShowUI.VIOLET.withAlpha(0.14F + 0.1F * this.level.get()) : Color.TRANSPARENT).effect(RoundedNodeEffect.create(16F)).attach(this);
			RectNode.create(56, 189 + i * 68, 16, 16).color(() -> active ? ShowUI.VIOLET : this.muted()).effect(RoundedNodeEffect.create(5F)).attach(this);
			TextNode.create(92, 197 + i * 68).text(() -> Text.create(item, ShowUI.font(FontWeight.SEMI_BOLD, 21F, active ? this.tone(ShowTheme.ACTIVE_LIGHT, ShowTheme.ACTIVE_DARK) : this.muted()))).anchorY(Align.CENTER).attach(this);
		}

		TextNode.create(360, 60).text(() -> Text.create("Good evening, Ada", ShowUI.font(FontWeight.EXTRA_BOLD, 46F, this.text()))).attach(this);
		TextNode.create(362, 124).text(() -> Text.create("One signal drives every color on this screen.", ShowUI.font(FontWeight.MEDIUM, 21F, this.muted()))).attach(this);
		TextNode.create(1700, 98).text(() -> Text.create(this.dark.get() ? "Dark" : "Light", ShowUI.font(FontWeight.SEMI_BOLD, 21F, this.muted()))).anchorX(Align.END).anchorY(Align.CENTER).attach(this);
		ShowThemeNode.create(1730, 70).toggle(this.dark).onChange(event -> this.level.sequence(400F, event.getValue() ? 1F : 0F, TweenEquations.QUAD_OUT).start()).attach(this);

		this.stat(0, "Listeners", "48,210", "+12.4 %", ShowUI.VIOLET);
		this.stat(1, "Minutes played", "1.2 M", "+8.1 %", ShowUI.PINK);
		this.stat(2, "New followers", "3,904", "+21.7 %", ShowUI.CYAN);

		this.card(360, 430, 960, 590).attach(this);
		TextNode.create(400, 470).text(() -> Text.create("Weekly plays", ShowUI.font(FontWeight.BOLD, 26F, this.text()))).attach(this);
		TextNode.create(400, 508).text(() -> Text.create("Last 12 weeks", ShowUI.font(FontWeight.MEDIUM, 18F, this.muted()))).attach(this);
		RectNode.create(400, 560, 880, 420).layer((node, mouseX, mouseY) -> this.chart(0D, 0D, 880D, 420D)).attach(this);

		this.card(1360, 430, 500, 590).attach(this);
		TextNode.create(1400, 470).text(() -> Text.create("Top artists", ShowUI.font(FontWeight.BOLD, 26F, this.text()))).attach(this);
		final String[] artists = {"Amber Lane", "The Shallows", "Rosa Mendes", "North Lights", "Kite Club"};
		final Color[] colors = {ShowUI.AMBER, ShowUI.CYAN, ShowUI.PINK, ShowUI.EMERALD, ShowUI.SKY};
		for (int i = 0; i < artists.length; i++) {
			final double y = 540D + i * 92D;
			final String artist = artists[i];
			final int plays = 9400 - i * 1350;
			RectNode.create(1400, y, 60, 60).color(ShowUI.diagonal(colors[i], colors[i].to(ShowUI.VIOLET, 0.5F))).effect(CircleNodeEffect.create()).attach(this);
			TextNode.create(1480, y + 18D).text(() -> Text.create(artist, ShowUI.font(FontWeight.SEMI_BOLD, 22F, this.text()))).anchorY(Align.CENTER).attach(this);
			TextNode.create(1480, y + 46D).text(() -> Text.create(String.format(Locale.US, "%,d plays", plays), ShowUI.font(FontWeight.MEDIUM, 17F, this.muted()))).anchorY(Align.CENTER).attach(this);
		}
	}

	private void stat(final int index, final String label, final String value, final String delta, final Color accent) {
		final double x = 360D + index * 500D;
		this.card(x, 190, 460, 200).attach(this);
		RectNode
		.create(x + 40D, 230, 44, 44)
		.color(accent.withAlpha(0.18F))
		.effect(RoundedNodeEffect.create(14F))
		.body(icon -> {
			RectNode.create(14, 14, 16, 16).color(accent).effect(CircleNodeEffect.create()).attach(icon);
		})
		.attach(this);
		TextNode.create(x + 100D, 252).text(() -> Text.create(label, ShowUI.font(FontWeight.SEMI_BOLD, 20F, this.muted()))).anchorY(Align.CENTER).attach(this);
		TextNode.create(x + 38D, 300).text(() -> Text.create(value, ShowUI.font(FontWeight.BLACK, 50F, this.text()))).attach(this);
		TextNode.create(x + 420D, 330).text(Text.create(delta, ShowUI.font(FontWeight.BOLD, 20F, ShowUI.EMERALD))).anchorX(Align.END).anchorY(Align.CENTER).attach(this);
	}

	private RectNode card(final double x, final double y, final double width, final double height) {
		return RectNode
		.create(x, y, width, height)
		.color(() -> this.tone(Color.WHITE, ShowTheme.CARD_DARK))
		.effect(RoundedNodeEffect.create(28F))
		.effect(ShadowNodeEffect.create(Color.BLACK, 40F).offsetY(14D).color(() -> Color.BLACK.withAlpha(0.07F + 0.33F * this.level.get())));
	}

	private void chart(final double x, final double y, final double width, final double height) {
		final double[] values = {0.42D, 0.55D, 0.48D, 0.62D, 0.58D, 0.71D, 0.66D, 0.78D, 0.74D, 0.86D, 0.81D, 0.95D};
		for (int i = 0; i <= 4; i++) {
			DrawUtils.SHAPE.drawRect(x, y + i * (height - 40D) / 4D, width, 1D, this.line());
		}
		final double slot = width / values.length;
		final List<Point> points = new ArrayList<>();
		for (int i = 0; i < values.length; i++) {
			final double bar = values[i] * (height - 40D);
			final double bx = x + i * slot + slot * 0.2D;
			DrawUtils.SHAPE.drawRoundedRect(bx, y + height - 40D - bar, slot * 0.6D, bar, ShowUI.vertical(ShowUI.FUCHSIA, ShowUI.VIOLET.withAlpha(0.35F)), 10F, true, true, true, false);
			points.add(Point.of(bx + slot * 0.3D, y + height - 40D - bar - 26D));
		}
		DrawUtils.SHAPE.drawLine(this.tone(ShowTheme.CURVE_LIGHT, ShowUI.TEXT).withAlpha(0.7F), 3F, points.toArray(new Point[0]));
	}

	private Color text() {
		return this.tone(ShowTheme.TEXT_LIGHT, ShowUI.TEXT);
	}

	private Color muted() {
		return this.tone(ShowTheme.MUTED_LIGHT, ShowTheme.MUTED_DARK);
	}

	private Color line() {
		return this.tone(ShowTheme.LINE_LIGHT, ShowTheme.LINE_DARK);
	}

	private Color tone(final Color light, final Color dark) {
		return light.to(dark, this.level.get());
	}

}