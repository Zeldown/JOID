package be.zeldown.joid.lib.resource.dto.animation;

import java.io.IOException;
import java.io.InputStream;

import lombok.NonNull;

public interface IAnimationReader {

	public @NonNull Animation read(final @NonNull InputStream stream) throws IOException;

}