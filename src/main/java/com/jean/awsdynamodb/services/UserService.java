package com.jean.awsdynamodb.services;

import com.amazonaws.services.dynamodbv2.datamodeling.S3Link;
import com.amazonaws.services.s3.AmazonS3;
import com.jean.awsdynamodb.dtos.UserDto;
import com.jean.awsdynamodb.dtos.input.UserCreateInputDto;
import com.jean.awsdynamodb.entities.UserEntity;
import com.jean.awsdynamodb.exceptions.NotFoundException;
import com.jean.awsdynamodb.repositories.UserRepository;
import com.jean.awsdynamodb.utils.Constants;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final AmazonS3 amazonS3;
    private static final String USER_NOT_FOUND = "Usuário não encontrado";

    public UserDto create(final UserCreateInputDto userCreateInputDto) {
        final UserEntity userEntity = modelMapper.map(userCreateInputDto, UserEntity.class);
        userRepository.save(userEntity);
        return modelMapper.map(userEntity, UserDto.class);
    }

    public List<UserDto> search(final String q) {
        final List<UserEntity> userEntities = userRepository.search(q);
        return mapFrom(userEntities);
    }

    private List<UserDto> mapFrom(final List<UserEntity> userEntities) {
        return userEntities.stream()
                .map(userEntity -> {
                    final UserDto userDto = modelMapper.map(userEntity, UserDto.class);

                    if (Objects.nonNull(userEntity.getAvatar())) {
                        userDto.setAvatar(buildUrl(userEntity.getAvatar().getUrl()));
                    }

                    return userDto;
                }).
                        collect(Collectors.toList());
    }

    public List<UserDto> index() {
        final List<UserEntity> userEntities = userRepository.findAll();
        return mapFrom(userEntities);
    }

    public UserDto update(final UserDto userDto) {
        final UserEntity userEntity = modelMapper.map(userDto, UserEntity.class);
        userRepository.save(userEntity);
        return modelMapper.map(userEntity, UserDto.class);
    }

    public void delete(final String id) {
        final UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        userRepository.delete(userEntity);
    }

    public UserDto get(final String id) {
        final UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        return modelMapper.map(userEntity, UserDto.class);
    }

    public void addAvatar(final String id, final MultipartFile multipartFile) throws IOException {
        final UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        final File file = new File("C:\\Users\\jean.silva\\files\\" + multipartFile.getOriginalFilename());
        multipartFile.transferTo(file);
        userRepository.addAvatar(userEntity, file);
    }

    public void deleteAvatar(final String id) {
        final UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        userRepository.deleteAvatar(userEntity, true);
    }

    public boolean bucketExist(final UserEntity userEntity) {
        return amazonS3.doesObjectExist(Constants.BUCKET_NAME,
                String.format("%s/%s", userEntity.getId(), userEntity.getAvatar()));
    }

    public File getAvatar(final String id) {
        final UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        if (Objects.isNull(userEntity.getAvatar())) {
            throw new NotFoundException("");
        }

        File avatar = new File("C:\\Users\\jean.silva\\files\\" + userEntity.getAvatar().getKey());

        userEntity.getAvatar().downloadTo(avatar);

        return avatar;
    }

    private String buildUrl(final URL url) {
        return url.getProtocol() + "://" + url.getHost() + url.getPath();
    }
}
