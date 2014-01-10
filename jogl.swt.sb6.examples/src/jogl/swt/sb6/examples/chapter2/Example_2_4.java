package jogl.swt.sb6.examples.chapter2;

import static javax.media.opengl.GL4.*;

import javax.media.opengl.GL4;

import jogl.swt.utils.GLUtils;
import jogl.swt.utils.GLUtils.ShaderType;
import jogl.swt.utils.views.JOGLView;

public class Example_2_4 extends JOGLView {
	@Override
	protected void render(GL4 gl) {
		gl.glDrawArrays(GL_TRIANGLES, 0, 3);
	}

	@Override
	protected void startup(GL4 gl) {
	}

	@Override
	protected void shutdown(GL4 gl) {
	}
	
	@Override
	protected String[] getShaderSourceLines(ShaderType shaderType) {
		if(shaderType == ShaderType.VERTEX_SHADER)
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter2/vshader_2_4.glsl"));
		else if(shaderType == ShaderType.FRAGMENT_SHADER)
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter2/fshader_2_4.glsl"));
		else
			return super.getShaderSourceLines(shaderType);
	}
	
	@Override
	protected void resize(GL4 gl, int x, int y, int width, int height) {
	}
}
