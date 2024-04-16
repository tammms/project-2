package vttp.project.backend.controller;

import java.io.StringReader;
import java.util.List;
import java.util.Optional;

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
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import vttp.project.backend.model.NotificationRequest;
import vttp.project.backend.service.PushNotificationService;
import vttp.project.backend.service.Utils;

@RestController
@RequestMapping(path = "/api")
public class PushNotificationController {

    @Autowired
    PushNotificationService pushNotificationSvc;

    @PostMapping("/notification/send")
    public ResponseEntity<JsonObject> sendMedicationReminder(@RequestBody String payload) {

        JsonReader reader = Json.createReader(new StringReader(payload));
        JsonObject obj = reader.readObject();

        String guardianId = obj.getString("guardianId");
        String token = obj.getString("token");

        System.out.println("\nFCM token: " + token);

        List<NotificationRequest> requestList = pushNotificationSvc.getAllNotificationSchedule(guardianId);

        if (requestList.size() > 0) {

            try {
                pushNotificationSvc.sendNotification(requestList, token);

                JsonObject respObject = Json.createObjectBuilder()
                        .add("title", "Set Push Notification Status")
                        .add("message", "Reminders Successfully added")
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
                    .add("message", "No Scheduled Notifications")
                    // .add("token", object.getString("token"))
                    .build();

            return ResponseEntity.ok(respObject);
        }

    }

    @PostMapping("/notification/reminder")
    public ResponseEntity<String> changeMedicationReminder(@RequestBody String payload) {

        System.out.println("\nPayload " + payload);

        JsonReader reader = Json.createReader(new StringReader(payload));
        JsonObject obj = reader.readObject();

        String object = obj.getJsonObject("request").toString();

        // {"guardianId":"qwer1235","patientId":"zxcv0995","patientFirstName":"belly","scheduleTimings":{"beforeBreakfast":"04:02","afterBreakfast":"09:00","beforeLunch":"12:00","afterLunch":"13:00","beforeDinner":"18:00","afterDinner":"20:00"},"reminderFrequencies":["beforeDinner","beforeBreakfast","beforeLunch"],"hasWeekly":true,"weeklyStart":2}

        // System.out.println("\nNotification Request to String" + object);

        NotificationRequest request = Utils.stringToNotificationRequest(object);

        try {
            pushNotificationSvc.changeMedicationReminder(request);
            JsonObject respObject = Json.createObjectBuilder()
                    .add("message", "Reminder Successfully Added")
                    .add("status", 200)
                    .build();

            return ResponseEntity.ok(respObject.toString());

        } catch (Exception e) {
            // TODO: handle exception
            JsonObject err = Json.createObjectBuilder()
                    .add("message", "Failed to update Reminders")
                    .add("status", 400)
                    .build();
            return ResponseEntity.status(400)
                    .body(err.toString());
        }

        // return null;
    }

    @DeleteMapping(path = "/notification/delete/{guardianId}/{patientId}")
    public ResponseEntity<String> deleteNotificationRecord(@PathVariable("guardianId") String guardianId,
            @PathVariable("patientId") String patientId) {

        // System.out.println("\nDelete mapping is ok");
        // System.out.println("\npayload from frontend " + patientId);

        if (pushNotificationSvc.deteleNotificationSchedule(patientId, guardianId)) {
            JsonObject resp = Json.createObjectBuilder()
                    .add("message", "Reminder Deleted Successfully")
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

    }

    @GetMapping("/notification/schedule")
    public ResponseEntity<String> getPatientNotificationSchedule(@RequestParam String guardianId,
            @RequestParam String patientId) {

        System.out.println("\n\nGet notification schedule is ok");
        // System.out.println("\nPayload patient " + patientId);
        // System.out.println("\nPayload guradian " + guardianId);

        Optional<String> opt = pushNotificationSvc.getPatientNotificationSchedule(guardianId, patientId);

        if (opt.isPresent()) {
            String schedule = opt.get();
            return ResponseEntity.ok(schedule);
        }

        return ResponseEntity.ok(null);
    }

   
}
