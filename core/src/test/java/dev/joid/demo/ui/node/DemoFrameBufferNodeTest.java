package dev.joid.demo.ui.node;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingFrameBuffer;
import dev.joid.lib.render.framebuffer.FrameBuffer;

public class DemoFrameBufferNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void keepsItsFrameBufferWhenACopyIsDetached() throws ReflectiveOperationException {
		final DemoFrameBufferNode original = DemoFrameBufferNode.create(0D, 0D, 40D, 25D);
		this.bridges.getRender().beginFrame();
		original.draw(0D, 0D);
		original.<DemoFrameBufferNode>copy().detach();
		final Field field = DemoFrameBufferNode.class.getDeclaredField("frameBuffer");
		field.setAccessible(true);
		Assert.assertFalse(((RecordingFrameBuffer) ((FrameBuffer) field.get(original)).getHandle()).isDeleted());
	}

}