# IT25102084 — Hospital Coordinator
## BloodConnect Group Project — Role Contribution

**Student ID:** IT25102084  
**Assigned Role:** Hospital Coordinator  
**Portal URL:** `http://localhost:8089/portal/hospital`  
**Demo Login:** username `hospital` / password `pass123`

---

## Role Overview

The Hospital Coordinator acts on behalf of a hospital to requisition blood for patients and wards. This user can:

- Submit hospital blood requisitions linked to their hospital
- Track dispatch status of submitted requisitions
- View, create, update, and delete hospital requisition records
- Check real-time blood availability before placing a requisition
- Manage inter-facility blood transfers

---

## My Permissions

| Permission Code | Label | Description |
|---|---|---|
| `VIEW_AVAILABILITY` | View Blood Availability | Check current blood unit availability before submitting |
| `SUBMIT_REQUISITION` | Submit Hospital Requisitions | Create hospital blood requisitions for patients and wards |
| `TRACK_DISPATCH` | Track Requisitions & Dispatch | View dispatch statuses and tracking details for hospital orders |
| `MANAGE_TRANSFER` | Manage Blood Transfers | Coordinate inter-facility blood transfers and requisitions |

---

## Backend Files & Methods

### PortalController.java
**Path:** `src/main/java/com/blooddonation/spring/controller/PortalController.java`

| Method | HTTP | URL | Lines | Description |
|---|---|---|---|---|
| `hospitalPortal()` | GET | `/portal/hospital` | 113 – 123 | Load main hospital portal with active requisitions |
| `hospitalRecords()` | GET | `/portal/hospital/records` | 125 – 135 | Load full requisition records list (CRUD view) |
| `submitRequisition()` | POST | `/portal/hospital/requisition` | 137 – 158 | Submit a new hospital blood requisition |
| `cancelHospitalRequisition()` | POST | `/portal/hospital/requisition/{id}/cancel` | 160 – 171 | Cancel an existing hospital requisition |
| `createHospitalRecord()` | POST | `/portal/hospital/records` | 173 – 189 | Create a new record from the records view |
| `updateHospitalRecord()` | POST | `/portal/hospital/records/{id}/update` | 191 – 223 | Update patient name, blood group, urgency, etc. |
| `deleteHospitalRecord()` | POST | `/portal/hospital/records/{id}/delete` | 225 – 236 | Delete a hospital requisition record |
| `checkAvailability()` | GET | `/portal/hospital/check-availability` | 238 – 244 | AJAX endpoint — returns available unit count |

### BloodDonationService.java (methods used)
**Path:** `src/main/java/com/blooddonation/spring/service/BloodDonationService.java`

| Method | Description |
|---|---|
| `getRequestsForHospital(hospital)` | Retrieve requisitions filtered by hospital name |
| `createHospitalRequisition(req)` | Save new hospital requisition with type = "Hospital Requisition" |
| `updateHospitalRequisition(changes, username)` | Update requisition fields |
| `deleteHospitalRequisition(id, username)` | Delete a hospital requisition |
| `countAvailableUnits(bloodGroup, component, branch)` | Count approved available units |

### AppPermission.java
**Path:** `src/main/java/com/blooddonation/spring/model/AppPermission.java`

| Enum Entry | Line |
|---|---|
| `SUBMIT_REQUISITION` | 16 |
| `TRACK_DISPATCH` | 17 |
| `MANAGE_TRANSFER` | 18 |

### UserRole.java
**Path:** `src/main/java/com/blooddonation/spring/model/UserRole.java`

| Enum Entry | Line |
|---|---|
| `HOSPITAL_COORDINATOR("Hospital Coordinator")` | 5 |

### AuthService.java
**Path:** `src/main/java/com/blooddonation/spring/service/AuthService.java`

| Entry | Line | Description |
|---|---|---|
| `DEFAULT_PERMISSIONS` for HOSPITAL_COORDINATOR | 23 | `VIEW_AVAILABILITY,SUBMIT_REQUISITION,TRACK_DISPATCH,MANAGE_TRANSFER` |
| `PORTAL_PERMISSIONS` for "hospital" | 32 | Portal access permission set |
| `getPortalPath()` HOSPITAL_COORDINATOR case | 244 | Redirect to `/portal/hospital` after login |

---

## Frontend Files

| File | Path | Purpose |
|---|---|---|
| `hospital.html` | `src/main/resources/templates/portal/hospital.html` | Main portal — submit requisitions + view status |
| `hospital-records.html` | `src/main/resources/templates/portal/hospital-records.html` | Full CRUD management of hospital requisition records |

---

## Key Use Cases

| Use Case | Action | Portal |
|---|---|---|
| UC-09 | Submit hospital blood requisition | `POST /portal/hospital/requisition` |
| PBI-09 | Track dispatch status | `/portal/hospital` |
| — | Manage requisition records (full CRUD) | `/portal/hospital/records` |

---

## System Flow

```text
Hospital Coordinator logs in → /portal/hospital
  ├── Checks availability (AJAX: /portal/hospital/check-availability)
  ├── Submits hospital requisition (linked to their hospital name)
  │     → Request type = "Hospital Requisition"
  │     → Status = "Submitted"
  ├── Blood Bank Manager reviews and approves → "Approved"
  ├── Delivery Officer schedules dispatch → "Dispatch Scheduled"
  └── Tracks dispatch status from records view
```

---

## Notes
- `user.getHospital()` is pre-filled from the coordinator's account (e.g., "National Hospital Colombo")
- `REQUEST_TYPE` is `"Hospital Requisition"` (distinguishes from patient requests)
- Availability check AJAX call returns count: e.g., `"4 units available for B+ (Whole Blood)"`
