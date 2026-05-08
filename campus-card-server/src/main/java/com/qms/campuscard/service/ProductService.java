package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.dto.ProductDTO;
import com.qms.campuscard.entity.Product;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {

    boolean addProduct(Product product);

    boolean updateProduct(Product product);

    boolean deleteProduct(Long id);

    Product getProductById(Long id);

    IPage<ProductDTO> getProductList(Page<Product> page, String productName, Long merchantId, Integer status);

    String uploadImage(MultipartFile file, Long productId);
}
