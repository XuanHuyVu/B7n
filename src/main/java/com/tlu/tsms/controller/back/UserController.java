package com.tlu.tsms.controller.back;

import com.tlu.tsms.common.Constant;
import com.tlu.tsms.controller.BaseController;
import com.tlu.tsms.dto.UserDto;
import com.tlu.tsms.manager.UserManager;
import com.tlu.tsms.response.PageResponse;
import com.tlu.tsms.response.ResponseDto;
import com.tlu.tsms.utils.PageableUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin/users")
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final UserManager userManager;

    @PostMapping
    public ResponseEntity<ResponseDto<?>> saveUser(@Valid @RequestBody UserDto dto) {
        userManager.saveUser(dto);
        return success();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ResponseDto<UserDto>> update(@PathVariable Long id, @Valid @RequestBody UserDto dto) {
        return success(userManager.updateUser(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<UserDto>> getById(@PathVariable Long id) {
        return success(userManager.getById(id));
    }

    @GetMapping
    public ResponseEntity<ResponseDto<PageResponse<UserDto>>> page(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = Constant.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = Constant.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "id,desc") String sort
    ) {
        Pageable pageable = PageableUtils.pageable(page, size, sort);
        PageResponse<UserDto> data = PageResponse.toResponse(userManager.getPage(search, pageable));
        return success(data);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDelete(@PathVariable Long id) {
        userManager.delete(id);
        return success();
    }

    @PostMapping("/deleted/{id}/restore")
    public ResponseEntity<?> restore(@PathVariable Long id) {
        userManager.restore(id);
        return success();
    }

    @GetMapping("/deleted")
    public ResponseEntity<ResponseDto<PageResponse<UserDto>>> pageDeletedUsers(
            @RequestParam(defaultValue = Constant.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = Constant.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "id,desc") String sort) {
        Pageable pageable = PageableUtils.pageable(page, size, sort);
        PageResponse<UserDto> data = PageResponse.toResponse(userManager.getDeletedUsers(pageable));
        return success(data);
    }

    @DeleteMapping("/deleted/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userManager.deletePer(id);
        return success();
    }
}
