package jogl.swt.sb6.examples.chapter3;

import static javax.media.opengl.GL4.*;

import java.nio.FloatBuffer;

import javax.media.opengl.GL4;

import jogl.swt.utils.views.JOGLView;

public class Example_3_2 extends JOGLView {
	private int[] vertexArray = new int[1];

	@Override
	protected void render(GL4 gl) {
		long currentTime = System.currentTimeMillis();
		
		FloatBuffer offset = FloatBuffer.allocate(4);
		offset.put(0, (float)(Math.sin(currentTime / 1000.0) * 0.5));
		offset.put(1, (float)(Math.cos(currentTime / 1000.0) * 0.6));
		offset.put(2, 0.0f);
		offset.put(3,  0.0f);
		
		FloatBuffer color = FloatBuffer.allocate(4);
		color.put(0, (float) (Math.sin(currentTime / 1000.0) * 0.5 + 0.5));
		color.put(1, (float) (Math.cos(currentTime / 1000.0) * 0.5 + 0.5));
		color.put(2,  0.0f);
		color.put(3, 1.0f);
		
		gl.glVertexAttrib4fv(0, offset);
		gl.glVertexAttrib4fv(1, color);
		
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
	protected String[] getShaderSourceLines(int shaderType) {
		if(shaderType == GL_VERTEX_SHADER)
			return readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter3/vshader_3_2.glsl"));
		else if(shaderType == GL_FRAGMENT_SHADER)
			return readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter3/fshader_3_2.glsl"));
		else
			return super.getShaderSourceLines(shaderType);
	}

}
