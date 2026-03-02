package com.pixiv.artwork.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 熔断降级配置
 * 
 * 配置 Feign 调用的熔断规则，包括：
 * - 慢调用比例熔断：当调用响应时间过长时触发熔断
 * - 异常比例熔断：当调用异常比例过高时触发熔断
 * - 异常数熔断：当调用异常数量过多时触发熔断
 * 
 * @author Pixiv Platform Team
 */
@Configuration
public class SentinelConfig {

        private static final Logger log = LoggerFactory.getLogger(SentinelConfig.class);

        /**
         * 初始化 Sentinel 熔断规则
         */
        @PostConstruct
        public void initRules() {
                initDegradeRules();
                initFlowRules();
                log.info("Sentinel 熔断降级规则初始化完成");
        }

        /**
         * 初始化熔断降级规则
         */
        private void initDegradeRules() {
                List<DegradeRule> rules = new ArrayList<>();

                // 用户服务熔断规则 - 慢调用比例
                DegradeRule userServiceSlowRule = new DegradeRule("GET:http://user-service/api/users/{userId}")
                                .setGrade(RuleConstant.DEGRADE_GRADE_RT) // 慢调用比例策略
                                .setCount(1000) // 响应时间阈值（毫秒）
                                .setTimeWindow(10) // 熔断时长（秒）
                                .setMinRequestAmount(5) // 最小请求数
                                .setSlowRatioThreshold(0.5) // 慢调用比例阈值（50%）
                                .setStatIntervalMs(1000); // 统计时长（毫秒）
                rules.add(userServiceSlowRule);

                // 用户服务熔断规则 - 异常比例
                DegradeRule userServiceErrorRatioRule = new DegradeRule("GET:http://user-service/api/users/{userId}")
                                .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO) // 异常比例策略
                                .setCount(0.5) // 异常比例阈值（50%）
                                .setTimeWindow(10) // 熔断时长（秒）
                                .setMinRequestAmount(5) // 最小请求数
                                .setStatIntervalMs(1000); // 统计时长（毫秒）
                rules.add(userServiceErrorRatioRule);

                // 用户服务熔断规则 - 异常数
                DegradeRule userServiceErrorCountRule = new DegradeRule("GET:http://user-service/api/users/{userId}")
                                .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT) // 异常数策略
                                .setCount(10) // 异常数阈值
                                .setTimeWindow(10) // 熔断时长（秒）
                                .setMinRequestAmount(5) // 最小请求数
                                .setStatIntervalMs(1000); // 统计时长（毫秒）
                rules.add(userServiceErrorCountRule);

                // 画师信息查询熔断规则
                DegradeRule artistServiceRule = new DegradeRule("GET:http://user-service/api/users/{userId}/artist")
                                .setGrade(RuleConstant.DEGRADE_GRADE_RT)
                                .setCount(1000)
                                .setTimeWindow(10)
                                .setMinRequestAmount(5)
                                .setSlowRatioThreshold(0.5)
                                .setStatIntervalMs(1000);
                rules.add(artistServiceRule);

                // 画师身份验证熔断规则
                DegradeRule isArtistRule = new DegradeRule("GET:http://user-service/api/users/{userId}/is-artist")
                                .setGrade(RuleConstant.DEGRADE_GRADE_RT)
                                .setCount(1000)
                                .setTimeWindow(10)
                                .setMinRequestAmount(5)
                                .setSlowRatioThreshold(0.5)
                                .setStatIntervalMs(1000);
                rules.add(isArtistRule);

                // 文件服务熔断规则
                DegradeRule fileServiceRule = new DegradeRule("POST:http://file-service/api/files/upload")
                                .setGrade(RuleConstant.DEGRADE_GRADE_RT)
                                .setCount(5000) // 文件上传超时时间更长
                                .setTimeWindow(10)
                                .setMinRequestAmount(3)
                                .setSlowRatioThreshold(0.6)
                                .setStatIntervalMs(1000);
                rules.add(fileServiceRule);

                // 加载熔断规则
                DegradeRuleManager.loadRules(rules);
                log.info("已加载 {} 条熔断降级规则", rules.size());
        }

        /**
         * 初始化流控规则（限流）
         */
        private void initFlowRules() {
                List<FlowRule> rules = new ArrayList<>();

                // 用户服务调用限流规则
                FlowRule userServiceFlowRule = new FlowRule("GET:http://user-service/api/users/{userId}")
                                .setGrade(RuleConstant.FLOW_GRADE_QPS) // QPS 限流
                                .setCount(100) // QPS 阈值
                                .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT); // 直接拒绝
                rules.add(userServiceFlowRule);

                // 画师信息查询限流规则
                FlowRule artistServiceFlowRule = new FlowRule("GET:http://user-service/api/users/{userId}/artist")
                                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                                .setCount(100)
                                .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
                rules.add(artistServiceFlowRule);

                // 文件服务调用限流规则
                FlowRule fileServiceFlowRule = new FlowRule("POST:http://file-service/api/files/upload")
                                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                                .setCount(50) // 文件上传限流更严格
                                .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
                rules.add(fileServiceFlowRule);

                // 加载流控规则
                FlowRuleManager.loadRules(rules);
                log.info("已加载 {} 条流控规则", rules.size());
        }
}
