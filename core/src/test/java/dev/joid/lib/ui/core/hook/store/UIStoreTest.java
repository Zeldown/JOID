package dev.joid.lib.ui.core.hook.store;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.gson.JsonObject;

import dev.joid.internal.JOIDEnv;
import lombok.NonNull;

public class UIStoreTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	private File    previous;
	private boolean created;

	@Before
	public void useATemporaryConfig() {
		final boolean existed = new File("config").exists();
		this.previous = JOIDEnv.getDataDir();
		this.created = !existed && new File("config").exists();
		JOIDEnv.dataDir(this.folder.getRoot());
	}

	@After
	public void restoreTheConfig() {
		JOIDEnv.dataDir(this.previous);
		if (this.created) {
			new File("config").delete();
		}
	}

	@Test
	public void readsItsAnnotation() {
		final UIStoreData data = new SharedStore().getData();
		Assert.assertEquals("shared", data.id());
		Assert.assertSame(UIStoreScope.GLOBAL, data.scope());
	}

	@Test
	public void keepsItsAnnotation() {
		final SharedStore store = new SharedStore();
		Assert.assertSame(store.getData(), store.getData());
	}

	@Test
	public void refusesAStoreWithoutAnnotation() {
		try {
			new BareStore().getData();
			Assert.fail("A store without annotation must be refused");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The store " + BareStore.class.getName() + " has no @UIStoreData annotation, add it to the class", expected.getMessage());
		}
	}

	@Test
	public void writesAnEmptyJsonByDefault() {
		final SharedStore store = new SharedStore();
		store.init();
		final JsonObject ignored = new JsonObject();
		ignored.addProperty("ignored", true);
		store.fromJson(ignored);
		store.destroy();
		Assert.assertEquals(new JsonObject(), store.toJson());
	}

	@Test
	public void savesItselfThroughTheHook() throws IOException {
		new SavedStore().save();
		final File file = new File(new File(this.folder.getRoot(), "store"), "saved.store");
		Assert.assertEquals("{\"saved\":true}", new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
	}

	@Test
	public void savesNothingWhenNotPermanent() {
		new SharedStore().save();
		Assert.assertFalse(new File(this.folder.getRoot(), "store").exists());
	}

	@UIStoreData(id = "shared", scope = UIStoreScope.GLOBAL)
	public static class SharedStore extends UIStore {}

	@UIStoreData(id = "saved", scope = UIStoreScope.PERMANENT)
	public static class SavedStore extends UIStore {

		@Override
		public @NonNull JsonObject toJson() {
			final JsonObject json = new JsonObject();
			json.addProperty("saved", true);
			return json;
		}

	}

	public static class BareStore extends UIStore {}

}