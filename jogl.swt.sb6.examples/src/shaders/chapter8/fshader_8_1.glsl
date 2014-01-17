#version 430 core

out vec4 color;

in FRAGMENT {
	vec2 tc;
} fs_in;

layout (binding = 1) uniform sampler2D tex_color;

void main(void) {
	color = texture(tex_color, fs_in.tc);
}