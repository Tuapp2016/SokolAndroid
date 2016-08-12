package sokol.sokolandroid.models;

import java.util.List;

public class UserByRoutes {
    private List<String> routes;

    public UserByRoutes(List<String> routes) {
        this.routes = routes;
    }

    public List<String> getRoutes() {
        return routes;
    }

    public void setRoutes(List<String> routes) {
        this.routes = routes;
    }
}

