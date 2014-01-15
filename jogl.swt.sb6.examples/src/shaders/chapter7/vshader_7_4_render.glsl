#version 430 core

layout (location = 0) in vec4 position;

uniform mat4 view_proj;

void main() {
	vec4 realPos = position * 0.03;
	realPos[3] = 1.0;
	gl_Position = view_proj * realPos;
}