package io.quickledger.mappers;

import io.quickledger.dto.ReferralDto;
import io.quickledger.entities.Referral;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReferralMapper {

    ReferralMapper INSTANCE = Mappers.getMapper(ReferralMapper.class);

    ReferralDto toDto(Referral referral);

    Referral toEntity(ReferralDto referralDto);

    List<ReferralDto> toDtoList(List<Referral> referrals);
}
