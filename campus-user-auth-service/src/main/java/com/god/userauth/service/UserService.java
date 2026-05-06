package com.god.userauth.service;

import com.god.common.dto.UserSimpleDTO;
import com.god.userauth.dto.UpdateUserRequest;
import com.god.userauth.vo.UserProfileVO;

import java.util.List;
import java.util.Map;

public interface UserService {

    UserProfileVO getCurrentUser();

    UserProfileVO updateCurrentUser(UpdateUserRequest request);

    UserProfileVO getUserProfile(Long id);

    UserSimpleDTO getUserSimple(Long id);

    Map<Long, UserSimpleDTO> batchGetUserSimple(List<Long> userIds);
}
