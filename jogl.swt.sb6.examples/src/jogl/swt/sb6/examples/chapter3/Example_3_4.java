package jogl.swt.sb6.examples.chapter3;

import static javax.media.opengl.GL4.*;

import java.nio.FloatBuffer;

import javax.media.opengl.GL4;

import jogl.swt.utils.GLUtils;
import jogl.swt.utils.GLUtils.ShaderType;
import jogl.swt.utils.views.JOGLView;

public class Example_3_4 extends JOGLView {
	@Override
	protected void render(GL4 gl) {
		long currentTime = System.currentTimeMillis();
		
		FloatBuffer offset = FloatBuffer.allocate(4);
		offset.put(0, (float)(Math.sin(currentTime / 1000.0) * 0.5));
		offset.put(1, (float)(Math.cos(currentTime / 1000.0) * 0.6));
		offset.put(2, 0.0f);
		offset.put(3,  0.0f);
		
		FloatBuffer color = FloatBuffer.allocate(4);
		color.put(0, (float) (Math.sin(currentTime / 500.0) * 0.5 + 0.5));
		color.put(1, 0.0f);
		color.put(2, (float) (Math.cos(currentTime / 500.0) * 0.5 + 0.5));
		color.put(3, 1.0f);
		
		gl.glVertexAttrib4fv(0, offset);
		gl.glVertexAttrib4fv(1, color);
		
		gl.glDrawArrays(GL_PATCHES, 0, 3);
	}

	@Override
	protected void startup(GL4 gl) {
		gl.glPatchParameteri(GL_PATCH_VERTICES, 3);
		gl.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
	}

	@Override
	protected void shutdown(GL4 gl) {
	}
	
	@Override
	protected String[] getShaderSourceLines(ShaderType shaderType) {
		switch (shaderType) {
		case VERTEX_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter3/vshader_3_3.glsl"));
		case FRAGMENT_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter3/fshader_3_3.glsl"));
		case TESS_CONTROL_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter3/tess_control_shader_3_4.glsl"));
		case TESS_EVALUATION_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter3/tess_evaluation_shader_3_4.glsl"));
		default:
			return super.getShaderSourceLines(shaderType);
		}
	}

	@Override
	protected void resize(GL4 gl, int x, int y, int width, int height) {
	}
}
