# IT25102082 — Blood Bank Manager
## BloodConnect Group Project — Role Contribution

**Student ID:** IT25102082  
**Assigned Role:** Blood Bank Manager  
**Portal URL:** `http://localhost:8089/portal/manager`  
**Demo Login:** username `manager` / password `pass123`

---

## Role Overview

The Blood Bank Manager is the central authority for blood inventory and request approval. This user:

- Monitors real-time blood stock levels across all branches and blood groups
- Receives low-stock alerts when inventory falls below threshold
- Reviews and approves urgent patient blood requests (submitted by recipients)
- Approves hospital requisitions submitted by hospital coordinators
- Manages blood unit stock records (create, update, delete)
- Generates and reviews inventory summary reports

---

## My Permissions

| Permission Code | Label | Description |
|---|---|---|
| `VIEW_STOCK` | Monitor Stock Dashboard | View real-time stock levels grouped by group, component, and branch |
| `APPROVE_REQUEST` | Approve Patient Requests | Review and approve urgent patient blood requests |
| `EMERGENCY_DISPATCH` | Emergency Requisition Approvals | Approve and authorize hospital requisitions and emergency dispatches |
| `LOW_STOCK_ALERT` | View Low Stock Alerts | Monitor automated low inventory warnings across branches |
| `VIEW_REPORTS` | View Stock Reports | Generate and review inventory summary reports |

---

## Backend Files & Methods

### PortalController.java
**Path:** `src/main/java/com/blooddonation/spring/controller/PortalController.java`

| Method | HTTP | URL | Lines | Description |
|---|---|---|---|---|
| `managerPortal()` | GET | `/portal/manager` | 417 – 429 | Load stock dashboard with low-stock alerts and pending approvals |
| `managerRecords()` | GET | `/portal/manager/records` | 431 – 440 | Load blood unit records CRUD view |
| `createManagerBloodUnit()` | POST | `/portal/manager/records` | 442 – 453 | Create a new stock record entry |
| `updateManagerBloodUnit()` | POST | `/portal/manager/records/{id}/update` | 455 – 480 | Update blood group, component, branch, expiry, status |
| `deleteManagerBloodUnit()` | POST | `/portal/manager/records/{id}/delete` | 482 – 493 | Remove a blood unit stock record |
| `approveRequest()` | POST | `/portal/manager/approve/{id}` | 495 – 506 | Approve a Pending patient or hospital request |

### BloodDonationService.java (methods owned/used)
**Path:** `src/main/java/com/blooddonation/spring/service/BloodDonationService.java`

| Method | Description |
|---|---|
| `getStockDashboard(branch)` | Returns `List<StockSummary>` — units grouped by group + component |
| `getLowStockAlerts()` | Returns blood groups/components where count < LOW_STOCK_THRESHOLD (10) or < CRITICAL_STOCK_THRESHOLD (5) |
| `approveRequest(id)` | Sets request status from "Pending" or "Submitted" → "Approved" |
| `countAvailableUnits(bloodGroup, component, branch)` | Real-time count of approved available units |
| `getRequests(status, bloodGroup, city)` | Retrieve requests filtered by status |

### Model — StockSummary.java
**Path:** `src/main/java/com/blooddonation/spring/model/StockSummary.java`

DTO (Data Transfer Object) for the stock dashboard. Fields:

| Field | Type | Description |
|---|---|---|
| `bloodGroup` | String | e.g., `A+`, `O-` |
| `component` | String | e.g., `Whole Blood`, `Plasma`, `Platelets` |
| `count` | long | Number of approved available units |
| `branch` | String | Blood bank branch (optional filter) |

### Repository — RequestRepository.java
**Path:** `src/main/java/com/blooddonation/spring/repository/RequestRepository.java`

Spring Data JPA interface for blood request database access. Includes queries for pending requests and hospital requisitions.

### AppPermission.java
**Path:** `src/main/java/com/blooddonation/spring/model/AppPermission.java`

| Enum Entry | Line |
|---|---|
| `VIEW_STOCK` | 34 |
| `APPROVE_REQUEST` | 35 |
| `EMERGENCY_DISPATCH` | 36 |
| `LOW_STOCK_ALERT` | 37 |
| `VIEW_REPORTS` | 38 |

### UserRole.java
**Path:** `src/main/java/com/blooddonation/spring/model/UserRole.java`

| Enum Entry | Line |
|---|---|
| `BLOOD_BANK_MANAGER("Blood Bank Manager")` | 8 |

### AuthService.java
**Path:** `src/main/java/com/blooddonation/spring/service/AuthService.java`

| Entry | Line | Description |
|---|---|---|
| `DEFAULT_PERMISSIONS` for BLOOD_BANK_MANAGER | 26 | `VIEW_STOCK,APPROVE_REQUEST,EMERGENCY_DISPATCH,VIEW_REPORTS,LOW_STOCK_ALERT` |
| `PORTAL_PERMISSIONS` for "manager" | 35 | Portal access permission set |
| `getPortalPath()` BLOOD_BANK_MANAGER case | 247 | Redirect to `/portal/manager` after login |

---

## Frontend Files

| File | Path | Purpose |
|---|---|---|
| `manager.html` | `src/main/resources/templates/portal/manager.html` | Stock dashboard + low-stock alerts + request approval panel |
| `manager-records.html` | `src/main/resources/templates/portal/manager-records.html` | Blood unit stock records CRUD management |
| `inventory.html` | `src/main/resources/templates/admin/inventory.html` | Admin-side inventory view |

---

## Key Use Cases

| Use Case | Action | Portal |
|---|---|---|
| UC-13 | Monitor blood stock by group/component/branch | GET `/portal/manager` |
| PBI-13 | Approve urgent patient blood request | POST `/portal/manager/approve/{id}` |
| PBI-16 | Emergency hospital requisition approval | POST `/portal/manager/approve/{id}` |
| — | View low-stock alerts | Dashboard section in `/portal/manager` |
| — | Manage blood unit records | `/portal/manager/records` |

---

## Approval Flow

```text
Recipient submits request → status = "Pending"
Hospital Coordinator submits requisition → status = "Submitted"
  ↓
Blood Bank Manager logs in → /portal/manager
  ├── Views pending requests and submitted requisitions
  ├── Checks stock dashboard (real-time counts by blood group)
  ├── Reviews patient details (blood group, component, urgency, needed-by)
  └── Approves:
        POST /portal/manager/approve/{id}
        → Request status changes to "Approved"
        → Delivery Officer (IT25102081) can now schedule dispatch
```

---

## Stock Threshold Alerts

| Level | Threshold | Alert Type |
|---|---|---|
| Low Stock | < 10 units | Warning alert shown on dashboard |
| Critical Stock | < 5 units | Critical alert shown on dashboard |

---

## Notes
- `branchFilter` query param allows filtering stock dashboard by branch
- Manager can directly create/update/delete blood unit records (same as QA, but from stock management perspective)
- `EMERGENCY_DISPATCH` permission covers approving hospital requisitions — both `APPROVE_REQUEST` and `EMERGENCY_DISPATCH` grant the approve action
