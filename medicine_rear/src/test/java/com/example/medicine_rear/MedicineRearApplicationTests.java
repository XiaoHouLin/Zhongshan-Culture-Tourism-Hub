package com.example.medicine_rear;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class MedicineRearApplicationTests {
//    @Autowired
//    private DrugsMapper drugsMapper;

//    @Test
//    public void test() {
//        String mno = "D0101\n";
//        String[] mnoList = mno.split("\n");
//        int mnum = 0;
//        double mprice = 0.0;
//        String _mprice;
//        String _mnum;
//        for (String s : mnoList) {
//            if (Math.random() < 0.8) {
//                mprice = (Math.random() * 100);
//            } else {
//                mprice = (Math.random() * 900) + 100;
//            }
//            _mprice = String.format("%.2f", mprice);
//            mnum = (int) (Math.floor(Math.random() * 5000) + 1);
//            _mnum = String.valueOf(mnum);
//            drugsMapper.updateDrugsNum(_mnum, _mprice, s);
//        }
//    }
    @Test
    public void testJwt() {
    }
}
