package com.example.user_service.mapper;

import com.example.user_service.dto.UserDTO;
import com.example.user_service.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserDTO userToUserDTO(User user);
    User userDtoToUser(UserDTO userDTO);
}
