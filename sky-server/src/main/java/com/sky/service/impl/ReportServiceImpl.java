package com.sky.service.impl;


import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.cert.TrustAnchor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;


    /**
     * 指定时间内的营业额统计
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end){
        // 先处理横坐标（日期）
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        // 在处理每日的营业额，因为上述代码已经得到了每天的日期，现在我们要根据日期查询出每日的营业额
        List<Double> trunoverList = new ArrayList<>();
        for(LocalDate date:dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map<String, Object> map =new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover==null?0.0:turnover;
            trunoverList.add(turnover);
        }

        // 整合数据
        TurnoverReportVO turnoverReportVO = TurnoverReportVO
                                            .builder()
                                            .dateList(StringUtils.join(dateList, ","))
                                            .turnoverList(StringUtils.join(trunoverList, ","))
                                            .build();
        return turnoverReportVO;
    }

    /**
     * 指定时间内的用户统计
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end){
        // 先处理时间
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        // 处理每日新增的用户数和总用户数
        List<Integer> newUserList =new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map<String, LocalDateTime> map1 = new HashMap<>();
            Map<String, LocalDateTime> map2 = new HashMap<>();
            map1.put("begin", beginTime);
            map1.put("end", endTime);
            map2.put("end", endTime);
            Integer newUserCount = userMapper.countUser(map1);
            Integer totalUserCount = userMapper.countUser(map2);
            newUserList.add(newUserCount);
            totalUserList.add(totalUserCount);
        }


        UserReportVO userReportVO = UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
        return userReportVO;
    }



}
