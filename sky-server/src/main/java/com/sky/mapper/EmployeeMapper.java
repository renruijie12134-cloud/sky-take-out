package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    // 这条语句比较简单，所以我们使用注解直接编写SQL
    // 但是如果SQL语句比较复杂，或者需要动态SQL，那么我们就需要使用XML文件来编写SQL语句  ，比如分页查询、多条件查询
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 插入员工数据
     * @param employee
     */

    // 已开启驼峰命名，
    @Insert("insert into employee (username, password, name, phone, sex, id_number, status, create_time, update_time, create_user, update_user) " +
            "values (#{username}, #{password}, #{name}, #{phone}, #{sex}, #{idNumber}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @AutoFill(value = OperationType.INSERT)
    void insert(Employee employee);

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     * @return
     * 接下来要为分页查询编写SQL，因为它比较复杂
     * 所以我们使用XML文件来编写SQL语句
     * // 这个 Page 本质上是一个List集合
     */
    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);


    /**
     * 根据主键动态修改属性
     * @param employee
     * @return
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Employee employee);


    /**
     * 根据ID查询员工信息
     * @parm id
     * @return
     */
    @Select("select * from employee where id = #{id}")
    Employee getById(Long id);



}
