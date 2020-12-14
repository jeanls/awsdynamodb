package com.jean.awsdynamodb.controllers;

import com.jean.awsdynamodb.dtos.UserDto;
import com.jean.awsdynamodb.dtos.input.UserCreateInputDto;
import com.jean.awsdynamodb.services.UserService;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.List;

@AllArgsConstructor
@CrossOrigin
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> create(@RequestBody @Valid final UserCreateInputDto userCreateInputDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(userCreateInputDto));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> index() {
        return ResponseEntity.ok(userService.index());
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/search")
    public ResponseEntity<List<UserDto>> search(@RequestParam(name = "q") final String q) {
        return ResponseEntity.ok(userService.search(q));
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> update(@RequestBody @Valid final UserDto userDto) {
        return ResponseEntity.ok(userService.update(userDto));
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> get(@PathVariable("id") final String id) {
        return ResponseEntity.ok(userService.get(id));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<UserDto> delete(@PathVariable("id") final String id) {
        userService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/addAvatar/{id}")
    public ResponseEntity<Object> addAvatar(@RequestParam("file") MultipartFile file,
                                            @PathVariable("id") final String id) throws IOException {
        userService.addAvatar(id, file);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/getAvatar/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody
    byte[] getAvatar(@PathVariable("id") final String id) throws IOException {
        final File avatar = userService.getAvatar(id);
        return FileUtils.readFileToByteArray(avatar);
    }

    @DeleteMapping(path = "/deleteAvatar/{id}")
    public ResponseEntity<UserDto> deleteAvatar(@PathVariable("id") final String id) {
        userService.deleteAvatar(id);
        return ResponseEntity.ok().build();
    }
}
