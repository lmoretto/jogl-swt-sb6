#version 430 core

in vec3 gNormal;
in vec4 gPosition;
// distances expressed in screen space --> linear interpolation must be used, not the usual perspective corrected interpolation
noperspective in vec3 gEdgeDistance;

out vec4 output_color;

uniform vec3 vLightPosition = vec3(-10.0, 40.0, 200.0);
uniform mat4 mv_matrix;
uniform vec4 line_color = vec4(1.0);
uniform float line_width = 0.3;

void main(void) {
	vec3 vLightDir = normalize(vec4(vLightPosition, 1.0) - gPosition).xyz;
	
	//diffuse color
	vec4 color = vec4(0.7, 0.6, 1.0, 1.0) * abs(dot(gNormal, vLightDir));
	
	float d = min (gEdgeDistance.x, gEdgeDistance.y);
	d = min(d, gEdgeDistance.z);
	
	float mixVal = smoothstep(line_width - 1, line_width + 1, d);
	output_color = mix(line_color, color, mixVal);
}