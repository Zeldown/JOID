package dev.joid.lib.ui.core.hook.property;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import dev.joid.internal.JOID;
import dev.joid.lib.ui.core.UI;
import lombok.AllArgsConstructor;

public class UIPropertyHookTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	private File previous;

	@Before
	public void useATemporaryConfig() {
		this.previous = JOID.inst().getConfigDir();
		JOID.inst().setConfigDir(this.folder.getRoot());
	}

	@After
	public void restoreTheConfig() {
		JOID.inst().setConfigDir(this.previous);
	}

	@Test
	public void restoresEveryPropertyOnTheNextLoad() {
		final PropertyUI saved = new PropertyUI();
		saved.zoom = 0.75D;
		saved.title = "Shop";
		saved.tabs = Arrays.asList("weapons", "armors");
		saved.position = new Position(12, 34);
		UIPropertyHook.save(saved);

		final PropertyUI loaded = new PropertyUI();
		UIPropertyHook.load(loaded);
		Assert.assertEquals(0.75D, loaded.zoom, 0D);
		Assert.assertEquals("Shop", loaded.title);
		Assert.assertEquals(Arrays.asList("weapons", "armors"), loaded.tabs);
		Assert.assertEquals(12, loaded.position.x);
		Assert.assertEquals(34, loaded.position.y);
	}

	@Test
	public void keepsTheDefaultsWithoutASavedFile() {
		final PropertyUI loaded = new PropertyUI();
		UIPropertyHook.load(loaded);
		Assert.assertEquals(1D, loaded.zoom, 0D);
		Assert.assertEquals("Home", loaded.title);
	}

	@Test
	public void writesOneReadableFilePerUi() throws Exception {
		UIPropertyHook.save(new PropertyUI());
		final File file = new File(new File(this.folder.getRoot(), "property"), PropertyUI.class.getName() + ".property");
		final String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
		Assert.assertTrue(json, json.contains("\"zoomLevel\":1.0"));
		Assert.assertTrue(json, json.contains("\"title\":\"Home\""));
		Assert.assertFalse(json, json.contains("tabs"));
	}

	@Test
	public void forgetsAPropertySetBackToNull() {
		final PropertyUI saved = new PropertyUI();
		UIPropertyHook.save(saved);
		saved.title = null;
		UIPropertyHook.save(saved);

		final PropertyUI loaded = new PropertyUI();
		UIPropertyHook.load(loaded);
		Assert.assertEquals("Home", loaded.title);
	}

	@Test
	public void startsOverFromACorruptedFile() throws Exception {
		final File file = new File(new File(this.folder.getRoot(), "property"), PropertyUI.class.getName() + ".property");
		Assert.assertTrue(file.getParentFile().mkdirs());
		Files.write(file.toPath(), "{ broken".getBytes(StandardCharsets.UTF_8));

		final PropertyUI loaded = new PropertyUI();
		UIPropertyHook.load(loaded);
		Assert.assertEquals(1D, loaded.zoom, 0D);
		Assert.assertFalse(file.exists());
	}

	public static class PropertyUI extends UI {

		@UIProperty("zoomLevel")
		private double zoom = 1D;

		@UIProperty
		private String title = "Home";

		@UIProperty
		private List<String> tabs;

		@UIProperty
		private Position position;

	}

	@AllArgsConstructor
	public static class Position {

		private final int x;
		private final int y;

	}

}