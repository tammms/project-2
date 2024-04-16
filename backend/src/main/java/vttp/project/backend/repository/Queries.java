package vttp.project.backend.repository;

public class Queries {

        public static final String SQL_INSERT_GUARDIAN = """

                        INSERT INTO guardian (guardian_id, first_name, last_name, email, phone_no, password)
                        VALUES
                        (?,?,?,?,?,?)

                        """;

        public static final String SQL_CHECK_GUARDIAN_EMAIL_EXIST = """

                        SELECT count(*) as email_count
                        FROM guardian
                        WHERE email = ?

                        """;

        public static final String SQL_GET_GUARDIAN = """

                        SELECT *
                        FROM guardian
                        WHERE email = ?

                        """;

        public static final String SQL_GET_GUARDIAN_BY_ID = """

                        SELECT *
                        FROM guardian
                        WHERE guardian_id = ?

                        """;

        public static final String SQL_UPDATE_GUARDIAN = """

                        UPDATE guardian set
                        first_name=? , last_name=?, phone_no =? , password=?
                        WHERE guardian_id = ?

                                        """;

        //////////////////////////////////////////////////////////////////////////////////////////////////////

        public static final String SQL_GET_PATIENT_BY_ID = """

                        SELECT count(*) as patient_count
                        FROM patient
                        WHERE patient_id = ?

                        """;

        public static final String SQL_GET_PATIENT_BY_DETAILS = """

                        SELECT count(*) as patient_count
                        FROM patient
                        WHERE  first_name = ? AND
                                last_name = ? AND
                                birth_date = ?

                        """;

        public static final String SQL_INSERT_PATIENT = """

                        INSERT INTO patient (patient_id, first_name, last_name, gender, birth_date, phone_no)
                        values
                                (?,?,?,?,?,?)

                        """;

        public static final String SQL_DELETE_PATIENT = """

                        DELETE FROM patient
                        WHERE patient_id = ?

                        """;

        public static final String SQL_UPDATE_PATIENT = """

                        UPDATE patient set
                        gender = ?, phone_no = ?
                        WHERE patient_id = ?

                        """;

        //////////////////////////////////////////////////////////////////////////////////////////////////////

        public static final String SQL_INSERT_RELATIONSHIP = """

                        INSERT INTO relations (relations_id, guardian_id, patient_id, patient_relation, role)
                        values
                                (?,?,?,?,?)

                        """;

        public static final String SQL_DELETE_RELATIONSHIP_USER = """

                        DELETE FROM relations
                        WHERE guardian_id = ? AND patient_id = ?

                        """;

        public static final String SQL_DELETE_RELATIONSHIP_ADMIN = """

                        DELETE FROM relations
                        WHERE patient_id = ?

                        """;

        public static final String SQL_UPDATE_RELATIONSHIP = """

                        UPDATE relations set
                        patient_relation = ?
                        WHERE patient_id = ?

                        """;

        //////////////////////////////////////////////////////////////////////////////////////////////////////

        public static final String SQL_VIEW_GET_RELATIONS_DETAILS = """

                        SELECT *
                        FROM fulldetails
                        WHERE guardian_id = ?

                        """;

        public static final String SQL_VIEW_GET_RELATIONS_BY_PATIENT_ID = """

                        SELECT count(*) as patient_count
                        FROM fulldetails
                        WHERE guardian_id = ? AND patient_id = ?

                        """;

        public static final String SQL_VIEW_GET_RELATIONS_BY_GID_PID = """

                        SELECT *
                        FROM fulldetails
                        WHERE guardian_id = ? AND patient_id = ?

                        """;

        //////////////////////////////////////////////////////////////////////////////////////////////////////

        public static final String SQL_VIEW_GET_USER_ROLE = """

                        SELECT *
                        FROM user_roles
                        WHERE role_id = ?

                        """;

}
