# Architecture Decision Register

| ADR | Title | Decision | Reversal Cost |
| :--- | :--- | :--- | :--- |
| **ADR-001** | Modular Gradle Architecture with Banned Dependency Direction | 15 modules in 3 layers. Feature-to-feature dependencies strictly banned. Enforced by verification task. | Medium (Gradle surgery, 2-3 days) |
| **ADR-002** | Supabase as Primary Backend | Postgres RLS, Storage, Realtime, Edge Functions. Database-enforced authorization immune to client tampering. | High (Rewrite rules, auth, realtime) |
| **ADR-003** | Backend-Issued Anonymous Identity, Never Local | Supabase anonymous sign-in creates `auth.users` row; 12-word recovery code maps to hashed secret. No local UUID spoofing. | Low |
| **ADR-004** | MediaPipe Face Landmarker for Live Multi-Face AR | MediaPipe Live Stream mode + GPU delegate + One Euro filter temporal smoothing with Hungarian IoU tracker. | Medium |
| **ADR-005** | Two-Stage Pipeline for Captured Photograph | ML Kit full-frame detection + 40% margin crop passed to MediaPipe landmarker. Recomputed at native source resolution. | Low |
| **ADR-006** | OpenGL ES 3.0 Fragment Shader for 3D LUT Application | Single shared shader program sampling 3D texture (`GL_TEXTURE_3D`) for preview and export. Eliminates drift. | Low |
| **ADR-007** | Custom .cube LUT Limits | Accept 17, 33, 65 point grids with 4 MB cap. Strict line-count and float parsing validation. | Low |
| **ADR-008** | Save Modes Chosen at Export | Original only / Filtered only / Both. Original bytes byte-for-byte preserved (SHA-256 verified). | Low |
| **ADR-009** | Realtime for Delivery, REST for History Backfill | Keyset pagination by `(created_at desc, id desc)`. Idempotent send with `client_message_id`. | Medium |
| **ADR-010** | Invitation Tokens as 32-Byte CSPRNG Random Secrets | Tokens hashed with SHA-256 at rest, never expose raw group IDs. Expiring and revocable. | Low |
| **ADR-011** | Media Storage Tiering | Preview 2048 px default, original on explicit request. Stretches free-tier storage 8x. | Medium |
| **ADR-012** | Concurrency Model | Kotlin Coroutines and StateFlow throughout; no RxJava. Jetpack Compose native. | Low |
