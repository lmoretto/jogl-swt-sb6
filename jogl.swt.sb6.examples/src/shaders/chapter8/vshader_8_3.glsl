#version 430 core

layout (location = 0) in vec4 vVertex;
layout (location = 1) in vec3 vNormal;

out Vertex {
	vec4 color;
} vertex;

uniform vec3 vLightPosition = vec3(-10.0, 40.0, 200.0);
uniform mat4 mv_matrix;

void main(void) {
	vec3 vEyeNormal = (mv_matrix * vec4(normalize(vNormal), 0.0)).xyz;
	
	vec4 vPosition4 = mv_matrix * vVertex;
	vec3 vPosition3 = vPosition4.xyz / vPosition4.w;
	
	vec3 vLightDir = normalize(vLightPosition - vPosition3);
	
	//diffuse color
	vertex.color = vec4(0.7, 0.6, 1.0, 1.0) * abs(dot(vEyeNormal, vLightDir));
	
	gl_Position = vVertex;
}