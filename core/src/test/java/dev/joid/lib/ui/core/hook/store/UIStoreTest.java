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

import dev.joid.internal.JOID;
import dev.joid.lib.ui.core.hook.store.context.StoreContext;
import dev.joid.lib.ui.core.hook.store.data.UIStoreData;
import lombok.NonNull;

public class UIStoreTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	private File    previous;
	private boolean created;

	@Before
	public void useATemporaryConfig() {
		final boolean existed = new File("config").exists();
		this.previous = JOID.inst().getConfigDir();
		this.created = !existed && new File("config").exists();
		JOID.inst().setConfigDir(this.folder.getRoot());
	}

	@After
	public void restoreTheConfig() {
		JOID.inst().setConfigDir(this.previous);
		if (this.created) {
			new File("config").delete();
		}
	}

	@Test
	public void readsItsAnnotation() {
		final UIStoreData data = new SharedStore().getData();
		Assert.assertEquals("shared", data.id());
		Assert.assertSame(StoreContext.GLOBAL, data.context());
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
			Assert.assertEquals("StoreData annotation is missing on " + BareStore.class.getName(), expected.getMessage());
		}
	}

	@Test
	public void leavesItsJsonUntouchedByDefault() {
		final JsonObject json = new JsonObject();
		final SharedStore store = new SharedStore();
		store.init();
		store.load(json);
		store.save(json);
		store.destroy();
		Assert.assertTrue(json.entrySet().isEmpty());
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

	@UIStoreData(id = "shared", context = StoreContext.GLOBAL)
	public static class SharedStore extends UIStore {}

	@UIStoreData(id = "saved", context = StoreContext.PERMANENT)
	public static class SavedStore extends UIStore {

		@Override
		public void save(final @NonNull JsonObject json) {
			json.addProperty("saved", true);
		}

	}

	public static class BareStore extends UIStore {}

}