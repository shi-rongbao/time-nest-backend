package com.shirongbao.timenest.strategy.nest;

import com.shirongbao.timenest.common.enums.NestTypeEnum;
import com.shirongbao.timenest.pojo.entity.TimeNest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: 图片nest
 */
@Component
public class ImageNest implements NestStrategy {

    @Override
    public int getCode() {
        return NestTypeEnum.IMAGE.getCode();
    }

    @Override
    public void unlockTimeNest(TimeNest timeNest) {
        // do nothing
    }

    @Override
    public TimeNest createTimeNest(TimeNest timeNest) {
        String imageUrl = timeNest.getImageUrl();
        if (StringUtils.isBlank(imageUrl)) {
            throw new RuntimeException("创建拾光纪条目异常，选择图片类型时必须上传图片！");
        }
        timeNest.setImageUrl(imageUrl);
        return timeNest;
    }

}
