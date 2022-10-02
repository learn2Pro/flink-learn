package org.flink.learn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

@Slf4j
public class MySqlSinkJob {

  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    DataStreamSource<Student> a = env.addSource(new StudentSource("a"));
    DataStreamSource<Student> b = env.addSource(new StudentSource("b"));
    a.join(b)
        .where(Student::getId)
        .equalTo(Student::getId)
        .window(TumblingProcessingTimeWindows.of(Time.seconds(5)))
        .apply((t0, t1) -> new Student(t0.getId(), t0.getName() + "#" + t1.getName(), t0.getAge()))
        .addSink(new MysqlSink());
    env.execute("MySqlSinkJob");
  }

  private static class StudentSource implements SourceFunction<Student> {

    private boolean isRunning = true;
    private final String prefix;
    private Integer i = 1;

    public StudentSource(String prefix) {
      this.prefix = prefix;
    }

    @Override
    public void run(SourceContext<Student> ctx) throws Exception {
      while (isRunning) {
        ctx.collect(new Student(i, this.prefix + "-" + i.toString(), i));
        i++;
        Thread.sleep(2000);
      }
    }

    @Override
    public void cancel() {
      this.isRunning = false;
    }
  }

  @Data
  private static class Student {

    private final Integer id;
    private final String name;
    private final Integer age;
  }

  private static class MysqlSink extends RichSinkFunction<Student> {

    PreparedStatement ps;
    private Connection connection;

    @Override
    public void open(Configuration parameters) throws Exception {
      super.open(parameters);
      connection = getConnection();
      String sql = "insert into Student(id, name, age) values(?, ?, ?);";
      ps = this.connection.prepareStatement(sql);
    }

    @Override
    public void invoke(Student value, Context context) throws Exception {
      ps.setInt(1, value.getId());
      ps.setString(2, value.getName());
      ps.setInt(3, value.getAge());
      ps.executeUpdate();
    }

    private static Connection getConnection() throws Exception {
      Connection con = null;
      try {
        Class.forName("com.mysql.jdbc.Driver");
        con = DriverManager.getConnection(
            "jdbc:mysql://9.135.90.207:3306/atlas?useUnicode=true&characterEncoding=utf8&useSSL=false&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai",
            "root", "Atlas123456");
        return con;
      } catch (Exception e) {
        log.error("-----------mysql get connection has exception , msg =  ", e);
        throw e;
      }
    }
  }
}
