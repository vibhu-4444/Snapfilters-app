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
- **Status:** Completed
- **Exit Criteria Status:**
  - [x] 12 built-in cinematic filters authored across 3 categories (Cinematic, Film, Natural) per Chapter 14
  - [x] Strict .cube parser implemented for 17, 33, 65 grid sizes with exact rejection messages (4 MB cap, float validation)
  - [x] OpenGL ES 3.0 fragment shader contract (`GL_TEXTURE_3D`) shared across preview and export (ADR-006)
  - [x] Filter intensity slider (0–100%) with 0% exact source pixel identity (FR-FLT-02)
  - [x] Non-destructive post-capture editor screen with full-bleed preview and filter tray
  - [x] 3 save modes (Original only, Filtered only, Both) with MediaStore integration and Room tracking
  - [x] Byte-for-byte original save verified via SHA-256 (FR-EDT-03)
  - [x] LRU cache of 3 LUTs in memory (~2.4 MB at 33^3) implemented (ADR-007)

## Milestone 3: External Sharing
- **Status:** Completed
- **Exit Criteria Status:**
  - [x] Android Sharesheet integration for any compatible destination (`FR-SHR-01`)
  - [x] Scoped FileProvider configured for `share_cache`, `exports`, and `media` paths (Chapter 17)
  - [x] Distinct "Share Photo" (image/jpeg) vs "Share Original File" (untouched bytes) flows (`FR-SHR-02`)
  - [x] Explicit quality disclaimers forbidding false marketing promises ("Lossless to WhatsApp", etc.) (`FR-SHR-03`)
  - [x] Automated GitHub Actions CI/CD workflow configured for continuous APK build & release artifact publishing

## Milestone 4: Anonymous Auth and QR Groups
- **Status:** Completed
- **Exit Criteria Status:**
  - [x] Supabase anonymous authentication with device-bound session token (`FR-IDN-01`, `FR-IDN-02`)
  - [x] Display name onboarding prompt and editing (`FR-IDN-03`)
  - [x] 12-word recovery mnemonic code generation (132-bit entropy) hashed at rest (`FR-IDN-04`, ADR-003)
  - [x] Group creation with atomic owner membership (`FR-GRP-01`)
  - [x] 32-byte CSPRNG token minting and QR generation (`FR-GRP-02`, ADR-010)
  - [x] QR redeem state machine implementing all 8 states (`FR-GRP-03` to `05`)
  - [x] Security definer functions drafted in `00003_invitations_and_recovery.sql` (Chapter 11)

## Milestone 5: Permissions and Join Approval
- **Status:** Completed (Ready for Review)
- **Exit Criteria Status:**
  - [x] 14 capabilities enforced in RLS security definer logic (`FR-PRM-01`, `FR-PRM-02`, Chapter 12)
  - [x] Adversarial policy tests verifying denial of tampered-client writes across all 14 permissions
  - [x] Admin approval & rejection flow UI ([PendingRequestsScreen.kt](feature/groups/src/main/kotlin/com/snapfilters/feature/groups/ui/PendingRequestsScreen.kt))
  - [x] Group permissions management UI ([GroupPermissionsScreen.kt](feature/groups/src/main/kotlin/com/snapfilters/feature/groups/ui/GroupPermissionsScreen.kt))
  - [x] Last-admin protection guard preventing removal or demotion of the final admin
  - [x] Database audit logging table `group_audit` implemented in `00004_permissions_matrix_and_audit.sql`

## Next Milestone: Milestone 6 (Internal Messaging)
- Realtime messaging delivery + REST keyset pagination `(created_at desc, id desc)` (ADR-009, Chapter 18)
- Idempotent send via `client_message_id` with duplicate defense (`FR-MSG-03`)
- Resumable media asset upload to Supabase storage bucket (`group-media`)
- Unread count badges derived from `conversation_members.last_read_at` (`FR-MSG-02`)
