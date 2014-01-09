package jogl.swt.sb6.examples.chapter3;

import static javax.media.opengl.GL4.*;

import java.nio.FloatBuffer;

import javax.media.opengl.GL4;

import jogl.swt.utils.views.JOGLView;

public class Example_3_5 extends JOGLView {
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
		color.put(0, (float) (Math.sin(currentTime / 500.0) * 0.5 + 0.5));
		color.put(1, 0.0f);
		color.put(2, (float) (Math.cos(currentTime / 500.0) * 0.5 + 0.5));
		color.put(3, 1.0f);
		
		gl.glVertexAttrib4fv(0, offset);
		gl.glVertexAttrib4fv(1, color);

		gl.glPointSize(5.0f);
		
		gl.glDrawArrays(GL_PATCHES, 0, 3);
	}

	@Override
	protected void startup(GL4 gl) {
		gl.glGenVertexArrays(vertexArray.length, vertexArray, 0);
		gl.glBindVertexArray(vertexArray[0]);
		
		gl.glPatchParameteri(GL_PATCH_VERTICES, 3);
	}

	@Override
	protected void shutdown(GL4 gl) {
		gl.glDeleteVertexArrays(vertexArray.length, vertexArray, 0);
	}
	
	@Override
	protected String[] getShaderSourceLines(ShaderType shaderType) {
		switch (shaderType) {
		case VERTEX_SHADER:
			return readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter3/vshader_3_3.glsl"));
		case FRAGMENT_SHADER:
			return readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter3/fshader_3_3.glsl"));
		case TESS_CONTROL_SHADER:
			return readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter3/tess_control_shader_3_4.glsl"));
		case TESS_EVALUATION_SHADER:
			return readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter3/tess_evaluation_shader_3_4.glsl"));
		case GEOMETRY_SHADER:
			return readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter3/geometry_shader_3_5.glsl"));
		default:
			return super.getShaderSourceLines(shaderType);
		}
	}

	@Override
	protected void resize(GL4 gl, int x, int y, int width, int height) {
	}
}
