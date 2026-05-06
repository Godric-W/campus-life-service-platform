package com.god.market.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.god.common.client.UserFeignClient;
import com.god.common.context.UserContext;
import com.god.common.dto.UserSimpleDTO;
import com.god.common.enums.MarketItemStatusEnum;
import com.god.common.exception.BusinessException;
import com.god.common.result.PageResult;
import com.god.common.result.ResultCode;
import com.god.market.dto.MarketItemQueryDTO;
import com.god.market.dto.PublishItemRequest;
import com.god.market.dto.UpdateItemRequest;
import com.god.market.entity.MarketItem;
import com.god.market.mapper.MarketItemMapper;
import com.god.market.service.MarketItemService;
import com.god.market.vo.MarketItemListVO;
import com.god.market.vo.MarketItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketItemServiceImpl implements MarketItemService {

    private final MarketItemMapper marketItemMapper;
    private final UserFeignClient userFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishItem(PublishItemRequest request) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        MarketItem item = new MarketItem();
        item.setSellerId(userId);
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setCategory(request.getCategory());
        item.setCoverImage(request.getCoverImage());
        item.setImages(request.getImages());
        item.setContactInfo(request.getContactInfo());
        item.setStatus(MarketItemStatusEnum.ON_SALE.getCode());
        item.setViewCount(0);
        item.setCreateTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());
        marketItemMapper.insert(item);
        return item.getId();
    }

    @Override
    public PageResult<MarketItemListVO> getItemList(MarketItemQueryDTO queryDTO) {
        Page<MarketItem> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<MarketItem> wrapper = new LambdaQueryWrapper<>();
        
        if (StrUtil.isNotBlank(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(MarketItem::getTitle, queryDTO.getKeyword())
                    .or().like(MarketItem::getDescription, queryDTO.getKeyword()));
        }
        if (StrUtil.isNotBlank(queryDTO.getCategory())) {
            wrapper.eq(MarketItem::getCategory, queryDTO.getCategory());
        }
        if (StrUtil.isNotBlank(queryDTO.getStatus())) {
            wrapper.eq(MarketItem::getStatus, queryDTO.getStatus());
        } else {
            wrapper.eq(MarketItem::getStatus, MarketItemStatusEnum.ON_SALE.getCode());
        }
        if (queryDTO.getSellerId() != null) {
            wrapper.eq(MarketItem::getSellerId, queryDTO.getSellerId());
        }
        
        wrapper.orderByDesc(MarketItem::getCreateTime);
        Page<MarketItem> result = marketItemMapper.selectPage(page, wrapper);
        
        List<MarketItemListVO> voList = result.getRecords().stream()
                .map(this::toListVO)
                .collect(Collectors.toList());
        
        return PageResult.of(result.getTotal(), (long) queryDTO.getPageNum(), (long) queryDTO.getPageSize(), voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MarketItemVO getItemDetail(Long id) {
        MarketItem item = getExistingItem(id);
        
        item.setViewCount(item.getViewCount() + 1);
        marketItemMapper.updateById(item);
        
        return toVO(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateItem(Long id, UpdateItemRequest request) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        MarketItem existing = getExistingItem(id);
        checkOwnership(existing, userId);
        
        if (MarketItemStatusEnum.SOLD.getCode().equals(existing.getStatus())) {
            throw new BusinessException(ResultCode.MARKET_ITEM_STATUS_ERROR, "已售出商品不能编辑");
        }

        MarketItem item = new MarketItem();
        item.setId(id);
        if (StrUtil.isNotBlank(request.getTitle())) {
            item.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            item.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            item.setPrice(request.getPrice());
        }
        if (request.getCategory() != null) {
            item.setCategory(request.getCategory());
        }
        if (request.getCoverImage() != null) {
            item.setCoverImage(request.getCoverImage());
        }
        if (request.getImages() != null) {
            item.setImages(request.getImages());
        }
        if (request.getContactInfo() != null) {
            item.setContactInfo(request.getContactInfo());
        }
        item.setUpdateTime(LocalDateTime.now());
        marketItemMapper.updateById(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offShelf(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        MarketItem item = getExistingItem(id);
        checkOwnership(item, userId);

        item.setStatus(MarketItemStatusEnum.OFF_SHELF.getCode());
        item.setUpdateTime(LocalDateTime.now());
        marketItemMapper.updateById(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsSold(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        MarketItem item = getExistingItem(id);
        checkOwnership(item, userId);

        item.setStatus(MarketItemStatusEnum.SOLD.getCode());
        item.setUpdateTime(LocalDateTime.now());
        marketItemMapper.updateById(item);
    }

    @Override
    public PageResult<MarketItemListVO> getMyItems(MarketItemQueryDTO queryDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        queryDTO.setSellerId(userId);
        queryDTO.setStatus(null);
        return getItemList(queryDTO);
    }

    private MarketItem getExistingItem(Long id) {
        MarketItem item = marketItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(ResultCode.MARKET_ITEM_NOT_FOUND);
        }
        return item;
    }

    private void checkOwnership(MarketItem item, Long userId) {
        if (!item.getSellerId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能操作自己发布的商品");
        }
    }

    private MarketItemVO toVO(MarketItem item) {
        String sellerName = getUserName(item.getSellerId());
        return MarketItemVO.builder()
                .id(item.getId())
                .sellerId(item.getSellerId())
                .sellerName(sellerName)
                .title(item.getTitle())
                .description(item.getDescription())
                .price(item.getPrice())
                .category(item.getCategory())
                .coverImage(item.getCoverImage())
                .images(item.getImages())
                .contactInfo(item.getContactInfo())
                .status(item.getStatus())
                .viewCount(item.getViewCount())
                .createTime(item.getCreateTime())
                .updateTime(item.getUpdateTime())
                .build();
    }

    private MarketItemListVO toListVO(MarketItem item) {
        String sellerName = getUserName(item.getSellerId());
        return MarketItemListVO.builder()
                .id(item.getId())
                .sellerId(item.getSellerId())
                .sellerName(sellerName)
                .title(item.getTitle())
                .price(item.getPrice())
                .category(item.getCategory())
                .coverImage(item.getCoverImage())
                .status(item.getStatus())
                .viewCount(item.getViewCount())
                .createTime(item.getCreateTime())
                .build();
    }

    private String getUserName(Long userId) {
        try {
            UserSimpleDTO user = userFeignClient.getUserSimple(userId).getData();
            return user != null ? user.getUsername() : "用户" + userId;
        } catch (Exception e) {
            return "用户" + userId;
        }
    }
}
