package jogl.swt.sb6.examples.chapter7;

import static javax.media.opengl.GL4.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL4;

import jogl.swt.utils.GLUtils;
import jogl.swt.utils.GLUtils.ProjectionType;
import jogl.swt.utils.GLUtils.ShaderType;
import jogl.swt.utils.math.Matrix4f;
import jogl.swt.utils.views.JOGLView;

public class Example_7_4 extends JOGLView {
	private static final int POINTS_X = 50;
	private static final int POINTS_Y = 50;
	private static final int POINTS_TOTAL = POINTS_X * POINTS_Y;
	private static final int CONNECTIONS_TOTAL = (POINTS_X - 1) * POINTS_Y + (POINTS_Y - 1) * POINTS_X;
	
	private static final int POSITION_A = 0;
	private static final int POSITION_B = POSITION_A + 1;
	private static final int VELOCITY_A = POSITION_B + 1;
	private static final int VELOCITY_B = VELOCITY_A + 1;
	private static final int CONNECTION = VELOCITY_B + 1;
	
	private int simulationProgram;
	private int simulationPositionLocation;
	private int simulationVelocityLocation;
	private int simulationConnectionLocation;
	private int iterationsPerFrame = 16;
	private int iterationIndex = 0;
	
	private int renderViewProjLocation;
	
	private int[] mVao = new int[2];
	private int[] mVbo = new int[5];
	private int[] mPosTbo = new int[2];
	private int[] mIndexBuffer = new int[1];

	@Override
	protected void render(GL4 gl) {
		//SIMULATION
		gl.glUseProgram(simulationProgram);
		
		//we don't want to draw anything, just simulate --> disable backend
		gl.glEnable(GL_RASTERIZER_DISCARD);
		
		for(int i = iterationsPerFrame; i != 0; i--) {
			//Set vertex attribute buffers and texture buffer
			gl.glBindVertexArray(mVao[iterationIndex & 1]);
			gl.glBindTexture(GL_TEXTURE_BUFFER, mPosTbo[iterationIndex & 1]);
			
			//increment iterationIndex to use the other buffers for transform feedback (double buffering)
			iterationIndex++;
			gl.glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, mVbo[POSITION_A + (iterationIndex & 1)]);
			gl.glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 1, mVbo[VELOCITY_A + (iterationIndex & 1)]);
			
			//begin transform feedback
			gl.glBeginTransformFeedback(GL_POINTS);
			
			//trigger simulation
			gl.glDrawArrays(GL_POINTS, 0, POINTS_TOTAL);
			
			//stop transform feedback
			gl.glEndTransformFeedback();
		}
		
		//RENDERING
		
		//enable backend
		gl.glDisable(GL_RASTERIZER_DISCARD);
		
		gl.glUseProgram(getProgram());
		
		gl.glUniformMatrix4fv(renderViewProjLocation, 1, false,
				Matrix4f.multiplyAll(getDefaultProjectionMatrix(ProjectionType.PERSPECTIVE), getLookAtMatrix()).toArray(), 0);
		
		gl.glPointSize(4.0f);
		gl.glDrawArrays(GL_POINTS, 0, POINTS_TOTAL);
		
		gl.glDrawElements(GL_LINES, CONNECTIONS_TOTAL * 2, GL_UNSIGNED_INT, 0);
	}

	@Override
	protected void startup(GL4 gl) {
		int vShaderSimulation = GLUtils.compileShader(GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter7/vshader_7_4_simulation.glsl")), gl, ShaderType.VERTEX_SHADER);
		simulationProgram = GLUtils.compileProgram(new Integer[]{vShaderSimulation}, gl);
		gl.glTransformFeedbackVaryings(simulationProgram, 2, new String[]{"tf_position_mass", "tf_velocity"}, GL_SEPARATE_ATTRIBS);
		gl.glLinkProgram(simulationProgram);
		GLUtils.printProgramLog(gl, simulationProgram);
		
		simulationPositionLocation = gl.glGetAttribLocation(simulationProgram, "position_mass");
		simulationVelocityLocation = gl.glGetAttribLocation(simulationProgram, "velocity");
		simulationConnectionLocation = gl.glGetAttribLocation(simulationProgram, "connection");
		
		float[] initialPositions = new float[POINTS_TOTAL * 4];
		float[] initialVelocities = new float[POINTS_TOTAL * 3];
		int[] connectionVectors = new int[POINTS_TOTAL * 4];
		
		//simulation data initialization
		int n = 0;
		
		for(int j = 0; j < POINTS_Y; j++) {
			float fj = (float) j / (float) POINTS_Y;
			
			for (int i = 0; i < POINTS_X; i++) {
				float fi = (float) i / (float) POINTS_X;
				
				int positionIndexX = n*4;
				int velocityIndexX = n*3;
				
				initialPositions[positionIndexX + 0] = (fi - 0.5f) * (float)POINTS_X;
				initialPositions[positionIndexX + 1] = (fj - 0.5f) * (float)POINTS_Y;
				initialPositions[positionIndexX + 2] = (float) (0.6 * Math.sin(fi) * Math.cos(fj));
				initialPositions[positionIndexX + 3] = 1.0f;
				
				for (int k = 0; k < 3; k++) {
					initialVelocities[velocityIndexX + k] = 0.0f;
				}
				
				for (int k = 0; k < 4; k++) {
					connectionVectors[positionIndexX + k] = -1;
				}
				
				if(j != POINTS_Y - 1) {
					if(i != 0)
						connectionVectors[positionIndexX + 0] = n - 1;
					if(j != 0)
						connectionVectors[positionIndexX + 1] = n - POINTS_X;
					if(i != (POINTS_X - 1))
						connectionVectors[positionIndexX + 2] = n + 1;
					if(j != POINTS_Y - 1)
						connectionVectors[positionIndexX + 3] = n + POINTS_X;
				}
				
				n++;
			}
		}
		
		gl.glGenVertexArrays(mVao.length, mVao, 0);
		gl.glGenBuffers(mVbo.length, mVbo, 0);
		
		for(int i = 0; i < mVao.length; i++) {
			gl.glBindVertexArray(mVao[i]);
			
			gl.glBindBuffer(GL_ARRAY_BUFFER, mVbo[POSITION_A + i]);
			gl.glBufferData(GL_ARRAY_BUFFER, POINTS_TOTAL * 4 * 4, FloatBuffer.wrap(initialPositions), GL_DYNAMIC_COPY);
			gl.glVertexAttribPointer(simulationPositionLocation, 4, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(simulationPositionLocation);
			
			gl.glBindBuffer(GL_ARRAY_BUFFER, mVbo[VELOCITY_A + i]);
			gl.glBufferData(GL_ARRAY_BUFFER, POINTS_TOTAL * 3 * 4, FloatBuffer.wrap(initialVelocities), GL_DYNAMIC_COPY);
			gl.glVertexAttribPointer(simulationVelocityLocation, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(simulationVelocityLocation);
			
			gl.glBindBuffer(GL_ARRAY_BUFFER, mVbo[CONNECTION]);
			gl.glBufferData(GL_ARRAY_BUFFER, POINTS_TOTAL * 4 * 4, IntBuffer.wrap(connectionVectors), GL_STATIC_DRAW);
			gl.glVertexAttribIPointer(simulationConnectionLocation, 4, GL_INT, 0, 0);
			gl.glEnableVertexAttribArray(simulationConnectionLocation);
		}
		
		gl.glGenTextures(mPosTbo.length, mPosTbo, 0);
		gl.glBindTexture(GL_TEXTURE_BUFFER, mPosTbo[0]);
		gl.glTexBuffer(GL_TEXTURE_BUFFER, GL_RGBA32F, mVbo[POSITION_A]);
		gl.glBindTexture(GL_TEXTURE_BUFFER, mPosTbo[1]);
		gl.glTexBuffer(GL_TEXTURE_BUFFER, GL_RGBA32F, mVbo[POSITION_B]);
		
		gl.glGenBuffers(mIndexBuffer.length, mIndexBuffer, 0);
		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mIndexBuffer[0]);
		gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, CONNECTIONS_TOTAL * 2 * 4, null, GL_STATIC_DRAW);
		ByteBuffer mappedIndexBuffer = gl.glMapBufferRange(GL_ELEMENT_ARRAY_BUFFER, 0, CONNECTIONS_TOTAL * 2 * 4, GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
		for(int j = 0; j < POINTS_Y; j++) {
			for(int i = 0; i < POINTS_X - 1; i++) {
				mappedIndexBuffer.putInt(i + j * POINTS_X);
				mappedIndexBuffer.putInt(1 + i + j * POINTS_X);
			}
		}
		for(int i = 0; i < POINTS_X; i++) {
			for(int j = 0; j < POINTS_Y - 1; j++) {
				mappedIndexBuffer.putInt(i + j * POINTS_X);
				mappedIndexBuffer.putInt(POINTS_X + i + j * POINTS_X);
			}
		}
		gl.glUnmapBuffer(GL_ELEMENT_ARRAY_BUFFER);
		
		//rendering program
		int renderingProgram = getProgram();
		renderViewProjLocation = gl.glGetUniformLocation(renderingProgram, "view_proj");
	}
	
	@Override
	protected String[] getShaderSourceLines(ShaderType shaderType) {
		switch (shaderType) {
		case VERTEX_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter7/vshader_7_4_render.glsl"));
		case FRAGMENT_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter7/fshader_7_4_render.glsl"));
		default:
			return super.getShaderSourceLines(shaderType);
		}
	}

	@Override
	protected void shutdown(GL4 gl) {
		gl.glDeleteProgram(simulationProgram);
		gl.glDeleteVertexArrays(mVao.length, mVao, 0);
		gl.glDeleteBuffers(mVbo.length, mVbo, 0);
		gl.glDeleteTextures(mPosTbo.length, mPosTbo, 0);
		gl.glDeleteBuffers(mIndexBuffer.length, mIndexBuffer, 0);
	}

	@Override
	protected void resize(GL4 gl, int x, int y, int width, int height) {
	}

}
