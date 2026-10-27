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

import dev.joid.internal.TestUtils;
import dev.joid.lib.JOIDEnv;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIDataDebug;
import lombok.AllArgsConstructor;

public class UIPropertyHookTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	private File previous;

	@Before
	public void useATemporaryConfig() {
		this.previous = JOIDEnv.getDataDir();
		JOIDEnv.dataDir(this.folder.getRoot());
	}

	@After
	public void restoreTheConfig() {
		JOIDEnv.dataDir(this.previous);
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
		final String error = TestUtils.captureErr(() -> UIPropertyHook.load(loaded));
		Assert.assertEquals(1D, loaded.zoom, 0D);
		Assert.assertFalse(file.exists());
		Assert.assertEquals("{ broken", new String(Files.readAllBytes(new File(file.getPath() + ".corrupt").toPath()), StandardCharsets.UTF_8));
		Assert.assertTrue(error, error.startsWith("[JOID] The file " + file.getAbsolutePath() + " cannot be read (com.google.gson."));
		Assert.assertTrue(error, error.endsWith("), it is kept as " + file.getAbsolutePath() + ".corrupt and the properties of PropertyUI keep their default values" + System.lineSeparator()));
	}

	@Test
	public void keepsTheDefaultOfAPropertySavedWithAnotherType() throws Exception {
		final File file = new File(new File(this.folder.getRoot(), "property"), PropertyUI.class.getName() + ".property");
		Assert.assertTrue(file.getParentFile().mkdirs());
		Files.write(file.toPath(), "{\"zoomLevel\":\"wide\",\"title\":\"Shop\"}".getBytes(StandardCharsets.UTF_8));

		final PropertyUI loaded = new PropertyUI();
		final String error = TestUtils.captureErr(() -> UIPropertyHook.load(loaded));
		Assert.assertEquals(1D, loaded.zoom, 0D);
		Assert.assertEquals("Shop", loaded.title);
		Assert.assertTrue(error, error.startsWith("[JOID] The property zoomLevel of PropertyUI cannot be read into its field zoom ("));
		Assert.assertTrue(error, error.endsWith("), the field keeps its default value" + System.lineSeparator()));
	}

	@Test
	public void leavesAFinalPropertyAlone() throws Exception {
		UIPropertyHook.save(new FinalPropertyUI());
		final File file = new File(new File(this.folder.getRoot(), "property"), FinalPropertyUI.class.getName() + ".property");
		final String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
		Assert.assertTrue(json, json.contains("\"title\":\"Home\""));
		Assert.assertFalse(json, json.contains("origin"));

		Files.write(file.toPath(), "{\"origin\":{\"x\":5,\"y\":6},\"title\":\"Shop\"}".getBytes(StandardCharsets.UTF_8));
		final FinalPropertyUI loaded = new FinalPropertyUI();
		UIPropertyHook.load(loaded);
		Assert.assertEquals("Shop", loaded.title);
		Assert.assertEquals(1, loaded.origin.x);
		Assert.assertEquals(2, loaded.origin.y);
	}

	@UIDataDebug(profiler = false, hotreload = false)
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

	@UIDataDebug(profiler = false, hotreload = false)
	public static class FinalPropertyUI extends UI {

		@UIProperty
		private final Position origin = new Position(1, 2);

		@UIProperty
		private String title = "Home";

	}

}