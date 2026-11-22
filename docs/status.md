# Project Status & Tracking

## Milestone 1: Foundation, Design System, Camera
- **Status:** Completed
- **Exit Criteria Status:**
  - [x] Project architecture established with 15 Gradle modules across 3 layers
  - [x] Module graph rule enforced (`feature-* -> feature-*` strictly banned) per ADR-001
  - [x] Camera preview, capture, and controls implemented with CameraX & Camera2 interop
  - [x] FR-CAM-01 to FR-CAM-09 implemented with unit test coverage
  - [x] Core design system tokens (`#0B0B0C`, `#C9A227`, etc.) and UI primitives implemented
  - [x] Supabase initial schemas (`00001_initial_schema.sql`) and RLS security rules (`00002_security_and_rls.sql`) drafted

## Milestone 2: Filters, LUTs, Editor, Local Saving
- **Status:** Completed (Ready for Review)
- **Exit Criteria Status:**
  - [x] 12 built-in cinematic filters authored across 3 categories (Cinematic, Film, Natural) per Chapter 14
  - [x] Strict .cube parser implemented for 17, 33, 65 grid sizes with exact rejection messages (4 MB cap, float validation)
  - [x] OpenGL ES 3.0 fragment shader contract (`GL_TEXTURE_3D`) shared across preview and export (ADR-006)
  - [x] Filter intensity slider (0–100%) with 0% exact source pixel identity (FR-FLT-02)
  - [x] Non-destructive post-capture editor screen with full-bleed preview and filter tray
  - [x] 3 save modes (Original only, Filtered only, Both) with MediaStore integration and Room tracking
  - [x] Byte-for-byte original save verified via SHA-256 (FR-EDT-03)
  - [x] LRU cache of 3 LUTs in memory (~2.4 MB at 33^3) implemented (ADR-007)

## Next Milestone: Milestone 3 (External Sharing)
- Android Sharesheet integration for compatible destinations (`FR-SHR-01`)
- Scoped FileProvider configured for `share_cache`, `exports`, and `media` paths (Chapter 17)
- Distinct "Share Photo" (recompressed image/jpeg) vs "Share Original File" (untouched file) flows (`FR-SHR-02`)
- Explicit quality disclaimers for third-party messaging apps like WhatsApp (`FR-SHR-03`)
