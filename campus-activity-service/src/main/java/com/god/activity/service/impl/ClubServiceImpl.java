package com.god.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.god.activity.dto.CreateClubRequest;
import com.god.activity.entity.Club;
import com.god.activity.mapper.ClubMapper;
import com.god.activity.service.ClubService;
import com.god.activity.vo.ClubVO;
import com.god.common.client.UserFeignClient;
import com.god.common.context.UserContext;
import com.god.common.dto.UserSimpleDTO;
import com.god.common.exception.BusinessException;
import com.god.common.result.ResultCode;
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
public class ClubServiceImpl implements ClubService {

    private final ClubMapper clubMapper;
    private final UserFeignClient userFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createClub(CreateClubRequest request) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        Club club = new Club();
        club.setName(request.getName());
        club.setDescription(request.getDescription());
        club.setLogo(request.getLogo());
        club.setAdminId(userId);
        club.setContactInfo(request.getContactInfo());
        club.setCreateTime(LocalDateTime.now());
        club.setUpdateTime(LocalDateTime.now());
        clubMapper.insert(club);
        return club.getId();
    }

    @Override
    public List<ClubVO> getClubList() {
        List<Club> clubs = clubMapper.selectList(new LambdaQueryWrapper<Club>()
                .orderByDesc(Club::getCreateTime));
        if (clubs.isEmpty()) {
            return List.of();
        }
        // 批量查询用户名：收集所有 adminId，一次远程调用拿到全部用户名，避免 N+1
        Map<Long, String> userNameMap = batchGetUserNames(
                clubs.stream().map(Club::getAdminId).collect(Collectors.toSet()));
        return clubs.stream()
                .map(club -> toVO(club, userNameMap.get(club.getAdminId())))
                .collect(Collectors.toList());
    }

    @Override
    public ClubVO getClubDetail(Long id) {
        Club club = getExistingClub(id);
        return toVO(club);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateClub(Long id, CreateClubRequest request) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        Club existing = getExistingClub(id);
        if (!existing.getAdminId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只有社团管理员可以修改社团信息");
        }

        Club club = new Club();
        club.setId(id);
        club.setName(request.getName());
        club.setDescription(request.getDescription());
        club.setLogo(request.getLogo());
        club.setContactInfo(request.getContactInfo());
        club.setUpdateTime(LocalDateTime.now());
        clubMapper.updateById(club);
    }

    @Override
    public List<ClubVO> getMyClubs() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        List<Club> clubs = clubMapper.selectList(new LambdaQueryWrapper<Club>()
                .eq(Club::getAdminId, userId)
                .orderByDesc(Club::getCreateTime));
        if (clubs.isEmpty()) {
            return List.of();
        }
        // 批量查询用户名：收集所有 adminId，一次远程调用拿到全部用户名，避免 N+1
        Map<Long, String> userNameMap = batchGetUserNames(
                clubs.stream().map(Club::getAdminId).collect(Collectors.toSet()));
        return clubs.stream()
                .map(club -> toVO(club, userNameMap.get(club.getAdminId())))
                .collect(Collectors.toList());
    }

    private Club getExistingClub(Long id) {
        Club club = clubMapper.selectById(id);
        if (club == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "社团不存在");
        }
        return club;
    }

    // 列表批量转换用，adminName 已从批量查询中获取
    private ClubVO toVO(Club club, String adminName) {
        return ClubVO.builder()
                .id(club.getId())
                .name(club.getName())
                .description(club.getDescription())
                .logo(club.getLogo())
                .adminId(club.getAdminId())
                .adminName(adminName != null ? adminName : "用户" + club.getAdminId())
                .contactInfo(club.getContactInfo())
                .createTime(club.getCreateTime())
                .updateTime(club.getUpdateTime())
                .build();
    }

    // 社团详情用，只查一个管理员，无需批量
    private ClubVO toVO(Club club) {
        return toVO(club, getUserName(club.getAdminId()));
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

    // 社团详情用，只查一个管理员，无需批量
    private String getUserName(Long userId) {
        try {
            UserSimpleDTO user = userFeignClient.getUserSimple(userId).getData();
            return user != null ? user.getUsername() : "用户" + userId;
        } catch (Exception e) {
            return "用户" + userId;
        }
    }
}
