package dev.joid.lib.ui.core.hook.property;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import dev.joid.internal.JOID;
import dev.joid.lib.ui.core.UI;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UIPropertyHook {

	private static final Gson GSON = new GsonBuilder().create();

	private static final Map<String, List<Field>> PROPERTY_MAP = new HashMap<>();

	public static void load(final @NonNull UI ui) {
		final List<Field> fields = UIPropertyHook.getFields(ui);
		if (fields.isEmpty()) {
			return;
		}

		final Optional<JsonObject> json = UIPropertyHook.loadFile(ui);
		if (!json.isPresent()) {
			return;
		}

		for (final Field field : fields) {
			final String key = UIPropertyHook.getKey(field);
			if (!json.get().has(key)) {
				continue;
			}

			try {
				field.set(ui, UIPropertyHook.GSON.fromJson(json.get().get(key), field.getGenericType()));
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void save(final @NonNull UI ui) {
		final List<Field> fields = UIPropertyHook.getFields(ui);
		if (fields.isEmpty()) {
			return;
		}

		final JsonObject json = UIPropertyHook.loadFile(ui).orElseGet(JsonObject::new);
		for (final Field field : fields) {
			final String key = UIPropertyHook.getKey(field);
			try {
				final Object value = field.get(ui);
				json.remove(key);
				if (value == null) {
					continue;
				}

				json.add(key, UIPropertyHook.GSON.toJsonTree(value, field.getGenericType()));
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

		UIPropertyHook.saveFile(ui, json);
	}

	private static @NonNull File getFile(final @NonNull UI ui) {
		return new File(new File(JOID.inst().getConfigDir(), "property"), ui.getClass().getName() + ".property");
	}

	private static @NonNull String getKey(final @NonNull Field field) {
		final UIProperty property = field.getAnnotation(UIProperty.class);
		return property.value().isEmpty() ? field.getName() : property.value();
	}

	private static @NonNull List<Field> getFields(final @NonNull UI ui) {
		final String className = ui.getClass().getName();
		if (!JOID.inst().isDevMode() && UIPropertyHook.PROPERTY_MAP.containsKey(className)) {
			return UIPropertyHook.PROPERTY_MAP.get(className);
		}

		Class<?> clazz = ui.getClass();
		final List<Field> fields = new ArrayList<>();
		while (clazz != null) {
			if (clazz.equals(Object.class)) {
				break;
			}

			for (final Field field : clazz.getDeclaredFields()) {
				if (!field.isAnnotationPresent(UIProperty.class) || Modifier.isFinal(field.getModifiers())) {
					continue;
				}

				field.setAccessible(true);
				fields.add(field);
			}

			clazz = clazz.getSuperclass();
		}

		UIPropertyHook.PROPERTY_MAP.put(className, fields);
		return fields;
	}

	private static @NonNull Optional<JsonObject> loadFile(final @NonNull UI ui) {
		final File file = UIPropertyHook.getFile(ui);
		if (!file.exists()) {
			return Optional.empty();
		}

		try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
			return Optional.ofNullable(UIPropertyHook.GSON.fromJson(reader, JsonObject.class));
		} catch (final Exception e) {
			System.err.println("Failed to load property file: " + file.getAbsolutePath());
			e.printStackTrace();
		}

		if (!file.delete()) {
			System.err.println("Failed to delete corrupted property file: " + file.getAbsolutePath());
		}
		return Optional.empty();
	}

	private static void saveFile(final @NonNull UI ui, final @NonNull JsonObject json) {
		final File file = UIPropertyHook.getFile(ui);
		final File parent = file.getParentFile();
		if (!parent.exists() && !parent.mkdirs()) {
			System.err.println("Failed to create property directory: " + parent.getAbsolutePath());
			return;
		}

		try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
			UIPropertyHook.GSON.toJson(json, writer);
		} catch (final Exception e) {
			System.err.println("Failed to save property file: " + file.getAbsolutePath());
			e.printStackTrace();
		}
	}

}