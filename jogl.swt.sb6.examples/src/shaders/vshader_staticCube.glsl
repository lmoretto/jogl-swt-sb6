#version 430 core

in vec4 position;
in int face;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;

// "vs_color is an output that will be sent to the next shader stage (e.g. to the framgent shader)
out VS_OUT {
	vec4 color;
} vs_out;

void main (void) {
	const vec4 colors[] = vec4[6] (
		vec4(1.0, 0.0, 0.0, 1.0), //BACK - RED
		vec4(0.0, 1.0, 0.0, 1.0), //RIGHT - GREEN
		vec4(0.0, 0.0, 1.0, 1.0), //FRONT - BLUE
		vec4(1.0, 1.0, 0.0, 1.0), //LEFT - YELLOW
		vec4(1.0, 0.0, 1.0, 1.0), //TOP - LIGHT BLUE
		vec4(0.0, 1.0, 1.0, 1.0)  //BOTTOM - MAGENTA
	);

	gl_Position = proj_matrix * mv_matrix * position;
	
	vs_out.color = colors[face];
}