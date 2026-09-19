package com.blooddonation.spring.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blood_units", indexes = {
    @Index(name = "ix_blood_units_status", columnList = "status"),
    @Index(name = "ix_blood_units_group_component", columnList = "bloodGroup, component"),
    @Index(name = "ix_blood_units_expires_at", columnList = "expiresAt")
})
public class BloodUnit {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36) private String id;
    @Column(name = "blood_group", nullable = false, length = 5) private String bloodGroup;
    @Column(name = "component", nullable = false, length = 30) private String component = "Whole Blood";
    @Column(name = "branch", nullable = false) private String branch = "Main Blood Bank";
    @Column(name = "status", nullable = false, length = 30) private String status = "Pending Screening";
    @Column(name = "collected_at") private LocalDateTime collectedAt = LocalDateTime.now();
    @Column(name = "expires_at", nullable = false) private LocalDateTime expiresAt;
    @Column(name = "hiv_non_reactive") private Boolean hivNonReactive;
    @Column(name = "hepatitis_bnon_reactive") private Boolean hepatitisBNonReactive;
    @Column(name = "hepatitis_cnon_reactive") private Boolean hepatitisCNonReactive;
    @Column(name = "syphilis_non_reactive") private Boolean syphilisNonReactive;
    @Column(name = "malaria_non_reactive") private Boolean malariaNonReactive;
    @Column(name = "cross_match_compatible") private Boolean crossMatchCompatible;
    @Column(name = "quality_notes", columnDefinition = "NVARCHAR(MAX)") private String qualityNotes;
    @Column(name = "screened_at") private LocalDateTime screenedAt;

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getBloodGroup() { return bloodGroup; } public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public String getComponent() { return component; } public void setComponent(String component) { this.component = component; }
    public String getBranch() { return branch; } public void setBranch(String branch) { this.branch = branch; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCollectedAt() { return collectedAt; } public void setCollectedAt(LocalDateTime collectedAt) { this.collectedAt = collectedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; } public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public Boolean getHivNonReactive() { return hivNonReactive; } public void setHivNonReactive(Boolean value) { hivNonReactive = value; }
    public Boolean getHepatitisBNonReactive() { return hepatitisBNonReactive; } public void setHepatitisBNonReactive(Boolean value) { hepatitisBNonReactive = value; }
    public Boolean getHepatitisCNonReactive() { return hepatitisCNonReactive; } public void setHepatitisCNonReactive(Boolean value) { hepatitisCNonReactive = value; }
    public Boolean getSyphilisNonReactive() { return syphilisNonReactive; } public void setSyphilisNonReactive(Boolean value) { syphilisNonReactive = value; }
    public Boolean getMalariaNonReactive() { return malariaNonReactive; } public void setMalariaNonReactive(Boolean value) { malariaNonReactive = value; }
    public Boolean getCrossMatchCompatible() { return crossMatchCompatible; } public void setCrossMatchCompatible(Boolean value) { crossMatchCompatible = value; }
    public String getQualityNotes() { return qualityNotes; } public void setQualityNotes(String qualityNotes) { this.qualityNotes = qualityNotes; }
    public LocalDateTime getScreenedAt() { return screenedAt; } public void setScreenedAt(LocalDateTime screenedAt) { this.screenedAt = screenedAt; }
}
