package vttp.project.backend.repository;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import vttp.project.backend.model.Guardian;
import vttp.project.backend.service.Utils;

@Repository
public class GuardianRepository {

    @Autowired
    private JdbcTemplate template;

    public boolean guardianEmailExists(String email) {

        SqlRowSet rs = template.queryForRowSet(Queries.SQL_CHECK_GUARDIAN_EMAIL_EXIST, email);

        if (!rs.next()) {
            return false;
        } else {
            int emails = rs.getInt("email_count");
            return emails > 0;
        }

    }

    public Optional<Guardian> getGuardian(String email, String password) {

        SqlRowSet rs = template.queryForRowSet(Queries.SQL_GET_GUARDIAN, email);

        if (!rs.next()) {
            return Optional.empty();
        } else {
            Guardian guardian = new Guardian();
            guardian.setGuardianId(rs.getString("guardian_id"));
            guardian.setFirstName(rs.getString("first_name"));
            guardian.setLastName(rs.getString("last_name"));
            guardian.setEmail(rs.getString("email"));
            guardian.setPhoneNo(rs.getString("phone_no"));
            guardian.setPassword(rs.getString("password"));
            return Optional.of(guardian);
        }
    }

    public Boolean addGuardian(Guardian guardian, String encodedPassword) {

        String firstName = Utils.formatUpperCase(guardian.getFirstName());
        String lastName = Utils.formatUpperCase(guardian.getLastName());

        if (!guardianEmailExists(guardian.getEmail())) {

            return template.update(Queries.SQL_INSERT_GUARDIAN,
                    guardian.getGuardianId(),
                    firstName,
                    lastName,
                    guardian.getEmail(),
                    guardian.getPhoneNo(),
                    encodedPassword) > 0;
        }

        return false;

    }

    public Optional<Guardian> getGuardianByID(String guardianId) {

        SqlRowSet rs = template.queryForRowSet(Queries.SQL_GET_GUARDIAN_BY_ID, guardianId);

        if (!rs.next()) {
            return Optional.empty();
        } else {
            Guardian guardian = new Guardian();
            guardian.setGuardianId(rs.getString("guardian_id"));
            guardian.setFirstName(rs.getString("first_name"));
            guardian.setLastName(rs.getString("last_name"));
            guardian.setEmail(rs.getString("email"));
            guardian.setPhoneNo(rs.getString("phone_no"));
            guardian.setPassword(rs.getString("password"));
            return Optional.of(guardian);
        }
    }


    public boolean editGuardian(Guardian guardian, String encodedPassword) {

        String firstName = Utils.formatUpperCase(guardian.getFirstName());
        String lastName = Utils.formatUpperCase(guardian.getLastName());

        return template.update(Queries.SQL_UPDATE_GUARDIAN,
                firstName,
                lastName,
                guardian.getPhoneNo(),
                encodedPassword,
                guardian.getGuardianId()) > 0;

    }

}
