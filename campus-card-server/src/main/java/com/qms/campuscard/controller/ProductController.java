package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.common.LogUtil;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.dto.ProductDTO;
import com.qms.campuscard.entity.Product;
import com.qms.campuscard.service.ProductService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

    @Resource
    private LogUtil logUtil;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 新增商品接口：为指定商户创建可销售商品并维护价格、库存等信息。
     */
    @PostMapping
    public Result<Boolean> addProduct(@RequestBody Product product) {
        boolean success = productService.addProduct(product);
        if (success) {
            logUtil.recordLog(1L, "新增", "product", product.getId(), "新增商品：" + product.getProductName());
        }
        return success ? Result.success("新增成功", true) : Result.error("新增失败");
    }

    @PutMapping
    public Result<Boolean> updateProduct(@RequestBody Product product) {
        boolean success = productService.updateProduct(product);
        if (success) {
            logUtil.recordLog(1L, "修改", "product", product.getId(), "修改商品：" + product.getProductName());
        }
        return success ? Result.success("更新成功", true) : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteProduct(@PathVariable Long id) {
        boolean success = productService.deleteProduct(id);
        if (success) {
            logUtil.recordLog(1L, "删除", "product", id, "删除商品");
        }
        return success ? Result.success("删除成功", true) : Result.error("删除失败");
    }

    /**
     * 商品详情接口：根据商品ID查询商品基础信息和库存状态。
     */
    @GetMapping("/{id}")
    public Result<Product> getProduct(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return product != null ? Result.success(product) : Result.error("商品不存在");
    }

    /**
     * 商品列表接口：按商品名称、商户和状态分页查询商品信息。
     */
    @GetMapping("/list")
    public Result<IPage<ProductDTO>> getProductList(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) Integer status) {
        Page<Product> pageParam = new Page<>(page, size);
        IPage<ProductDTO> productPage = productService.getProductList(pageParam, productName, merchantId, status);
        return Result.success(productPage);
    }

    /**
     * 商品图片上传接口：上传并保存指定商品的展示图片地址。
     */
    @PostMapping("/upload-image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file, @RequestParam("product_id") Long productId) {
        String url = productService.uploadImage(file, productId);
        logUtil.recordLog(1L, "上传", "product", productId, "上传商品图片");
        return Result.success("上传成功", url);
    }
}
