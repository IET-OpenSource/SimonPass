package it.iet.interfaces.facade.mapper;

/**
 * Interface defining the methods required for a bidirectional mapper between object.
 * @param <D> The Data Transfer Object, used in the Service layer and above
 * @param <E> The Entity, used in the Repository layer and below.
 */
public interface BidirectionalMapper<D, E> {

    /**
     * Creates a DTO object based off the entity parameter.
     * @param entity The entity to convert
     * @return The DTO obtained from the conversion
     */
    D toDto(E entity);

    /**
     * Creates an Entity object based off the DTO parameter.
     * @param dto The DTO to convert
     * @return The Entity obtained from the conversion
     */
    E toEntity(D dto);

    /**
     * Updated an Entity object based off the DTO parameter.
     * @param dto The DTO with newest parameters
     * @param entity The Entity to update
     * @return The Entity obtained from the update
     */
    E toUpdateEntity(D dto, E entity);

}
