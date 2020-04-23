package com.xiaogj.dataxserver.util;

import com.jayway.jsonpath.JsonPath;
import com.xiaogj.dataxserver.config.JobJsonConfig;
import com.xiaogj.dataxserver.vo.DBInfoVO;
import com.xiaogj.dataxserver.vo.SyncVO;
import com.xiaogj.dataxserver.vo.TableInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

@Slf4j
@Service
public class DataXJobFile {

    private static String template = null;

    @Autowired
    private JobJsonConfig jobJsonConfig;

    public void generateMs2ChJsonJobFile(DBInfoVO sourceDB, DBInfoVO targetDB, TableInfoVO tableInfo, String targetTableName, String jobJsonDir, String format) {
        String jsonPath = null;
        try {
            if (!Files.exists(Paths.get(jobJsonDir.toString()))) {
                Files.createDirectories(Paths.get(jobJsonDir.toString()));
            }
            jsonPath = new StringBuffer(jobJsonDir).append(sourceDB.getName()).append(".").append(tableInfo.getName()).append("_").append(format).append(".json").toString();
        } catch (IOException e) {
            log.error("create job directories fail:  ", e);
        }
        sourceDB.addJsonPathList(jsonPath);
        String json = getTemplate();

        //CharSequence cols = getColumnsString(columns);
        //int channels = getChannelNumber(migrationRecords);
        json = json.replace("{job.channel}", jobJsonConfig.getDataxJobChannel());

        //source db
        json = json.replace("{source.db.jdbcUrl}", sourceDB.getJdbcUrl());
        json = json.replace("{source.db.username}", sourceDB.getUsername());
        json = json.replace("{source.db.password}", sourceDB.getPassword());
        //json = json.replace("{source.db.table.name}", tableInfo.getName());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String startTimeStr = null;
        String endTimeStr = null;

        SyncVO syncVO = tableInfo.getSyncVO();

        Date serviceTimeSync = syncVO.getServiceTimeSync();
        if (serviceTimeSync != null) {
            startTimeStr = sdf.format(serviceTimeSync);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(serviceTimeSync);
            //间隔暂时默认为1天，存在结束日期大于当前日期场景要去处理
            calendar.add(Calendar.DATE, jobJsonConfig.getTimeInterval());
            Calendar calendarNow = Calendar.getInstance();
            calendarNow.set(Calendar.HOUR_OF_DAY, 0);
            calendarNow.set(Calendar.MINUTE, 0);
            calendarNow.set(Calendar.SECOND, 0);
            calendarNow.set(Calendar.MILLISECOND, 0);
            if (calendar.after(calendarNow))
            {
                log.warn("{} synchronization date plus interval days exceeds current date.", tableInfo.getName());
                endTimeStr = sdf.format(calendarNow.getTime());
                syncVO.setEndTime(new Date(calendarNow.getTime().getTime()));
            }else {
                endTimeStr = sdf.format(calendar.getTime());
                syncVO.setEndTime(new Date(calendar.getTime().getTime()));
            }
        } else {
            startTimeStr = "1970-01-01";
            Date baseServiceTime = sourceDB.getBaseServiceTime();
            endTimeStr = baseServiceTime.toString();
            syncVO.setEndTime(baseServiceTime);
        }
        String querySql = tableInfo.getQuerySql().replace("{start.time}", startTimeStr);
        querySql = querySql.replace("{end.time}", endTimeStr);
        json = json.replace("{source.db.querySql}", querySql);

        //target db
        json = json.replace("{target.db.jdbcUrl}", targetDB.getJdbcUrl());
        json = json.replace("{target.db.username}", targetDB.getUsername());
        json = json.replace("{target.db.password}", targetDB.getPassword());
        json = json.replace("{target.db.table.name}", targetTableName);
        json = json.replace("{target.db.table.columns}", tableInfo.getWriteColumn());

        log.info("Write ms2chJob for table: {}", tableInfo.getName());
        write2File(jsonPath, json);
    }

    public void generateJsonJobFile(String sourceTableName, String targetTableName, List<String> columns, String pk, String whereClause, long migrationRecords) {

        String json = getTemplate();

        CharSequence cols = getColumnsString(columns);
        //int channels = getChannelNumber(migrationRecords);

        //json = json.replace("{job.channel}", String.valueOf(channels));
        json = json.replace("{source.db.username}", jobJsonConfig.getSourceDbUsername());
        json = json.replace("{source.db.password}", jobJsonConfig.getSourceDbPassword());
        json = json.replace("{source.db.table.columns}", cols);
        json = json.replace("{source.db.table.pk}", pk == null ? "" : pk);
        json = json.replace("{source.db.table.name}", sourceTableName);
        json = json.replace("{source.db.url}", jobJsonConfig.getSourceDbUrl());
        json = json.replace("{source.db.type}", getDbType(jobJsonConfig.getSourceDbUrl()));

        if (whereClause != null && !"".equals(whereClause)) {
            json = json.replace("{source.db.table.where.clause}",
                    "\"where\": \" " + whereClause + "\",");
        } else {
            json = json.replace("{source.db.table.where.clause}\n                        ", "");
        }

        json = json.replace("{target.db.username}", jobJsonConfig.getTargetDbUsername());
        json = json.replace("{target.db.password}", jobJsonConfig.getTargetDbPassword());
        json = json.replace("{target.db.table.columns}", cols);
        json = json.replace("{target.db.table.name}", targetTableName);
        json = json.replace("{target.db.url}", jobJsonConfig.getTargetDbUrl());
        json = json.replace("{target.db.type}", getDbType(jobJsonConfig.getTargetDbUrl()));

        //log.info(json);

        try {
            log.info("Write job json for table:" + sourceTableName);
            writeToFile(sourceTableName, json);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private static String getDbType(String dbUrl) {
        String dbType = null;
        if (dbUrl != null) {
            String url = dbUrl.replaceFirst("jdbc:", "");
            url = url.replaceFirst("microsoft:", "");
            dbType = url.substring(0, url.indexOf(":"));
            if (dbType.indexOf("-") > 0) {
                dbType = dbType.substring(0, dbType.indexOf("-"));
            }
        }
        return dbType;
    }

    public String getJobFileWhereClause(String tableName) {
        String jsonContent = this.ReadFile(jobJsonConfig.getDataxToolFolder() + "/job/" + tableName + ".json");
        String value = null;
        try {
            value = JsonPath.read(jsonContent, "$.job.content[0].reader.parameter.where");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return value;

    }

    private boolean hasWhereColumn(String whereCase, List<String> columns) {
        String whereCol = null;

        if (!StringUtils.isEmpty(whereCase)) {
            whereCase = whereCase.replace(" ", "");
            whereCase = whereCase.replace("\"", "");
            whereCase = whereCase.replace("in", "=");
            String[] temp = whereCase.split("=");
            if (temp != null && temp.length > 1) {
                whereCol = temp[0];
            }
        }

        boolean result = false;
        if (whereCol != null && columns != null && columns.size() > 0) {
            for (String column : columns) {
                if (column != null && column.equalsIgnoreCase(whereCol)) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    private void write2File(String filePath, String json) {
        try {
            Path path = Files.createFile(Paths.get(filePath));
            BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
            writer.write(json);
            writer.flush();
            writer.close();
            log.info("Write json to file: {}", filePath);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void writeToFile(String fileName, String json) throws IOException {
        File file = new File(jobJsonConfig.getDataxToolFolder() + "/job/" + fileName + ".json");

        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write(json);
        out.close();
        log.info("Write json to file:" + file.getAbsolutePath());
    }

    private String getColumnsString(List<String> columns) {
        StringBuffer stb = new StringBuffer();
        for (String s : columns) {
            stb.append("\"");
            stb.append(s);
            stb.append("\",");
        }
        return stb.subSequence(0, stb.length() - 1).toString();
    }

    private String getTemplate() {
        if (template == null) {
            StringBuffer stb = new StringBuffer();
            try {
                readToBuffer(stb, "/job/ms2chJob.json");
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            template = stb.toString();
            //log.info(template);
        }

        return template;
    }

    private void readToBuffer(StringBuffer buffer, String filePath) throws IOException {
        //InputStream is = DataXJobFile.class.getClassLoader().getResourceAsStream(filePath);
        InputStream inputStream = new ClassPathResource(filePath).getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        line = reader.readLine();
        while (line != null) {
            buffer.append(line);
            buffer.append("\n");
            line = reader.readLine();
        }
        reader.close();
    }

    private String ReadFile(String path) {
        File file = new File(path);
        BufferedReader reader = null;
        String result = "";
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                result = result + tempString;
            }
            reader.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return result;
    }

}
