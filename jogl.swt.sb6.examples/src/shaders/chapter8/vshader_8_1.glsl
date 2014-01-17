#version 430 core

out FRAGMENT {
	vec2 tc;
} vs_out;

void main(void) {
	const vec4 vertices[] = vec4[] (
		vec4(-0.5, 0.0, -0.5, 1.0),
		vec4( 0.5, 0.0, -0.5, 1.0),
		vec4(-0.5, 0.0,  0.5, 1.0),
		vec4( 0.5, 0.0,  0.5, 1.0)
	);
	
	int x = gl_InstanceID & 63; // & 63 = % 64
	int y = gl_InstanceID >> 6; // >> 6 = / 64
	vec2 offs = vec2(x, y);
	
	vs_out.tc = (vertices[gl_VertexID].xz + offs + vec2(0.5)) / 64.0;
	gl_Position = vertices[gl_VertexID] + vec4(float(x - 32), 0.0,
											   float(y - 32), 0.0);
}