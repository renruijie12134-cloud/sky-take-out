package com.sky.mapper;

import com.sky.entity.Employee;
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

}
