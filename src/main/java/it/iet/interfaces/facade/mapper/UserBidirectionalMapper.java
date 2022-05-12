package it.iet.interfaces.facade.mapper;

import it.iet.infrastructure.mongo.entity.user.User;
import it.iet.interfaces.facade.dto.user.UpdateUserDTO;
import it.iet.interfaces.facade.dto.user.UserDTO;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;


@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserBidirectionalMapper extends BidirectionalMapper<UserDTO, User> {

    @Override
    UserDTO toDto(User entity);

    @Override
    @InheritInverseConfiguration
    User toEntity(UserDTO dto);

    @Override
    @InheritInverseConfiguration
    User toUpdateEntity(UserDTO dto, @MappingTarget User entity);


    User fromUpdate(UpdateUserDTO dto, @MappingTarget User entity);
}
