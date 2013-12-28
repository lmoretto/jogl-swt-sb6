package jogl.swt.sb6.examples.chapter2;

import static javax.media.opengl.GL4.*;

import javax.media.opengl.GL4;

import jogl.swt.utils.views.JOGLView;

public class Example_2_3 extends JOGLView {
	private int[] vertexArray = new int[1];
	
	private static final String[] V_SHADER_SOURCE = { 
			"#version 430 core								\n",
			"void main(void) {								\n",
			"	gl_Position = vec4(0.0, 0.0, 0.5, 1.0);		\n",
			"}"
	};

	private static final String[] F_SHADER_SOURCE = { 
			"#version 430 core								\n",
			"												\n",
			"out vec4 color;								\n",
			"												\n",
			"void main(void) {								\n",
			"	color = vec4(0.0, 0.8, 1.0, 1.0);			\n",
			"}"
	};

	
	@Override
	protected void render(GL4 gl) {
		long currentTime = System.currentTimeMillis();
		gl.glPointSize(40.0f * (float)(Math.sin(currentTime / 100.0) * 0.5 + 0.5));
		
		gl.glDrawArrays(GL_POINTS, 0, 1);
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
			return V_SHADER_SOURCE;
		else if (shaderType == ShaderType.FRAGMENT_SHADER)
			return F_SHADER_SOURCE;
		else
			return super.getShaderSourceLines(shaderType);
	}
}
