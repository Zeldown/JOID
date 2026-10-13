package dev.joid.lib.ui.task;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskEvent {

	@NonNull
	private final Task task;

}