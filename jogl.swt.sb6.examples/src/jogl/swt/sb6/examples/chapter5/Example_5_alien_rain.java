package jogl.swt.sb6.examples.chapter5;

import static javax.media.opengl.GL4.*;

import java.nio.ByteBuffer;

import javax.media.opengl.GL4;

import jogl.swt.utils.GLUtils;
import jogl.swt.utils.KTXUtils;
import jogl.swt.utils.GLUtils.ShaderType;
import jogl.swt.utils.views.JOGLView;

public class Example_5_alien_rain extends JOGLView {
	
	private int tex_alien_array;
	private int rain_buffer;

	private float[] droplet_x_offset = new float[256];
	private float[] droplet_rot_speed = new float[256];
	private float[] droplet_fall_speed = new float[256];
	private int alienIndexLoc;

	public Example_5_alien_rain() {
	}

	@Override
	protected void render(GL4 gl) {
		float t = (float) (getApplicationTime() / 1000.0f);
		
		gl.glBindBufferBase(GL_UNIFORM_BUFFER, 0, rain_buffer);
		ByteBuffer uniformBuffer = gl.glMapBufferRange(GL_UNIFORM_BUFFER,
				0,
				256 * 4 * 4,
				GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
		for(int i = 0; i < 256; i++) {
			float x = droplet_x_offset[i];
			float y = 2.0f - (((t + i) * droplet_fall_speed[i]) % 4.31f); //fmodf((t + float(i)) * droplet_fall_speed[i], 4.31f));
			float orientation = t * droplet_rot_speed[i];
			float unused = 0.0f;
			uniformBuffer.putFloat(x);
			uniformBuffer.putFloat(y);
			uniformBuffer.putFloat(orientation);
			uniformBuffer.putFloat(unused);
		}
		gl.glUnmapBuffer(GL_UNIFORM_BUFFER);
		
		for(int alien_index = 0; alien_index < 256; alien_index++) {
			gl.glVertexAttribI1i(alienIndexLoc, alien_index);
			gl.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
		}
	}

	@Override
	protected void startup(GL4 gl) {
		tex_alien_array = KTXUtils.loadKTXTexture(this.getClass().getResourceAsStream("/textures/aliens.ktx"), gl);
		gl.glBindTexture(GL_TEXTURE_2D_ARRAY, tex_alien_array);
		
		int[] tmp = new int[1];
		gl.glGenBuffers(1, tmp, 0);
		rain_buffer = tmp[0];
		gl.glBindBuffer(GL_UNIFORM_BUFFER, rain_buffer);
		gl.glBufferData(GL_UNIFORM_BUFFER, 256 * 4 * 4, null, GL_DYNAMIC_DRAW);
		
		for(int i = 0; i < 256; i++) {
			droplet_x_offset[i] = random_float() * 2.0f - 1.0f;
			droplet_rot_speed[i] = (random_float() + 0.5f) * (((i & 1) != 0) ? -3.0f : 3.0f);
			droplet_fall_speed[i] = random_float() + 0.2f;
		}
		
		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		alienIndexLoc = gl.glGetAttribLocation(getProgram(), "alien_index");
	}
	
	@Override
	protected String[] getShaderSourceLines(ShaderType shaderType) {
		if(shaderType == ShaderType.VERTEX_SHADER)
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter5/vshader_5_alienrain.glsl"));
		else if(shaderType == ShaderType.FRAGMENT_SHADER)
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter5/fshader_5_alienrain.glsl"));
		else
			return super.getShaderSourceLines(shaderType);
	}

	@Override
	protected void shutdown(GL4 gl) {
		gl.glDeleteTextures(1, new int[]{tex_alien_array}, 0);
	}

	@Override
	protected void resize(GL4 gl, int x, int y, int width, int height) {
	}
	
	// Random number generator
	private static long seed = 0x13371337L;

	private static float random_float()
	{
	    float res;
	    long tmp;
	    
	    long tmpSeed = seed * 16807;

	    seed = tmpSeed & 0xFFFFFFFFL;

	    tmp = seed ^ (seed >>> 4) ^ (seed << 15);
	    tmp = tmp & 0xFFFFFFFFL;

	    res = Float.intBitsToFloat((int)((tmp >> 9) | 0x3F800000L));

	    return (res - 1.0f);
	}

}
