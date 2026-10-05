package be.zeldown.joid.lib.asset.dto.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import be.zeldown.joid.lib.asset.Asset;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class FileAsset extends Asset {

	private final File file;

	private FileAsset(final @NonNull File file) {
		super(file.getAbsolutePath());
		this.file = file;
	}

	public static @NonNull FileAsset create(final @NonNull File file) {
		return new FileAsset(file);
	}

	@Override
	public @NonNull InputStream open() throws IOException {
		return new FileInputStream(this.file);
	}

}