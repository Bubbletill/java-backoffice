package store.bubbletill.backoffice.data;

public class TransactionListData {

    private int utid;
    private int store;
    private int register;
    private String date;
    private String time;
    private int trans;
    private TransactionType type;
    private String oper;
    private String items;
    private double total;
    private PaymentType primary_method;

    public int getUtid() {
        return utid;
    }

    public int getStore() {
        return store;
    }

    public int getRegister() {
        return register;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public int getTrans() {
        return trans;
    }

    public TransactionType getType() {
        return type;
    }

    public String getOper() {
        return oper;
    }

    public String getItems() {
        return items;
    }

    public double getTotal() {
        return total;
    }

    public PaymentType getPrimary_method() {
        return primary_method;
    }
}
