package com.blooddonation.spring.service;

import com.blooddonation.spring.model.*;
import com.blooddonation.spring.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class BloodDonationService {

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private ContactMessageRepository messageRepository;

    @Autowired
    private BloodUnitRepository bloodUnitRepository;

    @Autowired
    private DeliveryOrderRepository deliveryOrderRepository;

    @Autowired
    private DeliveryVehicleRepository vehicleRepository;

    @Autowired
    private DeliveryCourierRepository courierRepository;

    private static final int LOW_STOCK_THRESHOLD = 10;
    private static final int CRITICAL_STOCK_THRESHOLD = 5;

    // ─── Seed initial data if DB is empty ────────────────────────────────────
    @PostConstruct
    public void seedInitialData() {
        if (donorRepository.count() == 0) {
            // Seed Donor 1
            Donor d1 = new Donor();
            d1.setName("Aman Silva");
            d1.setPhone("0771234567");
            d1.setBloodGroup("B+");
            d1.setCity("Colombo");
            d1.setNotes("Available for donations on weekends");
            d1.setAvailability("Available");
            d1.setAllowCall(true);
            d1.setLastDonatedAt(LocalDateTime.of(2026, 5, 10, 10, 0));
            d1 = donorRepository.save(d1);

            AvailabilitySlot s1a = new AvailabilitySlot("Sat", "09:00", "13:00");
            s1a.setDonor(d1);
            AvailabilitySlot s1b = new AvailabilitySlot("Sun", "14:00", "18:00");
            s1b.setDonor(d1);
            d1.setAvailabilitySlots(Arrays.asList(s1a, s1b));

            DonationHistory h1 = new DonationHistory(
                    LocalDateTime.of(2026, 5, 10, 10, 0),
                    "National Blood Center Colombo",
                    "Annual blood camp");
            h1.setDonor(d1);
            d1.setDonationHistory(Arrays.asList(h1));
            donorRepository.save(d1);

            // Seed Donor 2
            Donor d2 = new Donor();
            d2.setName("Priya Perera");
            d2.setPhone("0712345678");
            d2.setBloodGroup("O-");
            d2.setCity("Kandy");
            d2.setNotes("Universal donor");
            d2.setAvailability("Available");
            d2.setAllowCall(true);
            d2 = donorRepository.save(d2);

            AvailabilitySlot s2a = new AvailabilitySlot("Mon", "08:00", "10:00");
            s2a.setDonor(d2);
            AvailabilitySlot s2b = new AvailabilitySlot("Wed", "16:00", "18:00");
            s2b.setDonor(d2);
            d2.setAvailabilitySlots(Arrays.asList(s2a, s2b));
            donorRepository.save(d2);

            // Seed Donor 3
            Donor d3 = new Donor();
            d3.setName("Dilshan Fernando");
            d3.setPhone("0753456789");
            d3.setBloodGroup("A+");
            d3.setCity("Galle");
            d3.setNotes("Recently donated");
            d3.setAvailability("Unavailable");
            d3.setAllowCall(false);
            d3.setLastDonatedAt(LocalDateTime.of(2026, 7, 20, 8, 0));
            d3 = donorRepository.save(d3);

            DonationHistory h3 = new DonationHistory(
                    LocalDateTime.of(2026, 7, 20, 8, 0),
                    "Teaching Hospital Karapitiya",
                    "First donation");
            h3.setDonor(d3);
            d3.setDonationHistory(Arrays.asList(h3));
            donorRepository.save(d3);
        }

        if (requestRepository.count() == 0) {
            Request r1 = new Request();
            r1.setPatientName("Kamal Jayawardena");
            r1.setBloodGroup("A+");
            r1.setCity("Colombo");
            r1.setHospital("Apollo Hospital");
            r1.setUnits(2);
            r1.setContactPhone("0719876543");
            r1.setNeededBy(LocalDateTime.of(2026, 8, 5, 12, 0));
            r1.setNotes("Urgent CABG surgery scheduled. Please support.");
            r1.setUrgency("Urgent");
            r1.setStatus("Pending");
            requestRepository.save(r1);

            Request r2 = new Request();
            r2.setPatientName("Ruwan Kumara");
            r2.setBloodGroup("O-");
            r2.setCity("Kandy");
            r2.setHospital("General Hospital Kandy");
            r2.setUnits(1);
            r2.setContactPhone("0777654321");
            r2.setNeededBy(LocalDateTime.of(2026, 7, 25, 15, 0));
            r2.setNotes("Needed for emergency dengue patient. Resolved.");
            r2.setStatus("Delivered");
            requestRepository.save(r2);
        }

        if (messageRepository.count() == 0) {
            ContactMessage m1 = new ContactMessage(
                    "Lasitha Bandara",
                    "lasitha@gmail.com",
                    "Inquiry about upcoming blood camp",
                    "Hello, I would like to know if there is any upcoming blood donation camp in Colombo next month. Thanks!");
            messageRepository.save(m1);
        }
        if (bloodUnitRepository.count() == 0) {
            for (int i = 0; i < 15; i++) seedBloodUnit("A+", "Whole Blood", "Colombo Central", 35);
            for (int i = 0; i < 8; i++) seedBloodUnit("O-", "Whole Blood", "Colombo Central", 28);
            for (int i = 0; i < 4; i++) seedBloodUnit("B+", "Plasma", "Kandy Branch", 90);
            for (int i = 0; i < 3; i++) seedBloodUnit("AB-", "Platelets", "Galle Branch", 5);
            seedBloodUnit("A+", "Whole Blood", "Kandy Branch", 35);
            seedBloodUnit("O+", "Red Cells", "Colombo Central", 20);
            BloodUnit pending = new BloodUnit();
            pending.setBloodGroup("B+"); pending.setComponent("Whole Blood"); pending.setBranch("Colombo Central");
            pending.setExpiresAt(LocalDateTime.now().plusDays(30)); pending.setStatus("Pending Screening");
            bloodUnitRepository.save(pending);
        }
        if (vehicleRepository.count() == 0) {
            DeliveryVehicle v1 = new DeliveryVehicle();
            v1.setVehicleNumber("WP-CAB-1234"); v1.setType("Refrigerated Van"); v1.setRouteZone("Western Province");
            vehicleRepository.save(v1);
            DeliveryVehicle v2 = new DeliveryVehicle();
            v2.setVehicleNumber("CP-TRK-5678"); v2.setType("Cold Chain Truck"); v2.setRouteZone("Central Province");
            vehicleRepository.save(v2);
        }
        if (courierRepository.count() == 0) {
            DeliveryCourier c1 = new DeliveryCourier();
            c1.setName("Nimal Perera"); c1.setLicenseNumber("DL-123456"); c1.setContact("0771112233"); c1.setRouteZone("Western Province");
            courierRepository.save(c1);
            DeliveryCourier c2 = new DeliveryCourier();
            c2.setName("Sunil Fernando"); c2.setLicenseNumber("DL-789012"); c2.setContact("0774445566"); c2.setRouteZone("Central Province");
            courierRepository.save(c2);
        }
    }

    private void seedBloodUnit(String group, String component, String branch, int expiryDays) {
        BloodUnit unit = new BloodUnit();
        unit.setBloodGroup(group); unit.setComponent(component); unit.setBranch(branch);
        unit.setExpiresAt(LocalDateTime.now().plusDays(expiryDays));
        unit.setHivNonReactive(true); unit.setHepatitisBNonReactive(true); unit.setHepatitisCNonReactive(true);
        unit.setSyphilisNonReactive(true); unit.setMalariaNonReactive(true); unit.setStatus("Approved - Available");
        unit.setScreenedAt(LocalDateTime.now()); bloodUnitRepository.save(unit);
    }

    @Transactional(readOnly = true)
    public List<BloodUnit> getBloodUnits(String group) {
        return bloodUnitRepository.findAll().stream()
                .filter(unit -> group == null || group.isBlank() || "Any".equalsIgnoreCase(group) || unit.getBloodGroup().equalsIgnoreCase(group))
                .sorted(Comparator.comparing(BloodUnit::getExpiresAt)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<BloodUnit> getBloodUnitById(String id) {
        return bloodUnitRepository.findById(id);
    }

    public BloodUnit createBloodUnit(BloodUnit unit) throws Exception {
        if (unit == null) throw new Exception("Blood unit is required");
        if (unit.getBloodGroup() == null || unit.getBloodGroup().isBlank()) throw new Exception("Blood group is required");
        if (unit.getComponent() == null || unit.getComponent().isBlank()) unit.setComponent("Whole Blood");
        if (unit.getBranch() == null || unit.getBranch().isBlank()) unit.setBranch("Main Blood Bank");
        if (unit.getExpiresAt() == null) throw new Exception("Expiry date is required");
        if (unit.getStatus() == null || unit.getStatus().isBlank()) unit.setStatus("Pending Screening");
        unit.setBloodGroup(unit.getBloodGroup().trim().toUpperCase());
        unit.setComponent(unit.getComponent().trim());
        unit.setBranch(unit.getBranch().trim());
        return bloodUnitRepository.saveAndFlush(unit);
    }

    public BloodUnit updateBloodUnit(BloodUnit unit) throws Exception {
        if (unit == null || unit.getId() == null || unit.getId().isBlank()) {
            throw new Exception("Blood unit ID is required for updates");
        }
        if (!bloodUnitRepository.existsById(unit.getId())) {
            throw new Exception("Blood unit not found");
        }
        if (unit.getBloodGroup() == null || unit.getBloodGroup().isBlank()) throw new Exception("Blood group is required");
        if (unit.getComponent() == null || unit.getComponent().isBlank()) throw new Exception("Component is required");
        if (unit.getBranch() == null || unit.getBranch().isBlank()) throw new Exception("Branch is required");
        if (unit.getExpiresAt() == null) throw new Exception("Expiry date is required");
        unit.setBloodGroup(unit.getBloodGroup().trim().toUpperCase());
        unit.setComponent(unit.getComponent().trim());
        unit.setBranch(unit.getBranch().trim());
        if (unit.getStatus() == null || unit.getStatus().isBlank()) unit.setStatus("Pending Screening");
        return bloodUnitRepository.saveAndFlush(unit);
    }

    public boolean deleteBloodUnit(String id) throws Exception {
        if (id == null || id.isBlank()) throw new Exception("Blood unit ID is required");
        if (!bloodUnitRepository.existsById(id)) throw new Exception("Blood unit not found");
        bloodUnitRepository.deleteById(id);
        bloodUnitRepository.flush();
        return true;
    }

    @Transactional(readOnly = true)
    public long countAvailableUnits(String bloodGroup, String component, String branch) {
        return bloodUnitRepository.findAll().stream()
                .filter(u -> "Approved - Available".equals(u.getStatus()))
                .filter(u -> u.getExpiresAt().isAfter(LocalDateTime.now()))
                .filter(u -> bloodGroup == null || u.getBloodGroup().equalsIgnoreCase(bloodGroup))
                .filter(u -> component == null || u.getComponent().equalsIgnoreCase(component))
                .filter(u -> branch == null || u.getBranch().equalsIgnoreCase(branch))
                .count();
    }

    @Transactional(readOnly = true)
    public List<StockSummary> getStockDashboard(String branchFilter) {
        return bloodUnitRepository.findAll().stream()
                .filter(u -> "Approved - Available".equals(u.getStatus()))
                .filter(u -> u.getExpiresAt().isAfter(LocalDateTime.now()))
                .filter(u -> branchFilter == null || branchFilter.isBlank() || u.getBranch().equalsIgnoreCase(branchFilter))
                .collect(Collectors.groupingBy(u -> u.getBloodGroup() + "|" + u.getComponent() + "|" + u.getBranch()))
                .entrySet().stream()
                .map(e -> {
                    String[] parts = e.getKey().split("\\|");
                    long count = e.getValue().size();
                    String status = count <= CRITICAL_STOCK_THRESHOLD ? "Critical" : count <= LOW_STOCK_THRESHOLD ? "Low" : "Adequate";
                    return new StockSummary(parts[0], parts[1], parts[2], count, status);
                })
                .sorted(Comparator.comparing(StockSummary::getBloodGroup).thenComparing(StockSummary::getBranch))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StockSummary> getLowStockAlerts() {
        return getStockDashboard(null).stream()
                .filter(s -> "Low".equals(s.getStatus()) || "Critical".equals(s.getStatus()))
                .collect(Collectors.toList());
    }

    public BloodUnit registerBloodUnit(BloodUnit unit) throws Exception {
        if (unit.getBloodGroup() == null || unit.getBloodGroup().isBlank() || unit.getExpiresAt() == null) throw new Exception("Blood group and expiry date are required");
        unit.setStatus("Pending Screening"); return bloodUnitRepository.save(unit);
    }

    public BloodUnit recordScreening(String id, boolean hiv, boolean hepB, boolean hepC, boolean syphilis, boolean malaria, String notes) throws Exception {
        BloodUnit unit = bloodUnitRepository.findById(id).orElseThrow(() -> new Exception("Blood unit not found"));
        unit.setHivNonReactive(hiv); unit.setHepatitisBNonReactive(hepB); unit.setHepatitisCNonReactive(hepC);
        unit.setSyphilisNonReactive(syphilis); unit.setMalariaNonReactive(malaria); unit.setQualityNotes(notes); unit.setScreenedAt(LocalDateTime.now());
        unit.setStatus(hiv && hepB && hepC && syphilis && malaria && unit.getExpiresAt().isAfter(LocalDateTime.now()) ? "Approved - Available" : "Rejected - Non-Compliant");
        return bloodUnitRepository.save(unit);
    }

    // ─── Donor operations ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Donor> searchDonors(String q, String bloodGroup, String city) {
        List<Donor> all = donorRepository.findAll();
        return all.stream()
            .filter(d -> {
                if (bloodGroup != null && !bloodGroup.trim().isEmpty() && !bloodGroup.equalsIgnoreCase("Any")) {
                    if (!d.getBloodGroup().equalsIgnoreCase(bloodGroup.trim())) return false;
                }
                if (city != null && !city.trim().isEmpty()) {
                    if (d.getCity() == null || !d.getCity().toLowerCase().contains(city.toLowerCase().trim()))
                        return false;
                }
                if (q != null && !q.trim().isEmpty()) {
                    String query = q.toLowerCase().trim();
                    boolean matchName = d.getName() != null && d.getName().toLowerCase().contains(query);
                    boolean matchPhone = d.getPhone() != null && d.getPhone().contains(query);
                    return matchName || matchPhone;
                }
                return true;
            })
            .sorted((d1, d2) -> {
                LocalDateTime c1 = d1.getCreatedAt() != null ? d1.getCreatedAt() : LocalDateTime.MIN;
                LocalDateTime c2 = d2.getCreatedAt() != null ? d2.getCreatedAt() : LocalDateTime.MIN;
                return c2.compareTo(c1);
            })
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<Donor> getDonorById(String id) {
        return donorRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Donor> getDonorByPhone(String phone) {
        String cleanPhone = phone.replaceAll("\\D", "");
        return donorRepository.findByPhone(cleanPhone);
    }

    public Donor registerDonor(Donor donor) throws Exception {
        if (donor.getName() == null || donor.getName().trim().length() < 2) {
            throw new Exception("Invalid donor name (minimum 2 characters)");
        }
        if (donor.getCity() == null || donor.getCity().trim().isEmpty()) {
            throw new Exception("Invalid city");
        }
        String cleanPhone = donor.getPhone() != null ? donor.getPhone().replaceAll("\\D", "") : "";
        if (cleanPhone.length() < 9 || cleanPhone.length() > 15) {
            throw new Exception("Invalid phone number digits (must be 9 to 15 digits)");
        }
        donor.setPhone(cleanPhone);

        if (donor.getId() == null || donor.getId().isEmpty()) {
            if (donorRepository.existsByPhone(cleanPhone)) {
                throw new Exception("A donor with this phone number is already registered!");
            }
        }
        return donorRepository.save(donor);
    }

    public Donor updateDonor(Donor donor) throws Exception {
        Donor existing = donorRepository.findById(donor.getId())
                .orElseThrow(() -> new Exception("Donor not found"));

        existing.setName(donor.getName());
        existing.setCity(donor.getCity());
        existing.setBloodGroup(donor.getBloodGroup());
        existing.setNotes(donor.getNotes());
        existing.setAvailability(donor.getAvailability());
        existing.setAllowCall(donor.isAllowCall());

        // Update availability slots
        existing.getAvailabilitySlots().clear();
        if (donor.getAvailabilitySlots() != null) {
            donor.getAvailabilitySlots().forEach(slot -> {
                slot.setDonor(existing);
                existing.getAvailabilitySlots().add(slot);
            });
        }
        return donorRepository.save(existing);
    }

    public Donor addDonation(String donorId, LocalDateTime date, String location, String notes) throws Exception {
        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new Exception("Donor not found"));

        LocalDateTime donationDate = date != null ? date : LocalDateTime.now();
        DonationHistory rec = new DonationHistory(donationDate, location, notes);
        rec.setDonor(donor);
        donor.getDonationHistory().add(rec);
        donor.setLastDonatedAt(donationDate);
        return donorRepository.save(donor);
    }

    public boolean deleteDonor(String id) {
        if (donorRepository.existsById(id)) {
            donorRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // ─── Request operations ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Request> getRequests(String status, String bloodGroup, String city) {
        List<Request> all = requestRepository.findAll();
        return all.stream()
            .filter(r -> {
                if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("all")) {
                    if (!r.getStatus().equalsIgnoreCase(status)) return false;
                }
                if (bloodGroup != null && !bloodGroup.isEmpty() && !bloodGroup.equalsIgnoreCase("Any")) {
                    if (!r.getBloodGroup().equalsIgnoreCase(bloodGroup)) return false;
                }
                if (city != null && !city.isEmpty()) {
                    if (r.getCity() == null || !r.getCity().toLowerCase().contains(city.toLowerCase().trim()))
                        return false;
                }
                return true;
            })
            .sorted(Comparator.comparing(Request::getNeededBy, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Request::getCreatedAt, Comparator.reverseOrder()))
            .collect(Collectors.toList());
    }

            @Transactional(readOnly = true)
            public List<Request> getRequestsForRequester(String username) {
            return getRequests(null, null, null).stream()
                .filter(request -> username != null && username.equalsIgnoreCase(request.getRequesterUsername()))
                .collect(Collectors.toList());
            }

            @Transactional(readOnly = true)
            public List<Request> getRequestsForHospital(String hospital) {
            return getRequests(null, null, null).stream()
                .filter(request -> hospital != null && request.getHospital() != null
                    && hospital.equalsIgnoreCase(request.getHospital()))
                .collect(Collectors.toList());
            }

    public Request createHospitalRequisition(Request req) throws Exception {
        req.setRequestType("Hospital Requisition");
        validateRequest(req);
        long available = countAvailableUnits(req.getBloodGroup(), req.getComponent(), null);
        req.setStatus(available >= req.getUnits() ? "Submitted" : "Partial Match");
        return requestRepository.save(req);
    }

    public Request updateHospitalRequisition(Request changes, String username) throws Exception {
        Request existing = requestRepository.findById(changes.getId())
                .orElseThrow(() -> new Exception("Requisition not found"));
        if (!"Hospital Requisition".equals(existing.getRequestType())
                || existing.getRequesterUsername() == null
                || !existing.getRequesterUsername().equalsIgnoreCase(username)) {
            throw new Exception("You can only update your own hospital requisitions");
        }
        if (!List.of("Submitted", "Partial Match").contains(existing.getStatus())) {
            throw new Exception("Only open requisitions can be updated");
        }
        existing.setPatientName(changes.getPatientName());
        existing.setBloodGroup(changes.getBloodGroup());
        existing.setComponent(changes.getComponent());
        existing.setUrgency(changes.getUrgency());
        existing.setCity(changes.getCity());
        existing.setUnits(changes.getUnits());
        existing.setContactPhone(changes.getContactPhone());
        existing.setNeededBy(changes.getNeededBy());
        existing.setNotes(changes.getNotes());
        validateRequest(existing);
        long available = countAvailableUnits(existing.getBloodGroup(), existing.getComponent(), null);
        existing.setStatus(available >= existing.getUnits() ? "Submitted" : "Partial Match");
        return requestRepository.save(existing);
    }

    public boolean deleteHospitalRequisition(String id, String username) throws Exception {
        Request existing = requestRepository.findById(id)
                .orElseThrow(() -> new Exception("Requisition not found"));
        if (!"Hospital Requisition".equals(existing.getRequestType())
                || existing.getRequesterUsername() == null
                || !existing.getRequesterUsername().equalsIgnoreCase(username)) {
            throw new Exception("You can only delete your own hospital requisitions");
        }
        if (!List.of("Submitted", "Partial Match").contains(existing.getStatus())) {
            throw new Exception("Only open requisitions can be deleted");
        }
        requestRepository.deleteById(id);
        return true;
    }

    public Request createRequest(Request req) throws Exception {
        validateRequest(req);
        if (req.getRequestType() == null || req.getRequestType().isBlank()) {
            req.setRequestType("Patient Request");
        }
        long available = countAvailableUnits(req.getBloodGroup(), req.getComponent(), null);
        req.setStatus(available >= req.getUnits() ? "Pending" : "Partial Match");
        return requestRepository.save(req);
    }

    private void validateRequest(Request req) throws Exception {
        if (req.getPatientName() == null || req.getPatientName().trim().length() < 2) {
            throw new Exception("Invalid patient name (min 2 characters)");
        }
        if (req.getBloodGroup() == null || req.getBloodGroup().isEmpty()) {
            throw new Exception("Please specify blood group needed");
        }
        if (req.getComponent() == null || req.getComponent().isBlank()) {
            throw new Exception("Please specify the blood component needed");
        }
        if (req.getCity() == null || req.getCity().trim().isEmpty()) {
            throw new Exception("Please specify city");
        }
        String cleanPhone = req.getContactPhone() != null ? req.getContactPhone().replaceAll("\\D", "") : "";
        if (cleanPhone.length() < 9 || cleanPhone.length() > 15) {
            throw new Exception("Invalid contact phone number (must be 9 to 15 digits)");
        }
        req.setContactPhone(cleanPhone);
        if (req.getNeededBy() == null) {
            req.setNeededBy(LocalDateTime.now().plusDays(2));
        }
    }

    public Request updateRequestStatus(String id, String status) throws Exception {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new Exception("Request not found"));
        List<String> validStatuses = List.of("Pending", "Partial Match", "Submitted", "Approved", "Rejected", "Dispatch Scheduled", "Delivered", "Cancelled");
        if (!validStatuses.contains(status)) throw new Exception("Invalid request status");
        request.setStatus(status);
        return requestRepository.save(request);
    }

    public Request cancelRequest(String id, String username) throws Exception {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new Exception("Request not found"));
        if (request.getRequesterUsername() == null || !request.getRequesterUsername().equalsIgnoreCase(username)) {
            throw new Exception("You can only cancel your own request");
        }
        if (!List.of("Pending", "Partial Match", "Submitted").contains(request.getStatus())) {
            throw new Exception("Only open requests can be cancelled");
        }
        request.setStatus("Cancelled");
        return requestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<DeliveryOrder> getDeliveryOrders() {
        return deliveryOrderRepository.findAll().stream().sorted(Comparator.comparing(DeliveryOrder::getScheduledAt, Comparator.nullsLast(Comparator.naturalOrder()))).collect(Collectors.toList());
    }

    public DeliveryOrder scheduleDelivery(String requestId, String courierName, String vehicleNumber, LocalDateTime scheduledAt, String notes) throws Exception {
        Request request = requestRepository.findById(requestId).orElseThrow(() -> new Exception("Request not found"));
        if (!"Approved".equals(request.getStatus())) throw new Exception("Only approved requests can be scheduled for delivery");
        if (courierName == null || courierName.isBlank() || vehicleNumber == null || vehicleNumber.isBlank() || scheduledAt == null) throw new Exception("Courier, vehicle and schedule are required");
        DeliveryOrder order = new DeliveryOrder(); order.setRequest(request); order.setCourierName(courierName); order.setVehicleNumber(vehicleNumber); order.setScheduledAt(scheduledAt); order.setNotes(notes);
        request.setStatus("Dispatch Scheduled"); requestRepository.save(request); return deliveryOrderRepository.save(order);
    }

    public void updateDeliveryStatus(String id, String status) throws Exception {
        DeliveryOrder order = deliveryOrderRepository.findById(id).orElseThrow(() -> new Exception("Delivery order not found"));
        if (!List.of("Scheduled", "Delivered", "Cancelled").contains(status)) throw new Exception("Invalid delivery status");
        order.setStatus(status);
        if ("Delivered".equals(status)) { order.setDeliveredAt(LocalDateTime.now()); order.getRequest().setStatus("Delivered"); }
        if ("Cancelled".equals(status)) order.getRequest().setStatus("Approved");
        deliveryOrderRepository.save(order);
    }

    public DeliveryVehicle registerVehicle(DeliveryVehicle vehicle) throws Exception {
        if (vehicle.getVehicleNumber() == null || vehicle.getVehicleNumber().isBlank()) throw new Exception("Vehicle number is required");
        if (vehicleRepository.existsByVehicleNumber(vehicle.getVehicleNumber())) throw new Exception("Vehicle already registered");
        vehicle.setStatus("Available");
        return vehicleRepository.save(vehicle);
    }

    public DeliveryCourier registerCourier(DeliveryCourier courier) throws Exception {
        if (courier.getName() == null || courier.getName().isBlank()) throw new Exception("Courier name is required");
        if (courier.getLicenseNumber() == null || courier.getLicenseNumber().isBlank()) throw new Exception("Driver license is required");
        if (courier.getContact() == null || courier.getContact().isBlank()) throw new Exception("Contact number is required");
        courier.setStatus("Available");
        return courierRepository.save(courier);
    }

    @Transactional(readOnly = true)
    public List<DeliveryVehicle> getAvailableVehicles() {
        return vehicleRepository.findByStatus("Available");
    }

    @Transactional(readOnly = true)
    public List<DeliveryCourier> getAvailableCouriers() {
        return courierRepository.findByStatus("Available");
    }

    @Transactional(readOnly = true)
    public List<DeliveryVehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<DeliveryCourier> getAllCouriers() {
        return courierRepository.findAll();
    }

    public Request approveRequest(String id) throws Exception {
        return updateRequestStatus(id, "Approved");
    }

    // ─── Contact message operations ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ContactMessage> getAllMessages() {
        return messageRepository.findAll().stream()
            .sorted(Comparator.comparing(ContactMessage::getCreatedAt, Comparator.reverseOrder()))
            .collect(Collectors.toList());
    }

    public ContactMessage createMessage(String name, String email, String subject, String message) throws Exception {
        if (name == null || name.trim().isEmpty()) throw new Exception("Name is required");
        if (email == null || email.trim().isEmpty()) throw new Exception("Email is required");
        if (subject == null || subject.trim().isEmpty()) throw new Exception("Subject is required");
        if (message == null || message.trim().isEmpty()) throw new Exception("Message content is required");

        ContactMessage msg = new ContactMessage(name, email, subject, message);
        return messageRepository.save(msg);
    }

    public ContactMessage markMessageAsRead(String id) throws Exception {
        ContactMessage msg = messageRepository.findById(id)
                .orElseThrow(() -> new Exception("Message not found with id: " + id));
        msg.setStatus("read");
        return messageRepository.save(msg);
    }

    public boolean deleteMessage(String id) {
        if (messageRepository.existsById(id)) {
            messageRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
Commit at 2026-08-27T09:28:16
Commit at 2026-08-16T04:44:50
Commit at 2026-08-04T10:17:35
Commit at 2026-09-15T13:21:43
Commit at 2026-08-08T19:01:37
Commit at 2026-08-23T07:40:37
Commit at 2026-08-06T20:15:36
Commit at 2026-08-04T03:30:23
Commit at 2026-08-16T23:49:26
Commit at 2026-08-31T01:18:15
Commit at 2026-08-10T18:31:13
Commit at 2026-08-29T03:35:22
Commit at 2026-08-25T01:12:12
Commit at 2026-09-16T19:12:49
Commit at 2026-08-18T01:22:25
Commit at 2026-08-28T04:46:08
Commit at 2026-08-05T00:13:47
Commit at 2026-08-16T16:28:09
Commit at 2026-09-05T03:19:43
Commit at 2026-08-13T08:30:31
Commit at 2026-08-10T22:12:36
Commit at 2026-09-07T14:07:50
Commit at 2026-08-24T13:39:55
Commit at 2026-08-17T16:23:04
Commit at 2026-08-20T03:23:53
Commit at 2026-08-27T06:06:59
Commit at 2026-08-22T07:51:52
Commit at 2026-08-19T10:19:15
Commit at 2026-08-10T20:17:15
Commit at 2026-09-07T09:12:22
Commit at 2026-08-13T20:40:16
Commit at 2026-08-11T16:52:28
Commit at 2026-09-07T11:08:19
Commit at 2026-09-05T05:56:42
Commit at 2026-08-13T17:04:19
Commit at 2026-08-20T16:25:37
Commit at 2026-08-26T20:00:39
Commit at 2026-08-26T01:55:16
