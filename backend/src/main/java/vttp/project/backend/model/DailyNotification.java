package vttp.project.backend.model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection ="events")
public class DailyNotification {

    private String guardianId;
    private String patientId;
    private String eventId;
    private String reminderType;
    private Boolean isValid;
    private String patientName;
    private String description;
    private List<String> startTime;


    public DailyNotification() {}

    public String getPatientName() {return patientName;}
    public void setPatientName(String patientName) {this.patientName = patientName;}

    public String getGuardianId() {return guardianId;}
    public void setGuardianId(String guardianId) {this.guardianId = guardianId;}
    
    public String getPatientId() {return patientId;}
    public void setPatientId(String patientId) {this.patientId = patientId;}

    public String getEventId() {return eventId;}
    public void setEventId(String eventId) {this.eventId = eventId;}

    public String getReminderType() {return reminderType;}
    public void setReminderType(String reminderType) {this.reminderType = reminderType;}

    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}

    public List<String> getStartTime() {return startTime;}
    public void setStartTime(List<String> startTime) {this.startTime = startTime;}

    public Boolean getIsValid() {return isValid;}
    public void setIsValid(Boolean isValid) {this.isValid = isValid;}
}
