package be.zeldown.joid.lib.ui.core.hook.store;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.ui.core.hook.store.context.StoreContext;
import be.zeldown.joid.lib.ui.core.hook.store.data.UIStoreData;
import lombok.NonNull;

@SuppressWarnings("unchecked")
public final class UIStoreHook {

	private static final Gson GSON = new GsonBuilder().create();
	private static final Map<Class<? extends UIStore>, UIStore> GLOBAL_CACHE = new HashMap<>();

	public static <T extends UIStore> @NonNull T useStore(final @NonNull Class<T> clazz, final Object... args) {
		if (UIStoreHook.GLOBAL_CACHE.containsKey(clazz)) {
			return (T) UIStoreHook.GLOBAL_CACHE.get(clazz);
		}

		final T store = UIStoreHook.createStoreInstance(clazz, args);
		final File file = UIStoreHook.getFile(store.getData().id());
		if (store.getData().context() == StoreContext.PERMANENT && file.exists()) {
			final JsonObject json = UIStoreHook.loadFile(store.getData().id());
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
			UIStoreHook.deleteFile(data.id());
		}

		store.destroy();
	}

	public static void saveStore(final @NonNull UIStore store) {
		if (store.getData().context() != StoreContext.PERMANENT) {
			return;
		}

		final JsonObject json = new JsonObject();
		store.save(json);
		UIStoreHook.saveFile(store.getData().id(), json);
	}

	public static void saveAll() {
		for (final UIStore store : UIStoreHook.GLOBAL_CACHE.values()) {
			UIStoreHook.saveStore(store);
		}
	}

	private static <T extends UIStore> T createStoreInstance(final @NonNull Class<T> clazz, final Object... args) {
		try {
			T store = null;
			for (final Constructor<?> constructor : clazz.getConstructors()) {
				if (constructor.getParameterCount() != args.length) {
					continue;
				}

				constructor.setAccessible(true);
				store = (T) constructor.newInstance(args);
			}

			if (store == null) {
				throw new IllegalArgumentException("No constructor found for class " + clazz.getName() + " with the provided arguments.");
			}

			return store;
		} catch (final Exception e) {
			throw new RuntimeException("Failed to create store instance for class " + clazz.getName(), e);
		}
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
			if (!file.exists()) {
				return null;
			}

			final FileReader reader = new FileReader(file);
			final JsonObject json = UIStoreHook.GSON.fromJson(reader, JsonObject.class);
			reader.close();
			return json;
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

			final FileWriter writer = new FileWriter(file);
			UIStoreHook.GSON.toJson(json, writer);
			writer.close();
		} catch (final Exception e) {
			System.err.println("Failed to save store file: " + id);
			e.printStackTrace();
		}
	}

}