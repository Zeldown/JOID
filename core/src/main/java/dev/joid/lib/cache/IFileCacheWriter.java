package dev.joid.lib.cache;

import java.io.File;
import java.io.IOException;

import lombok.NonNull;

@FunctionalInterface
public interface IFileCacheWriter {

	public void write(final @NonNull File file) throws IOException;

}