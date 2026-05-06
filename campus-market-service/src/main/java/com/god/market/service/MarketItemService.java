package com.god.market.service;

import com.god.common.result.PageResult;
import com.god.market.dto.MarketItemQueryDTO;
import com.god.market.dto.PublishItemRequest;
import com.god.market.dto.UpdateItemRequest;
import com.god.market.vo.MarketItemListVO;
import com.god.market.vo.MarketItemVO;

public interface MarketItemService {

    Long publishItem(PublishItemRequest request);

    PageResult<MarketItemListVO> getItemList(MarketItemQueryDTO queryDTO);

    MarketItemVO getItemDetail(Long id);

    void updateItem(Long id, UpdateItemRequest request);

    void offShelf(Long id);

    void markAsSold(Long id);

    PageResult<MarketItemListVO> getMyItems(MarketItemQueryDTO queryDTO);
}
