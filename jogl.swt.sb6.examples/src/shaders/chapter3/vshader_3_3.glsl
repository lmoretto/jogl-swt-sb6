#version 430 core

// "offset" and "color" are input vertex attributes
layout (location = 0) in vec4 offset;
layout (location = 1) in vec4 color;

// "vs_color is an output that will be sent to the next shader stage (e.g. to the framgent shader)
out VS_OUT {
	vec4 color;
} vs_out;

void main(void) {
	// hard-coded array of positions
	const vec4 vertices[3] = vec4[3](vec4(0.25, -0.25, 0.5, 1.0),
									vec4(-0.25, -0.25, 0.5, 1.0),
									vec4(0.25, 0.25, 0.5, 1.0));
									
	// Add "offset" to the hard-coded vertex position
	gl_Position = vertices[gl_VertexID] + offset;
	
	//output a fixed value for vs_color
	vs_out.color = color;
}