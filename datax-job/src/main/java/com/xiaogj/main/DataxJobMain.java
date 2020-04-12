package com.xiaogj.main;

import com.xiaogj.service.impl.JobJsonGenerator;
import com.xiaogj.service.impl.MigrationTask;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.GenericXmlApplicationContext;

public class DataxJobMain {

    protected static final Log log = LogFactory.getLog(DataxJobMain.class);

    public static void main(String[] args) {

        log.info(" --- DataX-Migration 1.0, From Yunxuetang(www.yxt.com), Based on Alibaba DataX !");
        log.info(" --- Copyright (C) 2010-2016, Yuexuetang Group. All Rights Reserved.");

        boolean onlyReportFlag = false;
        boolean onlyGenerateFlag = false;
        boolean onlyExecuteJobsFlag = false;
        if (args != null && args.length >= 1) {
            log.info("Main Parameters 1:" + args[0]);
            onlyReportFlag = "report".equalsIgnoreCase(args[0]) || "onlyreport".equalsIgnoreCase(args[0])
                    || "skip".equalsIgnoreCase(args[0]);

            onlyGenerateFlag = "generate".equalsIgnoreCase(args[0]) || "json".equalsIgnoreCase(args[0]);
            onlyExecuteJobsFlag = "run".equalsIgnoreCase(args[0]) || "execute".equalsIgnoreCase(args[0])
                    || "command".equalsIgnoreCase(args[0]);
        }

        GenericXmlApplicationContext context = new GenericXmlApplicationContext();
        context.setValidating(false);
        context.load("classpath*:applicationContext.xml");
        context.refresh();

        JobJsonGenerator gen = context.getBean(JobJsonGenerator.class);
        MigrationTask migration = context.getBean(MigrationTask.class);

        try {

            if (onlyGenerateFlag) {
                gen.generate();
            } else {
                if (!onlyReportFlag && !onlyExecuteJobsFlag) {
                    gen.generate();
                }
                migration.execute(onlyReportFlag);
            }

            exit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            context.close();
        }

    }

    private static void exit() {
        public class TestDatax {
            public static void main(String[] args) {
                try {
                    WebLogs.info("start");

                    String windowcmd = "cmd /c python datax.py D:\\Software\\install\\Environment\\DataX\\datax\\job\\mysql2mysql.json";
                    WebLogs.info(windowcmd);
                    //.exec("你的命令",null,new File("datax安装路径"));
                    Process pr = Runtime.getRuntime().exec(windowcmd,null,new File("D:\\Software\\install\\Environment\\DataX\\datax\\bin"));
                    BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                    String line = null;
                    while ((line = in.readLine()) != null) {
                        WebLogs.info(line);
                    }
                    in.close();
                    pr.waitFor();
                    WebLogs.info("end");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
