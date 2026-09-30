package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.weaver.AjAttribute;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *  自定义切面注解，实现公共on字段自动填充处理逻辑
 */

@Component
@Aspect
@Slf4j

public class AutoFillAspect {
    /**
     * 切入点
     * execution(* com.sky.mapper.*.*(..))  表示匹配com.sky.mapper包下的所有类  所有方法  所有参数
     * @annotation(com.sky.annotation.AutoFill) 表示匹配使用了com.sky.annotation.AutoFill注解的方法
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {}

    /**
     * 前置通知
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段填充{}", joinPoint);

        // 获取被拦截的方法上的数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//方法签名对像
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获取方法上的注解对象
        OperationType operationType = autoFill.value();//获取注解对象中的数据库操作类型

        // 获取当前被拦截的方法的参数--实体对象
        Object[] args = joinPoint.getArgs(); // 获取被拦截方法的所有参数
        if(args == null || args.length == 0){
            return ;  //被拦截方法根本就没有参数
        }
        // 规定被拦截方法啊的第一个参数为实体对象，以后如果想向被拦截方法中传参，实体对象要放在第一个
        Object entity = args[0];


        // 准备赋值数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        // 根据数据库操作类型，给实体对象的公共字段赋值
        if (operationType == OperationType.INSERT){
            // 插入
            try {
//                Method setCreateTime = entity.getClass().getMethod("setCreateTime", LocalDateTime.class);
//                Method setCreateUser = entity.getClass().getMethod("setCreateUser", Long.class);
//                Method setUpdateUser = entity.getClass().getMethod("setUpdateUser", Long.class);
//                Method setUpdateTime = entity.getClass().getMethod("setUpdateTime", LocalDateTime.class);
                Method setCreateTime = entity.getClass().getMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                log.error("自动填充字段出错", e);
                e.printStackTrace();
            }
        }else if(operationType == OperationType.UPDATE){
            try {
                // 更新
//                Method setUpdateTime = entity.getClass().getMethod("setUpdateTime", LocalDateTime.class);
//                Method setUpdateUser = entity.getClass().getMethod("setUpdateUser", Long.class);
                Method setUpdateTime = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER , Long.class);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                log.error("自动填充字段出错", e);
                e.printStackTrace();
            }
        }
    }

}
