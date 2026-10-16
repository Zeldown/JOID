package dev.joid.lib.ui.core.hook.store;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.google.gson.JsonObject;

import dev.joid.internal.JOIDEnv;
import dev.joid.lib.ui.core.hook.store.data.UIStoreData;
import dev.joid.lib.ui.core.hook.store.scope.StoreScope;
import dev.joid.lib.utils.json.JsonFiles;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UIStoreHook {

	private static final Map<Class<? extends UIStore>, UIStore> GLOBAL_CACHE = new ConcurrentHashMap<>();

	public static <T extends UIStore> @NonNull T useStore(final @NonNull Class<T> clazz, final @NonNull Supplier<T> factory) {
		if (UIStoreHook.GLOBAL_CACHE.containsKey(clazz)) {
			return (T) UIStoreHook.GLOBAL_CACHE.get(clazz);
		}

		final UIStoreData data = UIStoreHook.getData(clazz);
		final T store = factory.get();
		store.init();

		if (data.scope() == StoreScope.PERMANENT) {
			final JsonObject json = JsonFiles.read(UIStoreHook.getFile(store), "the store starts from init()");
			if (json != null) {
				store.fromJson(json.toString());
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

		store.destroy();
	}

	public static void saveStore(final @NonNull UIStore store) {
		if (store.getData().scope() != StoreScope.PERMANENT) {
			return;
		}

		JsonFiles.write(UIStoreHook.getFile(store), store.toJson());
	}

	public static void saveAll() {
		for (final UIStore store : UIStoreHook.GLOBAL_CACHE.values()) {
			UIStoreHook.saveStore(store);
		}
	}

	public static @NonNull UIStoreData getData(final @NonNull Class<? extends UIStore> clazz) {
		final UIStoreData data = clazz.getAnnotation(UIStoreData.class);
		if (data == null) {
			throw new IllegalStateException("The store " + clazz.getName() + " has no @UIStoreData annotation, add it to the class");
		}
		return data;
	}

	private static @NonNull String getId(final @NonNull UIStore store) {
		final String id = store.getData().id();
		return id.isEmpty() ? store.getClass().getName() : id;
	}

	private static @NonNull File getFile(final @NonNull UIStore store) {
		return new File(new File(JOIDEnv.getDataDir(), "store"), UIStoreHook.getId(store) + ".store");
	}

}