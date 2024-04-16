package vttp.project.backend.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

import vttp.project.backend.exception.PushNotificationException;
import vttp.project.backend.model.NotificationRequest;
import vttp.project.backend.model.ScheduledTimings;
import vttp.project.backend.repository.MedicationRepository;
import vttp.project.backend.repository.NotificationRepository;

@Service
public class PushNotificationService {

    @Autowired
    NotificationRepository notificationRepo;

    @Autowired
    MedicationRepository medRepo;

    @Autowired
    TaskScheduler taskScheduler;

    // key = patientid
    Map<String, List<ScheduledFuture<?>>> scheduledNotificationMap = new HashMap<>();

    public ScheduledFuture<?> createNotificationSchedule(String token, String patientName, String frequencyUnit,
            String time, String frequencyUnitsStatus, boolean hasWeekly, Integer weeklyStart)
            throws PushNotificationException {

        String frequencyUnitDisplay = Utils.convertFrequencyValue(frequencyUnit);
        Runnable sendNotification = () -> {

            Map<String, String> firebaseMessageBody = new HashMap<>();
            firebaseMessageBody.put("title", "Medication Reminder for %s".formatted(patientName));
            firebaseMessageBody.put("body", "Schedule: %s".formatted(frequencyUnitDisplay));

            try {
                Message message = Message
                        .builder()
                        .setToken(token)
                        .putAllData(firebaseMessageBody)
                        .build();

                // String response = FirebaseMessaging.getInstance().send(message);
                String response = FirebaseMessaging.getInstance().send(message);
                System.out.println("\n\nResponse: " + response);
                // return string;
            } catch (FirebaseMessagingException e) {
                System.out.println("error sending message");
                e.printStackTrace();
            }
        };

        String timingExpression = "";
        // System.out.println("cron tigger timing expression: " + time);
        // System.out.println("cron tigger timing expression: " + weeklyStart);

        if (frequencyUnitsStatus == "weekly" || hasWeekly == true) {
            timingExpression = reminderTimingConverterWeekly(time, weeklyStart);

        } else {
            timingExpression = reminderTimingConverterDaily(time);
        }

        CronTrigger timing = new CronTrigger(timingExpression);

        ScheduledFuture<?> scheduledNotification = taskScheduler.schedule(sendNotification, timing);
        if (scheduledNotification == null) {
            throw new PushNotificationException("Failed to schedule Reminder");
        }
        System.out.println(("Notification Scheduled %s for %s at: (%s)").formatted(frequencyUnitsStatus,
                frequencyUnitDisplay, timing));

        return scheduledNotification;
    }

    public String reminderTimingConverterDaily(String time) {

        // String[] timeArr = time.split(":");

        // return (("0 %s %s * * *").formatted(timeArr[1], timeArr[0]));

        // System.out.println("FOrmatted time: "+ formatted);
        // System.out.println("FOrmatted hour: "+ formatted.substring(11,13));
        // System.out.println("FOrmatted mins: "+ formatted.substring(14,16));

        String formatted = Utils.convertTimeStringToISO(time);
        String hours = formatted.substring(11,13);
        String mins = formatted.substring(14,16);

        return (("0 %s %s * * *").formatted(mins, hours));


    }

    public String reminderTimingConverterWeekly(String time, Integer weeklyStart) {

        

        String formatted = Utils.convertTimeStringToISO(time);
        String hours = formatted.substring(11,13);
        String mins = formatted.substring(14,16);

        // return (("* %s %s * * %d").formatted(mins, hours, weeklyStart));
        return (("0 %s %s * * *").formatted(mins, hours));



        // String[] timeArr = time.split(":");

        // // return (("* %s %s ? * %d").formatted(timeArr[1], timeArr[0], weeklyStart));
        // return (("0 %s %s * * *").formatted(timeArr[1], timeArr[0]));

    }



    public void addScheduleNotification(NotificationRequest request, String token) throws PushNotificationException {

        String patientId = request.getPatientId();
        List<ScheduledFuture<?>> scheduledNotificationList = new LinkedList<>();

        // delete current notifications
        if (scheduledNotificationMap.containsKey(patientId)) {
            cancelAllNotifications(patientId);
        }
        // NotificationRequest request = Utils.stringToNotificationRequest(payload);
        // String token = Utils.getTokenFromString(payload);

        // if equal false, means schedule is not added - user don't want notification
        // reminder
        // Boolean addedScheduleToDB =
        // notificationRepo.addNotificationSchedule(request);

        String patientName = request.getPatientFirstName();
        Integer weeklyStart = request.getWeeklyStart();
        String frequencyUnitsStatus = medRepo.frequencyUnitsStatus(patientId);

        ScheduledTimings inputScheduledTimings = request.getScheduleTimings();

        List<String> reminderFrequencies = new LinkedList<>();

        System.out.println("Patient frequency status: " + frequencyUnitsStatus);

        // to ensure weekly reminders don't get pushed daily - distinguish between daily
        // and weekly reminders for users who want both daily and weekly ones
        if (frequencyUnitsStatus == "both" && request.isHasWeekly() == false) {

            reminderFrequencies = request.getReminderFrequencies();
            List<String> dailyFrequencies = medRepo.getDailyFrequencies(patientId);
            List<String> weeklyFrequencies = new LinkedList<>();

            for (String reminderFreq : reminderFrequencies) {
                if (!dailyFrequencies.contains(reminderFreq)) {
                    weeklyFrequencies.add(reminderFreq);
                }
            }

            System.out.println("\nReminder frequencies: " + reminderFrequencies.toString());
            System.out.println("\nWEekly frequencies: " + weeklyFrequencies.toString());
            System.out.println("\nDaily frequencies: " + dailyFrequencies.toString());

            for (String frequencyUnit : weeklyFrequencies) {
                String time = Utils.getScheduledTiming(frequencyUnit, inputScheduledTimings);

                scheduledNotificationList.add(
                        createNotificationSchedule(token, patientName, frequencyUnit, time, frequencyUnitsStatus, true,
                                weeklyStart));

            }

            for (String frequencyUnit : dailyFrequencies) {
                String time = Utils.getScheduledTiming(frequencyUnit, inputScheduledTimings);
                scheduledNotificationList.add(
                        createNotificationSchedule(token, patientName, frequencyUnit, time, frequencyUnitsStatus, false,
                                weeklyStart));

            }

        } else {

            if (request.isHasWeekly() == true) {
                reminderFrequencies = medRepo.getWeeklyFrequencyUnits(patientId);
            } else {
                reminderFrequencies = request.getReminderFrequencies();
            }

            // System.out.println("if 1: " + medRepo.getWeeklyFrequencyUnits(patientId));
            // System.out.println("if 2: " + request.getReminderFrequencies());

            // System.out.println("Reminder Frequencies: " + frequencyUnitsStatus);

            for (String frequencyUnit : reminderFrequencies) {

                String time = Utils.getScheduledTiming(frequencyUnit, inputScheduledTimings);

                if (request.isHasWeekly() == true) {
                    // only sends out weekly reminders
                    scheduledNotificationList.add(createNotificationSchedule(token, patientName, frequencyUnit, time,
                            frequencyUnitsStatus, true,
                            weeklyStart));

                } else {
                    // only sends out daily or daily and weekly reminders
                    System.out.println("here is ok: ");

                    scheduledNotificationList.add(createNotificationSchedule(token, patientName, frequencyUnit, time,
                            frequencyUnitsStatus, false,
                            weeklyStart));
                }

            }
        }

        scheduledNotificationMap.put(patientId, scheduledNotificationList);

    }

    public void cancelAllNotifications(String patientId) {
        List<ScheduledFuture<?>> scheduledNotificationList = scheduledNotificationMap.get(patientId);

        for (ScheduledFuture<?> scheduledNotification : scheduledNotificationList) {
            scheduledNotification.cancel(false);
        }
        scheduledNotificationMap.remove(patientId);
        System.out.println("Cancelled all scheduled notifications");
    }

    public Boolean changeMedicationReminder(NotificationRequest request) {
        // NotificationRequest request = Utils.stringToNotificationRequest(payload);
        return notificationRepo.addNotificationSchedule(request);
    }

    public List<NotificationRequest> getAllNotificationSchedule(String guardianId) {
        return notificationRepo.getAllNotificationSchedule(guardianId);
    }

    public Optional<String> getPatientNotificationSchedule(String guardianId, String patientId) {
        return notificationRepo.getPatientNotificationSchedule(guardianId, patientId);
    }

    public void sendNotification(List<NotificationRequest> reminderList, String token)
            throws PushNotificationException {
        System.out.println("Number of Patients: " + reminderList.size());

        for (NotificationRequest request : reminderList) {
            System.out.println("\nRequest: " + request);
            addScheduleNotification(request, token);
            Integer count = scheduledNotificationMap.get(request.getPatientId()).size();
            System.out.println("Number of notifications scheduled for patient: " + count);
        }
    }

    public boolean deteleNotificationSchedule(String patientId, String guardianId) {

        if (scheduledNotificationMap.containsKey(patientId)) {
            cancelAllNotifications(patientId);
        }
        return notificationRepo.deteleNotificationSchedule(patientId, guardianId);
    }


  


}
