package jogl.swt.sb6.examples.chapter2;

import static javax.media.opengl.GL4.*;

import java.nio.FloatBuffer;

import javax.media.opengl.GL4;

import jogl.swt.utils.views.JOGLView;

public class Ch2Pg14 extends JOGLView {

	@Override
	protected void internalDisplay(GL4 gl) {
		FloatBuffer red = FloatBuffer.allocate(4);
		red.put(0, 1.0f);
		red.put(1, 0.0f);
		red.put(2, 0.0f);
		red.put(3, 1.0f);
		gl.glClearBufferfv(GL_COLOR, 0, red);
	}

}
