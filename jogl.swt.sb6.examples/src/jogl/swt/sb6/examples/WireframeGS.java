package jogl.swt.sb6.examples;

import static javax.media.opengl.GL.GL_LINE_WIDTH;
import static javax.media.opengl.GL4.*;

import java.io.IOException;

import javax.media.opengl.GL4;

import org.eclipse.swt.events.KeyEvent;

import jogl.swt.utils.GLUtils;
import jogl.swt.utils.GLUtils.ProjectionType;
import jogl.swt.utils.GLUtils.ShaderType;
import jogl.swt.utils.math.Matrix4f;
import jogl.swt.utils.math.MatrixUtils;
import jogl.swt.utils.sb6m.SB6MObject;
import jogl.swt.utils.views.JOGLView;

public class WireframeGS extends JOGLView {
	//no wireframe
	private int nowfProgram;
	private int nowfMvMatLocation;
	private int nowfMvpMatLocation;
	
	//basic wireframe
	private int basicwfProgram;
	private int basicwfMvpMatLocation;
	
	//geometry shader wireframe
	private int gswfMvMatLocation;
	private int gswfMvpMatLocation;
	private int gswfViewportMatLocation;
	
	private boolean gsWireFrame = false;
	
	private SB6MObject object;
	private Matrix4f viewportMatrix;

	@Override
	protected void render(GL4 gl) {
		Matrix4f mvMatrix = getLookAtMatrix();
		Matrix4f projMatrix = getDefaultProjectionMatrix(ProjectionType.PERSPECTIVE);
		Matrix4f mvpMatrix = projMatrix.mult(mvMatrix);
		
		if(gsWireFrame) {
			gl.glUseProgram(getProgram());
			
			gl.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
			
			gl.glUniformMatrix4fv(gswfMvMatLocation, 1, false, mvMatrix.toArray(), 0);
			gl.glUniformMatrix4fv(gswfMvpMatLocation, 1, false, mvpMatrix.toArray(), 0);
			gl.glUniformMatrix4fv(gswfViewportMatLocation, 1, false, viewportMatrix.toArray(), 0);
			
			object.render(gl);
		}
		else {
			gl.glUseProgram(nowfProgram);
			
			float[] currentLineWidth = new float[1];
			gl.glGetFloatv(GL_LINE_WIDTH, currentLineWidth, 0);
			gl.glLineWidth(1.0f);
			
			gl.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
			
			gl.glUniformMatrix4fv(nowfMvMatLocation, 1, false, mvMatrix.toArray(), 0);
			gl.glUniformMatrix4fv(nowfMvpMatLocation, 1, false, mvpMatrix.toArray(), 0);
			
			object.render(gl);
			
			//wire frame
			gl.glUseProgram(basicwfProgram);
			
			gl.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
			
			gl.glUniformMatrix4fv(basicwfMvpMatLocation, 1, false, mvpMatrix.toArray(), 0);
			object.render(gl);
			
			gl.glLineWidth(currentLineWidth[0]);
		}
	}

	@Override
	protected void startup(GL4 gl) {
		try {
			object = SB6MObject.load(this.getClass().getResourceAsStream("/objects/bunny_1k.sbm"), gl);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//no wireframe program
		int vShader = GLUtils.compileShader(GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/vshader_diffuse.glsl")), gl, ShaderType.VERTEX_SHADER);
		int fShader = GLUtils.compileShader(GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/fshader_diffuse.glsl")), gl, ShaderType.FRAGMENT_SHADER);
		
		nowfProgram = GLUtils.compileProgram(new Integer[]{vShader, fShader}, gl);
		
		gl.glDeleteShader(vShader);
		gl.glDeleteShader(fShader);

		nowfMvMatLocation = gl.glGetUniformLocation(nowfProgram, "mv_matrix");
		nowfMvpMatLocation = gl.glGetUniformLocation(nowfProgram, "mvp_matrix");
		
		//basic wireframe program
		vShader = GLUtils.compileShader(GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/wireframe/vshader_passthrough.glsl")), gl, ShaderType.VERTEX_SHADER);
		fShader = GLUtils.compileShader(GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/wireframe/fshader_basicwireframe.glsl")), gl, ShaderType.FRAGMENT_SHADER);
		
		basicwfProgram = GLUtils.compileProgram(new Integer[]{vShader, fShader}, gl);
		
		gl.glDeleteShader(vShader);
		gl.glDeleteShader(fShader);
		
		basicwfMvpMatLocation = gl.glGetUniformLocation(basicwfProgram, "mvp_matrix");
		
		//geometry shader wireframe program
		int gswfProgram = getProgram();

		gswfMvMatLocation = gl.glGetUniformLocation(gswfProgram, "mv_matrix");
		gswfMvpMatLocation = gl.glGetUniformLocation(gswfProgram, "mvp_matrix");
		gswfViewportMatLocation = gl.glGetUniformLocation(gswfProgram, "viewport_matrix");
		
		gl.glEnable(GL_CULL_FACE);
	}

	@Override
	protected void shutdown(GL4 gl) {
		object.free(gl);
		gl.glDeleteProgram(basicwfProgram);
		gl.glDeleteProgram(nowfProgram);
	}

	@Override
	protected void resize(GL4 gl, int x, int y, int width, int height) {
		viewportMatrix = MatrixUtils.viewport(x, y, width, height);
	}
	
	@Override
	protected String[] getShaderSourceLines(ShaderType shaderType) {
		switch (shaderType) {
		case VERTEX_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/wireframe/with_geometry_shader/vshader.glsl"));
		case GEOMETRY_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/wireframe/with_geometry_shader/gshader.glsl"));
		case FRAGMENT_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/wireframe/with_geometry_shader/fshader.glsl"));
		default:
			return super.getShaderSourceLines(shaderType);
		}
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		super.keyPressed(e);
		
		if(e.character == 'g' || e.character == 'G') {
			gsWireFrame = !gsWireFrame;
			
			if(gsWireFrame)
				System.out.println("Using geometry shader wireframe");
			else
				System.out.println("Using standard wireframe");
		}
	}

}
