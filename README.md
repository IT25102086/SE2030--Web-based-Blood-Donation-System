# IT25102085 — Quality Assurance Officer
## BloodConnect Group Project — Role Contribution

**Student ID:** IT25102085  
**Assigned Role:** Quality Assurance Officer  
**Portal URL:** `http://localhost:8089/portal/qa`  
**Demo Login:** username `qa` / password `pass123`

---

## Role Overview

The Quality Assurance Officer is responsible for blood safety screening. Every blood unit must be screened for infectious markers before it can be released to recipients or hospitals. This user can:

- View all blood units in inventory (pending, screened, rejected)
- Log infectious marker test results (HIV, Hepatitis B, Hepatitis C, Syphilis, Malaria)
- Approve or reject blood units based on screening results
- Create, update, and delete blood unit records
- Verify expiry dates and log discarded/non-compliant units

---

## My Permissions

| Permission Code | Label | Description |
|---|---|---|
| `VIEW_INVENTORY` | View Blood Inventory | Inspect blood units awaiting screening or quarantine |
| `LOG_SCREENING` | Log Screening Results | Record infectious marker tests (HIV, HepB, HepC, Syphilis, Malaria) |
| `VERIFY_EXPIRY` | Verify Expiry Dates | Inspect expiry dates and compliance verification |
| `LOG_DISCARD` | Log Discarded Units | Record disposal and non-compliance notes for defective units |

---

## Backend Files & Methods

### PortalController.java
**Path:** `src/main/java/com/blooddonation/spring/controller/PortalController.java`

| Method | HTTP | URL | Lines | Description |
|---|---|---|---|---|
| `qaPortal()` | GET | `/portal/qa` | 247 – 259 | Load QA screening panel with all blood units |
| `logScreening()` | POST | `/portal/qa/screening` | 261 – 275 | Record 5-marker infectious disease test result |
| `qaRecords()` | GET | `/portal/qa/records` | 277 – 286 | Load blood unit records management view |
| `createQABloodUnit()` | POST | `/portal/qa/records` | 288 – 299 | Create a new blood unit record |
| `updateQABloodUnit()` | POST | `/portal/qa/records/{id}/update` | 301 – 326 | Update blood group, component, status, quality notes |
| `deleteQABloodUnit()` | POST | `/portal/qa/records/{id}/delete` | 328 – 339 | Remove a blood unit record |

### BloodDonationService.java (methods owned/used)
**Path:** `src/main/java/com/blooddonation/spring/service/BloodDonationService.java`

| Method | Description |
|---|---|
| `recordScreening(unitId, hiv, hepB, hepC, syphilis, malaria, notes)` | Records all 5 markers; sets status to "Approved - Available" or "Rejected - Failed Screening" |
| `getBloodUnits(bloodGroup)` | Returns all blood units, optionally filtered by blood group |
| `getBloodUnitById(id)` | Returns a single blood unit by ID |
| `createBloodUnit(unit)` | Saves a new blood unit record to inventory |
| `updateBloodUnit(unit)` | Persists changes to an existing blood unit |
| `deleteBloodUnit(id)` | Removes a blood unit from inventory |

### Model — BloodUnit.java
**Path:** `src/main/java/com/blooddonation/spring/model/BloodUnit.java`

Full JPA entity. Key fields:

| Field | Type | Description |
|---|---|---|
| `id` | String | Auto-generated UUID |
| `bloodGroup` | String | e.g., `A+`, `O-`, `B+` |
| `component` | String | e.g., `Whole Blood`, `Plasma`, `Platelets` |
| `branch` | String | Blood bank branch location |
| `collectedAt` | LocalDateTime | Collection timestamp |
| `expiresAt` | LocalDateTime | Expiry timestamp |
| `status` | String | `Pending Screening` / `Approved - Available` / `Rejected - Failed Screening` |
| `qualityNotes` | String | Screening notes |
| `hivPositive` | boolean | HIV marker result |
| `hepBPositive` | boolean | Hepatitis B marker result |
| `hepCPositive` | boolean | Hepatitis C marker result |
| `syphilisPositive` | boolean | Syphilis marker result |
| `malariaPositive` | boolean | Malaria marker result |

### Repository — BloodUnitRepository.java
**Path:** `src/main/java/com/blooddonation/spring/repository/BloodUnitRepository.java`

Full Spring Data JPA interface for blood unit database access.

### AppPermission.java
**Path:** `src/main/java/com/blooddonation/spring/model/AppPermission.java`

| Enum Entry | Line |
|---|---|
| `VIEW_INVENTORY` | 21 |
| `LOG_SCREENING` | 22 |
| `VERIFY_EXPIRY` | 23 |
| `LOG_DISCARD` | 24 |

### UserRole.java
**Path:** `src/main/java/com/blooddonation/spring/model/UserRole.java`

| Enum Entry | Line |
|---|---|
| `QA_OFFICER("Quality Assurance Officer")` | 6 |

### AuthService.java
**Path:** `src/main/java/com/blooddonation/spring/service/AuthService.java`

| Entry | Line | Description |
|---|---|---|
| `DEFAULT_PERMISSIONS` for QA_OFFICER | 24 | `VIEW_INVENTORY,LOG_SCREENING,VERIFY_EXPIRY,LOG_DISCARD` |
| `PORTAL_PERMISSIONS` for "qa" | 33 | Portal access permission set |
| `getPortalPath()` QA_OFFICER case | 245 | Redirect to `/portal/qa` after login |

### Database Script
| File | Path | Purpose |
|---|---|---|
| `qa-blood-units.sql` | `database/qa-blood-units.sql` | Creates `Blood_Connect` database and `blood_units` table with indexes |

---

## Frontend Files

| File | Path | Purpose |
|---|---|---|
| `qa.html` | `src/main/resources/templates/portal/qa.html` | Main QA portal — 5-marker screening form per blood unit |
| `qa-records.html` | `src/main/resources/templates/portal/qa-records.html` | Full CRUD management of blood unit records |
| `inventory.html` | `src/main/resources/templates/admin/inventory.html` | Admin-side inventory view |

---

## Key Use Cases

| Use Case | Action | Portal |
|---|---|---|
| UC-17 | View pending blood units | GET `/portal/qa` |
| PBI-17 | Record 5-marker screening test | POST `/portal/qa/screening` |
| — | Manage blood unit records (full CRUD) | `/portal/qa/records` |

---

## Screening Logic Flow

```text
Blood unit arrives → status = "Pending Screening"
  QA Officer logs into /portal/qa
  ├── Selects blood unit by ID
  ├── Records test results:
  │     HIV: positive/negative
  │     Hepatitis B: positive/negative
  │     Hepatitis C: positive/negative
  │     Syphilis: positive/negative
  │     Malaria: positive/negative
  └── System sets status:
        Any positive → "Rejected - Failed Screening"
        All negative → "Approved - Available"
                         ↓
             Unit appears in /availability (public)
             Unit appears in Recipient portal (IT25102090)
             Unit appears in Manager stock dashboard (IT25102082)
```

---

## Notes
- Blood units with status `"Approved - Available"` are the only ones shown to recipients
- Expiry check: units past `expiresAt` should be discarded using `LOG_DISCARD` permission
- `qualityNotes` field stores free-text observations during screening
