package com.god.activity.service;

import com.god.activity.dto.CreateClubRequest;
import com.god.activity.vo.ClubVO;

import java.util.List;

public interface ClubService {

    Long createClub(CreateClubRequest request);

    List<ClubVO> getClubList();

    ClubVO getClubDetail(Long id);

    void updateClub(Long id, CreateClubRequest request);

    List<ClubVO> getMyClubs();
}
