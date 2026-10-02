package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 对前端传来的密码需要进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     * @param employeeDTO
     */
    public void save(EmployeeDTO employeeDTO) {
        // 接下来我需要把 前端传递到 employeeDTO 中的数据传递到持久层
        // 所以我就需要把前端传递来的参数先给到实体类
        Employee employee = new Employee();
        // 对象属性拷贝
        // 将 employeeDTO 中的属性拷贝到 employee 中
        BeanUtils.copyProperties(employeeDTO, employee);
        // 实体类employee中有一些属性没有接收到值，需要手动赋值
        // 设置密码--密码要进行md5加密。且初始密码为123456 保存在PasswordConstant类中，这是一个常量类
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));


        // 设置账号状态--StatusConstant是我们定义的常量类，里面定义了账号状态的常量
        // ENABLE 表示账号正常，DISABLE 表示账号被锁定
        employee.setStatus(StatusConstant.ENABLE);

        // 设置当前记录的创建时间和修改时间
//        // 已经通过 AOP  处理了这些公共代码，所以这里不需要再处理
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//
//
//        // 设置当前记录的创建人和修改人
//        // 设置当前记录的创建人和修改人  后期它应该是一个登录的员工id
//        // 在拦截器 JwtTokenAdminInterceptor 中，已经把当前登录员工的id存入了ThreadLocal中了‘
//        // 现在我们直接用就好
//        employee.setCreateUser(BaseContext.getCurrentId());
//        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);


    }


    /**
     * 分页查询
     * @param employeePageQueryDTO
     */
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        // 导入了一个分页坐标：>pagehelper-spring-boot-starter
        // 这就使得接下来的操作很方便
        // 告诉我们引入的插件，我想查第几页，每页多少条，
        // 将 limit ? ? 中的参数设置好
        // 告诉 PageHelper：紧接着执行的 MyBatis 查询，需要查询第 2 页，每页 10 条。这一行本身没有查询数据库。
        // 它只是把分页信息准备好,然后等待下一条 MyBatis 查询
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        // pagehelper-spring-boot-starter 要求我们的返回值是固定的，就叫 page
        // 这个 Page 本质上是一个List集合
        // 真正执行查询,
        // PageHelper 会拦截紧接着的 MyBatis 查询，并根据数据库类型给 SQL 增加分页逻辑。
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);

        //第一行设置“下一次查询”的分页参数，
        // 第二行执行真正的数据库查询，PageHelper 会拦截这次查询并自动完成分页。

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 员工启用或禁用
     * @param status
     * @param id
     * @return

     */
    public void startOrStop(Integer status, Long id) {
        // 最终我希望执行：update employee set status = ? where id = ?
        // 但是为了提升复用性，我打算把它写成动态SQL，
        // 做动态更新的话就要传入一个实体类

        // 第一种对象创建方式
//        Employee employee = new Employee();
//        employee.setStatus(status);
//        employee.setId(id);

        // 第二种对象创建方式  Employee 这个类被@Builder 注解处理过，所以可以使用
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();

        employeeMapper.update(employee);

    }

    /**
     * 根据ID查询员工信息
     * @parm id
     * @return
     */
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        // 这个位置会读取到数据库里保存的员工信息，这个信息里有密码
        // 虽然密码是见过加密的，但是，就算是加密的，我也不想让前端看
        employee.setPassword("*******");
        return employee;
    }


    /**
     * 编辑员工信息
     */
    public void update(EmployeeDTO employeeDTO){
        Employee employee = new Employee();
        // 对象属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);


        // 其他employeeDTO中没有的属性可以不管
        // 但是，这个修改时间和修改人，还是得改一改的

        // 已经通过 AOP  处理了这些公共代码，所以这里不需要再处理
//        employee.setUpdateTime(LocalDateTime.now());
//
//        // 设置当前记录的创建人和修改人
//        // 设置当前记录的创建人和修改人  后期它应该是一个登录的员工id
//        // 在拦截器 JwtTokenAdminInterceptor 中，已经把当前登录员工的id存入了ThreadLocal中了‘
//        // 现在我们直接用就好
//        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.update(employee);
    }


}
/*


 */