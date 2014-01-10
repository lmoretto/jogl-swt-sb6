package jogl.swt.sb6.examples.chapter3;

import static javax.media.opengl.GL4.*;

import java.nio.FloatBuffer;

import javax.media.opengl.GL4;

import jogl.swt.utils.GLUtils;
import jogl.swt.utils.GLUtils.ShaderType;
import jogl.swt.utils.views.JOGLView;

public class Example_3_1 extends JOGLView {
	@Override
	protected void render(GL4 gl) {
		long currentTime = System.currentTimeMillis();
		
		FloatBuffer attrib = FloatBuffer.allocate(4);
		attrib.put(0, (float)(Math.sin(currentTime / 1000.0) * 0.5));
		attrib.put(1, (float)(Math.cos(currentTime / 1000.0) * 0.6));
		attrib.put(2, 0.0f);
		attrib.put(3,  0.0f);
		
		gl.glVertexAttrib4fv(0, attrib);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, 3);
	}

	@Override
	protected void startup(GL4 gl) {
	}

	@Override
	protected void shutdown(GL4 gl) {
	}
	
	@Override
	protected String[] getShaderSourceLines(ShaderType shaderType) {
		if(shaderType == ShaderType.VERTEX_SHADER)
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter3/vshader_3_1.glsl"));
		else if(shaderType == ShaderType.FRAGMENT_SHADER)
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter2/fshader_2_4.glsl"));
		else
			return super.getShaderSourceLines(shaderType);
	}

	@Override
	protected void resize(GL4 gl, int x, int y, int width, int height) {
	}
}
