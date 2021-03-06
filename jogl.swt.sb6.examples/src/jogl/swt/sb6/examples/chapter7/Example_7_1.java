package jogl.swt.sb6.examples.chapter7;

import static javax.media.opengl.GL4.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.media.opengl.GL4;

import jogl.swt.utils.GLUtils;
import jogl.swt.utils.GLUtils.ProjectionType;
import jogl.swt.utils.GLUtils.ShaderType;
import jogl.swt.utils.math.MatrixUtils;
import jogl.swt.utils.math.Matrix4f;
import jogl.swt.utils.views.JOGLView;

public class Example_7_1 extends JOGLView {
	private int mvMatrixLoc;
	private int projMatrixLoc;
	private int posBuffer;
	private int indexBuffer;
	
	@Override
	protected void render(GL4 gl) {
		float currentTime = (float) (getApplicationTime() / 1000.0);
		
		float f = (float) currentTime * (float)Math.PI * 0.1f;
		
		Matrix4f mvMatrix = Matrix4f.multiplyAll(
				getLookAtMatrix(), 
				MatrixUtils.translate((float)Math.sin(2.1f * f) * 0.5f,
						(float) Math.cos(1.7f * f) * 0.5f,
						(float) Math.sin(1.3f * f) * (float) Math.cos(1.5f * f) * 2.0f),
				MatrixUtils.rotateDegree(currentTime * 45.0f, 0.0f, 1.0f, 0.0f),
				MatrixUtils.rotateDegree(currentTime * 81.0f, 1.0f, 0.0f, 0.0f));
		
		gl.glUniformMatrix4fv(mvMatrixLoc, 1, false, mvMatrix.toArray(), 0);
		gl.glUniformMatrix4fv(projMatrixLoc, 1, false, getDefaultProjectionMatrix(ProjectionType.PERSPECTIVE).toArray(), 0);
		
		gl.glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_SHORT, 0);
	}

	@Override
	protected void startup(GL4 gl) {
		int[] buffers = new int[2];
		gl.glGenBuffers(buffers.length, buffers, 0);
		
		posBuffer = buffers[0];
		indexBuffer = buffers[1];
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, posBuffer);
		FloatBuffer buffer = FloatBuffer.wrap(CUBE_VERTEX_POSITIONS);
		gl.glBufferData(GL_ARRAY_BUFFER, buffer.limit() * 4, buffer, GL_STATIC_DRAW);
		
		//	in vec4 position
		int posLoc = gl.glGetAttribLocation(getProgram(), "position");
		gl.glVertexAttribPointer(posLoc, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(posLoc);
		
		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
		ShortBuffer indexes = ShortBuffer.wrap(VERTEX_INDICES);
		gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexes.limit() * 2, indexes, GL_STATIC_DRAW);
		
		/*
			uniform mat4 mv_matrix;
			uniform mat4 proj_matrix;
		 */
		mvMatrixLoc = gl.glGetUniformLocation(getProgram(), "mv_matrix");
		projMatrixLoc = gl.glGetUniformLocation(getProgram(), "proj_matrix");
		
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
	}

	@Override
	protected void shutdown(GL4 gl) {
		IntBuffer buffers = IntBuffer.allocate(2);
		buffers.put(posBuffer);
		buffers.put(indexBuffer);
		
		buffers.position(0);
		
		gl.glDeleteBuffers(2, buffers);
	}
	
	@Override
	protected void resize(GL4 gl, int x, int y, int width, int height) {
	}
	
	@Override
	protected String[] getShaderSourceLines(ShaderType shaderType) {
		if(shaderType == ShaderType.VERTEX_SHADER)
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter5/vshader_5_1.glsl"));
		else if(shaderType == ShaderType.FRAGMENT_SHADER)
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter3/fshader_3_3.glsl"));
		else
			return super.getShaderSourceLines(shaderType);
	}
	
	private static final short[] VERTEX_INDICES = {
		0, 1, 2,
		2, 1, 3,
		2, 3, 4,
		4, 3, 5,
		4, 5, 6,
		6, 5, 7,
		6, 7, 0,
		0, 7, 1,
		6, 0, 2,
		2, 4, 6,
		7, 5, 3,
		7, 3, 1
	};
	
	private static final float[] CUBE_VERTEX_POSITIONS = {
		-0.25f, -0.25f, -0.25f,
		-0.25f,  0.25f, -0.25f,
		 0.25f, -0.25f, -0.25f,
		 0.25f,  0.25f, -0.25f,
		 0.25f, -0.25f,  0.25f,
		 0.25f,  0.25f,  0.25f,
		-0.25f, -0.25f,  0.25f,
		-0.25f,  0.25f,  0.25f,
	};
}
