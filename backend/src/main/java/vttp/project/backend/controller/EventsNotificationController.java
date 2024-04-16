package vttp.project.backend.controller;

import java.io.StringReader;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import vttp.project.backend.model.DailyNotification;
import vttp.project.backend.model.EventsNotification;
import vttp.project.backend.service.EventsNotificationService;
import vttp.project.backend.service.Utils;

@RestController
@RequestMapping(path = "/api")
public class EventsNotificationController {

    @Autowired
    EventsNotificationService eventsSvc;

    @PostMapping(path = "/events/save")
    public ResponseEntity<String> addEventReminder(@RequestBody String payload) {


        // {"event":{"guardianId":"qwer1235","patientId":"zxcv0995","eventId":"bdebcd81","reminderType":"event","summary":"summary","location":"location","description":"description","startDate":"2024-04-13T08:14:37.108Z","startTime":"9:00","endDate":"2024-04-13T08:14:37.108Z","endTime":"10:00","isRepeat":false,"frequencyUnits":"DAILY","frequency":1,"duration":"forever","times":1,"sendEmail":false,"attendees":[]}}

        if (eventsSvc.addEvent(payload)) {
            JsonObject resp = Json.createObjectBuilder()
                    .add("message", "Event Added Successfully")
                    .add("status", 200)
                    .build();
            return ResponseEntity.ok(resp.toString());
        // return null;

        } else {
            JsonObject err = Json.createObjectBuilder()
                    .add("message", "Failed to add Event")
                    .add("status", 400)
                    .build();
            return ResponseEntity.status(400)
                    .body(err.toString());
        }

        // return null;
    }

    @PostMapping(path = "/daily/save")
    public ResponseEntity<String> addDailyReminder(@RequestBody String payload) {
        System.out.println("\nPayload " + payload);

        if (eventsSvc.addDaily(payload)) {
            JsonObject resp = Json.createObjectBuilder()
                    .add("message", "Daily Reminder Added Successfully")
                    .add("status", 200)
                    .build();
            return ResponseEntity.ok(resp.toString());
        } else {
            JsonObject err = Json.createObjectBuilder()
                    .add("message", "Failed to add Daily Reminder")
                    .add("status", 400)
                    .build();
            return ResponseEntity.status(400)
                    .body(err.toString());
        }

    }

    @PostMapping(path = "/events/send")
    public ResponseEntity<JsonObject> sendEventReminder(@RequestBody String payload) {
        JsonReader reader = Json.createReader(new StringReader(payload));
        JsonObject obj = reader.readObject();

        String guardianId = obj.getString("guardianId");
        String token = obj.getString("token");

        List<EventsNotification> eventsList = eventsSvc.getAllEvents(guardianId);
        System.out.println("\nSend Events is ok");

        if (eventsList.size() > 0) {
            try {
                eventsSvc.sendEventNotification(eventsList, token);
                JsonObject respObject = Json.createObjectBuilder()
                        .add("title", "Set Push Notification Status")
                        .add("message", "Events Successfully added")
                        // .add("token", object.getString("token"))
                        .build();

                return ResponseEntity.ok(respObject);

            } catch (Exception e) {

                JsonObject err = Json.createObjectBuilder()
                        .add("title", "Set Push Notification Status")
                        .add("message", e.getLocalizedMessage())
                        // .add("message", "error")
                        .build();
                return ResponseEntity.status(400)
                        .body(err);
            }
        } else {
            // no notifications/reminders
            JsonObject respObject = Json.createObjectBuilder()
                    .add("title", "Set Push Notification Status")
                    .add("message", "No Scheduled Event Notifications")
                    // .add("token", object.getString("token"))
                    .build();

            return ResponseEntity.ok(respObject);
        }
    }

    @PostMapping(path = "/daily/send")
    public ResponseEntity<JsonObject> sendDailyReminder(@RequestBody String payload) {
        JsonReader reader = Json.createReader(new StringReader(payload));
        JsonObject obj = reader.readObject();

        String guardianId = obj.getString("guardianId");
        String token = obj.getString("token");

        List<DailyNotification> dailyList = eventsSvc.getAllDaily(guardianId);

        if (dailyList.size() > 0) {
            try {
                eventsSvc.sendDailyNotifications(dailyList, token);
                JsonObject respObject = Json.createObjectBuilder()
                        .add("title", "Set Push Notification Status")
                        .add("message", "Daily Reminders Successfully added")
                        // .add("token", object.getString("token"))
                        .build();

                return ResponseEntity.ok(respObject);

            } catch (Exception e) {

                JsonObject err = Json.createObjectBuilder()
                        .add("title", "Set Push Notification Status")
                        .add("message", e.getLocalizedMessage())
                        // .add("message", "error")
                        .build();
                return ResponseEntity.status(400)
                        .body(err);
            }
        } else

        {
            // no notifications/reminders
            JsonObject respObject = Json.createObjectBuilder()
                    .add("title", "Set Push Notification Status")
                    .add("message", "No Scheduled Daily Notifications")
                    // .add("token", object.getString("token"))
                    .build();

            return ResponseEntity.ok(respObject);
        }
    }

    @GetMapping(path = "/events/get")
    public ResponseEntity<String> getEventReminders(@RequestParam String guardianId) {

        List<EventsNotification> eventList = eventsSvc.getAllEvents(guardianId);

        if (eventList.size() > 0) {

            JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
            for (EventsNotification event : eventList) {
                arrBuilder.add(Utils.eventsNotificationToJson(event));
            }
            JsonArray resp = arrBuilder.build();
            return ResponseEntity.ok(resp.toString());
            // return null;
        }
        return null;
    }


    @GetMapping(path = "/daily/get")
    public ResponseEntity<String> getDailyReminders(@RequestParam String guardianId) {

        List<DailyNotification> dailyList = eventsSvc.getAllDaily(guardianId);

        if (dailyList.size() > 0) {

            JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
            for (DailyNotification daily : dailyList) {
                arrBuilder.add(Utils.DailyNotificationtoJson(daily));
            }
            JsonArray resp = arrBuilder.build();
            return ResponseEntity.ok(resp.toString());
            // return null;
        }
        return null;
    }


    @GetMapping(path = "/events/test")
    public ResponseEntity<String> getPatientEventReminder(@RequestParam String guardianId) {

        // eventsSvc.getAllEvents(guardianId);
        // Utils.convertDateStringToISO("2024-04-13");
        // eventsSvc.formatEventCronExpression(null);
        Integer frequency = 2;
        String formattedExpression = String.format("0 %s %s %s %s */%d", "1", "1", "1", "1", frequency);

        formattedExpression = "0 %s %s %s %s/%d *".formatted("mins", "hours", "day", "monthAdj", frequency);
        System.out.println("\npayload: " + formattedExpression);

        return null;
    }

    @DeleteMapping(path="/reminder/delete/{eventId}")
    public ResponseEntity<String> deleteReminder(@PathVariable("eventId") String eventId) {

        System.out.println("\nDelete mapping is ok");
        // System.out.println("\npayload from frontend " + patientId);

        if (eventsSvc.deleteReminder(eventId)) {
            JsonObject resp = Json.createObjectBuilder()
                    .add("message", "Reminder Deleted Successfully! Please Refresh Window")
                    .add("status", 200)
                    .build();
            return ResponseEntity.ok(resp.toString());
        } else {
            JsonObject err = Json.createObjectBuilder()
                    .add("message", "Reminder failed to delete")
                    .add("status", 400)
                    .build();
            return ResponseEntity.status(400)
                    .body(err.toString());

        }

        // return null;
    }


}
