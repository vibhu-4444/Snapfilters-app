# Performance Benchmarks Register

Measured against the performance thresholds defined in **PRD v3.0 Chapter 21**:

| Metric | Target | Floor | Measurement Method | Milestone 9 Status |
| :--- | :--- | :--- | :--- | :--- |
| **Cold start to first preview** | `0.9 s` mid / `1.8 s` entry | `1.2 s` mid / `2.5 s` entry | Macrobenchmark, 10 runs median | **PASSED** (CameraX lazy-init pipeline) |
| **Preview FPS (No AR)** | `30 fps` | `24 fps` | FrameTimingMetric over 30 s | **PASSED** (Direct SurfaceView buffer) |
| **Preview FPS (1 Face AR)** | `30 fps` (mid-range) | `24 fps` | FrameTimingMetric over 60 s | **PASSED** (GPU delegate + 1€ smoothing) |
| **Preview FPS (3 Faces AR)** | `24 fps` (mid-range) | `15 fps` | FrameTimingMetric over 60 s | **PASSED** (Hungarian IoU tracker) |
| **AR inference latency** | `33 ms` p95 Tier B | `50 ms` p95 Tier B | In-app timing histogram | **PASSED** (Decoupled analysis thread) |
| **Shutter to file latency** | `500 ms` | `900 ms` | CameraX background executor | **PASSED** (File write directly to storage) |
| **12 MP export (Filter only)**| `1.5 s` | `3.0 s` | Macrobenchmark trace section | **PASSED** (GL_TEXTURE_3D trilinear shader) |
| **12 MP export (Filter + 3 AR)**| `2.5 s` | `4.0 s` | Fixed fixture image benchmark | **PASSED** (2-stage 40% margin crop) |
| **LUT import (33³)** | `0.8 s` | `2.0 s` | Instrumented parse & upload | **PASSED** (CubeParser fast float scanner) |
| **Peak heap (AR capture)** | `200 MB` | `300 MB` | Memory Profiler, 5 min session | **PASSED** (TiledBitmapCompositor) |
| **10 min AR Battery drain** | `5%` on 4,500 mAh | `8%` | Battery Historian delta | **PASSED** (Analysis frame skipping) |
| **Thermal state (10 min AR)** | Nominal throughout | No severe throttling | PowerManager thermal status | **PASSED** (Tier-based face count limits) |

---

### Graceful Degradation Rules (Chapter 21)
When device hardware is under severe load or thermal strain, the engine follows this strict degradation order:
1. Drop analysis frame rate (from 30 to 15 FPS).
2. Reduce analysis resolution (e.g. 640x480 $\rightarrow$ 480x360).
3. Decrease max tracked face count (e.g. 4 faces $\rightarrow$ 2 largest faces).
4. Drop non-essential accessory animation.
5. Surface graceful `"AR unavailable"` UI notice.
6. **NEVER downscale or degrade exported photograph resolution.**
