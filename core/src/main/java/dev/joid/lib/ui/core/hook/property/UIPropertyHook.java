package dev.joid.lib.ui.core.hook.property;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import dev.joid.internal.JOIDLog;
import dev.joid.internal.JsonFiles;
import dev.joid.lib.JOIDEnv;
import dev.joid.lib.ui.core.UI;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UIPropertyHook {

	private static final Gson GSON = new GsonBuilder().create();

	private static final Map<String, List<Field>> PROPERTY_MAP = new ConcurrentHashMap<>();

	public static void load(final @NonNull UI ui) {
		final List<Field> fields = UIPropertyHook.getFields(ui);
		if (fields.isEmpty()) {
			return;
		}

		final JsonObject json = UIPropertyHook.read(ui);
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
			} catch (final Exception exception) {
				JOIDLog.warn("The property " + key + " of " + JOIDLog.nameOf(ui.getClass()) + " cannot be read into its field " + field.getName() + " (" + exception + "), the field keeps its default value");
			}
		}
	}

	public static void save(final @NonNull UI ui) {
		final List<Field> fields = UIPropertyHook.getFields(ui);
		if (fields.isEmpty()) {
			return;
		}

		final JsonObject loaded = UIPropertyHook.read(ui);
		final JsonObject json = loaded != null ? loaded : new JsonObject();
		for (final Field field : fields) {
			final String key = UIPropertyHook.getKey(field);
			try {
				final Object value = field.get(ui);
				if (value == null) {
					json.remove(key);
				} else {
					json.add(key, UIPropertyHook.GSON.toJsonTree(value, field.getGenericType()));
				}
			} catch (final Exception | StackOverflowError exception) {
				JOIDLog.warn("The field " + field.getName() + " of " + JOIDLog.nameOf(ui.getClass()) + " cannot be saved as the property " + key + " (" + exception + "), its previous value is kept");
			}
		}

		JsonFiles.write(UIPropertyHook.getFile(ui), UIPropertyHook.GSON.toJson(json));
	}

	private static File getFile(final UI ui) {
		return new File(new File(JOIDEnv.getDataDir(), "property"), ui.getClass().getName() + ".property");
	}

	private static String getKey(final Field field) {
		final UIProperty property = field.getAnnotation(UIProperty.class);
		return property.value().isEmpty() ? field.getName() : property.value();
	}

	private static List<Field> getFields(final UI ui) {
		final String className = ui.getClass().getName();
		if (!JOIDEnv.isDevMode() && UIPropertyHook.PROPERTY_MAP.containsKey(className)) {
			return UIPropertyHook.PROPERTY_MAP.get(className);
		}

		final List<Field> fields = new ArrayList<>();
		for (Class<?> clazz = ui.getClass(); clazz != UI.class; clazz = clazz.getSuperclass()) {
			for (final Field field : clazz.getDeclaredFields()) {
				if (!field.isAnnotationPresent(UIProperty.class) || Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
					continue;
				}

				field.setAccessible(true);
				fields.add(field);
			}
		}

		UIPropertyHook.PROPERTY_MAP.put(className, fields);
		return fields;
	}

	private static JsonObject read(final UI ui) {
		return JsonFiles.read(UIPropertyHook.getFile(ui), "the properties of " + JOIDLog.nameOf(ui.getClass()) + " keep their default values");
	}

}