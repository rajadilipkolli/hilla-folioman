package com.app.folioman.portfolio.domain;

import com.app.folioman.portfolio.domain.models.request.CasDTO;
import com.app.folioman.portfolio.domain.models.request.InvestorInfoDTO;
import com.app.folioman.portfolio.domain.models.request.UserFolioDTO;
import com.app.folioman.portfolio.domain.models.request.UserSchemeDTO;
import com.app.folioman.portfolio.domain.models.request.UserTransactionDTO;
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
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "investorInfoEntity", ignore = true)
    @Mapping(target = "casTypeEnum", source = "casDTO.casType")
    @Mapping(target = "fileTypeEnum", source = "casDTO.fileType")
    UserCasDetailsEntity convert(
            CasDTO casDTO, AtomicInteger newFolios, AtomicInteger newSchemes, AtomicInteger newTransactions);

    @Mapping(target = "schemes", ignore = true)
    @Mapping(target = "userCasDetailsEntity", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    UserFolioDetailsEntity mapUserFolioDTOToUserFolioDetails(
            UserFolioDTO folioDTO, AtomicInteger newSchemes, AtomicInteger newTransactions);

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "userFolioDetails", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    UserSchemeDetailsEntity schemeDTOToSchemeEntity(UserSchemeDTO schemeDTO, AtomicInteger newTransactions);

    @Mapping(target = "userSchemeDetails", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "transactionDate", source = "date")
    UserTransactionDetails transactionDTOToTransactionEntity(UserTransactionDTO transactionDTO);

    @AfterMapping
    default void addFolioEntityToCaseDetails(
            CasDTO casDTO,
            AtomicInteger newFolios,
            AtomicInteger newSchemes,
            AtomicInteger newTransactions,
            @MappingTarget UserCasDetailsEntity userCasDetailsEntity) {
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
            @MappingTarget UserFolioDetailsEntity userFolioDetailsEntity) {
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
            @MappingTarget UserSchemeDetailsEntity userSchemeDetailsEntity) {
        Consumer<UserTransactionDTO> addTransactionEntityConsumer = transactionDTO -> {
            userSchemeDetailsEntity.addTransaction(transactionDTOToTransactionEntity(transactionDTO));
            newTransactions.getAndIncrement();
        };
        schemeDTO.transactions().forEach(addTransactionEntityConsumer);
    }

    @Mapping(target = "userCasDetailsEntity", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    InvestorInfoEntity mapInvestorInfoDTOToInvestorInfoEntity(InvestorInfoDTO investorInfo);
}
