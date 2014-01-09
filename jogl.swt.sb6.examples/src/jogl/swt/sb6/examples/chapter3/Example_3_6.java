package jogl.swt.sb6.examples.chapter3;

import static javax.media.opengl.GL4.*;

import javax.media.opengl.GL4;

import jogl.swt.utils.views.JOGLView;

public class Example_3_6 extends JOGLView {
	private int[] vertexArray = new int[1];

	@Override
	protected void render(GL4 gl) {
		gl.glDrawArrays(GL_TRIANGLES, 0, 3);
	}

	@Override
	protected void startup(GL4 gl) {
		gl.glGenVertexArrays(vertexArray.length, vertexArray, 0);
		gl.glBindVertexArray(vertexArray[0]);
	}

	@Override
	protected void shutdown(GL4 gl) {
		gl.glDeleteVertexArrays(vertexArray.length, vertexArray, 0);
	}
	
	@Override
	protected String[] getShaderSourceLines(ShaderType shaderType) {
		if(shaderType == ShaderType.VERTEX_SHADER)
			return readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter2/vshader_2_4.glsl"));
		else if(shaderType == ShaderType.FRAGMENT_SHADER)
			return readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter3/fshader_3_6.glsl"));
		else
			return super.getShaderSourceLines(shaderType);
	}

	@Override
	protected void resize(GL4 gl, int x, int y, int width, int height) {
	}
}
