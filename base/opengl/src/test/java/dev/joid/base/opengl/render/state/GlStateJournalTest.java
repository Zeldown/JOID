package dev.joid.base.opengl.render.state;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.binding.IGlFrameBufferBinding;
import dev.joid.base.opengl.capability.GlCapabilities;

public class GlStateJournalTest {

	private final List<String> calls = new ArrayList<>();

	@Test
	public void leavesAClientMemoryAttributeOfTheHostAsItIs() {
		final GlStateJournal journal = this.journal(false);
		journal.start();
		journal.saveVertexAttribute(0);
		journal.restore();
		Assert.assertFalse(this.calls.toString(), this.calls.stream().anyMatch(call -> call.startsWith("vertexAttribPointer")));
		Assert.assertTrue(this.calls.toString(), this.calls.contains("enableVertexAttribArray 0"));
	}

	@Test
	public void restoresAClientMemoryAttributeWhereTheBindingCanPointAtIt() {
		final GlStateJournal journal = this.journal(true);
		journal.start();
		journal.saveVertexAttribute(0);
		journal.restore();
		Assert.assertTrue(this.calls.toString(), this.calls.contains("vertexAttribPointer 0 1234"));
	}

	private GlStateJournal journal(final boolean clientMemory) {
		final IGlBinding binding = GlStateJournalTest.fake(IGlBinding.class, (method, args) -> {
			switch (method.getName()) {
			case "getString":
				return args.length > 1 ? null : args[0].equals(GlConstants.VERSION) ? "2.1" : args[0].equals(GlConstants.SHADING_LANGUAGE_VERSION) ? "1.20" : null;
			case "getVertexAttribi":
				return args[1].equals(GlConstants.VERTEX_ATTRIB_ARRAY_ENABLED) ? 1 : args[1].equals(GlConstants.VERTEX_ATTRIB_ARRAY_BUFFER_BINDING) ? 0 : 4;
			case "getVertexAttribPointer":
				return 1234L;
			case "getFrameBufferBinding":
				return GlStateJournalTest.fake(IGlFrameBufferBinding.class, (call, values) -> null);
			case "canPointClientMemory":
				return clientMemory;
			case "bindBuffer":
			case "bufferData":
			case "drawArrays":
			case "vertexAttrib2f":
			case "vertexAttrib3f":
			case "vertexAttrib4f":
			case "bindVertexArray":
			case "vertexAttribPointer":
			case "enableVertexAttribArray":
			case "disableVertexAttribArray":
				this.calls.add(method.getName() + " " + args[0] + (method.getName().equals("vertexAttribPointer") ? " " + args[5] : ""));
				return null;
			default:
				return null;
			}
		});
		return GlStateJournal.create(binding, GlCapabilities.read(binding));
	}

	private static <T> T fake(final Class<T> type, final Answer answer) {
		return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, (proxy, method, args) -> {
			final Object value = answer.answer(method, args == null ? new Object[0] : args);
			if (value != null || !method.getReturnType().isPrimitive()) {
				return value;
			}
			return method.getReturnType() == boolean.class ? (Object) false : method.getReturnType() == long.class ? (Object) 0L : method.getReturnType() == void.class ? null : (Object) 0;
		});
	}

	@FunctionalInterface
	private interface Answer {

		public Object answer(final Method method, final Object[] args);

	}

}