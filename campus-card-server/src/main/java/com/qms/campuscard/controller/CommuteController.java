package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.common.LogUtil;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.entity.CommuteRoute;
import com.qms.campuscard.entity.CommuteVehicle;
import com.qms.campuscard.entity.CommuteStation;
import com.qms.campuscard.entity.CommuteSchedule;
import com.qms.campuscard.entity.CommuteRecord;
import com.qms.campuscard.service.*;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/commute")
public class CommuteController {

    @Resource
    private LogUtil logUtil;
    
    @Resource
    private CommuteRouteService commuteRouteService;
    
    @Resource
    private CommuteVehicleService commuteVehicleService;
    
    @Resource
    private CommuteStationService commuteStationService;
    
    @Resource
    private CommuteScheduleService commuteScheduleService;
    
    @Resource
    private CommuteRecordService commuteRecordService;

    // 线路管理
    @PostMapping("/route")
    public Result<String> addRoute(@RequestBody CommuteRoute route) {
        boolean result = commuteRouteService.addRoute(route);
        if (result) {
            logUtil.recordLog(1L, "新增", "commute_route", null, "新增线路：" + route.getRouteName());
            return Result.success("新增线路成功");
        } else {
            return Result.error("新增线路失败");
        }
    }

    @GetMapping("/route/list")
    public Result<IPage<CommuteRoute>> getRouteList(
            @RequestParam(required = false) String routeName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        IPage<CommuteRoute> routeList = commuteRouteService.getRouteList(routeName, status, page, size);
        logUtil.recordLog(1L, "查询", "commute_route", null, "查询线路列表");
        return Result.success(routeList);
    }

    @GetMapping("/route/{id}")
    public Result<CommuteRoute> getRouteDetail(@PathVariable Long id) {
        CommuteRoute route = commuteRouteService.getRouteById(id);
        logUtil.recordLog(1L, "查询", "commute_route", id, "查询线路详情");
        return Result.success(route);
    }

    @PutMapping("/route")
    public Result<String> updateRoute(@RequestBody CommuteRoute route) {
        boolean result = commuteRouteService.updateRoute(route);
        if (result) {
            logUtil.recordLog(1L, "修改", "commute_route", route.getId(), "修改线路：" + route.getRouteName());
            return Result.success("修改线路成功");
        } else {
            return Result.error("修改线路失败");
        }
    }

    @DeleteMapping("/route/{id}")
    public Result<String> deleteRoute(@PathVariable Long id) {
        boolean result = commuteRouteService.deleteRoute(id);
        if (result) {
            logUtil.recordLog(1L, "删除", "commute_route", id, "删除线路");
            return Result.success("删除线路成功");
        } else {
            return Result.error("删除线路失败");
        }
    }

    // 车辆管理
    @PostMapping("/vehicle")
    public Result<String> addVehicle(@RequestBody CommuteVehicle vehicle) {
        boolean result = commuteVehicleService.addVehicle(vehicle);
        if (result) {
            logUtil.recordLog(1L, "新增", "commute_vehicle", null, "新增车辆：" + vehicle.getPlateNumber());
            return Result.success("新增车辆成功");
        } else {
            return Result.error("新增车辆失败");
        }
    }

    @GetMapping("/vehicle/list")
    public Result<IPage<CommuteVehicle>> getVehicleList(
            @RequestParam(required = false) String plateNumber,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        IPage<CommuteVehicle> vehicleList = commuteVehicleService.getVehicleList(plateNumber, status, page, size);
        logUtil.recordLog(1L, "查询", "commute_vehicle", null, "查询车辆列表");
        return Result.success(vehicleList);
    }

    @GetMapping("/vehicle/{id}")
    public Result<CommuteVehicle> getVehicleDetail(@PathVariable Long id) {
        CommuteVehicle vehicle = commuteVehicleService.getVehicleById(id);
        logUtil.recordLog(1L, "查询", "commute_vehicle", id, "查询车辆详情");
        return Result.success(vehicle);
    }

    @PutMapping("/vehicle")
    public Result<String> updateVehicle(@RequestBody CommuteVehicle vehicle) {
        boolean result = commuteVehicleService.updateVehicle(vehicle);
        if (result) {
            logUtil.recordLog(1L, "修改", "commute_vehicle", vehicle.getId(), "修改车辆：" + vehicle.getPlateNumber());
            return Result.success("修改车辆成功");
        } else {
            return Result.error("修改车辆失败");
        }
    }

    @DeleteMapping("/vehicle/{id}")
    public Result<String> deleteVehicle(@PathVariable Long id) {
        boolean result = commuteVehicleService.deleteVehicle(id);
        if (result) {
            logUtil.recordLog(1L, "删除", "commute_vehicle", id, "删除车辆");
            return Result.success("删除车辆成功");
        } else {
            return Result.error("删除车辆失败");
        }
    }

    // 站点管理
    @PostMapping("/station")
    public Result<String> addStation(@RequestBody CommuteStation station) {
        boolean result = commuteStationService.addStation(station);
        if (result) {
            logUtil.recordLog(1L, "新增", "commute_station", null, "新增站点：" + station.getStationName());
            return Result.success("新增站点成功");
        } else {
            return Result.error("新增站点失败");
        }
    }

    @GetMapping("/station/list")
    public Result<IPage<CommuteStation>> getStationList(
            @RequestParam(required = false) String stationName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        IPage<CommuteStation> stationList = commuteStationService.getStationList(stationName, status, page, size);
        logUtil.recordLog(1L, "查询", "commute_station", null, "查询站点列表");
        return Result.success(stationList);
    }

    @GetMapping("/station/{id}")
    public Result<CommuteStation> getStationDetail(@PathVariable Long id) {
        CommuteStation station = commuteStationService.getStationById(id);
        logUtil.recordLog(1L, "查询", "commute_station", id, "查询站点详情");
        return Result.success(station);
    }

    @PutMapping("/station")
    public Result<String> updateStation(@RequestBody CommuteStation station) {
        boolean result = commuteStationService.updateStation(station);
        if (result) {
            logUtil.recordLog(1L, "修改", "commute_station", station.getId(), "修改站点：" + station.getStationName());
            return Result.success("修改站点成功");
        } else {
            return Result.error("修改站点失败");
        }
    }

    @DeleteMapping("/station/{id}")
    public Result<String> deleteStation(@PathVariable Long id) {
        boolean result = commuteStationService.deleteStation(id);
        if (result) {
            logUtil.recordLog(1L, "删除", "commute_station", id, "删除站点");
            return Result.success("删除站点成功");
        } else {
            return Result.error("删除站点失败");
        }
    }

    // 时刻表管理
    @PostMapping("/schedule")
    public Result<String> addSchedule(@RequestBody CommuteSchedule schedule) {
        boolean result = commuteScheduleService.addSchedule(schedule);
        if (result) {
            logUtil.recordLog(1L, "新增", "commute_schedule", null, "新增时刻表");
            return Result.success("新增时刻表成功");
        } else {
            return Result.error("新增时刻表失败");
        }
    }

    @GetMapping("/schedule/list")
    public Result<IPage<CommuteSchedule>> getScheduleList(
            @RequestParam(required = false) Long routeId,
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        IPage<CommuteSchedule> scheduleList = commuteScheduleService.getScheduleList(routeId, vehicleId, status, page, size);
        logUtil.recordLog(1L, "查询", "commute_schedule", null, "查询时刻表列表");
        return Result.success(scheduleList);
    }

    @GetMapping("/schedule/{id}")
    public Result<CommuteSchedule> getScheduleDetail(@PathVariable Long id) {
        CommuteSchedule schedule = commuteScheduleService.getScheduleById(id);
        logUtil.recordLog(1L, "查询", "commute_schedule", id, "查询时刻表详情");
        return Result.success(schedule);
    }

    @PutMapping("/schedule")
    public Result<String> updateSchedule(@RequestBody CommuteSchedule schedule) {
        boolean result = commuteScheduleService.updateSchedule(schedule);
        if (result) {
            logUtil.recordLog(1L, "修改", "commute_schedule", schedule.getId(), "修改时刻表");
            return Result.success("修改时刻表成功");
        } else {
            return Result.error("修改时刻表失败");
        }
    }

    @DeleteMapping("/schedule/{id}")
    public Result<String> deleteSchedule(@PathVariable Long id) {
        boolean result = commuteScheduleService.deleteSchedule(id);
        if (result) {
            logUtil.recordLog(1L, "删除", "commute_schedule", id, "删除时刻表");
            return Result.success("删除时刻表成功");
        } else {
            return Result.error("删除时刻表失败");
        }
    }
    
    // 添加乘车记录
    @PostMapping("/record")
    public Result<String> addCommuteRecord(@RequestBody CommuteRecord record) {
        boolean result = commuteRecordService.addCommuteRecord(record);
        if (result) {
            logUtil.recordLog(1L, "新增", "commute_record", null, "新增乘车记录：卡ID" + record.getCardId());
            return Result.success("新增乘车记录成功");
        } else {
            return Result.error("新增乘车记录失败");
        }
    }
}
