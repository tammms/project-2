package vttp.project.backend.model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection ="medicationRecord")
public class MedicationRecord {

    
    private String patientId;
    private List<Medication> medicationList;
    private List<String> healthConditionList;
    private String notes;

    public MedicationRecord(){}

    public String getPatientId() {return patientId;}
    public void setPatientId(String patientId) {this.patientId = patientId;}    

    public List<Medication> getMedicationList() {return medicationList;}
    public void setMedicationList(List<Medication> medicationList) {this.medicationList = medicationList;}

    public List<String> getHealthConditionList() {return healthConditionList;}
    public void setHealthConditionList(List<String> healthConditionList) {this.healthConditionList = healthConditionList;}

    public String getNotes() {return notes;}
    public void setNotes(String notes) {this.notes = notes;}

    @Override
    public String toString() {
        return "MedicationRecord [patientId=" + patientId + ", medicationList=" + medicationList
                + ", healthConditionList=" + healthConditionList + ", notes=" + notes + "]";
    }

    

}
