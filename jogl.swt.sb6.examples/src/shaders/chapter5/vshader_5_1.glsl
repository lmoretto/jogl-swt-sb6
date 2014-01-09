#version 430 core

in vec4 position;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;

// "vs_color is an output that will be sent to the next shader stage (e.g. to the framgent shader)
out VS_OUT {
	vec4 color;
} vs_out;

void main (void) {
	gl_Position = proj_matrix * mv_matrix * position;
	
	vs_out.color = position * 2.0 + vec4(0.5, 0.5, 0.5, 0.0);
}