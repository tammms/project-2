package vttp.project.backend.model;

public class Relationship {


    private String relationsId;
    private String guardianId;
    private String patientId;
    private String patientRelation;

    public String getRelationsId() {return relationsId;}
    public void setRelationsId(String relationsId) {this.relationsId = relationsId;}

    public String getGuardianId() {return guardianId;}
    public void setGuardianId(String guardianId) {this.guardianId = guardianId;}

    public String getPatientId() {return patientId;}
    public void setPatientId(String patientId) {this.patientId = patientId;}

    public String getPatientRelation() {return patientRelation;}
    public void setPatientRelation(String patientRelation) {this.patientRelation = patientRelation;}

    
    
}
