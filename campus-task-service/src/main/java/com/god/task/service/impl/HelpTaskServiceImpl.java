package com.god.task.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.god.common.client.UserFeignClient;
import com.god.common.context.UserContext;
import com.god.common.dto.UserSimpleDTO;
import com.god.common.enums.TaskStatusEnum;
import com.god.common.exception.BusinessException;
import com.god.common.result.PageResult;
import com.god.common.result.ResultCode;
import com.god.task.dto.PublishTaskRequest;
import com.god.task.dto.TaskQueryDTO;
import com.god.task.entity.HelpTask;
import com.god.task.mapper.HelpTaskMapper;
import com.god.task.service.HelpTaskService;
import com.god.task.vo.HelpTaskListVO;
import com.god.task.vo.HelpTaskVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HelpTaskServiceImpl implements HelpTaskService {

    private final HelpTaskMapper helpTaskMapper;
    private final UserFeignClient userFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishTask(PublishTaskRequest request) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        HelpTask task = new HelpTask();
        task.setPublisherId(userId);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setTaskType(request.getTaskType());
        task.setReward(request.getReward());
        task.setPickupAddress(request.getPickupAddress());
        task.setDeliveryAddress(request.getDeliveryAddress());
        task.setDeadline(request.getDeadline());
        task.setContactInfo(request.getContactInfo());
        task.setStatus(TaskStatusEnum.PUBLISHED.getCode());
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        helpTaskMapper.insert(task);
        return task.getId();
    }

    @Override
    public PageResult<HelpTaskListVO> getTaskList(TaskQueryDTO queryDTO) {
        Page<HelpTask> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<HelpTask> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(HelpTask::getTitle, queryDTO.getKeyword())
                    .or().like(HelpTask::getDescription, queryDTO.getKeyword()));
        }
        if (StrUtil.isNotBlank(queryDTO.getTaskType())) {
            wrapper.eq(HelpTask::getTaskType, queryDTO.getTaskType());
        }
        if (StrUtil.isNotBlank(queryDTO.getStatus())) {
            wrapper.eq(HelpTask::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getPublisherId() != null) {
            wrapper.eq(HelpTask::getPublisherId, queryDTO.getPublisherId());
        }
        if (queryDTO.getAccepterId() != null) {
            wrapper.eq(HelpTask::getAccepterId, queryDTO.getAccepterId());
        }

        wrapper.orderByDesc(HelpTask::getCreateTime);
        Page<HelpTask> result = helpTaskMapper.selectPage(page, wrapper);

        // 批量查询用户名：收集当前页所有 publisherId，一次远程调用拿到全部用户名，避免 N+1
        Set<Long> publisherIds = result.getRecords().stream()
                .map(HelpTask::getPublisherId)
                .collect(Collectors.toSet());
        Map<Long, String> userNameMap = batchGetUserNames(publisherIds);

        List<HelpTaskListVO> voList = result.getRecords().stream()
                .map(task -> toListVO(task, userNameMap.get(task.getPublisherId())))
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), (long) queryDTO.getPageNum(), (long) queryDTO.getPageSize(), voList);
    }

    @Override
    public HelpTaskVO getTaskDetail(Long id) {
        HelpTask task = getExistingTask(id);
        return toVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptTask(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        HelpTask task = getExistingTask(id);

        if (task.getPublisherId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "不能接自己发布的任务");
        }

        if (!TaskStatusEnum.PUBLISHED.getCode().equals(task.getStatus())) {
            throw new BusinessException(ResultCode.TASK_STATUS_ERROR, "只能接单已发布的任务");
        }

        task.setAccepterId(userId);
        task.setStatus(TaskStatusEnum.ACCEPTED.getCode());
        task.setUpdateTime(LocalDateTime.now());
        helpTaskMapper.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        HelpTask task = getExistingTask(id);

        if (!userId.equals(task.getAccepterId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只有接单者可以完成任务");
        }

        if (!TaskStatusEnum.ACCEPTED.getCode().equals(task.getStatus())) {
            throw new BusinessException(ResultCode.TASK_STATUS_ERROR, "只能完成已接单的任务");
        }

        task.setStatus(TaskStatusEnum.COMPLETED.getCode());
        task.setUpdateTime(LocalDateTime.now());
        helpTaskMapper.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTask(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        HelpTask task = getExistingTask(id);

        if (!userId.equals(task.getPublisherId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只有发布者可以取消任务");
        }

        if (!TaskStatusEnum.PUBLISHED.getCode().equals(task.getStatus())) {
            throw new BusinessException(ResultCode.TASK_STATUS_ERROR, "只能取消未接单的任务");
        }

        task.setStatus(TaskStatusEnum.CANCELLED.getCode());
        task.setUpdateTime(LocalDateTime.now());
        helpTaskMapper.updateById(task);
    }

    @Override
    public PageResult<HelpTaskListVO> getMyPublished(TaskQueryDTO queryDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        queryDTO.setPublisherId(userId);
        queryDTO.setStatus(null);
        return getTaskList(queryDTO);
    }

    @Override
    public PageResult<HelpTaskListVO> getMyAccepted(TaskQueryDTO queryDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        queryDTO.setAccepterId(userId);
        queryDTO.setStatus(null);
        return getTaskList(queryDTO);
    }

    private HelpTask getExistingTask(Long id) {
        HelpTask task = helpTaskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        return task;
    }

    private HelpTaskVO toVO(HelpTask task) {
        String publisherName = getUserName(task.getPublisherId());
        String accepterName = task.getAccepterId() != null ? getUserName(task.getAccepterId()) : null;
        
        return HelpTaskVO.builder()
                .id(task.getId())
                .publisherId(task.getPublisherId())
                .publisherName(publisherName)
                .accepterId(task.getAccepterId())
                .accepterName(accepterName)
                .title(task.getTitle())
                .description(task.getDescription())
                .taskType(task.getTaskType())
                .reward(task.getReward())
                .pickupAddress(task.getPickupAddress())
                .deliveryAddress(task.getDeliveryAddress())
                .deadline(task.getDeadline())
                .contactInfo(task.getContactInfo())
                .status(task.getStatus())
                .createTime(task.getCreateTime())
                .updateTime(task.getUpdateTime())
                .build();
    }

    private HelpTaskListVO toListVO(HelpTask task, String publisherName) {
        return HelpTaskListVO.builder()
                .id(task.getId())
                .publisherId(task.getPublisherId())
                .publisherName(publisherName != null ? publisherName : "用户" + task.getPublisherId())
                .title(task.getTitle())
                .taskType(task.getTaskType())
                .reward(task.getReward())
                .pickupAddress(task.getPickupAddress())
                .deliveryAddress(task.getDeliveryAddress())
                .deadline(task.getDeadline())
                .status(task.getStatus())
                .createTime(task.getCreateTime())
                .build();
    }

    // 批量获取用户名，一次远程调用查询所有用户，失败时返回空Map，调用方降级展示"用户{id}"
    private Map<Long, String> batchGetUserNames(Collection<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        try {
            Map<Long, UserSimpleDTO> userMap = userFeignClient.batchGetUserSimple(new ArrayList<>(userIds)).getData();
            if (userMap == null) {
                return Map.of();
            }
            return userMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getUsername()));
        } catch (Exception e) {
            return Map.of();
        }
    }

    // 任务详情用，只查一两个用户，无需批量
    private String getUserName(Long userId) {
        try {
            UserSimpleDTO user = userFeignClient.getUserSimple(userId).getData();
            return user != null ? user.getUsername() : "用户" + userId;
        } catch (Exception e) {
            return "用户" + userId;
        }
    }
}
