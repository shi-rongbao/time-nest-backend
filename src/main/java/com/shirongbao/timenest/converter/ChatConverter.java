package com.shirongbao.timenest.converter;

import com.shirongbao.timenest.common.entity.PageResult;
import com.shirongbao.timenest.pojo.bo.ChatSessionBo;
import com.shirongbao.timenest.pojo.vo.ChatSessionVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author: ShiRongbao
 * @date: 2025-07-18
 * @description:
 */
@Mapper
public interface ChatConverter {

    ChatConverter INSTANCE = Mappers.getMapper(ChatConverter.class);

    PageResult<ChatSessionVo> chatSessionBoVo(PageResult<ChatSessionBo> sessionBoPageResult);
}
