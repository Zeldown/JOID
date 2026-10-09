package dev.joid.test.contract;

import java.util.Map;
import java.util.function.Supplier;

import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.test.snapshot.ISnapshotBackend;
import lombok.NonNull;

public interface IBorrowedTextureBackend extends ISnapshotBackend {

	public @NonNull ITexture borrow(final @NonNull Supplier<Object> texture);

	public boolean isHostTexture(final @NonNull Object texture);
	public @NonNull Object createHostTexture(final int width, final int height, final int color, final boolean mipmapped);

	public @NonNull Map<@NonNull String, @NonNull String> readHostParameters(final @NonNull Object texture);

}