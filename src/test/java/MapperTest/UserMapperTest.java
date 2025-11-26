package MapperTest;

import com.lilicould.blog.config.DataSourceConfig;
import com.lilicould.blog.config.MyBatisConfig;
import com.lilicould.blog.config.TransactionConfig;
import com.lilicould.blog.dao.UserMapper;
import com.lilicould.blog.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * UserMapper 的单元测试类
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DataSourceConfig.class, MyBatisConfig.class, TransactionConfig.class})
@Transactional // 启用事务支持，保证测试不会真正修改数据库
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    /**
     * 测试根据用户名查询用户
     */
    @Test
    public void testSelectByUsername() {
        // 假设数据库中存在一个用户名为 "LiliCould" 的用户
        User user = userMapper.selectByUsername("LiliCould");
        assertNotNull("用户不应为空", user);
        assertEquals("用户名应匹配", "LiliCould", user.getUsername());
    }

    /**
     * 测试根据邮箱查询用户
     */
    @Test
    public void testSelectByEmail() {
        // 假设数据库中存在一个邮箱为 "123456@qq.com" 的用户
        User user = userMapper.selectByEmail("123456@qq.com");
        assertNotNull("用户不应为空", user);
        assertEquals("邮箱应匹配", "123456@qq.com", user.getEmail());
    }

    /**
     * 测试根据ID查询用户
     */
    @Test
    public void testSelectById() {
        // 假设数据库中存在一个 ID 为 1 的用户
        User user = userMapper.selectById(10L);
        assertNotNull("用户不应为空", user);
        assertEquals("用户ID应匹配", Long.valueOf(10), user.getId());
    }

    /**
     * 测试插入用户
     */
    @Test
    public void testInsert() {
        User newUser = new User();
        newUser.setUsername("insertTest");
        newUser.setPassword("password123");
        newUser.setEmail("insert@test.com");
        newUser.setNickname("测试用户");
        newUser.setCreateTime(new Date(System.currentTimeMillis()));
        newUser.setLastLoginTime(new Date());

        int result = userMapper.insert(newUser);
        assertEquals("应该成功插入一行数据", 1, result);
        // 因为使用了事务，这条记录实际上会被回滚，不会真正保存到数据库中
    }

    /**
     * 测试更新用户的最后登录时间
     */
    @Test
    public void testUpdateLastLoginTime() {
        Long userId = 10L; // 假设这个用户已存在
        Date newLoginTime = new Date(System.currentTimeMillis());

        int result = userMapper.updateLastLoginTime(userId, newLoginTime);
        assertEquals("应该成功更新一行数据", 1, result);
    }

    /**
     * 测试更新用户信息
     */
    @Test
    public void testUpdate() {
        User user = userMapper.selectById(10L);
        assertNotNull("原始用户必须存在", user);

        String oldEmail = user.getEmail();
        String newEmail = "updated@test.com";

        user.setEmail(newEmail);
        int result = userMapper.update(user);

        assertEquals("应该成功更新一行数据", 1, result);

        // 再次查询验证是否真的更新了（虽然由于事务最终会回滚）
        User updatedUser = userMapper.selectById(10L);
        assertEquals("邮箱应该已被更新", newEmail, updatedUser.getEmail());
    }

    /**
     * 测试更新用户密码
     */
    @Test
    public void testUpdatePassword() {
        Long userId = 10L; // 假设这个用户已存在
        String newPassword = "newPassword123";

        int result = userMapper.updatePassword(userId, newPassword);
        assertEquals("应该成功更新一行数据", 1, result);
    }
}
