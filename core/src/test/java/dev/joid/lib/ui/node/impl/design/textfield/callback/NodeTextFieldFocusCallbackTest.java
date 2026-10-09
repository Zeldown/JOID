package dev.joid.lib.ui.node.impl.design.textfield.callback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.font.FontBounds;
import dev.joid.lib.font.IFont;
import dev.joid.lib.font.IFontProvider;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Phase;
import dev.joid.lib.ui.node.impl.design.textfield.MultilineTextFieldNode;
import dev.joid.lib.ui.node.impl.design.textfield.TextFieldNode;
import lombok.NonNull;

public class NodeTextFieldFocusCallbackTest {

	private static final IFont FONT = () -> NodeTextFieldFocusCallbackTest.PROVIDER;

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
		final NodeTextFieldFocusCallback<TextFieldNode> callback = node -> received.add(node);
		final TextFieldNode field = TextFieldNode.create(0D, 0D, 100D);
		final DispatchContext context = DispatchContext.create();
		callback.pre(field, context);
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(context.isCancelled());
		callback.post(field, context);
		Assert.assertEquals(Collections.singletonList(field), received);
		Assert.assertTrue(context.isCancelled());
	}

	@Test
	public void ignoresAConsumedContext() {
		final List<Object> received = new ArrayList<>();
		final NodeTextFieldFocusCallback<TextFieldNode> callback = node -> received.add(node);
		callback.post(TextFieldNode.create(0D, 0D, 100D), DispatchContext.create(true));
		Assert.assertTrue(received.isEmpty());
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingNode() {
		final NodeTextFieldFocusCallback<TextFieldNode> callback = node -> {};
		callback.post(null, DispatchContext.create());
	}

	@Test
	public void seesTheNewFocusOfAField() {
		final List<Boolean> focuses = new ArrayList<>();
		final TextFieldNode field = TextFieldNode.create(0D, 0D, 100D).onFocus(node -> focuses.add(node.isFocused()));
		final MultilineTextFieldNode multiline = MultilineTextFieldNode.create(0D, 0D, 100D, 100D).onFocus(node -> focuses.add(node.isFocused()));
		field.focused(true).focused(false);
		multiline.focused(true).focused(false);
		Assert.assertEquals(Arrays.asList(true, false, true, false), focuses);
	}

	@Test
	public void runsEveryCallbackOfAField() {
		final List<String> received = new ArrayList<>();
		final TextFieldNode field = TextFieldNode.create(0D, 0D, 100D).onFocus(node -> received.add("first")).onFocus(node -> received.add("second"));
		field.focused(true);
		Assert.assertEquals(Arrays.asList("first", "second"), received);
	}

	@Test
	public void keepsAFieldUnfocusedWhenThePrePhaseConsumesTheClick() {
		final List<Object> received = new ArrayList<>();
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(TextInfo.create(NodeTextFieldFocusCallbackTest.FONT, 10F)).onFocus(new NodeTextFieldFocusCallback<TextFieldNode>() {

			@Override
			public void apply(final @NonNull TextFieldNode node) {
				received.add(node);
			}

			@Override
			@NodeCallbackMethod(Phase.PRE)
			public void pre(final @NonNull TextFieldNode node, final @NonNull DispatchContext context) {
				context.cancel();
			}

		});
		this.bridges.open(new NodeUI(field));
		this.bridges.move(150D, 120D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertFalse(field.isFocused());
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void keepsAMultilineFieldUnfocusedWhenThePrePhaseConsumesIt() {
		final MultilineTextFieldNode field = MultilineTextFieldNode.create(0D, 0D, 100D, 100D).onFocus(new NodeTextFieldFocusCallback<MultilineTextFieldNode>() {

			@Override
			public void apply(final @NonNull MultilineTextFieldNode node) {}

			@Override
			@NodeCallbackMethod(Phase.PRE)
			public void pre(final @NonNull MultilineTextFieldNode node, final @NonNull DispatchContext context) {
				context.cancel();
			}

		});
		Assert.assertFalse(field.focused(true).isFocused());
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