package jogl.swt.sb6.examples.chapter2;

import static javax.media.opengl.GL4.*;

import java.nio.FloatBuffer;

import javax.media.opengl.GL4;

import jogl.swt.utils.views.JOGLView;

public class Example_2_1 extends JOGLView {

	@Override
	protected void render(GL4 gl) {
		FloatBuffer red = FloatBuffer.allocate(4);
		red.put(0, 1.0f);
		red.put(1, 0.0f);
		red.put(2, 0.0f);
		red.put(3, 1.0f);
		gl.glClearBufferfv(GL_COLOR, 0, red);
	}

	@Override
	protected void startup(GL4 gl) {
	}

	@Override
	protected void shutdown(GL4 gl) {
	}

	@Override
	protected void resize(GL4 gl, int x, int y, int width, int height) {
	}
}
