package com.xiaogj.dataxserver;

import com.alibaba.druid.util.StringUtils;
import com.xiaogj.dataxserver.service.IDBInfoService;
import com.xiaogj.dataxserver.service.impl.JobJsonGenerator;
import com.xiaogj.dataxserver.service.impl.MigrationTask;
import com.xiaogj.dataxserver.util.JdbcHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@SpringBootApplication
public class DataxServerApplication implements CommandLineRunner {

    @Autowired
    private JobJsonGenerator jobJsonGenerator;

    @Autowired
    private MigrationTask migrationTask;

    @Autowired
    private JdbcHelper jdbcHelperl;

    @Autowired
    private IDBInfoService idbInfoService;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DataxServerApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }

    @Override
    public void run(String... args) throws Exception {

        log.info(" --- Run DataX !");
//        boolean onlyReportFlag = false;
//        boolean onlyGenerateFlag = false;
//        boolean onlyExecuteJobsFlag = false;
//        if (args != null && args.length >= 1) {
//            log.info("Main Parameters 1:" + args[0]);
//            onlyReportFlag = "report".equalsIgnoreCase(args[0]) || "onlyreport".equalsIgnoreCase(args[0])
//                    || "skip".equalsIgnoreCase(args[0]);
//
//            onlyGenerateFlag = "generate".equalsIgnoreCase(args[0]) || "json".equalsIgnoreCase(args[0]);
//            onlyExecuteJobsFlag = "run".equalsIgnoreCase(args[0]) || "execute".equalsIgnoreCase(args[0])
//                    || "command".equalsIgnoreCase(args[0]);
//        }
//
//        try {
//
//            if (onlyGenerateFlag) {
//                jobJsonGenerator.generate();
//            } else {
//                if (!onlyReportFlag && !onlyExecuteJobsFlag) {
//                    jobJsonGenerator.generate();
//                }
//                migrationTask.execute(onlyReportFlag);
//            }
//
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }
       // runDatax();
        creatTable();
    }

    public void runDatax() {
        try {
            List<String> strings = jobJsonGenerator.batchGenerateForDB();
            System.out.println(System.getProperty("os.name").toLowerCase().contains("windows"));
            System.out.println(System.getProperty("user.dir"));
            String cmd = null;
            String[] cmds = new String[3];
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                cmd = "cmd /c python3 D:\\Tools\\ETL\\dev\\DataX\\target\\datax-3.0.2\\datax-3.0.2\\bin\\datax.py " + strings.get(0);
            } else {
                cmds = new String[]{"/bin/sh", "-c", "python /usr/local/datax/datax-3.0.2/bin/datax.py " + strings.get(0)};
            }
            log.info(cmds.toString());
            Process pr = Runtime.getRuntime().exec(cmds);
            //Process pr = Runtime.getRuntime().exec(windowcmd, null, new File("D:\\Tools\\ETL\\dev\\DataX\\target\\datax-3.0.2\\datax-3.0.2\\bin"));
            BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                log.info(line);
            }
            in.close();
            pr.waitFor();
            log.info("end");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void creatTable() throws SQLException {
        idbInfoService.createTabel();
        log.info("end");
    }
}
