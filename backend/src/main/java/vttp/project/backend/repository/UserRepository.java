package vttp.project.backend.repository;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import vttp.project.backend.model.User;

@Repository
public class UserRepository {

    @Autowired
    JdbcTemplate template;

    public String formatRoleId(String guardianId, String patientId){
        return ("%s%s").formatted(guardianId, patientId);
    }


    public Optional<User> getUser(String roleId){

        SqlRowSet rs = template.queryForRowSet(Queries.SQL_VIEW_GET_USER_ROLE, roleId);

        if(!rs.next()){
            return Optional.empty();
        }else{
            User user = new User();
            user.setRoleId(rs.getString("role_id"));
            user.setGuardianId(rs.getString("guardian_id"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setPatientId(rs.getString("patient_id"));
            user.setRole(rs.getString("role"));
            user.setEnabled(true);
            return Optional.of(user);
        }
    }

    
    
}
