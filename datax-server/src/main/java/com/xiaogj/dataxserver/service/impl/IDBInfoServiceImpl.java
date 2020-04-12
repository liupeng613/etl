package com.xiaogj.dataxserver.service.impl;

import com.xiaogj.dataxserver.config.JobJsonConfig;
import com.xiaogj.dataxserver.service.IDBInfoService;
import com.xiaogj.dataxserver.util.JdbcHelper;
import com.xiaogj.dataxserver.vo.DBInfoVO;
import com.xiaogj.dataxserver.vo.DataTable;
import com.xiaogj.dataxserver.vo.DataTableStatus;
import com.xiaogj.dataxserver.vo.TableInfoVO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class IDBInfoServiceImpl implements IDBInfoService {
    protected static final Log log = LogFactory.getLog(IDBInfoServiceImpl.class);

    @Autowired
    private JdbcHelper jdbcHelper;

    @Autowired
    private JobJsonConfig jobJsonConfig;

//    @Override
//    public List<DataTable> getTargetTransfterTables() throws SQLException {
//        String sql = jobJsonConfig.getMigrationQueryTargetTablesSql();
//        ResultSet rs = null;
//        List<DataTable> result = null;
//        try {
//            rs = jdbcHelper.getTargetConnection().prepareStatement(sql).executeQuery();
//
//            if (rs != null) {
//                result = new ArrayList<DataTable>();
//                while (rs.next()) {
//                    DataTable ta = new DataTable();
//                    ta.setName(rs.getString(1));
//                    result.add(ta);
//                }
//            }
//        } catch (SQLException e) {
//            log.error(e.getMessage(), e);
//            throw e;
//        }
//        jdbcHelper.closeTargetConnection();
//        return result;
//    }

//    @Override
//    public List<String> getTargetTransfterTableColumns(String tableName) throws SQLException {
//        String sql = jobJsonConfig.getMigrationQueryTargetTableColumnsSql();
//        sql = sql.replace("{0}", tableName);
//        List<String> result = null;
//
//        try {
//            ResultSet rs = jdbcHelper.getTargetConnection().prepareStatement(sql).executeQuery();
//            if (rs != null) {
//                result = new ArrayList<String>();
//                while (rs.next()) {
//                    result.add(rs.getString(1));
//                }
//            }
//        } catch (SQLException e) {
//            log.error(e.getMessage(), e);
//            throw e;
//        }
//        jdbcHelper.closeTargetConnection();
//        return result;
//    }

//根据目标数据连接获取
//    @Override
//    public List<String> getTargetTransfterTablePrimaryKey(String tableName) throws SQLException {
//        String sql = jobJsonConfig.getMigrationQueryTargetTablePrimaryKeysSql();
//        sql = sql.replace("{0}", tableName);
//        List<String> result = null;
//        try {
//            ResultSet rs = jdbcHelper.getTargetConnection().prepareStatement(sql).executeQuery();
//
//            if (rs != null) {
//                result = new ArrayList<String>();
//                while (rs.next()) {
//                    result.add(rs.getString(1));
//                }
//            }
//        } catch (SQLException e) {
//            log.error(e.getMessage(), e);
//            throw e;
//        }
//        jdbcHelper.closeTargetConnection();
//        return result;
//    }



    @Override
    public List<DataTableStatus> getSourceTransfterTablesStatus() throws SQLException {
        String sql = jobJsonConfig.getMigrationQuerySourceTablesStatusSql();

        List<DataTableStatus> result = null;

        try {
            ResultSet rs = jdbcHelper.getDefaultConnection().prepareStatement(sql).executeQuery();
            if (rs != null) {
                result = new ArrayList<DataTableStatus>();
                while (rs.next()) {
                    DataTableStatus ta = new DataTableStatus();
                    ta.setName(rs.getString(1));
                    ta.setSize(rs.getFloat(2));
                    ta.setCount(rs.getLong(3));
                    result.add(ta);
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        jdbcHelper.closeDefaultConnection();
        return result;
    }


    @Override
    public long getSourceTransfterTableMigrationCount(String tableName, String whereClause) throws SQLException {
        StringBuffer sql = new StringBuffer();
        sql.append("select count(*) from " + tableName + "  ");
        if (whereClause != null && !"".equals(whereClause)) {
            whereClause = whereClause.replace("\"", "");
            sql.append(" where " + whereClause);
        }
        long result = 0;

        try {
            ResultSet rs = jdbcHelper.getDefaultConnection().prepareStatement(sql.toString()).executeQuery();
            if (rs != null) {
                rs.next();
                result = rs.getLong(1);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        jdbcHelper.closeDefaultConnection();
        return result;
    }

//    @Override
//    public long getTargetTransfterTableMigrationFinishedCount(String tableName, String whereClause) throws SQLException {
//        StringBuffer sql = new StringBuffer();
//        sql.append("select count(*) from " + tableName + "  ");
//        if (whereClause != null && !"".equals(whereClause)) {
//            whereClause = whereClause.replace("\"", "");
//            sql.append(" where " + whereClause);
//        }
//        long result = 0;
//
//        try {
//            ResultSet rs = jdbcHelper.getTargetConnection().prepareStatement(sql.toString()).executeQuery();
//            if (rs != null) {
//                rs.next();
//                result = rs.getLong(1);
//            }
//        } catch (SQLException e) {
//            log.error(e.getMessage(), e);
//            throw e;
//        }
//        jdbcHelper.closeTargetConnection();
//        return result;
//    }

    @Override
    public List<DBInfoVO> getDBInfoList() {
        //StringBuffer sql = new StringBuffer("select * from db_info");此处从主数据库获取所有数据库属性
        List<DBInfoVO> dbInfoVOList = new ArrayList<>();
        DBInfoVO vo = new DBInfoVO();
        vo.setJdbcUrl("jdbc:sqlserver://local054.xiaogj.com:5291;DatabaseName=xgj_w1_mallplus");
        vo.setUsername("devp_mall");
        vo.setPassword("xiaogj2020");
        vo.setQuerySql("select * from ");
        dbInfoVOList.add(vo);

        //如果mysql db——info 中没有，则从最小一天开始同步（每次同步获取uodatetime，createtime更新）
        return dbInfoVOList;
    }

    @Override
    public List<TableInfoVO> getTableInfoList() {
        //StringBuffer sql = new StringBuffer("select * from table_info");此处从主数据库获取所有表属性
        List<TableInfoVO> tableInfoList = new ArrayList<>();
        TableInfoVO vo = new TableInfoVO();
        vo.setName("tClass");
        vo.setOrder(1);
        tableInfoList.add(vo);
        return tableInfoList;
    }

    @Override
    public void createTabel() throws SQLException {
        String sql = "CREATE TABLE `tClass13`(\n" +
                "    cID     String ,\n" +
                "    cCreateTime date  ,\n" +
                "    cUpdateTime date  ,\n" +
                "    cCompanyID  String ,\n" +
                "    cShiftID String ,\n" +
                "    cCampusID String ,\n" +
                "    cName String ,\n" +
                "    cPinyinPre String ,\n" +
                "    cMaxStudentsAmount Int64  ,\n" +
                "    cOpenDate datetime  ,\n" +
                "    cIsFinished Int64 ,\n" +
                "    cFinishedDate datetime ,\n" +
                "    cHeadMasterUserID String ,\n" +
                "    cClassroomID String ,\n" +
                "    cOrder Int64 ,\n" +
                "    cDescribe String ,\n" +
                "    cStatus Int64 ,\n" +
                "    cType Int64 ,\n" +
                "    cTeacherSalaryType String ,\n" +
                "    cClassMasterSalaryType String ,\n" +
                "    cSalePersonSalaryType String ,\n" +
                "    cYear Int64 ,\n" +
                "    cTerm String,\n" +
                "    cCloseDate datetime ,\n" +
                "    cCourseTimes decimal(8,2) ,\n" +
                "    cShiftScheduleID String ,\n" +
                "    cLastClasstime String ,\n" +
                "    cShiftAmount Int64 ,\n" +
                "    cSyncTime datetime ,\n" +
                "    cValidAttendanceMinutes Int64 ,\n" +
                "    cTag String ,\n" +
                "    cIsSkipHoliday Int64 ,\n" +
                "    cIsSendCourseMsg Int64 ,\n" +
                "    cGroupStatus Int64 ,\n" +
                "    cGroupID Int64 ,\n" +
                "    cDepartID String ,\n" +
                "    cIsVisualize Int64,\n" +
                "    cFeeEffectiveType Int64 ,\n" +
                "    cFeeEffectiveValue String ,\n" +
                "    cMinStudentsAmount Int64,\n" +
                "    cClassOpenStatus Int64 ,\n" +
                "    cAttendClassSchedule Int64 ,\n" +
                "    cIsLimitAgeRange Int64 ,\n" +
                "    cMinBirthDate datetime  ,\n" +
                "    cMaxBirthDate datetime\n" +
                ")\n" +
                "ENGINE = ReplicatedMergeTree('/clickhouse/cxiaogj1/tables/class13/01', '01', cCreateTime, (cCompanyID, cCreateTime), 8192)  ;";
        jdbcHelper.getTargetConnection().prepareStatement(sql).executeQuery();
    }

    @Override
    public List<DataTable> getTargetTransfterTables() throws SQLException {
        return null;
    }

    @Override
    public List<String> getTargetTransfterTableColumns(String tableName) throws SQLException {
        return null;
    }

    @Override
    public List<String> getTargetTransfterTablePrimaryKey(String tableName) throws SQLException {
        return null;
    }

    @Override
    public long getTargetTransfterTableMigrationFinishedCount(String tableName, String whereClause) throws SQLException {
        return 0;
    }
}
