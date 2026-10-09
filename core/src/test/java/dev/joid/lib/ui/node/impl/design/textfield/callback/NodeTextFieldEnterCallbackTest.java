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
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Phase;
import dev.joid.lib.ui.node.impl.design.textfield.TextFieldNode;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.key.Key;
import lombok.NonNull;

public class NodeTextFieldEnterCallbackTest {

	private static final IFont FONT = () -> NodeTextFieldEnterCallbackTest.PROVIDER;

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
		final NodeTextFieldEnterCallback<TextFieldNode> callback = (node, text) -> received.addAll(Arrays.asList(node, text));
		final TextFieldNode field = TextFieldNode.create(0D, 0D, 100D);
		final InternalContext context = InternalContext.create();
		callback.pre(field, context, "hello");
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(context.isCancelled());
		callback.post(field, context, "hello");
		Assert.assertEquals(Arrays.asList(field, "hello"), received);
		Assert.assertTrue(context.isCancelled());
	}

	@Test
	public void ignoresAConsumedContext() {
		final List<Object> received = new ArrayList<>();
		final NodeTextFieldEnterCallback<TextFieldNode> callback = (node, text) -> received.add(node);
		callback.post(TextFieldNode.create(0D, 0D, 100D), InternalContext.create(true), "hello");
		Assert.assertTrue(received.isEmpty());
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingText() {
		final NodeTextFieldEnterCallback<TextFieldNode> callback = (node, text) -> {};
		callback.post(TextFieldNode.create(0D, 0D, 100D), InternalContext.create(), null);
	}

	@Test
	public void receivesTheTextOfAFieldLeftWithEnter() {
		final List<Object> received = new ArrayList<>();
		final TextFieldNode field = this.field().onEnter((node, text) -> received.addAll(Arrays.asList(node, text, node.isFocused())));
		this.bridges.open(new NodeUI(field));
		this.bridges.getUi().keyTyped('\r', Key.ENTER);
		Assert.assertEquals(Arrays.asList(field, "hello", false), received);
	}

	@Test
	public void stillUnfocusesTheFieldWhenThePrePhaseConsumesTheEnter() {
		final List<Object> received = new ArrayList<>();
		final TextFieldNode field = this.field().onEnter(new NodeTextFieldEnterCallback<TextFieldNode>() {

			@Override
			public void apply(final @NonNull TextFieldNode node, final @NonNull String text) {
				received.add(text);
			}

			@Override
			@NodeCallbackMethod(Phase.PRE)
			public void pre(final @NonNull TextFieldNode node, final @NonNull InternalContext context, final @NonNull String text) {
				context.cancel();
			}

		});
		this.bridges.open(new NodeUI(field));
		this.bridges.getUi().keyTyped('\r', Key.NUMPAD_ENTER);
		Assert.assertFalse(field.isFocused());
		Assert.assertTrue(received.isEmpty());
	}

	private TextFieldNode field() {
		return TextFieldNode.create(0D, 0D, 100D).info(TextInfo.create(NodeTextFieldEnterCallbackTest.FONT, 10F)).text("hello").focused(true);
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