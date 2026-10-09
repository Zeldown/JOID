package dev.joid.lib.ui.core.hook.store;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.gson.JsonObject;

import dev.joid.internal.JOID;
import dev.joid.lib.ui.core.hook.store.data.UIStoreData;
import dev.joid.lib.ui.core.hook.store.scope.StoreScope;

import lombok.AllArgsConstructor;
import lombok.NonNull;

public class UIStoreHookTest {

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
		UIStoreHook.destroyStore(new GlobalStore());
		UIStoreHook.destroyStore(new PermanentStore());
		JOID.inst().setConfigDir(this.previous);
		if (this.created) {
			new File("config").delete();
		}
	}

	@Test
	public void createsALocalStoreOnEveryUse() {
		final LocalStore first = UIStoreHook.useStore(LocalStore.class);
		final LocalStore second = UIStoreHook.useStore(LocalStore.class);
		Assert.assertNotSame(first, second);
		Assert.assertEquals(1, first.inits);
		Assert.assertEquals(1, second.inits);
	}

	@Test
	public void sharesAGlobalStore() {
		final GlobalStore store = UIStoreHook.useStore(GlobalStore.class);
		Assert.assertSame(store, UIStoreHook.useStore(GlobalStore.class));
		Assert.assertEquals(1, store.inits);
	}

	@Test
	public void passesTheArgumentsToTheConstructor() {
		final ArgumentStore store = UIStoreHook.useStore(ArgumentStore.class, "shop", 3);
		Assert.assertEquals("shop", store.name);
		Assert.assertEquals(Integer.valueOf(3), store.count);
	}

	@Test
	public void savesAStoreWithoutIdUnderItsClassName() {
		final UnnamedStore store = UIStoreHook.useStore(UnnamedStore.class);
		try {
			store.save();
			Assert.assertTrue(new File(new File(this.folder.getRoot(), "store"), UnnamedStore.class.getName() + ".store").exists());
		} finally {
			UIStoreHook.destroyStore(store);
		}
	}

	@Test
	public void buildsAStoreWithTheConstructorThatAcceptsItsArguments() {
		Assert.assertEquals("name shop", UIStoreHook.useStore(OverloadedStore.class, "shop").origin);
		Assert.assertEquals("size 3", UIStoreHook.useStore(OverloadedStore.class, 3).origin);
		Assert.assertEquals("pair shop 3", UIStoreHook.useStore(OverloadedStore.class, "shop", 3).origin);
	}

	@Test
	public void refusesArgumentsThatNoConstructorOfAStoreAccepts() {
		try {
			UIStoreHook.useStore(OverloadedStore.class, 3D);
			Assert.fail("No constructor of the store accepts a double");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("No public constructor of " + OverloadedStore.class.getName() + " accepts the arguments [3.0]", expected.getMessage());
		}
	}

	@Test
	public void refusesArgumentsThatSeveralConstructorsOfAStoreAccept() {
		try {
			UIStoreHook.useStore(OverloadedStore.class, (Object) null);
			Assert.fail("Both constructors of the store accept null");
		} catch (final IllegalArgumentException expected) {
			Assert.assertTrue(expected.getMessage(), expected.getMessage().startsWith("Several public constructors of " + OverloadedStore.class.getName() + " accept the arguments [null]: "));
		}
	}

	@Test
	public void wrapsAFailingConstructor() {
		try {
			UIStoreHook.useStore(FailingStore.class);
			Assert.fail("The failure of the constructor must reach the caller");
		} catch (final RuntimeException expected) {
			Assert.assertTrue(expected.getCause() instanceof InvocationTargetException);
			Assert.assertEquals("broken store", expected.getCause().getCause().getMessage());
		}
	}

	@Test(expected = IllegalStateException.class)
	public void refusesAStoreWithoutAnnotation() {
		UIStoreHook.useStore(BareStore.class);
	}

	@Test
	public void initializesAPermanentStoreWithoutFile() {
		final PermanentStore store = UIStoreHook.useStore(PermanentStore.class);
		Assert.assertTrue(store.initialized);
		Assert.assertFalse(store.loaded);
		Assert.assertEquals("default", store.value);
	}

	@Test
	public void restoresAPermanentStoreFromItsFile() throws IOException {
		this.write("{\"value\":\"saved\"}");
		final PermanentStore store = UIStoreHook.useStore(PermanentStore.class);
		Assert.assertTrue(store.loaded);
		Assert.assertFalse(store.initialized);
		Assert.assertEquals("saved", store.value);
	}

	@Test
	public void writesAPermanentStoreToItsFile() throws IOException {
		final PermanentStore store = UIStoreHook.useStore(PermanentStore.class);
		store.value = "written";
		UIStoreHook.saveStore(store);
		Assert.assertEquals("{\"value\":\"written\"}", this.read());
	}

	@Test
	public void writesIntoAnExistingStoreFolder() throws IOException {
		Assert.assertTrue(new File(this.folder.getRoot(), "store").mkdirs());
		UIStoreHook.saveStore(new PermanentStore());
		Assert.assertEquals("{\"value\":\"default\"}", this.read());
	}

	@Test
	public void writesAPermanentStoreInUtf8() throws IOException {
		final PermanentStore store = UIStoreHook.useStore(PermanentStore.class);
		store.value = "Français";
		UIStoreHook.saveStore(store);
		Assert.assertEquals("{\"value\":\"Français\"}", this.read());
	}

	@Test
	public void restoresAPermanentStoreFromUtf8() throws IOException {
		this.write("{\"value\":\"Français\"}");
		Assert.assertEquals("Français", UIStoreHook.useStore(PermanentStore.class).value);
	}

	@Test
	public void writesNothingForAStoreThatIsNotPermanent() {
		UIStoreHook.saveStore(new LocalStore());
		UIStoreHook.saveStore(UIStoreHook.useStore(GlobalStore.class));
		Assert.assertFalse(new File(this.folder.getRoot(), "store").exists());
	}

	@Test
	public void savesEveryGlobalStore() throws IOException {
		UIStoreHook.useStore(GlobalStore.class);
		UIStoreHook.useStore(PermanentStore.class).value = "all";
		UIStoreHook.saveAll();
		Assert.assertEquals("{\"value\":\"all\"}", this.read());
	}

	@Test
	public void forgetsADestroyedGlobalStore() {
		final GlobalStore store = UIStoreHook.useStore(GlobalStore.class);
		UIStoreHook.destroyStore(store);
		Assert.assertEquals(1, store.destroys);
		Assert.assertNotSame(store, UIStoreHook.useStore(GlobalStore.class));
	}

	@Test
	public void destroysALocalStore() {
		final LocalStore store = UIStoreHook.useStore(LocalStore.class);
		UIStoreHook.destroyStore(store);
		Assert.assertEquals(1, store.destroys);
	}

	@Test
	public void deletesTheFileOfADestroyedPermanentStore() {
		final PermanentStore store = UIStoreHook.useStore(PermanentStore.class);
		UIStoreHook.saveStore(store);
		Assert.assertTrue(this.file().exists());
		UIStoreHook.destroyStore(store);
		Assert.assertTrue(store.destroyed);
		Assert.assertFalse(this.file().exists());
	}

	@Test
	public void startsOverFromAnEmptyFile() throws IOException {
		this.write("");
		final PermanentStore store = UIStoreHook.useStore(PermanentStore.class);
		Assert.assertTrue(store.initialized);
		Assert.assertFalse(store.loaded);
		Assert.assertFalse(this.file().exists());
	}

	@Test
	public void startsOverFromACorruptedFile() throws IOException {
		this.write("{ broken");
		final List<PermanentStore> stores = new ArrayList<>();
		final String error = UIStoreHookTest.capture(() -> stores.add(UIStoreHook.useStore(PermanentStore.class)));
		Assert.assertTrue(error, error.contains("Failed to load store file: permanent"));
		Assert.assertTrue(stores.get(0).initialized);
		Assert.assertEquals("default", stores.get(0).value);
	}

	@Test
	public void reportsAStoreFileThatCannotBeWritten() {
		final PermanentStore store = UIStoreHook.useStore(PermanentStore.class);
		Assert.assertTrue(this.file().mkdirs());
		final String error = UIStoreHookTest.capture(() -> UIStoreHook.saveStore(store));
		Assert.assertTrue(error, error.contains("Failed to save store file: permanent"));
	}

	@Test
	public void deletesACorruptedFile() throws IOException {
		final File file = new File(new File(this.folder.getRoot(), "store"), "permanent.store");
		Assert.assertTrue(file.getParentFile().mkdirs());
		Files.write(file.toPath(), "{ broken".getBytes(StandardCharsets.UTF_8));

		final String error = UIStoreHookTest.capture(() -> UIStoreHook.useStore(PermanentStore.class));
		Assert.assertFalse(error, error.contains("Failed to delete corrupted store file"));
		Assert.assertFalse(file.exists());
	}

	private File file() {
		return new File(new File(this.folder.getRoot(), "store"), "permanent.store");
	}

	private String read() throws IOException {
		return new String(Files.readAllBytes(this.file().toPath()), StandardCharsets.UTF_8);
	}

	private void write(final String content) throws IOException {
		Assert.assertTrue(this.file().getParentFile().mkdirs());
		Files.write(this.file().toPath(), content.getBytes(StandardCharsets.UTF_8));
	}

	private static String capture(final Runnable runnable) {
		final PrintStream error = System.err;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			System.setErr(new PrintStream(output, true));
			runnable.run();
		} finally {
			System.setErr(error);
		}
		return new String(output.toByteArray(), StandardCharsets.UTF_8);
	}

	@UIStoreData(id = "local")
	public static class LocalStore extends UIStore {

		private int inits;
		private int destroys;

		@Override
		public void init() {
			this.inits++;
		}

		@Override
		public void destroy() {
			this.destroys++;
		}

	}

	@UIStoreData(id = "global", scope = StoreScope.GLOBAL)
	public static class GlobalStore extends UIStore {

		private int inits;
		private int destroys;

		@Override
		public void init() {
			this.inits++;
		}

		@Override
		public void destroy() {
			this.destroys++;
		}

	}

	@UIStoreData(id = "permanent", scope = StoreScope.PERMANENT)
	public static class PermanentStore extends UIStore {

		private String value = "default";

		private boolean initialized;
		private boolean loaded;
		private boolean destroyed;

		@Override
		public void init() {
			this.initialized = true;
		}

		@Override
		public void destroy() {
			this.destroyed = true;
		}

		@Override
		public void load(final @NonNull JsonObject json) {
			this.loaded = true;
			this.value = json.get("value").getAsString();
		}

		@Override
		public void save(final @NonNull JsonObject json) {
			json.addProperty("value", this.value);
		}

	}

	@AllArgsConstructor
	@UIStoreData(id = "argument")
	public static class ArgumentStore extends UIStore {

		private final String  name;
		private final Integer count;

	}

	@UIStoreData(id = "failing")
	public static class FailingStore extends UIStore {

		public FailingStore() {
			throw new IllegalStateException("broken store");
		}

	}

	public static class BareStore extends UIStore {}

	@UIStoreData(scope = StoreScope.PERMANENT)
	public static class UnnamedStore extends UIStore {}

	@UIStoreData(id = "overloaded")
	public static class OverloadedStore extends UIStore {

		private final String origin;

		public OverloadedStore(final String name) {
			this.origin = "name " + name;
		}

		public OverloadedStore(final Integer size) {
			this.origin = "size " + size;
		}

		public OverloadedStore(final String name, final int size) {
			this.origin = "pair " + name + " " + size;
		}

	}

}