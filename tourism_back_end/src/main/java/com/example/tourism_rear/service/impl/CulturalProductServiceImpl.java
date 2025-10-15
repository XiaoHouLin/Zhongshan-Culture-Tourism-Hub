package com.example.tourism_rear.service.impl;

import com.example.tourism_rear.mapper.CulturalProductMapper;
import com.example.tourism_rear.pack.pagination.PageBean;
import com.example.tourism_rear.pack.utils.CulturalProductCategory;
import com.example.tourism_rear.pack.utils.CulturalProductInfo;
import com.example.tourism_rear.pack.utils.EditCulturalProduct;
import com.example.tourism_rear.service.CulturalProductService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CulturalProductServiceImpl implements CulturalProductService {

    private final CulturalProductMapper productMapper;

    public CulturalProductServiceImpl(CulturalProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @Override
    public void addProduct(CulturalProductInfo productInfo) {
        productMapper.insert(productInfo);
    }

    @Override
    public void deleteProducts(List<String> productCodes) {
        productMapper.deleteProducts(productCodes);
    }

    @Override
    public boolean isProductCodeAvailable(String targetProductCode, String originalProductCode) {
        return productMapper.isProductCodeAvailable(targetProductCode, originalProductCode) <= 0;
    }

    @Override
    public boolean isProductCodeUnique(String productCode) {
        return productMapper.countByProductCode(productCode) <= 0;
    }

    @Override
    public PageBean list(Integer page, Integer pageSize,
                         String productCode, String productName, String displayMode, String highlight,
                         String category, Integer minPrice, Integer maxPrice,
                         Integer minStock, Integer maxStock) {
        try (Page<?> ignored = PageHelper.startPage(page, pageSize)) {
            List<CulturalProductInfo> products = productMapper.list(productCode, productName, displayMode, highlight,
                    category, minPrice, maxPrice, minStock, maxStock);
            Page<CulturalProductInfo> productPage = (Page<CulturalProductInfo>) products;
            return new PageBean(productPage.getTotal(), productPage.getResult());
        }
    }

    @Override
    public CulturalProductInfo getProductByCode(String productCode) {
        return productMapper.getProductByCode(productCode);
    }

    @Override
    public void updateProduct(EditCulturalProduct productInfo) {
        productMapper.updateProduct(productInfo);
    }

    @Override
    public List<CulturalProductCategory> categoryStatistics() {
        return productMapper.categoryStatistics();
    }
}
