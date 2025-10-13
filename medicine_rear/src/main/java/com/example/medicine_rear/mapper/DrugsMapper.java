package com.example.medicine_rear.mapper;

import com.example.medicine_rear.pack.utils.DrugsClassify;
import com.example.medicine_rear.pack.utils.DrugsInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DrugsMapper {
    /**
     * 查询所有药物
     *
     * @return List<DrugsInfo>
     */
    List<DrugsInfo> list(String mno, String mname, String mmode, String mefficacy, String mtype,
                         Integer minMprice, Integer maxMprice, Integer minMnum, Integer maxMnum);

    /**
     * 插入药物数据
     *
     * @param medicine 药物类
     */
    @Insert("INSERT INTO medicine VALUES (#{mno},#{mname},#{mmode},#{mefficacy},#{mtype},#{mprice},#{mnum})")
    void insert(DrugsInfo medicine);

    /**
     * 根据mno药物编号删除药物
     * 删除操作不可撤销！！
     *
     * @param mno 药物mno
     */
    void deleteDrugs(List<String> mno);

    /**
     * 根据mno查找主键
     */
    @Select("select count(*) from medicine where mno=#{changeMno} and mno != #{mno}")
    int isUniqueDrugs(String changeMno, String mno);

    @Select("select count(*) from medicine where mno=#{mno}")
    int multiIsUniqueDrugs(String mno);

    /**
     * 数据回显，根据mno获取修改药物信息
     *
     * @param mno 药物id
     */
    @Select("select * from medicine where mno=#{mno}")
    DrugsInfo getChangeDrugsInfo(String mno);

    /**
     * 修改药物信息
     *
     * @param drugsInfo 药物类
     */
    void updateDrugsInfo(DrugsInfo drugsInfo);

    /**
     * 修改药物数量(购买时，减去购买数量)
     *
     * @param mno 药物编号
     *            mnum 购买数量
     */
    void updateDrugsNum(Integer mnum, String mno);

    /**
     * 查询药物分类
     *
     * @return List<DrugsClassify>
     */
    List<DrugsClassify> drugsClassify();
}
