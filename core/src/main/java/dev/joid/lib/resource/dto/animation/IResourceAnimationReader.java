package dev.joid.lib.resource.dto.animation;

import java.io.IOException;
import java.io.InputStream;

import lombok.NonNull;

public interface IResourceAnimationReader {

	public @NonNull ResourceAnimation read(final @NonNull InputStream stream) throws IOException;

}