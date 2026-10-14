package dev.joid.base.openal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import dev.joid.base.openal.binding.IAlBinding;
import dev.joid.lib.bridge.audio.IAudioBridge;
import dev.joid.lib.bridge.audio.IAudioSource;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class AlAudioBridge implements IAudioBridge {

	private final IAlBinding         binding;
	private final Set<AlAudioSource> sourceSet;
	private final Map<Object, Float> groupVolumeMap;

	private IAudioGain gain;
	private boolean    ownContext;

	private AlAudioBridge(final IAlBinding binding) {
		this.binding        = binding;
		this.sourceSet      = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
		this.groupVolumeMap = new HashMap<>();
		this.gain           = (gain, group) -> gain * this.getGroupVolume(group);
		this.ownContext     = true;
	}

	public static @NonNull AlAudioBridge create(final @NonNull IAlBinding binding) {
		return new AlAudioBridge(binding);
	}

	public @NonNull AlAudioBridge gain(final @NonNull IAudioGain gain) {
		this.gain = gain;
		this.syncSources();
		return this;
	}

	public @NonNull AlAudioBridge groupVolume(final Object group, final float volume) {
		this.groupVolumeMap.put(group, volume);
		this.syncSources();
		return this;
	}

	public @NonNull AlAudioBridge ownContext(final boolean ownContext) {
		this.ownContext = ownContext;
		return this;
	}

	@Override
	public @NonNull IAudioSource createSource(final int sampleRate, final int channels) {
		if (this.ownContext && this.binding.getCurrentContext() == null) {
			this.binding.createContext();
			Runtime.getRuntime().addShutdownHook(new Thread(this.binding::destroyContext, "joid-al-shutdown"));
		}

		final AlAudioSource source = AlAudioSource.create(this, sampleRate, channels);
		this.sourceSet.add(source);
		return source;
	}

	public void releaseSource(final @NonNull AlAudioSource source) {
		this.sourceSet.remove(source);
	}

	public float getGroupVolume(final Object group) {
		return this.groupVolumeMap.getOrDefault(group, 1F);
	}

	public @NonNull Set<@NonNull AlAudioSource> getSourceSet() {
		return Collections.unmodifiableSet(this.sourceSet);
	}

	public @NonNull Map<Object, @NonNull Float> getGroupVolumeMap() {
		return Collections.unmodifiableMap(this.groupVolumeMap);
	}

	private void syncSources() {
		final List<AlAudioSource> sourceList;
		synchronized (this.sourceSet) {
			sourceList = new ArrayList<>(this.sourceSet);
		}
		sourceList.forEach(AlAudioSource::sync);
	}

}