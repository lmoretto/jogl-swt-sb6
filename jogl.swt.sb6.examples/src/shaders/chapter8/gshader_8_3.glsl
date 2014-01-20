#version 430 core

layout (triangles) in;
layout (triangle_strip, max_vertices = 3) out;

in Vertex {
	vec4 color;
} vertex[];

out vec4 color;

uniform vec3 viewpoint;
uniform mat4 mv_matrix;
uniform mat4 mvp_matrix;

void main(void) {
	vec3 ab = gl_in[1].gl_Position.xyz - gl_in[0].gl_Position.xyz;
	vec3 ac = gl_in[2].gl_Position.xyz - gl_in[0].gl_Position.xyz;
	vec3 normal = normalize(cross(ab, ac));
	
	vec3 transformed_normal = (mv_matrix * vec4(normal, 0.0)).xyz;
	vec3 vt = normalize(gl_in[0].gl_Position.xyz - viewpoint);
	
	float d = dot(vt, normal);
	
	if(d > 0.0) {
		int i;
		for(i = 0; i < 3; i++) {
			gl_Position = mvp_matrix * gl_in[i].gl_Position;
			color = vertex[i].color;
			EmitVertex();
		}
		EndPrimitive();
	}
}
