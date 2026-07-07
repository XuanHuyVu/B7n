package com.tlu.tsms.manager;

import com.tlu.tsms.dto.UserDto;
import com.tlu.tsms.service.UserService;
import com.tlu.tsms.utils.MapperUtils;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UserManager extends BaseManager {

    private final UserService userService;

    public void saveUser(@NonNull UserDto dto) {
        userService.save(dto);
    }

    public UserDto updateUser(Long id, UserDto dto) {
        UserDto patchDto = MapperUtils.convertValue(dto, UserDto.class);
        UserDto updated = userService.update(id, patchDto);
        return MapperUtils.convertValue(updated, UserDto.class);
    }

    public UserDto getById(Long id) {
        return MapperUtils.convertValue(userService.getById(id), UserDto.class);
    }

    public Page<UserDto> getPage(String search, Pageable pageable) {
        return userService.getPage(search, pageable);
    }

    public void delete(Long id) {
        userService.softDelete(id);
    }

    public void restore(Long id) {
        userService.restore(id);
    }

    public Page<UserDto> getDeletedUsers(Pageable pageable) {
        return userService.getDeletedUsers(pageable)
                .map(dto -> Objects.requireNonNull(MapperUtils.convertValue(dto, UserDto.class)));
    }

    public void deletePer(Long id) {
        userService.deletePer(id);
    }
}
