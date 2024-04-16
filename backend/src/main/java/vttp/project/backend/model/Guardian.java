package vttp.project.backend.model;

import java.util.List;

public class Guardian {

    // relationship?
    private String guardianId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNo;
    private String password;
    private List<Patient> patients;
    private String role = "ROLE_USER";
    private boolean enabled = true;
    
    public Guardian(){}

    public Guardian(String guardianId, String firstName, String lastName, String email, String phoneNo, String password,
            List<Patient> patients, String role, boolean enabled) {
        this.guardianId = guardianId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNo = phoneNo;
        this.password = password;
        this.patients = patients;
        this.role = role;
        this.enabled = enabled;
    }

    public String getRole() {return role;}
    public void setRole(String role) {this.role = role;}

    public boolean isEnabled() {return enabled;}
    public void setEnabled(boolean enabled) {this.enabled = enabled;}

    public String getGuardianId() {return guardianId;}
    public void setGuardianId(String guardianId) {this.guardianId = guardianId;}

    public String getFirstName() {return firstName;}
    public void setFirstName(String firstName) {this.firstName = firstName;}

    public String getLastName() {return lastName;}
    public void setLastName(String lastName) {this.lastName = lastName;}

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}

    public String getPhoneNo() {return phoneNo;}
    public void setPhoneNo(String phoneNo) {this.phoneNo = phoneNo;}

    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}

    public List<Patient> getPatients() {return patients;}
    public void setPatients(List<Patient> patients) {this.patients = patients;}


    

    
    
}
