package MapperTest;

import com.lilicould.blog.config.DataSourceConfig;
import com.lilicould.blog.config.MyBatisConfig;
import com.lilicould.blog.config.TransactionConfig;
import com.lilicould.blog.dao.ArticleMapper;
import com.lilicould.blog.entity.Article;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * ArticleMapper 的单元测试类
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DataSourceConfig.class, MyBatisConfig.class, TransactionConfig.class})
@Transactional // 启用事务支持，保证测试不会真正修改数据库
public class ArticleMapperTest {

    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 测试根据ID查询文章
     */
    @Test
    public void testSelectById() {
        // 假设数据库中存在一个 ID 为 1 的文章
        Article article = articleMapper.selectById(1L);
        assertNotNull("文章不应为空", article);
        assertEquals("文章ID应匹配", Long.valueOf(1), article.getId());
        System.out.println(article);
    }

    /**
     * 测试根据Slug查询文章
     */
    @Test
    public void testSelectBySlug() {
        // 假设数据库中存在一个 slug 为 "spring-boot-practice" 的文章
        Article article = articleMapper.selectBySlug("spring-boot-practice");
        assertNotNull("文章不应为空", article);
        assertEquals("文章Slug应匹配", "spring-boot-practice", article.getSlug());
        System.out.println(article);
    }

    /**
     * 测试判断文章是否存在
     */
    @Test
    public void testExistsById() {
        // 假设数据库中存在一个 ID 为 1 的文章
        boolean exists = articleMapper.existsById(1L);
        assertTrue("文章应该存在", exists);
    }

    /**
     * 测试模糊搜索，分页查询
     */
    @Test
    public void testSearchWithPagination() {
        // 模糊搜索关键词
        String keyword = "R";
        Integer pageNum = 1;
        Integer pageSize = 5;

        List<Article> articles = articleMapper.search(keyword, pageNum * pageSize, pageSize);
        assertNotNull("文章列表不应为空", articles);
        assertFalse("文章列表应该有数据", articles.isEmpty());
        for (Article article : articles) {
            System.out.println(article);
        }
    }

    /**
     * 测试模糊搜索，查询全部
     */
    @Test
    public void testSearchAll() {
        // 模糊搜索关键词
        String keyword = "R";

        List<Article> articles = articleMapper.searchAll(keyword);
        assertNotNull("文章列表不应为空", articles);
        assertFalse("文章列表应该有数据", articles.isEmpty());
        for (Article article : articles) {
            System.out.println(article);
        }
    }

    /**
     * 测试插入文章
     */
    @Test
    public void testInsert() {
        Article newArticle = new Article();
        newArticle.setTitle("测试文章标题");
        newArticle.setSlug("test-article-slug");
        newArticle.setSummary("这是一篇测试文章的摘要");
        newArticle.setContent("这是测试文章的内容");
        newArticle.setContentHtml("<p>这是测试文章的HTML内容</p>");
        newArticle.setCoverImage("https://example.com/test-cover.jpg");
        newArticle.setStatus("PUBLISHED");
        newArticle.setViewCount(0);
        newArticle.setLikeCount(0);
        newArticle.setCommentCount(0);
        newArticle.setIsTop(0);
        newArticle.setIsRecommend(0);
        newArticle.setAuthorId(1L);
        newArticle.setCategoryId(1L);
        newArticle.setCreateTime(new Date());
        newArticle.setUpdateTime(new Date());
        newArticle.setPublishTime(new Date());

        int result = articleMapper.insert(newArticle);
        assertEquals("应该成功插入一行数据", 1, result);
    }

    /**
     * 测试更新文章
     */
    @Test
    public void testUpdate() {
        Article article = articleMapper.selectById(1L);
        assertNotNull("原始文章必须存在", article);

        String oldTitle = article.getTitle();
        String newTitle = "更新后的标题";

        article.setTitle(newTitle);
//        article.setStatus("DRAFT"); // 更新为草稿状态,如果加了此行，那结果应该是失败，因为下面代码的selectById()方法会返回null
        int result = articleMapper.update(article);

        assertEquals("应该成功更新一行数据", 1, result);

        // 再次查询验证是否真的更新了（虽然由于事务最终会回滚）
        Article updatedArticle = articleMapper.selectById(1L);
        assertEquals("标题应该已被更新", newTitle, updatedArticle.getTitle());
    }
}
