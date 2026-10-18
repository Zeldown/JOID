package dev.joid.backend.lwjgl3.snapshot;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlBinding;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProfileGlBinding {

	public static @NonNull IGlBinding from(final @NonNull IGlBinding binding, final @NonNull SnapshotProfile profile) {
		return (IGlBinding) Proxy.newProxyInstance(IGlBinding.class.getClassLoader(), new Class<?>[] {IGlBinding.class}, (proxy, method, args) -> {
			final Object name = args == null ? null : args[0];
			if ("getInteger".equals(method.getName()) && Integer.valueOf(GlConstants.NUM_EXTENSIONS).equals(name)) {
				return ProfileGlBinding.getExtensions(binding, profile).size();
			}
			if ("getString".equals(method.getName()) && Integer.valueOf(GlConstants.EXTENSIONS).equals(name)) {
				return args.length == 2 ? ProfileGlBinding.getExtensions(binding, profile).get((int) args[1]) : String.join(" ", ProfileGlBinding.getExtensions(binding, profile));
			}
			if ("getString".equals(method.getName()) && Integer.valueOf(GlConstants.SHADING_LANGUAGE_VERSION).equals(name) && profile.getShadingLanguageVersion() != null) {
				return profile.getShadingLanguageVersion();
			}

			try {
				return method.invoke(binding, args);
			} catch (final InvocationTargetException exception) {
				throw exception.getCause();
			}
		});
	}

	private static List<String> getExtensions(final IGlBinding binding, final SnapshotProfile profile) {
		final List<String> extensionList = new ArrayList<>();
		if (binding.getString(GlConstants.VERSION).matches("^[012]\\..*")) {
			extensionList.addAll(Arrays.asList(binding.getString(GlConstants.EXTENSIONS).trim().split("\\s+")));
		} else {
			for (int i = 0; i < binding.getInteger(GlConstants.NUM_EXTENSIONS); i++) {
				extensionList.add(binding.getString(GlConstants.EXTENSIONS, i));
			}
		}
		extensionList.removeAll(profile.getHiddenExtensionList());
		return extensionList;
	}

}