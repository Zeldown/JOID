package dev.joid.lib.ui.core.hook.store;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.ToNumberPolicy;

import dev.joid.internal.JOIDLog;
import dev.joid.internal.JsonFiles;
import dev.joid.lib.JOIDEnv;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UIStoreHook {

	private static final Gson GSON = new GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).disableHtmlEscaping().create();

	private static final Map<Class<? extends UIStore>, UIStore> GLOBAL_CACHE = new ConcurrentHashMap<>();

	public static <T extends UIStore> @NonNull T useStore(final @NonNull Class<T> clazz, final @NonNull Supplier<T> factory) {
		if (UIStoreHook.GLOBAL_CACHE.containsKey(clazz)) {
			return (T) UIStoreHook.GLOBAL_CACHE.get(clazz);
		}

		final UIStoreData data = UIStoreHook.getData(clazz);
		T store = factory.get();
		store.init();

		if (data.scope() == UIStoreScope.PERMANENT) {
			final File file = UIStoreHook.getFile(store);
			final JsonObject json = JsonFiles.read(file, "the store starts from init()");
			if (json != null) {
				try {
					store.fromJson(UIStoreHook.GSON.fromJson(json, Map.class));
				} catch (final RuntimeException exception) {
					JOIDLog.warnOnce("UIStore.fromJson." + clazz.getName() + "." + exception.getClass().getName(), "The store " + clazz.getName() + " cannot be restored from " + file.getAbsolutePath() + " (" + exception + "), it starts from init(): catch the exception inside fromJson() to handle it", exception);
					JsonFiles.keep(file, exception.toString(), "the store starts from init()");
					store = factory.get();
					store.init();
				}
			}
		}

		if (data.scope().isGlobal()) {
			UIStoreHook.GLOBAL_CACHE.put(clazz, store);
		}

		return store;
	}

	public static void destroyStore(final @NonNull UIStore store) {
		final Class<? extends UIStore> clazz = store.getClass();
		final UIStoreData data = store.getData();
		if (data.scope().isGlobal()) {
			UIStoreHook.GLOBAL_CACHE.remove(clazz);
		}

		try {
			store.destroy();
		} catch (final RuntimeException exception) {
			JOIDLog.warnOnce("UIStore.destroy." + clazz.getName() + "." + exception.getClass().getName(), "The destroy of the store " + clazz.getName() + " failed (" + exception + "), it is destroyed anyway: catch the exception inside destroy() to handle it", exception);
		}
	}

	public static void saveStore(final @NonNull UIStore store) {
		if (store.getData().scope() != UIStoreScope.PERMANENT) {
			return;
		}

		JsonFiles.write(UIStoreHook.getFile(store), UIStoreHook.GSON.toJson(store.toJson()));
	}

	public static void saveAll() {
		for (final UIStore store : UIStoreHook.GLOBAL_CACHE.values()) {
			try {
				UIStoreHook.saveStore(store);
			} catch (final RuntimeException exception) {
				JOIDLog.warnOnce("UIStore.toJson." + store.getClass().getName() + "." + exception.getClass().getName(), "The store " + store.getClass().getName() + " cannot be saved (" + exception + "), the other stores are saved anyway: catch the exception inside toJson() to handle it", exception);
			}
		}
	}

	public static @NonNull UIStoreData getData(final @NonNull Class<? extends UIStore> clazz) {
		final UIStoreData data = clazz.getAnnotation(UIStoreData.class);
		if (data == null) {
			throw new IllegalStateException("The store " + clazz.getName() + " has no @UIStoreData annotation, add it to the class");
		}
		return data;
	}

	private static String getId(final UIStore store) {
		final String id = store.getData().id();
		return id.isEmpty() ? store.getClass().getName() : id;
	}

	private static File getFile(final UIStore store) {
		return new File(new File(JOIDEnv.getDataDir(), "store"), UIStoreHook.getId(store) + ".store");
	}

}