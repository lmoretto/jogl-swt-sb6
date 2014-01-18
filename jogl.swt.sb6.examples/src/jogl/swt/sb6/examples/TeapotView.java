package jogl.swt.sb6.examples;

import static javax.media.opengl.GL.GL_LINE_WIDTH;
import static javax.media.opengl.GL4.*;

import javax.media.opengl.GL4;

import org.eclipse.swt.events.KeyEvent;

import jogl.swt.sb6.examples.teapot.Teapot;
import jogl.swt.utils.GLUtils;
import jogl.swt.utils.GLUtils.ProjectionType;
import jogl.swt.utils.GLUtils.ShaderType;
import jogl.swt.utils.math.Matrix4f;
import jogl.swt.utils.math.MatrixUtils;
import jogl.swt.utils.views.JOGLView;

public class TeapotView extends JOGLView {

	@Override
	protected void render(GL4 gl) {
		gl.glDisable(GL_CULL_FACE);
		
		Matrix4f mvMatrix = getLookAtMatrix().mult(MatrixUtils.rotateDegree(90.0f, 1.0f, 0.0f, 0.0f));
		Matrix4f mvpMatrix = getDefaultProjectionMatrix(ProjectionType.PERSPECTIVE).mult(mvMatrix);
		
		gl.glUniformMatrix4fv(mvMatLocation, 1, false, mvMatrix.toArray(), 0);
		gl.glUniformMatrix4fv(mvpMatLocation, 1, false, mvpMatrix.toArray(), 0);
		
		float tessFactor = 16.0f;
		
		gl.glUniform1f(tessFactorLocation, tessFactor);
		
		float[] currentLineWidth = new float[1];
		gl.glGetFloatv(GL_LINE_WIDTH, currentLineWidth, 0);
		gl.glLineWidth(1.0f);
		
		if(wireframe)
			gl.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		else
			gl.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		
		teapot.render(gl);
		
		gl.glLineWidth(currentLineWidth[0]);
	}

	@Override
	protected void startup(GL4 gl) {
		int program = getProgram();
		
		teapot = new Teapot(gl);
		
		mvMatLocation = gl.glGetUniformLocation(program, "mv_matrix");
		mvpMatLocation = gl.glGetUniformLocation(program, "mvp_matrix");
		tessFactorLocation = gl.glGetUniformLocation(program, "tess_factor");
	}

	@Override
	protected void shutdown(GL4 gl) {
		teapot.free(gl);
	}
	
	@Override
	protected String[] getShaderSourceLines(ShaderType shaderType) {
		switch (shaderType) {
		case VERTEX_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/teapot/vshader_teapot.glsl"));
		case TESS_CONTROL_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/teapot/tcshader_teapot.glsl"));
		case TESS_EVALUATION_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/teapot/teshader_teapot.glsl"));
		case FRAGMENT_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/teapot/fshader_teapot.glsl"));
		default:
			return super.getShaderSourceLines(shaderType);
		}
	}

	@Override
	protected void resize(GL4 gl, int x, int y, int width, int height) {
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		super.keyPressed(e);
		
		if(e.character == 'w' || e.character == 'W')
			wireframe = !wireframe;
	}
	
	private Teapot teapot;
	private boolean wireframe = false;
	
	private int mvMatLocation;
	private int mvpMatLocation;
	private int tessFactorLocation;
}
