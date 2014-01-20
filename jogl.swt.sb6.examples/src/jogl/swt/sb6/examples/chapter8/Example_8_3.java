package jogl.swt.sb6.examples.chapter8;

import static javax.media.opengl.GL4.*;

import java.io.IOException;

import javax.media.opengl.GL4;

import jogl.swt.utils.GLUtils;
import jogl.swt.utils.GLUtils.ProjectionType;
import jogl.swt.utils.GLUtils.ShaderType;
import jogl.swt.utils.math.Matrix4f;
import jogl.swt.utils.sb6m.SB6MObject;
import jogl.swt.utils.views.JOGLView;

public class Example_8_3 extends JOGLView {
	private int viewpointLocation;
	private int mvMatLocation;
	private int mvpMatLocation;
	
	private SB6MObject bunny;

	@Override
	protected void render(GL4 gl) {
		gl.glDisable(GL_CULL_FACE);

		Matrix4f projMatrix = getDefaultProjectionMatrix(ProjectionType.PERSPECTIVE);
		Matrix4f viewMatrix = getLookAtMatrix();
		
		gl.glUniformMatrix4fv(mvMatLocation, 1, false, viewMatrix.toArray(), 0);
		gl.glUniformMatrix4fv(mvpMatLocation, 1, false, projMatrix.mult(viewMatrix).toArray(), 0);
		
		float f = getApplicationTime() / 1000.0f;
		float[] viewPoint = {
			(float) (Math.sin(f * 2.1f) * 70.0f),
			(float) (Math.cos(f * 1.4f) * 70.0f),
			(float) (Math.sin(f * 0.7f) * 70.0f)
		};
		
		gl.glUniform3fv(viewpointLocation, 1, viewPoint, 0);
		
		bunny.render(gl);
	}

	@Override
	protected void startup(GL4 gl) {
		int program = getProgram();
		
		viewpointLocation = gl.glGetUniformLocation(program, "viewpoint");
		mvMatLocation = gl.glGetUniformLocation(program, "mv_matrix");
		mvpMatLocation = gl.glGetUniformLocation(program, "mvp_matrix");
		
		try {
			bunny = SB6MObject.load(this.getClass().getResourceAsStream("/objects/bunny_1k.sbm"), gl);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void shutdown(GL4 gl) {
		bunny.free(gl);
	}

	@Override
	protected void resize(GL4 gl, int x, int y, int width, int height) {
	}
	
	@Override
	protected String[] getShaderSourceLines(ShaderType shaderType) {
		switch (shaderType) {
		case VERTEX_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter8/vshader_8_3.glsl"));
		case GEOMETRY_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter8/gshader_8_3.glsl"));
		case FRAGMENT_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter8/fshader_8_3.glsl"));
		default:
			return super.getShaderSourceLines(shaderType);
		}
	}

}
