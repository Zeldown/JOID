package dev.joid.lib.ui.node.impl.design.textfield;

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
import dev.joid.lib.font.ITextRenderer;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.event.impl.node.NodeCharEvent;
import dev.joid.lib.ui.event.impl.node.NodeKeyEvent;
import dev.joid.lib.ui.node.Node;

public class FieldNodeEventTest {

	private static final IFont FONT = () -> FieldNodeEventTest.RENDERER;

	private static final ITextRenderer RENDERER = new ITextRenderer() {

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
	public void receivesTheTextTheValueAndTheValidityOfATypedCharacter() {
		final List<Object> received = new ArrayList<>();
		final TextFieldNode field = this.field().onChange(event -> received.addAll(Arrays.asList(event.getNode(), event.getPrevious(), event.getText(), event.getValue(), event.isValid())));
		final MultilineTextFieldNode multiline = this.multiline().onChange(event -> received.addAll(Arrays.asList(event.getNode(), event.getPrevious(), event.getText(), event.getValue(), event.isValid())));
		this.bridges.open(new NodeUI(field, multiline));
		field.charTyped(NodeCharEvent.create(field, 'c', null));
		multiline.charTyped(NodeCharEvent.create(multiline, 'd', null));
		Assert.assertEquals(Arrays.asList(field, "ab", "abc", "abc", true, multiline, "ab", "abd", "abd", true), received);
	}

	@Test
	public void refusesOnlyTheChangesOfTheUser() {
		final List<String> proposals = new ArrayList<>();
		final TextFieldNode field = this.field().beforeChange(event -> {
			proposals.add(event.getText());
			event.cancel();
		});
		this.bridges.open(new NodeUI(field));
		field.charTyped(NodeCharEvent.create(field, 'c', null));
		Assert.assertEquals("ab", field.getText());
		field.text("xyz");
		Assert.assertEquals("xyz", field.getText());
		Assert.assertEquals(Collections.singletonList("abc"), proposals);
	}

	@Test
	public void followsItsSignalWhateverItsBeforeChangeListeners() {
		final StringSignal signal = new StringSignal("ab");
		final List<String> changes = new ArrayList<>();
		final TextFieldNode field = this.field().signal(signal).<TextFieldNode>beforeChange(event -> event.cancel()).onChange(event -> changes.add(event.getText()));
		this.bridges.open(new NodeUI(field));
		signal.set("abcd");
		Assert.assertEquals("abcd", field.getText());
		Assert.assertEquals(Collections.singletonList("abcd"), changes);
	}

	@Test
	public void skipsTheChangeListenersOfARefusedChange() {
		final List<String> changes = new ArrayList<>();
		final MultilineTextFieldNode field = this.multiline().beforeChange(event -> event.cancel()).onChange(event -> changes.add(event.getText()));
		this.bridges.open(new NodeUI(field));
		field.keyPressed(NodeKeyEvent.create(field, Key.ENTER, null));
		Assert.assertEquals("ab", field.getText());
		Assert.assertTrue(changes.isEmpty());
	}

	@Test
	public void submitsAFieldLeftWithEnter() {
		final List<Object> received = new ArrayList<>();
		final TextFieldNode field = TextFieldNode.create(0D, 0D, 100D).onSubmit(event -> received.addAll(Arrays.asList(event.getNode(), event.getNode().getText(), event.getNode().isFocused())));
		field.info(TextInfo.create(FieldNodeEventTest.FONT, 10F)).text("hello").focused(true);
		this.bridges.open(new NodeUI(field));
		this.bridges.getUi().keyPressed(Key.ENTER);
		Assert.assertEquals(Arrays.asList(field, "hello", false), received);
	}

	@Test
	public void seesTheNewFocusOfAField() {
		final List<Boolean> focuses = new ArrayList<>();
		final TextFieldNode field = TextFieldNode.create(0D, 0D, 100D).onFocus(event -> focuses.add(event.getNode().isFocused()));
		final MultilineTextFieldNode multiline = MultilineTextFieldNode.create(0D, 0D, 100D, 100D).onFocus(event -> focuses.add(event.getNode().isFocused()));
		field.focused(true).focused(false);
		multiline.focused(true).focused(false);
		Assert.assertEquals(Arrays.asList(true, false, true, false), focuses);
	}

	@Test
	public void runsEveryFocusListenerOfAField() {
		final List<String> received = new ArrayList<>();
		final TextFieldNode field = TextFieldNode.create(0D, 0D, 100D).onFocus(event -> received.add("first")).onFocus(event -> received.add("second"));
		field.focused(true);
		Assert.assertEquals(Arrays.asList("first", "second"), received);
	}

	private TextFieldNode field() {
		return TextFieldNode.create(0D, 0D, 100D).info(TextInfo.create(FieldNodeEventTest.FONT, 10F)).text("ab").focused(true).cursorPosition(2);
	}

	private MultilineTextFieldNode multiline() {
		return MultilineTextFieldNode.create(0D, 100D, 100D, 100D).info(TextInfo.create(FieldNodeEventTest.FONT, 10F)).text("ab").focused(true).cursorPosition(2);
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