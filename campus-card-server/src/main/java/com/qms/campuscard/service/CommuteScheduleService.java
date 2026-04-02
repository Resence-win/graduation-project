package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.entity.CommuteSchedule;

public interface CommuteScheduleService {
    IPage<CommuteSchedule> getScheduleList(Long routeId, Long vehicleId, Integer status, Integer page, Integer size);
    CommuteSchedule getScheduleById(Long id);
    boolean addSchedule(CommuteSchedule schedule);
    boolean updateSchedule(CommuteSchedule schedule);
    boolean deleteSchedule(Long id);
}
