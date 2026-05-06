package com.god.task.service;

import com.god.common.result.PageResult;
import com.god.task.dto.PublishTaskRequest;
import com.god.task.dto.TaskQueryDTO;
import com.god.task.vo.HelpTaskListVO;
import com.god.task.vo.HelpTaskVO;

public interface HelpTaskService {

    Long publishTask(PublishTaskRequest request);

    PageResult<HelpTaskListVO> getTaskList(TaskQueryDTO queryDTO);

    HelpTaskVO getTaskDetail(Long id);

    void acceptTask(Long id);

    void completeTask(Long id);

    void cancelTask(Long id);

    PageResult<HelpTaskListVO> getMyPublished(TaskQueryDTO queryDTO);

    PageResult<HelpTaskListVO> getMyAccepted(TaskQueryDTO queryDTO);
}
