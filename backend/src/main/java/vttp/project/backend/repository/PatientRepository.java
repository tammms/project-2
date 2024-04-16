package vttp.project.backend.repository;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import vttp.project.backend.exception.PatientException;
import vttp.project.backend.model.Patient;
import vttp.project.backend.service.Utils;

@Repository
public class PatientRepository {

    @Autowired
    JdbcTemplate template;


    public boolean patientDetailsExists(String firstName, String lastName, String birthDateString) {

        Date birthDate = Utils.convertStringToDate(birthDateString);
        
        SqlRowSet rs = template.queryForRowSet(Queries.SQL_GET_PATIENT_BY_DETAILS,
                                                        firstName, lastName, birthDate);

        if (!rs.next()) {
            return false;
        } else {
            int patients = rs.getInt("patient_count");
            System.out.println("\nsimilar patient count: " + patients);
            return patients > 0;
        }
    }

   



    public Boolean addNewPatient(Patient patient) throws PatientException {

        // INSERT INTO patient (first_name, last_name, gender, birth_date, phone_no)
        String firstName = Utils.formatUpperCase(patient.getFirstName());
        String lastName = Utils.formatUpperCase(patient.getLastName());

        Integer result =  template.update(Queries.SQL_INSERT_PATIENT, patient.getPatientId(),
                            firstName,
                            lastName,
                            patient.getGender(),
                            patient.getBirthDate(),
                            patient.getPhoneNo());
        
        if(result != 1){
            throw new PatientException("Error creating new patient");
        }                

       return result>0;         

    }


    public boolean patientIdExists(String patientId) {

        SqlRowSet rs = template.queryForRowSet(Queries.SQL_GET_PATIENT_BY_ID, patientId);

        if (!rs.next()) {
            return false;
        } else {
            int patients = rs.getInt("patient_count");
            return patients > 0;
        }
    }

    public boolean deletePatient(String patientId) throws PatientException{

        Integer result = template.update(Queries.SQL_DELETE_PATIENT, patientId);

        if(result<1){
            throw new PatientException("Failed to Delete Patient Record");
        }

        return result>0;
    }

    public boolean updatePatient(Patient patient) throws PatientException {

        Integer result = template.update(Queries.SQL_UPDATE_PATIENT,
        patient.getGender(), patient.getPhoneNo(), patient.getPatientId());

        if (result < 1) {
            throw new PatientException("Failed to update Patient Record");
        }
        return result > 0;
    }


}
