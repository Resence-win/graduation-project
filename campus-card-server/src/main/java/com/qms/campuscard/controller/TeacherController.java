package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.common.LogUtil;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.entity.Teacher;
import com.qms.campuscard.service.TeacherService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    private final TeacherService teacherService;

    @Resource
    private LogUtil logUtil;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @PostMapping
    public Result<Boolean> addTeacher(@RequestBody Teacher teacher) {
        boolean success = teacherService.addTeacher(teacher);
        if (success) {
            // 记录日志
            logUtil.recordLog(1L, "新增", "teacher", teacher.getId(), "新增教师：" + teacher.getName());
            return Result.success("添加成功", true);
        } else {
            return Result.error("添加失败");
        }
    }

    @GetMapping("/list")
    public Result<IPage<Teacher>> getTeacherList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String teacherNo,
            @RequestParam(required = false) String name) {
        Page<Teacher> pageParam = new Page<>(page, size);
        IPage<Teacher> teacherPage = teacherService.getTeacherList(pageParam, teacherNo, name);
        // 记录日志
        logUtil.recordLog(1L, "查询", "teacher", null, "查询教师列表");
        return Result.success(teacherPage);
    }

    @GetMapping("/{id}")
    public Result<Teacher> getTeacherById(@PathVariable Long id) {
        Teacher teacher = teacherService.getTeacherById(id);
        if (teacher != null) {
            // 记录日志
            logUtil.recordLog(1L, "查询", "teacher", id, "查询教师详情：" + teacher.getName());
            return Result.success(teacher);
        } else {
            return Result.error("教师不存在");
        }
    }

    @PutMapping
    public Result<Boolean> updateTeacher(@RequestBody Teacher teacher) {
        boolean success = teacherService.updateTeacher(teacher);
        if (success) {
            // 记录日志
            logUtil.recordLog(1L, "修改", "teacher", teacher.getId(), "修改教师：" + teacher.getName());
            return Result.success("更新成功", true);
        } else {
            return Result.error("更新失败");
        }
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteTeacher(@PathVariable Long id) {
        Teacher teacher = teacherService.getTeacherById(id);
        boolean success = teacherService.deleteTeacher(id);
        if (success) {
            // 记录日志
            logUtil.recordLog(1L, "删除", "teacher", id, "删除教师：" + (teacher != null ? teacher.getName() : "未知"));
            return Result.success("删除成功", true);
        } else {
            return Result.error("删除失败");
        }
    }
}