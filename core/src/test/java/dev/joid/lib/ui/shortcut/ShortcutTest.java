package dev.joid.lib.ui.shortcut;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.internal.font.DevFont;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.debug.UIDataDebug;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.textfield.TextFieldNode;
import dev.joid.lib.ui.node.impl.design.textfield.TextFieldNodeTest.FieldFont;

public class ShortcutTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private final List<String> trace = new ArrayList<>();

	@BeforeClass
	public static void loadTheDevFont() {
		DevFont.load();
	}

	@Test
	public void runsAShortcutOfItsUiAfterItsNodes() {
		final RectNode node = RectNode.create(0, 0, 100, 100).onKeyPressed((rect, key) -> this.trace.add("node"));
		final ShortcutUI ui = this.open(node);
		Shortcut.create(Key.S).onTrigger(event -> this.trace.add("shortcut")).attach(ui);
		Assert.assertTrue(ui.fireKeyPressed(Key.S));
		Assert.assertEquals(Arrays.asList("node", "shortcut"), this.trace);
	}

	@Test
	public void runsAShortcutThatOverridesTheFocusBeforeItsNodes() {
		final RectNode node = RectNode.create(0, 0, 100, 100).onKeyPressed((rect, key) -> this.trace.add("node"));
		final ShortcutUI ui = this.open(node);
		Shortcut.create(Key.S).overrideFocus(true).onTrigger(event -> this.trace.add("shortcut")).attach(ui);
		Assert.assertTrue(ui.fireKeyPressed(Key.S));
		Assert.assertEquals(Collections.singletonList("shortcut"), this.trace);
		Assert.assertNull(node.getLastKey());
	}

	@Test
	public void leavesTheKeyToAFocusedField() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(TextInfo.create(new FieldFont(), 10F)).focused(true);
		final ShortcutUI ui = this.open(field);
		Shortcut.create(Key.S).onTrigger(event -> this.trace.add("shortcut")).attach(ui);
		Assert.assertTrue(ui.fireKeyPressed(Key.S));
		Assert.assertTrue(this.trace.isEmpty());
		Assert.assertSame(Key.S, field.getLastKey());
	}

	@Test
	public void takesTheKeyFromAFocusedFieldWhenItOverridesTheFocus() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(TextInfo.create(new FieldFont(), 10F)).focused(true);
		final ShortcutUI ui = this.open(field);
		Shortcut.create(Key.S).overrideFocus(true).onTrigger(event -> this.trace.add("shortcut")).attach(ui);
		Assert.assertTrue(ui.fireKeyPressed(Key.S));
		Assert.assertEquals(Collections.singletonList("shortcut"), this.trace);
		Assert.assertNull(field.getLastKey());
		Assert.assertTrue(field.isFocused());
	}

	@Test
	public void takesTheKeyFromTheNextShortcuts() {
		final ShortcutUI ui = this.open();
		Shortcut.create(Key.S).onTrigger(event -> this.trace.add("first")).attach(ui);
		Shortcut.create(Key.S).onTrigger(event -> this.trace.add("second")).attach(ui);
		ui.fireKeyPressed(Key.S);
		Assert.assertEquals(Collections.singletonList("first"), this.trace);
	}

	@Test
	public void letsTheKeyThroughWhenItDoesNotTrigger() {
		final ShortcutUI ui = this.open();
		Shortcut.create(Key.S).onTrigger(event -> this.trace.add("shortcut")).attach(ui);
		Assert.assertFalse(ui.fireKeyPressed(Key.D));
		Assert.assertTrue(this.trace.isEmpty());
	}

	@Test
	public void needsExactlyItsModifiers() {
		final ShortcutUI ui = this.open();
		Shortcut.create(Key.S).onTrigger(event -> this.trace.add("plain")).attach(ui);
		Shortcut.create(Key.S).ctrl(true).shift(true).onTrigger(event -> this.trace.add("ctrl shift")).attach(ui);
		Shortcut.create(Key.S).alt(true).onTrigger(event -> this.trace.add("alt")).attach(ui);
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		ui.fireKeyPressed(Key.S);
		this.bridges.getWindow().getKeys().add(Key.RIGHT_SHIFT);
		ui.fireKeyPressed(Key.S);
		this.bridges.getWindow().getKeys().clear();
		this.bridges.getWindow().getKeys().add(Key.RIGHT_ALT);
		ui.fireKeyPressed(Key.S);
		this.bridges.getWindow().getKeys().clear();
		ui.fireKeyPressed(Key.S);
		Assert.assertEquals(Arrays.asList("ctrl shift", "alt", "plain"), this.trace);
	}

	@Test
	public void runsAShortcutOfANodeOnlyWhileItsNodeIsAttachedVisibleAndEnabled() {
		final AtomicBoolean shown = new AtomicBoolean(true);
		final AtomicBoolean enabled = new AtomicBoolean(true);
		final RectNode node = RectNode.create(0, 0, 100, 100).visible(rect -> shown.get()).enabled(rect -> enabled.get());
		final RectNode parent = RectNode.create(0, 0, 200, 200).append(node);
		final ShortcutUI ui = this.open(parent);
		Shortcut.create(Key.S).onTrigger(event -> this.trace.add("shortcut")).attach(node);
		Assert.assertTrue(ui.fireKeyPressed(Key.S));
		shown.set(false);
		Assert.assertFalse(ui.fireKeyPressed(Key.S));
		shown.set(true);
		enabled.set(false);
		Assert.assertFalse(ui.fireKeyPressed(Key.S));
		enabled.set(true);
		parent.remove(node);
		Assert.assertFalse(ui.fireKeyPressed(Key.S));
		parent.append(node);
		Assert.assertTrue(ui.fireKeyPressed(Key.S));
		Assert.assertEquals(Arrays.asList("shortcut", "shortcut"), this.trace);
	}

	@Test
	public void skipsAShortcutOfANodeHiddenWithItsParent() {
		final AtomicBoolean shown = new AtomicBoolean(true);
		final RectNode node = RectNode.create(0, 0, 100, 100);
		final ShortcutUI ui = this.open(RectNode.create(0, 0, 200, 200).visible(rect -> shown.get()).append(node));
		Shortcut.create(Key.S).onTrigger(event -> this.trace.add("shortcut")).attach(node);
		shown.set(false);
		Assert.assertFalse(ui.fireKeyPressed(Key.S));
		Assert.assertTrue(this.trace.isEmpty());
	}

	@Test
	public void prefersTheShortcutOfANodeToTheShortcutOfItsUi() {
		final RectNode node = RectNode.create(0, 0, 100, 100);
		final ShortcutUI ui = this.open(node);
		Shortcut.create(Key.S).onTrigger(event -> this.trace.add("ui")).attach(ui);
		Shortcut.create(Key.S).onTrigger(event -> this.trace.add("node")).attach(node);
		ui.fireKeyPressed(Key.S);
		Assert.assertEquals(Collections.singletonList("node"), this.trace);
	}

	@Test
	public void stopsOnceDetached() {
		final ShortcutUI ui = this.open();
		final Shortcut shortcut = Shortcut.create(Key.S).onTrigger(event -> this.trace.add("shortcut")).attach(ui).detach();
		Assert.assertFalse(ui.fireKeyPressed(Key.S));
		Assert.assertFalse(shortcut.isAttached());
		Assert.assertNull(shortcut.getUi());
		Assert.assertTrue(ui.getShortcutList().isEmpty());
		Assert.assertTrue(this.trace.isEmpty());
	}

	@Test
	public void movesToItsNewTarget() {
		final RectNode node = RectNode.create(0, 0, 100, 100);
		final ShortcutUI ui = this.open(node);
		final Shortcut shortcut = Shortcut.create(Key.S).attach(ui).attach(node);
		Assert.assertTrue(ui.getShortcutList().isEmpty());
		Assert.assertEquals(Collections.singletonList(shortcut), node.getShortcutList());
		Assert.assertNull(shortcut.getUi());
		Assert.assertSame(node, shortcut.getNode());
	}

	@Test
	public void givesItselfAndTheKeyToItsEvent() {
		final ShortcutUI ui = this.open();
		final List<ShortcutEvent> events = new ArrayList<>();
		final Shortcut shortcut = Shortcut.create(Key.S).onTrigger(events::add).attach(ui);
		ui.fireKeyPressed(Key.S);
		Assert.assertEquals(1, events.size());
		Assert.assertSame(shortcut, events.get(0).getShortcut());
		Assert.assertSame(Key.S, events.get(0).getKey());
	}

	@Test
	public void runsInTheContextOfItsUi() {
		final RectNode node = RectNode.create(0, 0, 100, 100);
		final ShortcutUI ui = this.open(node);
		final List<UI> currents = new ArrayList<>();
		Shortcut.create(Key.S).onTrigger(event -> currents.add(UI.getCurrent())).attach(ui);
		Shortcut.create(Key.D).onTrigger(event -> currents.add(UI.getCurrent())).attach(node);
		ui.fireKeyPressed(Key.S);
		ui.fireKeyPressed(Key.D);
		Assert.assertEquals(Arrays.asList(ui, ui), currents);
		Assert.assertNull(UI.getCurrent());
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesABindingWithoutResolver() {
		Shortcut.create("S");
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullKey() {
		Shortcut.create((Key) null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullListener() {
		Shortcut.create(Key.S).onTrigger(null);
	}

	private ShortcutUI open(final Node... nodes) {
		final ShortcutUI ui = new ShortcutUI(nodes);
		this.bridges.open(ui);
		return ui;
	}

	@UIDataDebug(profiler = false, hotreload = false)
	public static final class ShortcutUI extends UI {

		private final Node[] nodes;

		private ShortcutUI(final Node... nodes) {
			this.nodes = nodes;
		}

		@Override
		public void init() {
			super.add(this.nodes);
		}

	}

}