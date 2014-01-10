package jogl.swt.sb6.examples.chapter5;

import static javax.media.opengl.GL4.*;

import java.nio.FloatBuffer;

import javax.media.opengl.GL4;

import jogl.swt.utils.GLUtils;
import jogl.swt.utils.GLUtils.ShaderType;
import jogl.swt.utils.math.MatrixUtils;
import jogl.swt.utils.math.Matrix4f;
import jogl.swt.utils.views.JOGLView;

public class Example_5_1 extends JOGLView {
	private int mvMatrixLoc;
	private int projMatrixLoc;
	
	private Matrix4f projectionMatrix;

	@Override
	protected void render(GL4 gl) {
		float currentTime = (float) (getApplicationTime() / 1000.0);
		
		float f = (float) currentTime * (float)Math.PI * 0.1f;
		
		Matrix4f mvMatrix = Matrix4f.multiplyAll(
				MatrixUtils.translate(0.0f, 0.0f, -4.0f), 
				MatrixUtils.translate((float)Math.sin(2.1f * f) * 0.5f,
						(float) Math.cos(1.7f * f) * 0.5f,
						(float) Math.sin(1.3f * f) * (float) Math.cos(1.5f * f) * 2.0f),
				MatrixUtils.rotateDegree(currentTime * 45.0f, 0.0f, 1.0f, 0.0f),
				MatrixUtils.rotateDegree(currentTime * 81.0f, 1.0f, 0.0f, 0.0f));
		
		gl.glUniformMatrix4fv(mvMatrixLoc, 1, false, mvMatrix.toArray(), 0);
		gl.glUniformMatrix4fv(projMatrixLoc, 1, false, projectionMatrix.toArray(), 0);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
	}

	@Override
	protected void startup(GL4 gl) {
		int[] buffers = new int[1];
		gl.glGenBuffers(buffers.length, buffers, 0);
		gl.glBindBuffer(GL_ARRAY_BUFFER, buffers[0]);
		
		FloatBuffer buffer = FloatBuffer.wrap(CUBE_VERTEX_POSITIONS);
		gl.glBufferData(GL_ARRAY_BUFFER, buffer.limit() * 4, buffer, GL_STATIC_DRAW);
		
		//	in vec4 position
		int posLoc = gl.glGetAttribLocation(getProgram(), "position");
		gl.glVertexAttribPointer(posLoc, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(posLoc);
		
		/*
			uniform mat4 mv_matrix;
			uniform mat4 proj_matrix;
		 */
		mvMatrixLoc = gl.glGetUniformLocation(getProgram(), "mv_matrix");
		projMatrixLoc = gl.glGetUniformLocation(getProgram(), "proj_matrix");
		
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CW);
	}

	@Override
	protected void shutdown(GL4 gl) {
	}
	
	@Override
	protected void resize(GL4 gl, int x, int y, int width, int height) {
		float aspect = (float) width / (float) height;
		projectionMatrix = MatrixUtils.perspective(50.0f, aspect, 0.1f, 1000.0f);
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
	
	private static final float[] CUBE_VERTEX_POSITIONS = {
		-0.25f,  0.25f, -0.25f,
        -0.25f, -0.25f, -0.25f,
         0.25f, -0.25f, -0.25f,

         0.25f, -0.25f, -0.25f,
         0.25f,  0.25f, -0.25f,
        -0.25f,  0.25f, -0.25f,

         0.25f, -0.25f, -0.25f,
         0.25f, -0.25f,  0.25f,
         0.25f,  0.25f, -0.25f,

         0.25f, -0.25f,  0.25f,
         0.25f,  0.25f,  0.25f,
         0.25f,  0.25f, -0.25f,

         0.25f, -0.25f,  0.25f,
        -0.25f, -0.25f,  0.25f,
         0.25f,  0.25f,  0.25f,

        -0.25f, -0.25f,  0.25f,
        -0.25f,  0.25f,  0.25f,
         0.25f,  0.25f,  0.25f,

        -0.25f, -0.25f,  0.25f,
        -0.25f, -0.25f, -0.25f,
        -0.25f,  0.25f,  0.25f,

        -0.25f, -0.25f, -0.25f,
        -0.25f,  0.25f, -0.25f,
        -0.25f,  0.25f,  0.25f,

        -0.25f, -0.25f,  0.25f,
         0.25f, -0.25f,  0.25f,
         0.25f, -0.25f, -0.25f,

         0.25f, -0.25f, -0.25f,
        -0.25f, -0.25f, -0.25f,
        -0.25f, -0.25f,  0.25f,

        -0.25f,  0.25f, -0.25f,
         0.25f,  0.25f, -0.25f,
         0.25f,  0.25f,  0.25f,

         0.25f,  0.25f,  0.25f,
        -0.25f,  0.25f,  0.25f,
        -0.25f,  0.25f, -0.25f
	};
}
