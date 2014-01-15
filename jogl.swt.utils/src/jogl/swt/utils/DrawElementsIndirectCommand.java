package jogl.swt.utils;

import java.nio.ByteBuffer;

public class DrawElementsIndirectCommand {
	public static final int SIZE_IN_BYTES = 20;
	
	public DrawElementsIndirectCommand(int vertexCount, int instanceCount, int firstIndex, int baseVertex, int baseInstance) {
		this.vertexCount = vertexCount;
		this.instanceCount = instanceCount;
		this.firstIndex = firstIndex;
		this.baseVertex = baseVertex;
		this.baseInstance = baseInstance;
	}
	
	public int getVertexCount() {
		return vertexCount;
	}
	
	public int getInstanceCount() {
		return instanceCount;
	}
	
	public int getFirstIndex() {
		return firstIndex;
	}
	
	public int getBaseVertex() {
		return baseVertex;
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
				buffer.putInt(firstIndex);
				buffer.putInt(baseVertex);
				buffer.putInt(baseInstance);
			}
		}
	}
	
	private int vertexCount;
	private int instanceCount;
	private int firstIndex;
	private int baseVertex;
	private int baseInstance;
}
