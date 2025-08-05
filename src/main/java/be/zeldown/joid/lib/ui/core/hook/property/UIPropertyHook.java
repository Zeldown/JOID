package be.zeldown.joid.lib.ui.core.hook.property;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.ui.core.UI;
import lombok.NonNull;

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
			final UIProperty property = field.getAnnotation(UIProperty.class);
			final String key = property.value().isEmpty() ? field.getName() : property.value();
			if (!json.has(key)) {
				continue;
			}

			try {
				final Object value = UIPropertyHook.GSON.fromJson(json.get(key), field.getType());
				field.set(ui, value);
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

		final JsonObject json = UIPropertyHook.loadFile(ui);
		if (json == null) {
			return;
		}

		for (final Field field : fields) {
			final UIProperty property = field.getAnnotation(UIProperty.class);
			final String key = property.value().isEmpty() ? field.getName() : property.value();
			try {
				final Object value = field.get(ui);
				json.remove(key);
				if (value == null) {
					continue;
				}

				json.addProperty(key, UIPropertyHook.GSON.toJson(value));
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

		UIPropertyHook.saveFile(ui, json);
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
				if (!field.isAnnotationPresent(UIProperty.class) || java.lang.reflect.Modifier.isFinal(field.getModifiers())) {
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

	private static @NonNull JsonObject loadFile(final @NonNull UI ui) {
		final File parent = new File(JOID.inst().getConfigDir(), "property");
		final File file = new File(parent, ui.getClass().getName() + ".dat");
		try {
			if (!parent.exists()) {
				parent.mkdirs();
			}

			if (!file.exists()) {
				return null;
			}

			final FileReader reader = new FileReader(file);
			final JsonObject object = UIPropertyHook.GSON.fromJson(reader, JsonObject.class);
			reader.close();
			return object;
		} catch (final Exception e) {
			System.err.println("Failed to load property file: " + file.getAbsolutePath());
			e.printStackTrace();
			file.delete();
		}

		return null;
	}

	private static void saveFile(final @NonNull UI ui, final @NonNull JsonObject json) {
		final File parent = new File(JOID.inst().getConfigDir(), "property");
		final File file = new File(parent, ui.getClass().getName() + ".zui");
		try {
			if (!file.exists()) {
				if (!parent.exists()) {
					parent.mkdirs();
				}

				if (!file.createNewFile()) {
					System.err.println("Failed to create property file: " + file.getAbsolutePath());
				}
			}

			final FileWriter writer = new FileWriter(file);
			UIPropertyHook.GSON.toJson(json, writer);
			writer.close();
		} catch (final Exception e) {
			System.err.println("Failed to save property file: " + file.getAbsolutePath());
			e.printStackTrace();
		}
	}

}