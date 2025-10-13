package com.example.medicine_rear.controller;

import com.example.medicine_rear.pack.pagination.PageBean;
import com.example.medicine_rear.pack.utils.DrugsInfo;
import com.example.medicine_rear.pack.utils.EditDrugs;
import com.example.medicine_rear.pack.utils.Result;
import com.example.medicine_rear.service.DrugsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/drugsManagement")
public class DrugsController {
    @Autowired
    private DrugsService drugsService;

    /**
     * 添加一个药物
     *
     * @author : 🐒
     */
    @PostMapping("/insert")
    public Result drugsInfo(@RequestBody DrugsInfo drugsInfo) {
        log.info("添加一个药物：{}", drugsInfo);
        drugsService.addDrugsInfo(drugsInfo);
        return Result.success();
    }

    /**
     * (批量)删除药物(不可撤销)
     *
     * @author : 🐒
     */
    @DeleteMapping("/{mno}")
    public Result deleteDrugs(@PathVariable List<String> mno) {
        log.info("删除药物，mno为{}", mno);
        drugsService.deleteDrugs(mno);
        return Result.success();
    }

    /**
     * 检测主键是否重复，一种是传入2个值，比较现在主键与新主键是否重复；
     * 另一种是传入1个值，判断与数据库中所有药物主键是否重复
     *
     * @author : 🐒
     */
    @PostMapping("/{changeMno}&{mno}")
    public Result isUniqueDrugs(@PathVariable String changeMno, @PathVariable String mno) {
        log.info("药物{}尝试修改主键值为{}", mno, changeMno);
        if (drugsService.isUniqueDrugs(changeMno, mno)) {
            return Result.success();
        } else {
            return Result.error("主键重复");
        }
    }

    @PostMapping("/{mno}")
    public Result multiIsUniqueDrugs(@PathVariable String mno) {
        log.info("尝试添加新药物，主键值为{}", mno);
        if (drugsService.multiIsUniqueDrugs(mno)) {
            return Result.success();
        } else {
            return Result.error("主键重复");
        }
    }

    /**
     * 修改药物信息
     *
     * @author : 🐒
     */
    @PostMapping("/update")
    public Result updateDrugsInfo(@RequestBody EditDrugs editDrugs) {
        log.info("药物{}信息修改为：{}", editDrugs.getOldMno(), editDrugs);
        drugsService.updateDrugsInfo(editDrugs);
        return Result.success();
    }

    /**
     * 数据回显
     *
     * @return : medicine表选中的数据
     * @author : 🐒
     */
    @GetMapping("/{mno}")
    public Result getChangeDrugsInfo(@PathVariable String mno) {
        log.info("回显药物信息，药物编号为{}", mno);
        return Result.success(drugsService.getChangeDrugsInfo(mno));
    }

    /**
     * 查询药物所有信息
     *
     * @return : medicine表中所有的数据
     * @author : 🐒
     */
    @GetMapping
    public Result drugsInfo(@RequestParam(defaultValue = "1") Integer page,
                            @RequestParam(defaultValue = "20") Integer pageSize,
                            String mno, String mname, String mmode, String mefficacy,
                            String mtype, Integer minMprice, Integer maxMprice, Integer minMnum,
                            Integer maxMnum
    ) {
        log.info("分页查询药物信息,页码：{},每页数据量：{}," + "药物编号：{}，药物名：{}，" +
                        "使用方式{}，药效{}，类型{}，价格在{}到{}之间，库存在{}到{}之间",
                page, pageSize, mno, mname, mmode, mefficacy, mtype, minMprice, maxMprice, minMnum, maxMnum);
        PageBean drugsInfoList = drugsService.list(page, pageSize, mno, mname,
                mmode, mefficacy, mtype, minMprice, maxMprice, minMnum, maxMnum);
        return Result.success(drugsInfoList);
    }

    /**
     * 查询所有类型的购买信息
     *
     * @return : medicine表中所有的类型数量
     * @author : 🐒
     */
    @GetMapping("/classify")
    public Result drugsClassify() {
        log.info("查询所有类型的购买信息");
        return Result.success(drugsService.drugsClassify());
    }
}
