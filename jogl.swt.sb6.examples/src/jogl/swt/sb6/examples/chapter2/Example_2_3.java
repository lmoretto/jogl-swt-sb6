package jogl.swt.sb6.examples.chapter2;

import static javax.media.opengl.GL4.*;

import javax.media.opengl.GL4;

public class Example_2_3 extends Example_2_2 {
	private int renderingProgram;
	private int[] vertexArray = new int[1];
	
	@Override
	protected void render(GL4 gl) {
		super.render(gl);
		gl.glUseProgram(renderingProgram);
		gl.glDrawArrays(GL_POINTS, 0, 1);
		
		long currentTime = System.currentTimeMillis();
		gl.glPointSize(40.0f * (float)(Math.sin(currentTime / 100.0) * 0.5 + 0.5));
	}

	@Override
	protected void startup(GL4 gl) {
		super.startup(gl);
		renderingProgram = createShaderPrograms(gl);
		gl.glGenVertexArrays(vertexArray.length, vertexArray, 0);
		gl.glBindVertexArray(vertexArray[0]);
	}

	@Override
	protected void shutdown(GL4 gl) {
		super.shutdown(gl);
		gl.glDeleteProgram(renderingProgram);
		gl.glDeleteVertexArrays(vertexArray.length, vertexArray, 0);
	}
	
	private int createShaderPrograms(GL4 gl) {
		final String[] vShaderSource = { 
				"#version 430 core								\n",
				"void main(void) {								\n",
				"	gl_Position = vec4(0.0, 0.0, 0.5, 1.0);		\n",
				"}"
		};
		
		final String[] fShaderSource = { 
				"#version 430 core								\n",
				"												\n",
				"out vec4 color;								\n",
				"												\n",
				"void main(void) {								\n",
				"	color = vec4(0.0, 0.8, 1.0, 1.0);			\n",
				"}"
		};
		
		int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
		gl.glShaderSource(vShader, vShaderSource.length, vShaderSource, null);
		gl.glCompileShader(vShader);
		
		int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
		gl.glShaderSource(fShader, fShaderSource.length, fShaderSource, null);
		gl.glCompileShader(fShader);
		
		int program = gl.glCreateProgram();
		gl.glAttachShader(program, vShader);
		gl.glAttachShader(program, fShader);
		gl.glLinkProgram(program);
		
		gl.glDeleteShader(vShader);
		gl.glDeleteShader(fShader);
		
		return program;
	}
}
