package io.daocloud.agriculture.controller;

import io.daocloud.agriculture.linerregression.LinerRegressionModel;
import io.daocloud.agriculture.naivebayes.AgricultureNaiveBayes;
import org.junit.runners.Parameterized;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author qianggao
 */
@RestController
public class LinerRegressionPredictController {

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String hello(){


        return "hello";
    }

    @RequestMapping(value = "/liner")
    public String LinerPredict(String params){
        String[] features = params.split(",");

        double[] d_feature = new double[features.length];

        for(int i=0; i<features.length; i++){
            d_feature[i] = Double.parseDouble(features[i]);
        }

        double result = LinerRegressionModel.predict(d_feature);
        return Double.toString(result);
    }



    public static void main(String[] args){
        String p = "12,3,21,232,12,123,213,213";
        String[] s = p.split(",");
        for(int i=0;i<s.length;i++){
            System.out.println(s[i]);
        }

    }

}
