package com.shirongbao.timenest.converter;

import com.shirongbao.timenest.pojo.bo.TimeNestBo;
import com.shirongbao.timenest.pojo.dto.TimeNestDto;
import com.shirongbao.timenest.pojo.entity.TimeNest;
import com.shirongbao.timenest.pojo.vo.TimeNestVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-05-18
 * @description: 拾光纪转换器
 */
@Mapper
public interface TimeNestConverter {

    TimeNestConverter INSTANCE = Mappers.getMapper(TimeNestConverter.class);

    TimeNestVo timeNestBoToTimeNestVo(TimeNestBo timeNestBo);

    List<TimeNestVo> tineNestBoListToTimeNestVoList(List<TimeNestBo> timeNestBoList);

    List<TimeNestBo> tineNestListToTimeNestBoList(List<TimeNest> timeNestList);

    TimeNestBo timeNestDtoToTimeNestBo(TimeNestDto timeNestDto);

    TimeNest timeNestBoToTimeNest(TimeNestBo timeNestBo);

    TimeNestBo timeNestToTimeNestBo(TimeNest timeNest);
}
