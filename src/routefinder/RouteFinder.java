package routefinder;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

public class RouteFinder implements IRouteFinder {
    @Override
    public Map<String, List<Long>>
    getBusRouteTripsLengthsInMinutesToAndFromDestination(Map<String, String> destinationBusesMap) {
        HashMap<String, List<Long>> busRoutes = new HashMap<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
        
        for (Map.Entry<String, String> entry :
                destinationBusesMap.entrySet()) {

            String webPage = getUrlsText(entry.getValue());

            Pattern compile = compile("<table .*?</table>");
            Matcher mTable = compile.matcher(webPage);
            
            while (mTable.find()){
                String table = mTable.group();

                Matcher rMatcher = compile("<h2>(.*?)<small>(.*?)</small></h2>")
                        .matcher(table);
                
                
                rMatcher.find();
                
                if(!rMatcher.group(1).equals("Weekday"))
                    break;
                
                String route =  entry.getKey() + " - " + rMatcher.group(2);
                if(!busRoutes.containsKey(route))
                    busRoutes.put(route, new ArrayList<>());
                
                Matcher mTr = 
                        compile("<tr>\\s*<td class.*?</tr>")
                                  .matcher(table);
                
                while (mTr.find()){
                    String tr = mTr.group();
                    Matcher mTimeRow = 
                            compile("\\d{1,2}:\\d\\d (AM|PM)")
                            .matcher(tr);

                    List<MatchResult> matchResults = mTimeRow.results().toList();
                    
                    String from = matchResults.get(0).group();
                    String till = matchResults.get(matchResults.size() - 1).group();

                   
                    try {
                        
                        long fromTime = simpleDateFormat.parse(from).getTime();
                        long tillTime = simpleDateFormat.parse(till).getTime();
                        long time = Duration.ofMillis(tillTime).toMinutes() - 
                                    Duration.ofMillis(fromTime).toMinutes();

                        busRoutes.get(route).add(time);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        
        }


        return busRoutes;
    }

    @Override
    public Map<String, Map<String, String>> getBusRoutesUrls(char destInitial) {
        if(!Character.isAlphabetic(destInitial))
            throw new RuntimeException("Character is no alphabetic");
        
        Map<String, Map<String, String>> busRoutesUrls = new HashMap<>();
        String webPage = getUrlsText(TRANSIT_WEB_URL);

        String regex = "<td><a href=\"(.*?)\".*?>(.*?)</a></td>\\s*?<td>.*? to (.*?)</td>";
        Pattern compile = compile(regex);
        Matcher matcher = compile.matcher(webPage);

        while (matcher.find()) {
            String url = TRANSIT_WEB_URL + matcher.group(1).replaceFirst("/schedules/", "");
            String routeId = matcher.group(2);
            String destination = matcher.group(3);

            if (Character.toLowerCase(destination.charAt(0))
                    != Character.toLowerCase(destInitial)) continue;

            if (busRoutesUrls.containsKey(destination)) {
                busRoutesUrls.get(destination).put(routeId, url);
            } else {
                Map<String, String> map = new HashMap<>();
                map.put(routeId, url);
                busRoutesUrls.put(destination, map);
            }

        }
        return busRoutesUrls;
    }

    private String getUrlsText(String url) {
        StringBuilder webPage = new StringBuilder();

        try {
            URLConnection urlConnection = new URL(url).openConnection();
            Scanner scanner = new Scanner(urlConnection.getInputStream());
            scanner.useDelimiter("\\A");

            boolean isWrite = false;
            
            while (scanner.hasNext()) {
                String line = scanner.nextLine().replaceAll("&amp;", "&");

                if (line.contains("<table")) isWrite = true;
                if (isWrite) webPage.append(line);
                if (line.contains("</table>")) isWrite = false;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return webPage.toString();
    }
}
