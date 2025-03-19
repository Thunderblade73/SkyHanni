#version 120

uniform vec3 centerPos;
//uniform vec4 color;
uniform float radius;
uniform sampler2D texture;

varying vec4 outColor;
varying vec3 pos;

void main() {
    vec4 nativeColor = texture2D(texture, gl_TexCoord[0].st) * outColor;
    vec3 diff = (centerPos - pos);
    float distance = sqrt(pow(diff.x,2.0)+pow(diff.y,2.0)+pow(diff.z,2.0));
    float cutOff = 1.0f - step(distance,10.0f);
    gl_FragColor = ((1.0f-cutOff) * vec4(1)) + (cutOff * nativeColor);
}
