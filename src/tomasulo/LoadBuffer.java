package tomasulo;

public class LoadBuffer {
    int busy ;
    int address;

    public LoadBuffer(int busy, int address) {
        this.busy = busy;
        this.address = address;
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

    @Override
    public String toString() {
        return "LoadBuffer{" +
                "busy=" + busy +
                ", address=" + address +
                '}';
    }
}
