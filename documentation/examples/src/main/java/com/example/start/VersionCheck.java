package com.example.start;

import dev.joid.internal.JOID;

public final class VersionCheck {

	private VersionCheck() {}

	public static void main(final String[] arguments) {
		System.out.println("JOID " + JOID.VERSION);
	}

}