package com.example.tourism_rear.service;

import com.example.tourism_rear.pack.pagination.PageBean;
import com.example.tourism_rear.pack.utils.CulturalProductCategory;
import com.example.tourism_rear.pack.utils.CulturalProductInfo;
import com.example.tourism_rear.pack.utils.EditCulturalProduct;

import java.util.List;

public interface CulturalProductService {
    /**
     * 新增文创产品。
     */
    void addProduct(CulturalProductInfo productInfo);

    /**
     * 根据产品编号集合删除文创产品。
     */
    void deleteProducts(List<String> productCodes);

    /**
     * 验证产品编号是否可用（更新时）。
     */
    boolean isProductCodeAvailable(String targetProductCode, String originalProductCode);

    /**
     * 验证产品编号是否与现有记录冲突（新增时）。
     */
    boolean isProductCodeUnique(String productCode);

    /**
     * 获取下一个可用的文创产品编号（顺延，不复用已删除编号）。
     */
    String nextProductCode();

    /**
     * 获取当前已有的文创产品分类列表。
     */
    List<String> listCategories();

    /**
     * 分页查询文创产品。
     */
    PageBean list(Integer page, Integer pageSize,
                  String productCode, String productName, String displayMode, String highlight,
                  String category, Integer minPrice, Integer maxPrice,
                  Integer minStock, Integer maxStock);

    /**
     * 根据产品编号获取文创产品。
     */
    CulturalProductInfo getProductByCode(String productCode);

    /**
     * 更新文创产品。
     */
    void updateProduct(EditCulturalProduct productInfo);

    /**
     * 获取按分类汇总的购买数量。
     */
    List<CulturalProductCategory> categoryStatistics();
}
