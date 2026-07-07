//package com.tlu.tsms.entity;
//
//import com.tlu.tsms.common.TableNameConstant;
//import jakarta.persistence.*;
//import lombok.*;
//import lombok.experimental.SuperBuilder;
//
//@Data
//@EqualsAndHashCode(callSuper = true)
//@SuperBuilder
//@NoArgsConstructor
//@AllArgsConstructor
//@Entity
//@Table(name = TableNameConstant.PASSWORD_HISTORY)
//public class PasswordHistoryEntity extends BaseEntity {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "ID")
//    private Long id;
//
//    @Column(name = "USER_ID", nullable = false)
//    private Integer userId;
//
//    @Column(name = "PASSWORD_HASH", nullable = false)
//    private String passwordHash;
//}
