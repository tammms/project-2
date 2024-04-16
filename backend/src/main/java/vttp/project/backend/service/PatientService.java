package vttp.project.backend.service;

import java.io.StringReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import vttp.project.backend.exception.PatientException;
import vttp.project.backend.model.Patient;
import vttp.project.backend.repository.PatientRepository;
import vttp.project.backend.repository.RelationshipRepository;

@Service
public class PatientService {

    @Autowired
    PatientRepository patientRepo;

    @Autowired
    RelationshipRepository relationRepo;

    public boolean patientDetailsExists(String firstName, String lastName, String birthDate) {

        return patientRepo.patientDetailsExists(firstName, lastName, birthDate);
    }

    @Transactional(rollbackFor = PatientException.class)
    public boolean addNewPatient(String payload) throws PatientException {

        // Payload from
        // frontend:{"patient":{"patientId":"","firstName":"wilma","lastName":"tan","gender":"female","birthDate":"915120000000","phoneNo":"90204953","guardians":[],"medications":[],"medicalNotes":[]},"relationship":"parent","guardianId":"qwer1234"}

        JsonReader reader = Json.createReader(new StringReader(payload));
        JsonObject obj = reader.readObject();
        String patientString = obj.getJsonObject("patient").toString();

        Patient patient = Utils.stringToPatient(patientString);
        
        String relationship = obj.getString("relationship");
        String guardianId = obj.getString("guardianId");
        

        try {

            Boolean patientResult = patientRepo.addNewPatient(patient);
            Boolean relationResult = relationRepo.addRelationship(guardianId, patient.getPatientId(), relationship,
                    "ROLE_USERADMIN");

            return (patientResult && relationResult);
        } catch (Exception e) {
            throw new PatientException(e.getMessage());
        }


    }

    public boolean addExistingPatient(String payload) throws PatientException {

        JsonReader reader = Json.createReader(new StringReader(payload));
        JsonObject obj = reader.readObject();

        String guardianId = obj.getString("guardianId");
        String patientId = obj.getString("patientId");
        String relationship = obj.getString("relationship");

        if (patientRepo.patientIdExists(patientId)) {
            System.out.println("Patient ID exist");
            return relationRepo.addRelationship(guardianId, patientId, relationship, "ROLE_USER");
        } else

            return false;
    }

    @Transactional(rollbackFor = PatientException.class)
    public boolean deleteRelationAdmin(String patientId) throws PatientException {

        try {
            Boolean relation = relationRepo.deleteRelationAdmin(patientId);

            Boolean patient = patientRepo.deletePatient(patientId);
        
            return (relation && patient);

        } catch (Exception e) {
            throw new PatientException(e.getMessage());

        }

    }

    @Transactional(rollbackFor = PatientException.class)
    public boolean updatePatientDetails(String payload) throws PatientException {

        JsonReader reader = Json.createReader(new StringReader(payload));
        JsonObject obj = reader.readObject();
        String patientString = obj.getJsonObject("patient").toString();

        String relationship = obj.getString("relationship");

        JsonReader patientReader = Json.createReader(new StringReader(patientString));
        JsonObject object = patientReader.readObject();
        Patient patient = new Patient();

        patient.setPatientId(object.getString("patientId"));
        patient.setGender(object.getString("gender"));
        // Integer phone = object.getInt("phoneNo");
        // patient.setPhoneNo(phone.toString());

        try {
            patient.setPhoneNo(object.getString("phoneNo"));
                
            } catch (Exception e) {
                // TODO: handle exception
                Integer phone = object.getInt("phoneNo");
                patient.setPhoneNo(phone.toString());
            }

        try {
            Boolean relationResult = relationRepo.updateRelationship(patient.getPatientId(), relationship);
            Boolean patientResult = patientRepo.updatePatient(patient);
        
            return (relationResult && patientResult);

        } catch (Exception e) {
            throw new PatientException(e.getMessage());

        }

    }

}
