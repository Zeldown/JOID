package dev.joid.lib.ui.node.impl.design.text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.draw.text.builder.utils.TextOverflow;
import dev.joid.lib.draw.text.utils.TextMode;
import dev.joid.lib.font.IFont;
import dev.joid.lib.font.IFontProvider;
import dev.joid.lib.font.dto.FontBounds;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.utils.align.Align;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class TextNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private final TextFont font = new TextFont();

	@Test
	public void startsWithoutTextInNormalMode() {
		final TextNode node = TextNode.create(10D, 20D);
		Assert.assertNull(node.getText());
		Assert.assertSame(TextMode.NORMAL, node.getMode());
		Assert.assertFalse(node.isInitialized());
		Assert.assertEquals(10D, node.getX(), 0D);
		Assert.assertEquals(20D, node.getY(), 0D);
		Assert.assertEquals(0D, node.getWidth(), 0D);
		Assert.assertEquals(0D, node.getHeight(), 0D);
	}

	@Test
	public void takesItsTextAndItsMode() {
		final Text text = this.text("hello");
		final TextNode node = TextNode.create(10D, 20D);
		Assert.assertSame(node, node.text(text));
		Assert.assertSame(node, node.mode(TextMode.SPLIT));
		Assert.assertSame(text, node.getText());
		Assert.assertSame(TextMode.SPLIT, node.getMode());
		Assert.assertNull(node.text((Text) null).getText());
	}

	@Test
	public void sizesItselfToItsTextOnceAttached() {
		final TextNode node = TextNode.create(10D, 20D).text(this.text("hello"));
		this.bridges.getUi().add(new NodeUI(node));
		Assert.assertTrue(node.isInitialized());
		Assert.assertEquals(50D, node.getWidth(), 0D);
		Assert.assertEquals(20D, node.getHeight(), 0D);
	}

	@Test
	public void keepsTheBoxItWasGiven() {
		final TextNode node = TextNode.create(10D, 20D, 200D, 40D).text(this.text("hello"));
		this.bridges.open(new NodeUI(node));
		Assert.assertEquals(200D, node.getWidth(), 0D);
		Assert.assertEquals(40D, node.getHeight(), 0D);
		Assert.assertEquals(200D, node.getInitialWidth(), 0D);
		Assert.assertEquals(40D, node.getInitialHeight(), 0D);
	}

	@Test
	public void sizesOnlyItsHeightInOverflowMode() {
		final TextNode node = TextNode.create(0D, 0D, 30D, 0D).mode(TextMode.OVERFLOW).text(this.text("hello"));
		final TextNode given = TextNode.create(0D, 100D, 30D, 40D).mode(TextMode.OVERFLOW).text(this.text("hello"));
		this.bridges.getUi().add(new NodeUI(node, given));
		Assert.assertEquals(30D, node.getWidth(), 0D);
		Assert.assertEquals(20D, node.getHeight(), 0D);
		this.bridges.frame();
		Assert.assertEquals(30D, node.getWidth(), 0D);
		Assert.assertEquals(20D, node.getHeight(), 0D);
		Assert.assertEquals(40D, given.getHeight(), 0D);
	}

	@Test
	public void growsToFitEveryLineInSplitMode() {
		final TextNode node = TextNode.create(0D, 0D, 30D, 100D).mode(TextMode.SPLIT).text(this.text("ab cd ef"));
		this.bridges.getUi().add(new NodeUI(node));
		Assert.assertEquals(30D, node.getWidth(), 0D);
		Assert.assertEquals(60D, node.getHeight(), 0D);
		final List<Drawn> drawn = this.draw();
		Assert.assertEquals(3, drawn.size());
		Assert.assertEquals("ef", drawn.get(2).getText());
		Assert.assertEquals(40D, drawn.get(2).getY(), 0D);
		Assert.assertEquals(60D, node.getHeight(), 0D);
	}

	@Test
	public void keepsItsBoxAndDropsTheLinesOutsideInBoxMode() {
		final TextNode node = TextNode.create(0D, 0D, 30D, 40D).mode(TextMode.BOX).text(this.text("ab cd ef"));
		this.bridges.getUi().add(new NodeUI(node));
		final List<Drawn> drawn = this.draw();
		Assert.assertEquals(2, drawn.size());
		Assert.assertEquals("ab", drawn.get(0).getText());
		Assert.assertEquals("cd", drawn.get(1).getText());
		Assert.assertEquals(30D, node.getWidth(), 0D);
		Assert.assertEquals(40D, node.getHeight(), 0D);
	}

	@Test
	public void staysEmptyWithoutText() {
		final TextNode missing = TextNode.create(10D, 20D);
		final TextNode empty = TextNode.create(10D, 20D).text(Text.create());
		final TextNode blank = TextNode.create(10D, 20D).text(this.text(""));
		this.bridges.open(new NodeUI(missing, empty, blank));
		for (final TextNode node : new TextNode[] {missing, empty, blank}) {
			Assert.assertEquals(0D, node.getWidth(), 0D);
			Assert.assertEquals(0D, node.getHeight(), 0D);
		}
		Assert.assertTrue(this.font.getDrawn().isEmpty());
	}

	@Test
	public void drawsItsTextAtItsPosition() {
		this.bridges.open(new NodeUI(TextNode.create(10D, 20D).text(this.text("hello"))));
		final Drawn drawn = this.font.getDrawn().get(0);
		Assert.assertEquals("hello", drawn.getText());
		Assert.assertEquals(10D, drawn.getX(), 0D);
		Assert.assertEquals(20D, drawn.getY(), 0D);
	}

	@Test
	public void alignsItsTextInsideItsBox() {
		this.bridges.open(new NodeUI(TextNode.create(100D, 100D, 200D, 40D).text(Text.create("ab", this.info(), Align.CENTER, Align.CENTER))));
		final Drawn drawn = this.font.getDrawn().get(0);
		Assert.assertEquals(190D, drawn.getX(), 0D);
		Assert.assertEquals(110D, drawn.getY(), 0D);
	}

	@Test
	public void cutsAnOverflowingTextWithItsSuffix() {
		this.bridges.open(new NodeUI(TextNode.create(0D, 0D, 60D, 0D).mode(TextMode.OVERFLOW).text(Text.create("A very long subtitle", this.info()).overflow(TextOverflow.ELLIPSIS))));
		Assert.assertEquals("A v...", this.font.getDrawn().get(0).getText());
	}

	@Test
	public void followsItsTextOnEachDraw() {
		final AtomicReference<String> value = new AtomicReference<>("ab");
		final TextNode node = TextNode.create(0D, 0D).text(Text.create(value::get, this.info()));
		this.bridges.open(new NodeUI(node));
		Assert.assertEquals(20D, node.getWidth(), 0D);
		value.set("abcd");
		this.bridges.frame();
		Assert.assertEquals(40D, node.getWidth(), 0D);
		Assert.assertEquals(20D, node.getHeight(), 0D);
	}

	@Test
	public void sizesItselfOnTheFirstDrawOfATextGivenLate() {
		final TextNode normal = TextNode.create(0D, 0D);
		final TextNode overflow = TextNode.create(0D, 100D, 30D, 0D).mode(TextMode.OVERFLOW);
		final TextNode split = TextNode.create(0D, 200D, 30D, 0D).mode(TextMode.SPLIT);
		this.bridges.open(new NodeUI(normal, overflow, split));
		normal.text(this.text("hello"));
		overflow.text(this.text("hello"));
		split.text(this.text("ab cd"));
		this.bridges.frame();
		Assert.assertEquals(50D, normal.getWidth(), 0D);
		Assert.assertEquals(20D, normal.getHeight(), 0D);
		Assert.assertEquals(30D, overflow.getWidth(), 0D);
		Assert.assertEquals(20D, overflow.getHeight(), 0D);
		Assert.assertEquals(40D, split.getHeight(), 0D);
	}

	@Test
	public void sizesItselfAgainToItsTextOnReload() {
		final AtomicReference<String> value = new AtomicReference<>("hello");
		final TextNode node = TextNode.create(0D, 0D).text(Text.create(value::get, this.info()));
		final TextNode given = TextNode.create(0D, 100D, 200D, 40D).text(Text.create(value::get, this.info()));
		this.bridges.getUi().add(new NodeUI(node, given));
		value.set("hi");
		node.reload();
		given.reload();
		Assert.assertEquals(20D, node.getWidth(), 0D);
		Assert.assertEquals(200D, given.getWidth(), 0D);
		Assert.assertEquals(40D, given.getHeight(), 0D);
	}

	@Test
	public void keepsItsSizeOnceReset() {
		final AtomicReference<String> value = new AtomicReference<>("ab");
		final TextNode node = TextNode.create(0D, 0D).text(Text.create(value::get, this.info()));
		this.bridges.open(new NodeUI(node));
		Assert.assertSame(node, node.reset());
		Assert.assertEquals(20D, node.getInitialWidth(), 0D);
		Assert.assertEquals(20D, node.getInitialHeight(), 0D);
		value.set("abcd");
		this.bridges.frame();
		Assert.assertEquals(20D, node.getWidth(), 0D);
	}

	@Test
	public void takesItsCurrentSizeAsItsInitialSizeOnReset() {
		final TextNode node = TextNode.create(0D, 0D, 30D, 40D).reset();
		Assert.assertTrue(node.isInitialized());
		Assert.assertEquals(30D, node.getInitialWidth(), 0D);
		Assert.assertEquals(40D, node.getInitialHeight(), 0D);
	}

	@Test
	public void drawsItsSkeletonAtTheSizeOfItsText() {
		final TextNode node = TextNode.create(10D, 20D).wait(waiting -> false);
		this.bridges.open(new NodeUI(node));
		node.text(this.text("hello"));
		this.bridges.frame();
		Assert.assertEquals(50D, node.getWidth(), 0D);
		Assert.assertEquals(20D, node.getHeight(), 0D);
		Assert.assertTrue(this.font.getDrawn().isEmpty());
		final Color loading = Color.LOADING();
		final Draw skeleton = this.bridges.getRender().getDraws(loading.r, loading.g, loading.b).get(0);
		Assert.assertEquals(10D, skeleton.getLeft(), 0.001D);
		Assert.assertEquals(60D, skeleton.getRight(), 0.001D);
		Assert.assertEquals(20D, skeleton.getTop(), 0.001D);
		Assert.assertEquals(40D, skeleton.getBottom(), 0.001D);
	}

	@Test
	public void keepsTheSizeOfItsSkeletonWithoutText() {
		final TextNode missing = TextNode.create(10D, 20D).wait(waiting -> false);
		final TextNode empty = TextNode.create(10D, 20D).text(Text.create()).wait(waiting -> false);
		final TextNode given = TextNode.create(10D, 20D, 30D, 40D).text(this.text("hello")).wait(waiting -> false);
		this.bridges.open(new NodeUI(missing, empty, given));
		Assert.assertEquals(0D, missing.getWidth(), 0D);
		Assert.assertEquals(0D, missing.getHeight(), 0D);
		Assert.assertEquals(0D, empty.getWidth(), 0D);
		Assert.assertEquals(0D, empty.getHeight(), 0D);
		Assert.assertEquals(30D, given.getWidth(), 0D);
		Assert.assertEquals(40D, given.getHeight(), 0D);
	}

	private TextInfo info() {
		return TextInfo.create(this.font, 10F);
	}

	private Text text(final String text) {
		return Text.create(text, this.info());
	}

	private List<Drawn> draw() {
		this.font.getDrawn().clear();
		this.bridges.frame();
		return this.font.getDrawn();
	}

	public static final class NodeUI extends UI {

		private final Node[] nodes;

		private NodeUI(final Node... nodes) {
			this.nodes = nodes;
		}

		@Override
		public void init() {
			super.add(this.nodes);
		}

	}

	public static final class TextFont implements IFont, IFontProvider {

		@Getter
		private final List<Drawn> drawn = new ArrayList<>();

		@Override
		public IFontProvider getFontProvider() {
			return this;
		}

		@Override
		public FontBounds drawText(final double x, final double y, final String text, final TextInfo info) {
			this.drawn.add(new Drawn(text, x, y));
			return new FontBounds(this.getWidth(text, info), this.getHeight(text, info));
		}

		@Override
		public double getWidth(final String text, final TextInfo info) {
			return text.length() * 10D;
		}

		@Override
		public double getHeight(final String text, final TextInfo info) {
			return 20D;
		}

		@Override
		public double getLineHeight(final TextInfo info) {
			return 20D;
		}

	}

	@Getter
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static final class Drawn {

		private final String text;
		private final double x;
		private final double y;

	}

}