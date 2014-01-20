#version 430 core

layout (triangles) in;

layout (triangle_strip, max_vertices = 3) out;

uniform mat4 viewport_matrix;

out vec3 gNormal;
out vec4 gPosition;
// distances expressed in screen space --> linear interpolation must be used, not the usual perspective corrected interpolation
noperspective out vec3 gEdgeDistance;

in vec3 vNormal[];
in vec4 vPosition[];

void main(void) {
	// positions in screen coordinates (viewport coordinates)
	vec3 p0 = vec3(viewport_matrix * (gl_in[0].gl_Position / gl_in[0].gl_Position.w));
	vec3 p1 = vec3(viewport_matrix * (gl_in[1].gl_Position / gl_in[1].gl_Position.w));
	vec3 p2 = vec3(viewport_matrix * (gl_in[2].gl_Position / gl_in[2].gl_Position.w));
	
	//find the triangle altitudes (ha, hb, hc)
	float a = length(p1 - p2); // length of edge opposite to p0
	float b = length(p2 - p0); // length of edge opposite to p1
	float c = length(p1 - p0); // length of edge opposite to p2
	float alpha = acos( (b*b + c*c - a*a) / (2.0 * b * c) );
	float beta =  acos( (a*a + c*c - b*b) / (2.0 * a * c) );
	float ha = abs( c * sin(beta) );
	float hb = abs( c * sin(alpha) );
	float hc = abs( b * sin(alpha) );
	
	//send the triangle with the edge distances
	gEdgeDistance = vec3(ha, 0, 0);
	gNormal = vNormal[0];
	gPosition = vPosition[0];
	gl_Position = gl_in[0].gl_Position;
	EmitVertex();
	
	gEdgeDistance = vec3(0, hb, 0);
	gNormal = vNormal[1];
	gPosition = vPosition[1];
	gl_Position = gl_in[1].gl_Position;
	EmitVertex();
	
	gEdgeDistance = vec3(0, 0, hc);
	gNormal = vNormal[2];
	gPosition = vPosition[2];
	gl_Position = gl_in[2].gl_Position;
	EmitVertex();
	
	EndPrimitive();
}