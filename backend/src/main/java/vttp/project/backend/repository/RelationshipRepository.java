package vttp.project.backend.repository;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import vttp.project.backend.exception.PatientException;
import vttp.project.backend.service.Utils;

@Repository
public class RelationshipRepository {

    @Autowired
    JdbcTemplate template;

    public Boolean addRelationship(String guardianId, String patientId, String relationship, String role)
            throws PatientException {

        String relationsId = UUID.randomUUID().toString().substring(0, 8);

        // relations_id, guardian_id, patient_id, patient_relation
        Integer result = template.update(Queries.SQL_INSERT_RELATIONSHIP, relationsId, guardianId, patientId,
                relationship, role);

        if (result != 1) {
            throw new PatientException("Error creating Relationship between Guardian and Patient");

        }

        return result > 0;

    }

    public JsonArray getRelationsDetails(String guardianId) {

        SqlRowSet rs = template.queryForRowSet(Queries.SQL_VIEW_GET_RELATIONS_DETAILS, guardianId);

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

        while (rs.next()) {
            JsonObject details = Utils.relationsDataToJson(rs);
            arrayBuilder.add(details);

        }

        return arrayBuilder.build();

    }

    public boolean patientRelationExists(String guardianId, String patientId) {

        SqlRowSet rs = template.queryForRowSet(Queries.SQL_VIEW_GET_RELATIONS_BY_PATIENT_ID,
                guardianId, patientId);

        if (!rs.next()) {
            return false;
        } else {
            int patients = rs.getInt("patient_count");
            System.out.println("\nsimilar patient count: " + patients);
            return patients > 0;
        }
    }

    public JsonObject getPatientDetailsFromRelation(String guardianId, String patientId) {

        SqlRowSet rs = template.queryForRowSet(Queries.SQL_VIEW_GET_RELATIONS_BY_GID_PID,
                guardianId, patientId);

        if (!rs.next()) {
            return Json.createObjectBuilder().build();
        } else {
            JsonObject details = Utils.relationsDataToJson(rs);
            return details;
        }
    }

    public boolean deleteRelationUser(String guardianId, String patientId) {

        return template.update(Queries.SQL_DELETE_RELATIONSHIP_USER, guardianId, patientId) > 0;
    }

    public boolean deleteRelationAdmin(String patientId) throws PatientException {
        Integer result = template.update(Queries.SQL_DELETE_RELATIONSHIP_ADMIN, patientId);

        if (result < 1) {
            throw new PatientException("Failed to delete patient relationship");
        }
        return result > 0;

    }

    public boolean updateRelationship(String patientId, String relation) throws PatientException {

        Integer result = template.update(Queries.SQL_UPDATE_RELATIONSHIP, relation, patientId);

        if (result < 1) {
            throw new PatientException("Failed to update relationship");
        }
        return result > 0;
    }

}
