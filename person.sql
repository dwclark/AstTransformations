begin transaction;

create sequence Persons_seq;

create table "Persons" (
       "id" int not null primary key default nextval('Persons_seq'),
       "lastName" varchar(100) not null,
       "firstName" varchar(100) not null,
       "ssn" char(9) not null,
       "birthDate" timestamp not null,
       "gender" char(1) not null
);

commit transaction;
