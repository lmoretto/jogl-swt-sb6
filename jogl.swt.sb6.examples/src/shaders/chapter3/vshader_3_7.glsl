#version 430 core

// "vs_color is an output that will be sent to the next shader stage (e.g. to the framgent shader)
out vec4 vs_color;

void main(void) {
	// hard-coded array of positions
	const vec4 vertices[3] = vec4[3](vec4(0.25, -0.25, 0.5, 1.0),
									vec4(-0.25, -0.25, 0.5, 1.0),
									vec4(0.25, 0.25, 0.5, 1.0));
									
	// hard-coded array of colors
	const vec4 colors[3] = vec4[3](vec4(1.0, 0.0, 0.0, 1.0),
									vec4(0.0, 1.0, 0.0, 1.0),
									vec4(0.0, 0.0, 1.0, 1.0));
									
	// Add "offset" to the hard-coded vertex position
	gl_Position = vertices[gl_VertexID];
	
	//output a fixed value for vs_color
	vs_color = colors[gl_VertexID];
}