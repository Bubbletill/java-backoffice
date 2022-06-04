package store.bubbletill.backoffice.data;

public class OperatorData {

    private String id;
    private String name;
    private int manager;
    private String posperms;
    private String boperms;

    public String getOperatorId() {
        return id;
    }

    public String getName() {return name;}

    public boolean isManager() {
        return manager == 1;
    }
}
