package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.common.LogUtil;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.entity.Merchant;
import com.qms.campuscard.entity.MerchantType;
import com.qms.campuscard.service.MerchantService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/merchant")
public class MerchantController {

    private final MerchantService merchantService;

    @Resource
    private LogUtil logUtil;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @PostMapping("/type")
    public Result<Boolean> addMerchantType(@RequestBody MerchantType merchantType) {
        boolean success = merchantService.addMerchantType(merchantType);
        if (success) {
            // 记录日志
            logUtil.recordLog(1L, "新增", "merchant_type", merchantType.getId(), "新增商户类型：" + merchantType.getTypeName());
            return Result.success("添加成功", true);
        } else {
            return Result.error("添加失败");
        }
    }

    @GetMapping("/type/list")
    public Result<List<MerchantType>> getMerchantTypeList() {
        List<MerchantType> typeList = merchantService.getMerchantTypeList();
        // 记录日志
        logUtil.recordLog(1L, "查询", "merchant_type", null, "查询商户类型列表");
        return Result.success(typeList);
    }

    @DeleteMapping("/type/{id}")
    public Result<Boolean> deleteMerchantType(@PathVariable Long id) {
        boolean success = merchantService.deleteMerchantType(id);
        if (success) {
            // 记录日志
            logUtil.recordLog(1L, "删除", "merchant_type", id, "删除商户类型");
            return Result.success("删除成功", true);
        } else {
            return Result.error("删除失败");
        }
    }

    @PostMapping
    public Result<Boolean> addMerchant(@RequestBody Merchant merchant) {
        boolean success = merchantService.addMerchant(merchant);
        if (success) {
            // 记录日志
            logUtil.recordLog(1L, "新增", "merchant", merchant.getId(), "新增商户：" + merchant.getMerchantName());
            return Result.success("添加成功", true);
        } else {
            return Result.error("添加失败");
        }
    }

    @GetMapping("/list")
    public Result<IPage<com.qms.campuscard.dto.MerchantDTO>> getMerchantList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String merchantName,
            @RequestParam(required = false) Long typeId) {
        Page<Merchant> pageParam = new Page<>(page, size);
        IPage<com.qms.campuscard.dto.MerchantDTO> merchantPage = merchantService.getMerchantList(pageParam, merchantName, typeId);
        // 记录日志
        logUtil.recordLog(1L, "查询", "merchant", null, "查询商户列表");
        return Result.success(merchantPage);
    }

    @PutMapping
    public Result<Boolean> updateMerchant(@RequestBody Merchant merchant) {
        boolean success = merchantService.updateMerchant(merchant);
        if (success) {
            // 记录日志
            logUtil.recordLog(1L, "修改", "merchant", merchant.getId(), "修改商户：" + merchant.getMerchantName());
            return Result.success("更新成功", true);
        } else {
            return Result.error("更新失败");
        }
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteMerchant(@PathVariable Long id) {
        Merchant merchant = merchantService.getMerchantById(id);
        boolean success = merchantService.deleteMerchant(id);
        if (success) {
            // 记录日志
            logUtil.recordLog(1L, "删除", "merchant", id, "删除商户：" + (merchant != null ? merchant.getMerchantName() : "未知"));
            return Result.success("删除成功", true);
        } else {
            return Result.error("删除失败");
        }
    }

    @DeleteMapping("/batch")
    public Result<Boolean> deleteMerchants(@RequestBody List<Long> ids) {
        boolean success = merchantService.deleteMerchants(ids);
        if (success) {
            // 记录日志
            logUtil.recordLog(1L, "删除", "merchant", null, "批量删除商户，ID：" + ids.toString());
            return Result.success("批量删除成功", true);
        } else {
            return Result.error("批量删除失败");
        }
    }

    @PostMapping("/upload-logo")
    public Result<String> uploadLogo(@RequestParam("file") MultipartFile file, @RequestParam("merchant_id") Long merchantId) {
        try {
            String url = merchantService.uploadLogo(file, merchantId);
            // 记录日志
            logUtil.recordLog(1L, "上传", "merchant", merchantId, "上传商户Logo");
            return Result.success("上传成功", url);
        } catch (Exception e) {
            return Result.error("上传失败：" + e.getMessage());
        }
    }
}