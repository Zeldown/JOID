package dev.joid.lib.utils.json;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
		} catch (final IOException e) {
			JOIDLog.warn("The file " + file.getAbsolutePath() + " cannot be read (" + e + "), it is left untouched and " + consequence);
			return null;
		}

		String failure = "it holds no JSON object";
		try {
			final JsonObject object = JsonFiles.GSON.fromJson(json, JsonObject.class);
			if (object != null) {
				return object;
			}
		} catch (final JsonParseException e) {
			failure = e.toString();
		}

		final File corrupt = new File(file.getPath() + ".corrupt");
		try {
			Files.move(file.toPath(), corrupt.toPath(), StandardCopyOption.REPLACE_EXISTING);
			JOIDLog.warn("The file " + file.getAbsolutePath() + " cannot be read (" + failure + "), it is kept as " + corrupt.getAbsolutePath() + " and " + consequence);
		} catch (final IOException e) {
			JOIDLog.warn("The file " + file.getAbsolutePath() + " cannot be read (" + failure + ") nor renamed to " + corrupt.getAbsolutePath() + " (" + e + "), it is left untouched and " + consequence);
		}
		return null;
	}

	public static void write(final @NonNull File file, final @NonNull String json) {
		final File temporary = new File(file.getPath() + ".tmp");
		try {
			Files.createDirectories(file.getAbsoluteFile().getParentFile().toPath());
			Files.write(temporary.toPath(), json.getBytes(StandardCharsets.UTF_8));
			Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE);
		} catch (final IOException e) {
			temporary.delete();
			JOIDLog.warn("The file " + file.getAbsolutePath() + " cannot be saved (" + e + "), check that the data folder can be written");
		}
	}

}