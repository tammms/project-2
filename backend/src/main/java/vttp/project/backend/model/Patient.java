package vttp.project.backend.model;

import java.util.Date;
import java.util.List;

public class Patient {

    private String patientId;
    private String firstName;
    private String lastName;
    private String gender;
    private Date birthDate;
    private Integer age;
    private String phoneNo;
    private List<String> guardianIds;
    private List<Medication> medications;
    private List<String> medicalNotes;
   

    public Patient() {}

    public String getPatientId() {return patientId;}
    public void setPatientId(String patientId) {this.patientId = patientId;}

    public String getFirstName() {return firstName;}
    public void setFirstName(String firstName) {this.firstName = firstName;}

    public String getLastName() {return lastName;}
    public void setLastName(String lastName) {this.lastName = lastName;}

    public String getGender() {return gender;}
    public void setGender(String gender) {this.gender = gender;}

    public Date getBirthDate() {return birthDate;}
    public void setBirthDate(Date birthDate) {this.birthDate = birthDate;}

    public String getPhoneNo() {return phoneNo;}
    public void setPhoneNo(String phoneNo) {this.phoneNo = phoneNo;}

    public List<String> getGuardianIds() {return guardianIds;}
    public void setGuardianIds(List<String> guardianIds) {this.guardianIds = guardianIds;}

    public List<Medication> getMedications() {return medications;}
    public void setMedications(List<Medication> medications) {this.medications = medications;}

    public List<String> getIllness() {return medicalNotes;}
    public void setIllness(List<String> medicalNotes) {this.medicalNotes = medicalNotes;}

    public Integer getAge() {return age;}
    public void setAge(Integer age) {this.age = age;}

    public List<String> getMedicalNotes() {return medicalNotes;}
    public void setMedicalNotes(List<String> medicalNotes) {this.medicalNotes = medicalNotes;}


    @Override
    public String toString() {
        return "Patient [patientId=" + patientId + ", firstName=" + firstName + ", lastName=" + lastName + ", gender="
                + gender + ", birthDate=" + birthDate + ", phoneNo=" + phoneNo + ", guardianIds=" + guardianIds
                + ", medications=" + medications + ", medicalNotes=" + medicalNotes + "]";
    }

    public static void toJson(){

    }


    

    
    
    
}
