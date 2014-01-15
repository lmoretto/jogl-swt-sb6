package jogl.swt.utils;

import java.nio.ByteBuffer;

public class DrawArraysIndirectCommand {
	public static final int SIZE_IN_BYTES = 16;
	
	public DrawArraysIndirectCommand(int vertexCount, int instanceCount, int firstVertex, int baseInstance) {
		this.vertexCount = vertexCount;
		this.instanceCount = instanceCount;
		this.firstVertex = firstVertex;
		this.baseInstance = baseInstance;
	}
	
	public int getVertexCount() {
		return vertexCount;
	}
	
	public int getInstanceCount() {
		return instanceCount;
	}
	
	public int getFirstVertex() {
		return firstVertex;
	}
	
	public int getBaseInstance() {
		return baseInstance;
	}
	
	public void writeToBuffer(ByteBuffer buffer) {
		if(buffer != null) {
			int available = buffer.limit() - buffer.position();
			
			if(available >= SIZE_IN_BYTES) {
				buffer.putInt(vertexCount);
				buffer.putInt(instanceCount);
				buffer.putInt(firstVertex);
				buffer.putInt(baseInstance);
			}
		}
	}
	
	private int vertexCount;
	private int instanceCount;
	private int firstVertex;
	private int baseInstance;
}
