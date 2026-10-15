package dev.joid.lib.ui.shortcut;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.internal.JOIDEnv;
import dev.joid.internal.font.DevFont;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.debug.UIDataDebug;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventPriority;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.control.field.TextFieldNode;
import dev.joid.lib.ui.node.impl.control.field.TextFieldNodeTest.FieldFont;
import dev.joid.lib.ui.node.impl.shape.RectNode;

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
		final RectNode node = RectNode.create(0, 0, 100, 100).handleKeyPressed(event -> this.trace.add("node"));
		final ShortcutUI ui = this.open(node);
		Shortcut.create(Key.S).onTrigger(event -> this.trace.add("shortcut")).attach(ui);
		Assert.assertTrue(ui.fireKeyPressed(Key.S, Key.S));
		Assert.assertEquals(Arrays.asList("node", "shortcut"), this.trace);
	}

	@Test
	public void runsItsTriggerListenersByPriority() {
		final ShortcutUI ui = this.open();
		Shortcut.create(Key.S).onTrigger(event -> this.trace.add("normal")).onTrigger(EventListener.priority(EventPriority.HIGHEST, event -> this.trace.add("highest " + event.getKey()))).onTrigger(EventListener.priority(EventPriority.MONITOR, event -> this.trace.add("monitor"))).attach(ui);
		Assert.assertTrue(ui.fireKeyPressed(Key.S, Key.S));
		Assert.assertEquals(Arrays.asList("highest S", "normal", "monitor"), this.trace);
	}

	@Test
	public void runsAShortcutThatOverridesTheFocusBeforeItsNodes() {
		final RectNode node = RectNode.create(0, 0, 100, 100).handleKeyPressed(event -> this.trace.add("node"));
		final ShortcutUI ui = this.open(node);
		Shortcut.create(Key.S).overrideFocus(true).onTrigger(event -> this.trace.add("shortcut")).attach(ui);
		Assert.assertTrue(ui.fireKeyPressed(Key.S, Key.S));
		Assert.assertEquals(Collections.singletonList("shortcut"), this.trace);
		Assert.assertNull(node.getLastKey());
	}

	@Test
	public void leavesTheKeyToAFocusedField() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(TextInfo.create(new FieldFont(), 10F)).focused(true);
		final ShortcutUI ui = this.open(field);
		Shortcut.create(Key.S).onTrigger(event -> this.trace.add("shortcut")).attach(ui);
		Assert.assertTrue(ui.fireKeyPressed(Key.S, Key.S));
		Assert.assertTrue(this.trace.isEmpty());
		Assert.assertSame(Key.S, field.getLastKey());
	}

	@Test
	public void takesTheKeyFromAFocusedFieldWhenItOverridesTheFocus() {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(TextInfo.create(new FieldFont(), 10F)).focused(true);
		final ShortcutUI ui = this.open(field);
		Shortcut.create(Key.S).overrideFocus(true).onTrigger(event -> this.trace.add("shortcut")).attach(ui);
		Assert.assertTrue(ui.fireKeyPressed(Key.S, Key.S));
		Assert.assertEquals(Collections.singletonList("shortcut"), this.trace);
		Assert.assertNull(field.getLastKey());
		Assert.assertTrue(field.isFocused());
	}

	@Test
	public void takesTheKeyFromTheNextShortcuts() {
		final ShortcutUI ui = this.open();
		Shortcut.create(Key.S).onTrigger(event -> this.trace.add("first")).attach(ui);
		Shortcut.create(Key.S).onTrigger(event -> this.trace.add("second")).attach(ui);
		ui.fireKeyPressed(Key.S, Key.S);
		Assert.assertEquals(Collections.singletonList("first"), this.trace);
	}

	@Test
	public void letsTheKeyThroughWhenItDoesNotTrigger() {
		final ShortcutUI ui = this.open();
		Shortcut.create(Key.S).onTrigger(event -> this.trace.add("shortcut")).attach(ui);
		Assert.assertFalse(ui.fireKeyPressed(Key.D, Key.D));
		Assert.assertTrue(this.trace.isEmpty());
	}

	@Test
	public void needsExactlyItsModifiers() {
		final ShortcutUI ui = this.open();
		Shortcut.create(Key.S).onTrigger(event -> this.trace.add("plain")).attach(ui);
		Shortcut.create(Key.S).ctrl(true).shift(true).onTrigger(event -> this.trace.add("ctrl shift")).attach(ui);
		Shortcut.create(Key.S).alt(true).onTrigger(event -> this.trace.add("alt")).attach(ui);
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		ui.fireKeyPressed(Key.S, Key.S);
		this.bridges.getWindow().getKeys().add(Key.RIGHT_SHIFT);
		ui.fireKeyPressed(Key.S, Key.S);
		this.bridges.getWindow().getKeys().clear();
		this.bridges.getWindow().getKeys().add(Key.RIGHT_ALT);
		ui.fireKeyPressed(Key.S, Key.S);
		this.bridges.getWindow().getKeys().clear();
		ui.fireKeyPressed(Key.S, Key.S);
		Assert.assertEquals(Arrays.asList("ctrl shift", "alt", "plain"), this.trace);
	}

	@Test
	public void runsAShortcutOfANodeOnlyWhileItsNodeIsAttachedVisibleAndEnabled() {
		final AtomicBoolean shown = new AtomicBoolean(true);
		final AtomicBoolean enabled = new AtomicBoolean(true);
		final RectNode node = RectNode.create(0, 0, 100, 100).visible(() -> shown.get()).enabled(() -> enabled.get());
		final RectNode parent = RectNode.create(0, 0, 200, 200).append(node);
		final ShortcutUI ui = this.open(parent);
		Shortcut.create(Key.S).onTrigger(event -> this.trace.add("shortcut")).attach(node);
		Assert.assertTrue(ui.fireKeyPressed(Key.S, Key.S));
		shown.set(false);
		Assert.assertFalse(ui.fireKeyPressed(Key.S, Key.S));
		shown.set(true);
		enabled.set(false);
		Assert.assertFalse(ui.fireKeyPressed(Key.S, Key.S));
		enabled.set(true);
		parent.remove(node);
		Assert.assertFalse(ui.fireKeyPressed(Key.S, Key.S));
		parent.append(node);
		Assert.assertTrue(ui.fireKeyPressed(Key.S, Key.S));
		Assert.assertEquals(Arrays.asList("shortcut", "shortcut"), this.trace);
	}

	@Test
	public void skipsAShortcutOfANodeHiddenWithItsParent() {
		final AtomicBoolean shown = new AtomicBoolean(true);
		final RectNode node = RectNode.create(0, 0, 100, 100);
		final ShortcutUI ui = this.open(RectNode.create(0, 0, 200, 200).visible(() -> shown.get()).append(node));
		Shortcut.create(Key.S).onTrigger(event -> this.trace.add("shortcut")).attach(node);
		shown.set(false);
		Assert.assertFalse(ui.fireKeyPressed(Key.S, Key.S));
		Assert.assertTrue(this.trace.isEmpty());
	}

	@Test
	public void prefersTheShortcutOfANodeToTheShortcutOfItsUi() {
		final RectNode node = RectNode.create(0, 0, 100, 100);
		final ShortcutUI ui = this.open(node);
		Shortcut.create(Key.S).onTrigger(event -> this.trace.add("ui")).attach(ui);
		Shortcut.create(Key.S).onTrigger(event -> this.trace.add("node")).attach(node);
		ui.fireKeyPressed(Key.S, Key.S);
		Assert.assertEquals(Collections.singletonList("node"), this.trace);
	}

	@Test
	public void stopsOnceDetached() {
		final ShortcutUI ui = this.open();
		final Shortcut shortcut = Shortcut.create(Key.S).onTrigger(event -> this.trace.add("shortcut")).attach(ui).detach();
		Assert.assertFalse(ui.fireKeyPressed(Key.S, Key.S));
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
	public void keepsTheListOfItsOwnerInStep() {
		final RectNode node = RectNode.create(0, 0, 100, 100);
		final ShortcutUI ui = this.open(node);
		final Shortcut shortcut = Shortcut.create(Key.S).attach(ui);
		Assert.assertEquals(Collections.singletonList(shortcut), ui.getShortcutList());
		shortcut.attach(node);
		Assert.assertTrue(ui.getShortcutList().isEmpty());
		Assert.assertEquals(Collections.singletonList(shortcut), node.getShortcutList());
		shortcut.detach();
		Assert.assertNull(shortcut.getNode());
		Assert.assertTrue(node.getShortcutList().isEmpty());
		Assert.assertSame(node, node.addShortcut(shortcut));
		Assert.assertSame(node, shortcut.getNode());
		Assert.assertSame(node, node.removeShortcut(shortcut));
		Assert.assertFalse(shortcut.isAttached());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void exposesAReadOnlyShortcutList() {
		final ShortcutUI ui = this.open();
		Shortcut.create(Key.S).attach(ui);
		ui.getShortcutList().clear();
	}

	@Test
	public void givesItselfAndTheKeyToItsEvent() {
		final ShortcutUI ui = this.open();
		final List<ShortcutEvent> events = new ArrayList<>();
		final Shortcut shortcut = Shortcut.create(Key.S).onTrigger(events::add).attach(ui);
		ui.fireKeyPressed(Key.S, Key.S);
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
		ui.fireKeyPressed(Key.S, Key.S);
		ui.fireKeyPressed(Key.D, Key.D);
		Assert.assertEquals(Arrays.asList(ui, ui), currents);
		Assert.assertNull(UI.getCurrent());
	}

	@Test
	public void warnsOnceInDevModeForABindingWithoutResolver() {
		final boolean previous = JOIDEnv.isDevMode();
		final PrintStream error = System.err;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		JOIDEnv.devMode(true);
		try {
			System.setErr(new PrintStream(output, true));
			final ShortcutUI ui = this.open();
			Shortcut.create("unknown binding").onTrigger(event -> this.trace.add("shortcut")).attach(ui);
			Shortcut.create("unknown binding");
			Assert.assertFalse(ui.fireKeyPressed(Key.S, Key.S));
		} finally {
			System.setErr(error);
			JOIDEnv.devMode(previous);
		}
		final String printed = new String(output.toByteArray(), StandardCharsets.UTF_8);
		Assert.assertTrue(printed, printed.startsWith("[JOID] No key resolver knows the shortcut binding unknown binding of type java.lang.String"));
		Assert.assertEquals(printed, printed.indexOf("[JOID]"), printed.lastIndexOf("[JOID]"));
		Assert.assertTrue(this.trace.isEmpty());
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullBinding() {
		Shortcut.create(null);
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