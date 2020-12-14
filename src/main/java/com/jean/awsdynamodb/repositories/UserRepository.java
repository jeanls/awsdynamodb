package com.jean.awsdynamodb.repositories;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.jean.awsdynamodb.entities.UserEntity;
import com.jean.awsdynamodb.utils.Constants;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.*;

@Repository
@AllArgsConstructor
public class UserRepository {

    private final DynamoDBMapper dynamoDBMapper;

    public void save(final UserEntity userEntity) {
        dynamoDBMapper.save(userEntity);
    }

    public void delete(final UserEntity userEntity) {
        dynamoDBMapper.delete(userEntity);
        deleteAvatar(userEntity, false);
    }

    public Optional<UserEntity> findById(final String id) {
        return Optional.ofNullable(dynamoDBMapper.load(UserEntity.class, id));
    }

    public void addAvatar(final UserEntity userEntity, final File file) {
        userEntity.setAvatar(dynamoDBMapper.createS3Link(Constants.BUCKET_NAME, file.getName()));
        userEntity.getAvatar().uploadFrom(file);
        file.delete();
        save(userEntity);
    }

    public void deleteAvatar(final UserEntity userEntity, final boolean save) {
        if (Objects.nonNull(userEntity.getAvatar())) {
            userEntity.getAvatar().getAmazonS3Client().deleteObject(Constants.BUCKET_NAME,
                    userEntity.getAvatar().getKey());
            userEntity.setAvatar(null);
            if (save) {
                save(userEntity);
            }
        }
    }

    public String getAvatar(final UserEntity userEntity) {
        if (Objects.isNull(userEntity.getAvatar())) {
            return null;
        }
        return userEntity.getAvatar().getUrl().getPath();
    }

    public List<UserEntity> findAll() {
        return dynamoDBMapper.scan(UserEntity.class, new DynamoDBScanExpression());
    }

    public List<UserEntity> search(final String search) {
        final DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        final Map<String, AttributeValue> valueMap = new HashMap<>();
        final Map<String, String> attributeNames = new HashMap<>();

        valueMap.put(":search", new AttributeValue().withS(search));

//        scanExpression.withFilterExpression("(email = :search or #name = :search)");
        //like q%
        scanExpression.withFilterExpression("(begins_with(email, :search) or begins_with(#name, :search))");
        attributeNames.put("#name", "name");

        scanExpression.withExpressionAttributeNames(attributeNames);
        scanExpression.withExpressionAttributeValues(valueMap);

        return dynamoDBMapper.scan(UserEntity.class, scanExpression);
    }
}
