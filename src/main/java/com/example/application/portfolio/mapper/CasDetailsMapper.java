package com.example.application.portfolio.mapper;

import com.example.application.portfolio.entities.UserCASDetails;
import com.example.application.portfolio.entities.UserFolioDetails;
import com.example.application.portfolio.entities.UserSchemeDetails;
import com.example.application.portfolio.entities.UserTransactionDetails;
import com.example.application.portfolio.models.CasDTO;
import com.example.application.portfolio.models.UserFolioDTO;
import com.example.application.portfolio.models.UserSchemeDTO;
import com.example.application.portfolio.models.UserTransactionDTO;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CasDetailsMapper {

    @Mapping(target = "folios", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "casTypeEnum", source = "casType")
    @Mapping(target = "fileTypeEnum", source = "fileType")
    UserCASDetails convert(CasDTO casDTO);

    @Mapping(target = "schemes", ignore = true)
    @Mapping(target = "userCasDetails", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    UserFolioDetails mapUserFolioDTOToUserFolioDetails(UserFolioDTO folioDTO);

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "userFolioDetails", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    UserSchemeDetails schemeDTOToSchemeEntity(UserSchemeDTO schemeDTO);

    @Mapping(target = "userSchemeDetails", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "transactionDate", source = "date")
    UserTransactionDetails transactionDTOToTransactionEntity(UserTransactionDTO transactionDTO);
}
