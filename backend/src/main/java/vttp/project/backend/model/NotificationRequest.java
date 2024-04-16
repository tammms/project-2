package vttp.project.backend.model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection ="notifications")
public class NotificationRequest {

    private String guardianId;
    private String patientId;
    private String patientFirstName;
    private ScheduledTimings scheduleTimings;
    private List<String> reminderFrequencies;
    private boolean hasWeekly;
    private Integer weeklyStart;

    public NotificationRequest() {}

    public String getGuardianId() {return guardianId;}
    public void setGuardianId(String guardianId) {this.guardianId = guardianId;}

    public String getPatientId() {return patientId;}
    public void setPatientId(String patientId) {this.patientId = patientId;}

    public String getPatientFirstName() {return patientFirstName;}
    public void setPatientFirstName(String patientFirstName) {this.patientFirstName = patientFirstName;}

    public ScheduledTimings getScheduleTimings() {return scheduleTimings;}
    public void setScheduleTimings(ScheduledTimings timings) {this.scheduleTimings = timings;}

    public List<String> getReminderFrequencies() {return reminderFrequencies;}
    public void setReminderFrequencies(List<String> reminderFrequencies) {this.reminderFrequencies = reminderFrequencies;}

    public boolean isHasWeekly() {return hasWeekly;}
    public void setHasWeekly(boolean hasWeekly) {this.hasWeekly = hasWeekly;}

    public Integer getWeeklyStart() {return weeklyStart;}
    public void setWeeklyStart(Integer weeklyStart) {this.weeklyStart = weeklyStart;}

    @Override
    public String toString() {
        return "NotificationRequest [guardianId=" + guardianId + ", patientId=" + patientId + ", patientFirstName="
                + patientFirstName + ", scheduleTimings=" + scheduleTimings + ", reminderFrequencies="
                + reminderFrequencies + ", hasWeekly=" + hasWeekly + ", weeklyStart=" + weeklyStart + "]";
    }

    
    
    
}
