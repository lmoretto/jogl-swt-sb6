#version 430 core

layout (location = 0) in vec4 vVertex;

uniform mat4 mvp_matrix;

void main(void) {
	gl_Position = mvp_matrix * vVertex;
}