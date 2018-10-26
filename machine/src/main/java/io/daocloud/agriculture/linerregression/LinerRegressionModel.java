package io.daocloud.agriculture.linerregression;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.regression.LinearRegressionModel;
import org.apache.spark.mllib.regression.LinearRegressionWithSGD;
import scala.Tuple2;


/**
 * @author qianggao
 */
public class LinerRegressionModel {

    static SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("LinerRegression");
    static JavaSparkContext sc = new JavaSparkContext(conf);

    public static void train() {

        // Load and parse the data
        String path = "/Users/qianggao/Downloads/bigdata/wheat.info";
        JavaRDD<String> data = sc.textFile(path);
        JavaRDD<LabeledPoint> parsedData = data.map(line -> {
            //String[] parts = line.split(",");
            String[] features = line.split(",");
            double[] v = new double[features.length-3];
            for (int i = 0; i < features.length-3; i++) {
                v[i] = Double.parseDouble(features[i+3]);
            }
            return new LabeledPoint(Double.parseDouble(features[2]), Vectors.dense(v));
        });
        parsedData.cache();

        // Building the model
        int numIterations = 100;
        double stepSize = 0.00000001;
        LinearRegressionModel model =
                LinearRegressionWithSGD.train(JavaRDD.toRDD(parsedData), numIterations, stepSize);

        // Evaluate model on training examples and compute training error
        JavaPairRDD<Double, Double> valuesAndPreds = parsedData.mapToPair(point ->
                new Tuple2<>(model.predict(point.features()), point.label()));

        double MSE = valuesAndPreds.mapToDouble(pair -> {
            double diff = pair._1() - pair._2();
            return diff * diff;
        }).mean();
        System.out.println("training Mean Squared Error = " + MSE);

// Save and load model
        model.save(sc.sc(), "/Users/qianggao/Downloads/bigdata/wheatmodel.txt");
        //LinearRegressionModel sameModel = LinearRegressionModel.load(sc.sc(),"/Users/qianggao/Downloads/bigdata/javaLinearRegressionWithSGDModel");
        sc.stop();
    }

    public static double predict(double[] features){

        LinearRegressionModel model = LinearRegressionModel.load(sc.sc(), "/Users/qianggao/Downloads/bigdata/wheatmodel.txt");

        Vector dense = Vectors.dense(features);

        double result = model.predict(dense);

        sc.stop();

        return result;

    }

    public static void main(String[] args){
        //train();
        double[] features = {54,14,37,87,1886};
        double res = predict(features);
        System.out.println(res);
    }

}