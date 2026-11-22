# Privacy Plan & Data Inventory

Complies strictly with **PRD v3.0 Chapter 22**:

---

## 1. Data Inventory

| Data Type | Storage Location | Retention Window | Purpose |
| :--- | :--- | :--- | :--- |
| **Anonymous User ID** | `auth.users`, `profiles` | Until account deletion | Authentication & RLS access control |
| **Display Name** | `profiles` | Until account deletion | Sender identification in threads |
| **Recovery Code Hash** | `recovery_codes` | Single-use; revoked upon use or deletion | Optional identity recovery without email/phone |
| **Group Membership** | `group_members` | Until leaving group or account deletion | RLS authorization predicate |
| **Messages** | `messages` | Soft-deleted / tombstoned on account deletion | Conversation context preservation |
| **Shared Media** | `group-media` bucket | Until deleted by uploader or group deleted | Shared photograph exchange |

---

## 2. Facial Geometry & Privacy Guarantees

* **Zero Facial Identity:** No facial embeddings, templates, biometric identifiers, or mathematical representations of personal identity are ever computed, stored, or transmitted.
* **On-Device Memory Only:** Landmark detection runs locally on the phone's GPU/NPU. Face coordinates never leave local device memory.
* **Session Ephemeral:** All face tracking IDs and landmark buffers are destroyed the moment the camera or editor view is closed.
* **Zero Model Training:** User photographs and camera frames are never utilized for AI model training or telemetry.

---

## 3. Strict Permission Minimalist Policy

* `android.permission.CAMERA`: Requested only upon first camera launch with prior rationale dialog.
* `android.permission.READ_MEDIA_IMAGES`: Requested only when opening photo gallery.
* `android.permission.INTERNET`: Manifest declared for group sync.
* **BANNED PERMISSIONS:** Contacts, Location, Microphone, SMS, Call Log, and Body Sensors are never requested under any circumstances.
* **Zero Analytics / Ad Trackers:** The application contains zero advertising SDKs and zero third-party telemetry libraries.
