# IT25102081 — Delivery Coordination Officer
## BloodConnect Group Project — Role Contribution

**Student ID:** IT25102081  
**Assigned Role:** Delivery Coordination Officer  
**Portal URL:** `http://localhost:8089/portal/delivery`  
**Demo Login:** username `delivery` / password `pass123`

---

## Role Overview

The Delivery Coordination Officer manages the physical logistics of blood delivery from the blood bank to hospitals and patients. This user can:

- Register and manage refrigerated delivery vehicles
- Register and manage courier/driver personnel
- Schedule deliveries for approved blood requests
- Confirm completed deliveries or cancel failed ones
- View all approved requests awaiting dispatch assignment

---

## My Permissions

| Permission Code | Label | Description |
|---|---|---|
| `REGISTER_VEHICLE` | Register Delivery Vehicles | Register and manage cold-chain refrigerated delivery vehicles |
| `REGISTER_COURIER` | Register Delivery Couriers | Register and maintain delivery drivers and courier dispatchers |
| `SCHEDULE_DELIVERY` | Schedule Blood Deliveries | Schedule driver and vehicle dispatches for approved requisitions |
| `CONFIRM_DELIVERY` | Confirm Deliveries | Mark blood dispatches as safely delivered to destination |
| `CANCEL_DELIVERY` | Cancel Delivery Orders | Cancel dispatch orders in case of logistical impediments |

---

## Backend Files & Methods

### PortalController.java
**Path:** `src/main/java/com/blooddonation/spring/controller/PortalController.java`

| Method | HTTP | URL | Lines | Description |
|---|---|---|---|---|
| `deliveryPortal()` | GET | `/portal/delivery` | 342 – 357 | Load delivery dashboard with orders, requests, vehicles, couriers |
| `registerVehicle()` | POST | `/portal/delivery/vehicles` | 359 – 370 | Register a new refrigerated delivery vehicle |
| `registerCourier()` | POST | `/portal/delivery/couriers` | 372 – 383 | Register a new delivery courier/driver |
| `scheduleDelivery()` | POST | `/portal/delivery/schedule` | 385 – 400 | Create delivery order linking request + courier + vehicle + time |
| `updateDeliveryStatus()` | POST | `/portal/delivery/{id}/status` | 402 – 414 | Update delivery status (Confirmed / Cancelled) |

### BloodDonationService.java (methods owned/used)
**Path:** `src/main/java/com/blooddonation/spring/service/BloodDonationService.java`

| Method | Description |
|---|---|
| `registerVehicle(vehicle)` | Save a new DeliveryVehicle entity |
| `registerCourier(courier)` | Save a new DeliveryCourier entity |
| `scheduleDelivery(requestId, courierName, vehicleNumber, scheduledAt, notes)` | Create DeliveryOrder + update request status to "Dispatch Scheduled" |
| `updateDeliveryStatus(id, status)` | Update delivery order status to Confirmed / Cancelled |
| `getDeliveryOrders()` | Retrieve all delivery orders |
| `getAllVehicles()` | Retrieve all registered vehicles |
| `getAllCouriers()` | Retrieve all registered couriers |
| `getAvailableCouriers()` | Retrieve couriers with Available status |
| `getAvailableVehicles()` | Retrieve vehicles with Available status |

### Model — DeliveryOrder.java
**Path:** `src/main/java/com/blooddonation/spring/model/DeliveryOrder.java`

| Field | Type | Description |
|---|---|---|
| `id` | String | Auto-generated UUID |
| `requestId` | String | Linked blood request ID |
| `courierName` | String | Assigned courier name |
| `vehicleNumber` | String | Assigned vehicle plate number |
| `scheduledAt` | LocalDateTime | Planned dispatch time |
| `status` | String | `Scheduled` / `Confirmed` / `Cancelled` |
| `notes` | String | Delivery notes |

### Model — DeliveryVehicle.java
**Path:** `src/main/java/com/blooddonation/spring/model/DeliveryVehicle.java`

| Field | Description |
|---|---|
| `vehicleNumber` | License plate (primary key) |
| `vehicleType` | e.g., Van, Motorcycle |
| `coldChainCapable` | boolean — refrigerated? |
| `status` | Available / On Route / Maintenance |

### Model — DeliveryCourier.java
**Path:** `src/main/java/com/blooddonation/spring/model/DeliveryCourier.java`

| Field | Description |
|---|---|
| `id` | Auto-generated UUID |
| `name` | Courier full name |
| `phone` | Contact number |
| `licenseNumber` | Driver license |
| `status` | Available / On Duty |

### Repository Files
| File | Path |
|---|---|
| `DeliveryOrderRepository.java` | `src/main/java/com/blooddonation/spring/repository/DeliveryOrderRepository.java` |
| `DeliveryVehicleRepository.java` | `src/main/java/com/blooddonation/spring/repository/DeliveryVehicleRepository.java` |
| `DeliveryCourierRepository.java` | `src/main/java/com/blooddonation/spring/repository/DeliveryCourierRepository.java` |

### AppPermission.java
**Path:** `src/main/java/com/blooddonation/spring/model/AppPermission.java`

| Enum Entry | Line |
|---|---|
| `REGISTER_VEHICLE` | 27 |
| `REGISTER_COURIER` | 28 |
| `SCHEDULE_DELIVERY` | 29 |
| `CONFIRM_DELIVERY` | 30 |
| `CANCEL_DELIVERY` | 31 |

### UserRole.java
**Path:** `src/main/java/com/blooddonation/spring/model/UserRole.java`

| Enum Entry | Line |
|---|---|
| `DELIVERY_OFFICER("Delivery Coordination Officer")` | 7 |

### AuthService.java
**Path:** `src/main/java/com/blooddonation/spring/service/AuthService.java`

| Entry | Line | Description |
|---|---|---|
| `DEFAULT_PERMISSIONS` for DELIVERY_OFFICER | 25 | `REGISTER_VEHICLE,REGISTER_COURIER,SCHEDULE_DELIVERY,CANCEL_DELIVERY,CONFIRM_DELIVERY` |
| `PORTAL_PERMISSIONS` for "delivery" | 34 | Portal access permission set |
| `getPortalPath()` DELIVERY_OFFICER case | 246 | Redirect to `/portal/delivery` after login |

---

## Frontend Files

| File | Path | Purpose |
|---|---|---|
| `delivery.html` | `src/main/resources/templates/portal/delivery.html` | Main delivery portal — vehicle, courier, schedule management |
| `deliveries.html` | `src/main/resources/templates/admin/deliveries.html` | Admin delivery management view |

---

## Key Use Cases

| Use Case | Action | Portal |
|---|---|---|
| UC-02 | Register delivery vehicles | POST `/portal/delivery/vehicles` |
| PBI-01 | Register couriers | POST `/portal/delivery/couriers` |
| — | Schedule delivery for approved request | POST `/portal/delivery/schedule` |
| — | Confirm or cancel delivery | POST `/portal/delivery/{id}/status` |

---

## Delivery Scheduling Flow

```text
Blood Bank Manager approves request → status = "Approved"
  ↓
Delivery Officer logs in → /portal/delivery
  ├── Views approved requests (awaiting dispatch)
  ├── Selects request to deliver
  ├── Assigns available courier + available vehicle
  ├── Sets scheduled dispatch date/time
  ├── Submits → DeliveryOrder created
  │              Request status → "Dispatch Scheduled"
  └── On delivery:
        Confirms → status = "Confirmed" (Delivered)
        Problem   → status = "Cancelled"
```

---

## Notes
- Only requests with status `"Approved"` appear in the scheduling dropdown
- Vehicles and couriers marked as "Available" are shown in dropdown lists
- Scheduling a delivery automatically updates the linked Request status to `"Dispatch Scheduled"`
