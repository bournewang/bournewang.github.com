package com.example;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import sun.lwawt.macosx.CSystemTray;

public class WordCount4 {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        ParameterTool paras = ParameterTool.fromArgs(args);
        DataStreamSource<String> stream = env.socketTextStream(paras.get("host"), paras.getInt("port"));
        SingleOutputStreamOperator<WordAndOne> result =
                stream.flatMap(new StringSplitTask()).keyBy("word").sum("count");

        result.print();
        try {
            env.execute("wordcount2");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class StringSplitTask implements FlatMapFunction<String, WordAndOne>{
        @Override
        public void flatMap(String s, Collector<WordAndOne> collector) throws Exception {
            String[] words = s.split(" ");
            for (String word: words) {
                collector.collect(new WordAndOne(word, 1));
            }
        }
    }
    public static class WordAndOne{
        private String word;
        private Integer count;

        public WordAndOne(){}
        public WordAndOne(String word, Integer count) {
            this.word = word;
            this.count = count;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public String getWord() {
            return word;
        }

        public Integer getCount() {
            return count;
        }

        @Override
        public String toString() {
            return "WordAndOne{" +
                    "word='" + word + '\'' +
                    ", count=" + count +
                    '}';
        }
    }

}