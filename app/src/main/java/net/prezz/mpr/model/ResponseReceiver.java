package net.prezz.mpr.model;

public abstract class ResponseReceiver<Response> {
    public void buildingDatabase() {
    }

    public abstract void receiveResponse(Response response);
}
