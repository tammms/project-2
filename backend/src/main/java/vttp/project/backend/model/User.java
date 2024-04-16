package vttp.project.backend.model;

public class User {

    // guardian_id, email, password, NULL AS patientId, role, enabled
    private String roleId;
    private String guardianId;
    private String email;
    private String password;
    private String patientId;
    private String role;
    private boolean enabled;

    public User() {}

    public User(String roleId, String guardianId, String email, String password, String patientId, String role,
            boolean enabled) {
        this.roleId = roleId;
        this.guardianId = guardianId;
        this.email = email;
        this.password = password;
        this.patientId = patientId;
        this.role = role;
        this.enabled = enabled;
    }

    public String getGuardianId() {return guardianId;}
    public void setGuardianId(String guardianId) {this.guardianId = guardianId;}

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}

    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}

    public String getPatientId() {return patientId;}
    public void setPatientId(String patientId) {this.patientId = patientId;}

    public String getRole() {return role;}
    public void setRole(String role) {this.role = role;}

    public boolean isEnabled() {return enabled;}
    public void setEnabled(boolean enabled) {this.enabled = enabled;}

    public String getRoleId() {return roleId;}
    public void setRoleId(String roleId) {this.roleId = roleId;}

    
    
}
