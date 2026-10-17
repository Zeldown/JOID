package dev.joid.lib.ui.core.hook.store;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import dev.joid.internal.JOIDEnv;
import dev.joid.lib.signal.impl.iterable.ListSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;

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
		this.previous = JOIDEnv.getDataDir();
		this.created = !existed && new File("config").exists();
		JOIDEnv.dataDir(this.folder.getRoot());
	}

	@After
	public void restoreTheConfig() {
		UIStoreHook.destroyStore(new GlobalStore());
		UIStoreHook.destroyStore(new PermanentStore());
		UIStoreHook.destroyStore(new SignalStore());
		UIStoreHook.destroyStore(new ListStore());
		JOIDEnv.dataDir(this.previous);
		if (this.created) {
			new File("config").delete();
		}
	}

	@Test
	public void createsALocalStoreOnEveryUse() {
		final LocalStore first = UIStoreHook.useStore(LocalStore.class, LocalStore::new);
		final LocalStore second = UIStoreHook.useStore(LocalStore.class, LocalStore::new);
		Assert.assertNotSame(first, second);
		Assert.assertEquals(1, first.inits);
		Assert.assertEquals(1, second.inits);
	}

	@Test
	public void sharesAGlobalStore() {
		final GlobalStore store = UIStoreHook.useStore(GlobalStore.class, GlobalStore::new);
		Assert.assertSame(store, UIStoreHook.useStore(GlobalStore.class, GlobalStore::new));
		Assert.assertEquals(1, store.inits);
	}

	@Test
	public void buildsAStoreWithItsFactory() {
		final ArgumentStore store = UIStoreHook.useStore(ArgumentStore.class, () -> new ArgumentStore("shop", 3));
		Assert.assertEquals("shop", store.name);
		Assert.assertEquals(Integer.valueOf(3), store.count);
	}

	@Test
	public void savesAStoreWithoutIdUnderItsClassName() {
		final UnnamedStore store = UIStoreHook.useStore(UnnamedStore.class, UnnamedStore::new);
		try {
			store.save();
			Assert.assertTrue(new File(new File(this.folder.getRoot(), "store"), UnnamedStore.class.getName() + ".store").exists());
		} finally {
			UIStoreHook.destroyStore(store);
		}
	}

	@Test
	public void letsTheFailureOfItsFactoryReachTheCaller() {
		try {
			UIStoreHook.useStore(FailingStore.class, FailingStore::new);
			Assert.fail("The failure of the factory must reach the caller");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("broken store", expected.getMessage());
		}
	}

	@Test
	public void refusesAStoreWithoutAnnotationBeforeBuildingIt() {
		try {
			UIStoreHook.useStore(BareStore.class, BareStore::new);
			Assert.fail("A store without annotation must be refused");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The store " + BareStore.class.getName() + " has no @UIStoreData annotation, add it to the class", expected.getMessage());
		}
	}

	@Test
	public void initializesAPermanentStoreWithoutFile() {
		final PermanentStore store = UIStoreHook.useStore(PermanentStore.class, PermanentStore::new);
		Assert.assertTrue(store.initialized);
		Assert.assertFalse(store.loaded);
		Assert.assertEquals("default", store.value);
	}

	@Test
	public void restoresAPermanentStoreFromItsFile() throws IOException {
		this.write("{\"value\":\"saved\"}");
		final PermanentStore store = UIStoreHook.useStore(PermanentStore.class, PermanentStore::new);
		Assert.assertEquals("init fromJson", store.order);
		Assert.assertEquals("saved", store.value);
	}

	@Test
	public void writesAPermanentStoreToItsFile() throws IOException {
		final PermanentStore store = UIStoreHook.useStore(PermanentStore.class, PermanentStore::new);
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
		final PermanentStore store = UIStoreHook.useStore(PermanentStore.class, PermanentStore::new);
		store.value = "Français";
		UIStoreHook.saveStore(store);
		Assert.assertEquals("{\"value\":\"Français\"}", this.read());
	}

	@Test
	public void restoresAPermanentStoreFromUtf8() throws IOException {
		this.write("{\"value\":\"Français\"}");
		Assert.assertEquals("Français", UIStoreHook.useStore(PermanentStore.class, PermanentStore::new).value);
	}

	@Test
	public void reloadsASignalWithoutItsStartingValue() throws IOException {
		final SignalStore store = UIStoreHook.useStore(SignalStore.class, SignalStore::new);
		store.count.set(7);
		UIStoreHook.saveStore(store);
		UIStoreHook.destroyStore(store);

		final File file = new File(new File(this.folder.getRoot(), "store"), "signal.store");
		Assert.assertEquals("{\"count\":{\"value\":7}}", new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
		Assert.assertEquals(7, UIStoreHook.useStore(SignalStore.class, SignalStore::new).count.get().intValue());
	}

	@Test
	public void reloadsAReadOnlyCollectionSignal() throws IOException {
		final ListStore store = UIStoreHook.useStore(ListStore.class, ListStore::new);
		store.names.add("b");
		UIStoreHook.saveStore(store);
		UIStoreHook.destroyStore(store);

		final ListStore loaded = UIStoreHook.useStore(ListStore.class, ListStore::new);
		Assert.assertEquals(Arrays.asList("a", "b"), loaded.names.get());
		try {
			loaded.names.get().add("x");
			Assert.fail("The list of a reloaded ListSignal is read-only");
		} catch (final UnsupportedOperationException expected) {
			Assert.assertEquals(Arrays.asList("a", "b"), loaded.names.peek());
		}
		Assert.assertEquals(Arrays.asList("a", "b", "c"), loaded.names.add("c").get());
		Assert.assertEquals(Collections.emptyList(), loaded.names.reset().get());
	}

	@Test
	public void writesNothingForAStoreThatIsNotPermanent() {
		UIStoreHook.saveStore(new LocalStore());
		UIStoreHook.saveStore(UIStoreHook.useStore(GlobalStore.class, GlobalStore::new));
		Assert.assertFalse(new File(this.folder.getRoot(), "store").exists());
	}

	@Test
	public void savesEveryGlobalStore() throws IOException {
		UIStoreHook.useStore(GlobalStore.class, GlobalStore::new);
		UIStoreHook.useStore(PermanentStore.class, PermanentStore::new).value = "all";
		UIStoreHook.saveAll();
		Assert.assertEquals("{\"value\":\"all\"}", this.read());
	}

	@Test
	public void forgetsADestroyedGlobalStore() {
		final GlobalStore store = UIStoreHook.useStore(GlobalStore.class, GlobalStore::new);
		UIStoreHook.destroyStore(store);
		Assert.assertEquals(1, store.destroys);
		Assert.assertNotSame(store, UIStoreHook.useStore(GlobalStore.class, GlobalStore::new));
	}

	@Test
	public void destroysALocalStore() {
		final LocalStore store = UIStoreHook.useStore(LocalStore.class, LocalStore::new);
		UIStoreHook.destroyStore(store);
		Assert.assertEquals(1, store.destroys);
	}

	@Test
	public void keepsTheFileOfADestroyedPermanentStore() {
		final PermanentStore store = UIStoreHook.useStore(PermanentStore.class, PermanentStore::new);
		UIStoreHook.saveStore(store);
		Assert.assertTrue(this.file().exists());
		UIStoreHook.destroyStore(store);
		Assert.assertTrue(store.destroyed);
		Assert.assertTrue(this.file().exists());
	}

	@Test
	public void startsOverFromAnEmptyFile() throws IOException {
		this.write("");
		final PermanentStore store = UIStoreHook.useStore(PermanentStore.class, PermanentStore::new);
		Assert.assertTrue(store.initialized);
		Assert.assertFalse(store.loaded);
		Assert.assertFalse(this.file().exists());
		Assert.assertTrue(new File(this.file().getPath() + ".corrupt").exists());
	}

	@Test
	public void startsOverFromACorruptedFile() throws IOException {
		this.write("{ broken");
		final List<PermanentStore> stores = new ArrayList<>();
		final String error = UIStoreHookTest.capture(() -> stores.add(UIStoreHook.useStore(PermanentStore.class, PermanentStore::new)));
		Assert.assertTrue(error, error.startsWith("[JOID] The file " + this.file().getAbsolutePath() + " cannot be read (com.google.gson.JsonSyntaxException: "));
		Assert.assertTrue(error, error.endsWith("), it is kept as " + this.file().getAbsolutePath() + ".corrupt and the store starts from init()" + System.lineSeparator()));
		Assert.assertTrue(stores.get(0).initialized);
		Assert.assertEquals("default", stores.get(0).value);
	}

	@Test
	public void reportsAStoreFileThatCannotBeWritten() {
		final PermanentStore store = UIStoreHook.useStore(PermanentStore.class, PermanentStore::new);
		Assert.assertTrue(this.file().mkdirs());
		final String error = UIStoreHookTest.capture(() -> UIStoreHook.saveStore(store));
		Assert.assertTrue(error, error.startsWith("[JOID] The file " + this.file().getAbsolutePath() + " cannot be saved ("));
		Assert.assertTrue(error, error.endsWith("), check that the data folder can be written" + System.lineSeparator()));
	}

	@Test
	public void keepsACorruptedFileInPlaceOfAnOlderOne() throws IOException {
		this.write("{ broken");
		final File corrupt = new File(this.file().getPath() + ".corrupt");
		Files.write(corrupt.toPath(), "older".getBytes(StandardCharsets.UTF_8));

		UIStoreHookTest.capture(() -> UIStoreHook.useStore(PermanentStore.class, PermanentStore::new));
		Assert.assertFalse(this.file().exists());
		Assert.assertEquals("{ broken", new String(Files.readAllBytes(corrupt.toPath()), StandardCharsets.UTF_8));
	}

	@Test
	public void leavesAFileItCannotReadUntouched() {
		Assert.assertTrue(this.file().mkdirs());
		final List<PermanentStore> stores = new ArrayList<>();
		final String error = UIStoreHookTest.capture(() -> stores.add(UIStoreHook.useStore(PermanentStore.class, PermanentStore::new)));
		Assert.assertTrue(error, error.startsWith("[JOID] The file " + this.file().getAbsolutePath() + " cannot be read ("));
		Assert.assertTrue(error, error.endsWith("), it is left untouched and the store starts from init()" + System.lineSeparator()));
		Assert.assertTrue(stores.get(0).initialized);
		Assert.assertTrue(this.file().isDirectory());
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

	@UIStoreData(id = "global", scope = UIStoreScope.GLOBAL)
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

	@UIStoreData(id = "permanent", scope = UIStoreScope.PERMANENT)
	public static class PermanentStore extends UIStore {

		private String value = "default";
		private String order = "";

		private boolean initialized;
		private boolean loaded;
		private boolean destroyed;

		@Override
		public void init() {
			this.initialized = true;
			this.order = (this.order + " init").trim();
		}

		@Override
		public void destroy() {
			this.destroyed = true;
		}

		@Override
		public void fromJson(final @NonNull JsonObject json) {
			this.loaded = true;
			this.order = (this.order + " fromJson").trim();
			this.value = json.get("value").getAsString();
		}

		@Override
		public @NonNull JsonObject toJson() {
			final JsonObject json = new JsonObject();
			json.addProperty("value", this.value);
			return json;
		}

	}

	@UIStoreData(id = "signal", scope = UIStoreScope.PERMANENT)
	public static class SignalStore extends UIStore {

		private static final Gson GSON = new GsonBuilder().create();

		private IntegerSignal count = new IntegerSignal(3);

		@Override
		public void fromJson(final @NonNull JsonObject json) {
			this.count = SignalStore.GSON.fromJson(json.get("count"), IntegerSignal.class);
		}

		@Override
		public @NonNull JsonObject toJson() {
			final JsonObject json = new JsonObject();
			json.add("count", SignalStore.GSON.toJsonTree(this.count));
			return json;
		}

	}

	@UIStoreData(id = "list", scope = UIStoreScope.PERMANENT)
	public static class ListStore extends UIStore {

		private static final Gson GSON = new GsonBuilder().create();

		private ListSignal<String> names = new ListSignal<>(Collections.singletonList("a"));

		@Override
		public void fromJson(final @NonNull JsonObject json) {
			this.names = ListStore.GSON.fromJson(json.get("names"), new TypeToken<ListSignal<String>>() {}.getType());
		}

		@Override
		public @NonNull JsonObject toJson() {
			final JsonObject json = new JsonObject();
			json.add("names", ListStore.GSON.toJsonTree(this.names));
			return json;
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

	public static class BareStore extends UIStore {

		public BareStore() {
			throw new IllegalStateException("built without annotation");
		}

	}

	@UIStoreData(scope = UIStoreScope.PERMANENT)
	public static class UnnamedStore extends UIStore {}

}