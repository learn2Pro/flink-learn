package org.flink.learn;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.flink.learn.entity.Alert;
import org.flink.learn.entity.Transaction;
import org.flink.learn.sink.AlertSink;
import org.flink.learn.source.TransactionSource;

public class FraudDetectionJob {

  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    DataStream<Transaction> transactions = env
        .addSource(new TransactionSource())
        .name("transactions");

    DataStream<Alert> alerts = transactions
        .keyBy(Transaction::getAccountId)
        .process(new FraudDetector())
        .name("fraud-detector");

    alerts
        .addSink(new AlertSink())
        .name("send-alerts");

    env.execute("Fraud Detection");
  }

  private static class FraudDetector extends KeyedProcessFunction<Long, Transaction, Alert> {

    private static final long serialVersionUID = 1L;

    private static final double SMALL_AMOUNT = 1.00;
    private static final double LARGE_AMOUNT = 500.00;
    private static final long ONE_MINUTE = 60 * 1000;
    private transient ValueState<Boolean> flagState;
    private transient ValueState<Long> timerState;

    @Override
    public void open(Configuration parameters) {
      ValueStateDescriptor<Boolean> flagDescriptor = new ValueStateDescriptor<>(
          "flag",
          Types.BOOLEAN);
      flagState = getRuntimeContext().getState(flagDescriptor);
      ValueStateDescriptor<Long> timerDescriptor = new ValueStateDescriptor<>(
          "timer-state",
          Types.LONG);
      timerState = getRuntimeContext().getState(timerDescriptor);

    }

    @Override
    public void processElement(
        Transaction transaction,
        Context context,
        Collector<Alert> collector) throws Exception {

//            Alert alert = new Alert();
//            alert.setId(transaction.getAccountId());
//
//            collector.collect(alert);

      // Get the current state for the current key
      Boolean lastTransactionWasSmall = flagState.value();

      // Check if the flag is set
      if (lastTransactionWasSmall != null) {
        if (transaction.getAmount() > LARGE_AMOUNT) {
          // Output an alert downstream
          Alert alert = new Alert();
          alert.setId(transaction.getAccountId());

          collector.collect(alert);
        }

        // Clean up our state
        cleanUp(context);
      }

      if (transaction.getAmount() < SMALL_AMOUNT) {
        // Set the flag to true
        flagState.update(true);

        // set the timer and timer state
        long timer = context.timerService().currentProcessingTime() + ONE_MINUTE;
        context.timerService().registerProcessingTimeTimer(timer);
        timerState.update(timer);
      }
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<Alert> out) {
      // remove flag after 1 minute
      timerState.clear();
      flagState.clear();
    }

    private void cleanUp(Context ctx) throws Exception {
      // delete timer
      Long timer = timerState.value();
      ctx.timerService().deleteProcessingTimeTimer(timer);

      // clean up all state
      timerState.clear();
      flagState.clear();
    }
  }


}
