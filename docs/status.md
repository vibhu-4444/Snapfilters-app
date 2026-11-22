# Project Status & Tracking

## Milestone 1: Foundation, Design System, Camera
- **Status:** Completed (Ready for Review)
- **Exit Criteria Status:**
  - [x] Project architecture established with 15 Gradle modules across 3 layers
  - [x] Module graph rule enforced (`feature-* -> feature-*` strictly banned) per ADR-001
  - [x] Camera preview, capture, and controls implemented with CameraX & Camera2 interop
  - [x] FR-CAM-01 to FR-CAM-09 implemented with unit test coverage
  - [x] Core design system tokens (`#0B0B0C`, `#C9A227`, etc.) and UI primitives (`ShutterButton`, `ModeChip`, `PermissionCard`, `EmptyState`) implemented
  - [x] Supabase initial schemas (`00001_initial_schema.sql`) and RLS security rules (`00002_security_and_rls.sql`) drafted

## Architecture Compliance Register
- [x] Operating Rule 1: Repository inspected before writing (empty state documented)
- [x] Operating Rule 12: Incremental milestone delivery (Milestone 1 delivered, Milestone 2 staged)
- [x] ADR-001: 15-module graph with forbidden-edge check
- [x] ADR-002: Supabase database schema and RLS policies defined
- [x] ADR-006: OpenGL ES 3.0 fragment shader interface declared in core-common
- [x] ADR-008: Original byte preservation contracts and save modes defined
- [x] ADR-010: 32-byte CSPRNG token hash design integrated in database schema

## Next Milestone: Milestone 2 (Filters, LUTs, Editor, Local Saving)
- 12 built-in cinematic filters (.cube format at 33^3)
- Strict .cube parser supporting 17/33/65 grid points with 4 MB file cap
- OpenGL ES 3.0 3D texture shader implementation (`GL_TEXTURE_3D` and 2D fallback atlas)
- Post-capture editor screen with live intensity slider (0-100%)
- Non-destructive MediaStore save pipeline (Original only, Filtered only, Both)
