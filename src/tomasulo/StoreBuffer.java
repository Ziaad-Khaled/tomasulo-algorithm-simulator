package tomasulo;

public class StoreBuffer {
    int busy ;
    int address;
    String vj;
    String qj;

    public StoreBuffer(int busy, int address, String vj, String qj) {
        this.busy = busy;
        this.address = address;
        this.vj = vj;
        this.qj = qj;
    }

    public int getBusy() {
        return busy;
    }

    public void setBusy(int busy) {
        this.busy = busy;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public String getVj() {
        return vj;
    }

    public void setVj(String vj) {
        this.vj = vj;
    }

    public String getQj() {
        return qj;
    }

    public void setQj(String qj) {
        this.qj = qj;
    }

    @Override
    public String toString() {
        return "StoreBuffer{" +
                "busy=" + busy +
                ", address=" + address +
                ", vj='" + vj + '\'' +
                ", qj='" + qj + '\'' +
                '}';
    }
}
