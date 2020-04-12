package com.xiaogj.dataxserver.util;

import com.jayway.jsonpath.JsonPath;
import com.xiaogj.dataxserver.config.JobJsonConfig;
import com.xiaogj.dataxserver.vo.DBInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class DataXJobFile {

    private static String template = null;

    @Autowired
    private JobJsonConfig jobJsonConfig;

    public void generateMs2ChJsonJobFile(DBInfoVO sourceDB, DBInfoVO targetDB, String tableName, List<String> jsonPathList) {
        long timeMillis = System.currentTimeMillis();
        //String jsonPath = new StringBuffer("/usr/local/datax/job/").append(tableName).append("_").append(timeMillis).append(".json").toString();
        String jsonPath = null;
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            jsonPath = new StringBuffer("D:\\Tools\\ETL\\dev\\DataX\\target\\datax-3.0.2\\datax-3.0.2\\job").append(tableName).append("_").append(timeMillis).append(".json").toString();
        } else {
            jsonPath = new StringBuffer("/usr/local/datax/datax-3.0.2/job/").append(tableName).append("_").append(timeMillis).append(".json").toString();
        }
        jsonPathList.add(jsonPath);
        String json = getTemplate();
        //CharSequence cols = getColumnsString(columns);
        //int channels = getChannelNumber(migrationRecords);
        json = json.replace("{job.channel}", "1");


        json = json.replace("{source.db.username}", sourceDB.getUsername());
        json = json.replace("{source.db.password}", sourceDB.getPassword());
        //json = json.replace("{source.db.table.columns}", cols);
        //json = json.replace("{source.db.table.pk}", pk == null ? "" : pk);
        json = json.replace("{source.db.table.name}", "xgj_w1_mallplus.dbo."+tableName);
        json = json.replace("{source.db.jdbcUrl}", sourceDB.getJdbcUrl());
        //json = json.replace("{source.db.type}", getDbType(config.getSourceDbUrl()));
        //json = json.replace("{source.db.type}", "sqlserver");


        json = json.replace("{target.db.username}", targetDB.getUsername());
        json = json.replace("{target.db.password}", targetDB.getPassword());
        //json = json.replace("{target.db.table.columns}", cols);
        json = json.replace("{target.db.table.name}", "perfor."+tableName);
        json = json.replace("{target.db.jdbcUrl}", targetDB.getJdbcUrl());
        //json = json.replace("{target.db.type}", getDbType(config.getTargetDbUrl()));

        //log.info(json);

        log.info("Write ms2chJob for table: {}", tableName);
        write2File(jsonPath, json);
    }

    public static void main(String[] args) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = dateTimeFormat.format(0);
        System.out.println(format);
    }

    public void generateJsonJobFile(String sourceTableName, String targetTableName, List<String> columns, String pk, String whereClause, long migrationRecords) {

        String json = getTemplate();

        CharSequence cols = getColumnsString(columns);
        int channels = getChannelNumber(migrationRecords);

        json = json.replace("{job.channel}", String.valueOf(channels));
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

    private int getChannelNumber(long migrationRecords) {
        int result = 1;
        if ("true".equalsIgnoreCase(jobJsonConfig.getDataxUseMultipleChannel()) && migrationRecords > 0) {
            if (migrationRecords > jobJsonConfig.getDataxUse2ChannelRecordsOver()) {
                result = 2;
            }
            if (migrationRecords > jobJsonConfig.getDataxUse4ChannelRecordsOver()) {
                result = 4;
            }
            if (migrationRecords > jobJsonConfig.getDataxUseNChannelRecordsOver()) {
                result = jobJsonConfig.getDataxUseNChannelNumber();
            }

        }
        return result;
    }

    public String getSourceGlobalTableWhereClause(List<String> columns) {
        String whereCase1 = jobJsonConfig.getGlobalWhereClause();
        String whereCase2 = jobJsonConfig.getGlobalWhere2Clause();
        String result = null;
        if (columns != null && !columns.isEmpty()) {
            if (hasWhereColumn(whereCase1, columns)) {
                result = whereCase1;
            } else if (hasWhereColumn(whereCase2, columns)) {
                result = whereCase2;
            }
        }
        return result;
    }

    /**
     * Pre-condition: the job file has need to be generated.
     *
     * @param tableName String
     * @return String
     */
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
            log.info("Write json to file: {}",  filePath);
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

    private CharSequence getColumnsString(List<String> columns) {
        StringBuffer stb = new StringBuffer();

        for (String s : columns) {
            stb.append("\"");
            stb.append(s);
            stb.append("\",");
        }
        return stb.subSequence(0, stb.length() - 1);
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
