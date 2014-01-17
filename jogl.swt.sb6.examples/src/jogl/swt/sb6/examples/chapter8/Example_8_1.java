package jogl.swt.sb6.examples.chapter8;

import static javax.media.opengl.GL4.*;

import javax.media.opengl.GL4;

import org.eclipse.swt.events.KeyEvent;

import jogl.swt.utils.GLUtils;
import jogl.swt.utils.KTXUtils;
import jogl.swt.utils.GLUtils.ShaderType;
import jogl.swt.utils.math.Matrix4f;
import jogl.swt.utils.math.MatrixUtils;
import jogl.swt.utils.math.Vec3f;
import jogl.swt.utils.views.JOGLView;

public class Example_8_1 extends JOGLView {
	private int heightMap;
	private int colorMap;
	private int mvpMatLocation;
	private int projMatLocation;
	private int tessFactorMultiplierLocation;
	private int dmapDepthLocation;
	
	private boolean enableDisplacement = true;
	private boolean wireFrame = false;

	@Override
	protected void render(GL4 gl) {
		gl.glEnable(GL_CULL_FACE);
		
		float t = getApplicationTime() / 1000.0f * 0.03f;
		float r = (float) (Math.sin(t * 5.37f) * 15.0f + 16.0f);
		float h = (float) (Math.cos(t * 4.79f) * 2.0f + 3.2f);
		
		Matrix4f mvMatrix = MatrixUtils.lookAt(new Vec3f((float) Math.sin(t) * r, h, (float) Math.cos(t) * r),
				new Vec3f(0.0f, 0.0f, 0.0f),
				new Vec3f(0.0f, 1.0f, 0.0f));
		
		Matrix4f projMatrix = MatrixUtils.perspective(60.0, (float)getWidth() / (float)getHeight(), 0.1f, 1000.0f);
		Matrix4f mvpMatrix = projMatrix.mult(mvMatrix);
		
		gl.glUniformMatrix4fv(mvpMatLocation, 1, false, mvpMatrix.toArray(), 0);
		gl.glUniformMatrix4fv(projMatLocation, 1, false, projMatrix.toArray(), 0);
		
		float tessFactMultiplier = 16.0f;
		float dmapDepth = 6.0f;
		
		gl.glUniform1f(tessFactorMultiplierLocation, tessFactMultiplier);
		gl.glUniform1f(dmapDepthLocation, enableDisplacement ? dmapDepth : 0.0f);
		
		if(wireFrame)
			gl.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		else
			gl.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		
		gl.glDrawArraysInstanced(GL_PATCHES, 0, 4, 64 * 64);
	}

	@Override
	protected void startup(GL4 gl) {
		//set vertices per patch to 4
		gl.glPatchParameteri(GL_PATCH_VERTICES, 4);
		
		int program = getProgram();
		
		mvpMatLocation = gl.glGetUniformLocation(program, "mvp_matrix");
		projMatLocation = gl.glGetUniformLocation(program, "proj_matrix");
		tessFactorMultiplierLocation = gl.glGetUniformLocation(program, "tess_mult");
		dmapDepthLocation = gl.glGetUniformLocation(program, "dmap_depth");
		
		gl.glActiveTexture(GL_TEXTURE0);
		heightMap = KTXUtils.loadKTXTexture(this.getClass().getResourceAsStream("/textures/terragen1.ktx"), gl);
		gl.glActiveTexture(GL_TEXTURE1);
		colorMap = KTXUtils.loadKTXTexture(this.getClass().getResourceAsStream("/textures/terragen_color.ktx"), gl);
	}

	@Override
	protected void shutdown(GL4 gl) {
		gl.glDeleteTextures(2, new int[]{heightMap, colorMap}, 0);
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
		case 'd':
		case 'D':
			enableDisplacement = !enableDisplacement;
			break;
		default:
			break;
		}
	}
	
	@Override
	protected String[] getShaderSourceLines(ShaderType shaderType) {
		switch (shaderType) {
		case VERTEX_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter8/vshader_8_1.glsl"));
		case TESS_CONTROL_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter8/tcshader_8_1.glsl"));
		case TESS_EVALUATION_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter8/teshader_8_1.glsl"));
		case FRAGMENT_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter8/fshader_8_1.glsl"));
		default:
			return super.getShaderSourceLines(shaderType);
		}
	}

}
