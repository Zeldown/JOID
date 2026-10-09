package dev.joid.test.contract;

import java.util.Map;

import dev.joid.test.snapshot.ISnapshotBackend;
import lombok.NonNull;

public interface IStateGuardBackend extends ISnapshotBackend {

	public void drawExternal();
	public void fill(final int x, final int y, final int width, final int height, final int color);
	public void inject(final @NonNull StateTrap trap);
	public boolean supports(final @NonNull StateTrap trap);

	public @NonNull Map<@NonNull String, @NonNull String> readState();

}