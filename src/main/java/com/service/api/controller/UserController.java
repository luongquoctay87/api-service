package com.service.api.controller;


import com.service.api.form.UserForm;
import com.service.api.response.ApiResponse;
import com.service.api.response.ErrorResponse;
import com.service.api.response.PageResponse;
import com.service.config.Translator;
import com.service.dto.UserDTO;
import com.service.exception.ResourceNotFoundException;
import com.service.model.AppUser;
import com.service.service.UserService;
import com.service.util.ApiConst;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j(topic = "USER_CONTROLLER")
@Tag(name = "User Controller")
@Validated
public class UserController {
    private final ModelMapper mapper;
    private final PasswordEncoder encoder;
    private final UserService userService;

    @GetMapping(headers = ApiConst.API_VERSION_1)
    public ApiResponse getUserList(
            @RequestParam(name = "search", defaultValue = "") Optional<String> _search,
            @RequestParam(name = "pageNo", defaultValue = "1") Optional<Integer> _pageNo,
            @RequestParam(name = "pageSize", defaultValue = "20") Optional<Integer> _pageSize) {
        log.info("Request api GET api/v1/users");

        Page<AppUser> users = userService.findAll(_search.get(), _pageNo.get(), _pageSize.get());
        List<UserDTO> userDTOs = users.getContent().stream().map(i -> mapper.map(i, UserDTO.class)).collect(Collectors.toList());
        PageResponse view = PageResponse.builder()
                .pageNo(_pageNo.get())
                .pageSize(_pageSize.get())
                .total(users.getTotalElements())
                .items(userDTOs)
                .build();

        return new ApiResponse(HttpStatus.OK.value(), "users", view);
    }

    @GetMapping(path = "/{id}", headers = ApiConst.API_VERSION_1)
    public ApiResponse getUser(@PathVariable("id") @Min(1) Integer _id) throws ResourceNotFoundException {
        log.info("Request api GET api/v1/users/{}", _id);
        AppUser user = userService.getById(_id);
        return new ApiResponse(HttpStatus.OK.value(), "user", user);
    }

    // @PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN', 'ROLE_MANAGER')")
    @PostMapping(headers = ApiConst.API_VERSION_1)
    public ApiResponse createUser(@Valid @RequestBody UserForm form) {
        log.info("Request api POST api/v1/users");

        AppUser user = AppUser.builder()
                .email(form.getEmail())
                .username(form.getUsername())
                .password(encoder.encode(form.getPassword()))
                .createdDate(new Date())
                .build();

        try {
            userService.save(user);
            return new ApiResponse(HttpStatus.CREATED.value(), Translator.toLocale("user-add-success"), user.getId());
        } catch (Exception e) {
            log.error("Can not create user");
            return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), Translator.toLocale("user-add-fail"));
        }
    }

    @PutMapping(headers = ApiConst.API_VERSION_1)
    public ApiResponse updateUser(@Valid @RequestBody UserForm form) throws ResourceNotFoundException {
        log.info("Request api PUT api/v1/users/{}", form.getId());

        AppUser user = userService.getById(form.getId());
        user.setUsername(form.getUsername());
        userService.save(user);
        return new ApiResponse(HttpStatus.OK.value(), Translator.toLocale("user-update-success"));
    }

    // @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    @DeleteMapping(path = "/{id}", headers = ApiConst.API_VERSION_1)
    public ApiResponse deleteUser(@PathVariable(value = "id") @Min(1) Long _id) throws ResourceNotFoundException {
        log.info("Request api DELETE api/v1/users/{}", _id);

        userService.delete(_id);
        return new ApiResponse(HttpStatus.OK.value(), Translator.toLocale("user-delete-success"));
    }

    @PatchMapping(path = "/change-password/{id}", headers = ApiConst.API_VERSION_1)
    public ApiResponse changPassword(@PathVariable("id") @Min(1) Long _id, @RequestParam("password") String password) throws ResourceNotFoundException {
        log.info("Request api PATCH api/v1/users/changePassword/{}", _id);

        AppUser user = userService.getById(_id);
        user.setPassword(encoder.encode(password));
        userService.save(user);
        return new ApiResponse(HttpStatus.OK.value(), Translator.toLocale("user-change-password-success"));
    }

}
