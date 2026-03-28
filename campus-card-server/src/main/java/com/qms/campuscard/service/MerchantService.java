package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.Merchant;
import com.qms.campuscard.entity.MerchantType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MerchantService {

    boolean addMerchantType(MerchantType merchantType);

    List<MerchantType> getMerchantTypeList();

    boolean deleteMerchantType(Long id);

    boolean addMerchant(Merchant merchant);

    IPage<Merchant> getMerchantList(Page<Merchant> page, String merchantName, Long typeId);

    boolean updateMerchant(Merchant merchant);

    boolean deleteMerchant(Long id);

    boolean deleteMerchants(List<Long> ids);

    String uploadLogo(MultipartFile file);
}