package dev.joid.lib.ui.node.impl.design.textfield.callback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.font.IFont;
import dev.joid.lib.font.IFontProvider;
import dev.joid.lib.font.dto.FontBounds;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import dev.joid.lib.ui.node.impl.design.textfield.MultilineTextFieldNode;
import dev.joid.lib.ui.node.impl.design.textfield.TextFieldNode;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.key.Key;
import lombok.NonNull;

public class NodeTextFieldChangeCallbackTest {

	private static final IFont FONT = () -> NodeTextFieldChangeCallbackTest.PROVIDER;

	private static final IFontProvider PROVIDER = new IFontProvider() {

		@Override
		public FontBounds drawText(final double x, final double y, final String text, final TextInfo info) {
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

	};

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeAndConsumesTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeTextFieldChangeCallback<TextFieldNode> callback = (node, oldText, newText) -> received.addAll(Arrays.asList(node, oldText, newText));
		final TextFieldNode field = TextFieldNode.create(0D, 0D, 100D);
		final InternalContext context = InternalContext.create();
		callback.pre(field, context, "a", "ab");
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(context.isCancelled());
		callback.post(field, context, "a", "ab");
		Assert.assertEquals(Arrays.asList(field, "a", "ab"), received);
		Assert.assertTrue(context.isCancelled());
	}

	@Test
	public void ignoresAConsumedContext() {
		final List<Object> received = new ArrayList<>();
		final NodeTextFieldChangeCallback<TextFieldNode> callback = (node, oldText, newText) -> received.add(node);
		callback.post(TextFieldNode.create(0D, 0D, 100D), InternalContext.create(true), "a", "ab");
		Assert.assertTrue(received.isEmpty());
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingText() {
		final NodeTextFieldChangeCallback<TextFieldNode> callback = (node, oldText, newText) -> {};
		callback.post(TextFieldNode.create(0D, 0D, 100D), InternalContext.create(), null, "ab");
	}

	@Test
	public void receivesTheTextsAroundATypedCharacter() {
		final List<Object> received = new ArrayList<>();
		final TextFieldNode field = this.field().onChange((node, oldText, newText) -> received.addAll(Arrays.asList(node, oldText, newText)));
		final MultilineTextFieldNode multiline = this.multiline().onChange((node, oldText, newText) -> received.addAll(Arrays.asList(node, oldText, newText)));
		this.bridges.open(new NodeUI(field, multiline));
		field.keyPressed('c', Key.C, InternalContext.create());
		multiline.keyPressed('d', Key.D, InternalContext.create());
		Assert.assertEquals(Arrays.asList(field, "ab", "abc", multiline, "ab", "abd"), received);
	}

	@Test
	public void keepsTheTextWhenThePrePhaseConsumesTheChange() {
		final TextFieldNode field = this.field().onChange(new NodeTextFieldChangeCallback<TextFieldNode>() {

			@Override
			public void apply(final @NonNull TextFieldNode node, final @NonNull String oldText, final @NonNull String newText) {}

			@Override
			@NodeCallbackMethod(Type.PRE)
			public void pre(final @NonNull TextFieldNode node, final @NonNull InternalContext context, final @NonNull String oldText, final @NonNull String newText) {
				context.cancel();
			}

		});
		this.bridges.open(new NodeUI(field));
		field.keyPressed('c', Key.C, InternalContext.create());
		field.text("xyz");
		Assert.assertEquals("ab", field.getText());
	}

	@Test
	public void keepsTheTextOfAMultilineFieldWhenThePrePhaseConsumesTheChange() {
		final MultilineTextFieldNode field = this.multiline().onChange(new NodeTextFieldChangeCallback<MultilineTextFieldNode>() {

			@Override
			public void apply(final @NonNull MultilineTextFieldNode node, final @NonNull String oldText, final @NonNull String newText) {}

			@Override
			@NodeCallbackMethod(Type.PRE)
			public void pre(final @NonNull MultilineTextFieldNode node, final @NonNull InternalContext context, final @NonNull String oldText, final @NonNull String newText) {
				context.cancel();
			}

		});
		this.bridges.open(new NodeUI(field));
		field.keyPressed(' ', Key.ENTER, InternalContext.create());
		Assert.assertEquals("ab", field.getText());
	}

	private TextFieldNode field() {
		return TextFieldNode.create(0D, 0D, 100D).info(TextInfo.create(NodeTextFieldChangeCallbackTest.FONT, 10F)).text("ab").focused(true).cursorPosition(2);
	}

	private MultilineTextFieldNode multiline() {
		return MultilineTextFieldNode.create(0D, 100D, 100D, 100D).info(TextInfo.create(NodeTextFieldChangeCallbackTest.FONT, 10F)).text("ab").focused(true).cursorPosition(2);
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

}