package com.example.backend.engine;

public interface Engine {

	public static Engine createHidden(final int width, final int height) {
		throw new UnsupportedOperationException("Engine stands for the API of your engine, create its hidden surface there");
	}

	public void destroy();

	public boolean isRunning();
	public void pollEvents();
	public void drawScene();
	public void present();

	public int getWidth();
	public int getHeight();

	public double getMouseX();
	public double getMouseY();
	public boolean isCursorLocked();

	public boolean isKeyDown(final int code);
	public String getKeyName(final int code);

	public String getClipboard();
	public void setClipboard(final String text);

	public boolean isRenderThread();
	public void post(final Runnable task);

	public long getGameNanos();
	public boolean isInterfaceHidden();
	public float getVolume(final Object group);

	public void addListener(final EngineListener listener);
	public void drawPreview(final int x, final int y, final int width, final int height);

	public EngineDevice getDevice();

}