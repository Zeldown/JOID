package dev.joid.lib.utils.key;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import dev.joid.internal.JOID;
import dev.joid.internal.font.DevFont;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.textfield.TextFieldNode;
import dev.joid.lib.ui.node.impl.design.textfield.TextFieldNodeTest.FieldFont;

public class KeyLayoutTest {

	private static final Map<Key, String> AZERTY_MAP = new EnumMap<>(Key.class);

	static {
		KeyLayoutTest.AZERTY_MAP.put(Key.A, "q");
		KeyLayoutTest.AZERTY_MAP.put(Key.M, ",");
		KeyLayoutTest.AZERTY_MAP.put(Key.Q, "a");
		KeyLayoutTest.AZERTY_MAP.put(Key.W, "z");
		KeyLayoutTest.AZERTY_MAP.put(Key.Z, "w");
		KeyLayoutTest.AZERTY_MAP.put(Key.COMMA, ";");
		KeyLayoutTest.AZERTY_MAP.put(Key.EQUAL, "=");
		KeyLayoutTest.AZERTY_MAP.put(Key.MINUS, ")");
		KeyLayoutTest.AZERTY_MAP.put(Key.SLASH, "!");
		KeyLayoutTest.AZERTY_MAP.put(Key.PERIOD, ":");
		KeyLayoutTest.AZERTY_MAP.put(Key.DIGIT_1, "&");
		KeyLayoutTest.AZERTY_MAP.put(Key.SEMICOLON, "m");
		KeyLayoutTest.AZERTY_MAP.put(Key.BACKSLASH, "*");
		KeyLayoutTest.AZERTY_MAP.put(Key.APOSTROPHE, "ù");
		KeyLayoutTest.AZERTY_MAP.put(Key.LEFT_BRACKET, "^");
		KeyLayoutTest.AZERTY_MAP.put(Key.GRAVE_ACCENT, "²");
		KeyLayoutTest.AZERTY_MAP.put(Key.RIGHT_BRACKET, "$");
	}

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	private final FieldFont    font   = new FieldFont();
	private final List<String> trace  = new ArrayList<>();
	private final KeyLayout    layout = KeyLayout.create(KeyLayoutTest::azerty);

	private File    configDir;
	private boolean devMode;

	@BeforeClass
	public static void loadTheDevFont() {
		DevFont.load();
	}

	@Before
	public void useAnAzertyKeyboard() {
		this.configDir = JOID.inst().getConfigDir();
		this.devMode = JOID.inst().isDevMode();
		JOID.inst().setConfigDir(this.folder.getRoot());
		this.bridges.getWindow().setLayout(this.layout);
	}

	@After
	public void restoreTheSettings() {
		JOID.inst().setConfigDir(this.configDir).setDevMode(this.devMode);
	}

	@Test
	public void translatesTheLettersOfTheActiveLayout() {
		Assert.assertSame(Key.A, this.layout.translate(Key.Q));
		Assert.assertSame(Key.Q, this.layout.translate(Key.A));
		Assert.assertSame(Key.Z, this.layout.translate(Key.W));
		Assert.assertSame(Key.W, this.layout.translate(Key.Z));
		Assert.assertSame(Key.R, this.layout.translate(Key.R));
		Assert.assertSame(Key.M, this.layout.translate(Key.SEMICOLON));
		Assert.assertSame(Key.COMMA, this.layout.translate(Key.M));
		Assert.assertSame(Key.SEMICOLON, this.layout.translate(Key.COMMA));
	}

	@Test
	public void keepsTheKeysThatTypeNoLetter() {
		Assert.assertSame(Key.DIGIT_1, this.layout.translate(Key.DIGIT_1));
		Assert.assertSame(Key.PERIOD, this.layout.translate(Key.PERIOD));
		Assert.assertSame(Key.APOSTROPHE, this.layout.translate(Key.APOSTROPHE));
		Assert.assertSame(Key.ENTER, this.layout.translate(Key.ENTER));
		Assert.assertSame(Key.F5, this.layout.translate(Key.F5));
		Assert.assertSame(Key.NUMPAD_1, this.layout.translate(Key.NUMPAD_1));
		Assert.assertSame(Key.LEFT_CONTROL, this.layout.translate(Key.LEFT_CONTROL));
	}

	@Test
	public void holdsTheLayoutLetterAndThePhysicalKey() {
		this.bridges.getWindow().getKeys().addAll(Arrays.asList(Key.Q, Key.LEFT_CONTROL));
		Assert.assertTrue(Key.A.isDown());
		Assert.assertFalse(Key.Q.isDown());
		Assert.assertTrue(Key.Q.isPhysicalDown());
		Assert.assertFalse(Key.A.isPhysicalDown());
		Assert.assertTrue(Key.LEFT_CONTROL.isDown());
		Assert.assertTrue(Key.LEFT_CONTROL.isPhysicalDown());
	}

	@Test
	public void selectsAllWithTheKeyThatTypesAOnAnAzertyKeyboard() {
		final TextFieldNode field = this.field("hello");
		this.control(Key.Q);
		Assert.assertEquals(0, field.getSelectionStart());
		Assert.assertEquals(5, field.getCursorPos());
	}

	@Test
	public void copiesCutsAndPastesOnAnAzertyKeyboard() {
		final TextFieldNode field = this.field("abc");
		this.control(Key.Q);
		this.control(Key.C);
		Assert.assertEquals("abc", this.bridges.getWindow().getClipboard());
		this.control(Key.X);
		Assert.assertEquals("", field.getText());
		this.control(Key.V);
		this.control(Key.V);
		Assert.assertEquals("abcabc", field.getText());
	}

	@Test
	public void reloadsWithControlROnAnAzertyKeyboardInDevMode() {
		JOID.inst().setDevMode(true);
		final ShortcutUI ui = new ShortcutUI();
		this.bridges.open(ui);
		this.control(Key.R);
		Assert.assertEquals(2, ui.inits);
	}

	@Test
	public void runsAKeybindWithTheKeyThatTypesItsLetter() {
		final ShortcutUI ui = new ShortcutUI();
		this.bridges.open(ui);
		ui.keybind(() -> this.trace.add("undo"), Key.LEFT_CONTROL, Key.Z);
		this.control(Key.Z);
		Assert.assertTrue(this.trace.isEmpty());
		this.control(Key.W);
		Assert.assertEquals(Collections.singletonList("undo"), this.trace);
	}

	@Test
	public void runsAKeybindOnADigitWhateverItsLayoutCharacter() {
		final ShortcutUI ui = new ShortcutUI();
		this.bridges.open(ui);
		ui.keybind(() -> this.trace.add("first tab"), Key.LEFT_CONTROL, Key.DIGIT_1);
		this.control(Key.DIGIT_1);
		Assert.assertEquals(Collections.singletonList("first tab"), this.trace);
	}

	private TextFieldNode field(final String text) {
		final TextFieldNode field = TextFieldNode.create(100D, 100D, 200D).info(TextInfo.create(this.font, 10F)).text(text).focused(true);
		this.bridges.open(new ShortcutUI(field));
		return field;
	}

	private void control(final Key physical) {
		this.bridges.getWindow().getKeys().addAll(Arrays.asList(Key.LEFT_CONTROL, physical));
		this.bridges.getUi().keyTyped('\0', this.layout.translate(physical));
		this.bridges.getWindow().getKeys().clear();
	}

	private static String azerty(final Key key) {
		return KeyLayoutTest.AZERTY_MAP.getOrDefault(key, key.name().toLowerCase(Locale.ROOT));
	}

	public static final class ShortcutUI extends UI {

		private final Node[] nodes;

		private int inits;

		private ShortcutUI(final Node... nodes) {
			this.nodes = nodes;
		}

		@Override
		public void init() {
			this.inits++;
			super.add(this.nodes);
		}

	}

}