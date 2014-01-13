package jogl.swt.sb6.examples;

import static javax.media.opengl.GL4.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL4;

import jogl.swt.utils.GLUtils;
import jogl.swt.utils.GLUtils.ProjectionType;
import jogl.swt.utils.GLUtils.ShaderType;
import jogl.swt.utils.math.Matrix4f;
import jogl.swt.utils.views.JOGLView;

public class StaticCube extends JOGLView {
	private int mvMatrixLoc;
	private int projMatrixLoc;

	private int posLoc;
	private int faceLoc;

	private int[] buffers;
	
	@Override
	protected void render(GL4 gl) {
		Matrix4f mvMatrix = Matrix4f.multiplyAll(
				getLookAtMatrix(), 
				Matrix4f.identity);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, buffers[0]);
		gl.glVertexAttribPointer(posLoc, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(posLoc);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, buffers[1]);
		gl.glVertexAttribIPointer(faceLoc, 1, GL_INT, 0, 0);
		gl.glEnableVertexAttribArray(faceLoc);
		
		gl.glUniformMatrix4fv(mvMatrixLoc, 1, false, mvMatrix.toArray(), 0);
		gl.glUniformMatrix4fv(projMatrixLoc, 1, false, getDefaultProjectionMatrix(ProjectionType.PERSPECTIVE).toArray(), 0);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
	}

	@Override
	protected void startup(GL4 gl) {
		buffers = new int[2];
		gl.glGenBuffers(buffers.length, buffers, 0);
		gl.glBindBuffer(GL_ARRAY_BUFFER, buffers[0]);
		
		FloatBuffer positionBuffer = FloatBuffer.wrap(CUBE_VERTEX_POSITIONS);
		gl.glBufferData(GL_ARRAY_BUFFER, positionBuffer.limit() * 4, positionBuffer, GL_STATIC_DRAW);
		
		//	in vec4 position
		posLoc = gl.glGetAttribLocation(getProgram(), "position");
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, buffers[1]);
		IntBuffer faceBuffer = IntBuffer.wrap(CUBE_VERTEX_FACES);
		gl.glBufferData(GL_ARRAY_BUFFER, faceBuffer.limit() * 4, faceBuffer, GL_STATIC_DRAW);
		
		faceLoc = gl.glGetAttribLocation(getProgram(), "face");
		
		/*
			uniform mat4 mv_matrix;
			uniform mat4 proj_matrix;
		 */
		mvMatrixLoc = gl.glGetUniformLocation(getProgram(), "mv_matrix");
		projMatrixLoc = gl.glGetUniformLocation(getProgram(), "proj_matrix");
		
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	}

	@Override
	protected void shutdown(GL4 gl) {
	}
	
	@Override
	protected void resize(GL4 gl, int x, int y, int width, int height) {
	}
	
	@Override
	protected String[] getShaderSourceLines(ShaderType shaderType) {
		if(shaderType == ShaderType.VERTEX_SHADER)
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/vshader_staticCube.glsl"));
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
	
	private static final int[] CUBE_VERTEX_FACES = {
		0,0,0,0,0,0,
		1,1,1,1,1,1,
		2,2,2,2,2,2,
		3,3,3,3,3,3,
		4,4,4,4,4,4,
		5,5,5,5,5,5,
	};
}
