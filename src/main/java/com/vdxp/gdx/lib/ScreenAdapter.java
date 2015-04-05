package com.vdxp.gdx.lib;

public abstract class ScreenAdapter implements com.badlogic.gdx.Screen {

	@Override
	public void show() {
	}

	@Override
	public void resize(final int width, final int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
		dispose();
	}

	@Override
	public void dispose() {
	}

}
