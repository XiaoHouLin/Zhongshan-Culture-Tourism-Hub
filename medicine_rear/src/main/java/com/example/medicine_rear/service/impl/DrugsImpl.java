package com.example.medicine_rear.service.impl;

import com.example.medicine_rear.mapper.DrugsMapper;
import com.example.medicine_rear.pack.pagination.PageBean;
import com.example.medicine_rear.pack.utils.DrugsClassify;
import com.example.medicine_rear.pack.utils.DrugsInfo;
import com.example.medicine_rear.service.DrugsService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DrugsImpl implements DrugsService {
    @Autowired
    private DrugsMapper drugsMapper;

    /**
     * 添加药物
     *
     * @author : 🐒
     */
    @Override
    public void addDrugsInfo(DrugsInfo drugsInfo) {
        drugsMapper.insert(drugsInfo);
    }

    /**
     * 删除药物(不可撤销)
     *
     * @author : 🐒
     */
    @Override
    public void deleteDrugs(List<String> mno) {
        drugsMapper.deleteDrugs(mno);
    }

    /**
     * 检测药物主键是否重复
     *
     * @author : 🐒
     */
    @Override
    public boolean isUniqueDrugs(String changeMno, String mno) {
        return drugsMapper.isUniqueDrugs(changeMno, mno) <= 0;
    }

    @Override
    public boolean multiIsUniqueDrugs(String mno) {
        return drugsMapper.multiIsUniqueDrugs(mno) <= 0;
    }

    /**
     * 查询药物所有信息
     *
     * @return : medicine表中所有的数据
     * @author : 🐒
     */
    @Override
    public PageBean list(Integer page, Integer pageSize,
                         String mno, String mname, String mmode, String mefficacy,
                         String mtype, Integer minMprice, Integer maxMprice, Integer minMnum, Integer maxMnum) {
        try (Page<?> ignored = PageHelper.startPage(page, pageSize)) {
            List<DrugsInfo> drugsInfoList = drugsMapper.list(mno, mname, mmode, mefficacy, mtype, minMprice, maxMprice, minMnum, maxMnum);
            Page<DrugsInfo> drugsInfoPage = (Page<DrugsInfo>) drugsInfoList;
            return new PageBean(drugsInfoPage.getTotal(), drugsInfoPage.getResult());
        }
    }

    /**
     * 数据回显
     *
     * @return : medicine表所选中mno的所有数据
     * @author : 🐒
     */
    @Override
    public DrugsInfo getChangeDrugsInfo(String mno) {
        return drugsMapper.getChangeDrugsInfo(mno);
    }

    /**
     * 修改药物信息
     *
     * @author : 🐒
     */
    @Override
    public void updateDrugsInfo(DrugsInfo drugsInfo) {
        drugsMapper.updateDrugsInfo(drugsInfo);
    }

    /**
     * 根据订单详情表中所有的purchaseNum,统计药物数量
     *
     * @author : 🐒
     */
    @Override
    public List<DrugsClassify> drugsClassify() {
        return drugsMapper.drugsClassify();
    }
}
