package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.Merchant;
import com.qms.campuscard.entity.MerchantType;
import com.qms.campuscard.mapper.MerchantMapper;
import com.qms.campuscard.mapper.MerchantTypeMapper;
import com.qms.campuscard.service.MerchantService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MerchantServiceImpl implements MerchantService {

    @Resource
    private MerchantMapper merchantMapper;

    @Resource
    private MerchantTypeMapper merchantTypeMapper;

    @Value("${file.upload.path:/tmp/upload}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:/upload}")
    private String urlPrefix;

    @Override
    public boolean addMerchantType(MerchantType merchantType) {
        merchantType.setIsDeleted(0);
        merchantType.setCreateTime(LocalDateTime.now());
        return merchantTypeMapper.insert(merchantType) > 0;
    }

    @Override
    public List<MerchantType> getMerchantTypeList() {
        QueryWrapper<MerchantType> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0);
        return merchantTypeMapper.selectList(queryWrapper);
    }

    @Override
    public boolean deleteMerchantType(Long id) {
        MerchantType merchantType = new MerchantType();
        merchantType.setId(id);
        merchantType.setIsDeleted(1);
        return merchantTypeMapper.updateById(merchantType) > 0;
    }

    @Override
    public boolean addMerchant(Merchant merchant) {
        merchant.setStatus(1);
        merchant.setIsDeleted(0);
        merchant.setCreateTime(LocalDateTime.now());
        return merchantMapper.insert(merchant) > 0;
    }

    @Override
    public IPage<Merchant> getMerchantList(Page<Merchant> page, String merchantName, Long typeId) {
        QueryWrapper<Merchant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0);
        
        if (merchantName != null && !merchantName.isEmpty()) {
            queryWrapper.like("merchant_name", merchantName);
        }
        if (typeId != null) {
            queryWrapper.eq("type_id", typeId);
        }
        
        queryWrapper.orderByDesc("create_time");
        return merchantMapper.selectPage(page, queryWrapper);
    }

    @Override
    public boolean updateMerchant(Merchant merchant) {
        merchant.setUpdateTime(LocalDateTime.now());
        return merchantMapper.updateById(merchant) > 0;
    }

    @Override
    public boolean deleteMerchant(Long id) {
        Merchant merchant = new Merchant();
        merchant.setId(id);
        merchant.setIsDeleted(1);
        merchant.setUpdateTime(LocalDateTime.now());
        return merchantMapper.updateById(merchant) > 0;
    }

    @Override
    public boolean deleteMerchants(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        for (Long id : ids) {
            deleteMerchant(id);
        }
        return true;
    }

    @Override
    public String uploadLogo(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + extension;

            // 获取项目根目录的绝对路径
            String projectPath = System.getProperty("user.dir");
            String absoluteUploadPath = projectPath + File.separator + uploadPath.replace("./", "");

            File uploadDir = new File(absoluteUploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            File destFile = new File(uploadDir, newFilename);
            file.transferTo(destFile);

            return urlPrefix + "/" + newFilename;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public Merchant getMerchantById(Long id) {
        QueryWrapper<Merchant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        queryWrapper.eq("is_deleted", 0);
        return merchantMapper.selectOne(queryWrapper);
    }
}