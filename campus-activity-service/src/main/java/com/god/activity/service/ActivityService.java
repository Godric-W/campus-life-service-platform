package com.god.activity.service;

import com.god.common.result.PageResult;
import com.god.activity.dto.ActivityQueryDTO;
import com.god.activity.dto.PublishActivityRequest;
import com.god.activity.vo.ActivityListVO;
import com.god.activity.vo.ActivityVO;

public interface ActivityService {

    Long publishActivity(PublishActivityRequest request);

    PageResult<ActivityListVO> getActivityList(ActivityQueryDTO queryDTO);

    ActivityVO getActivityDetail(Long id);

    void registerActivity(Long id);

    void cancelRegistration(Long id);

    void cancelActivity(Long id);

    PageResult<ActivityListVO> getMyRegistered(ActivityQueryDTO queryDTO);

    PageResult<ActivityListVO> getMyPublished(ActivityQueryDTO queryDTO);
}
