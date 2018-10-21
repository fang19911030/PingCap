/*We assume IP range doesn't has any overlap, so we we could map this range to a long type number and then use binary
search to get the result. We also use multithread to improve the performance*/

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.*;

public class IPDatabase {
    private TreeMap<Long,Location> Database;
    private ExecutorService executor;


    public IPDatabase(){
        Database = new TreeMap<>();
        executor = Executors.newCachedThreadPool();
    }

    public IPDatabase(String path) throws IOException{
        this();
        loadData(path);
    }

    public void loadData(String path){
        assert(Database != null);
        try {
            FileInputStream fi = new FileInputStream(path);
            InputStreamReader ir = new InputStreamReader(fi, "UTF-8");
            Scanner sc = new Scanner(ir);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] fields = line.split(" ");
                Database.put(IPtoLong(fields[0]), new Location(fields[2], fields[3]));
                Database.put(IPtoLong(fields[1]), new Location(fields[2], fields[3]));
            }
            sc.close();
            ir.close();
            fi.close();
        }catch (IOException e){
            System.out.println(String.format("File: %s doesn't exist", path));
        }

    }

    public Location query(String IP) throws InterruptedException,ExecutionException{
        Future<Location> futureResult = executor.submit(new Query(IP));
        return futureResult.get();
    }

    public List<Location> parallelQuery(List<String> IPs) throws InterruptedException,ExecutionException{
        List<Callable<Location>> jobs = new ArrayList<>();
        for(String IP : IPs) jobs.add(new Query(IP));

        List<Future<Location>> futureResult = executor.invokeAll(jobs);

        List<Location> result = new ArrayList<>();
        for(Future<Location> f : futureResult)
            result.add(f.get());

        return result;

    }

    private long IPtoLong(String IP){
        String[] IPSegments = IP.split("\\.");
        long result = 0;
        for(String IPSegment: IPSegments){
            result += Long.valueOf(IPSegment);
            result <<= 8;
        }
        return result;
    }

    public static void main(String[] args){
        try{
            IPDatabase ipDatabase = new IPDatabase("test");
            System.out.println(ipDatabase.query("111.111.111.233"));
            System.out.println(ipDatabase.query("111.111.222.234"));
            System.out.println(ipDatabase.query("111.222.0.222"));
            System.out.println(ipDatabase.query("111.223.111.222"));
            System.out.println(ipDatabase.query("100.222.111.222"));

            System.out.println("Parallel Query:");
            List<String> queries = new ArrayList<>();
            queries.add("111.111.111.233");
            queries.add("111.111.222.234");
            queries.add("111.222.0.222");
            queries.add("111.223.111.222");
            queries.add("100.222.111.222");
            List<Location> results = ipDatabase.parallelQuery(queries);
            for(Location l : results)
                System.out.println(l);

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /*If the higher entry and the lower entry share the same location, we just found the corresponding location of given ip,
    otherwise the given ip doesn't fall in any given range.*/
    class Query implements Callable {
        String IP;

        public Query(String IP){
            this.IP = IP;
        }

        @Override
        public Location call() {
            long numIP = IPtoLong(IP);
            Map.Entry<Long, Location> lower = Database.floorEntry(numIP);
            Map.Entry<Long, Location> higher = Database.higherEntry(numIP);
            if(lower == null || higher == null)
                return null;
            if(lower.getValue().equals(higher.getValue()))
                return lower.getValue();

            return null;
        }
    }



}