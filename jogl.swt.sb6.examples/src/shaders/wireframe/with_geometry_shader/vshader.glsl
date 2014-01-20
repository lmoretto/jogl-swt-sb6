#version 430 core

layout (location = 0) in vec4 position;
layout (location = 1) in vec3 normal;

out vec3 vNormal;
out vec4 vPosition;

uniform mat4 mv_matrix;
uniform mat4 mvp_matrix;

void main(void) {
	vNormal = normalize(mv_matrix * vec4(normalize(normal), 0.0)).xyz;
	vPosition = mv_matrix * position;
	gl_Position = mvp_matrix * position;
}