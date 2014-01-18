#version 430 core

in vec4 position;

uniform mat4 mvp_matrix;

void main(void) {
	gl_Position = mvp_matrix * position;
}