package com.xiaogj.dataxserver.util;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;

import java.io.IOException;

/**
 * @ClassName: RemoteExecuteCommand
 * @Description: 远程执行Linux命令
 * @date: 2017年10月9日 下午5:44:42
 */
public class RemoteLinuxUtil {
    // 字符编码默认是utf-8
    private static String DEFAULTCHART = "UTF-8";
    private Connection conn;
    private String ip;
    private String userName;
    private String userPwd;

    public RemoteLinuxUtil(String ip, String userName, String userPwd) {
        this.ip = ip;
        this.userName = userName;
        this.userPwd = userPwd;
    }


    public RemoteLinuxUtil() {

    }

    /**
     * 远程登录linux主机
     *
     * @return 登录成功返回true，否则返回false
     * @throws Exception
     * @since V0.1
     */
    public Boolean login() throws Exception {
        boolean flg = false;
        try {
            conn = new Connection(ip);
            // 连接
            conn.connect();
            // 认证
            flg = conn.authenticateWithPassword(userName, userPwd);
        } catch (IOException e) {
            throw new Exception("远程连接服务器失败", e);
        }
        return flg;
    }

    /**
     * 远程执行shll脚本或者命令
     *
     * @param cmd 即将执行的命令
     * @return 命令执行完后返回的结果值
     * @throws Exception
     * @since V0.1
     */
    public String execute(String cmd) throws Exception {
        String result = "";
        Session session = null;
        try {
            if (login()) {
                // 打开一个会话
                session = conn.openSession();
                // 执行命令
                session.execCommand(cmd);
//                result = processStdout(session.getStdout(), DEFAULTCHART);
//                // 如果为输出为空，说明脚本执行出错了
//                if (StringUtils.isBlank(result)) {
//                    result = processStdout(session.getStderr(), DEFAULTCHART);
//                }
                conn.close();
                session.close();
            }
        } catch (IOException e) {
            throw new Exception("命令执行失败", e);
        } finally {
            if (conn != null) {
                conn.close();
            }
            if (session != null) {
                session.close();
            }
        }
        return result;
    }

    /**
     * 解析脚本执行返回的结果集
     *
     * @return 以纯文本的格式返回
     * @throws Exception
     * @since V0.1
     */
//    private String processStdout(InputStream in, String charset) throws Exception {
//        InputStream stdout = new StreamGobbler(in);
//        StringBuffer buffer = new StringBuffer();
//        InputStreamReader isr = null;
//        BufferedReader br = null;
//        try {
//            isr = new InputStreamReader(stdout, charset);
//            br = new BufferedReader(isr);
//            String line = null;
//            while ((line = br.readLine()) != null) {
//                buffer.append(line + "\n");
//            }
//        } catch (UnsupportedEncodingException e) {
//            throw new Exception("不支持的编码字符集异常", e);
//        } catch (IOException e) {
//            throw new Exception("读取指纹失败", e);
//        } finally {
//            IOUtils.close(br);
//            IOUtils.close(isr);
//            IOUtils.close(stdout);
//        }
//        return buffer.toString();
//    }
    public static void execLinuxCommand(String sql) throws Exception {
        RemoteLinuxUtil rec = new RemoteLinuxUtil("172.16.1.196", "xiaogj", "xiaogj2020");
        rec.execute("docker exec -i cs clickhouse-client --multiline --password xiaogj2021 --query=\"" + sql + "\";");
    }
}