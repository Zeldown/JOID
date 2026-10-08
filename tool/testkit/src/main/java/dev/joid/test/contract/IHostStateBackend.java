package dev.joid.test.contract;

import java.util.Map;

import dev.joid.test.snapshot.ISnapshotBackend;
import lombok.NonNull;

public interface IHostStateBackend extends ISnapshotBackend {

	public void drawHost();
	public void inject(final @NonNull HostTrap trap);
	public boolean supports(final @NonNull HostTrap trap);

	public @NonNull Map<@NonNull String, @NonNull String> readState();

}