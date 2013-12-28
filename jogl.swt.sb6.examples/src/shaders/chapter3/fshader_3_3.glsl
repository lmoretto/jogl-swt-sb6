#version 430 core

// Input from the vertex shader 
in VS_OUT {
	vec4 color;
} fs_in;

// Output to the frame buffer
out vec4 color;

void main(void) {
	// Simply assign the color we were given by the vertex shader to our output
	color = fs_in.color;
}