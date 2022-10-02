package org.flink.learn;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.util.Collector;

public class NewSourceWordCountJob {

  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();
    environment.addSource(new NewSource())
        .flatMap(new LineSplitter())
        .keyBy(t2 -> t2.getField(0))
        .sum(1)
        .print();
    environment.execute("Java WordCount from SocketTextStream Example");
  }

  static class NewSource implements SourceFunction<String> {

    private String[] words = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k",
        "l", "m", "n"};
    private boolean isRunning = false;
    private int i = 0;

    public NewSource() {
      this.isRunning = true;
    }

    @Override
    public void run(SourceContext<String> ctx) throws Exception {
      while (isRunning) {
        ctx.collect(words[i]);
        i = (i + 1) % words.length;
        Thread.sleep(1000);
      }
    }

    @Override
    public void cancel() {
      this.isRunning = false;
    }
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
