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
- **Status:** Completed
- **Exit Criteria Status:**
  - [x] 14 capabilities enforced in RLS security definer logic (`FR-PRM-01`, `FR-PRM-02`, Chapter 12)
  - [x] Adversarial policy tests verifying denial of tampered-client writes across all 14 permissions
  - [x] Admin approval & rejection flow UI ([PendingRequestsScreen.kt](feature/groups/src/main/kotlin/com/snapfilters/feature/groups/ui/PendingRequestsScreen.kt))
  - [x] Group permissions management UI ([GroupPermissionsScreen.kt](feature/groups/src/main/kotlin/com/snapfilters/feature/groups/ui/GroupPermissionsScreen.kt))
  - [x] Last-admin protection guard preventing removal or demotion of the final admin
  - [x] Database audit logging table `group_audit` implemented in `00004_permissions_matrix_and_audit.sql`

## Milestone 6: Internal Messaging
- **Status:** Completed
- **Exit Criteria Status:**
  - [x] Realtime message delivery and REST keyset pagination `(created_at desc, id desc)` (ADR-009, Chapter 18)
  - [x] Idempotent send via `client_message_id` with duplicate defense (`FR-MSG-03`)
  - [x] Unread count calculated from `last_read_at` timestamp surviving process death (`FR-MSG-02`)
  - [x] Immediate read revocation and thread cache clearing (`FR-MSG-05`)
  - [x] Tombstone resolution for deleted senders ("Former member")
  - [x] Message bubbles with inline retry affordance for failed transmissions
  - [x] Room database entities (`MessageEntity`, `ConversationEntity`) and DAO (`ChatDao`) added

## Milestone 7: Live Multi-Face AR
- **Status:** Completed
- **Exit Criteria Status:**
  - [x] Live multi-face pipeline with device tiering (Tier A: 4 faces, Tier B: 3 faces, Tier C: 2 faces) (`FR-AR-01`, Chapter 15)
  - [x] Hungarian-style greedy IoU (0.6) + landmark distance (0.4) tracker preventing accessory swap (`FR-AR-03`)
  - [x] 40% frame width jump guard holding last pose for 300 ms (ADR-004)
  - [x] 15-frame miss grace window surviving blinks and hand occlusions
  - [x] 2-consecutive-frame activation guard preventing false-positive flashes
  - [x] One Euro filter temporal smoothing per landmark group
  - [x] AR tray UI with quiet hint for undetected faces (Chapter 6 State matrix)

## Milestone 8: Still-Photo AR
- **Status:** Completed
- **Exit Criteria Status:**
  - [x] Two-stage still-photo pipeline: ML Kit full-frame detection + 40% per-face crop passed to landmarker (ADR-005)
  - [x] Recomputed accessory placement at source capture resolution (`FR-AR-02`)
  - [x] Placement error mathematically proven under 1.5% of face width vs reference
  - [x] High-resolution accessory rendering drawn at real face size (1,200 px face gets 1,200 px accessory)
  - [x] Memory-disciplined horizontal tiled rendering (`TiledBitmapCompositor`) avoiding silent downscaling
  - [x] Graceful fallback when no faces are detected in still pass

## Milestone 9: Testing, Performance, Production Readiness
- **Status:** Completed
- **Exit Criteria Status:**
  - [x] Measured benchmarks against NFR table (NFR-01 to NFR-08) documented in `docs/benchmarks/BENCHMARKS.md`
  - [x] Privacy plan and biometric policy documented in `docs/privacy/PRIVACY_PLAN.md`
  - [x] Account deletion cascade (`delete_account()`, Chapter 22) implemented in `supabase/migrations/00005_account_deletion_and_privacy.sql`
  - [x] Settings UI and account deletion confirmation flow implemented in `feature/settings`
  - [x] Unit and adversarial cascade tests implemented in `AccountDeletionCascadeTest.kt`
  - [x] All 9 engineering milestones from PRD v3.0 successfully implemented, documented, and delivered!

