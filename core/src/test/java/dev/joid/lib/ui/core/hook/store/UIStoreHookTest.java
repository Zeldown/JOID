package dev.joid.lib.ui.core.hook.store;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import dev.joid.internal.JOIDWarnings;
import dev.joid.internal.TestUtils;
import dev.joid.lib.JOIDEnv;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import lombok.AllArgsConstructor;
import lombok.NonNull;

public class UIStoreHookTest {

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
		UIStoreHook.destroyStore(new GlobalStore());
		UIStoreHook.destroyStore(new PermanentStore());
		UIStoreHook.destroyStore(new SignalStore());
		UIStoreHook.destroyStore(new MapStore());
		UIStoreHook.destroyStore(new UnsavableStore());
		JOIDEnv.dataDir(this.previous);
		JOIDWarnings.forget();
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
	public void leavesTheFileOfAnUnchangedStoreUntouched() throws IOException {
		this.write("{\"value\":\"saved\"}");
		final PermanentStore store = UIStoreHook.useStore(PermanentStore.class, PermanentStore::new);
		Assert.assertTrue(this.file().setLastModified(1000L));
		UIStoreHook.saveStore(store);
		Assert.assertEquals(1000L, this.file().lastModified());
		store.value = "changed";
		UIStoreHook.saveStore(store);
		Assert.assertEquals("{\"value\":\"changed\"}", this.read());
		Assert.assertTrue(this.file().setLastModified(1000L));
		UIStoreHook.saveStore(store);
		Assert.assertEquals(1000L, this.file().lastModified());
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
	public void restoresTheJsonTypesOfAPermanentStore() throws IOException {
		final String json = "{\"text\":\"a\",\"count\":7,\"ratio\":0.5,\"on\":true,\"list\":[1,\"b\"],\"object\":{\"key\":\"c\"}}";
		final File file = new File(new File(this.folder.getRoot(), "store"), "map.store");
		Assert.assertTrue(file.getParentFile().mkdirs());
		Files.write(file.toPath(), json.getBytes(StandardCharsets.UTF_8));

		final MapStore store = UIStoreHook.useStore(MapStore.class, MapStore::new);
		Assert.assertEquals("a", store.json.get("text"));
		Assert.assertEquals(7L, store.json.get("count"));
		Assert.assertEquals(0.5D, store.json.get("ratio"));
		Assert.assertEquals(true, store.json.get("on"));
		Assert.assertEquals(Arrays.asList(1L, "b"), store.json.get("list"));
		Assert.assertEquals(Collections.singletonMap("key", "c"), store.json.get("object"));

		UIStoreHook.saveStore(store);
		Assert.assertEquals(json, new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
	}

	@Test
	public void writesTheSpecialFloatingPointValuesOfAPermanentStore() throws IOException {
		final MapStore store = UIStoreHook.useStore(MapStore.class, MapStore::new);
		store.json = new LinkedHashMap<>();
		store.json.put("nan", Double.NaN);
		store.json.put("positive", Double.POSITIVE_INFINITY);
		store.json.put("negative", Float.NEGATIVE_INFINITY);
		UIStoreHook.saveStore(store);
		UIStoreHook.destroyStore(store);

		final File file = new File(new File(this.folder.getRoot(), "store"), "map.store");
		Assert.assertEquals("{\"nan\":NaN,\"positive\":Infinity,\"negative\":-Infinity}", new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
		Assert.assertEquals(3, UIStoreHook.useStore(MapStore.class, MapStore::new).json.size());
	}

	@Test
	public void reloadsASignalFromItsValue() throws IOException {
		final SignalStore store = UIStoreHook.useStore(SignalStore.class, SignalStore::new);
		store.count.set(7);
		UIStoreHook.saveStore(store);
		UIStoreHook.destroyStore(store);

		final File file = new File(new File(this.folder.getRoot(), "store"), "signal.store");
		Assert.assertEquals("{\"count\":7}", new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
		Assert.assertEquals(7, UIStoreHook.useStore(SignalStore.class, SignalStore::new).count.get().intValue());
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
	public void savesTheOtherGlobalStoresWhenOneCannotBeSerialized() throws IOException {
		UIStoreHook.useStore(UnsavableStore.class, UnsavableStore::new);
		UIStoreHook.useStore(PermanentStore.class, PermanentStore::new).value = "all";
		final String error = TestUtils.captureErr(UIStoreHook::saveAll);
		Assert.assertTrue(error, error.startsWith("[JOID] The store " + UnsavableStore.class.getName() + " cannot be saved (java.lang.IllegalStateException: broken toJson), the other stores are saved anyway: catch the exception inside toJson() to handle it"));
		Assert.assertEquals("{\"value\":\"all\"}", this.read());
	}

	@Test
	public void letsTheFailureOfAnExplicitSaveReachTheCaller() {
		try {
			new UnsavableStore().save();
			Assert.fail("The failure of toJson() must reach the caller of save()");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("broken toJson", expected.getMessage());
		}
	}

	@Test
	public void forgetsAGlobalStoreWhoseDestroyFails() {
		final UndestroyableStore store = UIStoreHook.useStore(UndestroyableStore.class, UndestroyableStore::new);
		final String error = TestUtils.captureErr(() -> UIStoreHook.destroyStore(store));
		final UndestroyableStore next = UIStoreHook.useStore(UndestroyableStore.class, UndestroyableStore::new);
		TestUtils.captureErr(() -> UIStoreHook.destroyStore(next));
		Assert.assertTrue(error, error.startsWith("[JOID] The destroy of the store " + UndestroyableStore.class.getName() + " failed (java.lang.IllegalStateException: broken destroy), it is destroyed anyway: catch the exception inside destroy() to handle it"));
		Assert.assertNotSame(store, next);
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
		final List<PermanentStore> stores = new ArrayList<>();
		final String error = TestUtils.captureErr(() -> stores.add(UIStoreHook.useStore(PermanentStore.class, PermanentStore::new)));
		Assert.assertTrue(error, error.startsWith("[JOID] The file " + this.file().getAbsolutePath() + " cannot be read (it holds no JSON object), it is kept as " + this.file().getAbsolutePath() + ".corrupt and "));
		final PermanentStore store = stores.get(0);
		Assert.assertTrue(store.initialized);
		Assert.assertFalse(store.loaded);
		Assert.assertFalse(this.file().exists());
		Assert.assertTrue(new File(this.file().getPath() + ".corrupt").exists());
	}

	@Test
	public void startsOverFromACorruptedFile() throws IOException {
		this.write("{ broken");
		final List<PermanentStore> stores = new ArrayList<>();
		final String error = TestUtils.captureErr(() -> stores.add(UIStoreHook.useStore(PermanentStore.class, PermanentStore::new)));
		Assert.assertTrue(error, error.startsWith("[JOID] The file " + this.file().getAbsolutePath() + " cannot be read (com.google.gson.JsonSyntaxException: "));
		Assert.assertTrue(error, error.endsWith("), it is kept as " + this.file().getAbsolutePath() + ".corrupt and the store starts from init()" + System.lineSeparator()));
		Assert.assertTrue(stores.get(0).initialized);
		Assert.assertEquals("default", stores.get(0).value);
	}

	@Test
	public void startsFromInitWhenTheStoreCannotBeRestored() throws IOException {
		this.write("{}");
		final List<PermanentStore> stores = new ArrayList<>();
		final String error = TestUtils.captureErr(() -> stores.add(UIStoreHook.useStore(PermanentStore.class, PermanentStore::new)));
		Assert.assertTrue(error, error.startsWith("[JOID] The store " + PermanentStore.class.getName() + " cannot be restored from " + this.file().getAbsolutePath() + " (java.lang.NullPointerException), it starts from init(): catch the exception inside fromJson() to handle it"));
		Assert.assertEquals("init", stores.get(0).order);
		Assert.assertFalse(stores.get(0).loaded);
		Assert.assertSame(stores.get(0), UIStoreHook.useStore(PermanentStore.class, PermanentStore::new));
		Assert.assertFalse(this.file().exists());
		Assert.assertEquals("{}", new String(Files.readAllBytes(new File(this.file().getPath() + ".corrupt").toPath()), StandardCharsets.UTF_8));
	}

	@Test
	public void keepsAStoreFileItCannotRestoreThroughTheNextSessions() throws IOException {
		this.write("{\"value\":{\"volume\":5}}");
		final File corrupt = new File(this.file().getPath() + ".corrupt");
		for (int session = 0; session < 2; session++) {
			TestUtils.captureErr(() -> UIStoreHook.useStore(PermanentStore.class, PermanentStore::new));
			UIStoreHook.saveAll();
			UIStoreHook.destroyStore(new PermanentStore());
		}
		Assert.assertEquals("{\"value\":\"default\"}", this.read());
		Assert.assertEquals("{\"value\":{\"volume\":5}}", new String(Files.readAllBytes(corrupt.toPath()), StandardCharsets.UTF_8));
	}

	@Test
	public void reportsAStoreFileThatCannotBeWritten() {
		final PermanentStore store = UIStoreHook.useStore(PermanentStore.class, PermanentStore::new);
		Assert.assertTrue(this.file().mkdirs());
		final String error = TestUtils.captureErr(() -> UIStoreHook.saveStore(store));
		Assert.assertTrue(error, error.startsWith("[JOID] The file " + this.file().getAbsolutePath() + " cannot be saved ("));
		Assert.assertTrue(error, error.endsWith("), check that the data folder can be written" + System.lineSeparator()));
	}

	@Test
	public void keepsACorruptedFileInPlaceOfAnOlderOne() throws IOException {
		this.write("{ broken");
		final File corrupt = new File(this.file().getPath() + ".corrupt");
		Files.write(corrupt.toPath(), "older".getBytes(StandardCharsets.UTF_8));

		TestUtils.captureErr(() -> UIStoreHook.useStore(PermanentStore.class, PermanentStore::new));
		Assert.assertFalse(this.file().exists());
		Assert.assertEquals("{ broken", new String(Files.readAllBytes(corrupt.toPath()), StandardCharsets.UTF_8));
	}

	@Test
	public void leavesAFileItCannotReadUntouched() {
		Assert.assertTrue(this.file().mkdirs());
		final List<PermanentStore> stores = new ArrayList<>();
		final String error = TestUtils.captureErr(() -> stores.add(UIStoreHook.useStore(PermanentStore.class, PermanentStore::new)));
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
		public @NonNull Map<String, Object> toJson() {
			final Map<String, Object> json = new LinkedHashMap<>();
			json.put("value", this.value);
			return json;
		}

		@Override
		public void fromJson(final @NonNull Map<String, Object> json) {
			this.loaded = true;
			this.order = (this.order + " fromJson").trim();
			this.value = (String) Objects.requireNonNull(json.get("value"));
		}

	}

	@UIStoreData(id = "signal", scope = UIStoreScope.PERMANENT)
	public static class SignalStore extends UIStore {

		private final IntegerSignal count = new IntegerSignal(3);

		@Override
		public @NonNull Map<String, Object> toJson() {
			final Map<String, Object> json = new LinkedHashMap<>();
			json.put("count", this.count.get());
			return json;
		}

		@Override
		public void fromJson(final @NonNull Map<String, Object> json) {
			this.count.set(((Number) json.get("count")).intValue());
		}

	}

	@UIStoreData(id = "map", scope = UIStoreScope.PERMANENT)
	public static class MapStore extends UIStore {

		private Map<String, Object> json;

		@Override
		public @NonNull Map<String, Object> toJson() {
			return this.json;
		}

		@Override
		public void fromJson(final @NonNull Map<String, Object> json) {
			this.json = json;
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

	@UIStoreData(id = "unsavable", scope = UIStoreScope.PERMANENT)
	public static class UnsavableStore extends UIStore {

		@Override
		public @NonNull Map<String, Object> toJson() {
			throw new IllegalStateException("broken toJson");
		}

	}

	@UIStoreData(id = "undestroyable", scope = UIStoreScope.GLOBAL)
	public static class UndestroyableStore extends UIStore {

		@Override
		public void destroy() {
			throw new IllegalStateException("broken destroy");
		}

	}

}