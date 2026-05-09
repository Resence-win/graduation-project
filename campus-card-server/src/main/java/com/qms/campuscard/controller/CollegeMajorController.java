package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.common.LogUtil;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.entity.CollegeMajor;
import com.qms.campuscard.service.CollegeMajorService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/college-major")
public class CollegeMajorController {

    private final CollegeMajorService collegeMajorService;

    @Resource
    private LogUtil logUtil;

    public CollegeMajorController(CollegeMajorService collegeMajorService) {
        this.collegeMajorService = collegeMajorService;
    }

    @PostMapping
    public Result<Boolean> addCollegeMajor(@RequestBody CollegeMajor collegeMajor) {
        boolean success = collegeMajorService.addCollegeMajor(collegeMajor);
        if (success) {
            logUtil.recordLog(1L, "新增", "college_major", collegeMajor.getId(),
                    "新增学院专业：" + collegeMajor.getCollegeName() + "-" + collegeMajor.getMajorName());
            return Result.success("添加成功", true);
        }
        return Result.error("添加失败");
    }

    @PutMapping
    public Result<Boolean> updateCollegeMajor(@RequestBody CollegeMajor collegeMajor) {
        boolean success = collegeMajorService.updateCollegeMajor(collegeMajor);
        if (success) {
            logUtil.recordLog(1L, "修改", "college_major", collegeMajor.getId(),
                    "修改学院专业：" + collegeMajor.getCollegeName() + "-" + collegeMajor.getMajorName());
            return Result.success("更新成功", true);
        }
        return Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteCollegeMajor(@PathVariable Long id) {
        boolean success = collegeMajorService.deleteCollegeMajor(id);
        if (success) {
            logUtil.recordLog(1L, "删除", "college_major", id, "删除学院专业");
            return Result.success("删除成功", true);
        }
        return Result.error("删除失败");
    }

    @GetMapping("/list")
    public Result<IPage<CollegeMajor>> getCollegeMajorList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String collegeName,
            @RequestParam(required = false) String majorName,
            @RequestParam(required = false) Integer status) {
        Page<CollegeMajor> pageParam = new Page<>(page, size);
        IPage<CollegeMajor> collegeMajorPage = collegeMajorService.getCollegeMajorPage(pageParam, collegeName, majorName, status);
        logUtil.recordLog(1L, "查询", "college_major", null, "查询学院专业列表");
        return Result.success(collegeMajorPage);
    }

    @GetMapping("/active")
    public Result<List<CollegeMajor>> getActiveCollegeMajors() {
        return Result.success(collegeMajorService.getActiveCollegeMajors());
    }

    @GetMapping("/options")
    public Result<Map<String, List<String>>> getCollegeMajorOptions() {
        return Result.success(collegeMajorService.getCollegeMajorOptions());
    }
}
