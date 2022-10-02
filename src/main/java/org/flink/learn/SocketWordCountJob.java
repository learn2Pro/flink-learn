package org.flink.learn;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class SocketWordCountJob {

  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();
    ParameterTool parameterTool = ParameterTool.fromArgs(args);
    String host = args[0];
    Integer port = Integer.parseInt(args[1]);
    environment.socketTextStream(host, port)
        .flatMap(new LineSplitter())
        .keyBy(t2 -> t2.getField(0))
        .sum(1)
        .print();
    environment.execute("Java WordCount from SocketTextStream Example");
  }


  static class LineSplitter implements FlatMapFunction<String, Tuple2<String, Integer>> {

    /**
     * The core method of the FlatMapFunction. Takes an element from the input data set and
     * transforms it into zero, one, or more elements.
     *
     * @param value The input value.
     * @param out   The collector for returning result values.
     * @throws Exception This method may throw exceptions. Throwing an exception will cause the
     *                   operation to fail and may trigger recovery.
     */
    @Override
    public void flatMap(String value, Collector<Tuple2<String, Integer>> out) throws Exception {
      String[] tokens = value.split("[\t|' ']");
      for (String item : tokens) {
        out.collect(Tuple2.of(item, 1));
      }
    }
  }

}
