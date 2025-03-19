#version 120

varying vec4 outColor;
varying vec3 pos;

void main() {
    pos = (gl_ModelViewMatrix * gl_Vertex).xyz;
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    gl_TexCoord[0] = gl_MultiTexCoord0;
    outColor = gl_Color;
}
