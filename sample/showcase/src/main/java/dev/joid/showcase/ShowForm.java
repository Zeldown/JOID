package dev.joid.showcase;

import dev.joid.demo.DemoFont;
import dev.joid.demo.font.markup.DemoTextMarkup;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.node.effect.impl.BlurNodeEffect;
import dev.joid.lib.ui.node.effect.impl.CircleNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.geom.Point;

public class ShowForm extends ShowUI {

	private final StringSignal bio   = new StringSignal("I write <b>the first</b> <c=f472b6>programs</c>");
	private final StringSignal name  = new StringSignal("");
	private final StringSignal email = new StringSignal("");

	@Override
	protected void scene() {
		this.backdrop();
		this.blob(-300, 350, 900, ShowUI.VIOLET.withAlpha(0.40F), 9D, 0D, 50D);
		this.blob(1300, -300, 900, ShowUI.SKY.withAlpha(0.20F), 9D, 0.4D, 50D);
		this.blob(1250, 700, 700, ShowUI.PINK.withAlpha(0.20F), 9D, 0.7D, 50D);

		this.form();
		this.profile();
	}

	private void form() {
		final TextInfo input = ShowUI.font(FontWeight.MEDIUM, 26F, ShowUI.TEXT);
		ShowUI.glass(110, 120, 750, 840, 32F).attach(this);
		TextNode.create(156, 164).text(Text.create("Create your account", ShowUI.font(FontWeight.EXTRA_BOLD, 44F, ShowUI.TEXT))).attach(this);
		TextNode.create(158, 226).text(Text.create("Fields, selection and markup, drawn in your kit.", ShowUI.font(FontWeight.MEDIUM, 20F, ShowUI.MUTED))).attach(this);

		TextNode.create(160, 300).text(Text.create("Full name", ShowUI.font(FontWeight.SEMI_BOLD, 19F, ShowUI.MUTED))).attach(this);
		ShowFieldNode.create(158, 332, 654, ShowUI.VIOLET).info(input).marginHorizontal(24D).placeholder("Your name").text(this.name).attach(this);

		TextNode.create(160, 440).text(Text.create("Email", ShowUI.font(FontWeight.SEMI_BOLD, 19F, ShowUI.MUTED))).attach(this);
		ShowFieldNode.create(158, 472, 654, ShowUI.SKY).info(input).marginHorizontal(24D).placeholder("you@example.com").text(this.email).attach(this);
		CircleNode
		.create(752, 488, 36)
		.color(ShowUI.EMERALD)
		.visible(() -> this.isValid())
		.interactive(false)
		.effect(ShadowNodeEffect.create(ShowUI.EMERALD, 14F))
		.layer((node, mouseX, mouseY) -> DrawUtils.SHAPE.drawLine(ShowUI.NIGHT, 3.5F, Point.create(10D, 18D), Point.create(16D, 24D), Point.create(27D, 11D)))
		.attach(this);

		TextNode.create(160, 580).text(Text.create("Bio, with markup", ShowUI.font(FontWeight.SEMI_BOLD, 19F, ShowUI.MUTED))).attach(this);
		ShowFieldNode.create(158, 612, 654, ShowUI.PINK).info(input.copy().markups(DemoTextMarkup.inst())).marginHorizontal(24D).markup(true).text(this.bio).attach(this);

		RectNode
		.create(158, 770, 654, 76)
		.color(() -> this.isValid() ? ShowUI.VIOLET.toGradient(ShowUI.PINK) : Color.WHITE.withAlpha(0.08F))
		.effect(RoundedNodeEffect.create(20F))
		.effect(ShadowNodeEffect.create(ShowUI.VIOLET, 30F).color(() -> ShowUI.FUCHSIA.withAlpha(this.isValid() ? 0.7F : 0F)))
		.body(button -> {
			TextNode.create(327, 38).text(() -> Text.create("Create account", ShowUI.font(FontWeight.BOLD, 24F, this.isValid() ? Color.WHITE : ShowUI.FAINT))).anchor(Align.CENTER).attach(button);
		})
		.attach(this);
	}

	private void profile() {
		ShowUI.glass(960, 120, 850, 840, 32F).attach(this);
		TextNode.create(1006, 164).text(Text.create("PREVIEW", ShowUI.font(FontWeight.BOLD, 18F, ShowUI.FAINT).letterSpacing(0.25F))).attach(this);
		CircleNode.create(1235, 230, 400).color(ShowUI.VIOLET.withAlpha(0.45F)).effect(BlurNodeEffect.create(70F)).attach(this);
		RectNode
		.create(1305, 300, 200, 200)
		.color(ShowUI.diagonal(ShowUI.FUCHSIA, ShowUI.VIOLET))
		.effect(CircleNodeEffect.create())
		.effect(ShadowNodeEffect.create(ShowUI.VIOLET.withAlpha(0.8F), 36F))
		.body(avatar -> {
			TextNode.create(100, 100).text(() -> Text.create(ShowForm.initials(this.name.get()), ShowUI.font(FontWeight.BLACK, 72F, Color.WHITE))).anchor(Align.CENTER).attach(avatar);
		})
		.attach(this);
		TextNode.create(1405, 560).text(() -> Text.create(this.name.get().isEmpty() ? "Your name" : this.name.get(), TextInfo.create(DemoFont.PLAYFAIR_DISPLAY, FontWeight.REGULAR, 64F, this.name.get().isEmpty() ? ShowUI.FAINT : ShowUI.TEXT))).anchorX(Align.CENTER).attach(this);
		TextNode.create(1405, 660).text(() -> Text.create(this.email.get().isEmpty() ? "you@example.com" : this.email.get(), ShowUI.font(FontWeight.MEDIUM, 24F, ShowUI.MUTED))).anchorX(Align.CENTER).attach(this);
		TextNode.create(1405, 730).text(() -> Text.create(this.bio.get(), ShowUI.font(FontWeight.MEDIUM, 28F, ShowUI.TEXT).markups(DemoTextMarkup.inst()))).anchorX(Align.CENTER).attach(this);
		RectNode
		.create(1300, 810, 210, 48)
		.color(ShowUI.EMERALD.withAlpha(0.16F))
		.visible(() -> this.isValid())
		.effect(RoundedNodeEffect.create(24F))
		.body(chip -> {
			TextNode.create(105, 24).text(Text.create("Verified email", ShowUI.font(FontWeight.BOLD, 19F, ShowUI.EMERALD))).anchor(Align.CENTER).attach(chip);
		})
		.attach(this);
	}

	private boolean isValid() {
		return this.email.get().matches("[^@ ]+@[^@ ]+\\.[a-z]{2,}") && !this.name.get().trim().isEmpty();
	}

	private static String initials(final String name) {
		final StringBuilder builder = new StringBuilder();
		for (final String word : name.trim().split("\\s+")) {
			if (!word.isEmpty() && builder.length() < 2) {
				builder.append(Character.toUpperCase(word.charAt(0)));
			}
		}
		return builder.length() == 0 ? "?" : builder.toString();
	}

}