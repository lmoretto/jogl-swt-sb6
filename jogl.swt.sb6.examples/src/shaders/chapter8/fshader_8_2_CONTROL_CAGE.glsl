#version 430 core

uniform vec4 draw_color;

out vec4 color;

void main(void) {
	color = draw_color;
}