package dev.joid.lib.ui.core.hook.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import dev.joid.internal.JOID;
import dev.joid.lib.ui.core.hook.store.context.StoreContext;
import dev.joid.lib.ui.core.hook.store.data.UIStoreData;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@SuppressWarnings("unchecked")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UIStoreHook {

	private static final Gson GSON = new GsonBuilder().create();
	private static final Map<Class<? extends UIStore>, UIStore> GLOBAL_CACHE = new HashMap<>();

	public static <T extends UIStore> @NonNull T useStore(final @NonNull Class<T> clazz, final Object... args) {
		if (UIStoreHook.GLOBAL_CACHE.containsKey(clazz)) {
			return (T) UIStoreHook.GLOBAL_CACHE.get(clazz);
		}

		final T store = UIStoreHook.createStoreInstance(clazz, args);
		final File file = UIStoreHook.getFile(UIStoreHook.getId(store));
		if (store.getData().context() == StoreContext.PERMANENT && file.exists()) {
			final JsonObject json = UIStoreHook.loadFile(UIStoreHook.getId(store));
			if (json == null) {
				if (!file.delete()) {
					System.err.println("Failed to delete corrupted store file: " + file.getAbsolutePath());
				}
				store.init();
			} else {
				store.load(json);
			}
		} else {
			store.init();
		}

		if (store.getData().context().isGlobal()) {
			UIStoreHook.GLOBAL_CACHE.put(clazz, store);
		}

		return store;
	}

	public static void destroyStore(final @NonNull UIStore store) {
		final Class<? extends UIStore> clazz = store.getClass();
		final UIStoreData data = store.getData();
		if (data.context().isGlobal()) {
			UIStoreHook.GLOBAL_CACHE.remove(clazz);
		}

		if (store.getData().context() == StoreContext.PERMANENT) {
			UIStoreHook.deleteFile(UIStoreHook.getId(store));
		}

		store.destroy();
	}

	public static void saveStore(final @NonNull UIStore store) {
		if (store.getData().context() != StoreContext.PERMANENT) {
			return;
		}

		final JsonObject json = new JsonObject();
		store.save(json);
		UIStoreHook.saveFile(UIStoreHook.getId(store), json);
	}

	public static void saveAll() {
		for (final UIStore store : UIStoreHook.GLOBAL_CACHE.values()) {
			UIStoreHook.saveStore(store);
		}
	}

	private static <T extends UIStore> T createStoreInstance(final @NonNull Class<T> clazz, final Object... args) {
		final List<Constructor<?>> constructors = new ArrayList<>();
		for (final Constructor<?> constructor : clazz.getConstructors()) {
			if (UIStoreHook.accepts(constructor, args)) {
				constructors.add(constructor);
			}
		}

		if (constructors.isEmpty()) {
			throw new IllegalArgumentException("No public constructor of " + clazz.getName() + " accepts the arguments " + Arrays.toString(args));
		}

		if (constructors.size() > 1) {
			throw new IllegalArgumentException("Several public constructors of " + clazz.getName() + " accept the arguments " + Arrays.toString(args) + ": " + constructors);
		}

		try {
			return (T) constructors.get(0).newInstance(args);
		} catch (final Exception e) {
			throw new RuntimeException("Failed to create store instance for class " + clazz.getName(), e);
		}
	}

	private static boolean accepts(final Constructor<?> constructor, final Object... args) {
		final Class<?>[] types = constructor.getParameterTypes();
		if (types.length != args.length) {
			return false;
		}

		for (int i = 0; i < types.length; i++) {
			if (args[i] == null ? types[i].isPrimitive() : !MethodType.methodType(types[i]).wrap().returnType().isInstance(args[i])) {
				return false;
			}
		}

		return true;
	}

	private static @NonNull String getId(final @NonNull UIStore store) {
		final String id = store.getData().id();
		return id.isEmpty() ? store.getClass().getName() : id;
	}

	private static void deleteFile(final @NonNull String id) {
		final File parent = new File(JOID.inst().getConfigDir(), "store");
		final File file = new File(parent, id + ".store");
		if (file.exists()) {
			file.delete();
		}
	}

	private static @NonNull File getFile(final @NonNull String id) {
		final File parent = new File(JOID.inst().getConfigDir(), "store");
		return new File(parent, id + ".store");
	}

	private static @NonNull JsonObject loadFile(final @NonNull String id) {
		try {
			final File file = UIStoreHook.getFile(id);
			try (final Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
				return UIStoreHook.GSON.fromJson(reader, JsonObject.class);
			}
		} catch (final Exception e) {
			System.err.println("Failed to load store file: " + id);
			e.printStackTrace();
		}

		return null;
	}

	private static void saveFile(final @NonNull String id, final @NonNull JsonObject json) {
		try {
			final File file = UIStoreHook.getFile(id);
			if (!file.exists()) {
				final File parent = file.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}

				if (!file.createNewFile()) {
					System.err.println("Failed to create store file: " + file.getAbsolutePath());
				}
			}

			try (final Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
				UIStoreHook.GSON.toJson(json, writer);
			}
		} catch (final Exception e) {
			System.err.println("Failed to save store file: " + id);
			e.printStackTrace();
		}
	}

}