package com.tlu.tsms.service;

import com.tlu.tsms.dto.UserDto;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Transactional;

public interface UserService {

    @Transactional
    void save(UserDto dto);

    @Transactional
    UserDto update(Long id, UserDto patchDto);

    UserDto getById(Long id);

    Page<UserDto> getPage(String q, Pageable pageable);

    @Transactional
    void softDelete(Long id);

    @Transactional
    void restore(Long id);

    Page<UserDto> getDeletedUsers(Pageable pageable);

    void deletePer(Long id);
}