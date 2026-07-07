package com.tlu.tsms.service.impl;

import com.tlu.tsms.common.EAccountStatus;
import com.tlu.tsms.exception.EStatusCode;
import com.tlu.tsms.dto.UserDto;
import com.tlu.tsms.entity.UserEntity;
import com.tlu.tsms.logging.AuditLog;
import com.tlu.tsms.logging.ManagerIf;
import com.tlu.tsms.service.BaseService;
import com.tlu.tsms.service.UserService;
import com.tlu.tsms.service.dao.UserDAO;
import com.tlu.tsms.utils.DateUtils;
import com.tlu.tsms.utils.ErrorUtils;
import com.tlu.tsms.utils.MapperUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends BaseService implements UserService {

    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;

    private static final Set<String> PATCH_IGNORE_FIELDS = Set.of(
            "id",
            "passwordHash",
            "deletedAt",
            "createdDate",
            "lastUpdatedDate",
            "createdBy",
            "lastUpdatedBy",
            "isLocked",
            "failedLoginAttempts",
            "lockedUntil",
            "lockedReason",
            "lastFailedLoginAt",
            "lastLoginAt",
            "lastLoginDevice",
            "resetPasswordTokenExpiresAt",
            "passwordChangedAt"
    );

    @Override
    @AuditLog(
            module = "USER",
            action = "CREATE",
            resourceType = "USER",
            message = "'Create user email=' + #dto.email + ', userCode=' + #dto.userCode"
    )
    @ManagerIf(when = ManagerIf.When.SUCCESS)
    public void save(UserDto dto) {
        Optional.ofNullable(dto)
                .filter(d -> {
                    if (userDAO.existsActiveByEmail(d.getEmail()))
                        throw ErrorUtils.exception(EStatusCode.USER_EMAIL_EXISTS, Map.of("email", d.getEmail()));
                    if (userDAO.existsActiveByUserCode(d.getUserCode()))
                        throw ErrorUtils.exception(EStatusCode.USER_CODE_EXISTS, Map.of("userCode", d.getUserCode()));
                    return true;
                }).map(d -> {
                    UserEntity entity = new UserEntity();
                    MapperUtils.copyIgnore(d, entity, Set.of("id", "isLocked", "failedLoginAttempts", "password"));
                    entity.setPasswordHash(passwordEncoder.encode(d.getPassword()));
                    if (Objects.isNull(entity.getAccountStatus())) entity.setAccountStatus(EAccountStatus.ACTIVE);
                    return entity;
                }).ifPresent(userDAO::save);
    }

    @Override
    @CachePut(cacheNames = "userById", key = "#id")
    @AuditLog(
            module = "USER",
            action = "UPDATE",
            resourceType = "USER",
            resourceId = "#id",
            message = "'Update user id=' + #id"
    )
    @ManagerIf(when = ManagerIf.When.SUCCESS)
    public UserDto update(Long id, UserDto dto) {
        if (Objects.isNull(dto))
            throw ErrorUtils.exception(EStatusCode.BAD_REQUEST, Map.of("dto", "must not be null"));
        return userDAO.findActiveById(id)
                .map(entity -> {
                    if (Objects.nonNull(dto.getEmail()) && !dto.getEmail().equals(entity.getEmail())
                            && userDAO.existsActiveByEmail(dto.getEmail())) {
                        throw ErrorUtils.exception(EStatusCode.USER_EMAIL_EXISTS, Map.of("email", dto.getEmail()));
                    }
                    if (Objects.nonNull(dto.getUserCode())
                            && !dto.getUserCode().equals(entity.getUserCode()) && userDAO.existsActiveByUserCode(dto.getUserCode())) {
                        throw ErrorUtils.exception(EStatusCode.USER_CODE_EXISTS, Map.of("userCode", dto.getUserCode()));
                    }
                    MapperUtils.copyNonNullIgnore(dto, entity, PATCH_IGNORE_FIELDS);
                    userDAO.save(entity);
                    return MapperUtils.convertValue(entity, UserDto.class);
                })
                .orElseThrow(() -> ErrorUtils.exception(EStatusCode.USER_NOT_FOUND, Map.of("user", "not found")));
    }


    @Override
    @Cacheable(cacheNames = "userById", key = "#id")
    public UserDto getById(Long id) {
        UserEntity entity = userDAO.findActiveById(id)
                .orElseThrow(() -> ErrorUtils.exception(EStatusCode.USER_NOT_FOUND, Map.of("user", "not found")));
        return MapperUtils.convertValue(entity, UserDto.class);
    }

    @Override
    public Page<UserDto> getPage(String search, Pageable pageable) {
        Page<UserEntity> page = (Objects.isNull(search) || search.isBlank())
                ? userDAO.findAllActive(pageable)
                : userDAO.searchActive(search.trim(), pageable);
        return page.map(e -> Objects.requireNonNull(MapperUtils.convertValue(e, UserDto.class)));
    }

    @Override
    @CacheEvict(cacheNames = "userById", key = "#id")
    @AuditLog(
            module = "USER",
            action = "SOFT_DELETE",
            resourceType = "USER",
            resourceId = "#id",
            message = "'Soft delete user id=' + #id"
    )
    @ManagerIf(when = ManagerIf.When.SUCCESS)
    public void softDelete(Long id) {
        UserEntity entity = userDAO.findActiveById(id)
                .orElseThrow(() -> ErrorUtils.exception(EStatusCode.USER_NOT_FOUND, Map.of("user", "not found")));
        entity.setDeletedAt(DateUtils.now());
        entity.setAccountStatus(EAccountStatus.DELETED);
        userDAO.save(entity);
    }

    @Override
    @CacheEvict(cacheNames = "userById", key = "#id")
    @AuditLog(
            module = "USER",
            action = "RESTORE",
            resourceType = "USER",
            resourceId = "#id",
            message = "'Restore user id=' + #id"
    )
    @ManagerIf(when = ManagerIf.When.SUCCESS)
    public void restore(Long id) {
        UserEntity entity = userDAO.findById(id)
                .orElseThrow(() -> ErrorUtils.exception(EStatusCode.USER_NOT_FOUND, Map.of("user", "not found")));
        if (Objects.isNull(entity.getDeletedAt()))
            throw ErrorUtils.exception(EStatusCode.USER_ALREADY_RESTORED, Map.of("user", "already restored"));
        entity.setDeletedAt(null);
        entity.setAccountStatus(EAccountStatus.ACTIVE);
        userDAO.save(entity);
    }

    @Override
    public Page<UserDto> getDeletedUsers(Pageable pageable) {
        Page<UserEntity> page = userDAO.findAllDeleted(pageable);
        return page.map(e -> Objects.requireNonNull(MapperUtils.convertValue(e, UserDto.class)));
    }

    @Override
    @CacheEvict(cacheNames = "userById", key = "#id")@AuditLog(
            module = "USER",
            action = "DELETE_PER",
            resourceType = "USER",
            resourceId = "#id",
            message = "'Permanent delete user id=' + #id"
    )
    @ManagerIf(when = ManagerIf.When.SUCCESS)
    public void deletePer(Long id) {
        UserEntity entity = userDAO.findById(id)
                .orElseThrow(() -> ErrorUtils.exception(EStatusCode.USER_NOT_FOUND, Map.of("user", "not found")));
        if (Objects.isNull(entity.getDeletedAt()))
            throw ErrorUtils.exception(EStatusCode.BAD_REQUEST, Map.of("user", "must soft-delete before permanent delete"));
        userDAO.delete(entity);
    }
}
