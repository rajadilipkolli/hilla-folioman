package com.app.folioman.portfolio.mapper;

import com.app.folioman.portfolio.entities.UserCASDetails;
import com.app.folioman.portfolio.entities.UserFolioDetails;
import com.app.folioman.portfolio.entities.UserSchemeDetails;
import com.app.folioman.portfolio.entities.UserTransactionDetails;
import com.app.folioman.portfolio.models.request.CasDTO;
import com.app.folioman.portfolio.models.request.UserFolioDTO;
import com.app.folioman.portfolio.models.request.UserSchemeDTO;
import com.app.folioman.portfolio.models.request.UserTransactionDTO;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CasDetailsMapper {

    @Mapping(target = "folios", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "casTypeEnum", source = "casDTO.casType")
    @Mapping(target = "fileTypeEnum", source = "casDTO.fileType")
    UserCASDetails convert(
            CasDTO casDTO, AtomicInteger newFolios, AtomicInteger newSchemes, AtomicInteger newTransactions);

    @Mapping(target = "schemes", ignore = true)
    @Mapping(target = "userCasDetails", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    UserFolioDetails mapUserFolioDTOToUserFolioDetails(
            UserFolioDTO folioDTO, AtomicInteger newSchemes, AtomicInteger newTransactions);

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "userFolioDetails", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    UserSchemeDetails schemeDTOToSchemeEntity(UserSchemeDTO schemeDTO, AtomicInteger newTransactions);

    @Mapping(target = "userSchemeDetails", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "transactionDate", source = "date")
    UserTransactionDetails transactionDTOToTransactionEntity(UserTransactionDTO transactionDTO);

    @AfterMapping
    default void addFolioEntityToCaseDetails(
            CasDTO casDTO,
            AtomicInteger newFolios,
            AtomicInteger newSchemes,
            AtomicInteger newTransactions,
            @MappingTarget UserCASDetails userCasDetailsEntity) {
        Consumer<UserFolioDTO> addFolioEntityConsumer = folioDTO -> {
            userCasDetailsEntity.addFolioEntity(
                    mapUserFolioDTOToUserFolioDetails(folioDTO, newSchemes, newTransactions));
            newFolios.getAndIncrement();
        };
        casDTO.folios().forEach(addFolioEntityConsumer);
    }

    @AfterMapping
    default void addSchemaEntityToFolioEntity(
            UserFolioDTO folioDTO,
            AtomicInteger newSchemes,
            AtomicInteger newTransactions,
            @MappingTarget UserFolioDetails userFolioDetailsEntity) {
        Consumer<UserSchemeDTO> addSchemeEntityConsumer = schemeDTO -> {
            userFolioDetailsEntity.addScheme(schemeDTOToSchemeEntity(schemeDTO, newTransactions));
            newSchemes.getAndIncrement();
        };
        folioDTO.schemes().forEach(addSchemeEntityConsumer);
    }

    @AfterMapping
    default void addTransactionEntityToSchemeEntity(
            UserSchemeDTO schemeDTO,
            AtomicInteger newTransactions,
            @MappingTarget UserSchemeDetails userSchemeDetailsEntity) {
        Consumer<UserTransactionDTO> addTransactionEntityConsumer = transactionDTO -> {
            userSchemeDetailsEntity.addTransaction(transactionDTOToTransactionEntity(transactionDTO));
            newTransactions.getAndIncrement();
        };
        schemeDTO.transactions().forEach(addTransactionEntityConsumer);
    }
}
