package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.annotation.Audit;
import cn.lilicould.liliblog.constant.StatusConstant;
import cn.lilicould.liliblog.entity.BlogConfig;
import cn.lilicould.liliblog.enums.CodeEnum;
import cn.lilicould.liliblog.exception.BusinessException;
import cn.lilicould.liliblog.mapper.BlogConfigMapper;
import cn.lilicould.liliblog.response.AuthorConfigVO;
import cn.lilicould.liliblog.response.BlogConfigSectionVO;
import cn.lilicould.liliblog.response.BlogConfigVO;
import cn.lilicould.liliblog.response.HeroConfigVO;
import cn.lilicould.liliblog.service.BlogConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Lili_Could
 * @description 针对表【blog_config(站点配置表)】的数据库操作Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlogConfigServiceImpl extends ServiceImpl<BlogConfigMapper, BlogConfig>
        implements BlogConfigService {

    /**
     * 已建模的分组键
     */
    public static final String KEY_HERO = "hero";
    public static final String KEY_AUTHOR = "author";

    /**
     * 接口结构版本，结构不兼容变更时递增
     */
    private static final int SCHEMA_VERSION = 1;

    private static final String CACHE_NAME = "blogConfig";

    /**
     * 单个分组 JSON 的大小上限（字符数）
     */
    private static final int MAX_SECTION_LENGTH = 256 * 1024;

    private static final Pattern KEY_PATTERN = Pattern.compile("^[a-z][a-z0-9-]{1,63}$");

    /**
     * 允许的图标标识，前端据此映射组件与配色
     */
    private static final Set<String> ALLOWED_ICONS = Set.of("github", "bilibili", "music", "mail");

    private static final Set<String> WEB_SCHEMES = Set.of("http", "https");

    private final ObjectMapper objectMapper;

    @Override
    @Cacheable(cacheNames = CACHE_NAME, key = "'public'", sync = true)
    public BlogConfigVO getPublicConfig() {
        List<BlogConfig> rows = lambdaQuery()
                .eq(BlogConfig::getEnabled, StatusConstant.ENABLED)
                .orderByAsc(BlogConfig::getId)
                .list();

        BlogConfigVO vo = new BlogConfigVO();
        vo.setVersion(SCHEMA_VERSION);
        Map<String, Object> extensions = new LinkedHashMap<>();

        for (BlogConfig row : rows) {
            String key = row.getConfigKey();
            try {
                switch (key) {
                    case KEY_HERO -> vo.setHero(parse(row.getConfigValue(), HeroConfigVO.class));
                    case KEY_AUTHOR -> vo.setAuthor(parse(row.getConfigValue(), AuthorConfigVO.class));
                    // 未建模的分组原样透出，新增内容类型不需要改这里
                    default -> extensions.put(key, parse(row.getConfigValue(), Object.class));
                }
            } catch (Exception e) {
                // 单条脏数据不能拖垮整站配置，跳过并由前端走默认值
                log.error("站点配置解析失败，已跳过该分组: key={}", key, e);
            }
        }

        vo.setExtensions(extensions);
        vo.setUpdatedAt(rows.stream()
                .map(BlogConfig::getUpdateTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null));
        return vo;
    }

    @Override
    public List<BlogConfigSectionVO> listSections() {
        return lambdaQuery()
                .orderByAsc(BlogConfig::getId)
                .list()
                .stream()
                .map(this::toSectionVO)
                .toList();
    }

    @Override
    public BlogConfigSectionVO getSection(String key) {
        validateKey(key);
        BlogConfig row = lambdaQuery()
                .eq(BlogConfig::getConfigKey, key)
                .one();
        if (row == null) {
            throw new BusinessException(CodeEnum.BLOG_CONFIG_NOT_FOUND);
        }
        return toSectionVO(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = CACHE_NAME, allEntries = true)
    @Audit(
            module = "blogConfig",
            operation = "UPDATE",
            description = "'更新站点配置:' + #key",
            targetType = "BLOG_CONFIG",
            target = "#key"
    )
    public void upsertSection(String key, JsonNode value, Integer enabled, String remark) {
        validateKey(key);
        if (value == null || value.isNull() || !value.isObject()) {
            throw new BusinessException(CodeEnum.PARAM_FORMAT_ERROR);
        }
        if (enabled != null && !StatusConstant.ENABLED.equals(enabled) && !StatusConstant.DISABLED.equals(enabled)) {
            throw new BusinessException(CodeEnum.PARAM_FORMAT_ERROR);
        }

        String json = objectMapper.writeValueAsString(value);
        validateSection(key, json);

        BlogConfig row = lambdaQuery()
                .eq(BlogConfig::getConfigKey, key)
                .one();
        if (row == null) {
            row = new BlogConfig();
            row.setConfigKey(key);
            row.setEnabled(enabled == null ? StatusConstant.ENABLED : enabled);
        } else if (enabled != null) {
            row.setEnabled(enabled);
        }
        if (remark != null) {
            row.setRemark(remark);
        }
        row.setConfigValue(json);
        saveOrUpdate(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = CACHE_NAME, allEntries = true)
    @Audit(
            module = "blogConfig",
            operation = "DELETE",
            description = "'删除站点配置:' + #key",
            targetType = "BLOG_CONFIG",
            target = "#key"
    )
    public void deleteSection(String key) {
        validateKey(key);
        boolean removed = remove(new LambdaQueryWrapper<BlogConfig>()
                .eq(BlogConfig::getConfigKey, key));
        if (!removed) {
            throw new BusinessException(CodeEnum.BLOG_CONFIG_NOT_FOUND);
        }
    }

    private BlogConfigSectionVO toSectionVO(BlogConfig row) {
        return BlogConfigSectionVO.builder()
                .id(row.getId())
                .key(row.getConfigKey())
                .value(readRaw(row.getConfigValue()))
                .enabled(row.getEnabled())
                .remark(row.getRemark())
                .createTime(row.getCreateTime())
                .updateTime(row.getUpdateTime())
                .build();
    }

    private Object readRaw(String json) {
        try {
            return parse(json, Object.class);
        } catch (Exception e) {
            log.warn("站点配置原始内容解析失败: {}", e.getMessage());
            return json;
        }
    }

    /**
     * 反序列化时忽略未知字段：配置表里的 JSON 可能比当前 VO 多出字段
     */
    private <T> T parse(String json, Class<T> type) {
        return objectMapper.readerFor(type)
                .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .readValue(json);
    }

    private void validateKey(String key) {
        if (key == null || !KEY_PATTERN.matcher(key).matches()) {
            throw new BusinessException(CodeEnum.PARAM_FORMAT_ERROR);
        }
    }

    private void validateSection(String key, String json) {
        if (json == null || json.length() > MAX_SECTION_LENGTH) {
            throw invalid();
        }
        try {
            switch (key) {
                case KEY_HERO -> validateHero(parse(json, HeroConfigVO.class));
                case KEY_AUTHOR -> validateAuthor(parse(json, AuthorConfigVO.class));
                // 扩展分组只校验 JSON 合法性与大小
                default -> {
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw invalid();
        }
    }

    private void validateHero(HeroConfigVO hero) {
        if (hero == null) {
            throw invalid();
        }

        HeroConfigVO.Typewriter typewriter = hero.getTypewriter();
        if (typewriter != null) {
            if (typewriter.getPhrases() != null) {
                requireMaxSize(typewriter.getPhrases().size(), 100);
                typewriter.getPhrases().forEach(item -> requireText(item, 200));
            }
            if (typewriter.getTemplates() != null) {
                requireMaxSize(typewriter.getTemplates().size(), 50);
                for (HeroConfigVO.CodeTemplate template : typewriter.getTemplates()) {
                    requireText(template.getKey(), 32);
                    requireText(template.getLabel(), 32);
                    limitText(template.getPrefix(), 200);
                    limitText(template.getSuffix(), 200);
                }
            }
        }

        if (hero.getSlides() != null) {
            requireMaxSize(hero.getSlides().size(), 20);
            for (HeroConfigVO.Slide slide : hero.getSlides()) {
                requireText(slide.getKey(), 64);
                requireUrl(slide.getSrc(), false);
                limitText(slide.getAlt(), 100);
            }
        }
    }

    private void validateAuthor(AuthorConfigVO author) {
        if (author == null) {
            throw invalid();
        }

        AuthorConfigVO.Profile profile = author.getProfile();
        if (profile != null) {
            limitText(profile.getAvatar(), 512);
            requireText(profile.getNickname(), 64);
            limitText(profile.getHandle(), 64);
            limitText(profile.getEmail(), 128);
            limitText(profile.getSignature(), 200);
            limitText(profile.getBadge(), 32);
            validateUrl(profile.getAvatar(), false);
        }

        if (author.getSocialLinks() != null) {
            requireMaxSize(author.getSocialLinks().size(), 20);
            for (AuthorConfigVO.SocialLink link : author.getSocialLinks()) {
                requireText(link.getKey(), 64);
                requireText(link.getName(), 64);
                requireUrl(link.getUrl(), true);
                if (link.getIcon() != null && !ALLOWED_ICONS.contains(link.getIcon())) {
                    throw invalid();
                }
            }
        }

        if (author.getInfoGroups() != null) {
            requireMaxSize(author.getInfoGroups().size(), 20);
            for (AuthorConfigVO.InfoGroup group : author.getInfoGroups()) {
                requireText(group.getKey(), 64);
                requireText(group.getText(), 64);
                limitText(group.getEmoji(), 16);
                if (group.getItems() != null) {
                    requireMaxSize(group.getItems().size(), 50);
                    group.getItems().forEach(item -> requireText(item, 100));
                }
            }
        }
    }

    private void requireMaxSize(int actual, int max) {
        if (actual > max) {
            throw invalid();
        }
    }

    private void requireText(String value, int maxLength) {
        if (value == null || value.isBlank() || value.length() > maxLength) {
            throw invalid();
        }
    }

    private void limitText(String value, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw invalid();
        }
    }

    private void requireUrl(String url, boolean allowMailto) {
        if (url == null || url.isBlank()) {
            throw invalid();
        }
        validateUrl(url, allowMailto);
    }

    private void validateUrl(String url, boolean allowMailto) {
        if (url == null || url.isBlank()) {
            return;
        }
        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();
            if (scheme == null) {
                throw invalid();
            }
            scheme = scheme.toLowerCase(Locale.ROOT);
            boolean allowed = WEB_SCHEMES.contains(scheme) || (allowMailto && "mailto".equals(scheme));
            if (!allowed) {
                throw invalid();
            }
            if (!"mailto".equals(scheme) && uri.getHost() == null) {
                throw invalid();
            }
        } catch (IllegalArgumentException e) {
            throw invalid();
        }
    }

    private BusinessException invalid() {
        return new BusinessException(CodeEnum.BLOG_CONFIG_INVALID);
    }
}
