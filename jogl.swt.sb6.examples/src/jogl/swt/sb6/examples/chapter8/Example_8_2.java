package jogl.swt.sb6.examples.chapter8;

import static javax.media.opengl.GL4.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.media.opengl.GL4;

import org.eclipse.swt.events.KeyEvent;

import jogl.swt.utils.GLUtils;
import jogl.swt.utils.GLUtils.ProjectionType;
import jogl.swt.utils.GLUtils.ShaderType;
import jogl.swt.utils.math.Matrix4f;
import jogl.swt.utils.views.JOGLView;

public class Example_8_2 extends JOGLView {
	private int mvMatLocation;
	private int projMatLocation;
	private int tessFactorLocation;
	private int patchBuffer;
	private int cageIndices;
	
	private boolean wireFrame = false;
	private boolean showControlCage = false;
	private boolean showContrlPoints = false;
	
	private int cageProgram;
	private int mvpLocation;
	private int drawColor;

	@Override
	protected void render(GL4 gl) {
		float t = getApplicationTime() / 1000.0f;
		
		//Even if our VAO is bound, we need to rebind patchBuffer to GL_ARRAY_BUFFER to change its data.
		//This is because the last GL_ARRAY_BUFFER bound buffer wasn't patchBuffer, but the axis buffer from the base JOGLView class
		gl.glBindBuffer(GL_ARRAY_BUFFER, patchBuffer);
		ByteBuffer buf = gl.glMapBufferRange(GL_ARRAY_BUFFER, 0, PATCH_INITIALIZER.length * Float.SIZE / 8, GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
		
		for(int i = 0; i < 16; i++) {
			buf.putFloat(PATCH_INITIALIZER[3*i + 0]);
			buf.putFloat(PATCH_INITIALIZER[3*i + 1]);
			
			float fi = (float) i / 16.0f;
			
			buf.putFloat((float) Math.sin(t * (0.2f + fi * 0.3f)));
		}
		
		gl.glUnmapBuffer(GL_ARRAY_BUFFER);
		
		Matrix4f mvMatrix = getLookAtMatrix();
		
		Matrix4f projMatrix = getDefaultProjectionMatrix(ProjectionType.PERSPECTIVE);
		
		gl.glUniformMatrix4fv(mvMatLocation, 1, false, mvMatrix.toArray(), 0);
		gl.glUniformMatrix4fv(projMatLocation, 1, false, projMatrix.toArray(), 0);
		
		float tessFactor = 64.0f;
		gl.glUniform1f(tessFactorLocation, tessFactor);
		
		float[] currentLineWidth = new float[1];
		gl.glGetFloatv(GL_LINE_WIDTH, currentLineWidth, 0);
		gl.glLineWidth(1.0f);
		
		if(wireFrame)
			gl.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		else
			gl.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		
		gl.glDrawArrays(GL_PATCHES, 0, 16);
		
		gl.glUseProgram(cageProgram);
		gl.glUniformMatrix4fv(mvpLocation, 1, false, projMatrix.mult(mvMatrix).toArray(), 0);
		
		if(showContrlPoints) {
			float[] currentPointSize = new float[1];
			gl.glGetFloatv(GL_POINT_SIZE, currentPointSize, 0);
			
			FloatBuffer colorBuf = FloatBuffer.allocate(4);
			colorBuf.put(0, 0.2f);
			colorBuf.put(1, 0.7f);
			colorBuf.put(2, 0.9f);
			colorBuf.put(3, 1.0f);
			
			gl.glPointSize(9.0f);
			gl.glUniform4fv(drawColor, 1, colorBuf);
			gl.glDrawArrays(GL_POINTS, 0, 16);
			
			gl.glPointSize(currentPointSize[0]);
		}
		
		if(showControlCage) {
			FloatBuffer colorBuf = FloatBuffer.allocate(4);
			colorBuf.put(0, 0.7f);
			colorBuf.put(1, 0.9f);
			colorBuf.put(2, 0.2f);
			colorBuf.put(3, 1.0f);
			
			gl.glUniform4fv(drawColor, 1, colorBuf);
			gl.glDrawElements(GL_LINES, 48, GL_UNSIGNED_SHORT, 0);
		}
		
		gl.glLineWidth(currentLineWidth[0]);
	}

	@Override
	protected void startup(GL4 gl) {
		//set vertices per patch to 16
		gl.glPatchParameteri(GL_PATCH_VERTICES, 16);
		
		int program = getProgram();
		
		mvMatLocation = gl.glGetUniformLocation(program, "mv_matrix");
		projMatLocation = gl.glGetUniformLocation(program, "proj_matrix");
		tessFactorLocation = gl.glGetUniformLocation(program, "tess_factor");
		
		int cageVShader = GLUtils.compileShader(GLUtils.readShaderSource(
									this.getClass().getResourceAsStream("/shaders/chapter8/vshader_8_2_CONTROL_CAGE.glsl")),
								gl, ShaderType.VERTEX_SHADER);
		
		int cageFShader = GLUtils.compileShader(GLUtils.readShaderSource(
									this.getClass().getResourceAsStream("/shaders/chapter8/fshader_8_2_CONTROL_CAGE.glsl")),
								gl, ShaderType.FRAGMENT_SHADER);
		
		cageProgram = GLUtils.compileProgram(new Integer[]{cageVShader, cageFShader}, gl);
		GLUtils.printProgramLog(gl, cageProgram);
		
		gl.glDeleteShader(cageVShader);
		gl.glDeleteShader(cageFShader);
		
		mvpLocation = gl.glGetUniformLocation(cageProgram, "mvp_matrix");
		drawColor = gl.glGetUniformLocation(cageProgram, "draw_color");
		
		int buffer[] = new int[2];
		gl.glGenBuffers(buffer.length, buffer, 0);
		patchBuffer = buffer[0];
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, patchBuffer);
		gl.glBufferData(GL_ARRAY_BUFFER, PATCH_INITIALIZER.length * Float.SIZE / 8, null, GL_DYNAMIC_DRAW);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		cageIndices = buffer[1];
		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, cageIndices);
		gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, CAGE_INDICES.length * 2, ShortBuffer.wrap(CAGE_INDICES), GL_STATIC_DRAW);
	}

	@Override
	protected void shutdown(GL4 gl) {
		gl.glDeleteProgram(cageProgram);
		gl.glDeleteBuffers(2, new int[]{patchBuffer, cageIndices}, 0);
	}

	@Override
	protected void resize(GL4 gl, int x, int y, int width, int height) {
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		super.keyPressed(e);
		
		switch (e.character) {
		case 'w':
		case 'W':
			wireFrame = !wireFrame;
			break;
		case 'x':
		case 'X':
			showContrlPoints = !showContrlPoints;
			break;
		case 'c':
		case 'C':
			showControlCage = !showControlCage;
			break;
		default:
			break;
		}
	}
	
	@Override
	protected String[] getShaderSourceLines(ShaderType shaderType) {
		switch (shaderType) {
		case VERTEX_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter8/vshader_8_2.glsl"));
		case TESS_CONTROL_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter8/tcshader_8_2.glsl"));
		case TESS_EVALUATION_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter8/teshader_8_2.glsl"));
		case FRAGMENT_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter8/fshader_8_2.glsl"));
		default:
			return super.getShaderSourceLines(shaderType);
		}
	}
	
	private static final float PATCH_INITIALIZER[] =
    {
        -1.0f,  -1.0f,  0.0f,
        -0.33f, -1.0f,  0.0f,
         0.33f, -1.0f,  0.0f,
         1.0f,  -1.0f,  0.0f,

        -1.0f,  -0.33f, 0.0f,
        -0.33f, -0.33f, 0.0f,
         0.33f, -0.33f, 0.0f,
         1.0f,  -0.33f, 0.0f,

        -1.0f,   0.33f, 0.0f,
        -0.33f,  0.33f, 0.0f,
         0.33f,  0.33f, 0.0f,
         1.0f,   0.33f, 0.0f,

        -1.0f,   1.0f,  0.0f,
        -0.33f,  1.0f,  0.0f,
         0.33f,  1.0f,  0.0f,
         1.0f,   1.0f,  0.0f,
    };
	
	private static final short CAGE_INDICES[] =
    {
        0, 1, 1, 2, 2, 3,
        4, 5, 5, 6, 6, 7,
        8, 9, 9, 10, 10, 11,
        12, 13, 13, 14, 14, 15,

        0, 4, 4, 8, 8, 12,
        1, 5, 5, 9, 9, 13,
        2, 6, 6, 10, 10, 14,
        3, 7, 7, 11, 11, 15
    };
}
