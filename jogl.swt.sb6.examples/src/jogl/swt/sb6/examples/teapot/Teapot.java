package jogl.swt.sb6.examples.teapot;

import static javax.media.opengl.GL4.*;

import java.nio.FloatBuffer;

import javax.media.opengl.GL4;

import jogl.swt.utils.math.Matrix4f;
import jogl.swt.utils.math.Vec3f;

public class Teapot {
	public Teapot(GL4 gl, int positionAttributeLocation) {
		int vertices = 32 * 16;
		float[] v = new float[vertices * 3];
		
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		
		gl.glGenBuffers(buffer.length, buffer, 0);
		
		generatePatches(v);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, buffer[0]);
		gl.glBufferData(GL_ARRAY_BUFFER, 3 * vertices * Float.SIZE / 8, FloatBuffer.wrap(v), GL_STATIC_DRAW);
		gl.glVertexAttribPointer(positionAttributeLocation, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(positionAttributeLocation);
		
		gl.glBindVertexArray(0);
	}
	
	public Teapot(GL4 gl) {
		this(gl, 0);
	}
	
	private static void generatePatches(float[] v) {
		int index[] = new int[]{0};
		
		//Build the different patches
		//RIM
		buildPatchReflect(0, v, index, true, true);
		
		//BODY
		buildPatchReflect(1, v, index, true, true);
	    buildPatchReflect(2, v, index, true, true);
		
		//LID
	    buildPatchReflect(3, v, index, true, true);
	    buildPatchReflect(4, v, index, true, true);
		
		//BOTTOM
	    buildPatchReflect(5, v, index, true, true);
		
		//HANDLE
	    buildPatchReflect(6, v, index, false, true);
	    buildPatchReflect(7, v, index, false, true);
		
		//SPOUT
	    buildPatchReflect(8, v, index, false, true);
	    buildPatchReflect(9, v, index, false, true);
	}
	
	private static void buildPatchReflect(int patchNum,
	           float[] v, int[] index, boolean reflectX, boolean reflectY) {
		Vec3f[][] patch = new Vec3f[4][4];
		Vec3f[][] patchRevV = new Vec3f[4][4];
		getPatch(patchNum, patch, false);
		getPatch(patchNum, patchRevV, true);
		
		// Patch without modification
	    buildPatch(patchRevV, v, index, Matrix4f.identity);
	    
	    // Patch reflected in x
	    if( reflectX ) {
	        buildPatch(patch, v,
	                   index, new Matrix4f(
	                   				-1.0f,  0.0f,  0.0f,  0.0f,
	                   				 0.0f,  1.0f,  0.0f,  0.0f,
	                   				 0.0f,  0.0f,  1.0f,  0.0f,
	                   				 0.0f,  0.0f,  0.0f,  1.0f
	                   				 ));
	    }
	    
	    if(reflectY) {
	    	buildPatch(patch, v,
	                   index, new Matrix4f(
	                   				 1.0f,  0.0f,  0.0f,  0.0f,
	                   				 0.0f, -1.0f,  0.0f,  0.0f,
	                   				 0.0f,  0.0f,  1.0f,  0.0f,
	                   				 0.0f,  0.0f,  0.0f,  1.0f
	                   				 ));
	    }
	    
	    if(reflectX && reflectY) {
	    	buildPatch(patchRevV, v,
	                   index, new Matrix4f(
	                   				-1.0f,  0.0f,  0.0f,  0.0f,
	                   				 0.0f, -1.0f,  0.0f,  0.0f,
	                   				 0.0f,  0.0f,  1.0f,  0.0f,
	                   				 0.0f,  0.0f,  0.0f,  1.0f
	                   				 ));
	    }
	}

	private static void buildPatch(Vec3f[][] patch, float[] v, int[] index,
			Matrix4f reflect) {
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				Vec3f pt = reflect.multPoint(patch[i][j]);
				
				int idx = index[0];
				
				v[idx++] = pt.x;
				v[idx++] = pt.y;
				v[idx++] = pt.z;
				
				index[0] = idx;
			}
		}
	}
	
	private static void getPatch(int patchNum, Vec3f[][] patch, boolean reverseV) {
		for(int u = 0; u < 4; u++) {
			for(int v = 0; v < 4; v++) {
				if(reverseV) {
					patch[u][v] = new Vec3f(
	                        TeapotData.CP_DATA[ TeapotData.PATCH_DATA[patchNum][u*4+(3-v)] ][0],
	                        TeapotData.CP_DATA[ TeapotData.PATCH_DATA[patchNum][u*4+(3-v)] ][1],
	                        TeapotData.CP_DATA[ TeapotData.PATCH_DATA[patchNum][u*4+(3-v)] ][2]
	                        );
				}
				else {
					patch[u][v] = new Vec3f(
	                        TeapotData.CP_DATA[ TeapotData.PATCH_DATA[patchNum][u*4+v] ][0],
	                        TeapotData.CP_DATA[ TeapotData.PATCH_DATA[patchNum][u*4+v] ][1],
	                        TeapotData.CP_DATA[ TeapotData.PATCH_DATA[patchNum][u*4+v] ][2]
	                        );
				}
			}
		}
	}

	public void render(GL4 gl) {
		gl.glPatchParameteri(GL_PATCH_VERTICES, 16);
		
		gl.glBindVertexArray(vao[0]);
		gl.glDrawArrays(GL_PATCHES, 0, 32 * 16);
	}

	public void free(GL4 gl) {
		gl.glDeleteBuffers(buffer.length, buffer, 0);
		gl.glDeleteVertexArrays(vao.length, vao, 0);
	}
	
	private int[] vao = new int[1];
	private int[] buffer = new int[1];
}
