#version 430 core

uniform vec4 line_color = vec4(1.0);

out vec4 output_color;

void main (void) {
	output_color = line_color;
}