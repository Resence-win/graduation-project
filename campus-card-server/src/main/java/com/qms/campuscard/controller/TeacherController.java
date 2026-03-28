package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.entity.Teacher;
import com.qms.campuscard.service.TeacherService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @PostMapping
    public Result<Boolean> addTeacher(@RequestBody Teacher teacher) {
        boolean success = teacherService.addTeacher(teacher);
        if (success) {
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
        return Result.success(teacherPage);
    }

    @GetMapping("/{id}")
    public Result<Teacher> getTeacherById(@PathVariable Long id) {
        Teacher teacher = teacherService.getTeacherById(id);
        if (teacher != null) {
            return Result.success(teacher);
        } else {
            return Result.error("教师不存在");
        }
    }

    @PutMapping
    public Result<Boolean> updateTeacher(@RequestBody Teacher teacher) {
        boolean success = teacherService.updateTeacher(teacher);
        if (success) {
            return Result.success("更新成功", true);
        } else {
            return Result.error("更新失败");
        }
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteTeacher(@PathVariable Long id) {
        boolean success = teacherService.deleteTeacher(id);
        if (success) {
            return Result.success("删除成功", true);
        } else {
            return Result.error("删除失败");
        }
    }
}