#version 430 core

//cubic bezier patch control grid: 4*4 control points
layout (vertices = 16) out;

uniform float tess_factor = 16.0;

void main(void)
{
    if (gl_InvocationID == 0)
    {
    	gl_TessLevelInner[0] = tess_factor;
    	gl_TessLevelInner[1] = tess_factor;
        gl_TessLevelOuter[0] = tess_factor;
        gl_TessLevelOuter[1] = tess_factor;
        gl_TessLevelOuter[2] = tess_factor;
        gl_TessLevelOuter[3] = tess_factor;
    }
    
    gl_out[gl_InvocationID].gl_Position = gl_in[gl_InvocationID].gl_Position;
}