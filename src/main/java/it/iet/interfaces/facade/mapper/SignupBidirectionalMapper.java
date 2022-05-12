package it.iet.interfaces.facade.mapper;

import it.iet.infrastructure.mongo.entity.user.User;
import it.iet.interfaces.facade.dto.user.SignUpDTO;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SignupBidirectionalMapper extends BidirectionalMapper<SignUpDTO, User>{

    @Override
    SignUpDTO toDto(User entity);

    @Override
    @InheritInverseConfiguration
    User toEntity(SignUpDTO dto);

    @Override
    @InheritInverseConfiguration
    User toUpdateEntity(SignUpDTO dto, @MappingTarget User entity);
}
