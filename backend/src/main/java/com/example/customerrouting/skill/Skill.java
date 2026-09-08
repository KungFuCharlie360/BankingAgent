package com.example.customerrouting.skill;
import jakarta.persistence.*; import java.util.*;
@Entity @Table(name="skills") public class Skill { @Id private UUID id=UUID.randomUUID(); @Column(unique=true,nullable=false) private String code; private String name; protected Skill(){} public Skill(String code,String name){this.code=code;this.name=name;} public UUID getId(){return id;} public String getCode(){return code;} public String getName(){return name;} }
