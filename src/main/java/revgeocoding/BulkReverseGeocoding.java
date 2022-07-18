package revgeocoding;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BulkReverseGeocoding {

    static class ReverseGeocodingTask implements Runnable {

        private final Double lon;

        private final Double lat;

        private final String coords;

        public ReverseGeocodingTask(double lon, double lat, String coords) {
            this.lon = lon;
            this.lat = lat;
            this.coords = coords;
        }

        @Override
        public void run() {
//            http://10.114.132.35:8219/search/v1/geocoding?location=30.76821141164547%2C111.17364815699241&coordinateType=gcj02ll&ApiAuthorization=USER_AK
            System.out.println("running  " + lon + "," + lat + "," + coords);
            RestTemplate restTemplate = new RestTemplate();
            String latLon = lat + "," + lon;
            Map<String, String> paramMap = new HashMap<>(2);
            paramMap.put("latLon", latLon);
            paramMap.put("coords", coords);
            ResponseEntity<String> res = restTemplate.getForEntity(
                    "http://10.114.132.35:8219/search/v1/geocoding?location={latLon}" +
                            "&coordinateType={coords}&ApiAuthorization=USER_AK", String.class, paramMap);

            System.out.println(res);
        }
    }


    public static void main(String[] args) {

        ThreadPoolExecutor executor = new ThreadPoolExecutor(16, 64,
                2, TimeUnit.MINUTES, new ArrayBlockingQueue<>(100));

        // 待处理待坐标点集合
        List<String> lonLatList = Arrays.asList("111.17269544768665,30.742666839787834",
                "111.46245287298514,30.816136955315933");

        // 待处理待坐标点的坐标类型，可选为bd09ll,gcj02ll,wgs84ll
        String coords = "gcj02ll";

        for (String s : lonLatList) {
            double lon = Double.parseDouble(s.split(",")[0]);
            double lat = Double.parseDouble(s.split(",")[1]);
            ReverseGeocodingTask reverseGeocodingTask = new ReverseGeocodingTask(lon, lat, coords);
            System.out.println("creating  " + lon + "," + lat + "," + coords);
            executor.submit(reverseGeocodingTask);
        }

        executor.shutdown();
    }
}
