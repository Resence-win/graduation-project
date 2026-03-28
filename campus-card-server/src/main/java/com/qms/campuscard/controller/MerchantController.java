package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.entity.Merchant;
import com.qms.campuscard.entity.MerchantType;
import com.qms.campuscard.service.MerchantService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/merchant")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @PostMapping("/type")
    public Result<Boolean> addMerchantType(@RequestBody MerchantType merchantType) {
        boolean success = merchantService.addMerchantType(merchantType);
        if (success) {
            return Result.success("添加成功", true);
        } else {
            return Result.error("添加失败");
        }
    }

    @GetMapping("/type/list")
    public Result<List<MerchantType>> getMerchantTypeList() {
        List<MerchantType> typeList = merchantService.getMerchantTypeList();
        return Result.success(typeList);
    }

    @DeleteMapping("/type/{id}")
    public Result<Boolean> deleteMerchantType(@PathVariable Long id) {
        boolean success = merchantService.deleteMerchantType(id);
        if (success) {
            return Result.success("删除成功", true);
        } else {
            return Result.error("删除失败");
        }
    }

    @PostMapping
    public Result<Boolean> addMerchant(@RequestBody Merchant merchant) {
        boolean success = merchantService.addMerchant(merchant);
        if (success) {
            return Result.success("添加成功", true);
        } else {
            return Result.error("添加失败");
        }
    }

    @GetMapping("/list")
    public Result<IPage<Merchant>> getMerchantList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String merchantName,
            @RequestParam(required = false) Long typeId) {
        Page<Merchant> pageParam = new Page<>(page, size);
        IPage<Merchant> merchantPage = merchantService.getMerchantList(pageParam, merchantName, typeId);
        return Result.success(merchantPage);
    }

    @PutMapping
    public Result<Boolean> updateMerchant(@RequestBody Merchant merchant) {
        boolean success = merchantService.updateMerchant(merchant);
        if (success) {
            return Result.success("更新成功", true);
        } else {
            return Result.error("更新失败");
        }
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteMerchant(@PathVariable Long id) {
        boolean success = merchantService.deleteMerchant(id);
        if (success) {
            return Result.success("删除成功", true);
        } else {
            return Result.error("删除失败");
        }
    }

    @DeleteMapping("/batch")
    public Result<Boolean> deleteMerchants(@RequestBody List<Long> ids) {
        boolean success = merchantService.deleteMerchants(ids);
        if (success) {
            return Result.success("批量删除成功", true);
        } else {
            return Result.error("批量删除失败");
        }
    }

    @PostMapping("/upload-logo")
    public Result<String> uploadLogo(@RequestParam("file") MultipartFile file) {
        try {
            String url = merchantService.uploadLogo(file);
            return Result.success("上传成功", url);
        } catch (Exception e) {
            return Result.error("上传失败：" + e.getMessage());
        }
    }
}