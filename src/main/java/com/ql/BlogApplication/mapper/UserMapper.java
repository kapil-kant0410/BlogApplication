package com.ql.BlogApplication.mapper;

import com.ql.BlogApplication.constant.MessageCodes;
import com.ql.BlogApplication.dto.UserResponseDto;
import com.ql.BlogApplication.entity.User;
import com.ql.BlogApplication.entity.Role;
import com.ql.BlogApplication.exception.RoleNotFoundException;
import com.ql.BlogApplication.repository.RoleRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class UserMapper {

    private final RoleRepository roleRepository;

    public UserMapper(RoleRepository roleRepository){
        this.roleRepository=roleRepository;
    }

   public List<String> findUserRoles(Set<String> userRoleIds){

      return  userRoleIds.stream().map(roleId -> {
          Role role= roleRepository.findById(roleId).orElseThrow(()-> new RoleNotFoundException(MessageCodes.messages.get(211)));
          return role.getName();
       }).toList();

   }

    public List<UserResponseDto> toDtoList(List<User> users){
        return users.stream()
                .map(user -> UserResponseDto.builder()
                        .name(user.getName())
                        .email(user.getEmail())
                        .role(findUserRoles(user.getRoleIds()))
                        .build())
                .toList();
    }

    public UserResponseDto toDto(User user){
        return UserResponseDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .role(findUserRoles(user.getRoleIds()))
                .build();
    }

}
