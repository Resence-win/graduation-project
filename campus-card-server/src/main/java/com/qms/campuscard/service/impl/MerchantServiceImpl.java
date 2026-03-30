package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.Merchant;
import com.qms.campuscard.entity.MerchantType;
import com.qms.campuscard.dto.MerchantDTO;
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
    public IPage<MerchantDTO> getMerchantList(Page<Merchant> page, String merchantName, Long typeId) {
        QueryWrapper<Merchant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0);
        
        if (merchantName != null && !merchantName.isEmpty()) {
            queryWrapper.like("merchant_name", merchantName);
        }
        if (typeId != null) {
            queryWrapper.eq("type_id", typeId);
        }
        
        queryWrapper.orderByDesc("create_time");
        IPage<Merchant> merchantPage = merchantMapper.selectPage(page, queryWrapper);
        
        // 转换为DTO并添加类型名称
        return merchantPage.convert(merchant -> {
            MerchantDTO dto = new MerchantDTO();
            dto.setId(merchant.getId());
            dto.setMerchantName(merchant.getMerchantName());
            dto.setTypeId(merchant.getTypeId());
            dto.setLocation(merchant.getLocation());
            dto.setLogo(merchant.getLogo());
            dto.setStatus(merchant.getStatus());
            dto.setCreateTime(merchant.getCreateTime());
            dto.setUpdateTime(merchant.getUpdateTime());
            dto.setIsDeleted(merchant.getIsDeleted());
            
            // 获取商户类型名称
            if (merchant.getTypeId() != null) {
                QueryWrapper<MerchantType> typeQuery = new QueryWrapper<>();
                typeQuery.eq("id", merchant.getTypeId());
                typeQuery.eq("is_deleted", 0);
                MerchantType merchantType = merchantTypeMapper.selectOne(typeQuery);
                if (merchantType != null) {
                    dto.setTypeName(merchantType.getTypeName());
                }
            }
            
            return dto;
        });
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
    public String uploadLogo(MultipartFile file, Long merchantId) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + extension;

            // 直接使用项目根目录下的upload目录
            String absoluteUploadPath = System.getProperty("user.dir") + File.separator + "upload";

            File uploadDir = new File(absoluteUploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            File destFile = new File(uploadDir, newFilename);
            file.transferTo(destFile);

            String url = urlPrefix + "/" + newFilename;

            // 更新商户的logo字段
            Merchant merchant = getMerchantById(merchantId);
            if (merchant != null) {
                merchant.setLogo(url);
                merchant.setUpdateTime(LocalDateTime.now());
                merchantMapper.updateById(merchant);
            }

            return url;
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