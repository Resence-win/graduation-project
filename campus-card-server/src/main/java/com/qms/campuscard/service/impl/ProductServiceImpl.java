package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.dto.ProductDTO;
import com.qms.campuscard.entity.Merchant;
import com.qms.campuscard.entity.Product;
import com.qms.campuscard.mapper.MerchantMapper;
import com.qms.campuscard.mapper.ProductMapper;
import com.qms.campuscard.service.ProductService;
import com.qms.campuscard.util.UploadPathResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    @Resource
    private ProductMapper productMapper;

    @Resource
    private MerchantMapper merchantMapper;

    @Resource
    private UploadPathResolver uploadPathResolver;

    @Value("${file.upload.url-prefix:/upload}")
    private String urlPrefix;

    @Override
    public boolean addProduct(Product product) {
        validateProduct(product);
        product.setStatus(product.getStatus() == null ? 1 : product.getStatus());
        product.setIsDeleted(0);
        product.setCreateTime(LocalDateTime.now());
        return productMapper.insert(product) > 0;
    }

    @Override
    public boolean updateProduct(Product product) {
        if (product.getId() == null) {
            throw new RuntimeException("商品ID不能为空");
        }
        validateProduct(product);
        product.setUpdateTime(LocalDateTime.now());
        return productMapper.updateById(product) > 0;
    }

    @Override
    public boolean deleteProduct(Long id) {
        Product product = new Product();
        product.setId(id);
        product.setIsDeleted(1);
        product.setUpdateTime(LocalDateTime.now());
        return productMapper.updateById(product) > 0;
    }

    @Override
    public Product getProductById(Long id) {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        queryWrapper.eq("is_deleted", 0);
        return productMapper.selectOne(queryWrapper);
    }

    @Override
    public IPage<ProductDTO> getProductList(Page<Product> page, String productName, Long merchantId, Integer status) {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0);
        if (productName != null && !productName.isEmpty()) {
            queryWrapper.like("product_name", productName);
        }
        if (merchantId != null) {
            queryWrapper.eq("merchant_id", merchantId);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("create_time");

        IPage<Product> productPage = productMapper.selectPage(page, queryWrapper);
        return productPage.convert(this::toDto);
    }

    @Override
    public String uploadImage(MultipartFile file, Long productId) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.lastIndexOf(".") >= 0) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID() + extension;
            Path uploadDir = uploadPathResolver.resolvePath("product");
            Files.createDirectories(uploadDir);

            File destFile = uploadDir.resolve(newFilename).toFile();
            file.transferTo(destFile);

            String url = urlPrefix + "/product/" + newFilename;
            Product product = getProductById(productId);
            if (product != null) {
                product.setImage(url);
                product.setUpdateTime(LocalDateTime.now());
                productMapper.updateById(product);
            }
            return url;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    private void validateProduct(Product product) {
        if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
            throw new RuntimeException("商品名称不能为空");
        }
        if (product.getMerchantId() == null) {
            throw new RuntimeException("所属商户不能为空");
        }
        Merchant merchant = merchantMapper.selectById(product.getMerchantId());
        if (merchant == null || merchant.getIsDeleted() == 1) {
            throw new RuntimeException("所属商户不存在");
        }
        if (merchant.getStatus() != null && merchant.getStatus() != 1) {
            throw new RuntimeException("所属商户状态异常");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("商品价格必须大于0");
        }
        if (product.getStock() == null || product.getStock() < 0) {
            throw new RuntimeException("商品库存不能小于0");
        }
    }

    private ProductDTO toDto(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setProductName(product.getProductName());
        dto.setMerchantId(product.getMerchantId());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setDescription(product.getDescription());
        dto.setImage(product.getImage());
        dto.setStatus(product.getStatus());
        dto.setCreateTime(product.getCreateTime());
        dto.setUpdateTime(product.getUpdateTime());
        dto.setIsDeleted(product.getIsDeleted());

        if (product.getMerchantId() != null) {
            Merchant merchant = merchantMapper.selectById(product.getMerchantId());
            if (merchant != null && merchant.getIsDeleted() == 0) {
                dto.setMerchantName(merchant.getMerchantName());
            }
        }
        return dto;
    }
}
