#version 430 core

void main(void) {
	// hard-coded array of positions
	const vec4 vertices[3] = vec4[3](vec4(0.25, -0.25, 0.5, 1.0),
									vec4(-0.25, -0.25, 0.5, 1.0),
									vec4(0.25, 0.25, 0.5, 1.0));
									
	//gl_VertexID is the 0-based index of the current vertex in the input vertex array
	gl_Position = vertices[gl_VertexID];
}