package com.example.tourism_rear.mapper;

import com.example.tourism_rear.pack.utils.CulturalProductCategory;
import com.example.tourism_rear.pack.utils.CulturalProductInfo;
import com.example.tourism_rear.pack.utils.EditCulturalProduct;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CulturalProductMapper {

    List<CulturalProductInfo> list(@Param("productCode") String productCode,
                                   @Param("productName") String productName,
                                   @Param("displayMode") String displayMode,
                                   @Param("highlight") String highlight,
                                   @Param("category") String category,
                                   @Param("minPrice") Integer minPrice,
                                   @Param("maxPrice") Integer maxPrice,
                                   @Param("minStock") Integer minStock,
                                   @Param("maxStock") Integer maxStock);

    @Insert("INSERT INTO cultural_product (productCode, productName, displayMode, highlight, category, price, stock, create_time) " +
            "VALUES (#{productCode},#{productName},#{displayMode},#{highlight},#{category},#{price},#{stock},#{createTime})")
    void insert(CulturalProductInfo productInfo);

    void deleteProducts(List<String> productCodes);

    @Select("select count(*) from cultural_product where productCode=#{targetProductCode} and productCode != #{originalProductCode}")
    int isProductCodeAvailable(@Param("targetProductCode") String targetProductCode,
                               @Param("originalProductCode") String originalProductCode);

    @Select("select count(*) from cultural_product where productCode=#{productCode}")
    int countByProductCode(@Param("productCode") String productCode);

    @Select("select * from cultural_product where productCode=#{productCode}")
    CulturalProductInfo getProductByCode(@Param("productCode") String productCode);

    void updateProduct(EditCulturalProduct productInfo);

    void updateProductStock(@Param("quantity") Integer quantity, @Param("productCode") String productCode);

    List<CulturalProductCategory> categoryStatistics();
}
