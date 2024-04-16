package vttp.project.backend.controller;

import java.io.StringReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
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
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import vttp.project.backend.repository.NotificationRepository;
import vttp.project.backend.service.JwtService;
import vttp.project.backend.service.RelationshipService;

@RestController
@RequestMapping(path = "/api")
public class RelationshipController {

    @Autowired
    RelationshipService relationSvc;

    @Autowired
    JwtService jwtSvc;

    @Autowired
    NotificationRepository reminderRepo;

    @GetMapping(path = "/relationDetails")
    public ResponseEntity<String> getPatientRelationDetails(@RequestParam String guardianId) {

        reminderRepo.getAllNotificationSchedule(guardianId);

        JsonArray detailsArray = relationSvc.getPatientRelationDetails(guardianId);
        System.out.println("\nRelations from backend: " + detailsArray.size());
        System.out.println("\nRelations from backend: " + detailsArray.toString());

        return ResponseEntity.ok(detailsArray.toString());

    }

    @GetMapping(path = "/checkRelations")
    public ResponseEntity<Boolean> patientDetailsExists(@RequestParam String guardianId,
        @RequestParam String patientId) {

        if (relationSvc.patientRelationExists(guardianId, patientId)) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.ok(false);

    }

    @GetMapping(path = "/getDetailsFromRelation")
    public ResponseEntity<String> getPatientDetailsFromRelation(@RequestParam String guardianId,
            @RequestParam String patientId) {

        JsonObject detailsObj = relationSvc.getPatientDetailsFromRelation(guardianId, patientId);

        return ResponseEntity.ok(detailsObj.toString());
    }

    @PostMapping(path = "/auth/selectPatient")
    public ResponseEntity<String> selectPatient(@RequestBody String payload) {

        System.out.println("\nPayload: " + payload);

        JsonReader reader = Json.createReader(new StringReader(payload));
        JsonObject object = reader.readObject();

        String guardianId = object.getString("guardianId");
        String patientId = object.getString("patientId");

        UserDetails userDetails = relationSvc.selectPatient(guardianId, patientId);

        var jwtToken = "";

        jwtToken = jwtSvc.generateToken(userDetails);
        System.out.println("\njwtTOken: " + jwtToken);
        System.out.println("\nAuthorities: " + userDetails.getAuthorities().toString());

        JsonObject resp = Json.createObjectBuilder()
                .add("authority", userDetails.getAuthorities().toString())
                .add("token", jwtToken)
                .add("status", 200)
                .build();
        return ResponseEntity.status(200)
                .body(resp.toString());

    }

    @DeleteMapping(path = "/deleteRelation/user/{guardianId}/{patientId}")
    public ResponseEntity<String> deleteRelation(@PathVariable("guardianId") String guardianId,
    @PathVariable("patientId") String patientId){

        System.out.println("\npayload from frontend " + patientId);

        if(relationSvc.deleteRelationUser(guardianId, patientId)){
            JsonObject resp = Json.createObjectBuilder()
                    .add("message", "Relationship Deleted Successfully! Please refresh window")
                    .add("status", 200)
                    .build();
            return ResponseEntity.ok(resp.toString());
        } else {
            JsonObject err = Json.createObjectBuilder()
                    .add("message", "Reminder failed to Relationship")
                    .add("status", 400)
                    .build();
            return ResponseEntity.status(400)
                    .body(err.toString());

        }

        // return null;
    }


    

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public String userAccess() {
        return "User Content.";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('USERADMIN')")
    public String adminAccess() {
        return "Admin Content.";
    }

}
