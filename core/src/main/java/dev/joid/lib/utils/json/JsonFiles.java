package dev.joid.lib.utils.json;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import dev.joid.internal.JOIDLog;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonFiles {

	private static final Gson GSON = new GsonBuilder().create();

	public static JsonObject read(final @NonNull File file, final @NonNull String consequence) {
		if (!file.exists()) {
			return null;
		}

		final String json;
		try {
			json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
		} catch (final IOException exception) {
			JOIDLog.warn("The file " + file.getAbsolutePath() + " cannot be read (" + exception + "), it is left untouched and " + consequence);
			return null;
		}

		String failure = "it holds no JSON object";
		try {
			final JsonObject object = JsonFiles.GSON.fromJson(json, JsonObject.class);
			if (object != null) {
				return object;
			}
		} catch (final JsonParseException exception) {
			failure = exception.toString();
		}

		JsonFiles.keep(file, failure, consequence);
		return null;
	}

	public static void keep(final @NonNull File file, final @NonNull String failure, final @NonNull String consequence) {
		final File corrupt = new File(file.getPath() + ".corrupt");
		try {
			Files.move(file.toPath(), corrupt.toPath(), StandardCopyOption.REPLACE_EXISTING);
			JOIDLog.warn("The file " + file.getAbsolutePath() + " cannot be read (" + failure + "), it is kept as " + corrupt.getAbsolutePath() + " and " + consequence);
		} catch (final IOException exception) {
			JOIDLog.warn("The file " + file.getAbsolutePath() + " cannot be read (" + failure + ") nor renamed to " + corrupt.getAbsolutePath() + " (" + exception + "), it is left untouched and " + consequence);
		}
	}

	public static void write(final @NonNull File file, final @NonNull String json) {
		Path temporary = null;
		try {
			final Path directory = Files.createDirectories(file.getAbsoluteFile().getParentFile().toPath());
			temporary = Files.createTempFile(directory, file.getName(), ".tmp");
			Files.write(temporary, json.getBytes(StandardCharsets.UTF_8));
			Files.move(temporary, file.toPath(), StandardCopyOption.ATOMIC_MOVE);
		} catch (final IOException exception) {
			if (temporary != null) {
				temporary.toFile().delete();
			}
			JOIDLog.warn("The file " + file.getAbsolutePath() + " cannot be saved (" + exception + "), check that the data folder can be written");
		}
	}

}