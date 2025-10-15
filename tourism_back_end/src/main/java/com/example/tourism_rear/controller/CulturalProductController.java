package com.example.tourism_rear.controller;

import com.example.tourism_rear.pack.pagination.PageBean;
import com.example.tourism_rear.pack.utils.CulturalProductInfo;
import com.example.tourism_rear.pack.utils.EditCulturalProduct;
import com.example.tourism_rear.pack.utils.Result;
import com.example.tourism_rear.service.CulturalProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/cultural-products")
public class CulturalProductController {

    private final CulturalProductService productService;

    public CulturalProductController(CulturalProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public Result createProduct(@RequestBody CulturalProductInfo productInfo) {
        log.info("添加文创产品：{}", productInfo);
        productService.addProduct(productInfo);
        return Result.success();
    }

    @DeleteMapping("/{productCodes}")
    public Result deleteProducts(@PathVariable List<String> productCodes) {
        log.info("批量删除文创产品，编号集合：{}", productCodes);
        productService.deleteProducts(productCodes);
        return Result.success();
    }

    @PostMapping("/validate/{targetProductCode}&{originalProductCode}")
    public Result validateProductCode(@PathVariable String targetProductCode,
                                      @PathVariable String originalProductCode) {
        log.info("文创产品{}尝试修改编号为{}", originalProductCode, targetProductCode);
        return productService.isProductCodeAvailable(targetProductCode, originalProductCode)
                ? Result.success()
                : Result.error("产品编号重复");
    }

    @PostMapping("/validate/{productCode}")
    public Result validateProductCodeOnCreate(@PathVariable String productCode) {
        log.info("验证新增文创产品编号是否重复：{}", productCode);
        return productService.isProductCodeUnique(productCode)
                ? Result.success()
                : Result.error("产品编号重复");
    }

    @PostMapping("/update")
    public Result updateProduct(@RequestBody EditCulturalProduct productInfo) {
        log.info("文创产品{}信息更新为：{}", productInfo.getOldProductCode(), productInfo);
        productService.updateProduct(productInfo);
        return Result.success();
    }

    @GetMapping("/{productCode}")
    public Result getProduct(@PathVariable String productCode) {
        log.info("查询文创产品，编号为{}", productCode);
        return Result.success(productService.getProductByCode(productCode));
    }

    @GetMapping
    public Result listProducts(@RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "20") Integer pageSize,
                               String productCode, String productName, String displayMode, String highlight,
                               String category, Integer minPrice, Integer maxPrice, Integer minStock,
                               Integer maxStock) {
        log.info("分页查询文创产品信息, 页码：{}, 数量：{}，条件：编号{}，名称{}，风格{}，亮点{}，分类{}，价格{}-{}，库存{}-{}",
                page, pageSize, productCode, productName, displayMode, highlight, category,
                minPrice, maxPrice, minStock, maxStock);
        PageBean productPage = productService.list(page, pageSize, productCode, productName, displayMode,
                highlight, category, minPrice, maxPrice, minStock, maxStock);
        return Result.success(productPage);
    }

    @GetMapping("/category-statistics")
    public Result categoryStatistics() {
        log.info("查询文创产品分类统计信息");
        return Result.success(productService.categoryStatistics());
    }
}
