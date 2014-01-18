#version 430 core

layout (quads, equal_spacing, cw) in;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;

//Surface normal. Computed only to properly color the surface, not required for the tesselation
out TES_OUT {
	vec3 N;
} tes_out;

//qaudratic bezier curve interpolation function: A, B and C are the curve control points
vec4 quadratic_bezier(vec4 A, vec4 B, vec4 C, float t) {
	vec4 D = mix(A, B, t); //A + t(B-A) = (1 - t)A + tB;
	vec4 E = mix(B, C, t);
	
	vec4 P = mix(D, E, t);
	return P;
}

//cubic bezier curve interpolation function: A, B, C and D are the curve control points
vec4 cubic_bezier(vec4 A, vec4 B, vec4 C, vec4 D, float t) {
	vec4 E = mix(A, B, t);
	vec4 F = mix(B, C, t);
	vec4 G = mix(C, D, t);
	
	return quadratic_bezier(E, F, G, t);
}

//at contains t0 and t1: t1 is used to interpolate the first time along the 4 curves represented by the four columns of the input 4*4 control point grid --> obtain 4 new points
//t0 is used to interpolate along the curve represented by the new four points just obtained
vec4 evaluate_patch(vec2 at) {
	vec4 P[4];
	
	int i;
	
	for(i = 0; i < 4; i++) {
		P[i] = cubic_bezier(
					gl_in[i  +  0].gl_Position,
					gl_in[i  +  4].gl_Position,
					gl_in[i  +  8].gl_Position,
					gl_in[i  + 12].gl_Position,
					at.y);
	}
	
	return cubic_bezier(P[0], P[1], P[2], P[3], at.x);
}

const float epsilon = 0.001; //Used to interpolate near the point in order to compute the surface normal

void main(void) {
	vec4 p1 = evaluate_patch(gl_TessCoord.xy);
	
	gl_Position = proj_matrix * p1;
	
	//surface normal computation, just for the fragment color
	vec4 p2 = evaluate_patch(gl_TessCoord.xy + vec2 (0.0, epsilon));
	vec4 p3 = evaluate_patch(gl_TessCoord.xy + vec2 (epsilon, 0.0));
	
	vec3 v1 = normalize(p2.xyz - p1.xyz);
	vec3 v2 = normalize(p3.xyz - p1.xyz);
	
	tes_out.N = cross(v1, v2);
}