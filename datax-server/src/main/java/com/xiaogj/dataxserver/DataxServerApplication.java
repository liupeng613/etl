package com.xiaogj.dataxserver;

import com.xiaogj.dataxserver.config.JobJsonConfig;
import com.xiaogj.dataxserver.service.IDBInfoService;
import com.xiaogj.dataxserver.service.ISyncDataService;
import com.xiaogj.dataxserver.util.JdbcHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.SQLException;

@Slf4j
@SpringBootApplication
public class DataxServerApplication implements CommandLineRunner {

    @Autowired
    private ISyncDataService syncDataService;

    @Autowired
    private JdbcHelper jdbcHelperl;

    @Autowired
    private IDBInfoService idbInfoService;

    @Autowired
    private JobJsonConfig jobJsonConfig;

    @Autowired
    private JdbcHelper jdbcHelper;

    @Autowired
    private IDBInfoService dbInfoService;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DataxServerApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }

    @Override
    public void run(String... args) throws SQLException {
        syncData();
    }

    public void syncData() {
        try {
            long start = System.currentTimeMillis();
            String dbid = System.getProperty("dbid");
            if (dbid != null && !dbid.equals("")) {
                Integer sourcedbId = Integer.valueOf(dbid);
                syncDataService.syncData(sourcedbId);
            } else {
                log.warn("请选择数据库ID参数: java -Ddbid=xxx -jar ......");
            }
            long runTime = (System.currentTimeMillis() - start) / 1000;
            log.info("all end.......runtime ==>> {} s", runTime);
        } catch (Exception e) {
            log.error("syncData fail: ", e);
        }
    }
}
