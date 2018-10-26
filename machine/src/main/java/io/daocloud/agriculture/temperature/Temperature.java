package io.daocloud.agriculture.temperature;

import com.alibaba.fastjson.JSONObject;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.mqtt.MQTTUtils;
import org.apache.spark.util.StatCounter;
import scala.Tuple2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Temperature {
    public static void receive_topic() throws InterruptedException {
        SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("Agriculture bigdata");
        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(15));
        JavaReceiverInputDStream messages = MQTTUtils.createStream(jssc, "tcp://192.168.2.128:30701", "temperature");

        jssc.checkpoint("/Users/qianggao/IdeaProjects/flink/agricultureiot/tmp");

        JavaDStream location2tems = messages.flatMap(new FlatMapFunction<String, JSONObject>() {
            @Override
            public Iterator<JSONObject> call(String jstring) throws Exception {
                List<JSONObject> arr = new ArrayList<JSONObject>();
                JSONObject obj = JSONObject.parseObject(jstring);

                Object id = obj.get("id");
                Object temperature = obj.get("temperature");
                arr.add(obj);
                return arr.iterator();
            }
        });
        JavaPairDStream location2TemPairDStream = location2tems.mapToPair(new PairFunction<JSONObject, String, Float>() {

            @Override
            public Tuple2<String, Float> call(JSONObject jsonObject) throws Exception {
                String location = (String) jsonObject.get("id");
                BigDecimal temp = (BigDecimal)jsonObject.get("temperature");
                float temperature = temp.floatValue();
                return new Tuple2<String,Float>(location,temperature);
            }
        });
        JavaPairDStream temperatureStature = location2TemPairDStream.updateStateByKey(new Function2<List<Float>, Optional<StatCounter>, Optional<StatCounter>>() {
            @Override
            public Optional<StatCounter> call(List<Float> newTemperatures, Optional<StatCounter> statsYet) throws Exception {
                StatCounter stats = (StatCounter) statsYet.or(new StatCounter());
                for (float temp : newTemperatures) {
                    stats.merge(temp);
                }
                return Optional.of(stats);
            }
        });

        JavaPairDStream<String, Double> averageTemForPeriod = temperatureStature.mapToPair(new PairFunction<Tuple2<String, StatCounter>, String, Double>() {

            @Override
            public Tuple2<String, Double> call(Tuple2<String, StatCounter> statCounterTuple) throws Exception {
                String id = statCounterTuple._1();
                double temp = statCounterTuple._2().mean();
                long time = System.currentTimeMillis();
                PersistentInfluxDB.writeinfo(id, temp,"temperature",time,"mydb");
                return new Tuple2<String, Double>(id, temp);
            }
        });
        averageTemForPeriod.count().print();
        //messages.window(Durations.seconds(30)).count().print();
        jssc.start();
        jssc.awaitTermination();
    }

    public static void main(String[] args) throws InterruptedException {
        receive_topic();
        //long time = System.currentTimeMillis();
        //PersistentInfluxDB.writeinfo("12121",121.121,"temperature",time,"mydb");
    }

}
