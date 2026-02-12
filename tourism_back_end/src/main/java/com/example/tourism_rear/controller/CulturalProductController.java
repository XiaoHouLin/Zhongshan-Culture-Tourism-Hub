package com.example.tourism_rear.controller;

import com.example.tourism_rear.pack.pagination.PageBean;
import com.example.tourism_rear.pack.utils.Jwt;
import com.example.tourism_rear.pack.utils.CulturalProductInfo;
import com.example.tourism_rear.pack.utils.EditCulturalProduct;
import com.example.tourism_rear.pack.utils.Result;
import com.example.tourism_rear.service.RealtimeService;
import com.example.tourism_rear.service.CulturalProductService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/cultural-products")
public class CulturalProductController {

    private final CulturalProductService productService;
    private final RealtimeService realtimeService;

    public CulturalProductController(CulturalProductService productService, RealtimeService realtimeService) {
        this.productService = productService;
        this.realtimeService = realtimeService;
    }

    @PostMapping
    public Result createProduct(@RequestBody CulturalProductInfo productInfo) {
        log.info("添加文创产品：{}", productInfo);
        productService.addProduct(productInfo);
        realtimeService.broadcastGlobalRefresh();
        return Result.success();
    }

    @DeleteMapping("/{productCodes}")
    public Result deleteProducts(@PathVariable List<String> productCodes) {
        log.info("批量删除文创产品，编号集合：{}", productCodes);
        productService.deleteProducts(productCodes);
        realtimeService.broadcastGlobalRefresh();
        return Result.success();
    }

    @GetMapping("/delete-check")
    public Result deleteCheck(@RequestParam String productCodes) {
        List<String> codes = parseProductCodes(productCodes);
        int count = productService.countPendingPayingUsers(codes);
        return Result.success(Map.of("payingUsers", count));
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
        realtimeService.broadcastGlobalRefresh();
        return Result.success();
    }

    @GetMapping("/{productCode}")
    public Result getProduct(@PathVariable String productCode) {
        log.info("查询文创产品，编号为{}", productCode);
        return Result.success(productService.getProductByCode(productCode));
    }

    @GetMapping("/next-code")
    public Result nextProductCode() {
        log.info("获取下一个文创产品编号");
        return Result.success(productService.nextProductCode());
    }

    @GetMapping("/categories")
    public Result listCategories() {
        log.info("查询文创产品分类列表");
        return Result.success(productService.listCategories());
    }

    @GetMapping
    public Result listProducts(@RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "20") Integer pageSize,
                               String productCode, String productName, String displayMode, String highlight,
                               String category, String categoryList, Integer minPrice, Integer maxPrice, Integer minStock,
                               Integer maxStock,
                               @RequestParam(required = false) Boolean exactMatch) {
        List<String> categoryFilters = parseCategoryList(categoryList);
        log.info("分页查询文创产品信息, 页码：{}, 数量：{}，条件：编号{}，名称{}，风格{}，亮点{}，分类{}，分类集合{}，价格{}-{}，库存{}-{}",
                page, pageSize, productCode, productName, displayMode, highlight, category, categoryFilters,
                minPrice, maxPrice, minStock, maxStock);
        PageBean productPage = productService.list(page, pageSize, productCode, productName, displayMode,
                highlight, category, categoryFilters, minPrice, maxPrice, minStock, maxStock, exactMatch);
        return Result.success(productPage);
    }

    @GetMapping("/category-statistics")
    public Result categoryStatistics() {
        log.info("查询文创产品分类统计信息");
        return Result.success(productService.categoryStatistics());
    }

    @GetMapping("/category-statistics/personal")
    public Result categoryStatisticsByUid(HttpServletRequest request) {
        Integer uid = getUidFromRequest(request);
        if (uid == null) {
            return Result.error("无权限");
        }
        log.info("查询用户文创品类偏好统计, uid={}", uid);
        return Result.success(productService.categoryStatisticsByUid(uid));
    }

    @GetMapping("/category-product-statistics")
    public Result categoryProductStatistics() {
        log.info("查询商城文创品类统计");
        return Result.success(productService.categoryProductStatistics());
    }

    private Integer getUidFromRequest(HttpServletRequest request) {
        String jwt = request.getHeader("Authorization");
        if (jwt == null || !jwt.contains("Bearer ")) {
            return null;
        }
        jwt = jwt.substring(7);
        try {
            Claims claims = Jwt.checkToken(jwt);
            Object uid = claims.get("uid");
            return uid == null ? null : Integer.parseInt(uid.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> parseCategoryList(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return Arrays.stream(raw.split("[,，、]"))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }

    private List<String> parseProductCodes(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split("[,，、]"))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }
}
