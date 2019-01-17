package RiotAPI.LeagueServicesStatus;

import RiotAPI.Utils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.web.client.RestTemplate;
import static RiotAPI.ApiConstants.*;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LeagueServicesStatus {

    private String name;
    private List<Services> services;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Services> getServices() {
        return services;
    }

    public void setServices(List<Services> services) {
        this.services = services;
    }

    public LeagueServicesStatus() {
    }

    public String toString() {
        String status = "";
        for(int i = 0; i<services.size(); i++) {
            status+=services.get(i).getName()+": "+services.get(i).getStatus().toUpperCase()+"\n";
        }
        return status;
    }

    public LeagueServicesStatus get(String platform) {
        return new RestTemplate().getForObject(Utils.createURI(platform, SERVICES_STATUS+"?api_key="), LeagueServicesStatus.class);
    }

}
