package vttp.project.backend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import vttp.project.backend.model.MedicationRecord;
import vttp.project.backend.repository.MedicationRepository;
import vttp.project.backend.service.MedicationService;
import vttp.project.backend.service.Utils;

@RestController
@RequestMapping(path = "/api")
public class MedicationController {

    @Autowired
    MedicationService medSvc;

    @GetMapping(path = "/medicines")
    public ResponseEntity<String> getAllMedicineNames() {

        List<String> medList = medSvc.getAllMedicineNames();

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

        for (String med : medList) {
            arrayBuilder.add(med);
        }

        return ResponseEntity.ok(arrayBuilder.build().toString());
    }

    @PostMapping(path = "/medicationRecord")
    public ResponseEntity<String> addMedicationRecord(@RequestBody String payload) {

        // System.out.println("\n\npayload\n" + payload);
        if (medSvc.addNewMedicationRecord(payload)) {
            JsonObject resp = Json.createObjectBuilder()
                    .add("message", "Medical Record added Successfully")
                    .add("status", 200)
                    .build();
            return ResponseEntity.ok(resp.toString());
        } else {
            JsonObject err = Json.createObjectBuilder()
                    .add("message", "Medical Record failed to upload")
                    .add("status", 400)
                    .build();
            return ResponseEntity.status(400)
                    .body(err.toString());

        }

        // return null;
    }

    @Autowired
    MedicationRepository medRepo;

    @GetMapping(path = "/listMedicalRecords")
    public ResponseEntity<String> getMedicationRecord(@RequestParam String patientId) {

        // System.out.println("\npayload from frontend " + patientId);
        // medRepo.getDailyFrequencies(patientId);
        // medRepo.getWeeklyFrequencyUnits(patientId);

        Optional<MedicationRecord> opt = medSvc.getNewMedicationRecord(patientId);

        if (opt.isPresent()) {
            MedicationRecord medicalRecord = opt.get();
            // return ResponseEntity.ok()
            JsonObject medObj = Utils.medicationRecordToJson(medicalRecord);
            return ResponseEntity.ok(medObj.toString());
        } 
        // else {

        //     JsonObject resp = Json.createObjectBuilder()
        //             .add("message", "No Medical Records Saved")
        //             .add("status", 200)
        //             .build();
        //     return ResponseEntity.status(200)
        //             .body(resp.toString());
        // }

        return null;

    }

    @DeleteMapping(path = "/patients/{patientId}")
    @PreAuthorize("hasRole('USERADMIN')")
    public ResponseEntity<String> deleteMedicationRecord(@PathVariable String patientId) {
        System.out.println("\nDelete mapping");
        System.out.println("\npayload from frontend " + patientId);

        if (medSvc.deleteMedicationRecord(patientId)) {
            JsonObject resp = Json.createObjectBuilder()
                    .add("message", "Medical Record Deleted Successfully")
                    .add("status", 200)
                    .build();
            return ResponseEntity.ok(resp.toString());
        } else {
            JsonObject err = Json.createObjectBuilder()
                    .add("message", "Medical Record failed to delete")
                    .add("status", 400)
                    .build();
            return ResponseEntity.status(400)
                    .body(err.toString());

        }
    }



    @GetMapping(path = "/medicationFrequencies")
    public ResponseEntity<String> getMedicationFrequencies(@RequestParam String patientId){
        // System.out.println("\nGet frequencies");
        // System.out.println("\npayload from frontend " + patientId);

        String frequencyStatus = medSvc.frequencyUnitsStatus(patientId);

        List<String> frequencyList = medSvc.getMedicationFrequencies(patientId);

        if(frequencyList.size()>0){
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

            for (String freq : frequencyList) {
                arrayBuilder.add(freq);
            }

            JsonObject resp = Json.createObjectBuilder()
                                .add("frequency", arrayBuilder.build())
                                // .add("weekly", medSvc.containsWeekly(patientId))
                                .add("frequencyStatus", frequencyStatus)
                                .build();

        // System.out.println("\nResponse " + resp.toString());
            return ResponseEntity.ok(resp.toString());
            // return ResponseEntity.ok("{}");
        }

        return ResponseEntity.ok("{}");
    
    }

}
