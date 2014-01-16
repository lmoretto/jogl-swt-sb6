#version 410 core

// Per-vertex inputs
layout (location = 0) in vec4 position;
layout (location = 1) in vec3 normal;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
// Clip plane
uniform vec4 clip_plane = vec4(1.0, 1.0, 0.0, 0.85);
uniform vec4 clip_sphere = vec4(0.0, 0.0, 0.0, 4.0);

// Position of light
uniform vec3 light_pos = vec3(100.0, 100.0, 100.0);

// Outputs from vertex shader
out VS_OUT
{
    vec3 N;
    vec3 L;
    vec3 V;
} vs_out;

void main(void) {
	//view-space position
	vec4 P = mv_matrix * position;
	
	//view-space normal
	vec4 normal4 = vec4(normal, 0.0); 
	
	vs_out.N = (mv_matrix * normal4).xyz;
	
	//light vector
	vs_out.L = light_pos - P.xyz;
	
	//view vector
	vs_out.V = -P.xyz;
	
	//write clip distances
	//distance from plain = object-space position dot clip_plane
	gl_ClipDistance[0] = dot(position, clip_plane);
	//distance from sphere = length(vector from sphere center to view-space position) - sphere radius (view space position = [x/w, y/w, z/w])
	gl_ClipDistance[1] = length(position.xyz / position.w - clip_sphere.xyz) - clip_sphere.w;
	
	gl_Position = proj_matrix * P;  
}
