package sokol.sokolandroid.models;

import java.util.List;

public class UserByRoutes {
    private List<String> routes;
    private List<String> names;

    public UserByRoutes(){

    }

    public UserByRoutes(List<String> routes, List<String> names) {
        this.routes = routes;
        this.names = names;
    }

    public List<String> getRoutes() {
        return routes;
    }

    public void setRoutes(List<String> routes) {
        this.routes = routes;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }
}
