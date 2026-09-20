# IT25102090 — Recipient / Patient
## BloodConnect Group Project — Role Contribution

**Student ID:** IT25102090  
**Assigned Role:** Recipient / Patient  
**Portal URL:** `http://localhost:8089/portal/recipient`  
**Demo Login:** username `recipient` / password `pass123`

---

## Role Overview

The Recipient / Patient role represents a hospital patient or individual in urgent need of blood. This user can:

- Search and view approved, available blood units by blood group
- Submit urgent patient blood requests with needed-by date and contact details
- Track and cancel their own submitted requests
- See real-time status updates (Pending → Approved → Dispatch Scheduled → Delivered)

---

## My Permissions

| Permission Code | Label | Description |
|---|---|---|
| `VIEW_AVAILABILITY` | View Blood Availability | Search and view screened, approved blood units |
| `SUBMIT_REQUEST` | Submit Blood Requests | Create and submit urgent patient blood requests |
| `TRACK_REQUEST` | Track Request Status | Monitor progress and status of submitted requests |

---

## Backend Files & Methods

### PortalController.java
**Path:** `src/main/java/com/blooddonation/spring/controller/PortalController.java`

| Method | HTTP | URL | Lines | Description |
|---|---|---|---|---|
| `recipientPortal()` | GET | `/portal/recipient` | 67 – 79 | Load recipient dashboard — available units + my requests |
| `submitBloodRequest()` | POST | `/portal/recipient/request` | 81 – 97 | Submit new blood request with urgency and needed-by date |
| `cancelRecipientRequest()` | POST | `/portal/recipient/request/{id}/cancel` | 99 – 110 | Cancel a pending request by request ID |

### AppPermission.java
**Path:** `src/main/java/com/blooddonation/spring/model/AppPermission.java`

| Enum Entry | Lines |
|---|---|
| `VIEW_AVAILABILITY` | 11 |
| `SUBMIT_REQUEST` | 12 |
| `TRACK_REQUEST` | 13 |

### UserRole.java
**Path:** `src/main/java/com/blooddonation/spring/model/UserRole.java`

| Enum Entry | Line |
|---|---|
| `RECIPIENT("Recipient / Patient")` | 4 |

### AuthService.java
**Path:** `src/main/java/com/blooddonation/spring/service/AuthService.java`

| Entry | Line | Description |
|---|---|---|
| `DEFAULT_PERMISSIONS` for RECIPIENT | 22 | `VIEW_AVAILABILITY,SUBMIT_REQUEST,TRACK_REQUEST` |
| `PORTAL_PERMISSIONS` for "recipient" | 31 | Portal access permission set |
| `getPortalPath()` RECIPIENT case | 243 | Redirect to `/portal/recipient` after login |

### Shared Models Used
| File | Path | Role |
|---|---|---|
| `Request.java` | `src/main/java/com/blooddonation/spring/model/Request.java` | Blood request entity |
| `BloodUnit.java` | `src/main/java/com/blooddonation/spring/model/BloodUnit.java` | Blood unit availability data |

---

## Frontend Files

| File | Path | Purpose |
|---|---|---|
| `recipient.html` | `src/main/resources/templates/portal/recipient.html` | Main recipient portal UI — availability search + request form + status tracking |
| `availability.html` | `src/main/resources/templates/availability.html` | Public blood availability search page |
| `requests.html` | `src/main/resources/templates/requests.html` | Public blood request board |

---

## Key Use Cases

| Use Case | Action | Portal |
|---|---|---|
| UC-06 | View blood availability by group | `/portal/recipient` |
| PBI-05 | Submit urgent patient blood request | `POST /portal/recipient/request` |
| PBI-06 | Track and cancel own requests | `/portal/recipient` + cancel POST |

---

## System Flow

```text
Recipient logs in → /portal/recipient
  ├── Views available blood units (Approved - Available status)
  ├── Fills request form (patient name, blood group, component, units, needed-by)
  ├── Submits request → status becomes "Pending"
  ├── Blood Bank Manager reviews and approves → "Approved"
  ├── Delivery Officer schedules dispatch → "Dispatch Scheduled"
  └── Tracks request status on same dashboard
```

---

## Notes
- Requests are visible to the Blood Bank Manager (IT25102082) for approval
- `REQUEST_TYPE` for recipient is `"Patient Request"`
- Cancellation only allowed if request is still in Pending status
