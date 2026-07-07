package com.tlu.tsms.entity;

import com.tlu.tsms.common.ELoginStatus;
import com.tlu.tsms.common.TableNameConstant;
import com.tlu.tsms.converter.ELoginStatusConverter;
import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = TableNameConstant.LOGIN_LOG)
public class LoginLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "USER_ID")
    private Integer userId;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "LOGIN_STATUS")
    @Convert(converter = ELoginStatusConverter.class)
    private ELoginStatus loginStatus;

    @Column(name = "USER_AGENT")
    private String userAgent;

    @Column(name = "FAILURE_REASON")
    private String failureReason;

    @Column(name = "LOGIN_AT")
    private Date loginAt;
}
