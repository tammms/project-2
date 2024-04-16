package vttp.project.backend.service;

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import vttp.project.backend.exception.PushNotificationException;
import vttp.project.backend.model.DailyNotification;
import vttp.project.backend.model.EventsNotification;
import vttp.project.backend.repository.EventsNotificationRepository;

@Service
public class EventsNotificationService {

    @Autowired
    EventsNotificationRepository eventsRepo;

    @Autowired
    TaskScheduler taskScheduler;

    // key = eventId
    Map<String, List<ScheduledFuture<?>>> scheduledReminderMap = new HashMap<>();

    public void cancelReminder(String eventId) {
        List<ScheduledFuture<?>> scheduledReminder = scheduledReminderMap.get(eventId);
        for (ScheduledFuture<?> reminder : scheduledReminder) {
            reminder.cancel(false);
        }

        scheduledReminderMap.remove(eventId);
        System.out.println("\nCancelled scheduled Reminder for: %s".formatted(eventId));
    }

    ////////////////////////////////////////////////////////////////////////
    // Events reminders
    ////////////////////////////////////////////////////////////////////////

    public boolean addEvent(String payload) {

        JsonReader reader = Json.createReader(new StringReader(payload));
        JsonObject obj = reader.readObject();
        String object = obj.getJsonObject("event").toString();

        EventsNotification event = Utils.stringToEventNotification(object);
        return eventsRepo.addEvent(event);
    }

    // DAILY
    // title: Daily Reminder for (patient name)
    // body: description

    public List<EventsNotification> getAllEvents(String guardianId) {
        return eventsRepo.getAllEvents(guardianId);
    }

    public void sendEventNotification(List<EventsNotification> eventsList, String token)
            throws PushNotificationException {

        for (EventsNotification event : eventsList) {
            Integer count = 0;
            addEventNotificationSchedule(event, token);
            count += 1;
            System.out.println("Number of Events Scheudled for %s: %d".formatted(event.getPatientName(), count));
        }
    }

    public void addEventNotificationSchedule(EventsNotification event, String token) throws PushNotificationException {

        String eventId = event.getEventId();

        if (scheduledReminderMap.containsKey(eventId)) {
            cancelReminder(eventId);
        }

        Boolean isRepeat = event.isRepeat();

        if (isRepeat == false) {
            // whether start date is before current date:
            // if reminded triggered already = return false (invalid)
            Boolean eventReminderStatus = Utils.checkEventDateValidity(event.getStartDate(), event.getStartTime());

            // whether end date is before current date:
            Boolean eventEndStatus = Utils.checkEventDateValidity(event.getEndDate(), event.getEndTime());

            // if (eventReminderStatus == false) {
            // if (eventEndStatus == true) {
            // // reminder has been triggered, but event has not ended
            // event.setIsValid(false);
            // eventsRepo.addEvent(event);
            // System.out.println("\nScheduled Date is later than Current Date");
            // System.out.println("End Date is earlier than Current Date");
            // System.out.println("Change validity in repo");
            // System.out.println("Don't schedule reminder");

            // } else {
            // eventsRepo.deleteReminder(eventId);
            // System.out.println("\nScheduled Date is later than Current Date");
            // System.out.println("End Date is later than Current Date");
            // System.out.println("delete reminder from repo");
            // System.out.println("Don't schedule reminder");

            // }
            // } else {
            // ScheduledFuture<?> scheduledReminder = createNotificationSchedule(event,
            // token);
            // // Arrays.asList(scheduledReminder);
            // scheduledReminderMap.put(eventId, Arrays.asList(scheduledReminder));

            // System.out.println("\nScheduled Date is earlier than Current Date");
            // System.out.println("Schedule reminder");
            // }

            // } else {
            // ScheduledFuture<?> scheduledReminder = createNotificationSchedule(event,
            // token);
            // // Arrays.asList(scheduledReminder);
            // scheduledReminderMap.put(eventId, Arrays.asList(scheduledReminder));
            // }

            if (eventReminderStatus == false) {
                System.out.println("\nScheduled Date is later than Current Date");
                System.out.println("\nSchedule reminder");

                ScheduledFuture<?> scheduledReminder = createNotificationSchedule(event, token);
                // Arrays.asList(scheduledReminder);
                scheduledReminderMap.put(eventId, Arrays.asList(scheduledReminder));

            } else {
                System.out.println("\nScheduled Date is earlier than Current Date");
                System.out.println("Reminder has been sent");

                if (eventEndStatus == true) {
                    System.out.println("End Date is earlier than Current Date");
                    System.out.println("Delete reminder from repo");
                    System.out.println("Don't schedule reminder");
                    // reminder has been triggered, but event has not ended
                    eventsRepo.deleteReminder(eventId);

                    
                } else {
                    System.out.println("\nEnd Date is after than Current Date");
                    System.out.println("Update validity");
                    System.out.println("Don't schedule reminder");
                    event.setIsValid(false);
                    eventsRepo.addEvent(event);

                }

            }

        } else {
            ScheduledFuture<?> scheduledReminder = createNotificationSchedule(event, token);
            // Arrays.asList(scheduledReminder);
            scheduledReminderMap.put(eventId, Arrays.asList(scheduledReminder));

        }

    }

    // EVENT
    // title: Reminder for (patient name)
    // body: summary
    public ScheduledFuture<?> createNotificationSchedule(EventsNotification event, String token)
            throws PushNotificationException {

        Runnable sendNotification = () -> {

            Map<String, String> firebaseMessageBody = new HashMap<>();
            firebaseMessageBody.put("title", "Reminder for %s".formatted(event.getPatientName()));
            firebaseMessageBody.put("body", "Description: %s".formatted(event.getSummary()));

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

        String timingExpression = formatEventCronExpression(event);
        CronTrigger timing = new CronTrigger(timingExpression);

        ScheduledFuture<?> scheduledNotification = taskScheduler.schedule(sendNotification, timing);
        if (scheduledNotification == null) {
            throw new PushNotificationException("Failed to schedule Reminder");
        }

        System.out.println(("Notification Scheduled at:(%s)").formatted(timing));

        return scheduledNotification;
        // return null;
    }

    public String formatEventCronExpression(EventsNotification event) {

        String dateFormatISO = Utils.convertDateStringToISO(event.getStartDate());
        String year = dateFormatISO.substring(0, 4);
        String month = dateFormatISO.substring(5, 7);
        String day = dateFormatISO.substring(8, 10);

        String timeFormatISO = Utils.convertTimeStringToISO(event.getStartTime());
        String hours = timeFormatISO.substring(11, 13);
        String mins = timeFormatISO.substring(14, 16);

        // String[] startDateArr = event.getStartDate().split("-");
        // String year = startDateArr[0];
        // String month = startDateArr[1];
        // String day = startDateArr[2];

        // String[] timeArr = event.getStartTime().split(":");
        // String hours = timeArr[0];
        // String mins = timeArr[1];

        String frequencyUnits = event.getFrequencyUnits();
        int frequency = event.getFrequency();

        String formattedExpression = "";

        Boolean isRepeat = event.isRepeat();

        System.out.println("\nisRepeated: " + isRepeat);
        System.out.println("\nFrquency UNits: " + frequencyUnits);
        System.out.println("\nFrquency: " + frequency);

        if (isRepeat) {
            // true - need repeat
            switch (frequencyUnits) {
                case "DAILY":
                    formattedExpression = "0 %s %s %s/%d * *".formatted(mins, hours, day, frequency, month);
                    return formattedExpression;

                case "WEEKLY":
                    formattedExpression = "0 %s %s %s/%d * *".formatted(mins, hours, day, frequency * 7, month);
                    return formattedExpression;

                case "MONTHLY":
                    formattedExpression = "0 %s %s %s %s/%d *".formatted(mins, hours, day, month, frequency);
                    return formattedExpression;

                case "YEARLY":
                    formattedExpression = "0 %s %s %s %s */%d".formatted(mins, hours, day, month, frequency);
                    return formattedExpression;
                default:
                    formattedExpression = ("0 %s %s %s %s *").formatted(mins, hours, day, month, year);
                    return formattedExpression;

            }

        } else {
            // false - no repeat
            formattedExpression = ("0 %s %s %s %s *").formatted(mins, hours, day, month);
        }

        // System.out.println("FOrmatted time: " + dateFormatISO);
        // System.out.println("FOrmatted year: "+ year);
        // System.out.println("FOrmatted month: "+ month);
        // System.out.println("FOrmatted day: "+ day);

        // System.out.println("FOrmatted time: " + timeFormatISO);
        // System.out.println("FOrmatted hour: "+ timeFormatISO.substring(11,13));
        // System.out.println("FOrmatted mins: "+ timeFormatISO.substring(14,16));
        System.out.println("\nisRepeated: " + isRepeat);
        System.out.println("\nFrquency UNits: " + frequencyUnits);
        System.out.println("\nFrquency: " + frequency);
        System.out.println("\nExpression: " + formattedExpression);

        return formattedExpression;

    }

    ////////////////////////////////////////////////////////////////////////
    // Dailys reminders
    ////////////////////////////////////////////////////////////////////////

    public boolean addDaily(String payload) {
        JsonReader reader = Json.createReader(new StringReader(payload));
        JsonObject obj = reader.readObject();
        String object = obj.getJsonObject("daily").toString();

        DailyNotification daily = Utils.stringToDailyNotification(object);
        return eventsRepo.addDaily(daily);
    }

    public boolean deleteReminder(String eventId) {
        if (scheduledReminderMap.containsKey(eventId)) {
            cancelReminder(eventId);
        }
        return eventsRepo.deleteReminder(eventId);
    }

    public List<DailyNotification> getAllDaily(String guardianId) {
        return eventsRepo.getAllDaily(guardianId);
    }

    public void sendDailyNotifications(List<DailyNotification> dailyList, String token)
            throws PushNotificationException {

        for (DailyNotification daily : dailyList) {
            String eventId = daily.getEventId();
            List<ScheduledFuture<?>> dailyNotificationList = addDailyNotification(daily, token);
            scheduledReminderMap.put(eventId, dailyNotificationList);

            System.out.println("Number of Daily Scheudled for %s: %d".formatted(daily.getPatientName(),
                    scheduledReminderMap.get(eventId).size()));
        }

    }

    public List<ScheduledFuture<?>> addDailyNotification(DailyNotification daily, String token)
            throws PushNotificationException {

        String eventId = daily.getEventId();

        if (scheduledReminderMap.containsKey(eventId)) {
            cancelReminder(eventId);
        }

        List<ScheduledFuture<?>> dailyNotificationList = new LinkedList<>();
        List<String> timingsList = daily.getStartTime();

        for (String time : timingsList) {
            dailyNotificationList.add(createDailySchedule(daily, token, time));
        }

        return dailyNotificationList;
    }

    public ScheduledFuture<?> createDailySchedule(DailyNotification daily, String token, String startTime)
            throws PushNotificationException {

        Runnable sendNotification = () -> {

            Map<String, String> firebaseMessageBody = new HashMap<>();
            firebaseMessageBody.put("title", "Reminder for %s".formatted(daily.getPatientName()));
            firebaseMessageBody.put("body", "Description: %s\n for %s".formatted(daily.getDescription(), startTime));

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

        String timingExpression = formatDailyCronExpression(startTime);

        CronTrigger timing = new CronTrigger(timingExpression);

        ScheduledFuture<?> scheduledNotification = taskScheduler.schedule(sendNotification, timing);
        if (scheduledNotification == null) {
            throw new PushNotificationException("Failed to schedule Reminder");
        }

        System.out.println(("Notification Scheduled at:(%s)").formatted(timing));

        return scheduledNotification;
        // return null;
    }

    public String formatDailyCronExpression(String time) {

        String formatted = Utils.convertTimeStringToISO(time);
        String hours = formatted.substring(11, 13);
        String mins = formatted.substring(14, 16);

        return (("0 %s %s * * *").formatted(mins, hours));

        // String[] timeArr = time.split(":");

        // return (("0 %s %s * * *").formatted(timeArr[1], timeArr[0]));

    }


    public boolean deleteReminderByPatientId(String patientId) {
        
        return eventsRepo.deleteReminderByPatientId(patientId);
    }

}
