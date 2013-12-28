#version 430 core

// "offset" is an input vertex attribute
layout (location = 0) in vec4 offset;

void main(void) {
	// hard-coded array of positions
	const vec4 vertices[3] = vec4[3](vec4(0.25, -0.25, 0.5, 1.0),
									vec4(-0.25, -0.25, 0.5, 1.0),
									vec4(0.25, 0.25, 0.5, 1.0));
									
	// Add "offset" to the hard-coded vertex position
	gl_Position = vertices[gl_VertexID] + offset;
}