package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 新增员工
     * @param employeeDTO
     */
    void save(EmployeeDTO employeeDTO);

    /**
     * 分页查询
     * @param employeePageQueryDTO
     */
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 员工启用或禁用
     * @param status
     * @param id
     * @return

     */
    void startOrStop(Integer status, Long id);

    /**
     * 根据ID查询员工信息
     * @parm id
     * @return
     */
    Employee getById(Long id);

    /**
     * 编辑员工信息
     */
    void update(EmployeeDTO employeeDTO);

}
