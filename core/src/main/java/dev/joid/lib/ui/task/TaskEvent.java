package dev.joid.lib.ui.task;

import dev.joid.lib.ui.event.Event;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TaskEvent extends Event {

	private final Task task;

	public static @NonNull TaskEvent create(final @NonNull Task task) {
		return new TaskEvent(task);
	}

}