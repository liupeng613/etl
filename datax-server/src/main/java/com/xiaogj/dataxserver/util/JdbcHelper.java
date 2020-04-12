package com.xiaogj.dataxserver.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.xiaogj.dataxserver.config.JobJsonConfig;
import com.xiaogj.dataxserver.vo.DBInfoVO;
import lombok.extern.slf4j.Slf4j;
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

    private static DruidDataSource dataSource = null;
    public static ThreadLocal<Connection> container = new ThreadLocal<Connection>();


	private static DruidDataSource sourceDataSource = null;
	public static ThreadLocal<Connection> sourceContainer = new ThreadLocal<Connection>();


	private static DruidDataSource targetDataSource = null;
	public static ThreadLocal<Connection> targetContainer = new ThreadLocal<Connection>();

    private DruidDataSource initDataSource(String jdbcUrl, String username, String password) {
        dataSource = new DruidDataSource();
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(username);// 用户名
        dataSource.setPassword(password);// 密码
        dataSource.setInitialSize(2);
        dataSource.setMaxActive(20);
        dataSource.setMinIdle(0);
        dataSource.setMaxWait(60000);
        //targetds.setValidationQuery("SELECT 1");
        dataSource.setTestOnBorrow(false);
        dataSource.setTestWhileIdle(true);
        dataSource.setPoolPreparedStatements(false);

		targetDataSource = new DruidDataSource();
		targetDataSource.setUrl(jdbcUrl);
		targetDataSource.setUsername(username);// 用户名
		targetDataSource.setPassword(password);// 密码
		targetDataSource.setInitialSize(2);
		targetDataSource.setMaxActive(20);
		targetDataSource.setMinIdle(0);
		targetDataSource.setMaxWait(60000);
		//targetds.setValidationQuery("SELECT 1");
		targetDataSource.setTestOnBorrow(false);
		targetDataSource.setTestWhileIdle(true);
		targetDataSource.setPoolPreparedStatements(false);

        return dataSource;
    }

    /**
     * 获取数据连接
     *
     * @return
     */
    public Connection getDefaultConnection() {
        Connection conn = container.get();
        try {
            if (dataSource == null) {
                initDataSource(config.getSourceDbUrl(), config.getSourceDbUsername(), config.getSourceDbPassword());
            }
            conn = dataSource.getConnection();
            log.info(Thread.currentThread().getName() + " Source Connection started......");
            container.set(conn);
        } catch (Exception e) {
            log.info(Thread.currentThread().getName() + " Get Source connection failed!");
            e.printStackTrace();
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
            Connection conn = container.get();
            if (conn != null) {
                conn.close();
                log.info(Thread.currentThread().getName() + " Source Connection closed.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            try {
                container.remove();// 从当前线程移除连接切记
            } catch (Exception e2) {
                e2.printStackTrace();
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
				initDataSource(dbInfoVO.getJdbcUrl(), dbInfoVO.getUsername(), dbInfoVO.getPassword());
			}
			conn = sourceDataSource.getConnection();
			log.info(Thread.currentThread().getName() + " Source Connection started......");
			sourceContainer.set(conn);
		} catch (Exception e) {
			log.info(Thread.currentThread().getName() + " Get Source connection failed!");
			e.printStackTrace();
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
				log.info(Thread.currentThread().getName() + " Source Connection closed.");
			}
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			try {
				sourceContainer.remove();// 从当前线程移除连接切记
			} catch (Exception e2) {
				e2.printStackTrace();
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
				initDataSource(dbInfoVO.getJdbcUrl(), dbInfoVO.getUsername(), dbInfoVO.getPassword());
			}
			conn = targetDataSource.getConnection();
			log.info(Thread.currentThread().getName() + " Source Connection started......");
			targetContainer.set(conn);
		} catch (Exception e) {
			log.info(Thread.currentThread().getName() + " Get Source connection failed!");
			e.printStackTrace();
		}
		return conn;
	}

	public Connection getTargetConnection() {
		Connection conn = targetContainer.get();
		try {
			if (targetDataSource == null) {
				initDataSource(config.getTargetDbUrl(), config.getTargetDbUsername(), config.getTargetDbPassword());
			}
			conn = targetDataSource.getConnection();
			log.info(Thread.currentThread().getName() + " Source Connection started......");
			targetContainer.set(conn);
		} catch (Exception e) {
			log.info(Thread.currentThread().getName() + " Get Source connection failed!");
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
				log.info(Thread.currentThread().getName() + " Source Connection closed.");
			}
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			try {
				targetContainer.remove();// 从当前线程移除连接切记
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

    /**
     * 获取数据连接
     * initDataSource方法需要返回新对象才可用
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