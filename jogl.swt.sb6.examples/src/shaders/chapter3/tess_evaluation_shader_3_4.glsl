#version 430 core

layout (triangles, equal_spacing, cw) in;

in VS_OUT {
	vec4 color;
} tc_in[];

out VS_OUT {
	vec4 color;
} te_out;

void main(void) {
	gl_Position =  (gl_TessCoord.x * gl_in[0].gl_Position + 
					gl_TessCoord.y * gl_in[1].gl_Position +
					gl_TessCoord.z * gl_in[2].gl_Position);
					
	te_out.color = tc_in[0].color;  
}