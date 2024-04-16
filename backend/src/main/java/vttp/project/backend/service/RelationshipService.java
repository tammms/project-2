package vttp.project.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import vttp.project.backend.model.MyPrincipal;
import vttp.project.backend.repository.RelationshipRepository;
import vttp.project.backend.repository.UserRepository;

@Service
public class RelationshipService {

    @Autowired
    RelationshipRepository relationRepo;

    @Autowired
    UserRepository userRepo;

    @Autowired
    AuthenticationManager authenticationManager;

    public JsonArray getPatientRelationDetails(String guardianId){
        return relationRepo.getRelationsDetails(guardianId);
    }

    public boolean patientRelationExists(String guardianId, String patientId) {
        return relationRepo.patientRelationExists(guardianId, patientId);
    }

    public JsonObject getPatientDetailsFromRelation(String guardianId, String patientId){
        return relationRepo.getPatientDetailsFromRelation(guardianId, patientId);
    }



    public UserDetails selectPatient(String guardianId, String patientId){

        String roleId = userRepo.formatRoleId(guardianId, patientId);
        UserDetails user = userRepo.getUser(roleId)
                .map(MyPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Error: Patient Relation not found"));
            
        return user;
    }

    public boolean deleteRelationUser(String guardianId, String patientId){
        return relationRepo.deleteRelationUser(guardianId, patientId);
    }

    

    
    
}
