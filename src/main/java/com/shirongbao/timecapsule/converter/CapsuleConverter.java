package com.shirongbao.timecapsule.converter;

import com.shirongbao.timecapsule.pojo.bo.CapsuleBo;
import com.shirongbao.timecapsule.pojo.entity.Capsule;
import com.shirongbao.timecapsule.pojo.response.CapsuleVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-05-18
 * @description:
 */
@Mapper
public interface CapsuleConverter {

    CapsuleConverter INSTANCE = Mappers.getMapper(CapsuleConverter.class);

    CapsuleVo capsuleToCapsuleVo(Capsule capsule);

    List<CapsuleVo> capsuleBoListToCapsuleVoList(List<CapsuleBo> capsuleBoList);

    List<CapsuleBo> capsuleListToCapsuleBoList(List<Capsule> capsuleList);
}
