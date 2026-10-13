package dev.joid.lib.video;

import lombok.NonNull;

public final class VideoNativesException extends RuntimeException {

	public VideoNativesException(final @NonNull String message) {
		super(message);
	}

	public VideoNativesException(final @NonNull String message, final @NonNull Throwable cause) {
		super(message, cause);
	}

}