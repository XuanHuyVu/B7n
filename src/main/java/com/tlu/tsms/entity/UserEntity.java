package com.tlu.tsms.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tlu.tsms.common.EAccountStatus;
import com.tlu.tsms.common.EUserRole;
import com.tlu.tsms.common.TableNameConstant;
import com.tlu.tsms.converter.EAccountStatusConverter;
import com.tlu.tsms.converter.EUserRoleConverter;
import com.tlu.tsms.utils.DateUtils;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = TableNameConstant.USER)
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "PASSWORD_HASH")
    private String passwordHash;

    @Column(name = "USER_CODE")
    private String userCode;

    @Column(name = "FULL_NAME")
    private String fullName;

    @Column(name = "ROLE")
    @Convert(converter = EUserRoleConverter.class)
    private EUserRole role;

    @Column(name = "ACCOUNT_STATUS")
    @Convert(converter = EAccountStatusConverter.class)
    private EAccountStatus accountStatus;

    @Column(name = "IS_LOCKED")
    @Builder.Default
    private Boolean isLocked = false;

    @Column(name = "LOCKED_UNTIL")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.NORMAL_TIME_PATTERN, timezone = DateUtils.DEFAULT_TIMEZONE_GMT7)
    private Date lockedUntil;

    @Column(name = "LOCKED_REASON")
    private String lockedReason;

    @Column(name = "FAILED_LOGIN_ATTEMPTS")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "LAST_FAILED_LOGIN_AT")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.NORMAL_TIME_PATTERN, timezone = DateUtils.DEFAULT_TIMEZONE_GMT7)
    private Date lastFailedLoginAt;

    @Column(name = "LAST_LOGIN_AT")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.NORMAL_TIME_PATTERN, timezone = DateUtils.DEFAULT_TIMEZONE_GMT7)
    private Date lastLoginAt;

    @Column(name = "LAST_LOGIN_DEVICE")
    private String lastLoginDevice;

    @Column(name = "RESET_PASSWORD_TOKEN")
    private String resetPasswordToken;

    @Column(name = "RESET_PASSWORD_TOKEN_EXPIRES_AT")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.NORMAL_TIME_PATTERN, timezone = DateUtils.DEFAULT_TIMEZONE_GMT7)
    private Date resetPasswordTokenExpiresAt;

    @Column(name = "DELETED_AT")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.NORMAL_TIME_PATTERN, timezone = DateUtils.DEFAULT_TIMEZONE_GMT7)
    private Date deletedAt;

    @Column(name = "PASSWORD_CHANGED_AT")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.NORMAL_TIME_PATTERN, timezone = DateUtils.DEFAULT_TIMEZONE_GMT7)
    private Date passwordChangedAt;
}