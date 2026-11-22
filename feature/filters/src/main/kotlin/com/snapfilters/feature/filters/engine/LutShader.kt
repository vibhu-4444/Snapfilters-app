package com.snapfilters.feature.filters.engine

/**
 * OpenGL ES 3.0 Fragment Shader Contract per PRD v3.0 Chapter 14.
 * Both live preview and export use this exact shader object to prevent preview/export drift (ADR-006).
 */
object LutShader {
    const val VERTEX_SHADER = """#version 300 es
layout (location = 0) in vec4 aPosition;
layout (location = 1) in vec2 aTexCoord;
out vec2 vTexCoord;

void main() {
    gl_Position = aPosition;
    vTexCoord = aTexCoord;
}
"""

    const val FRAGMENT_SHADER = """#version 300 es
precision mediump float;
in vec2 vTexCoord;
out vec4 fragColor;

uniform sampler2D uSourceTexture;
uniform sampler3D uLut;
uniform float uIntensity; // 0.0 - 1.0
uniform float uLutSize;   // 17, 33 or 65

vec3 applyLut(vec3 c) {
    vec3 scale = vec3((uLutSize - 1.0) / uLutSize);
    vec3 off = vec3(1.0 / (2.0 * uLutSize));
    vec3 lut = texture(uLut, c * scale + off).rgb;
    return mix(c, lut, uIntensity); // intensity is a mix, not an alpha
}

void main() {
    vec4 src = texture(uSourceTexture, vTexCoord);
    fragColor = vec4(applyLut(src.rgb), src.a);
}
"""
}
