#version 430 core

//Set the output control points number
layout (vertices = 3) out;

in VS_OUT {
	vec4 color;
} vs_in[];

out VS_OUT {
	vec4 color;
} tc_out[];

void main(void) {
	//gl_InvocationID is the 0-based index of the control point within the patch being processed
	if(gl_InvocationID == 0) {
		gl_TessLevelInner[0] = 5.0;
		gl_TessLevelOuter[0] = 5.0;
		gl_TessLevelOuter[1] = 5.0;
		gl_TessLevelOuter[2] = 5.0;
	}
	
	gl_out[gl_InvocationID].gl_Position = gl_in[gl_InvocationID].gl_Position;
	
	tc_out[gl_InvocationID].color = vs_in[gl_InvocationID].color;
}