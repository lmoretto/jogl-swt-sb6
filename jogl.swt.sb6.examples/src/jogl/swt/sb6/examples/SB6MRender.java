package jogl.swt.sb6.examples;

import static javax.media.opengl.GL4.*;

import java.io.IOException;

import javax.media.opengl.GL4;

import jogl.swt.utils.GLUtils;
import jogl.swt.utils.GLUtils.ProjectionType;
import jogl.swt.utils.GLUtils.ShaderType;
import jogl.swt.utils.KTXUtils;
import jogl.swt.utils.math.Matrix4f;
import jogl.swt.utils.math.MatrixUtils;
import jogl.swt.utils.sb6m.SB6MObject;
import jogl.swt.utils.views.JOGLView;

public class SB6MRender extends JOGLView {

	@Override
	protected void render(GL4 gl) {
		float green[] = { 0.0f, 0.25f, 0.0f, 1.0f };
		gl.glClearBufferfv(GL_COLOR, 0, green, 0);
		
		float currentTime = getApplicationTime() / 1000.0f;
		
		Matrix4f perspective = getDefaultProjectionMatrix(ProjectionType.PERSPECTIVE);
		gl.glUniformMatrix4fv(projLocation, 1, false, perspective.toArray(), 0);
		
		Matrix4f mvMatrix = Matrix4f.multiplyAll(
				getLookAtMatrix(),
				MatrixUtils.translate(0.0f, -0.5f, -7.0f),
				MatrixUtils.rotateDegree(currentTime * 5.0f, 0.0f, 1.0f, 0.0f),
				Matrix4f.identity);
		gl.glUniformMatrix4fv(mvLocation, 1, false, mvMatrix.toArray(), 0);
		
		object.render(gl);
	}

	@Override
	protected void startup(GL4 gl) {
		int program = getProgram();
		
		mvLocation = gl.glGetUniformLocation(program, "mv_matrix");
		projLocation = gl.glGetUniformLocation(program, "proj_matrix");
		
		try {
			object = SB6MObject.load(this.getClass().getResourceAsStream("/objects/ladybug.sbm"), gl);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		
		int[] textures = new int[2];
		gl.glGenTextures(2, textures, 0);
		texColor = textures[0];
		texNormal = textures[1];
		
		gl.glActiveTexture(GL_TEXTURE0);
		KTXUtils.loadKTXTexture(this.getClass().getResourceAsStream("/textures/ladybug_co.ktx"), gl, texColor);
		
		gl.glActiveTexture(GL_TEXTURE1);
		KTXUtils.loadKTXTexture(this.getClass().getResourceAsStream("/textures/ladybug_nm.ktx"), gl, texNormal);
	}

	@Override
	protected void shutdown(GL4 gl) {
		if(object != null)
			object.free(gl);
		
		gl.glDeleteTextures(1, new int[]{texColor}, 0);
		gl.glDeleteTextures(1, new int[]{texNormal}, 0);
	}

	@Override
	protected void resize(GL4 gl, int x, int y, int width, int height) {
	}
	
	@Override
	protected String[] getShaderSourceLines(ShaderType shaderType) {
		switch (shaderType) {
		case VERTEX_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/sb6mrender.vs.glsl"));
		case FRAGMENT_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/sb6mrender.fs.glsl"));
		default:
			return super.getShaderSourceLines(shaderType);
		}
	}
	
	private int mvLocation;
	private int projLocation;
	
	private int texColor;
	private int texNormal;
	private SB6MObject object;
}
