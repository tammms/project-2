drop database if exists caregiving;

create database caregiving;

use caregiving;

create table guardian(

    guardian_id varchar(64) not null,
    first_name varchar(64) not null,
    last_name varchar(64) not null,
    email varchar(64) not null,
    phone_no char(8) not null,
    password varchar(64) not null,
    role ENUM('ROLE_USER', 'ROLE_USERADMIN') default "ROLE_USER",
    enabled boolean default 1,
    last_update timestamp default current_timestamp on update current_timestamp,

    constraint pk_gid primary key(guardian_id)

);



create table patient(

    patient_id varchar(64) not null,
    first_name varchar(64) not null,
    last_name varchar(64) not null,
    gender varchar(64) not null,
    birth_date date not null,
    phone_no char(8),
    last_update timestamp default current_timestamp on update current_timestamp,

    constraint pk_pid primary key(patient_id)

);


create table relations(

    relations_id varchar(64) not null,
    guardian_id varchar(64) not null,
    patient_id varchar(64) not null,
    patient_relation varchar(64) not null,
    role ENUM('ROLE_USER', 'ROLE_USERADMIN') not null,
    enabled boolean default 1,
    last_update timestamp default current_timestamp on update current_timestamp,

    constraint pk_relations primary key(relations_id),
    constraint fk_gid foreign key(guardian_id) references guardian(guardian_id),
    constraint fk_pid foreign key(patient_id) references patient(patient_id)
    
    
);

CREATE VIEW user_roles AS
SELECT guardian_id as role_id,
		guardian_id, email, password, NULL AS patient_id, role, enabled
FROM guardian as g
UNION
SELECT role_id, guardian_id, email, password, patient_id, role, enabled
FROM (
SELECT CONCAT(r.guardian_id, r.patient_id) as role_id,
		r.guardian_id, g.email, g.password, r.patient_id, r.role, r.enabled
FROM relations as r
LEFT JOIN guardian as g
ON r.guardian_id = g.guardian_id) AS rel;



CREATE VIEW fulldetails AS
SELECT g.guardian_id, g.first_name AS guardian_first_name, g.last_name AS guardian_last_name,
		g.email, g.phone_no AS guardian_phone_no, g.password,
		r.relations_id, r.patient_relation, r.role,
        p.patient_id, p.first_name AS patient_first_name, p.last_name AS patient_last_name,
        p.gender, p.birth_date,
        DATE_FORMAT(NOW(), '%Y') - DATE_FORMAT(birth_date, '%Y') AS age,
        p.phone_no AS patient_phone_no        
FROM guardian AS g
JOIN relations AS r
ON g.guardian_id = r.guardian_id
JOIN patient AS p
ON p.patient_id = r.patient_id;


grant all privileges on caregiving.* to fred@'%';
flush privileges;

insert into guardian(guardian_id, first_name, last_name, email, phone_no, password)
    values
    ("qwer1234", "barney", "lee", "barney@gmail.com", "93484727", "$2a$10$oq3oTPL5IoWWmamND34OsuyAys6SNzkTfjGlhazYIc514U6SGbhvy"),
    ("qwer1235", "susan", "lim", "susan@gmail.com", "93484728", "$2a$10$oq3oTPL5IoWWmamND34OsuyAys6SNzkTfjGlhazYIc514U6SGbhvy");

insert into patient(patient_id, first_name, last_name, gender, birth_date, phone_no)
    values
    ("zxcv0997", "wilma", "tan", "Female", "1999-01-01", "93841928"),
    ("zxcv0998", "fred", "lee", "Male", "1982-01-02", "93841237"),
    ("zxcv0996", "orange", "tan", "Male", "1969-01-01", "93841928"),
    ("zxcv0995", "mango", "lee", "Male", "1947-01-02", "93841237");

insert into relations(relations_id, guardian_id, patient_id, patient_relation, role)
    values
        ("lkhj9867","qwer1234","zxcv0997","parent", "ROLE_USER"),
        ("lkhj9837","qwer1234","zxcv0998","child", "ROLE_USERADMIN"),
        ("lkhj9865","qwer1235","zxcv0996","friend", "ROLE_USER"),
        ("lkhj9832","qwer1235","zxcv0995","relative", "ROLE_USERADMIN")


