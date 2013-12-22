package jogl.swt.sb6.examples.chapter2;

import static javax.media.opengl.GL4.*;

import java.nio.FloatBuffer;

import javax.media.opengl.GL4;

import jogl.swt.utils.views.JOGLView;

public class Example_2_2 extends JOGLView {

	@Override
	protected void internalDisplay(GL4 gl) {
		FloatBuffer color = FloatBuffer.allocate(4);
		long currentTime = System.currentTimeMillis();
		color.put(0, (float) (Math.sin(currentTime / 100.0) * 0.5 + 0.5));
		color.put(1, (float) (Math.cos(currentTime / 100.0) * 0.5 + 0.5));
		color.put(2,  0.0f);
		color.put(3, 1.0f);
		gl.glClearBufferfv(GL_COLOR, 0, color);
	}

}
