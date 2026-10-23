package dev.joid.lib.ui.node.impl.control.field;

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
import dev.joid.lib.ui.core.NodeUI;
import dev.joid.lib.ui.event.impl.node.NodeCharEvent;
import dev.joid.lib.ui.event.impl.node.NodeKeyEvent;

public class FieldNodeEventTest {

	private static final IFont FONT = () -> FieldNodeEventTest.RENDERER;

	private static final ITextRenderer RENDERER = new ITextRenderer() {

		@Override
		public FontBounds drawText(final double x, final double y, final String text, final TextInfo info) {
			return FontBounds.of(this.getWidth(text, info), this.getHeight(text, info));
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
	public void refusesOnlyTheUser() {
		final List<String> proposals = new ArrayList<>();
		final StringSignal text = new StringSignal("supplied");
		final TextFieldNode field = this.field().beforeChange(event -> {
			proposals.add(event.getText());
			event.cancel();
		});
		this.bridges.open(new NodeUI(field));
		field.charTyped(NodeCharEvent.create(field, 'c', null));
		Assert.assertEquals("ab", field.getText());
		field.text("xyz");
		Assert.assertEquals("xyz", field.getText());
		field.text(text);
		Assert.assertEquals("supplied", field.getText());
		Assert.assertEquals(Arrays.asList("abc"), proposals);
	}

	@Test
	public void followsItsSignalWhateverItsBeforeChangeListeners() {
		final StringSignal signal = new StringSignal("ab");
		final List<String> changes = new ArrayList<>();
		final TextFieldNode field = this.field().text(signal).<TextFieldNode>beforeChange(event -> event.cancel()).onChange(event -> changes.add(event.getText()));
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
		field.keyPressed(NodeKeyEvent.create(field, Key.ENTER, Key.ENTER, null));
		Assert.assertEquals("ab", field.getText());
		Assert.assertTrue(changes.isEmpty());
	}

	@Test
	public void submitsAFieldLeftWithEnter() {
		final List<Object> received = new ArrayList<>();
		final TextFieldNode field = TextFieldNode.create(0D, 0D, 100D).onSubmit(event -> received.addAll(Arrays.asList(event.getNode(), event.getNode().getText(), event.getNode().isFocused())));
		field.info(TextInfo.create(FieldNodeEventTest.FONT, 10F)).text("hello").focused(true);
		this.bridges.open(new NodeUI(field));
		this.bridges.getUi().keyPressed(Key.ENTER, Key.ENTER);
		Assert.assertEquals(Arrays.asList(field, "hello", false), received);
	}

	@Test
	public void seesTheNewFocusOfAField() {
		final List<Boolean> focuses = new ArrayList<>();
		final TextFieldNode field = this.field().focused(false).onFocus(event -> focuses.add(event.getNode().isFocused())).onBlur(event -> focuses.add(event.getNode().isFocused()));
		final MultilineTextFieldNode multiline = this.multiline().focused(false).onFocus(event -> focuses.add(event.getNode().isFocused())).onBlur(event -> focuses.add(event.getNode().isFocused()));
		this.bridges.open(new NodeUI(field, multiline));
		field.focused(true).focused(false);
		multiline.focused(true).focused(false);
		Assert.assertEquals(Arrays.asList(true, false, true, false), focuses);
	}

	@Test
	public void runsEveryFocusListenerOfAField() {
		final List<String> received = new ArrayList<>();
		final TextFieldNode field = this.field().focused(false).onFocus(event -> received.add("first")).onFocus(event -> received.add("second"));
		this.bridges.open(new NodeUI(field));
		field.focused(true);
		Assert.assertEquals(Arrays.asList("first", "second"), received);
	}

	@Test
	public void seesTheCursorAndTheSelectionOfAGivenTextInItsListeners() {
		final List<String> seen = new ArrayList<>();
		final TextFieldNode field = this.field().text("abcdef");
		this.bridges.open(new NodeUI(field));
		field.selection(1, 6);
		field.onChange(event -> seen.add("change " + field.getCursorPosition() + " " + field.getSelection().getStart() + "-" + field.getSelection().getEnd()));
		field.onCursorMove(event -> seen.add("cursor " + field.getCursorPosition() + " " + field.getSelection().getStart() + "-" + field.getSelection().getEnd()));
		field.text("ab");
		field.text(new StringSignal("abcd"));
		Assert.assertEquals(Arrays.asList("change 2 0-2", "cursor 2 0-2", "change 4 2-4", "cursor 4 2-4"), seen);
	}

	@Test
	public void seesTheCommittedTextOfAFieldLeftByItsFocusedSetter() {
		final List<String> seen = new ArrayList<>();
		final IntegerFieldNode field = IntegerFieldNode.create(0D, 0D, 100D).<IntegerFieldNode>info(TextInfo.create(FieldNodeEventTest.FONT, 10F)).<IntegerFieldNode>text("1").focused(true);
		this.bridges.open(new NodeUI(field));
		field.text("007");
		field.selection(1, 3);
		field.onChange(event -> seen.add(field.getText() + " " + field.getCursorPosition() + " " + field.getSelection().getStart() + "-" + field.getSelection().getEnd()));
		field.focused(false);
		Assert.assertEquals(Collections.singletonList("7 1 0-1"), seen);
	}

	private TextFieldNode field() {
		return TextFieldNode.create(0D, 0D, 100D).info(TextInfo.create(FieldNodeEventTest.FONT, 10F)).text("ab").focused(true).cursorPosition(2);
	}

	private MultilineTextFieldNode multiline() {
		return MultilineTextFieldNode.create(0D, 100D, 100D, 100D).info(TextInfo.create(FieldNodeEventTest.FONT, 10F)).text("ab").focused(true).cursorPosition(2);
	}

}