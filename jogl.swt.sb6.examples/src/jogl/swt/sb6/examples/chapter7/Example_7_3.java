package jogl.swt.sb6.examples.chapter7;

import static javax.media.opengl.GL4.*;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.media.opengl.GL4;

import org.eclipse.swt.events.KeyEvent;

import jogl.swt.utils.DrawArraysIndirectCommand;
import jogl.swt.utils.GLUtils;
import jogl.swt.utils.GLUtils.ShaderType;
import jogl.swt.utils.math.Matrix4f;
import jogl.swt.utils.math.MatrixUtils;
import jogl.swt.utils.math.Vec3f;
import jogl.swt.utils.sb6m.SB6MObject;
import jogl.swt.utils.sb6m.SB6MSubObject;
import jogl.swt.utils.views.JOGLView;

public class Example_7_3 extends JOGLView {
	private static final int NUM_DRAWS = 50000;
	
	@Override
	public void keyPressed(KeyEvent e) {
		super.keyPressed(e);
		
		if(e.character == 'M' || e.character == 'm') {
			multiDraw = !multiDraw;
			if(multiDraw)
				System.out.println("Now using multi-indirect draw");
			else
				System.out.println("Now using single-direct draws");
		}
	}
	
	@Override
	protected void render(GL4 gl) {
		float time = getApplicationTime() / 1000.0f;
		gl.glUniform1f(timeLocation, time);
		
		Matrix4f viewMatrix = MatrixUtils.lookAt(
				new Vec3f(100.0f * (float) Math.cos(time * 0.023f), 100.0f * (float) Math.cos(time * 0.023f), 300.0f * (float) Math.sin(time * 0.037f) - 600.0f),
				new Vec3f(0.0f, 0.0f, 260.0f),
				new Vec3f(0.1f - (float) Math.cos(time * 0.1f) * 0.3f, 1.0f, 0.0f).normalize());
		gl.glUniformMatrix4fv(viewMatrixLocation, 1, false, viewMatrix.toArray(), 0);
		
		Matrix4f projectionMatrix = MatrixUtils.perspective(50.0, (double)getWidth() / (double)getHeight(), 1.0, 2000.0);
		
		gl.glUniformMatrix4fv(viewprojMatrixLocation, 1, false,
				Matrix4f.multiplyAll(projectionMatrix, viewMatrix).toArray(), 0);
		
		//Make sure we are using the object vao
		gl.glBindVertexArray(object.getVao());
		
		if(multiDraw) {
			gl.glMultiDrawArraysIndirect(GL_TRIANGLES, null, NUM_DRAWS, 0);
		}
		else {
			for(int i = 0; i < NUM_DRAWS; i++) {
				SB6MSubObject subObject = object.getSubObjectInfo(i % object.getSubObjectCount());
				gl.glDrawArraysInstancedBaseInstance(GL_TRIANGLES, subObject.getFirst(), subObject.getCount(),
						1, i);
			}
		}
	}

	@Override
	protected void startup(GL4 gl) {
		int program = getProgram();
		
		//draw_id attribute location
		drawIdLocation = gl.glGetAttribLocation(program, "draw_id");
		
		//uniform locations
		timeLocation = gl.glGetUniformLocation(program, "time");
		viewMatrixLocation = gl.glGetUniformLocation(program, "view_matrix");
		viewprojMatrixLocation = gl.glGetUniformLocation(program, "viewproj_matrix");
		
		try {
			object = SB6MObject.load(this.getClass().getResourceAsStream("/objects/asteroids.sbm"), gl);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int[] buffers = new int[2];
		gl.glGenBuffers(buffers.length, buffers, 0);
		indirectDrawBuffer = buffers[0];
		
		gl.glBindBuffer(GL_DRAW_INDIRECT_BUFFER, indirectDrawBuffer);
		gl.glBufferData(GL_DRAW_INDIRECT_BUFFER,
						NUM_DRAWS * DrawArraysIndirectCommand.SIZE_IN_BYTES,
						null,
						GL_STATIC_DRAW);
		
		ByteBuffer buf = gl.glMapBufferRange(GL_DRAW_INDIRECT_BUFFER,
							0,
							NUM_DRAWS * DrawArraysIndirectCommand.SIZE_IN_BYTES,
							GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
		
		for(int i = 0 ; i < NUM_DRAWS; i++) {
			SB6MSubObject subObject = object.getSubObjectInfo(i % object.getSubObjectCount());
			
			DrawArraysIndirectCommand cmd = new DrawArraysIndirectCommand(subObject.getCount(), 1, subObject.getFirst(), i);
			cmd.writeToBuffer(buf);
		}
		
		boolean unmapped = gl.glUnmapBuffer(GL_DRAW_INDIRECT_BUFFER);
		if (!unmapped)
			System.out.println("Something wrong");
		
		//Make sure we are binding the object vao. This is required to set the drawId attrib pointer in the same vao to be rendered later.
		gl.glBindVertexArray(object.getVao());
		
		drawIdBuffer = buffers[1];
		gl.glBindBuffer(GL_ARRAY_BUFFER, drawIdBuffer);
		gl.glBufferData(GL_ARRAY_BUFFER,
						NUM_DRAWS * 4,
						null,
						GL_STATIC_DRAW);
		
		buf = gl.glMapBufferRange(GL_ARRAY_BUFFER,
								  0,
								  NUM_DRAWS * 4,
								  GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
		
		for(int i = 0; i < NUM_DRAWS; i++) {
			buf.putInt(i);
		}
		
		unmapped = gl.glUnmapBuffer(GL_ARRAY_BUFFER);
		if (!unmapped)
			System.out.println("Something wrong");
		
		gl.glVertexAttribIPointer(drawIdLocation,
								  1,
								  GL_UNSIGNED_INT,
								  0,
								  0);
		//IMPORTANT: every instance (not every vertex) will get a different draw_id value
		gl.glVertexAttribDivisor(drawIdLocation, 1);
		gl.glEnableVertexAttribArray(drawIdLocation);
	}

	@Override
	protected void shutdown(GL4 gl) {
		object.free(gl);
		gl.glDeleteBuffers(2, new int[]{indirectDrawBuffer, drawIdBuffer}, 0);
	}

	@Override
	protected void resize(GL4 gl, int x, int y, int width, int height) {
	}
	
	@Override
	protected String[] getShaderSourceLines(ShaderType shaderType) {
		switch (shaderType) {
		case VERTEX_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter7/vshader_7_3.glsl"));
		case FRAGMENT_SHADER:
			return GLUtils.readShaderSource(this.getClass().getResourceAsStream("/shaders/chapter7/fshader_7_3.glsl"));
		default:
			return super.getShaderSourceLines(shaderType);
		}
	}
	
	private volatile boolean multiDraw = true;
	
	private SB6MObject object;
	private int indirectDrawBuffer;
	private int drawIdBuffer;
	private int drawIdLocation;
	
	/* Uniforms */
	private int timeLocation;
	private int viewMatrixLocation;
	private int viewprojMatrixLocation;
}
