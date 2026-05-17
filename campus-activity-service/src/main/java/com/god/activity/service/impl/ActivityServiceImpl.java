package com.god.activity.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.god.activity.dto.ActivityQueryDTO;
import com.god.activity.dto.PublishActivityRequest;
import com.god.activity.entity.Activity;
import com.god.activity.entity.ActivityRegistration;
import com.god.activity.entity.Club;
import com.god.activity.mapper.ActivityMapper;
import com.god.activity.mapper.ActivityRegistrationMapper;
import com.god.activity.mapper.ClubMapper;
import com.god.activity.service.ActivityService;
import com.god.activity.vo.ActivityListVO;
import com.god.activity.vo.ActivityVO;
import com.god.common.client.UserFeignClient;
import com.god.common.context.UserContext;
import com.god.common.dto.UserSimpleDTO;
import com.god.common.enums.ActivityStatusEnum;
import com.god.common.exception.BusinessException;
import com.god.common.result.PageResult;
import com.god.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityMapper activityMapper;
    private final ActivityRegistrationMapper registrationMapper;
    private final ClubMapper clubMapper;
    private final UserFeignClient userFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishActivity(PublishActivityRequest request) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        if (request.getClubId() != null) {
            Club club = clubMapper.selectById(request.getClubId());
            if (club == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "社团不存在");
            }
            if (!club.getAdminId().equals(userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "只有社团管理员可以发布社团活动");
            }
        }

        Activity activity = new Activity();
        activity.setClubId(request.getClubId());
        activity.setPublisherId(userId);
        activity.setTitle(request.getTitle());
        activity.setDescription(request.getDescription());
        activity.setLocation(request.getLocation());
        activity.setCoverImage(request.getCoverImage());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setSignupDeadline(request.getSignupDeadline());
        activity.setMaxParticipants(request.getMaxParticipants());
        activity.setCurrentParticipants(0);
        activity.setStatus(ActivityStatusEnum.PUBLISHED.getCode());
        activity.setCreateTime(LocalDateTime.now());
        activity.setUpdateTime(LocalDateTime.now());
        activityMapper.insert(activity);
        return activity.getId();
    }

    @Override
    public PageResult<ActivityListVO> getActivityList(ActivityQueryDTO queryDTO) {
        Page<Activity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(Activity::getTitle, queryDTO.getKeyword())
                    .or().like(Activity::getDescription, queryDTO.getKeyword()));
        }
        if (queryDTO.getClubId() != null) {
            wrapper.eq(Activity::getClubId, queryDTO.getClubId());
        }
        if (StrUtil.isNotBlank(queryDTO.getStatus())) {
            wrapper.eq(Activity::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getPublisherId() != null) {
            wrapper.eq(Activity::getPublisherId, queryDTO.getPublisherId());
        }

        wrapper.orderByDesc(Activity::getCreateTime);
        Page<Activity> result = activityMapper.selectPage(page, wrapper);

        List<ActivityListVO> voList = result.getRecords().stream()
                .map(this::toListVO)
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), (long) queryDTO.getPageNum(), (long) queryDTO.getPageSize(), voList);
    }

    @Override
    public ActivityVO getActivityDetail(Long id) {
        Activity activity = getExistingActivity(id);
        Long userId = UserContext.getUserId();
        boolean isRegistered = false;
        if (userId != null) {
            Long count = registrationMapper.selectCount(new LambdaQueryWrapper<ActivityRegistration>()
                    .eq(ActivityRegistration::getActivityId, id)
                    .eq(ActivityRegistration::getUserId, userId));
            isRegistered = count > 0;
        }
        return toVO(activity, isRegistered);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerActivity(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        Activity activity = getExistingActivity(id);

        if (!ActivityStatusEnum.PUBLISHED.getCode().equals(activity.getStatus())) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND, "活动不可报名");
        }

        if (activity.getSignupDeadline() != null && LocalDateTime.now().isAfter(activity.getSignupDeadline())) {
            throw new BusinessException(ResultCode.ACTIVITY_REGISTRATION_CLOSED);
        }

        if (activity.getMaxParticipants() != null && activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
            throw new BusinessException(ResultCode.ACTIVITY_PARTICIPANTS_FULL);
        }

        Long count = registrationMapper.selectCount(new LambdaQueryWrapper<ActivityRegistration>()
                .eq(ActivityRegistration::getActivityId, id)
                .eq(ActivityRegistration::getUserId, userId));
        if (count > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "已经报名过该活动");
        }

        ActivityRegistration registration = new ActivityRegistration();
        registration.setActivityId(id);
        registration.setUserId(userId);
        registration.setStatus("REGISTERED");
        registration.setCreateTime(LocalDateTime.now());
        registration.setUpdateTime(LocalDateTime.now());
        registrationMapper.insert(registration);

        activity.setCurrentParticipants(activity.getCurrentParticipants() + 1);
        activity.setUpdateTime(LocalDateTime.now());
        activityMapper.updateById(activity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelRegistration(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        Activity activity = getExistingActivity(id);

        ActivityRegistration registration = registrationMapper.selectOne(new LambdaQueryWrapper<ActivityRegistration>()
                .eq(ActivityRegistration::getActivityId, id)
                .eq(ActivityRegistration::getUserId, userId));
        if (registration == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "未报名该活动");
        }

        registrationMapper.deleteById(registration.getId());

        activity.setCurrentParticipants(Math.max(0, activity.getCurrentParticipants() - 1));
        activity.setUpdateTime(LocalDateTime.now());
        activityMapper.updateById(activity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelActivity(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        Activity activity = getExistingActivity(id);

        if (!activity.getPublisherId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只有发布者可以取消活动");
        }

        activity.setStatus(ActivityStatusEnum.CANCELLED.getCode());
        activity.setUpdateTime(LocalDateTime.now());
        activityMapper.updateById(activity);
    }

    @Override
    public PageResult<ActivityListVO> getMyRegistered(ActivityQueryDTO queryDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        Page<ActivityRegistration> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<ActivityRegistration> registrations = registrationMapper.selectPage(page,
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getUserId, userId)
                        .orderByDesc(ActivityRegistration::getCreateTime));

        List<Long> activityIds = registrations.getRecords().stream()
                .map(ActivityRegistration::getActivityId)
                .collect(Collectors.toList());

        if (activityIds.isEmpty()) {
            return PageResult.of(0L, (long) queryDTO.getPageNum(), (long) queryDTO.getPageSize(), List.of());
        }

        List<Activity> activities = activityMapper.selectBatchIds(activityIds);
        List<ActivityListVO> voList = activities.stream()
                .map(this::toListVO)
                .collect(Collectors.toList());

        return PageResult.of(registrations.getTotal(), (long) queryDTO.getPageNum(), (long) queryDTO.getPageSize(), voList);
    }

    @Override
    public PageResult<ActivityListVO> getMyPublished(ActivityQueryDTO queryDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        queryDTO.setPublisherId(userId);
        queryDTO.setStatus(null);
        return getActivityList(queryDTO);
    }

    private Activity getExistingActivity(Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        }
        return activity;
    }

    private ActivityVO toVO(Activity activity, boolean isRegistered) {
        String publisherName = getUserName(activity.getPublisherId());
        String clubName = activity.getClubId() != null ? getClubName(activity.getClubId()) : null;
        
        return ActivityVO.builder()
                .id(activity.getId())
                .clubId(activity.getClubId())
                .clubName(clubName)
                .publisherId(activity.getPublisherId())
                .publisherName(publisherName)
                .title(activity.getTitle())
                .description(activity.getDescription())
                .location(activity.getLocation())
                .coverImage(activity.getCoverImage())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .signupDeadline(activity.getSignupDeadline())
                .maxParticipants(activity.getMaxParticipants())
                .currentParticipants(activity.getCurrentParticipants())
                .status(activity.getStatus())
                .isRegistered(isRegistered)
                .createTime(activity.getCreateTime())
                .updateTime(activity.getUpdateTime())
                .build();
    }

    private ActivityListVO toListVO(Activity activity) {
        String clubName = activity.getClubId() != null ? getClubName(activity.getClubId()) : null;
        String publisherName = getUserName(activity.getPublisherId());
        
        return ActivityListVO.builder()
                .id(activity.getId())
                .clubId(activity.getClubId())
                .clubName(clubName)
                .publisherId(activity.getPublisherId())
                .publisherName(publisherName)
                .title(activity.getTitle())
                .location(activity.getLocation())
                .coverImage(activity.getCoverImage())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .signupDeadline(activity.getSignupDeadline())
                .maxParticipants(activity.getMaxParticipants())
                .currentParticipants(activity.getCurrentParticipants())
                .status(activity.getStatus())
                .createTime(activity.getCreateTime())
                .build();
    }

    private String getClubName(Long clubId) {
        Club club = clubMapper.selectById(clubId);
        return club != null ? club.getName() : null;
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
