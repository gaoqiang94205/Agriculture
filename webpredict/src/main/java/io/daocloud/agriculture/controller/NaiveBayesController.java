package io.daocloud.agriculture.controller;

import io.daocloud.agriculture.naivebayes.AgricultureNaiveBayes;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author qianggao
 */
@RestController
public class NaiveBayesController {

    @RequestMapping(value = "/bayes")
    public String BayesPredict(String params){

        String[] features = params.split(",");

        double[] d_feature = new double[features.length];

        for(int i=0; i<features.length; i++){
            d_feature[i] = Double.parseDouble(features[i]);
        }
        double result = AgricultureNaiveBayes.predict(d_feature);

        return Double.toString(result);
    }

}
