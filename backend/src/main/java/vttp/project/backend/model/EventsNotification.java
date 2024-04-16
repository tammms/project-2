package vttp.project.backend.model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection ="events")
public class EventsNotification {
    
    private String guardianId;
    private String patientId;
    private String eventId;
    private String reminderType;
    private String patientName;
    private String summary;
    private Boolean isValid;
    private String location;
    private String description;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private boolean isRepeat;
    private String frequencyUnits;
    private Integer frequency;
    private boolean sendEmail;
    private List<String> attendees;


    public EventsNotification() {}

    

    public String getGuardianId() {return guardianId;}
    public void setGuardianId(String guardianId) {this.guardianId = guardianId;}

    public String getPatientId() {return patientId;}
    public void setPatientId(String patientId) {this.patientId = patientId;}

    public String getEventId() {return eventId;}
    public void setEventId(String eventId) {this.eventId = eventId;}

    public String getReminderType() {return reminderType;}
    public void setReminderType(String reminderType) {this.reminderType = reminderType;}

    public String getSummary() {return summary;}
    public void setSummary(String summary) {this.summary = summary;}

    public String getLocation() {return location;}
    public void setLocation(String location) {this.location = location;}

    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}

    public String getStartDate() {return startDate;}
    public void setStartDate(String startDate) {this.startDate = startDate;}

    public String getStartTime() {return startTime;}
    public void setStartTime(String startTime) {this.startTime = startTime;}

    public String getEndDate() {return endDate;}
    public void setEndDate(String endDate) {this.endDate = endDate;}

    public String getEndTime() {return endTime;}
    public void setEndTime(String endTime) {this.endTime = endTime;}

    public boolean isRepeat() {return isRepeat;}
    public void setRepeat(boolean isRepeat) {this.isRepeat = isRepeat;}

    public String getFrequencyUnits() {return frequencyUnits;}
    public void setFrequencyUnits(String frequencyUnits) {this.frequencyUnits = frequencyUnits;}

    public Integer getFrequency() {return frequency;}
    public void setFrequency(Integer frequency) {this.frequency = frequency;}

    public boolean isSendEmail() {return sendEmail;}
    public void setSendEmail(boolean sendEmail) {this.sendEmail = sendEmail;}

    public List<String> getAttendees() {return attendees;}
    public void setAttendees(List<String> attendees) {this.attendees = attendees;}


    public String getPatientName() {return patientName;}
    public void setPatientName(String patientName) {this.patientName = patientName;}


    public Boolean getIsValid() {return isValid;}
    public void setIsValid(Boolean isValid) {this.isValid = isValid;}

    

    

    
}
