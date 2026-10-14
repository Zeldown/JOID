package dev.joid.lib.ui.core.hook.store;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import dev.joid.internal.JOIDEnv;
import dev.joid.internal.JOIDLog;
import dev.joid.lib.ui.core.hook.store.data.UIStoreData;
import dev.joid.lib.ui.core.hook.store.scope.StoreScope;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("unchecked")
public final class UIStoreHook {

	private static final Gson GSON = new GsonBuilder().create();
	private static final Map<Class<? extends UIStore>, UIStore> GLOBAL_CACHE = new HashMap<>();

	public static <T extends UIStore> @NonNull T useStore(final @NonNull Class<T> clazz, final Object... args) {
		if (UIStoreHook.GLOBAL_CACHE.containsKey(clazz)) {
			return (T) UIStoreHook.GLOBAL_CACHE.get(clazz);
		}

		final T store = UIStoreHook.createStoreInstance(clazz, args);
		final File file = UIStoreHook.getFile(UIStoreHook.getId(store));
		if (store.getData().scope() == StoreScope.PERMANENT && file.exists()) {
			final String json = UIStoreHook.loadFile(file);
			if (json == null) {
				store.init();
			} else {
				store.fromJson(json);
			}
		} else {
			store.init();
		}

		if (store.getData().scope().isGlobal()) {
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

		if (store.getData().scope() == StoreScope.PERMANENT) {
			UIStoreHook.deleteFile(UIStoreHook.getId(store));
		}

		store.destroy();
	}

	public static void saveStore(final @NonNull UIStore store) {
		if (store.getData().scope() != StoreScope.PERMANENT) {
			return;
		}

		UIStoreHook.saveFile(UIStoreHook.getId(store), store.toJson());
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
		} catch (final InvocationTargetException e) {
			throw new IllegalStateException("The constructor of the store " + clazz.getName() + " failed: " + e.getCause(), e.getCause());
		} catch (final ReflectiveOperationException e) {
			throw new IllegalArgumentException("The store " + clazz.getName() + " cannot be created (" + e + "), make it a public class that is not abstract", e);
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
		final File parent = new File(JOIDEnv.getDataDir(), "store");
		final File file = new File(parent, id + ".store");
		if (file.exists()) {
			file.delete();
		}
	}

	private static @NonNull File getFile(final @NonNull String id) {
		final File parent = new File(JOIDEnv.getDataDir(), "store");
		return new File(parent, id + ".store");
	}

	private static String loadFile(final @NonNull File file) {
		final String json;
		try {
			json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
		} catch (final IOException e) {
			JOIDLog.warn("The store file " + file.getAbsolutePath() + " cannot be read (" + e + "), it is left untouched and the store starts from init()");
			return null;
		}

		String failure = "it holds no JSON object";
		try {
			if (UIStoreHook.GSON.fromJson(json, JsonObject.class) != null) {
				return json;
			}
		} catch (final JsonParseException e) {
			failure = e.toString();
		}

		final File corrupt = new File(file.getPath() + ".corrupt");
		try {
			Files.move(file.toPath(), corrupt.toPath(), StandardCopyOption.REPLACE_EXISTING);
			JOIDLog.warn("The store file " + file.getAbsolutePath() + " cannot be read (" + failure + "), it is kept as " + corrupt.getAbsolutePath() + " and the store starts from init()");
		} catch (final IOException e) {
			JOIDLog.warn("The store file " + file.getAbsolutePath() + " cannot be read (" + failure + ") nor renamed to " + corrupt.getAbsolutePath() + " (" + e + "), it is left untouched and the store starts from init()");
		}
		return null;
	}

	private static void saveFile(final @NonNull String id, final @NonNull String json) {
		try {
			final File file = UIStoreHook.getFile(id);
			if (!file.exists()) {
				final File parent = file.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}

				if (!file.createNewFile()) {
					JOIDLog.warn("The store file " + file.getAbsolutePath() + " cannot be created, check that the data folder can be written");
				}
			}

			try (final Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
				writer.write(json);
			}
		} catch (final Exception e) {
			JOIDLog.warn("The store " + id + " cannot be saved into " + UIStoreHook.getFile(id).getAbsolutePath() + " (" + e + "), check that the data folder can be written");
		}
	}

}