#version 430 core

layout (triangles) in;
layout (points, max_vertices=3) out;

in VS_OUT {
	vec4 color;
} g_in[];

out VS_OUT {
	vec4 color;
} g_out[];

void main(void) {
	int i;
	for(i = 0; i < gl_in.length(); i++) {
		gl_Position = gl_in[i].gl_Position;
		g_out[i].color = g_in[i].color;
		EmitVertex();
	}
}