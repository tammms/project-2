package vttp.project.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import vttp.project.backend.exception.PatientException;
import vttp.project.backend.service.EventsNotificationService;
import vttp.project.backend.service.MedicationService;
import vttp.project.backend.service.PatientService;
import vttp.project.backend.service.PushNotificationService;

@RestController
@RequestMapping(path = "/api")
public class PatientController {

    @Autowired
    PatientService patientSvc;

    @Autowired
    MedicationService medSvc;

    @Autowired
    PushNotificationService notificationSvc;

    @Autowired
    EventsNotificationService eventsNotificationSvc;

    @GetMapping(path = "/checkPatient")
    public ResponseEntity<Boolean> patientDetailsExists(@RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String birthDate) {
        // https://stackoverflow.com/questions/41684114/easy-way-to-make-a-confirmation-dialog-in-angular

        // System.out.println("\nfirstName from frontend:" + firstName);
        // System.out.println("\nlastName from frontend:" + lastName);
        // System.out.println("\nbirthDate from frontend:" +
        // Utils.convertStringToDate(birthDate)
        // );

        if (patientSvc.patientDetailsExists(firstName, lastName, birthDate)) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.ok(false);

    }

    @PostMapping(path = "/newPatient")
    public ResponseEntity<String> addNewPatient(@RequestBody String payload) {

        System.out.println("\nPayload from frontend:" + payload);

        try {
            patientSvc.addNewPatient(payload);
            JsonObject resp = Json.createObjectBuilder()
                    .add("message", "Patient added Successfully")
                    .add("status", 200)
                    .build();
            return ResponseEntity.ok(resp.toString());
        } catch (PatientException e) {
            JsonObject err = Json.createObjectBuilder()
                    .add("message", e.getLocalizedMessage())
                    .add("status", 400)
                    .build();
            return ResponseEntity.status(400)
                    .body(err.toString());

        }

    }

    @PostMapping(path = "/addExisting")
    public ResponseEntity<String> patientIdExists(@RequestBody String payload) {

        System.out.println("\nPayload from frontend:" + payload);

        try {
            if (patientSvc.addExistingPatient(payload)) {
                JsonObject resp = Json.createObjectBuilder()
                        .add("message", "Patient Successfully added")
                        .build();
                return ResponseEntity.status(200)
                        .body(resp.toString());
            } else {
                JsonObject err = Json.createObjectBuilder()
                        .add("message", "Invalid Patient ID. Please create a new patient profile")
                        .build();
                return ResponseEntity.status(200)
                        .body(err.toString());
            }

        } catch (PatientException e) {
            JsonObject err = Json.createObjectBuilder()
                    .add("message", "Failed to Add Patient to Dashboard")
                    .build();
            return ResponseEntity.status(400)
                    .body(err.toString());

        }

        // return null;
    }

    @DeleteMapping(path="/deleteRelation/admin/{guardianId}/{patientId}")
    @PreAuthorize("hasRole('USERADMIN')")
    public ResponseEntity<String> deletePatientAdmin(@PathVariable("guardianId") String guardianId,
        @PathVariable("patientId") String patientId){

            System.out.println("deletemapping is ok");
            System.out.println("PatientId: " + patientId);
            System.out.println("guardianId: " + guardianId);

        try {
        patientSvc.deleteRelationAdmin(patientId);
        System.out.println("here is ok");

        medSvc.deleteMedicationRecord(patientId);
        notificationSvc.deteleNotificationSchedule(patientId, guardianId);
        eventsNotificationSvc.deleteReminderByPatientId(patientId);


        JsonObject resp = Json.createObjectBuilder()
                    .add("message", "Reminder Deleted Successfully!")
                    .add("status", 200)
                    .build();
            return ResponseEntity.ok(resp.toString());
            
        } catch (Exception e) {
            JsonObject err = Json.createObjectBuilder()
                    .add("message", e.getLocalizedMessage())
                    .add("status", 400)
                    .build();
            return ResponseEntity.status(400)
                    .body(err.toString());
        }

        // return null;
    }


    @PutMapping(path = "/patient/update/{patientId}")
    public ResponseEntity<String> updatePatientInfo(@PathVariable("patientId") String patientId,
    @RequestBody String payload){

        System.out.println("Put mapping is ok");
        System.out.println("payload: " + payload);

        try {
            patientSvc.updatePatientDetails(payload);
            JsonObject resp = Json.createObjectBuilder()
                    .add("message", "Patient Updated Successfully")
                    .add("status", 200)
                    .build();
            return ResponseEntity.ok(resp.toString());
        } catch (PatientException e) {
            JsonObject err = Json.createObjectBuilder()
                    .add("message", e.getLocalizedMessage())
                    .add("status", 400)
                    .build();
            return ResponseEntity.status(400)
                    .body(err.toString());

        }


        // return null;

    }


    
}
