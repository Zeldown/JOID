package dev.joid.test.snapshot;

import java.io.File;
import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SnapshotSettingsTest {

	private Properties previous;

	@Before
	public void isolateTheProperties() {
		this.previous = System.getProperties();
		final Properties properties = new Properties();
		properties.putAll(this.previous);
		properties.remove("joid.snapshot.update");
		properties.remove("joid.snapshot.cache");
		properties.remove("joid.snapshot.output");
		properties.remove("joid.snapshot.references");
		System.setProperties(properties);
	}

	@After
	public void restoreTheProperties() {
		System.setProperties(this.previous);
	}

	@Test
	public void keepsTheProjectFoldersByDefault() {
		Assert.assertFalse(SnapshotSettings.isUpdate());
		Assert.assertEquals(new File(".snapshots/cache"), SnapshotSettings.getCache());
		Assert.assertEquals(new File("build/snapshots/renders"), SnapshotSettings.getOutput());
		Assert.assertEquals(new File(".snapshots/references"), SnapshotSettings.getReferences());
	}

	@Test
	public void readsTheSystemProperties() {
		System.setProperty("joid.snapshot.update", "true");
		System.setProperty("joid.snapshot.cache", "cache");
		System.setProperty("joid.snapshot.output", "renders");
		System.setProperty("joid.snapshot.references", "references");
		Assert.assertTrue(SnapshotSettings.isUpdate());
		Assert.assertEquals(new File("cache"), SnapshotSettings.getCache());
		Assert.assertEquals(new File("renders"), SnapshotSettings.getOutput());
		Assert.assertEquals(new File("references"), SnapshotSettings.getReferences());
	}

	@Test
	public void updatesOnlyWhenTheFlagIsTrue() {
		System.setProperty("joid.snapshot.update", "yes");
		Assert.assertFalse(SnapshotSettings.isUpdate());
	}

}