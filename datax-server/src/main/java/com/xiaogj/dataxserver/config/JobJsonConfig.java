package com.xiaogj.dataxserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:config.properties")
public class JobJsonConfig {

    @Value("${default.db.name}")
    private String defaultDbName;

    @Value("${default.db.url}")
    private String defaultDbUrl;

    @Value("${default.db.username}")
    private String defaultDbUsername;

    @Value("${default.db.password}")
    private String defaultDbPassword;

    @Value("${source.db.name}")
    private String sourceDbName;

    @Value("${source.db.url}")
    private String sourceDbUrl;

    @Value("${source.db.username}")
    private String sourceDbUsername;

    @Value("${source.db.password}")
    private String sourceDbPassword;

    @Value("${target.db.name}")
    private String targetDbName;

    @Value("${target.db.url}")
    private String targetDbUrl;

    @Value("${target.db.username}")
    private String targetDbUsername;

    @Value("${target.db.password}")
    private String targetDbPassword;
    @Value("${online.target.db.name}")
    private String onlineTargetDbName;

    @Value("${online.target.db.url}")
    private String onlineTargetDbUrl;

    @Value("${online.target.db.username}")
    private String onlineTargetDbUsername;

    @Value("${online.target.db.password}")
    private String onlineTargetDbPassword;

    @Value("${migration.datax.tool.folder}")
    private String dataxToolFolder;

    @Value("${migration.time.interval}")
    private int timeInterval;

    @Value("${migration.json.del}")
    private boolean jobJsonDel;

    @Value("${migration.temp.table.del}")
    private boolean tempTableDel;

    @Value("${clickhouse.optimize.times}")
    private int optimizeTimes;

    @Value("${datax.job.channel}")
    private String dataxJobChannel;

    public String getDataxJobChannel() {
        return dataxJobChannel;
    }

    public void setDataxJobChannel(String dataxJobChannel) {
        this.dataxJobChannel = dataxJobChannel;
    }

    public int getOptimizeTimes() {
        return optimizeTimes;
    }

    public void setOptimizeTimes(int optimizeTimes) {
        this.optimizeTimes = optimizeTimes;
    }

    public boolean isTempTableDel() {
        return tempTableDel;
    }

    public void setTempTableDel(boolean tempTableDel) {
        this.tempTableDel = tempTableDel;
    }

    public boolean isJobJsonDel() {
        return jobJsonDel;
    }

    public void setJobJsonDel(boolean jobJsonDel) {
        this.jobJsonDel = jobJsonDel;
    }

    public int getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(int timeInterval) {
        this.timeInterval = timeInterval;
    }

    public String getOnlineTargetDbName() {
        return onlineTargetDbName;
    }

    public void setOnlineTargetDbName(String onlineTargetDbName) {
        this.onlineTargetDbName = onlineTargetDbName;
    }

    public String getOnlineTargetDbUrl() {
        return onlineTargetDbUrl;
    }

    public void setOnlineTargetDbUrl(String onlineTargetDbUrl) {
        this.onlineTargetDbUrl = onlineTargetDbUrl;
    }

    public String getOnlineTargetDbUsername() {
        return onlineTargetDbUsername;
    }

    public void setOnlineTargetDbUsername(String onlineTargetDbUsername) {
        this.onlineTargetDbUsername = onlineTargetDbUsername;
    }

    public String getOnlineTargetDbPassword() {
        return onlineTargetDbPassword;
    }

    public void setOnlineTargetDbPassword(String onlineTargetDbPassword) {
        this.onlineTargetDbPassword = onlineTargetDbPassword;
    }

    public String getDefaultDbName() {
        return defaultDbName;
    }

    public void setDefaultDbName(String defaultDbName) {
        this.defaultDbName = defaultDbName;
    }

    public String getDefaultDbUrl() {
        return defaultDbUrl;
    }

    public void setDefaultDbUrl(String defaultDbUrl) {
        this.defaultDbUrl = defaultDbUrl;
    }

    public String getDefaultDbUsername() {
        return defaultDbUsername;
    }

    public void setDefaultDbUsername(String defaultDbUsername) {
        this.defaultDbUsername = defaultDbUsername;
    }

    public String getDefaultDbPassword() {
        return defaultDbPassword;
    }

    public void setDefaultDbPassword(String defaultDbPassword) {
        this.defaultDbPassword = defaultDbPassword;
    }

    public String getSourceDbName() {
        return sourceDbName;
    }

    public void setSourceDbName(String sourceDbName) {
        this.sourceDbName = sourceDbName;
    }

    public String getSourceDbUrl() {
        return sourceDbUrl;
    }

    public void setSourceDbUrl(String sourceDbUrl) {
        this.sourceDbUrl = sourceDbUrl;
    }

    public String getSourceDbUsername() {
        return sourceDbUsername;
    }

    public void setSourceDbUsername(String sourceDbUsername) {
        this.sourceDbUsername = sourceDbUsername;
    }

    public String getSourceDbPassword() {
        return sourceDbPassword;
    }

    public void setSourceDbPassword(String sourceDbPassword) {
        this.sourceDbPassword = sourceDbPassword;
    }

    public String getTargetDbName() {
        return targetDbName;
    }

    public void setTargetDbName(String targetDbName) {
        this.targetDbName = targetDbName;
    }

    public String getTargetDbUrl() {
        return targetDbUrl;
    }

    public void setTargetDbUrl(String targetDbUrl) {
        this.targetDbUrl = targetDbUrl;
    }

    public String getTargetDbUsername() {
        return targetDbUsername;
    }

    public void setTargetDbUsername(String targetDbUsername) {
        this.targetDbUsername = targetDbUsername;
    }

    public String getTargetDbPassword() {
        return targetDbPassword;
    }

    public void setTargetDbPassword(String targetDbPassword) {
        this.targetDbPassword = targetDbPassword;
    }

    public String getDataxToolFolder() {
        return dataxToolFolder;
    }

    public void setDataxToolFolder(String dataxToolFolder) {
        this.dataxToolFolder = dataxToolFolder;
    }

}
