package io.daocloud.agriculture.naivebayes;


import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.NaiveBayes;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import scala.Tuple2;

// $example off$

public class AgricultureNaiveBayes {

    static SparkConf sparkConf = new SparkConf().setMaster("local[6]").setAppName("JavaNaiveBayesExample");
    static JavaSparkContext jsc = new JavaSparkContext(sparkConf);

    public static void train() {
        String training_data = "/Users/qianggao/Downloads/bigdata/yumi.info";

        JavaRDD<String> data = jsc.textFile(training_data);

        JavaRDD<LabeledPoint> parsedData = data.map(line -> {
            String[] features = line.split(",");
            double[] v = new double[features.length-1];
            for (int i = 0; i < features.length-1; i++) {
                v[i] = Double.parseDouble(features[i]);
            }
            return new LabeledPoint(Double.parseDouble(features[features.length-1]), Vectors.dense(v));
        });

        JavaRDD<LabeledPoint>[] tmp = parsedData.randomSplit(new double[]{0.9, 0.1});

        JavaRDD<LabeledPoint> training = tmp[0];
        JavaRDD<LabeledPoint> test = tmp[1];

        NaiveBayesModel model = NaiveBayes.train(training.rdd(), 1.0);
        JavaPairRDD<Double, Double> predictionAndLabel =
                test.mapToPair(p -> new Tuple2<>(model.predict(p.features()), p.label()));
        double accuracy =
                predictionAndLabel.filter(pl -> pl._1().equals(pl._2())).count() / (double) test.count();

        System.out.println(accuracy);

        // Save and load model
        model.save(jsc.sc(), "/Users/qianggao/Downloads/bigdata/yumimodel");
        NaiveBayesModel sameModel = NaiveBayesModel.load(jsc.sc(), "/Users/qianggao/Downloads/bigdata/yumimodel");

        jsc.stop();
    }
    public static double predict(double[] sam){

        NaiveBayesModel sameModel = NaiveBayesModel.load(jsc.sc(), "/Users/qianggao/Downloads/bigdata/yumimodel");


        Vector sample = Vectors.dense(sam);

        double result = sameModel.predict(sample);

        System.out.println(result);

        return result;

    }

    public static void main(String[] args){
        //train();

        double[] sam = {1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 1, 0, 0, 1, 0};
        predict(sam);
    }

}
