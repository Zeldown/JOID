package dev.joid.lib.ui.core.hook.property;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import dev.joid.internal.JOIDEnv;
import dev.joid.internal.JOIDLog;
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

		final JsonObject json = UIPropertyHook.loadFile(ui);
		if (json == null) {
			return;
		}

		for (final Field field : fields) {
			final String key = UIPropertyHook.getKey(field);
			if (!json.has(key)) {
				continue;
			}

			try {
				field.set(ui, UIPropertyHook.GSON.fromJson(json.get(key), field.getGenericType()));
			} catch (final Exception e) {
				JOIDLog.warn("The property " + key + " of " + ui.getClass().getSimpleName() + " cannot be read into its field " + field.getName() + " (" + e + "), the field keeps its default value");
			}
		}
	}

	public static void save(final @NonNull UI ui) {
		final List<Field> fields = UIPropertyHook.getFields(ui);
		if (fields.isEmpty()) {
			return;
		}

		final JsonObject loaded = UIPropertyHook.loadFile(ui);
		final JsonObject json = loaded != null ? loaded : new JsonObject();
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
				JOIDLog.warn("The field " + field.getName() + " of " + ui.getClass().getSimpleName() + " cannot be saved as the property " + key + " (" + e + "), the property is not saved");
			}
		}

		UIPropertyHook.saveFile(ui, json);
	}

	private static @NonNull File getFile(final @NonNull UI ui) {
		return new File(new File(JOIDEnv.getDataDir(), "property"), ui.getClass().getName() + ".property");
	}

	private static @NonNull String getKey(final @NonNull Field field) {
		final UIProperty property = field.getAnnotation(UIProperty.class);
		return property.value().isEmpty() ? field.getName() : property.value();
	}

	private static @NonNull List<Field> getFields(final @NonNull UI ui) {
		final String className = ui.getClass().getName();
		if (!JOIDEnv.isDevMode() && UIPropertyHook.PROPERTY_MAP.containsKey(className)) {
			return UIPropertyHook.PROPERTY_MAP.get(className);
		}

		Class<?> clazz = ui.getClass();
		final List<Field> fields = new ArrayList<>();
		while (clazz != null) {
			if (clazz.equals(Object.class)) {
				break;
			}

			for (final Field field : clazz.getDeclaredFields()) {
				if (!field.isAnnotationPresent(UIProperty.class) || Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
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

	private static JsonObject loadFile(final @NonNull UI ui) {
		final File file = UIPropertyHook.getFile(ui);
		if (!file.exists()) {
			return null;
		}

		try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
			return UIPropertyHook.GSON.fromJson(reader, JsonObject.class);
		} catch (final JsonIOException | IOException e) {
			JOIDLog.warn("The property file " + file.getAbsolutePath() + " cannot be read (" + e + "), it is left untouched and the properties of " + ui.getClass().getSimpleName() + " keep their default values");
			return null;
		} catch (final JsonParseException e) {
			final File corrupt = new File(file.getPath() + ".corrupt");
			try {
				Files.move(file.toPath(), corrupt.toPath(), StandardCopyOption.REPLACE_EXISTING);
				JOIDLog.warn("The property file " + file.getAbsolutePath() + " cannot be read (" + e + "), it is kept as " + corrupt.getAbsolutePath() + " and the properties of " + ui.getClass().getSimpleName() + " keep their default values");
			} catch (final IOException move) {
				JOIDLog.warn("The property file " + file.getAbsolutePath() + " cannot be read (" + e + ") nor renamed to " + corrupt.getAbsolutePath() + " (" + move + "), it is left untouched and the properties of " + ui.getClass().getSimpleName() + " keep their default values");
			}
			return null;
		}
	}

	private static void saveFile(final @NonNull UI ui, final @NonNull JsonObject json) {
		final File file = UIPropertyHook.getFile(ui);
		final File parent = file.getParentFile();
		if (!parent.exists() && !parent.mkdirs()) {
			JOIDLog.warn("The property folder " + parent.getAbsolutePath() + " cannot be created, the properties of " + ui.getClass().getSimpleName() + " are not saved: check that the data folder can be written");
			return;
		}

		try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
			UIPropertyHook.GSON.toJson(json, writer);
		} catch (final Exception e) {
			JOIDLog.warn("The property file " + file.getAbsolutePath() + " cannot be saved (" + e + "), check that the data folder can be written");
		}
	}

}