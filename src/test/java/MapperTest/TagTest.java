package MapperTest;

import com.lilicould.blog.config.DataSourceConfig;
import com.lilicould.blog.config.MyBatisConfig;
import com.lilicould.blog.config.TransactionConfig;
import com.lilicould.blog.dao.TagMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import com.lilicould.blog.entity.Tag;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DataSourceConfig.class, MyBatisConfig.class, TransactionConfig.class})
@Transactional // 启用事务支持，保证测试不会真正修改数据库
public class TagTest {
    @Autowired
    private TagMapper tagMapper;

    @Test
    public void testInsert() {
        Tag tag = new Tag();
        tag.setName("测试标签");
        tag.setSlug("test-tag");

        int result = tagMapper.insert(tag);
        System.out.println(tagMapper.selectBySlug("test-tag"));
        Assert.assertEquals(1, result);
    }
}
