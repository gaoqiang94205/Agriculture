package io.daocloud.agriculture.temperature;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;

import java.util.concurrent.TimeUnit;

public class PersistentInfluxDB {
    public static void writeinfo(String id, Double temp, String measure, long time, String dbname){
        String default_url = "http://192.168.2.128:30969";
        String url = System.getenv("influxDb_url");
        String username  = System.getenv("influx_username");
        String password = System.getenv("influx_password");
        InfluxDB influx;
        String user=username==null?"daocloud":username;
        String pwd= password==null?"daocloud":password;

        if(url==null || "".equals(url)) {
            influx = InfluxDBFactory.connect(default_url, user, pwd);
        }else{
            influx = InfluxDBFactory.connect(url, user, pwd);
        }

        boolean influxDBstarted = false;

        do {
            try{Pong pong = influx.ping();
                if(!pong.getVersion().equalsIgnoreCase("unknown")){
                    influxDBstarted = true;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        } while(!influxDBstarted);

        influx.enableBatch(1, 100, TimeUnit.MILLISECONDS);

        Point point = Point.measurement(measure).tag("id", id)
                .field("temperature", temp).time(time,TimeUnit.MILLISECONDS).build();

        influx.write(dbname,"24_hours", point);

       /* BatchPoints batchs = BatchPoints.database("mydb").tag("id","11111").
                consistency(InfluxDB.ConsistencyLevel.ALL).build();
        Point p = Point.measurement("temperature").time(System.currentTimeMillis(), TimeUnit.MILLISECONDS).
                field("temperature", 110).build();
        batchs.point(p);
        influx.write(batchs);*/
    }


    public static void main(String[] args){

    }
}
