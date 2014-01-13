#version 430 core

in vec4 position;
in vec4 instance_color;
in vec4 instance_position;

out Fragment {
	vec4 color;
} fragment;

uniform mat4 mvp;

void main() {
	gl_Position = mvp * (position + instance_position);
	fragment.color = instance_color;
}