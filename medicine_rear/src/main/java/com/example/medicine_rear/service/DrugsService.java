package com.example.medicine_rear.service;

import com.example.medicine_rear.pack.pagination.PageBean;
import com.example.medicine_rear.pack.utils.DrugsClassify;
import com.example.medicine_rear.pack.utils.DrugsInfo;

import java.util.List;

public interface DrugsService {
    /**
     * 新增药物
     *
     * @author : 🐒
     */
    void addDrugsInfo(DrugsInfo drugsInfo);

    /**
     * 删除药物(不可撤销)
     *
     * @author : 🐒
     */
    void deleteDrugs(List<String> mno);

    /**
     * 检测药物主键是否重复
     *
     * @author : 🐒
     */
    boolean isUniqueDrugs(String changeMno, String mno);
    boolean multiIsUniqueDrugs(String mno);

    /**
     * 查询所有药物信息
     *
     * @author : 🐒
     */
    PageBean list(Integer page, Integer pageSize,
                  String mno, String mname, String mmode, String mefficacy,
                  String mtype, Integer minMprice, Integer maxMprice,
                  Integer minMnum, Integer maxMnum);

    /**
     * 数据回显
     */
    DrugsInfo getChangeDrugsInfo(String mno);

    /**
     * 修改药物信息
     */
    void updateDrugsInfo(DrugsInfo drugsInfo);

    /**
     * 统计药物分类
     */
    List<DrugsClassify> drugsClassify();
}
