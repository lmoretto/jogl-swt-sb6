package jogl.swt.sb6.examples.chapter7;

import static javax.media.opengl.GL4.*;

import java.nio.FloatBuffer;

import javax.media.opengl.GL4;

import jogl.swt.utils.GLUtils;
import jogl.swt.utils.GLUtils.ProjectionType;
import jogl.swt.utils.GLUtils.ShaderType;
import jogl.swt.utils.math.Matrix4f;
import jogl.swt.utils.views.JOGLView;

public class Example_7_2 extends JOGLView {
	private int[] buffer = new int[1];
	
	private int position;
	private int instanceColor;
	private int instancePosition;
	private int mvp;

	public Example_7_2() {
	}

	@Override
	protected void render(GL4 gl) {
		Matrix4f mvpMatrix = Matrix4f.multiplyAll(
				getDefaultProjectionMatrix(ProjectionType.PERSPECTIVE), 
				getLookAtMatrix());
		
		gl.glUniformMatrix4fv(mvp, 1, false, mvpMatrix.toArray(), 0);
		
		gl.glDrawArraysInstanced(GL_TRIANGLE_FAN, 0, 4, 4);
	}

	@Override
	protected void startup(GL4 gl) {
		int program = getProgram();
		/*
			in vec4 position;
			in vec4 instance_color;
			in vec4 instance_position;
			uniform mat4 mvp;
		 */
		
		position = gl.glGetAttribLocation(program, "position");
		instanceColor = gl.glGetAttribLocation(program, "instance_color");
		instancePosition = gl.glGetAttribLocation(program, "instance_position");
		mvp = gl.glGetUniformLocation(program, "mvp");
		
		gl.glGenBuffers(1, buffer, 0);
		gl.glBindBuffer(GL_ARRAY_BUFFER, buffer[0]);
		gl.glBufferData(GL_ARRAY_BUFFER, 
				SQUARE_VERTICES.length * 4 +
				INSTANCE_COLORS.length * 4 +
				INSTANCE_POSITIONS.length * 4,
				null, GL_STATIC_DRAW);
		
		int offset = 0;
		gl.glBufferSubData(GL_ARRAY_BUFFER, offset, SQUARE_VERTICES.length * 4, FloatBuffer.wrap(SQUARE_VERTICES));
		
		offset += SQUARE_VERTICES.length * 4;
		gl.glBufferSubData(GL_ARRAY_BUFFER, offset, INSTANCE_COLORS.length * 4, FloatBuffer.wrap(INSTANCE_COLORS));
		
		offset += INSTANCE_COLORS.length * 4;
		gl.glBufferSubData(GL_ARRAY_BUFFER, offset, INSTANCE_POSITIONS.length * 4, FloatBuffer.wrap(INSTANCE_POSITIONS));
		
		offset += INSTANCE_POSITIONS.length * 4;
		
		gl.glVertexAttribPointer(position, 4, GL_FLOAT, false, 0, 0);
		gl.glVertexAttribPointer(instanceColor, 4, GL_FLOAT, false, 0, SQUARE_VERTICES.length * 4);
		gl.glVertexAttribPointer(instancePosition, 4, GL_FLOAT, false, 0, SQUARE_VERTICES.length * 4 + INSTANCE_COLORS.length * 4);
		
		gl.glEnableVertexAttribArray(position);
		gl.glEnableVertexAttribArray(instanceColor);
		gl.glEnableVertexAttribArray(instancePosition);
		
		gl.glVertexAttribDivisor(instanceColor, 1); //new color for every new instance
		gl.glVertexAttribDivisor(instancePosition, 1); //new position for every new instance
	}

	@Override
	protected void shutdown(GL4 gl) {
		gl.glDeleteBuffers(1, buffer, 0);
	}

	@Override
	protected void resize(GL4 gl, int x, int y, int width, int height) {
	}
	
	@Override
	protected String[] getShaderSourceLines(ShaderType shaderType) {
		switch (shaderType) {
		case VERTEX_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter7/vshader_7_2.glsl"));
		case FRAGMENT_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter7/fshader_7_2.glsl"));
		default:
			return super.getShaderSourceLines(shaderType);
		}
	}
	
	private static final float[] SQUARE_VERTICES = {
		-1.0f, -1.0f, 0.0f, 1.0f,
		 1.0f, -1.0f, 0.0f, 1.0f,
		 1.0f,  1.0f, 0.0f, 1.0f,
		-1.0f,  1.0f, 0.0f, 1.0f,
	};
	
	private static final float[] INSTANCE_COLORS = {
		1.0f, 0.0f, 0.0f, 1.0f,
		0.0f, 1.0f, 0.0f, 1.0f,
		0.0f, 0.0f, 1.0f, 1.0f,
		1.0f, 1.0f, 0.0f, 1.0f,
	};
	
	private static final float[] INSTANCE_POSITIONS = {
		-2.0f, -2.0f, 0.0f, 0.0f,
		 2.0f, -2.0f, 0.0f, 0.0f,
		 2.0f,  2.0f, 0.0f, 0.0f,
		-2.0f,  2.0f, 0.0f, 0.0f,
	};
}
