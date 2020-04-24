package com.xiaogj.dataxserver.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.xiaogj.dataxserver.config.JobJsonConfig;
import com.xiaogj.dataxserver.vo.DBInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * JDBC封装类
 */
@Slf4j
@Service
public class JdbcHelper {

    @Autowired
    private JobJsonConfig config;

    private static DruidDataSource defaultDataSource = null;
    public static ThreadLocal<Connection> defaultContainer = new ThreadLocal<Connection>();


    private static DruidDataSource sourceDataSource = null;
    public static ThreadLocal<Connection> sourceContainer = new ThreadLocal<Connection>();


    private static DruidDataSource targetDataSource = null;
    public static ThreadLocal<Connection> targetContainer = new ThreadLocal<Connection>();

    private static DruidDataSource onlineDataSource = null;
    public static ThreadLocal<Connection> onlineContainer = new ThreadLocal<Connection>();

    private DruidDataSource initDataSource(String jdbcUrl, String username, String password) {
        DruidDataSource ds = new DruidDataSource();
        ds.setUrl(jdbcUrl);
        ds.setUsername(username);// 用户名
        ds.setPassword(password);// 密码
        ds.setInitialSize(2);
        ds.setMaxActive(40);
        ds.setMinIdle(0);
        ds.setMaxWait(60000);
        if (StringUtils.contains(jdbcUrl,"9000")) {
            ds.setDriverClassName("com.github.housepower.jdbc.ClickHouseDriver");
        }else{
            ds.setQueryTimeout(120);
            ds.setTestOnBorrow(true);
            //ds.setValidationQuery("SELECT 1");
        }
        ds.setTestWhileIdle(false);
        ds.setPoolPreparedStatements(false);

        return ds;
    }

    /**
     * 获取数据连接
     *
     * @return
     */
    public Connection getDefaultConnection() {
        Connection conn = defaultContainer.get();
        try {
            if (defaultDataSource == null) {
                defaultDataSource = initDataSource(config.getDefaultDbUrl(), config.getDefaultDbUsername(), config.getDefaultDbPassword());
            }
            conn = defaultDataSource.getConnection();
            log.debug("{} Default Connection started......", Thread.currentThread().getName());
            defaultContainer.set(conn);
        } catch (Exception e) {
            log.error("{} Get default connection failed: ", Thread.currentThread().getName(), e);
        }
        return conn;
    }

    /**
     * 关闭数据连接
     *
     * @return
     */
    public void closeDefaultConnection() {
        try {
            Connection conn = defaultContainer.get();
            if (conn != null) {
                conn.close();
                log.debug("{} Source Connection closed.", Thread.currentThread().getName());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            try {
                defaultContainer.remove();// 从当前线程移除连接切记
            } catch (Exception e2) {
                log.error("closeDefaultConnection fail: ", e2);
            }
        }
    }

    /**
     * 获取数据连接
     *
     * @return
     */
    public Connection getSourceConnection(DBInfoVO dbInfoVO) {
        Connection conn = sourceContainer.get();
        try {
            if (sourceDataSource == null) {
                sourceDataSource = initDataSource(dbInfoVO.getJdbcUrl(), dbInfoVO.getUsername(), dbInfoVO.getPassword());
            }
            conn = sourceDataSource.getConnection();
            log.debug("{} Source Connection started......", Thread.currentThread().getName());
            sourceContainer.set(conn);
        } catch (Exception e) {
            log.error("{} Get Source connection faile: ", Thread.currentThread().getName(), e);
        }
        return conn;
    }

    /**
     * 关闭数据连接
     *
     * @return
     */
    public void closeSourceConnection() {
        try {
            Connection conn = sourceContainer.get();
            if (conn != null) {
                conn.close();
                log.debug("{} Source Connection closed.", Thread.currentThread().getName());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            try {
                sourceContainer.remove();// 从当前线程移除连接切记
            } catch (Exception e2) {
                log.error("closeSourceConnection fail: ", e2);
            }
        }
    }

    /**
     * 获取数据连接
     *
     * @return
     */
    public Connection getTargetConnection(DBInfoVO dbInfoVO) {
        Connection conn = targetContainer.get();
        try {
            if (targetDataSource == null) {
                targetDataSource = initDataSource(dbInfoVO.getJdbcUrl(), dbInfoVO.getUsername(), dbInfoVO.getPassword());
            }
            conn = targetDataSource.getConnection();
            log.debug("{} Target Connection started......", Thread.currentThread().getName());
            targetContainer.set(conn);
        } catch (Exception e) {
            log.error("{} Get target connection fail: ", Thread.currentThread().getName(), e);
        }
        return conn;
    }

    public Connection getTargetConnection() {
        Connection conn = targetContainer.get();
        try {
            if (targetDataSource == null) {
                targetDataSource = initDataSource(config.getTargetDbUrl(), config.getTargetDbUsername(), config.getTargetDbPassword());
            }
            conn = targetDataSource.getConnection();
            log.debug("{} Target Connection started......", Thread.currentThread().getName());
            targetContainer.set(conn);
        } catch (Exception e) {
            log.error("{} Get target connection fail: ", Thread.currentThread().getName(), e);
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * 关闭数据连接
     *
     * @return
     */
    public void closeTargetConnection() {
        try {
            Connection conn = targetContainer.get();
            if (conn != null) {
                conn.close();
                log.debug("{} Target Connection closed.", Thread.currentThread().getName());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            try {
                targetContainer.remove();// 从当前线程移除连接切记
            } catch (Exception e2) {
                log.error("closeTargetConnection fail: ", e2);
            }
        }
    }

    /**
     * 获取数据连接
     *
     * @return
     */
    public Connection getOnlineConnection() {
        Connection conn = onlineContainer.get();
        try {
            if (onlineDataSource == null) {
                onlineDataSource = initDataSource(config.getOnlineTargetDbUrl(), config.getOnlineTargetDbUsername(), config.getOnlineTargetDbPassword());
            }
            conn = onlineDataSource.getConnection();
            log.debug("{} Online Connection started......", Thread.currentThread().getName());
            onlineContainer.set(conn);
        } catch (Exception e) {
            log.error("{} Get online connection fail: ", Thread.currentThread().getName(), e);
        }
        return conn;
    }

    /**
     * 关闭数据连接
     *
     * @return
     */
    public void closeOnlineConnection() {
        try {
            Connection conn = onlineContainer.get();
            if (conn != null) {
                conn.close();
                log.debug("{} Online Connection closed.", Thread.currentThread().getName());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            try {
                onlineContainer.remove();// 从当前线程移除连接切记
            } catch (Exception e2) {
                log.error("closeOnlineConnection fail: ", e2);
            }
        }
    }

    /**
     * 获取数据连接
     * initDataSource方法需要返回新对象才可用
     *
     * @return
     */
    public Connection getConnection(String jdbcUrl, String username, String password) {
        DruidDataSource druidDataSource = initDataSource(jdbcUrl, username, password);
        Connection conn = null;
        try {
            conn = druidDataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * 关闭数据连接
     *
     * @return
     */
    public void closeConnection(ResultSet rs, Statement st, Connection conn) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (st != null) {
                st.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}