package jogl.swt.sb6.examples.chapter7;

import java.io.IOException;
import java.nio.FloatBuffer;

import static javax.media.opengl.GL4.*;

import javax.media.opengl.GL4;

import jogl.swt.utils.GLUtils;
import jogl.swt.utils.GLUtils.ProjectionType;
import jogl.swt.utils.GLUtils.ShaderType;
import jogl.swt.utils.math.Matrix4f;
import jogl.swt.utils.math.MatrixUtils;
import jogl.swt.utils.math.Vec4f;
import jogl.swt.utils.sb6m.SB6MObject;
import jogl.swt.utils.views.JOGLView;

public class Example_7_5 extends JOGLView {

	@Override
	protected void render(GL4 gl) {
		float time = getApplicationTime() / 1000.0f;
		
		Matrix4f projectionMatrix = getDefaultProjectionMatrix(ProjectionType.PERSPECTIVE);
		Matrix4f mvMatrix = getLookAtMatrix();
		
		gl.glUniformMatrix4fv(projMatLocation, 1, false, projectionMatrix.toArray(), 0);
		gl.glUniformMatrix4fv(mvMatLocation, 1, false, mvMatrix.toArray(), 0);
		
		Matrix4f planeMatrix = MatrixUtils.rotateDegree(time  * 6.0f, 1.0f, 0.0f, 0.0f).mult(
				MatrixUtils.rotateDegree(time * 7.3f, 0.0f, 1.0f, 0.0f));
		
		Vec4f plane = new Vec4f(planeMatrix.m00, planeMatrix.m10, planeMatrix.m20, 0.0f).normalize();
		
		FloatBuffer buf = FloatBuffer.allocate(4);
		plane.append(buf);
		buf.position(0);
		gl.glUniform4fv(clipPlaneLocaiton, 1, buf);
		
		Vec4f sphere = new Vec4f( (float) Math.sin(time * 0.7f) * 3.0f, //sphere center x
						(float) Math.cos(time * 1.9f) * 3.0f, 			//sphere center y
						(float) Math.sin(time * 0.1f) * 3.0f, 			//sphere center z
						(float) Math.cos(time * 1.7f) + 2.5f);			//sphere radius
		buf.position(0);
		sphere.append(buf);
		buf.position(0);
		gl.glUniform4fv(clipSphereLocation, 1, buf);
		
		//enable GLSL gl_ClipDistance[] array's indices 0 and 1 (we will store the two custom distances there)
		gl.glEnable(GL_CLIP_DISTANCE0);
		gl.glEnable(GL_CLIP_DISTANCE1);
		
		/*
		  if back/front face culling is enabled (glEnable(GL_CULL_FACE);) we need to trigger two draws changing the winding order each time)
		
				gl.glFrontFace(GL_CW);
				object.render(gl);
				
				gl.glFrontFace(GL_CCW);
				object.render(gl);
		*/
		object.render(gl);
	}

	@Override
	protected void startup(GL4 gl) {
		try {
			object = SB6MObject.load(this.getClass().getResourceAsStream("/objects/dragon.sbm"), gl);
			
			int program = getProgram();
			
			projMatLocation = gl.glGetUniformLocation(program, "proj_matrix");
			mvMatLocation = gl.glGetUniformLocation(program, "mv_matrix");
			clipPlaneLocaiton = gl.glGetUniformLocation(program, "clip_plane");
			clipSphereLocation = gl.glGetUniformLocation(program, "clip_sphere");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void shutdown(GL4 gl) {
		object.free(gl);
	}

	@Override
	protected void resize(GL4 gl, int x, int y, int width, int height) {
	}
	
	@Override
	protected String[] getShaderSourceLines(ShaderType shaderType) {
		switch (shaderType) {
		case VERTEX_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter7/vshader_7_5.glsl"));
		case FRAGMENT_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter7/fshader_7_5.glsl"));
		default:
			return super.getShaderSourceLines(shaderType);
		}
	}
	
	private SB6MObject object;
	private int projMatLocation;
	private int mvMatLocation;
	private int clipPlaneLocaiton;
	private int clipSphereLocation;
}
