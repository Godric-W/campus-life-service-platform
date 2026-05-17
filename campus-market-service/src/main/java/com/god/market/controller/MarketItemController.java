package com.god.market.controller;

import com.god.common.result.PageResult;
import com.god.common.result.Result;
import com.god.market.dto.MarketItemQueryDTO;
import com.god.market.dto.PublishItemRequest;
import com.god.market.dto.UpdateItemRequest;
import com.god.market.service.MarketItemService;
import com.god.market.vo.MarketItemListVO;
import com.god.market.vo.MarketItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "二手商品管理", description = "校园二手商品发布、查询、编辑等接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/market/items")
public class MarketItemController {

    private final MarketItemService marketItemService;

    @Operation(summary = "发布商品", description = "发布一个新的二手商品")
    @PostMapping
    public Result<Long> publishItem(@Valid @RequestBody PublishItemRequest request) {
        return Result.success(marketItemService.publishItem(request));
    }

    @Operation(summary = "商品列表", description = "分页查询商品列表，支持关键词、分类、状态筛选")
    @GetMapping
    public Result<PageResult<MarketItemListVO>> getItemList(MarketItemQueryDTO queryDTO) {
        return Result.success(marketItemService.getItemList(queryDTO));
    }

    @Operation(summary = "商品详情", description = "查看商品详情，自动增加浏览次数")
    @GetMapping("/{id}")
    public Result<MarketItemVO> getItemDetail(
            @Parameter(description = "商品ID") @PathVariable("id") Long id) {
        return Result.success(marketItemService.getItemDetail(id));
    }

    @Operation(summary = "编辑商品", description = "编辑商品信息，仅发布者可操作")
    @PutMapping("/{id}")
    public Result<Void> updateItem(
            @Parameter(description = "商品ID") @PathVariable("id") Long id,
            @Valid @RequestBody UpdateItemRequest request) {
        marketItemService.updateItem(id, request);
        return Result.success();
    }

    @Operation(summary = "下架商品", description = "将商品下架，仅发布者可操作")
    @PutMapping("/{id}/off-shelf")
    public Result<Void> offShelf(
            @Parameter(description = "商品ID") @PathVariable("id") Long id) {
        marketItemService.offShelf(id);
        return Result.success();
    }
    @Operation(summary = "上架商品", description = "将商品上架，仅发布者可操作")
    @PutMapping("/{id}/on-shelf")
    public Result<Void> OnShelf(
            @Parameter(description = "商品ID") @PathVariable("id") Long id) {
        marketItemService.OnShelf(id);
        return Result.success();
    }

    @Operation(summary = "标记已售出", description = "将商品标记为已售出，仅发布者可操作")
    @PutMapping("/{id}/sold")
    public Result<Void> markAsSold(
            @Parameter(description = "商品ID") @PathVariable("id") Long id) {
        marketItemService.markAsSold(id);
        return Result.success();
    }

    @Operation(summary = "我的发布", description = "查询当前用户发布的所有商品")
    @GetMapping("/my")
    public Result<PageResult<MarketItemListVO>> getMyItems(MarketItemQueryDTO queryDTO) {
        return Result.success(marketItemService.getMyItems(queryDTO));
    }
}
